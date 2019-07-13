package com.example.examplemod.utilities.eventsnoopers;

import com.example.examplemod.utilities.Logging;
import com.example.examplemod.utilities.commands.Command;
import com.example.examplemod.utilities.commands.GenericCommand;
import com.example.examplemod.utilities.hackfmlevents.HackFMLEventListener;
import net.minecraft.command.ICommandSender;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.*;

public class EventSnooper implements HackFMLEventListener {
	private static final String COMMAND_NAME = "snoop";
	private static final String COMMAND_USAGE = "/snoop <event> [ ON | OFF ]";
	private static final String[] COMMAND_ALIASES = {};
	public static EventSnooper INSTANCE;
	private boolean snooping = false;
	private Set<Class<?>> snoopingTypes = new HashSet<>();

	public EventSnooper() {
		subscribeToFMLEvents();
		GenericCommand.create(COMMAND_NAME, COMMAND_USAGE, COMMAND_ALIASES).addTarget(this);
	}

	public static void init() {
		INSTANCE = new EventSnooper();
	}

	@Override
	public void handleFMLEvent(FMLPreInitializationEvent event) {
	}

	@SubscribeEvent
	public void snoopEvent(Event e) {
		if (snooping) {
			Class<?> eventClass = e.getClass();
			for (Class<?> clazz : snoopingTypes) {
				if (clazz.isAssignableFrom(eventClass)) {
					Logging.logInfo(">> event: " + e.getClass().getName());
					Logging.logInfo(">>      : " + e.toString());
				}
			}
		}
	}

	@Command
	public void doOn(ICommandSender sender) {
		doEnable(sender);
	}

	@Command
	public void doOff(ICommandSender sender) {
		doDisable(sender);
	}

	@Command
	public void doEnable(ICommandSender sender) {
		if (snoopingTypes.size() > 0) {
			MinecraftForge.EVENT_BUS.register(this);
		}
	}

	@Command
	public void doDisable(ICommandSender sender) {
		MinecraftForge.EVENT_BUS.unregister(this);
	}

	@Command
	public void doIt(ICommandSender sender, String event) {
		doIt(sender, event, "on");
	}

	@Command
	public void doIt(ICommandSender sender, String eventClassName, String onOff) {
		try {
			Class<?> eventClass = Class.forName(eventClassName);
			onOff = onOff.toLowerCase();
			if ("on".equals(onOff)) {
				snoopingTypes.add(eventClass);
				if (snooping && (snoopingTypes.size() == 1)) {
					MinecraftForge.EVENT_BUS.register(this);
				}
			} else if ("off".equals(onOff)) {
				snoopingTypes.remove(eventClass);
				if (snoopingTypes.size() == 0) {
					MinecraftForge.EVENT_BUS.unregister(this);
				}
			} else {
				GenericCommand.sendMsg(sender, COMMAND_USAGE);
			}
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			GenericCommand.sendMsg(sender, "Could not find class: " + eventClassName);
		}
	}

	@Command
	public void doList(ICommandSender sender) {
		List<String> typeNames = new ArrayList<>(snoopingTypes.size());
		for (Class<?> clazz : snoopingTypes) {
			typeNames.add(clazz.getName());
		}
		Collections.sort(typeNames);
		for (String name : typeNames) {
			GenericCommand.sendMsg(sender, name);
		}
	}
}
