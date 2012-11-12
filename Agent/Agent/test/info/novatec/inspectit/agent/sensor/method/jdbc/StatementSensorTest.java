package info.novatec.inspectit.agent.sensor.method.jdbc;

import static org.mockito.Mockito.verifyNoMoreInteractions;
import info.novatec.inspectit.agent.core.IIdManager;
import info.novatec.inspectit.agent.test.AbstractLogSupport;
import info.novatec.inspectit.util.Timer;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import org.mockito.Mock;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class StatementSensorTest extends AbstractLogSupport {

	StatementSensor sqlTimerSensor;

	@Mock
	Timer timer;

	@Mock
	IIdManager idManager;

	@Override
	protected Level getLogLevel() {
		return Level.OFF;
	}

	@BeforeMethod(dependsOnMethods = { "initMocks" })
	public void initTestClass() {
		sqlTimerSensor = new StatementSensor(timer, idManager);
	}

	@Test
	public void initSensor() {
		Map<String, Object> map = new HashMap<String, Object>();
		sqlTimerSensor.init(map);
		verifyNoMoreInteractions(timer, idManager);
	}

}
