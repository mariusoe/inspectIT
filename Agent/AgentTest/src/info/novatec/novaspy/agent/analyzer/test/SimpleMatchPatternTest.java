package info.novatec.novaspy.agent.analyzer.test;

import static org.testng.Assert.assertEquals;
import info.novatec.novaspy.agent.analyzer.impl.SimpleMatchPattern;

import org.testng.annotations.Test;

public class SimpleMatchPatternTest {

	@Test
	public void trailingPattern() {
		SimpleMatchPattern matchPattern = new SimpleMatchPattern("*test");
		assertEquals(matchPattern.match("testtest"), true);
		assertEquals(matchPattern.match("123test"), true);
		assertEquals(matchPattern.match("test"), true);
		assertEquals(matchPattern.match("hello"), false);
		assertEquals(matchPattern.match(""), false);
	}

	@Test
	public void middlePattern() {
		SimpleMatchPattern matchPattern = new SimpleMatchPattern("test*");
		assertEquals(matchPattern.match("testtest"), true);
		assertEquals(matchPattern.match("test123"), true);
		assertEquals(matchPattern.match("test"), true);
		assertEquals(matchPattern.match("hello"), false);
		assertEquals(matchPattern.match(""), false);
	}

	@Test
	public void leadingPattern() {
		SimpleMatchPattern matchPattern = new SimpleMatchPattern("*test*");
		assertEquals(matchPattern.match("testtesttest"), true);
		assertEquals(matchPattern.match("testtest"), true);
		assertEquals(matchPattern.match("123test123"), true);
		assertEquals(matchPattern.match("test123"), true);
		assertEquals(matchPattern.match("test"), true);
		assertEquals(matchPattern.match("hello"), false);
		assertEquals(matchPattern.match(""), false);
	}

	@Test
	public void mixedPattern() {
		SimpleMatchPattern matchPattern = new SimpleMatchPattern("test*hello*world");
		assertEquals(matchPattern.match("test1hello2world"), true);
		assertEquals(matchPattern.match("testhelloworld"), true);
		assertEquals(matchPattern.match("test123helloworld"), true);
		assertEquals(matchPattern.match("hello"), false);
		assertEquals(matchPattern.match(""), false);
	}

	@Test
	public void everythingPattern() {
		SimpleMatchPattern matchPattern = new SimpleMatchPattern("*");
		assertEquals(matchPattern.match("test1hello2world"), true);
		assertEquals(matchPattern.match("testhelloworld"), true);
		assertEquals(matchPattern.match("test123helloworld"), true);
		assertEquals(matchPattern.match("hello"), true);
		assertEquals(matchPattern.match(""), true);
	}

	@Test
	public void enhancedTests() {
		SimpleMatchPattern matchPattern = new SimpleMatchPattern("vsa.nprod.stamm.priv.regelstruktur.server.logic.*.*Evaluator");
		assertEquals(matchPattern.match("vsa.nprod.stamm.priv.regelstruktur.server.logic.rechnungssplitting.RechnungsSplittingEvaluator"), true);
	}

}
