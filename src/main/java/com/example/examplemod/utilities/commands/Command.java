package com.example.examplemod.utilities.commands;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Command {
	String help() default "";

	boolean requiresOp() default false;
}
