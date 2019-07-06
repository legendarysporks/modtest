package com.example.examplemodtests.utilities;

import com.example.examplemod.utilities.commands.CommandMethod;
import com.example.examplemod.utilities.commands.GenericCommand;
import com.example.examplemodtests.testUtilities.HackTestHarness;
import net.minecraft.command.ICommandSender;
import net.minecraft.profiler.Profiler;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraft.world.WorldProviderSurface;
import net.minecraft.world.chunk.IChunkProvider;

import java.util.ArrayList;
import java.util.List;

public class GenericCommandTest extends HackTestHarness.Suite {
	private DummyCommandSender remoteSender;
	private DummyCommandSender localSender;
	private GenericCommand cmd;

	@Override
	public void setup() {
		remoteSender = new DummyCommandSender(true);
		localSender = new DummyCommandSender(false);
		cmd = new TestCommand();
	}

	@Override
	public void teardown() {
		remoteSender = null;
		localSender = null;
		cmd = null;
	}

	public void testGetName() {
		assertTrue(cmd.getName().equals(TestCommand.NAME), "name not returned correctly");
	}

	public void testGetUsage() {
		assertTrue(cmd.getUsage(localSender).equals(TestCommand.USAGE), "usage not returned correctly");
		assertTrue(cmd.getUsage(remoteSender).equals(TestCommand.USAGE), "usage not returned correctly");
	}

	public void testGetAliases() {
		List<String> aliases = cmd.getAliases();
		assertTrue((aliases.size() == TestCommand.ALIASES.length), "incorrect number of aliases");
		for (int i = 0; i < TestCommand.ALIASES.length; i++) {
			assertTrue((aliases.get(i).equals(TestCommand.ALIASES[i])), "aliases are incorrect");
		}
	}

	public void testOnlyRespondsToRemoteCommands() {
		String[] args = {"help"};

		remoteSender.reset();
		cmd.execute(null, remoteSender, args);
		assertTrue(remoteSender.commandOutput.size() == 3, "Command executed on remote world when it shouldn't have");

		localSender.reset();
		cmd.execute(null, localSender, args);
		assertTrue(localSender.commandOutput.size() > 0, "Command did not execute");
	}

	@HackTestHarness.TestMetaInfo(terminateOnFailure = false)
	public void testMethodDispatching() {
		class TestCase {
			public final String[] args;
			public final String[] results;

			public TestCase(String[] args, String[] results) {
				this.args = args;
				this.results = results;
			}

			public String toString() {
				StringBuilder builder = new StringBuilder();
				for (String arg : args) {
					builder.append(arg);
					builder.append(" ");
				}
				return builder.toString();
			}
		}

		final TestCase[] cases = {
				new TestCase(new String[]{"help"}, new String[]{TestCommand.USAGE, "or Try /TestCommand help [ commands | help | test ]"}),
				new TestCase(new String[]{"commands"}, new String[]{"commands | help | test"}),
				new TestCase(new String[]{"help", "commands"}, new String[]{"commands - list available subcommands"}),
				new TestCase(new String[]{"test"}, new String[]{"doTest(i)"}),
				new TestCase(new String[]{"test", "1"}, new String[]{"doTest(i,1)"}),
				new TestCase(new String[]{"test", "1", "2"}, new String[]{"doTest(i,1,2)"}),
				new TestCase(new String[]{}, new String[]{"doIt(i)"}),
				new TestCase(new String[]{"notAMethod"}, new String[]{"doIt(i,notAMethod)"}),
				new TestCase(new String[]{"odle"}, new String[]{"doIt(i,odle)"}),
		};

		for (TestCase testCase : cases) {
			localSender.reset();
			cmd.execute(null, localSender, testCase.args);

			if (localSender.commandOutput.size() != testCase.results.length) {
				fail(String.format("wrong number of lines output for test case: %s. Expected %d but received %d.",
						testCase.toString(), testCase.results.length, localSender.commandOutput.size()));

			} else {
				for (int i = 0; i < testCase.results.length; i++) {
					assertTrue(testCase.results[i].equals(localSender.commandOutput.get(i)),
							String.format("'%s' did not get expected output. \nexpected: '%s'\nreceived: '%s'",
									testCase.toString(), testCase.results[i], localSender.commandOutput.get(i)));
				}
			}

		}
	}

	//------------ Command used for testing

	public static final class TestCommand extends GenericCommand {
		public static final String NAME = "TestCommand";
		public static final String USAGE = "TestCommand Usage Text";
		public static final String[] ALIASES = {"Alias1", "Alias2"};

		public TestCommand() {
			super(NAME, USAGE, ALIASES);
		}

		@CommandMethod
		public void doTest(ICommandSender sender) {
			sendMsg(sender, "doTest(i)");
		}

		@CommandMethod
		public void doTest(ICommandSender sender, String arg1) {
			sendMsg(sender, "doTest(i," + arg1 + ")");
		}

		@CommandMethod
		public void doTest(ICommandSender sender, String arg1, String arg2) {
			sendMsg(sender, "doTest(i," + arg1 + "," + arg2 + ")");
		}

		@CommandMethod
		public void doTest(ICommandSender sender, String arg1, String arg2, String arg3) {
			sendMsg(sender, "doTest(i," + arg1 + "," + arg2 + "," + arg3 + ")");
		}

		@CommandMethod
		public void doIt(ICommandSender sender) {
			sendMsg(sender, "doIt(i)");
		}

		@CommandMethod
		public void doIt(ICommandSender sender, String arg1) {
			sendMsg(sender, "doIt(i," + arg1 + ")");
		}

		@CommandMethod
		public void doodle(ICommandSender sender) {
			sendMsg(sender, "Why did you call doodle?");
		}
	}

	//------------ classes to stub out the minecraft interface

	// used for testOnlyRespondsToRemoteCommands
	private static final class DummyCommandSender implements ICommandSender {
		public List<String> commandOutput;
		public boolean isRemote;

		public DummyCommandSender(boolean remote) {
			isRemote = remote;
			reset();
		}

		public void reset() {
			commandOutput = new ArrayList<>();
		}

		@Override
		public String getName() {
			return "DummyCommandSender";
		}

		@Override
		public void sendMessage(ITextComponent component) {
			commandOutput.add(component.getUnformattedText());
		}

		@Override
		public boolean canUseCommand(int permLevel, String commandName) {
			return true;
		}

		@Override
		public World getEntityWorld() {
			// return a new world with the isRemote flag we want.  It
			// will probably barf if it's used for anything else.
			return new World(null, null, new WorldProviderSurface(), new Profiler(), isRemote) {
				@Override
				protected IChunkProvider createChunkProvider() {
					return null;
				}

				@Override
				protected boolean isChunkLoaded(int x, int z, boolean allowEmpty) {
					return false;
				}
			};
		}

		@Override
		public MinecraftServer getServer() {
			return null;
		}

	}
}

