package com.example.examplemod.utilities.eventsnoopers;

import com.example.examplemod.utilities.Logging;
import com.example.examplemod.utilities.commands.Command;
import com.example.examplemod.utilities.commands.GenericCommand;
import net.minecraft.command.ICommandSender;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.*;

public class EventSnooper {
	private static final String COMMAND_NAME = "snoop";
	private static final String COMMAND_USAGE = "/snoop on | off | <event> [ ON | OFF | IGNORE ]";
	private static final String[] COMMAND_ALIASES = {};
	public static EventSnooper INSTANCE;
	private boolean snooping = false;
	private Set<Class<?>> snoopingTypes = new HashSet<>();
	private static final String[] packages = {
			"net.minecraftforge.event.",
			"net.minecraftforge.event.brewing.",
			"net.minecraftforge.event.enchanting.",
			"net.minecraftforge.event.entity.",
			"net.minecraftforge.event.entity.item.",
			"net.minecraftforge.event.entity.living.",
			"net.minecraftforge.event.entity.minecart.",
			"net.minecraftforge.event.entity.player.",
			"net.minecraftforge.event.furnace.",
			"net.minecraftforge.event.terraingen.",
			"net.minecraftforge.event.village.",
			"net.minecraftforge.event.world.",
			"net.minecraftforge.client.event.",
			"net.minecraftforge.client.event.sound"
	};

	//---------------------------------------------------------------------------
	//---------------------------------------------------------------------------
	private Set<Class<?>> snoopingIgnoreTypes = new HashSet<>();

	//---------------------------------------------------------------------------
	//---------------------------------------------------------------------------

	public EventSnooper() {
		GenericCommand.create(COMMAND_NAME, COMMAND_USAGE, COMMAND_ALIASES).addTarget(this);
	}

	public static void init() {
		INSTANCE = new EventSnooper();
	}

	@Command
	public void doOn(ICommandSender sender) {
		doEnable(sender);
	}

	@Command
	public void doOff(ICommandSender sender) {
		doDisable(sender);
	}

	public void setBreakpointHere(Event e) {
		Logging.logInfo(">> event: " + e.getClass().getName());
	}

	@Command
	public void doEnable(ICommandSender sender) {
		snooping = true;
		if (snoopingTypes.size() > 0) {
			MinecraftForge.EVENT_BUS.register(this);
		}
	}

	@Command
	public void doDisable(ICommandSender sender) {
		snooping = false;
		MinecraftForge.EVENT_BUS.unregister(this);
	}

	@Command
	public void doOn(ICommandSender sender, String event) {
		doIt(sender, event, "on");
	}

	@Command
	public void doOff(ICommandSender sender, String event) {
		doIt(sender, event, "off");
	}

	@Command
	public void doIgnore(ICommandSender sender, String event) {
		doIt(sender, event, "ignore");
	}

	@Command
	public void doIt(ICommandSender sender, String event) {
		doIt(sender, event, "on");
	}

	@Command
	public void doIt(ICommandSender sender, String eventClassName, String onOff) {
		Class<?> eventClass = findEventClass(eventClassName);
		if (eventClass != null) {
			onOff = onOff.toLowerCase();
			if ("on".equals(onOff)) {
				snoopingIgnoreTypes.remove(eventClass);
				snoopingTypes.add(eventClass);
				if (snooping && (snoopingTypes.size() == 1)) {
					MinecraftForge.EVENT_BUS.register(this);
				}
				GenericCommand.sendMsg(sender, "Snooping enabled for " + eventClass.getName());
			} else if ("off".equals(onOff)) {
				snoopingTypes.remove(eventClass);
				if (snoopingTypes.size() == 0) {
					MinecraftForge.EVENT_BUS.unregister(this);
				}
				GenericCommand.sendMsg(sender, "Snooping disabled for " + eventClass.getName());
			} else if ("ignore".equals(onOff)) {
				if (!snoopingTypes.remove(eventClass)) {
					snoopingIgnoreTypes.add(eventClass);
					GenericCommand.sendMsg(sender, "Snooping ignores " + eventClass.getName());
				} else {
					if (snoopingTypes.size() == 0) {
						MinecraftForge.EVENT_BUS.unregister(this);
					}
					GenericCommand.sendMsg(sender, "Snooping disabled for " + eventClass.getName());
				}
			} else {
				GenericCommand.sendMsg(sender, COMMAND_USAGE);
			}
		} else {
			GenericCommand.sendMsg(sender, "Could not find class: " + eventClassName);
		}
	}

	@Command
	public void doList(ICommandSender sender) {
		GenericCommand.sendMsg(sender, "Snooping is " + ((snooping) ? "on" : "off"));
		GenericCommand.sendMsg(sender, "Snooping for:");
		for (String name : getNameList(snoopingTypes)) {
			GenericCommand.sendMsg(sender, "  " + name);
		}
		if (snoopingIgnoreTypes.size() > 0) {
			GenericCommand.sendMsg(sender, "Snooping ignores:");
			for (String name : getNameList(snoopingIgnoreTypes)) {
				GenericCommand.sendMsg(sender, "  " + name);
			}
		}
	}

	private List<String> getNameList(Set<Class<?>> classes) {
		List<String> typeNames = new ArrayList<>(classes.size());
		for (Class<?> clazz : classes) {
			typeNames.add(clazz.getName());
		}
		Collections.sort(typeNames);
		return typeNames;
	}

	@SubscribeEvent
	public void snoopEvent(Event e) {
		if (snooping) {
			Class<?> eventClass = e.getClass();
			if (!snoopingIgnoreTypes.contains(eventClass)) {
				for (Class<?> clazz : snoopingTypes) {
					if (clazz.isAssignableFrom(eventClass)) {
						setBreakpointHere(e);
					}
				}
			}
		}
	}

	private Class<?> findEventClass(String name) {
		Class<?> result = findClass(name);
		if (result != null) {
			return result;
		} else {
			for (String packagePrefix : packages) {
				result = findClass(packagePrefix + name);
				if (result != null) {
					return result;
				}
			}
		}
		return null;
	}

	private Class<?> findClass(String name) {
		try {
			return Class.forName(name);
		} catch (ClassNotFoundException e) {
			return null;
		}
	}
}
