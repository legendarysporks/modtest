package com.example.examplemod.utilities.commands;

public class InvalidValueException extends Exception {
	public final String settingName;
	public final String value;

	public InvalidValueException(String name, String value, String message) {
		super(message);
		this.settingName = name;
		this.value = value;
	}
}
