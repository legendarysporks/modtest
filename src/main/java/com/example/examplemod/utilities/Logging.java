package com.example.examplemod.utilities;

import com.example.examplemod.Reference;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;

public class Logging {
	public static Logger logger;

	/**
	 * Logging method that is safe to call even before the logger has been properly initialized.  Kind
	 * of a hack for debugging startup code.
	 */
	public static void logInfo(String info) {
		log(Level.INFO, info);
	}

	public static void logTrace(String info) {
		log(Level.INFO, info);
	}

	public static void log(Level level, String info) {
		if (logger != null) {
			logger.log(level, info);
		} else {
			System.out.println(Reference.MODNAME + "." + level + ":" + info);
		}
	}

}
