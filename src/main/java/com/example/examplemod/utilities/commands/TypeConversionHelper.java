package com.example.examplemod.utilities.commands;

import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class TypeConversionHelper {
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

	public static Object convertStringToTypeOrNull(String purpose, String value, Class<?> type) {
		try {
			return convertStringToType(purpose, value, type);
		} catch (InvalidValueException e) {
			return null;
		}
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

	public static void convertStringToCollection(String purpose, String value, Class<?> valueType, Collection result) throws InvalidValueException {
		String data = value.substring(1, value.length() - 1).trim();     // strip off enclosing {}
		if (data.length() > 0) {
			String[] elements = data.split(",");
			for (String element : elements) {
				element.trim();
				result.add(convertStringToType(purpose, element, valueType));
			}
		}
	}

	public static void convertStringToMap(String purpose, String value, Class<?> valueType, Map result) throws InvalidValueException {
		String data = value.substring(1, value.length() - 1);     // strip off enclosing {}
		if (data.length() > 0) {
			String[] elements = data.split(",");
			for (String element : elements) {
				element.trim();
				String[] kv = element.split("=");
				kv[0] = kv[0].trim();
				kv[1] = kv[1].trim();
				result.put(kv[0], convertStringToType(purpose, kv[1], valueType));
			}
		}
	}

	public static <P, R> Collection<R> convertContents(Collection<P> dataIn, Collection<R> dataOut, Function<P, R> func) {
		dataOut.clear();
		dataOut.addAll(dataIn.stream().map(func).filter(x -> x != null).sorted().collect(Collectors.toList()));
		return dataOut;
	}
}