package com.example.examplemodtests.utilities;

import com.example.examplemod.utilities.commands.InvalidValueException;
import com.example.examplemod.utilities.commands.Setting;
import com.example.examplemod.utilities.commands.SettingAccessor;
import com.example.examplemod.utilities.commands.SettingNotFoundException;
import com.example.examplemodtests.testUtilities.HackTestHarness;

public class GenericSettingsTest extends HackTestHarness.Suite {

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
