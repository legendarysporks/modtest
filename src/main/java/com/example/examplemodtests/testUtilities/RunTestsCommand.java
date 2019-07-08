package com.example.examplemodtests.testUtilities;

import com.example.examplemod.ExampleMod;
import com.example.examplemod.utilities.commands.Command;
import com.example.examplemod.utilities.commands.GenericCommand;
import com.example.examplemodtests.utilities.GenericCommandTest;
import com.example.examplemodtests.utilities.SettingAccessorTest;
import net.minecraft.command.ICommandSender;

public class RunTestsCommand extends GenericCommand {
	public static final String name = "RunTests";
	public static final String usage = "RunTests [ <testClassName> ]";
	public static final String[] aliases = {"runtests", "runTests", "RunTest", "runTest", "runtest"};

	public RunTestsCommand() {
		super(name, usage, aliases);
	}

	@Command
	public void doIt(ICommandSender sender) {
		TestExecution result1 = TestExecution.run(SettingAccessorTest.class, ExampleMod.logger);
		TestExecution result2 = TestExecution.run(GenericCommandTest.class, ExampleMod.logger);

		int testsFailures = result1.testFailures.size() + result2.testFailures.size();
		int testsRun = result1.testOrder.size() + result2.testOrder.size();

		sendMsg(sender, String.format("%d tests run.  %d passed.  %d failed.", testsRun, testsRun - testsFailures, testsFailures));
	}

	@Command
	public void doIt(ICommandSender sender, String testClassName) {
		try {
			Class<?> clazz = Class.forName(testClassName);
			if (TestExecution.Suite.class.isAssignableFrom(clazz)) {
				TestExecution results = TestExecution.run((Class<? extends TestExecution.Suite>) clazz, ExampleMod.logger);
				int testsFailures = results.testFailures.size();
				int testsRun = results.testOrder.size();
				sendMsg(sender, String.format("%d tests fun.  %d passed.  %d failed.", testsRun, testsRun - testsFailures, testsFailures));
			}
		} catch (ClassNotFoundException | ClassCastException e) {
			sendMsg(sender, "Test class '" + testClassName + "' not found.");
		}
	}
}
