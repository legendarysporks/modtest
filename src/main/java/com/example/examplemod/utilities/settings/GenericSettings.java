package com.example.examplemod.utilities.settings;

import com.example.examplemod.ExampleMod;
import com.example.examplemod.Reference;
import com.example.examplemod.utilities.commands.CommandMethod;
import com.example.examplemod.utilities.commands.GenericCommand;
import net.minecraft.command.ICommandSender;
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

public class GenericSettings {
	private static final String GET = "get";
	private static final String SET = "set";
	private static final String CONFIG_FILE_SUFFIX = ".cfg";
	private final Object target;
	private final Configuration configion;
	private final Map<String, Field> fields = new HashMap<>();
	private final Map<String, Method> getters = new HashMap<>();
	private final Map<String, Method> setters = new HashMap<>();

	public GenericSettings(Object target) {
		this.target = target;
		findSettings();
		configion = null;
	}

	public GenericSettings(Object target, String configFileName, String configVersion) {
		this.target = target;
		findSettings();
		File cfgFile = new File(Loader.instance().getConfigDir(), Reference.MODID + "-" + configFileName + CONFIG_FILE_SUFFIX);
		boolean isNewConfigFile = !cfgFile.exists();
		configion = new Configuration(cfgFile, configVersion);
		load();
		if (isNewConfigFile) {
			configion.save();
		}
	}

	/*
	go through the class of the target object and pull all the public fields and methods marked with the
	@Setting annotation and put them in the appropriate maps
	 */
	private void findSettings() {
		// find fields with one of the supported types
		Class clazz = target.getClass();
		while (clazz != Object.class) {
			for (Field field : clazz.getDeclaredFields()) {
				Annotation settingAnnotation = field.getAnnotation(Setting.class);
				if (settingAnnotation != null && Modifier.isPublic(field.getModifiers())) {
					Class<?> fieldType = field.getType();
					if (isSupportedType(fieldType)) {
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
							&& isSupportedType(method.getReturnType())
							&& (method.getParameterCount() == 0)) {
						getters.put(methodName.substring(GET.length()).toLowerCase(), method);
					} else if (methodName.startsWith(SET)
							&& (Void.TYPE == method.getReturnType())
							&& (method.getParameterCount() == 1)
							&& (isSupportedType(method.getParameterTypes()[0]))) {
						setters.put(methodName.substring(SET.length()).toLowerCase(), method);
					}
				}
			}

			// now loop back and check its superclass
			clazz = clazz.getSuperclass();
		}
	}

	private boolean isSupportedType(Class<?> type) {
		return type.isPrimitive() || (type == String.class);
	}

	public void load() {
		if (configion != null) {
			configion.load();
			for (String setting : getSettingNames()) {
				String propertyValue = "";
				try {
					Property property = configion.get(Configuration.CATEGORY_GENERAL, setting, get(setting));
					propertyValue = property.getString();
					set(setting, propertyValue);
				} catch (SettingNotFoundException e) {
					// this shouldn't happen since we only iterate over settings we know
				} catch (InvalidValueException e) {
					ExampleMod.logInfo(String.format("Invalid configion key: '%s' value: '%s' ignored", setting, propertyValue));
				}
			}
		}
	}

	public void save() {
		if (configion != null) {
			configion.save();
		}
	}

	public boolean hasGettableSetting(String settingName) {
		return fields.containsKey(settingName) || getters.containsKey(settingName);
	}

	public boolean hasSettableSetting(String settingName) {
		return fields.containsKey(settingName) || setters.containsKey(settingName);
	}

	public String get(String settingName) throws SettingNotFoundException {
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

	public boolean set(String settingName, String value) throws SettingNotFoundException, InvalidValueException {
		settingName = settingName.toLowerCase();
		Field field = fields.get(settingName);
		if (field != null) {
			try {
				field.setAccessible(true);
				field.set(target, convertValueToType(settingName, value, field.getType()));
				updateConfigFile(settingName, value);
				return true;
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
					method.invoke(target, convertValueToType(settingName, value, method.getParameterTypes()[0]));
					updateConfigFile(settingName, value);
					return true;
				} catch (IllegalAccessException | InvocationTargetException e) {
					// should not happen since we setAccessible(true) above
					e.printStackTrace();
				} finally {
					method.setAccessible(false);
				}
			}
		}
		throw new SettingNotFoundException(settingName);
	}

