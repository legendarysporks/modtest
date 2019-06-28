package com.example.examplemodtests.testUtilities;

import org.apache.logging.log4j.Logger;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestSuiteResults {
	public final Suite suite;
	public final List<String> testOrder = new ArrayList<>();
	public final Map<String, Throwable> testFailures = new HashMap<>();

	private TestSuiteResults(Suite suite, Logger outputLog) {
		this.suite = suite;
		runTests(outputLog);
	}

	public static TestSuiteResults run(Suite suite) {
		return new TestSuiteResults(suite, null);
	}

	public static TestSuiteResults run(Suite suite, Logger outputLog) {
		return new TestSuiteResults(suite, outputLog);
	}

	private void runTests(Logger outputLog) {
		try {
			suite.setup(outputLog);
			Class<?> clazz = suite.getClass();
			while (clazz != Object.class) {
				runTestMethods(clazz, outputLog);
				clazz = clazz.getSuperclass();
			}
		} finally {
			suite.teardown(outputLog);
		}
	}

	private void runTestMethods(Class<?> testClass, Logger logger) {
		String className = testClass.getName();
		for (Method method : testClass.getDeclaredMethods()) {
			if (hasTestMethodSigniture(method)) {
				String testName = method.getName();
				testOrder.add(testName);
				if (logger != null) {
					try {
						logger.info("Running " + className + "." + testName);
						method.invoke(suite, logger);
						logger.info("  passed");
					} catch (Throwable e) {
						Throwable cause = e.getCause();
						testFailures.put(testName, cause);
						logger.info("  ** FAILED");
						logger.debug(cause);
					}
				} else {
					try {
						method.invoke(suite);
					} catch (Throwable e) {
						testFailures.put(testName, e);
					}
				}
			}
		}
	}

	/* Check to see if a given method is of the form "void test(Logger)" */
	private boolean hasTestMethodSigniture(Method method) {
		Boolean result = method.getName().startsWith("test");
//		result = result && (method.getReturnType() == Void.class);
		result = result && (method.getParameterCount() == 1);
		result = result && (method.getGenericParameterTypes()[0] == Logger.class);
		return result;
	}

	public abstract static class Suite {
		public void setup(Logger outputLog) {
		}

		public void teardown(Logger outputLog) {
		}

		public void assertTrue(boolean condition) {
			if (!condition) {
				throw new TestAssertionFailedError("AssertTrue() failed");
			}
		}

		public void assertTrue(boolean condition, String message) {
			if (!condition) {
				throw new TestAssertionFailedError("AssertTrue() failed: " + message);
			}
		}
	}

	public static class TestAssertionFailedError extends Error {
		private String msg;

		public TestAssertionFailedError(String msg) {
			this.msg = msg;
		}

		public String toString() {
			return msg;
		}
	}
}
