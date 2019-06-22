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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GenericCommand implements ICommand, HackFMLEventListener {
	private static final String COMMAND_METHOD_PREFIX = "do";
	private static final String ROOT_COMMAND = COMMAND_METHOD_PREFIX + "it";

	private final List<String> aliasList = new ArrayList<>();
	private final String usage;
	private final String name;
	private final Map<CommandDispatcherKey, CommandDispatcher> commands = new HashMap<>();
	private final Map<String, String> help = new HashMap<>();

	@Retention(RetentionPolicy.RUNTIME)
	public @interface Meta {
		String help() default "";

		boolean requiresOp() default false;
	}

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
		Class clazz = getClass();
		while (clazz != Object.class) {
			for (Method method : clazz.getDeclaredMethods()) {
				if (isCommandMethodSigniture(method)) {
					CommandDispatcher dispatcher = new CommandDispatcher(method);
					commands.put(dispatcher.key, dispatcher);
					Meta annotation = method.getAnnotation(Meta.class);
					if (annotation != null) {
						help.put(method.getName().toLowerCase(), annotation.help());
					}
				}
			}
			clazz = clazz.getSuperclass();
		}
	}

	/* Check to see if a given method is of the form "void do<bla>(ICommandSender...)" */
	private boolean isCommandMethodSigniture(Method method) {
		//TODO add check for ICommandSender parameter
		return method.getName().startsWith(COMMAND_METHOD_PREFIX);
	}

	@Override
	public void handleFMLEvent(FMLServerStartingEvent event) {
		event.registerServerCommand(this);
		ExampleMod.logTrace(this.getName() + " command registered");
	}

	public void onServerStarting(FMLServerStartingEvent event) {
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

		if (world.isRemote) {
			ExampleMod.logInfo("Not processing on Client side");
		} else {
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
				method.invoke(this, sender, args);
			} else {
				sender.sendMessage(new TextComponentString("Command not understood.  Try " + name + " help"));
			}
		}
	}

	@Override
	public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
		return true;
	}

	@Override
	public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
		return null;
	}

	@Override
	public boolean isUsernameIndex(String[] args, int index) {
		return false;
	}

	@Override
	public int compareTo(ICommand o) {
		return 0;
	}

	@Meta(help = "doIt(ICommandSender sender, String text) help")
	public void doIt(ICommandSender sender, String text) {
		sender.sendMessage(new TextComponentString("Yup. " + text));
	}

	public void doIt(ICommandSender sender) {
		sender.sendMessage(new TextComponentString("Yes? Can I help you?"));
	}

	public void doHelp(ICommandSender sender) {
		sender.sendMessage(new TextComponentString(usage));
	}

	public void doHelp(ICommandSender sender, String subcommand) {
		String specialHelp = help.get(COMMAND_METHOD_PREFIX + subcommand.toLowerCase());
		if (specialHelp != null) {
			sender.sendMessage(new TextComponentString(specialHelp));
		} else {
			doHelp(sender);
		}
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

	private static class CommandDispatcher {
		public final CommandDispatcherKey key;
		public final Method method;

		CommandDispatcher(Method method) {
			this.key = new CommandDispatcherKey(method.getName().toLowerCase(), method.getParameterCount());
			this.method = method;
		}

		private void invoke(Object commandObject, ICommandSender sender, Object[] args) {
			try {
				method.invoke(commandObject, calculateArguments(sender, args));
			} catch (IllegalAccessException | InvocationTargetException e) {
				e.printStackTrace();
			}
		}

		private Object[] calculateArguments(ICommandSender sender, Object[] argsIn) {
			Object argsOut[];
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
