package com.example.examplemod.batfight.common;

import com.example.examplemod.utilities.commands.CommandMethod;
import com.example.examplemod.utilities.commands.GenericCommand;
import net.minecraft.command.ICommandSender;

public class BatFightCommand extends GenericCommand {
	private static final String NAME = "batfight";
	private static final String USAGE = "batfight ([add word] | [remove word] | [removeall])";
	private static final String[] ALIASES = {"bf"};

	public BatFightCommand() {
		super(NAME, USAGE, ALIASES);
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
