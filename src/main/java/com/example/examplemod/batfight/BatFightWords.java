package com.example.examplemod.batfight;

import com.google.common.collect.Lists;

import java.util.Collections;
import java.util.List;
import java.util.Random;

public class BatFightWords {
	private static final Random rand = new Random();
	private static final List<String> words = Lists.newArrayList(
			"AIEEE",
			"AIIEEE",
			"ARRGH",
			"AWK",
			"AWKKKKKK",
			"BAM",
			"BANG",
			"BANG-ETH",
			"BIFF",
			"BLOOP",
			"BLURP",
			"BOFF",
			"BONK",
			"CLANK",
			"CLANK-EST",
			"CLASH",
			"CLUNK",
			"CLUNK-ETH",
			"CRRAACK",
			"CRASH",
			"CRRAACK",
			"CRUNCH",
			"CRUNCH-ETH",
			"EEE-YOW",
			"FLRBBBBB",
			"GLIPP",
			"GLURPP",
			"KAPOW",
			"KAYO",
			"KER-SPLOOSH",
			"KERPLOP",
			"KLONK",
			"KLUNK",
			"KRUNCH",
			"OOOFF",
			"OOOOFF",
			"OUCH",
			"OUCH-ETH",
			"OWWW",
			"OW-ETH",
			"PAM",
			"PLOP",
			"POW",
			"POWIE",
			"QUNCKKK",
			"RAKKK",
			"RIP",
			"SLOSH",
			"SOCK",
			"SPLATS",
			"SPLATT",
			"SPLOOSH",
			"SWAAP",
			"SWISH",
			"SWOOSH",
			"THUNK",
			"THWACK",
			"THWACKE",
			"THWAPE",
			"THWAPP",
			"UGGH",
			"URKKK",
			"VRONK",
			"WHACK",
			"WHACK-ETH",
			"WHAM-ETH",
			"WHAMM",
			"WHAMMM",
			"WHAP",
			"Z-ZWAP",
			"ZAM",
			"ZAMM",
			"ZAMMM",
			"ZAP",
			"ZAP-ETH",
			"ZGRUPPP",
			"ZLONK",
			"ZLOPP",
			"ZLOTT",
			"ZOK",
			"ZOWIE",
			"ZWAPP",
			"ZZWAP",
			"ZZZZWAP",
			"ZZZZZWAP");

	public static void addWord(String word) {
		words.add(word);
	}

	public static void removeWord(String word) {
		words.remove(word);
	}

	public static void removeAll() {
		words.clear();
	}

	public static String getRandomWord() {
		if (words.size() > 0) {
			return words.get(rand.nextInt(words.size()));
		} else {
			return "";
		}
	}

	public static List<String> getAllWords() {
		return Collections.unmodifiableList(words);
	}
}
