package info.novatec.inspectit.agent.sensor.method.jdbc;

import info.novatec.inspectit.agent.core.impl.IdManager;
import info.novatec.inspectit.agent.test.AbstractLogSupport;
import info.novatec.inspectit.util.Timer;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.logging.Level;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class PreparedStatementHookTest extends AbstractLogSupport {

	@Mock
	private Timer timer;

	@Mock
	private IdManager idManager;

	@Mock
	private StatementStorage statementStorage;

	@Mock
	private NoSuchElementException myNoSuchElementException;

	@Mock
	private Map parameter;

	@BeforeMethod(dependsOnMethods = { "initMocks" })
	public void initTestClass() {
		Mockito.doThrow(myNoSuchElementException).when(statementStorage).addPreparedStatement(Mockito.anyObject());
	}

	@Test
	public void exceptionLoggingTest() {
		PreparedStatementHook hook = new PreparedStatementHook(timer, idManager, statementStorage, parameter);

		// Throwing the same exception a few times... (as statement storage always raises the
		// exception)
		hook.afterConstructor(null, 1, 10, "someObject", null, null);
		hook.afterConstructor(null, 1, 10, "someObject", null, null);
		hook.afterConstructor(null, 1, 10, "someObject", null, null);
		hook.afterConstructor(null, 1, 10, "someObject", null, null);
		hook.afterConstructor(null, 1, 10, "someObject", null, null);
		hook.afterConstructor(null, 1, 10, "someObject", null, null);

		// ... we still should get only one printing out of the exception
		Mockito.verify(myNoSuchElementException, Mockito.times(1)).printStackTrace();

		// ... if we have a different Statement (meaning different methodId) it should
		// print it out again
		hook.afterConstructor(null, 2, 10, "someObject", null, null);
		hook.afterConstructor(null, 2, 10, "someObject", null, null);
		hook.afterConstructor(null, 2, 10, "someObject", null, null);
		hook.afterConstructor(null, 2, 10, "someObject", null, null);
		hook.afterConstructor(null, 2, 10, "someObject", null, null);

		Mockito.verify(myNoSuchElementException, Mockito.times(2)).printStackTrace();
	}

	@Override
	protected Level getLogLevel() {
		return Level.OFF;
	}

}
