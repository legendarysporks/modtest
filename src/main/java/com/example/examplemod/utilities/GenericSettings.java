package com.example.examplemod.utilities;

import scala.annotation.meta.field;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class GenericSettings {
	private static final String GET = "get";
	private static final String SET = "set";
	private final Object target;
	private final Map<String, Field> settingFields = new HashMap<>();
	private final Map<String, Method> settingGetters = new HashMap<>();
	private final Map<String, Method> settingSetters = new HashMap<>();

	@Retention(RetentionPolicy.RUNTIME)
	public @interface Setting {
		String defaultValue() default "";
	}

	public GenericSettings(Object target) {
		this.target = target;
		findSettings();
	}

	private boolean isSupportedType(Class<?> type) {
		return type.isPrimitive() || (type == String.class);
	}

	private void findSettings() {
		// find fields with one of the supported types
		Class clazz = target.getClass();
		for (Field field : clazz.getDeclaredFields()) {
			Annotation settingAnnotation = field.getAnnotation(Setting.class);
			if (settingAnnotation != null) {
				Class<?> fieldType = field.getType();
				if (isSupportedType(fieldType)) {
					settingFields.put(field.getName(), field);
				}
			}
		}

		// find methods of the form <supportedType> get<bla>()
		for (Method method : clazz.getDeclaredMethods()) {
			Annotation settingAnnotation = method.getAnnotation(Setting.class);
			if (settingAnnotation != null) {
				String methodName = method.getName();
				if (methodName.startsWith(GET)
						&& isSupportedType(method.getReturnType())
						&& (method.getParameterCount() == 0)) {
					settingGetters.put(methodName.substring(GET.length()).toLowerCase(), method);
				}
			}
		}

		// find methods of the form void set<bla>(<supportedType> param)
		for (Method method : clazz.getDeclaredMethods()) {
			Annotation settingAnnotation = method.getAnnotation(Setting.class);
			if (settingAnnotation != null) {
				String methodName = method.getName();
				if (methodName.startsWith(SET)
						&& (Void.class == method.getReturnType())
						&& (method.getParameterCount() == 1)
						&& (isSupportedType(method.getParameterTypes()[0]))) {
					settingSetters.put(methodName.substring(SET.length()).toLowerCase(), method);
				}
			}
		}
	}

	public String get(String settingName) {
		Field field = settingFields.get(settingName);
		if (field != null) {
			try {
				field.setAccessible(true);
				return field.get(target).toString();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} finally {
				field.setAccessible(false);
			}
		} else {
			Method getter = settingGetters.get(settingName);
			if (getter != null ) {
				try {
					return getter.invoke(target).toString();
				} catch (IllegalAccessException | InvocationTargetException e) {
					e.printStackTrace();
				}
			}
		}
		// TODO this should throw an exception of some kind so we can distinguish a null setting and a missing setting
		return null;
	}

	public boolean set(String settingName, String value) {
		Field field = settingFields.get(settingName);
		if (field != null) {
			try {
				field.setAccessible(true);
				field.set(target , convertValueToType(value, field.getType()));
				return true;
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} finally {
				field.setAccessible(false);
			}
		} else {
			Method method = settingSetters.get(settingName);
			if (method != null) {
				try {
					method.setAccessible(true);
					method.invoke(target , convertValueToType(value, field.getType()));
					return true;
				} catch (IllegalAccessException | InvocationTargetException e) {
					e.printStackTrace();
				} finally {
					field.setAccessible(false);
				}
				return true;
			}
		}
		return false;
	}

	private Object convertValueToType(String value, Class<?> type) {
		if (type == String.class) {
			return value;
		} else if (type == boolean.class) {
			return Boolean.parseBoolean(value);
		} else if (type == Character.class) {
			return (value.length() == 0) ? "" : value.charAt(0);
		} else if (type == Byte.class) {
			return Byte.parseByte(value);
		} else if (type == Short.class) {
			return Short.parseShort(value);
		} else if (type == Integer.class) {
			return Integer.parseInt(value);
		} else if (type == Long.class) {
			return Long.parseLong(value);
		} else if (type == float.class) {
			return Float.parseFloat(value);
		} else if (type == double.class) {
			return Double.parseDouble(value);
		} else {
			// this should never happen if the locateSettings method works correctly
			throw new IllegalArgumentException("Can't handle settings of type: " + type.getName());
		}
	}
}

