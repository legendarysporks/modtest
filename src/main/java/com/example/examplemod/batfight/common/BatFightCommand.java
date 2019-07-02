package com.example.examplemod.batfight.common;

import com.example.examplemod.utilities.GenericCommand;
import net.minecraft.command.ICommandSender;

public class BatFightCommand extends GenericCommand {
	private static final String name = "batfight";
	private static final String usage = "batfight ([add word] | [remove word] | [removeall])";
	private static final String[] aliases = {"bf"};
	private static final int MAX_WORD_DUMP_LINE_LENGTH = 100;

	public BatFightCommand() {
		super(name, usage, aliases);
	}

	@CommandMethod(help = "batfight add <word> - adds a word to the list of words")
	public void doAdd(ICommandSender sender, String word) {
		BatFightWords.addWord(word);
		sendMsg(sender, "'" + word + "' added.");
	}

	@CommandMethod(help = "batfight remove <word> - removes a word to the list of words")
	public void doRemove(ICommandSender sender, String word) {
		BatFightWords.removeWord(word);
		sendMsg(sender, "'" + word + "' removed.");
	}

	@CommandMethod(help = "batfight removeAll - removes all words from the list of words")
	public void doRemoveAll(ICommandSender sender) {
		BatFightWords.removeAll();
		sendMsg(sender, "All words removed.");
	}

	@CommandMethod(help = "batfight something 1 2 3")
	public void doWords(ICommandSender sender) {
		sendMsg(sender, BatFightWords.getAllWords());
	}
}
