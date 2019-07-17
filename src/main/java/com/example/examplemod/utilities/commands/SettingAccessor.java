package com.example.examplemod.utilities.commands;

import com.example.examplemod.Reference;
import com.example.examplemod.utilities.Logging;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.common.Loader;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

// NOTE: this class is only public for testing purposes
public class SettingAccessor {
	private static final String GET = "get";
	private static final String SET = "set";
	private static final String CONFIG_FILE_SUFFIX = ".cfg";
	private final Object target;
	private final Configuration configuration;
	private final Map<String, Field> fields = new HashMap<>();
	private final Map<String, Method> getters = new HashMap<>();
	private final Map<String, Method> setters = new HashMap<>();

	public SettingAccessor(Object target) {
		this.target = target;
		buildSettingsMaps();
		configuration = null;
	}

	public SettingAccessor(Object target, String configFileName, String configVersion) {
		this.target = target;
		buildSettingsMaps();
		File cfgFile = new File(Loader.instance().getConfigDir(), Reference.MODID + "-" + configFileName + CONFIG_FILE_SUFFIX);
		configuration = new Configuration(cfgFile, configVersion);
		load();
	}

	public void load() {
		if (configuration != null) {
			configuration.load();
			for (String setting : getSettingNames()) {
				String propertyValue = "";
				try {
					propertyValue = getConfigurationFileValue(setting);
					setInstanceValue(setting, propertyValue);
				} catch (SettingNotFoundException | InvalidValueException e) {
					// this should only happen if there is an invalid / non-convertable value
					Logging.logInfo(String.format("Invalid configuration key: '%s' value: '%s' ignored", setting, propertyValue));
				}
			}
		}
	}

	public void save() {
		if (configuration != null) {
			for (String setting : getSettingNames()) {
				if (hasSettableSetting(setting)) {
					String propertyValue = "";
					try {
						propertyValue = getInstanceValue(setting);
						setConfigurationFileValue(setting, propertyValue);
					} catch (SettingNotFoundException e) {
						// this should probably never happen
						Logging.logInfo(String.format("Invalid configuration key: '%s' value: '%s' ignored", setting, propertyValue));
					}
				}
			}
			configuration.save();
		}
	}

	public boolean hasGettableSetting(String settingName) {
		return fields.containsKey(settingName) || getters.containsKey(settingName);
	}

	public boolean hasSettableSetting(String settingName) {
		return fields.containsKey(settingName) || setters.containsKey(settingName);
	}

	public String get(String settingName) throws SettingNotFoundException {
		// we assume the truth is stored in memory.
		return getInstanceValue(settingName);
	}

	public void set(String settingName, String value) throws SettingNotFoundException, InvalidValueException {
		// we assume the truth is stored in memory.  The configuration file is only updated on save().
		setInstanceValue(settingName, value);
	}

	public List<String> getSettingNames() {
		Set<String> set = new HashSet<>();
		set.addAll(fields.keySet());
		set.addAll(getters.keySet());
		set.addAll(setters.keySet());
		List<String> result = new ArrayList<>(set.size());
		result.addAll(set);
		Collections.sort(result);
		return result;
	}

	public Map<String, String> getSettings() {
		Map<String, String> result = new HashMap<>();
		for (String setting : getSettingNames()) {
			try {
				result.put(setting, getInstanceValue(setting));
			} catch (SettingNotFoundException e) {
				// this should probably never happen
				e.printStackTrace();
			}
		}
		return result;
	}

	public void setSettings(Map<String, String> settingsMapIn) {
		for (String setting : getSettingNames()) {
			try {
				setInstanceValue(setting, settingsMapIn.get(setting));
			} catch (SettingNotFoundException | InvalidValueException e) {
				// this should probably never happen
				e.printStackTrace();
			}
		}
	}

	//------------------------------------------------------------------------------------------
	// These methods actually munge the instance variables

