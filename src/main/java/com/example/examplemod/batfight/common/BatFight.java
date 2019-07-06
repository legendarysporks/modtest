package com.example.examplemod.batfight.common;

import com.example.examplemod.utilities.commands.CommandMethod;
import com.example.examplemod.utilities.commands.GenericCommand;
import net.minecraft.command.ICommandSender;
import net.minecraftforge.fml.common.SidedProxy;

/*
Generic BatFight interface used by the main mod.  Just used to initialize the
fight.
 */
public class BatFight {
	private static final String NAME = "batfight";
	private static final String USAGE = "batfight ([add word] | [remove word] | [removeall])";
	private static final String[] ALIASES = {"bf"};

	@SidedProxy(clientSide = "com.example.examplemod.batfight.client.BatFightClient",
			serverSide = "com.example.examplemod.batfight.server.BatFightServer")
	public static BatFight proxy;

	public BatFight() {
		GenericCommand.create(NAME, USAGE, ALIASES).addTarget(this);
	}

	@CommandMethod(help = "batfight add <word> - adds a word to the list of words")
	public void doAdd(ICommandSender sender, String word) {
		BatFightWords.addWord(word);
		GenericCommand.sendMsg(sender, "'" + word + "' added.");
	}

	@CommandMethod(help = "batfight remove <word> - removes a word to the list of words")
	public void doRemove(ICommandSender sender, String word) {
		BatFightWords.removeWord(word);
		GenericCommand.sendMsg(sender, "'" + word + "' removed.");
	}

	@CommandMethod(help = "batfight removeAll - removes all words from the list of words")
	public void doRemoveAll(ICommandSender sender) {
		BatFightWords.removeAll();
		GenericCommand.sendMsg(sender, "All words removed.");
	}

	@CommandMethod(help = "batfight something 1 2 3")
	public void doWords(ICommandSender sender) {
		GenericCommand.sendMsg(sender, BatFightWords.getAllWords());
	}
}
