package info.novatec.inspectit.agent.analyzer.test.classes;

/**
 * DO NOT MODIFY THIS CLASS UNLESS YOU KNOW WHAT YOU'RE DOING.
 * 
 * @author Eduard Tudenhoefner
 * 
 */
public class ExceptionalTestClass {
	public ExceptionalTestClass() {
	}

	public ExceptionalTestClass(String message) throws MyTestException {
		throw new MyTestException(message);
	}

}
