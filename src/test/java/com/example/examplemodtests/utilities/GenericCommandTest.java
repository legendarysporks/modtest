package com.example.examplemodtests.utilities;

import com.example.examplemod.utilities.GenericCommand;
import com.example.examplemodtests.testUtilities.TestSuiteResults;
import net.minecraft.command.ICommandSender;
import net.minecraft.profiler.Profiler;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraft.world.WorldProviderSurface;
import net.minecraft.world.chunk.IChunkProvider;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class GenericCommandTest extends TestSuiteResults.Suite {
	private static final String dummyName = "DummyName";
	private static final String dummyUsage = "DummyUsage";
	private static final String dummyAlias1 = "DummyAlias1";
	private static final String dummyAlias2 = "DummyAlias2";
	private static final String[] helpSubcommandArgs = {"help"};

	private DummyCommandSender remoteSender;
	private DummyCommandSender localSender;
	private GenericCommand cmd;

	public void setup(Logger logger) {
		remoteSender = new DummyCommandSender(true);
		localSender = new DummyCommandSender(false);
		cmd = new GenericCommand(dummyName, dummyUsage, dummyAlias1, dummyAlias2);
	}

	public void teardown(Logger logger) {
		remoteSender = null;
		localSender = null;
		cmd = null;
	}

	public void testGetName(Logger logger) {
		assertTrue(cmd.getName().equals(dummyName), "name not returned correctly");
	}

	public void testGetUsage(Logger logger) {
		assertTrue(cmd.getUsage(localSender).equals(dummyUsage), "usage not returned correctly");
		assertTrue(cmd.getUsage(remoteSender).equals(dummyUsage), "usage not returned correctly");
	}

	public void testGetAliases(Logger logger) {
		List<String> aliases = cmd.getAliases();
		assertTrue((aliases.size() == 2) && aliases.contains(dummyAlias1) && aliases.contains(dummyAlias2));
	}

	public void testOnlyRespondsToRemoteCommands(Logger logger) {
		remoteSender.reset();
		cmd.execute(null, remoteSender, helpSubcommandArgs);
		assertTrue(remoteSender.messagesSent.size() == 0, "Command executed on remote world when it shouldn't have");

		localSender.reset();
		cmd.execute(null, localSender, helpSubcommandArgs);
		assertTrue(localSender.messagesSent.size() > 0, "Command did not execute");
	}

	public void testGlobalHelp(Logger logger) {

	}

	//------------ classes to stub out the minecraft interface

	// used for testOnlyRespondsToRemoteCommands
	private static class DummyCommandSender implements ICommandSender {
		public List<ITextComponent> messagesSent;
		public boolean isRemote;

		public DummyCommandSender(boolean remote) {
			isRemote = remote;
			reset();
		}

		public void reset() {
			messagesSent = new ArrayList<>();
		}

		@Override
		public String getName() {
			return "DummyCommandSender";
		}

		@Override
		public void sendMessage(ITextComponent component) {
			messagesSent.add(component);
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

