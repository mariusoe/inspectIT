package info.novatec.inspectit.agent.util.test;

import static org.testng.Assert.assertEquals;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import info.novatec.inspectit.agent.test.AbstractLogSupport;
import info.novatec.inspectit.util.StringConstraint;

public class StringConstraintTest extends AbstractLogSupport {

	private StringConstraint constraint;
	
	private Map<String, String> parameter;
	
	private int stringLength;
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Level getLogLevel() {
		return Level.OFF;
	}
	
	@BeforeClass
	public void init() {
		stringLength = new Random().nextInt(1000) + 1;
		parameter = new HashMap<String, String>();
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
		
		assertEquals(resultString == testStr, true);
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
	
	private String fillString(char character, int count) {
		// creates a string of 'x' repeating characters
		char[] chars = new char[count];
		while (count > 0) {
			chars[--count] = character;
		}
		return new String(chars);
	}
}
