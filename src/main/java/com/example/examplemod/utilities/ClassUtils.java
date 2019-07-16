package com.example.examplemod.utilities;

import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.registry.EntityRegistry;

public class ClassUtils {
	private static final String[] eventPackages = {
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
	private static final String[] itemPackages = {
			"net.minecraftforge.item.",
			"net.minecraftforge.item.crafting.",
	};
	private static final String[] entityPackages = {
			"net.minecraftforge.entity.",
			"net.minecraftforge.entity.boss.",
			"net.minecraftforge.entity.effect.",
			"net.minecraftforge.entity.item.",
			"net.minecraftforge.entity.monster.",
			"net.minecraftforge.entity.passive.",
			"net.minecraftforge.entity.projectile.",
			"net.minecraftforge.entity.",
			"net.minecraftforge.entity.",
			"net.minecraftforge.entity.",
			"net.minecraftforge.entity.",
	};

	public static Class<? extends Event> findEventClass(String name) {
		return findEventClass(name, Event.class);
	}

	public static Class<? extends Event> findEventClass(String name, Class<? extends Event> clazz) {
		return findClass(name, eventPackages, clazz);
	}

	public static Class<? extends Item> findItemClass(String name) {
		return findItemClass(name, Item.class);
	}

	public static Class<? extends Item> findItemClass(String name, Class<? extends Item> clazz) {
		return findClass(name, itemPackages, clazz);
	}

	public static Class<? extends Entity> findEntityClass(String name) {
		return findEntityClass(name, Entity.class);
	}

	public static Class<? extends Entity> findEntityClass(String name, Class<? extends Entity> baseClass) {
		Class<? extends Entity> clazz = findClass(name, entityPackages, baseClass);
		if (EntityRegistry.getEntry(clazz) != null) {
			// only return classes for registered entities
			return clazz;
		} else {
			return null;
		}
	}

	public static <T> Class<? extends T> findClass(String name, String[] packages, Class<? extends T> baseClass) {
		Class<?> result = findClass(name);
		if (result != null && baseClass.isAssignableFrom(result)) {
			return (Class<? extends T>) result;
		} else {
			for (String packagePrefix : packages) {
				result = findClass(packagePrefix + name);
				if (result != null && baseClass.isAssignableFrom(result)) {
					return (Class<? extends T>) result;
				}
			}
		}
		return null;
	}

	private static Class<?> findClass(String name) {
		try {
			return Class.forName(name);
		} catch (ClassNotFoundException e) {
			return null;
		}
	}
}
