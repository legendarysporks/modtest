package com.example.examplemodtests.utilities;

import com.example.examplemod.utilities.commands.InvalidValueException;
import com.example.examplemod.utilities.commands.TypeConversionHelper;
import com.example.examplemodtests.testUtilities.TestExecution;

import java.util.HashMap;

public class TypeConversionHelperTest extends TestExecution.Suite {
	public void testPrimativeConversions() throws InvalidValueException {
		HashMap<String, Integer> testMap = new HashMap<>();

		testMap.put("1", 111);
		testMap.put("2", 222);

		String s = testMap.toString();
		HashMap<String, Integer> resultMap = new HashMap<>();
		TypeConversionHelper.convertStringToMap("lksjdf", s, Integer.class, resultMap);
		assertTrue(resultMap.get("1") == 111);
		assertTrue(resultMap.get("2") == 222);
	}
}