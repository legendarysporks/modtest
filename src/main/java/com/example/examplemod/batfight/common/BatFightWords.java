package com.example.examplemod.batfight.common;

import java.util.Random;

public class BatFightWords {
	private static final Random rand = new Random();
	private static final String words[] = {
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
			"ZZZZZWAP"
	};

	public static String getWord() {
		return words[rand.nextInt(words.length)];
	}
}