package com.example.examplemod.utilities.settings;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Setting {
	String defaultValue() default "";
}

