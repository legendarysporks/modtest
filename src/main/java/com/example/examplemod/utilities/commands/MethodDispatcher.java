package com.example.examplemod.utilities.commands;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

class MethodDispatcher {
	/** CommandDispatcher deals with command methods for a single instance object */
	private static final String COMMAND_METHOD_PREFIX = "do";
	private static final String ROOT_COMMAND = COMMAND_METHOD_PREFIX + "it";    //doIt
	private final Object target;
	private final Map<MethodInvokerKey, MethodInvoker> commands = new HashMap<>();
	private final List<String> tabCompletions = new ArrayList<>();
	private int maxArgumentCount = 0;

	public MethodDispatcher(Object target) {
		this.target = target;
		buildCommandMapping();
	}

	/* Search through the methods of this class and super classes to find command handler methods */
	private void buildCommandMapping() {
		Set<String> cmdSet = new HashSet<>();
		Class<?> clazz = target.getClass();
		while (clazz != Object.class) {
			for (Method method : clazz.getDeclaredMethods()) {
				if (isCommandMethodSigniture(method)) {
					MethodInvoker dispatcher = new MethodInvoker(method);
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
		if ((method.getAnnotation(Command.class) != null)
				&& method.getName().startsWith(COMMAND_METHOD_PREFIX)
				&& (method.getReturnType() == Void.TYPE)) {
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

	public boolean execute(MinecraftServer server, ICommandSender sender, String[] args) {
		World world = sender.getEntityWorld();

		if (!world.isRemote) {
			MethodInvoker method;
			if (args.length == 0) {
				// root command with no arguments
				method = commands.get(new MethodInvokerKey(1));
			} else {
				// check sub commands
				String possibleCommandMethodName = COMMAND_METHOD_PREFIX + args[0].toLowerCase();
				method = commands.get(new MethodInvokerKey(possibleCommandMethodName, args.length));
				if (method == null) {
					// root command with arguments
					method = commands.get(new MethodInvokerKey(args.length + 1));
				}
			}

			if (method != null) {
				// we found a matching method for the command and agruments
				if (method.hasPermission(sender)) {
					method.invoke(target, sender, args);
					return true;
				}
			}
		}
		return false;
	}

	public List<String> getSubcommands() {
		return tabCompletions;
	}

	public String getHelpString(String subcommand) {
		String subCommandMethodName = COMMAND_METHOD_PREFIX + subcommand.toLowerCase();
		for (int i = 0; i <= maxArgumentCount; i++) {
			String help = getHelpString(subCommandMethodName, i);
			if (help != null) {
				return help;
			}
		}
		return null;
	}

	public String getHelpString(String subCommandMethodName, int argCount) {
		MethodInvoker methodInvoker = commands.get(new MethodInvokerKey(subCommandMethodName, argCount));
		if (methodInvoker != null) {
			return methodInvoker.help;
		} else {
			return null;
		}
	}

	/** MethodInvokerKey is used as a hash key for looking up CommandDispatchers */
	private static class MethodInvokerKey implements Comparable<MethodInvokerKey> {
		public final String name;
		public final int methodArgCount;

		// root command - doIt()
		public MethodInvokerKey(int methodArgCount) {
			this.name = ROOT_COMMAND;
			this.methodArgCount = methodArgCount;
		}

		// sub command - do<Something>()
		public MethodInvokerKey(String name, int methodArgCount) {
			this.name = name.toLowerCase();
			this.methodArgCount = methodArgCount;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			MethodInvokerKey other = (MethodInvokerKey) o;
			return name.equals(other.name) && (methodArgCount == other.methodArgCount);
		}

		@Override
		public int hashCode() {
			return (methodArgCount * 31) ^ name.hashCode();
		}

		@Override
		public int compareTo(MethodInvokerKey o) {
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

	/** This class holds details about a command method and is used to invoke with proper arguments. */
	private class MethodInvoker {
		public final MethodInvokerKey key;
		public final Method method;
		public final String help;
		public final boolean requiresOp;

		MethodInvoker(Method method) {
			this.key = new MethodInvokerKey(method.getName().toLowerCase(), method.getParameterCount());
			this.method = method;
			Command annotation = method.getAnnotation(Command.class);
			help = annotation.help();
			requiresOp = annotation.requiresOp();
		}

		/** return true is sender can use this command */
		public boolean hasPermission(ICommandSender sender) {
			if (requiresOp) {
				if (sender instanceof EntityPlayerMP) {
					EntityPlayerMP player = (EntityPlayerMP) sender;
					// player is op so let them do it.
					// non-op player when op is required.  NO!
					return FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getOppedPlayers().getEntry(
							player.getGameProfile()) != null;
				} else {
					// a non-player (console?) issued the command.  Go for it.
					return true;
				}
			} else {
				// op not required
				return true;
			}
		}

		public void invoke(Object commandObject, ICommandSender sender, Object[] args) {
			try {
				method.invoke(commandObject, calculateArguments(sender, args));
			} catch (IllegalAccessException | InvocationTargetException e) {
				e.printStackTrace();
			}
		}

		/** Take the command line arguments and munge them into method arguments */
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
