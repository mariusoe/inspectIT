package info.novatec.novaspy.agent.sensor.method.jdbc.test;

import static org.mockito.Mockito.verifyNoMoreInteractions;
import info.novatec.novaspy.agent.config.IPropertyAccessor;
import info.novatec.novaspy.agent.core.IIdManager;
import info.novatec.novaspy.agent.sensor.method.jdbc.StatementSensor;
import info.novatec.novaspy.agent.test.AbstractLogSupport;
import info.novatec.novaspy.util.Timer;

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

	@Mock
	IPropertyAccessor propertyAccessor;

	@Override
	protected Level getLogLevel() {
		return Level.OFF;
	}

	@BeforeMethod(dependsOnMethods = { "initMocks" })
	public void initTestClass() {
		sqlTimerSensor = new StatementSensor(timer, idManager, propertyAccessor);
	}

	@Test
	public void initSensor() {
		Map<String, String> map = new HashMap<String, String>();
		sqlTimerSensor.init(map);
		verifyNoMoreInteractions(timer, idManager, propertyAccessor);
	}

}
