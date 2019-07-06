package com.example.examplemod.utilities.commands;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Setting {
	String defaultValue() default "";
}

