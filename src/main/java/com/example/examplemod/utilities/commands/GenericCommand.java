package com.example.examplemod.utilities.commands;

import com.example.examplemod.Reference;
import com.example.examplemod.utilities.Logging;
import com.example.examplemod.utilities.hackfmlevents.HackFMLEventListener;
import com.google.common.collect.Lists;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppingEvent;

import java.util.*;

public class GenericCommand implements ICommand, HackFMLEventListener {
	private static final int LINE_WRAP_LENGTH = 160;
	private static final List<GenericCommand> commands = new ArrayList<>();
	private static GenericCommand moduleCommand = null;
	private final List<String> aliasList = new ArrayList<>();
	private final String usage;
	private final String name;
	private final List<MethodDispatcher> dispatchers = new ArrayList<>();
	private final List<SettingAccessor> settings = new ArrayList<>();

	public GenericCommand(String name, String usage, String... aliases) {
		this.dispatchers.add(new MethodDispatcher(this));
		this.name = name;
		this.usage = usage;
		aliasList.addAll(Arrays.asList(aliases));
		subscribeToFMLEvents();
	}

	public static GenericCommand create(String name, String usage) {
		return create(name, usage, new String[0]);
	}

	public static GenericCommand create(String name, String usage, String... aliases) {
		if (moduleCommand == null) {
			// create a module command when the first command is created.
			moduleCommand = new GenericCommand(Reference.MODID, "/" + Reference.MODID, Reference.MODCOMMAND);
			moduleCommand.addTarget(new ModuleCommand());
		}
		GenericCommand cmd = new GenericCommand(name, usage, aliases);
		commands.add(cmd);
		return cmd;
	}

	public static void sendMsg(ICommandSender sender, String msg) {
		sender.sendMessage(new TextComponentString(msg));
	}

	public static void sendMsg(ICommandSender sender, Collection<String> words) {
		StringBuilder line = new StringBuilder();
		for (String word : words) {
			line.append(word);
			line.append(" ");
			if (line.length() > LINE_WRAP_LENGTH) {
				sendMsg(sender, line.toString());
				line = new StringBuilder();
			}
		}
		sendMsg(sender, line.toString());
	}

	/**
	 * Add another class to grab command methods from.  Targets that are explicitly added always
	 * take precidence over the this class and are searched in the order they are added.
	 */
	public GenericCommand addTarget(Object newTarget) {
		// add before the last element, which is always "this"
		dispatchers.add(dispatchers.size() - 1, new MethodDispatcher(newTarget));
		return this;
	}

	public GenericCommand addTargetWithSettings(Object newTarget) {
		dispatchers.add(dispatchers.size() - 1, new MethodDispatcher(newTarget));
		settings.add(new SettingAccessor(newTarget));
		return this;
	}

	public GenericCommand addTargetWithPersitentSettings(Object newTarget, String configFileName, String configVersion) {
		dispatchers.add(dispatchers.size() - 1, new MethodDispatcher(newTarget));
		settings.add(new SettingAccessor(newTarget, configFileName, configVersion));
		return this;
	}

	@Override
	public void handleFMLEvent(FMLServerStartingEvent event) {
		event.registerServerCommand(this);
		Logging.logTrace(this.getName() + " command registered");
	}

	/** This method saves all settings for this command on server shutdown */
	@Override
	public void handleFMLEvent(FMLServerStoppingEvent event) {
		saveSettings();
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return usage;
	}

	@Override
	public List<String> getAliases() {
		return aliasList;
	}

	public void loadSettings() {
		for (SettingAccessor accessor : settings) {
			accessor.load();
		}
	}

