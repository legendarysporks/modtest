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

	@Meta(help = "batfight add <word> - adds a word to the list of words")
	public void doAdd(ICommandSender sender, String word) {
		BatFightWords.addWord(word);
		sender.sendMessage(new TextComponentString("'" + word + "' added."));
	}

	@Meta(help = "batfight remove <word> - removes a word to the list of words")
	public void doRemove(ICommandSender sender, String word) {
		BatFightWords.removeWord(word);
		sender.sendMessage(new TextComponentString("'" + word + "' removed."));
	}

	@Meta(help = "batfight removeAll - removes all words from the list of words")
	public void doRemoveAll(ICommandSender sender) {
		BatFightWords.removeAll();
		sender.sendMessage(new TextComponentString("All words removed."));
	}

	@Meta(help = "batfight something 1 2 3")
	public void doSomething(ICommandSender sender, String word1, String word2, String word3) {
		BatFightWords.removeAll();
		sender.sendMessage(new TextComponentString("Words " + word1 + word2 + word3));
	}


}
