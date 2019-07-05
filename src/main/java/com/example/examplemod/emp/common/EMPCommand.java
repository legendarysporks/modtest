package com.example.examplemod.emp.common;

import com.example.examplemod.utilities.settings.GenericSettings.GenericCommandWithSettings;

public class EMPCommand extends GenericCommandWithSettings {
	public static final String NAME = "EMP";
	public static final String USAGE = "EMP whatever";
	public static final String[] ALIASES = {"emp"};

	public EMPCommand(EMPGun gun) {
		super(NAME, USAGE, ALIASES, gun);
	}
}
