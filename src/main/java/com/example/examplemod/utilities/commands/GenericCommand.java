package com.example.examplemod.utilities.commands;

import com.example.examplemod.ExampleMod;
import com.example.examplemod.utilities.hackfmlevents.HackFMLEventListener;
import com.google.common.collect.Lists;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;

import javax.annotation.Nullable;
import java.util.*;

public class GenericCommand implements ICommand, HackFMLEventListener {
	private static final int LINE_WRAP_LENGTH = 100;
	private final List<String> aliasList = new ArrayList<>();
	private final String usage;
	private final String name;
	private final List<MethodDispatcher> dispatchers = new ArrayList<>();

	public static GenericCommand create(String name, String usage, String... aliases) {
		return new GenericCommand(name, usage, aliases);
	}

	public GenericCommand(String name, String usage, String... aliases) {
		this.dispatchers.add(new MethodDispatcher(this));
		this.name = name;
		this.usage = usage;
		aliasList.addAll(Arrays.asList(aliases));
		subscribeToFMLEvents();
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

	@Override
	public void handleFMLEvent(FMLServerStartingEvent event) {
		event.registerServerCommand(this);
		ExampleMod.logTrace(this.getName() + " command registered");
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

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) {
		for (MethodDispatcher dispatcher : dispatchers) {
			if (dispatcher.execute(server, sender, args)) {
				return;
			}
		}
		sendMsg(sender, "Command not found or missing arguments");
		doHelp(sender);
	}

	@Override
	public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
		return true;
	}

	@Override
	public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
		if (args.length == 0) {
			//TODO hey jf - you could cache this.  Check to see how ofter it's called
			Set<String> set = new HashSet<>();
			for (MethodDispatcher dispatcher : dispatchers) {
				set.addAll(dispatcher.getSubcommands());
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

	/* Help for the root command.   Ex. /<name> help */
	@CommandMethod(help = "A command to get help on commands")
	public void doHelp(ICommandSender sender) {
		String usage = getUsage(sender);

		if (usage != null) {
			sendMsg(sender, getUsage(sender));

		}
		sendMsg(sender, "or Try /" + getName() + " help [ " + buildCommandsList() + " ]");
	}

	/* Help for a subcommand.  Ex. /<name> help <subcommand> */
	@CommandMethod(help = "help <subcommand> - get help on how to use <subcommand>")
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

	@CommandMethod(help = "commands - list available subcommands")
	public void doCommands(ICommandSender sender) {
		sendMsg(sender, buildCommandsList());
	}

	private String buildCommandsList() {
		StringBuilder builder = new StringBuilder();
		String seperator = "";
		for (MethodDispatcher dispatcher : dispatchers) {
			for (String subCmd : dispatcher.getSubcommands()) {
				builder.append(seperator);
				builder.append(subCmd);
				seperator = " | ";
			}
		}
		return builder.toString();
	}
}
