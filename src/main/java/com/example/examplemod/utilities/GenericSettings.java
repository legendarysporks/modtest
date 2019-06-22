package com.example.examplemod.utilities;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class GenericSettings {
	private final Object target;
	private final Map<String, Field> settings = new HashMap<>();

	@Retention(RetentionPolicy.RUNTIME)
	public @interface Setting {
		String defaultValue() default "";
	}

	public GenericSettings(Object target) {
		this.target = target;
	}

	private void locateSettings() {
		Class clazz = target.getClass();
		for (Field field : clazz.getDeclaredFields()) {
/*			try {
				field.setAccessible(true);
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} finally {
				field.setAccessible(false);
			} */

		}
	}


}
