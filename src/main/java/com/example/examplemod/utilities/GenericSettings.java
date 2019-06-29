package com.example.examplemod.utilities;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

public class GenericSettings {
	private static final String GET = "get";
	private static final String SET = "set";
	private final Object target;
	private final Map<String, Field> fields = new HashMap<>();
	private final Map<String, Method> getters = new HashMap<>();
	private final Map<String, Method> setters = new HashMap<>();

	public static class SettingNotFoundException extends Exception {
		public final String settingName;

		public SettingNotFoundException(String name) {
			settingName = name;
		}
	}

	public GenericSettings(Object target) {
		this.target = target;
		findSettings();
	}

	private void findSettings() {
		// find fields with one of the supported types
		Class clazz = target.getClass();
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

		// find methods of the form <supportedType> get<bla>()
		for (Method method : clazz.getDeclaredMethods()) {
			Annotation settingAnnotation = method.getAnnotation(Setting.class);
			if (settingAnnotation != null && Modifier.isPublic(method.getModifiers())) {
				String methodName = method.getName();
				if (methodName.startsWith(GET)
						&& isSupportedType(method.getReturnType())
						&& (method.getParameterCount() == 0)) {
					getters.put(methodName.substring(GET.length()).toLowerCase(), method);
				}
			}
		}

		// find methods of the form void set<bla>(<supportedType> param)
		for (Method method : clazz.getDeclaredMethods()) {
			Annotation settingAnnotation = method.getAnnotation(Setting.class);
			if (settingAnnotation != null && Modifier.isPublic(method.getModifiers())) {
				String methodName = method.getName();
				if (methodName.startsWith(SET)
						&& (Void.TYPE == method.getReturnType())
						&& (method.getParameterCount() == 1)
						&& (isSupportedType(method.getParameterTypes()[0]))) {
					setters.put(methodName.substring(SET.length()).toLowerCase(), method);
				}
			}
		}
	}

	private boolean isSupportedType(Class<?> type) {
		return type.isPrimitive() || (type == String.class);
	}

	public boolean hasGettableSetting(String settingName) {
		return fields.containsKey(settingName) || getters.containsKey(settingName);
	}

	public boolean hasSettableSetting(String settingName) {
		return fields.containsKey(settingName) || setters.containsKey(settingName);
	}

	public String get(String settingName) throws SettingNotFoundException {
		Field field = fields.get(settingName);
		if (field != null) {
			try {
				return field.get(target).toString();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		} else {
			Method getter = getters.get(settingName);
			if (getter != null) {
				try {
					return getter.invoke(target).toString();
				} catch (IllegalAccessException | InvocationTargetException e) {
					e.printStackTrace();
				}
			}
		}
		throw new SettingNotFoundException(settingName);
	}

	public boolean set(String settingName, String value) throws SettingNotFoundException, IllegalArgumentException {
		Field field = fields.get(settingName);
		if (field != null) {
			try {
				field.set(target, convertValueToType(value, field.getType()));
				return true;
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		} else {
			Method method = setters.get(settingName);
			if (method != null) {
				try {
					method.invoke(target, convertValueToType(value, method.getParameterTypes()[0]));
					return true;
				} catch (IllegalAccessException | InvocationTargetException e) {
					e.printStackTrace();
				}
			}
		}
		throw new SettingNotFoundException(settingName);
	}

	private Object convertValueToType(String value, Class<?> type) {
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
				throw new IllegalArgumentException("Can't handle settings of type: " + type.getName());
			}
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException("Argument is not a number");
		}
	}

	@Retention(RetentionPolicy.RUNTIME)
	public @interface Setting {
		String defaultValue() default "";
	}
}