	public void saveSettings() {
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

	public void set(String settingName, String value) throws SettingNotFoundException, InvalidValueException {
		for (SettingAccessor accessor : settings) {
			try {
				accessor.set(settingName, value);
				return;
			} catch (SettingNotFoundException e) {
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

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) {
		World world = sender.getEntityWorld();
		if (!world.isRemote) {
			if (args.length == 0) {
				// root command with no arguments  - doIt(sender)
				for (MethodDispatcher dispatcher : dispatchers) {
					if (dispatcher.invokeRootCommand(sender, args)) {
						return;
					}
				}
			} else {
				// sub command
				for (MethodDispatcher dispatcher : dispatchers) {
					if (dispatcher.invokeSubCommand(sender, args)) {
						return;
					}
				}
				// root command with arguments - doIt(sender, ...)
				for (MethodDispatcher dispatcher : dispatchers) {
					if (dispatcher.invokeRootCommandWithArgs(sender, args)) {
						return;
					}
				}
			}
		}
		sendMsg(sender, "Command not found or improper number of arguments");
		doHelp(sender);
	}

	@Override
	public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
		return true;
	}

	@Override
	public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, BlockPos targetPos) {
		if (args.length == 1) {
			Set<String> set = new HashSet<>();
			for (MethodDispatcher dispatcher : dispatchers) {
				for (String subCmd : dispatcher.getSubcommands()) {
					if (subCmd.startsWith(args[0])) {
						set.add(subCmd);
					}
				}
			}
			return Lists.newArrayList(set);
		} else {
			// Sorry.  No tab completions on commands that already partially formed
			return null;
		}
	}

/*
	public void doIt(ICommandSender sender) {
		// a command without a subcommand.  Subclasses can override this if they want
		// some real behavior.  For now, just dump help
		doHelp(sender);
	}
*/

	@Override
	public boolean isUsernameIndex(String[] args, int index) {
		return false;
	}

	@Override
	public int compareTo(ICommand o) {
		return getName().compareTo(o.getName());
	}

	//----------------------------------------------------------------------------------------------------
	// basic command line commands
	//----------------------------------------------------------------------------------------------------

	/* Help for the root command.   Ex. /<name> help */
	@Command(help = "A command to get help on commands")
	public void doHelp(ICommandSender sender) {
		String usage = getUsage(sender);

		if (usage != null) {
			sendMsg(sender, getUsage(sender));

		}
		sendMsg(sender, "or Try /" + getName() + " help [ " + buildCommandsList() + " ]");
	}

	/* Help for a subcommand.  Ex. /<name> help <subcommand> */
	@Command(help = "help <subcommand> - get help on how to use <subcommand>")
	public void doHelp(ICommandSender sender, String subcommand) {
		for (MethodDispatcher dispatcher : dispatchers) {
			String help = dispatcher.getHelpString(subcommand);
			if (help != null) {
				sendMsg(sender, help);
				return;
			}
		}
		doHelp(sender);
	}

	@Command(help = "commands - list available subcommands")
	public void doCommands(ICommandSender sender) {
		sendMsg(sender, buildCommandsList());
	}

	private String buildCommandsList() {
		List<String> commandNames = new ArrayList<>();
		for (MethodDispatcher dispatcher : dispatchers) {
			commandNames.addAll(dispatcher.getSubcommands());
		}
		Collections.sort(commandNames);
		StringBuilder builder = new StringBuilder();
		String seperator = "";
		for (String subCmd : commandNames) {
			builder.append(seperator);
			builder.append(subCmd);
			seperator = " | ";
		}
		return builder.toString();
	}

	@Command(help = "List avialable settings")
	public void doSettings(ICommandSender sender) {
		sendMsg(sender, "settings: " + getSettingNames());
	}

	@Command(help = "Get the value of a setting:  'get <settingName>'")
	public void doGet(ICommandSender sender, String setting) {
		try {
			sendMsg(sender, setting + " = " + get(setting));
		} catch (SettingNotFoundException e) {
			sendMsg(sender, e.getMessage());
		}
	}

	@Command(help = "Set the value of a setting: 'set <settingName> <value>'")
	public void doSet(ICommandSender sender, String setting, String value) {
		try {
			set(setting, value);
			sendMsg(sender, setting + " set to " + value);
		} catch (Exception e) {
			sendMsg(sender, e.getMessage());
		}
	}

	@Command(help = "List aliases for this command")
	public void doAliases(ICommandSender sender) {
		sendMsg(sender, aliasList.toString());
	}

	//----------------------------------------------------------------------------------------------------
	// Root module command
	//----------------------------------------------------------------------------------------------------
	private static class ModuleCommand {
		@Command
		public void doIt(ICommandSender sender) {
			doHelp(sender);
		}

		@Command
		public void doHelp(ICommandSender sender) {
			for (GenericCommand cmd : commands) {
				sendMsg(sender, "    " + cmd.getName() + " - " + cmd.getUsage(sender));
			}
		}
	}
}
