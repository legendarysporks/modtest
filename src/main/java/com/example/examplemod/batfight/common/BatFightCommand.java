package com.example.examplemod.batfight.common;

import com.example.examplemod.utilities.GenericCommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.text.TextComponentString;

public class BatFightCommand extends GenericCommand {
	private static final String name = "batfight";
	private static final String usage = "batfight ([add word] | [remove word] | [removeall])";
	private static final String[] aliases = {"bf"};

	public BatFightCommand() {
		super(name, usage, aliases);
	}

	@GenericCommand.Meta(help = "batfight add <word> - adds a word to the list of words")
	public void doAdd(ICommandSender sender, String word) {
		BatFightWords.addWord(word);
		sender.sendMessage(new TextComponentString("'" + word + "' added."));
	}

	@GenericCommand.Meta(help = "batfight remove <word> - removes a word to the list of words")
	public void doRemove(ICommandSender sender, String word) {
		BatFightWords.removeWord(word);
		sender.sendMessage(new TextComponentString("'" + word + "' removed."));
	}

	@GenericCommand.Meta(help = "batfight removeAll - removes all words from the list of words")
	public void doRemoveAll(ICommandSender sender, String word) {
		BatFightWords.removeAll();
		sender.sendMessage(new TextComponentString("All words removed."));
	}
}