	/*
	Pull all the public fields and methods from target marked with the
	@Setting annotation and put them in the appropriate maps
	 */
	private void buildSettingsMaps() {
		// find fields with one of the supported types
		Class clazz = target.getClass();
		while (clazz != Object.class) {
			for (Field field : clazz.getDeclaredFields()) {
				Annotation settingAnnotation = field.getAnnotation(Setting.class);
				if (settingAnnotation != null && Modifier.isPublic(field.getModifiers())) {
					Class<?> fieldType = field.getType();
					if (TypeConversionHelper.isSupportedType(fieldType)) {
						String name = field.getName();
						fields.put(name.toLowerCase(), field);
					}
				}
			}

			// find methods of the form <supportedType> get<bla>() and
			// methods of the form void set<bla>(<supportedType> param)
			for (Method method : clazz.getDeclaredMethods()) {
				Annotation settingAnnotation = method.getAnnotation(Setting.class);
				if (settingAnnotation != null && Modifier.isPublic(method.getModifiers())) {
					String methodName = method.getName();
					if (methodName.startsWith(GET)
							&& TypeConversionHelper.isSupportedType(method.getReturnType())
							&& (method.getParameterCount() == 0)) {
						getters.put(methodName.substring(GET.length()).toLowerCase(), method);
					} else if (methodName.startsWith(SET)
							&& (Void.TYPE == method.getReturnType())
							&& (method.getParameterCount() == 1)
							&& (TypeConversionHelper.isSupportedType(method.getParameterTypes()[0]))) {
						setters.put(methodName.substring(SET.length()).toLowerCase(), method);
					}
				}
			}

			// now loop back and check its superclass
			clazz = clazz.getSuperclass();
		}
	}

	private String getInstanceValue(String settingName) throws SettingNotFoundException {
		settingName = settingName.toLowerCase();
		Field field = fields.get(settingName);
		if (field != null) {
			try {
				field.setAccessible(true);
				return field.get(target).toString();
			} catch (IllegalAccessException e) {
				// should not happen since we setAccessible(true) above
				e.printStackTrace();
			} finally {
				field.setAccessible(false);
			}
		} else {
			Method getter = getters.get(settingName);
			if (getter != null) {
				try {
					getter.setAccessible(true);
					return getter.invoke(target).toString();
				} catch (IllegalAccessException | InvocationTargetException e) {
					// should not happen since we setAccessible(true) above
					e.printStackTrace();
				} finally {
					getter.setAccessible(false);
				}
			}
		}
		throw new SettingNotFoundException(settingName);
	}

	private void setInstanceValue(String settingName, String value) throws SettingNotFoundException, InvalidValueException {
		settingName = settingName.toLowerCase();
		Field field = fields.get(settingName);
		if (field != null) {
			try {
				field.setAccessible(true);
				field.set(target, TypeConversionHelper.convertStringToType(settingName, value, field.getType()));
				return;
			} catch (IllegalAccessException e) {
				// should not happen since we setAccessible(true) above
				e.printStackTrace();
			} finally {
				field.setAccessible(false);
			}
		} else {
			Method method = setters.get(settingName);
			if (method != null) {
				try {
					method.setAccessible(true);
					method.invoke(target, TypeConversionHelper.convertStringToType(settingName, value, method.getParameterTypes()[0]));
					return;
				} catch (IllegalAccessException | InvocationTargetException | InvalidValueException e) {
					// should not happen since we setAccessible(true) above
					e.printStackTrace();
				} finally {
					method.setAccessible(false);
				}
			}
		}
		throw new SettingNotFoundException(settingName);
	}

	//------------------------------------------------------------------------------------------
	// These methods actually munge the configuration file values

	private String getConfigurationFileValue(String settingName) throws SettingNotFoundException {
		return getConfigurationFileValue(Configuration.CATEGORY_GENERAL, settingName);
	}

	private String getConfigurationFileValue(String category, String settingName) throws SettingNotFoundException {
		assert configuration != null;
		return configuration.get(category, settingName, get(settingName)).getString();
	}

	private void setConfigurationFileValue(String settingName, String value) {
		setConfigurationFileValue(Configuration.CATEGORY_GENERAL, settingName, value);
	}

	private void setConfigurationFileValue(String category, String settingName, String value) {
		assert configuration != null;
		assert hasSettableSetting(settingName);
		Property property = configuration.get(category, settingName, value);
		property.set(value);
	}
}

