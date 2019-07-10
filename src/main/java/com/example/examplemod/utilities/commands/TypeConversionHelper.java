package com.example.examplemod.utilities.commands;

class TypeConversionHelper {
	public static boolean isSupportedType(Class<?> type) {
		return type.isPrimitive()
				|| (type == Boolean.class)
				|| (type == Character.class)
				|| (type == Byte.class)
				|| (type == Short.class)
				|| (type == Integer.class)
				|| (type == Long.class)
				|| (type == Float.class)
				|| (type == Double.class)
				|| (type == String.class);
	}

	public static Object convertStringToType(String purpose, String value, Class<?> type) throws InvalidValueException {
		try {
			if (type == String.class) {
				return value;
			} else if (type == Boolean.class || type == Boolean.TYPE) {
				return Boolean.parseBoolean(value);
			} else if (type == Character.class || type == Character.TYPE) {
				return value.charAt(0);
			} else if (type == Byte.class || type == Byte.TYPE) {
				return Byte.parseByte(value);
			} else if (type == Short.class || type == Short.TYPE) {
				return Short.parseShort(value);
			} else if (type == Integer.class || type == Integer.TYPE) {
				return Integer.parseInt(value);
			} else if (type == Long.class || type == Long.TYPE) {
				return Long.parseLong(value);
			} else if (type == Float.class || type == Float.TYPE) {
				return Float.parseFloat(value);
			} else if (type == Double.class || type == Double.TYPE) {
				return Double.parseDouble(value);
			} else {
				// this should never happen if the locateSettings method works correctly
				throw new InvalidValueException(purpose, value, "Value '" + value + "' is not a " + type.getName());
			}
		} catch (NumberFormatException e) {
			throw new InvalidValueException(purpose, value, "Value '" + value + "' is not a number");
		}
	}
}
