package info.novatec.inspectit.agent.analyzer.test.classes;

@SuppressWarnings("PMD")
public class MyTestError extends Error {
	MyTestError() {
		super();
	}

	MyTestError(String message) {
		super(message);
	}

	MyTestError(Throwable cause) {
		super(cause);
	}

}
