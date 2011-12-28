package info.novatec.inspectit.agent.util.test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotSame;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;
import info.novatec.inspectit.agent.test.AbstractLogSupport;
import info.novatec.inspectit.util.StringConstraint;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;

import org.apache.commons.lang.ObjectUtils;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class StringConstraintTest extends AbstractLogSupport {

	private StringConstraint constraint;

	private Map<String, Object> parameter;

	private int stringLength;

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Level getLogLevel() {
		return Level.OFF;
	}

	@BeforeMethod
	public void init() {
		stringLength = new Random().nextInt(1000) + 1;
		parameter = new HashMap<String, Object>();
		parameter.put("stringLength", String.valueOf(stringLength));
		constraint = new StringConstraint(parameter);
	}

	@Test
	public void stringTooLong() {
		String testStr = fillString('x', stringLength + 1);
		String resultString = constraint.crop(testStr);
		String ending = "...";

		assertEquals(resultString.length(), stringLength + ending.length());
		assertEquals(resultString.endsWith(ending), true);
	}

	@Test
	public void stringTooLongWithFinalChar() {
		char finalChar = '\'';
		String testStr = fillString('x', stringLength + 1);
		testStr += finalChar;
		String resultString = constraint.cropKeepFinalCharacter(testStr, finalChar);
		String ending = "..." + finalChar;

		assertEquals(resultString.length(), stringLength + ending.length());
		assertEquals(resultString.endsWith(ending), true);
	}

	@Test
	public void stringShortEnough() {
		String testStr = fillString('x', stringLength - 1);
		String resultString = constraint.crop(testStr);

		assertEquals(resultString == testStr, true);
		assertEquals(resultString.length(), testStr.length());
	}

	@Test
	public void stringExactSize() {
		String testStr = fillString('x', stringLength);
		String resultString = constraint.crop(testStr);

		assertSame(resultString, testStr);
		assertEquals(resultString.length(), testStr.length());
	}

	@Test
	public void stringLengthOf0() {
		parameter.put("stringLength", String.valueOf(0));
		StringConstraint constr = new StringConstraint(parameter);

		String testStr = fillString('x', 100);
		String resultString = constr.crop(testStr);

		assertEquals(resultString, "");
	}

	@Test
	public void stringLengthOf0WithFinalChar() {
		char finalChar = '\'';
		parameter.put("stringLength", String.valueOf(0));
		StringConstraint constr = new StringConstraint(parameter);

		String testStr = finalChar + fillString('x', 50) + finalChar;
		String resultString = constr.cropKeepFinalCharacter(testStr, finalChar);

		assertEquals(resultString, "");
	}

	@Test
	public void stringIsNull() {
		String testStr = null;
		String resultString = constraint.crop(testStr);

		assertEquals(resultString == null, true);
	}

	@Test
	public void cropStringMapNoCropping() {

		constraint = new StringConstraint(new HashMap<String, Object>() {
			{
				put("stringLength", "20");
			}
		});

		final String param1 = "p1";
		final String param2 = "p2";
		final String param3 = "p3";
		final String param1VReal = "value";
		final String param2VReal1 = "value5";
		final String param2VReal2 = "value6";
		final String param3VReal1 = "value7";
		final String param3VReal2 = "value8";
		final String[] param1V = new String[] { param1VReal };
		final String[] param2V = new String[] { param2VReal1, param2VReal2 };
		final String[] param3V = new String[] { param3VReal1, param3VReal2 };
		final Map<String, String[]> parameterMap = new HashMap<String, String[]>() {
			{
				put(param1, param1V);
				put(param2, param2V);
				put(param3, param3V);
			}
		};

		Map<String, String[]> result = constraint.crop(parameterMap);

		assertEquals(result.size(), parameterMap.size());
		assertTrue(ObjectUtils.equals(result, parameterMap));
		assertSame(result.get(param1), param1V);
		assertSame(result.get(param2), param2V);
		assertSame(result.get(param3), param3V);
		assertSame(result.get(param1)[0], param1VReal);
		assertSame(result.get(param2)[0], param2VReal1);
		assertSame(result.get(param2)[1], param2VReal2);
		assertSame(result.get(param3)[0], param3VReal1);
		assertSame(result.get(param3)[1], param3VReal2);
	}

	@Test
	/** Tests whether the first entry is correctly copied to new map */
	public void cropStringMapCropSecondEntry() {
		constraint = new StringConstraint(new HashMap<String, Object>() {
			{
				put("stringLength", "20");
			}
		});

		final String param1 = "p1";
		final String param2 = "p2";
		final String param3 = "p3";
		final String param1VReal = "value";
		final String param2VReal1 = "I am really very long and need to be cropped";
		final String param2VReal2 = "value6";
		final String param3VReal1 = "value7";
		final String param3VReal2 = "value8";
		final String[] param1V = new String[] { param1VReal };
		final String[] param2V = new String[] { param2VReal1, param2VReal2 };
		final String[] param3V = new String[] { param3VReal1, param3VReal2 };
		final Map<String, String[]> parameterMap = new HashMap<String, String[]>() {
			{
				put(param1, param1V);
				put(param2, param2V);
				put(param3, param3V);
			}
		};

		Map<String, String[]> result = constraint.crop(parameterMap);

		assertEquals(result.size(), parameterMap.size());
		assertFalse(ObjectUtils.equals(result, parameterMap), "need to be cropped");
		assertSame(result.get(param1), param1V);
		assertNotSame(result.get(param2), param2V);
		assertSame(result.get(param3), param3V);
		assertSame(result.get(param1)[0], param1VReal);
		assertNotSame(result.get(param2)[0], param2VReal1);
		assertSame(result.get(param2)[1], param2VReal2);
		assertSame(result.get(param3)[0], param3VReal1);
		assertSame(result.get(param3)[1], param3VReal2);
	}

	private String fillString(char character, int count) {
		// creates a string of 'x' repeating characters
		char[] chars = new char[count];
		while (count > 0) {
			chars[--count] = character;
		}
		return new String(chars);
	}
}
