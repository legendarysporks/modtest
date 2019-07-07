package com.example.examplemodtests.utilities;

import com.example.examplemod.utilities.commands.InvalidValueException;
import com.example.examplemod.utilities.commands.Setting;
import com.example.examplemod.utilities.commands.SettingAccessor;
import com.example.examplemod.utilities.commands.SettingNotFoundException;
import com.example.examplemodtests.testUtilities.TestExecution;

public class SettingAccessorTest extends TestExecution.Suite {

	public void testFieldSettings() throws SettingNotFoundException, InvalidValueException {
		SettingsTestClass s = new SettingsTestClass();
		SettingAccessor settings = new SettingAccessor(s);

		assertTrue(settings.get("firstName").equals("firstName-value"));
		settings.set("firstname", "firstName-value2");
		assertTrue(settings.get("FirstName").equals("firstName-value2"));
	}

	public void testSettings() throws SettingNotFoundException, InvalidValueException {
		SettingsTestClass s = new SettingsTestClass();
		SettingAccessor settings = new SettingAccessor(s);

		assertTrue(settings.get("ssn").equals("ssn-value"));
		settings.set("ssn", "ssn-value2");
		assertTrue(settings.get("ssn").equals("ssn-value2"));

		assertTrue(settings.get("age").equals("42"));
		settings.set("age", "43");
		assertTrue(settings.get("age").equals("43"));
	}

	public void testSettingAccess() {
		SettingsTestClass s = new SettingsTestClass();
		SettingAccessor settings = new SettingAccessor(s);
		try {
			settings.get("readOnlySetting");
		} catch (SettingNotFoundException e) {
			fail("could not access read-only setting");
		}
		try {
			settings.set("readOnlySetting", "newValue");
			fail("Was able to set a read-only setting");
		} catch (InvalidValueException e) {
			fail("Incorrent exception thrown");
		} catch (SettingNotFoundException e) {
		}

		try {
			settings.get("privateSetting");
			fail("Was able to get private field as a setting");
		} catch (SettingNotFoundException e) {
		}
		try {
			settings.set("privateSetting", "newValue");
			fail("Was able to set a private field as a setting");
		} catch (InvalidValueException e) {
			fail("Was able to access private field as a setting");
		} catch (SettingNotFoundException e) {
		}
	}

	public static class SettingsTestClass {
		// nothing.  Nada
		public String notASetting = "notASetting-value";
		@Setting
		public String firstName = "firstName-value";
		@Setting
		public int age = 42;
		// has getters/setters
		private String ssn = "ssn-value";
		// only has a getter
		private String readOnlySetting = "readOnlySetting-value";
		// shoudn't be available because it is not public
		@Setting
		private String privateSetting;

		// shoudn't be available because it is not public
		@Setting
		private String getPrivateStuff() {
			return "Uh oh";
		}

		@Setting
		public String getSsn() {
			return ssn;
		}

		@Setting
		public void setSsn(String ssn) {
			this.ssn = ssn;
		}

		@Setting
		public String getReadOnlySetting() {
			return readOnlySetting;
		}
	}
}
