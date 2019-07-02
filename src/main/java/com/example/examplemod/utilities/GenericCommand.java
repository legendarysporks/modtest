package com.example.examplemod.utilities;

import com.example.examplemod.ExampleMod;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;

import javax.annotation.Nullable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

public class GenericCommand implements ICommand, HackFMLEventListener {
	private static final String COMMAND_METHOD_PREFIX = "do";
	private static final String ROOT_COMMAND = COMMAND_METHOD_PREFIX + "it";
	private static final int MAX_WORD_DUMP_LINE_LENGTH = 100;

	private final List<String> aliasList = new ArrayList<>();
	private final String usage;
	private final String name;
	private final Map<CommandDispatcherKey, CommandDispatcher> commands = new HashMap<>();
	private final List<String> tabCompletions = new ArrayList<>();
	private int maxArgumentCount = 0;

	public GenericCommand(String name, String usage, String... aliases) {
		this.name = name;
		this.usage = usage;
		for (String alias : aliases) {
			aliasList.add(alias);
		}
		buildCommandMapping();
		subscribeToFMLEvents();
		ExampleMod.logTrace(name + " command constructed");
	}

	/* Search through the methods of this class and super classes to find command handler methods */
	private void buildCommandMapping() {
		Set<String> cmdSet = new HashSet<>();
		Class clazz = getClass();
		while (clazz != Object.class) {
			for (Method method : clazz.getDeclaredMethods()) {
				if (isCommandMethodSigniture(method)) {
					CommandDispatcher dispatcher = new CommandDispatcher(method);
					commands.put(dispatcher.key, dispatcher);
					maxArgumentCount = Math.max(maxArgumentCount, dispatcher.key.methodArgCount);
					if (!ROOT_COMMAND.equals(dispatcher.key.name)) {
						cmdSet.add(dispatcher.key.name.substring(COMMAND_METHOD_PREFIX.length()));
					}
				}
			}
			clazz = clazz.getSuperclass();
		}
		tabCompletions.addAll(cmdSet);
		Collections.sort(tabCompletions);
	}

	/* Check to see if a given method is of the form "void do<Bla>(ICommandSender [, String...])" */
	private boolean isCommandMethodSigniture(Method method) {
		//TODO hey jf - check for void return type
		if (method.getName().startsWith(COMMAND_METHOD_PREFIX)) {
			if (!Character.isUpperCase(method.getName().charAt(COMMAND_METHOD_PREFIX.length()))) {
				// first character after "do" needs to be upper case
				return false;
			}
			Class<?>[] paramTypes = method.getParameterTypes();
			if ((paramTypes.length < 1) || (paramTypes[0] != ICommandSender.class)) {
				// the first parameter must be of type ICommandSender
				return false;
			}
			for (int i = 1; i < paramTypes.length; i++) {
				// each param after the first must be of type String
				if (paramTypes[i] != String.class) return false;
			}
			return true;
		} else {
			return false;
		}
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
		World world = sender.getEntityWorld();

		if (world.isRemote == false) {
			CommandDispatcher method;
			if (args.length == 0) {
				// root command with no arguments
				method = commands.get(new CommandDispatcherKey(1));
			} else {
				// check sub commands
				String possibleCommandMethodName = COMMAND_METHOD_PREFIX + args[0].toLowerCase();
				method = commands.get(new CommandDispatcherKey(possibleCommandMethodName, args.length));
				if (method == null) {
					// root command with arguments
					method = commands.get(new CommandDispatcherKey(args.length + 1));
				}
			}

			if (method != null) {
				// we found a matching method for the command and agruments
				if (method.hasPermission(sender)) {
					method.invoke(this, sender, args);
				} else {
					sendMsg(sender, "You don't have permission to do that.");
				}
			} else if (args.length > 0) {
				// try to display help for subcommand
				sendMsg(sender, "Command not understood.");
				doHelp(sender, args[0]);
			} else {
				sendMsg(sender, "Command not understood.");
				doHelp(sender);
			}
		}
	}

	@Override
	public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
		return true;
	}

	@Override
	public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
		return (args.length == 0) ? tabCompletions : null;
	}

	@Override
	public boolean isUsernameIndex(String[] args, int index) {
		return false;
	}

	@Override
	public int compareTo(ICommand o) {
		return getName().compareTo(o.getName());
	}

