package com.example.examplemod.utilities.commands;

import com.example.examplemod.utilities.Logging;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.FMLCommonHandler;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

class MethodDispatcher {
	/** MethodDispatcher deals with command methods for a single instance object */
	private static final String COMMAND_METHOD_PREFIX = "do";
	private static final String ROOT_COMMAND = COMMAND_METHOD_PREFIX + "it";    //doIt
	private final Object target;
	private final Map<MethodInvokerKey, MethodInvoker> commands = new HashMap<>();
	private final List<String> tabCompletions = new ArrayList<>();
	private final int maxArgumentCount;

	public MethodDispatcher(Object target) {
		this.target = target;
		Set<String> cmdSet = new HashSet<>();
		Class<?> clazz = target.getClass();
		int maxArgCount = 0;
		while (clazz != Object.class) {
			for (Method method : clazz.getDeclaredMethods()) {
				if (isCommandMethodSigniture(method)) {
					MethodInvoker dispatcher = new MethodInvoker(method);
					if (commands.containsKey(dispatcher.key)) {
						// duplicate or ambiguous method
						Logging.logInfo("Duplicate or ambiguous command method: " + method.getDeclaringClass().getName()
								+ "." + method.getName() + "(" + dispatcher.key.methodArgCount + ")");
					} else {
						commands.put(dispatcher.key, dispatcher);
						maxArgCount = Math.max(maxArgCount, dispatcher.key.methodArgCount);
						if (!ROOT_COMMAND.equals(dispatcher.key.name)) {
							cmdSet.add(dispatcher.key.name.substring(COMMAND_METHOD_PREFIX.length()));
						}
					}
				}
			}
			clazz = clazz.getSuperclass();
		}
		tabCompletions.addAll(cmdSet);
		Collections.sort(tabCompletions);
		maxArgumentCount = maxArgCount;
	}

	/* Check to see if a given method is of the form "void do<Bla>(ICommandSender [, String...])" */
	private boolean isCommandMethodSigniture(Method method) {
		if ((method.getAnnotation(Command.class) != null)) {

			if (!method.getName().startsWith(COMMAND_METHOD_PREFIX)) {
				Logging.logInfo(String.format("Bad method signiture for @Command %s.%s.  Name must start with 'do'.",
						method.getDeclaringClass().getName(), method.getName()));
				return false;
			}
			if (method.getReturnType() != Void.TYPE) {
				Logging.logInfo(String.format("Bad method signiture for @Command %s.%s.  Return type must be void.",
						method.getDeclaringClass().getName(), method.getName()));
				return false;
			}
			if (!Character.isUpperCase(method.getName().charAt(COMMAND_METHOD_PREFIX.length()))) {
				// first character after "do" needs to be upper case
				Logging.logInfo(String.format("Bad method signiture for @Command %s.%s.  Upper case character must follow 'do'.",
						method.getDeclaringClass().getName(), method.getName()));
				return false;
			}
			Class<?>[] paramTypes = method.getParameterTypes();
			if ((paramTypes.length < 1) || (paramTypes[0] != ICommandSender.class)) {
				// the first parameter must be of type ICommandSender
				Logging.logInfo(String.format("Bad method signiture for @Command %s.%s.  Missing ICommandSender argument.",
						method.getDeclaringClass().getName(), method.getName()));
				return false;
			}
			for (int i = 1; i < paramTypes.length; i++) {
				// each param after the first must be of type String
//				if (paramTypes[i] != String.class) return false;
				if (!TypeConversionHelper.isSupportedType(paramTypes[i])) return false;
			}
			return true;
		} else {
			return false;
		}
	}

	public boolean invokeRootCommand(ICommandSender sender, String[] args) {
		assert args.length == 0;
		// root command with no arguments
		return invokeCommandIfFound(sender, args, commands.get(new MethodInvokerKey(1)));
	}

	public boolean invokeRootCommandWithArgs(ICommandSender sender, String[] args) {
		return invokeCommandIfFound(sender, args, commands.get(new MethodInvokerKey(args.length + 1)));
	}

	public boolean invokeSubCommand(ICommandSender sender, String[] args) {
		// check sub commands
		String possibleCommandMethodName = COMMAND_METHOD_PREFIX + args[0].toLowerCase();
		return invokeCommandIfFound(sender, args, commands.get(new MethodInvokerKey(possibleCommandMethodName, args.length)));
	}

	private boolean invokeCommandIfFound(ICommandSender sender, String[] args, MethodInvoker method) {
		if (method != null && method.hasPermission(sender)) {
			method.invoke(target, sender, args);
			return true;
		} else {
			return false;
		}
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
	private static class MethodInvoker {
		public final MethodInvokerKey key;
		public final Method method;
		public final String help;
		public final boolean requiresOp;

		public MethodInvoker(Method method) {
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
				Object[] mungedArgs = calculateArguments(sender, args);
				Class<?>[] paramTypes = method.getParameterTypes();
				for (int i = 1; i < mungedArgs.length; i++) {
					mungedArgs[i] = TypeConversionHelper.convertStringToType("argument", (String) mungedArgs[i], paramTypes[i]);
				}
				method.invoke(commandObject, mungedArgs);
			} catch (IllegalAccessException | InvocationTargetException | InvalidValueException e) {
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