	public void markDirty(String settingName) {
		try {
			updateConfigFile(settingName, get(settingName));
		} catch (SettingNotFoundException e) {
			e.printStackTrace();
		}
	}

	private void updateConfigFile(String settingName, String value) {
		if (configion != null) {
			Property property = configion.get(Configuration.CATEGORY_GENERAL, settingName, value);
			property.set(value);
			configion.save();
		}
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

	private Object convertValueToType(String settingName, String value, Class<?> type) throws InvalidValueException {
		try {
			if (type == String.class) {
				return value;
			} else if (type == boolean.class || type == Boolean.TYPE) {
				return Boolean.parseBoolean(value);
			} else if (type == Character.class || type == Character.TYPE) {
				return (value.length() == 0) ? "" : value.charAt(0);
			} else if (type == Byte.class || type == Byte.TYPE) {
				return Byte.parseByte(value);
			} else if (type == Short.class || type == Short.TYPE) {
				return Short.parseShort(value);
			} else if (type == Integer.class || type == Integer.TYPE) {
				return Integer.parseInt(value);
			} else if (type == Long.class || type == Long.TYPE) {
				return Long.parseLong(value);
			} else if (type == float.class || type == Float.TYPE) {
				return Float.parseFloat(value);
			} else if (type == double.class || type == Double.TYPE) {
				return Double.parseDouble(value);
			} else {
				// this should never happen if the locateSettings method works correctly
				throw new InvalidValueException(settingName, value, "Value '" + value + "' is not a " + type.getName());
			}
		} catch (NumberFormatException e) {
			throw new InvalidValueException(settingName, value, "Value '" + value + "' is not a number");
		}
	}

	public static class SettingNotFoundException extends Exception {
		public final String settingName;

		public SettingNotFoundException(String name) {
			super("Setting '" + name + "' not found");
			settingName = name;
		}
	}

	public static class InvalidValueException extends Exception {
		public final String settingName;
		public final String value;

		public InvalidValueException(String name, String value, String message) {
			super(message);
			this.settingName = name;
			this.value = value;
		}
	}

	@CommandMethod(help = "List avialable settings")
	public void doSettings(ICommandSender sender) {
		GenericCommand.sendMsg(sender, "settings: " + getSettingNames());
	}

	@CommandMethod(help = "Get the value of a setting:  'get <settingName>'")
	public void doGet(ICommandSender sender, String setting) {
		try {
			GenericCommand.sendMsg(sender, setting + " = " + get(setting));
		} catch (SettingNotFoundException e) {
			GenericCommand.sendMsg(sender, e.getMessage());
		}
	}

	@CommandMethod(help = "Set the value of a setting: 'set <settingName> <value>'")
	public void doSet(ICommandSender sender, String setting, String value) {
		try {
			set(setting, value);
			GenericCommand.sendMsg(sender, setting + " set to " + value);
		} catch (InvalidValueException | SettingNotFoundException e) {
			GenericCommand.sendMsg(sender, e.getMessage());
		}
	}

	public static class GenericCommandWithSettings extends GenericCommand {
		private final GenericSettings settings;

		public GenericCommandWithSettings(String name, String usage, String[] aliases, Object target) {
			super(name, usage, aliases);
			settings = new GenericSettings(target, name, "1.0");
		}

		@CommandMethod(help = "List avialable settings")
		public void doSettings(ICommandSender sender) {
			sendMsg(sender, getName() + " settings: " + settings.getSettingNames());
		}

		@CommandMethod(help = "Get the value of a setting:  'get <settingName>'")
		public void doGet(ICommandSender sender, String setting) {
			try {
				sendMsg(sender, getName() + "." + setting + " = " + settings.get(setting));
			} catch (SettingNotFoundException e) {
				sendMsg(sender, e.getMessage());
			}
		}

		@CommandMethod(help = "Set the value of a setting: 'set <settingName> <value>'")
		public void doSet(ICommandSender sender, String setting, String value) {
			try {
				settings.set(setting, value);
				sendMsg(sender, getName() + "." + setting + " set to " + value);
			} catch (InvalidValueException | SettingNotFoundException e) {
				sendMsg(sender, e.getMessage());
			}
		}

		public void markDirty(String settingName) {
			settings.markDirty(settingName);
		}
	}
}