/*
	public void doIt(ICommandSender sender) {
		// a command without a subcommand.  Subclasses can override this if they want
		// some real behavior.  For now, just dump help
		doHelp(sender);
	}
*/

	/* Help for the root command.   Ex. /<name> help */
	@CommandMeta(help = "A command to get help on commands")
	public void doHelp(ICommandSender sender) {
		String usage = getUsage(sender);

		if (usage != null) {
			sendMsg(sender, getUsage(sender));

		}
		StringBuilder msg = new StringBuilder();
		msg.append("or Try /");
		msg.append(getName());
		msg.append(" help [ ");
		msg.append(buildCommandsList());
		msg.append(" ]");
		sendMsg(sender, msg.toString());
	}

	/* Help for a subcommand.  Ex. /<name> help <subcommand> */
	@CommandMeta(help = "help <subcommand> - get help on how to use <subcommand>")
	public void doHelp(ICommandSender sender, String subcommand) {
		String subCommandMethodName = COMMAND_METHOD_PREFIX + subcommand.toLowerCase();
		boolean helpShown = false;
		for (int i = 0; i <= maxArgumentCount; i++) {
			CommandDispatcher dispatcher = commands.get(new CommandDispatcherKey(subCommandMethodName, i));
			if (dispatcher != null) {
				sendMsg(sender, dispatcher.help);
				helpShown = true;
			}
		}
		if (!helpShown) {
			// we didn't have ANY matching methods, so default to general help
			doHelp(sender);
		}
	}

	@CommandMeta(help = "commands - list available subcommands")
	public void doCommands(ICommandSender sender) {
		StringBuilder builder = new StringBuilder();
		builder.append("[ ");
		builder.append(buildCommandsList());
		builder.append(" ]");
		sendMsg(sender, builder.toString());
	}

	private String buildCommandsList() {
		StringBuilder builder = new StringBuilder();
		String seperator = "";
		for (String subCmd : tabCompletions) {
			builder.append(seperator);
			builder.append(subCmd);
			seperator = " | ";
		}
		return builder.toString();
	}

	protected void sendMsg(ICommandSender sender, String msg) {
		sender.sendMessage(new TextComponentString(msg));
	}

	protected void sendMsg(ICommandSender sender, Collection<String> words) {
		StringBuilder line = new StringBuilder();
		for (String word : words) {
			line.append(word);
			line.append(" ");
			if (line.length() > MAX_WORD_DUMP_LINE_LENGTH) {
				sendMsg(sender, line.toString());
				line = new StringBuilder();
			}
		}
		sendMsg(sender, line.toString());
	}

	@Retention(RetentionPolicy.RUNTIME)
	public @interface CommandMeta {
		String help() default "";

		boolean requiresOp() default false;
	}

	private static class CommandDispatcherKey implements Comparable<CommandDispatcherKey> {
		public final String name;
		public final int methodArgCount;

		// root command
		public CommandDispatcherKey(int methodArgCount) {
			this.name = ROOT_COMMAND;
			this.methodArgCount = methodArgCount;
		}

		// sub command
		public CommandDispatcherKey(String name, int methodArgCount) {
			this.name = name;
			this.methodArgCount = methodArgCount;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			CommandDispatcherKey other = (CommandDispatcherKey) o;
			return name.equals(other.name) && (methodArgCount == other.methodArgCount);
		}

		@Override
		public int hashCode() {
			return (methodArgCount * 31) ^ name.hashCode();
		}

		@Override
		public int compareTo(CommandDispatcherKey o) {
			// Sort ROOT_COMMAND first and other alphabetically
			if (name == ROOT_COMMAND) {
				return -1;
			} else if (o.name == ROOT_COMMAND) {
				return 1;
			} else {
				int result = name.compareTo(o.name);
				if (result == 0) {
					return methodArgCount - o.methodArgCount;
				} else {
					return 0;
				}
			}
		}

		public String getDisplayName() {
			if (name.equals(ROOT_COMMAND)) {
				return "";
			} else {
				return name;
			}
		}

		public boolean isRootCommand() {
			return ROOT_COMMAND.equals(name);
		}
	}

	private class CommandDispatcher {
		public final CommandDispatcherKey key;
		public final Method method;
		public final String help;

		CommandDispatcher(Method method) {
			//TODO hey jf - add permissions at some point
			//PermissionAPI.registerNode(String node, DefaultPermissionLevel level, String description)
			this.key = new CommandDispatcherKey(method.getName().toLowerCase(), method.getParameterCount());
			this.method = method;
			CommandMeta annotation = method.getAnnotation(CommandMeta.class);
			if (annotation != null) {
				help = annotation.help();
			} else {
				help = usage;
			}
		}

		public boolean hasPermission(ICommandSender sender) {
			//TODO hey jf - add permissions at some point
			//PermissionAPI.hasPermission( player, node);
			return true;
		}

		public void invoke(Object commandObject, ICommandSender sender, Object[] args) {
			try {
				method.invoke(commandObject, calculateArguments(sender, args));
			} catch (IllegalAccessException | InvocationTargetException e) {
				e.printStackTrace();
			}
		}

		private Object[] calculateArguments(ICommandSender sender, Object[] argsIn) {
			Object[] argsOut;
			if (key.isRootCommand()) {
				// we need to insert the sender in the arguments list
				// argsIn looks like { "param1", "param2",  ... }
				argsOut = new Object[argsIn.length + 1];
				System.arraycopy(argsIn, 0, argsOut, 1, argsIn.length);
			} else {
				// subcommand just plaster of the subcommand param with the sender
				// argsIn looks like { "subcommand", "param1", "param2",  ... }
				argsOut = new Object[argsIn.length];
				System.arraycopy(argsIn, 1, argsOut, 1, argsIn.length - 1);
			}
			// argsOut looks like { null, "param1", "param2", ... }
			argsOut[0] = sender;
			// argsOut looks like { sender, "param1", "param2", ... }
			return argsOut;

		}
	}
}
