package com.example.examplemodtests.testUtilities;

import com.example.examplemod.ExampleMod;
import com.example.examplemod.utilities.commands.GenericCommand;
import com.example.examplemodtests.utilities.GenericCommandTest;
import com.example.examplemodtests.utilities.GenericSettingsTest;
import net.minecraft.command.ICommandSender;

public class RunTestsCommand extends GenericCommand {
	public static final String name = "RunTests";
	public static final String usage = "RunTests [ <testClassName> ]";
	public static final String[] aliases = {"runtests", "runTests", "RunTest", "runTest", "runtest"};

	public RunTestsCommand() {
		super(name, usage, aliases);
	}

	public void doIt(ICommandSender sender) {
		HackTestHarness.run(GenericSettingsTest.class, ExampleMod.logger);
		HackTestHarness.run(GenericCommandTest.class, ExampleMod.logger);
	}

	public void doIt(ICommandSender sender, String testClassName) {
		try {
			Class<?> clazz = Class.forName(testClassName);
			if (HackTestHarness.Suite.class.isAssignableFrom(clazz)) {
				HackTestHarness.run((Class<? extends HackTestHarness.Suite>) clazz, ExampleMod.logger);
			}
		} catch (ClassNotFoundException | ClassCastException e) {
			sendMsg(sender, "Test class '" + testClassName + "' not found.");
		}
	}
}
