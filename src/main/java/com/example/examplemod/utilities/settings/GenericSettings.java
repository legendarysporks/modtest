package com.example.examplemod.utilities.settings;

import com.example.examplemod.utilities.commands.CommandMethod;
import com.example.examplemod.utilities.commands.GenericCommand;
import net.minecraft.command.ICommandSender;

import java.util.*;

public class GenericSettings {
	private List<SettingAccessor> settings = new ArrayList<>();

	public GenericSettings() {
	}

	public GenericSettings(Object target) {
		settings.add(new SettingAccessor(target));
	}

	public GenericSettings(Object target, String configFileName, String configVersion) {
		settings.add(new SettingAccessor(target, configFileName, configVersion));
	}

	public static GenericSettings create() {
		return new GenericSettings();
	}

	public GenericSettings withTarget(Object target) {
		settings.add(new SettingAccessor(target));
		return this;
	}

	public GenericSettings withTargetAndConfig(Object target, String configFileName, String configVersion) {
		settings.add(new SettingAccessor(target, configFileName, configVersion));
		return this;
	}

	public void load() {
		for (SettingAccessor accessor : settings) {
			accessor.load();
		}
	}

	public void save() {
		for (SettingAccessor accessor : settings) {
			accessor.save();
		}
	}

	public boolean hasGettableSetting(String settingName) {
		for (SettingAccessor accessor : settings) {
			if (accessor.hasGettableSetting(settingName)) {
				return true;
			}
		}
		return false;
	}

	public boolean hasSettableSetting(String settingName) {
		for (SettingAccessor accessor : settings) {
			if (accessor.hasSettableSetting(settingName)) {
				return true;
			}
		}
		return false;
	}

	public String get(String settingName) throws SettingNotFoundException {
		for (SettingAccessor accessor : settings) {
			try {
				return accessor.get(settingName);
			} catch (SettingNotFoundException e) {
			}
		}
		throw new SettingNotFoundException(settingName);
	}

	public boolean set(String settingName, String value) throws SettingNotFoundException, InvalidValueException {
		for (SettingAccessor accessor : settings) {
			try {
				return accessor.set(settingName, value);
			} catch (SettingNotFoundException | InvalidValueException e) {
			}
		}
		throw new SettingNotFoundException(settingName);
	}

	public List<String> getSettingNames() {
		Set<String> set = new HashSet<>();
		for (SettingAccessor accessor : settings) {
			set.addAll(accessor.getSettingNames());
		}
		List<String> result = new ArrayList<>(set.size());
		result.addAll(set);
		Collections.sort(result);
		return result;
	}

	//--------------------------------------------------------------------------------
	// Command handler methods

	@CommandMethod(help = "List avialable settings")
	public void doSettings(ICommandSender sender) {
		GenericCommand.sendMsg(sender, "settings: " + getSettingNames());
	}

	@CommandMethod(help = "Get the value of a setting:  'get <settingName>'")
	public void doGet(ICommandSender sender, String setting) {
		try {
			GenericCommand.sendMsg(sender, setting + " = " + get(setting));
		} catch (SettingNotFoundException e) {
			GenericCommand.sendMsg(sender, e.getMessage());
		}
	}

	@CommandMethod(help = "Set the value of a setting: 'set <settingName> <value>'")
	public void doSet(ICommandSender sender, String setting, String value) {
		try {
			set(setting, value);
			GenericCommand.sendMsg(sender, setting + " set to " + value);
		} catch (InvalidValueException | SettingNotFoundException e) {
			GenericCommand.sendMsg(sender, e.getMessage());
		}
	}
}

