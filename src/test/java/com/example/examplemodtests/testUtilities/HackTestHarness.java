package com.example.examplemodtests.testUtilities;

import java.io.PrintWriter;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.Logger;

public class HackTestHarness {
	private Suite suite;
	private boolean terminateOnFailure = true;
	public final List<String> testOrder = new ArrayList<>();
	public final List<TestFailure> testFailures = new ArrayList<>();
	public Logger logger;

	@Retention(RetentionPolicy.RUNTIME)
	public @interface TestMetaInfo {
		boolean terminateOnFailure() default true;
	}

	public static final class TestFailure {
		public final String testName;
		public final Throwable error;

		public TestFailure(String testName, String msg) {
			this.testName = testName;
			this.error = new TestFailureError(msg);
		}
		public TestFailure(String testName, Throwable error) {
			this.testName = testName;
			this.error = error;
		}
	}

	public static class Suite {
		private String testName;
		private HackTestHarness harness;

		public void setup() {
		}

		public void teardown() {
		}

		public void assertTrue(boolean condition) {
			assertFalse(!condition);
		}

		public void assertTrue(boolean condition, String message) {
			assertFalse(!condition, message);
		}

		public void assertFalse(boolean condition) {
			if (condition) {
				fail();
			}
		}

		public void assertFalse(boolean condition, String message) {
			if (condition) {
				fail(message);
			}
		}

		public void fail(String msg) {
			if (harness.terminateOnFailure) {
				// this will be caught in runTestMethods and log the error there
				// thereby terminating the test case
				throw new TestFailureError(msg);
			} else {
				// just log the error and continue
				harness.logError(testName, new TestFailureError(msg));
			}
		}

		public void fail() {
			if (harness.terminateOnFailure) {
				// throwing an error will terminate the test case and it will be caught in
				// runTestMethods where it will be logged.
				throw new TestFailureError(testName + " failed");
				// this will be caught in runTestMethods and log the error there
			} else {
				// just log the error and continue
				harness.logError(testName, new TestFailureError(testName + " failed"));
			}
		}

		protected HackTestHarness getTestHarness() {
			return harness;
		}

		private void setTestContext(String testName, HackTestHarness harness) {
			this.testName = testName;
			this.harness = harness;
		}
	}

	public static class TestFailureError extends Error {
		public TestFailureError() {
			super();
		}

		public TestFailureError(String msg) {
			super(msg);
		}
	}

	public static HackTestHarness run(Class<? extends Suite> testClass, Logger logger) {

		HackTestHarness harness = new HackTestHarness(testClass, logger);
		harness.runTests();
		return harness;
	}

	/*
		public static HackTestHarness run(Suite suite) {
			HackTestHarness harness = new HackTestHarness(suite, null);
			harness.runTests();
			return harness;
		}

		public static HackTestHarness run(Suite suite, PrintWriter outputLog) {
			HackTestHarness harness = new HackTestHarness(suite, outputLog);
			harness.runTests();
			return harness;
		}
	*/
	private HackTestHarness(Class<? extends Suite> testClass, Logger outputLog) {
		try {
			suite = testClass.newInstance();
		} catch (Exception e) {
			e.printStackTrace();
		}
		this.logger = outputLog;
	}

	private void runTests() {
		try {
			suite.setup();
			Class<?> clazz = suite.getClass();
			while (clazz != Object.class) {
				runTestMethods(clazz);
				clazz = clazz.getSuperclass();
			}
		} finally {
			suite.teardown();
		}
	}

	private void runTestMethods(Class<?> testClass) {
		String className = testClass.getName();
		for (Method method : testClass.getDeclaredMethods()) {
			if (hasTestMethodSigniture(method)) {
				String testName = className +"."+method.getName();
				suite.setTestContext(testName, this);
				testOrder.add(testName);
				try {
					TestMetaInfo annotation = method.getAnnotation(TestMetaInfo.class);
					if (annotation != null) {
						terminateOnFailure = annotation.terminateOnFailure();
					}
					if (logger != null) {
						int beforeFailureCount = testFailures.size();
						print("Running " + testName);
						try {
							method.invoke(suite);
						} catch (Throwable e) {
							logError(testName, e);
						}
						if (beforeFailureCount == testFailures.size()) {
							print("  passed");
						} else {
							print("  FAILED");
						}
					} else {
						try {
							method.invoke(suite);
						} catch (Throwable e) {
							logError(testName, e);
						}
					}
				} finally {
					terminateOnFailure = true;
				}
			}
		}
	}

	/* Check to see if a given method is of the form "void test(PrintWriter)" */
	private boolean hasTestMethodSigniture(Method method) {
		Boolean result = method.getName().startsWith("test");
//		result = result && (method.getReturnType() == Void.class);
		result = result && (method.getParameterTypes().length == 0);
		return result;
	}

	private void logError(String testName, Throwable err) {
		Throwable cause = err.getCause();
		if (cause != null) {
			testFailures.add(new TestFailure(testName, cause));
			print("  ** FAILED");
			print("  ** "+cause);
		} else {
			testFailures.add(new TestFailure(testName, err));
			print("  ** FAILED");
			print("  ** "+err);
		}
	}

	private void print(String s) {
		logger.info(s);
	}

	private void print(Throwable t) {
		logger.info(t);
	}

	public class SuiteTest extends Suite {
		@TestMetaInfo
		public void testStuff(HackTestHarness harness) {
			fail("-testStuff-");
		}

		@TestMetaInfo(terminateOnFailure = false)
		public void testContinueOnFailure(HackTestHarness harness) {
			fail("-testContinueOnFailure 1-");
			fail("-testContinueOnFailure 2-");
		}

		public void testHomegrownException(HackTestHarness harness) {
			throw new Error("-testHomegrownException error-");
		}

		public void testSomethingPasses(HackTestHarness harness) {

		}
	}
}
