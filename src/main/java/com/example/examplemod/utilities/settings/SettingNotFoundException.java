package com.example.examplemod.utilities.settings;

public class SettingNotFoundException extends Exception {
	public final String settingName;

	public SettingNotFoundException(String name) {
		super("Setting '" + name + "' not found");
		settingName = name;
	}
}
