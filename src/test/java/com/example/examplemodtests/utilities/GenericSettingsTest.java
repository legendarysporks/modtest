package com.example.examplemodtests.utilities;

import com.example.examplemod.utilities.GenericSettings;
import com.example.examplemodtests.testUtilities.HackTestHarness;

public class GenericSettingsTest extends HackTestHarness.Suite {

	public static class SettingsTestClass {
		// has getters/setters
		private String ssn = "ssn-value";

		// only has a getter
		private String readOnlySetting = "readOnlySetting-value";

		// nothing.  Nada
		public String notASetting = "notASetting-value";

		// shoudn't be available because it is not public
		@GenericSettings.Setting
		private String privateSetting;

		// shoudn't be available because it is not public
		@GenericSettings.Setting
		private String getPrivateStuff() {
			return "Uh oh";
		}

		@GenericSettings.Setting
		public String firstName = "firstName-value";

		@GenericSettings.Setting
		public int age = 42;

		@GenericSettings.Setting
		public String getSsn() {
			return ssn;
		}

		@GenericSettings.Setting
		public void setSsn(String ssn) {
			this.ssn = ssn;
		}

		@GenericSettings.Setting
		public String getReadOnlySetting() {
			return readOnlySetting;
		}
	}

	public void testFieldSettings() throws GenericSettings.SettingNotFoundException {
		SettingsTestClass s = new SettingsTestClass();
		GenericSettings settings = new GenericSettings(s);

		assertTrue(settings.get("firstName").equals("firstName-value"));
		settings.set("firstname", "firstName-value2");
		assertTrue(settings.get("FirstName").equals("firstName-value2"));
	}

	public void testSettings() throws GenericSettings.SettingNotFoundException {
		SettingsTestClass s = new SettingsTestClass();
		GenericSettings settings = new GenericSettings(s);

		assertTrue(settings.get("ssn").equals("ssn-value"));
		settings.set("ssn", "ssn-value2");
		assertTrue(settings.get("ssn").equals("ssn-value2"));

		assertTrue(settings.get("age").equals("42"));
		settings.set("age", "43");
		assertTrue(settings.get("age").equals("43"));
	}
}
