package info.novatec.inspectit.agent.config.test;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import info.novatec.inspectit.agent.config.IConfigurationStorage;
import info.novatec.inspectit.agent.config.ParserException;
import info.novatec.inspectit.agent.config.PriorityEnum;
import info.novatec.inspectit.agent.config.StorageException;
import info.novatec.inspectit.agent.config.impl.FileConfigurationReader;
import info.novatec.inspectit.agent.test.AbstractLogSupport;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.mockito.Mock;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class FileConfigurationReaderTest extends AbstractLogSupport {

	private File file;

	private PrintWriter writer;

	@Mock
	private IConfigurationStorage configurationStorage;

	private FileConfigurationReader fileConfigurationReader;

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Level getLogLevel() {
		return Level.OFF;
	}

	@BeforeMethod(alwaysRun = true)
	public void initConfigurationFile() throws Exception {
		if (null == file) {
			String tmpdir = System.getProperty("java.io.tmpdir");
			file = new File(tmpdir + "/inspectit-agent.cfg");
		} else {
			file.delete();
			file.createNewFile();
		}

		System.setProperty("inspectit.config", System.getProperty("java.io.tmpdir"));
		writer = new PrintWriter(new BufferedWriter(new FileWriter(file)));
	}

	@BeforeMethod(dependsOnMethods = { "initMocks" })
	public void initTestClass() {
		fileConfigurationReader = new FileConfigurationReader(configurationStorage);
	}

	@Test
	public void loadAndVerifyRepository() throws ParserException, StorageException {
		String localhost = "localhost";
		int port = 1099;
		String agentName = "CalculatorTestAgent";

		writer.println("repository " + localhost + " " + port + " " + agentName);
		writer.close();

		fileConfigurationReader.load();

		verify(configurationStorage, times(1)).setRepository(localhost, 1099);
		verify(configurationStorage, times(1)).setAgentName(agentName);
		verifyNoMoreInteractions(configurationStorage);
	}

	@Test
	public void loadAndVerifyMethodSensorType() throws ParserException, StorageException {
		String name = "average-timer";
		String clazz = "info.novatec.inspectit.agent.sensor.method.averagetimer.AverageTimerSensor";
		PriorityEnum priority = PriorityEnum.HIGH;
		String priorityString = (String) PriorityEnum.names().get(new Integer(priority.getValue()));

		writer.println("method-sensor-type " + name + " " + clazz + " " + priorityString);
		writer.close();

		fileConfigurationReader.load();

		verify(configurationStorage, times(1)).addMethodSensorType(name, clazz, priority, Collections.EMPTY_MAP);
		verifyNoMoreInteractions(configurationStorage);
	}

	@Test
	public void loadAndVerifyPlatformSensorType() throws ParserException, StorageException {
		String clazz = "info.novatec.inspectit.agent.sensor.platform.ClassLoadingInformation";

		writer.println("platform-sensor-type " + clazz);
		writer.close();

		fileConfigurationReader.load();

		verify(configurationStorage, times(1)).addPlatformSensorType(clazz, Collections.EMPTY_MAP);
		verifyNoMoreInteractions(configurationStorage);
	}

	@Test
	public void loadAndVerifyExceptionSensorNoParameter() throws ParserException, StorageException {
		String clazz = "info.novatec.inspectit.agent.sensor.exception.ExceptionSensor";
		String name = "exception-sensor";
		String targetClass = "java.lang.Throwable";
		writer.println(name + " " + targetClass);
		writer.close();

		fileConfigurationReader.load();

		verify(configurationStorage, times(1)).addExceptionSensorType(clazz, Collections.EMPTY_MAP);
		verify(configurationStorage, times(1)).addExceptionSensorTypeParameter(clazz, targetClass, false, Collections.EMPTY_MAP);
		verifyNoMoreInteractions(configurationStorage);
	}

	@Test
	public void loadAndVerifyExceptionSensorOneParameter() throws ParserException, StorageException {
		String clazz = "info.novatec.inspectit.agent.sensor.exception.ExceptionSensor";
		String name = "exception-sensor";
		String targetClass = "java.lang.Throwable superclass=true";
		writer.println(name + " " + targetClass);
		writer.close();

		Map<String, String> settings = new HashMap<String, String>();
		settings.put("superclass", "true");

		fileConfigurationReader.load();

		verify(configurationStorage, times(1)).addExceptionSensorType(clazz, settings);
		verify(configurationStorage, times(1)).addExceptionSensorTypeParameter(clazz, "java.lang.Throwable", false, settings);
		verifyNoMoreInteractions(configurationStorage);
	}

	@Test
	public void loadAndVerifyExceptionSensorWildcardParameter() throws ParserException, StorageException {
		String clazz = "info.novatec.inspectit.agent.sensor.exception.ExceptionSensor";
		String name = "exception-sensor";
		String targetClass = "java.lang.*";
		writer.println(name + " " + targetClass);
		writer.close();

		fileConfigurationReader.load();

		verify(configurationStorage, times(1)).addExceptionSensorType(clazz, Collections.EMPTY_MAP);
		verify(configurationStorage, times(1)).addExceptionSensorTypeParameter(clazz, targetClass, true, Collections.EMPTY_MAP);
		verifyNoMoreInteractions(configurationStorage);
	}

	@Test
	public void loadAndVerifyBufferStrategy() throws ParserException, StorageException {
		String clazz = "info.novatec.inspectit.agent.buffer.impl.SimpleBufferStrategy";

		writer.println("buffer-strategy " + clazz);
		writer.close();

		fileConfigurationReader.load();

		verify(configurationStorage, times(1)).setBufferStrategy(clazz, Collections.EMPTY_MAP);
		verifyNoMoreInteractions(configurationStorage);
	}

	@Test
	public void loadAndVerifySendingStrategy() throws ParserException, StorageException {
		String clazz = "info.novatec.inspectit.agent.sending.impl.TimeStrategy";
		String time = "time=5000";

		writer.println("send-strategy " + clazz + " " + time);
		writer.close();

		fileConfigurationReader.load();

		Map<String, String> settings = new HashMap<String, String>();
		settings.put("time", "5000");

		verify(configurationStorage, times(1)).addSendingStrategy(clazz, settings);
		verifyNoMoreInteractions(configurationStorage);
	}

	@Test
	public void loadAndVerifyStandardSensor() throws ParserException, StorageException {
		String sensorTypeName = "isequence";
		String className = "info.novatec.inspectitsamples.calculator.Calculator";
		String methodName = "actionPerformed";

		writer.println("sensor " + sensorTypeName + " " + className + " " + methodName + "() ");
		writer.close();

		fileConfigurationReader.load();

		verify(configurationStorage, times(1)).addSensor(sensorTypeName, className, methodName, Collections.EMPTY_LIST, false, Collections.EMPTY_MAP);
		verifyNoMoreInteractions(configurationStorage);
	}

	@Test
	public void loadAndVerifyRegexSensor() throws ParserException, StorageException {
		String sensorTypeName = "timer";
		String className = "*";
		String methodName = "*";

		writer.println("sensor " + sensorTypeName + " " + className + " " + methodName + "() ");
		writer.close();

		fileConfigurationReader.load();

		verify(configurationStorage, times(1)).addSensor(sensorTypeName, className, methodName, Collections.EMPTY_LIST, false, Collections.EMPTY_MAP);
		verifyNoMoreInteractions(configurationStorage);
	}

	@Test
	public void loadAndVerifySensorIgnoreSignature() throws ParserException, StorageException {
		String sensorTypeName = "isequence";
		String className = "info.novatec.inspectitsamples.calculator.Calculator";
		String methodName = "actionPerformed";

		writer.println("sensor " + sensorTypeName + " " + className + " " + methodName + " ");
		writer.close();

		fileConfigurationReader.load();

		verify(configurationStorage, times(1)).addSensor(sensorTypeName, className, methodName, Collections.EMPTY_LIST, true, Collections.EMPTY_MAP);
		verifyNoMoreInteractions(configurationStorage);
	}

	@Test
	public void loadAndVerifySensorWithOneParameter() throws ParserException, StorageException {
		String sensorTypeName = "isequence";
		String className = "info.novatec.inspectitsamples.calculator.Calculator";
		String methodName = "actionPerformed";
		String parameter = "java.lang.String";

		writer.println("sensor " + sensorTypeName + " " + className + " " + methodName + "(" + parameter + ") ");
		writer.close();

		fileConfigurationReader.load();

		List<String> parameterList = new ArrayList<String>();
		parameterList.add(parameter);

		verify(configurationStorage, times(1)).addSensor(sensorTypeName, className, methodName, parameterList, false, Collections.EMPTY_MAP);
		verifyNoMoreInteractions(configurationStorage);
	}

	@Test
	public void loadAndVerifySensorWithManyParameter() throws ParserException, StorageException {
		String sensorTypeName = "isequence";
		String className = "info.novatec.inspectitsamples.calculator.Calculator";
		String methodName = "actionPerformed";
		String parameterOne = "java.lang.String";
		String parameterTwo = "java.lang.Object";
		String parameterThree = "java.io.File";

		writer.println("sensor " + sensorTypeName + " " + className + " " + methodName + "(" + parameterOne + "," + parameterTwo + "," + parameterThree + ") ");
		writer.close();

		fileConfigurationReader.load();

		List<String> parameterList = new ArrayList<String>();
		parameterList.add(parameterOne);
		parameterList.add(parameterTwo);
		parameterList.add(parameterThree);

		verify(configurationStorage, times(1)).addSensor(sensorTypeName, className, methodName, parameterList, false, Collections.EMPTY_MAP);
		verifyNoMoreInteractions(configurationStorage);
	}

	@Test
	public void loadAndVerifySensorWithParameterRecord() throws ParserException, StorageException {
		String sensorTypeName = "isequence";
		String className = "info.novatec.inspectitsamples.calculator.Calculator";
		String methodName = "actionPerformed";
		String parameterRecord = "0;Source;text";

		writer.println("sensor " + sensorTypeName + " " + className + " " + methodName + "() " + " p=" + parameterRecord);
		writer.close();

		fileConfigurationReader.load();

		Map<String, List<String>> settings = new HashMap<String, List<String>>();
		List<String> propertyList = new ArrayList<String>();
		propertyList.add(parameterRecord);
		settings.put("property", propertyList);

		verify(configurationStorage, times(1)).addSensor(sensorTypeName, className, methodName, Collections.EMPTY_LIST, false, settings);
		verifyNoMoreInteractions(configurationStorage);
	}

	@Test
	public void loadAndVerifySensorWithFieldRecord() throws ParserException, StorageException {
		String sensorTypeName = "isequence";
		String className = "info.novatec.inspectitsamples.calculator.Calculator";
		String methodName = "actionPerformed";
		String fieldRecord = "LastOutput;jlbOutput.text";

		writer.println("sensor " + sensorTypeName + " " + className + " " + methodName + "() " + " f=" + fieldRecord);
		writer.close();

		fileConfigurationReader.load();

		Map<String, List<String>> settings = new HashMap<String, List<String>>();
		List<String> propertyList = new ArrayList<String>();
		propertyList.add(fieldRecord);
		settings.put("field", propertyList);

		verify(configurationStorage, times(1)).addSensor(sensorTypeName, className, methodName, Collections.EMPTY_LIST, false, settings);
		verifyNoMoreInteractions(configurationStorage);
	}

	@Test
	public void loadAndVerifyInvocSensorWithMinDuration() throws ParserException, StorageException {
		String sensorTypeName = "isequence";
		String className = "info.novatec.inspectitsamples.calculator.Calculator";
		String methodName = "actionPerformed";
		String minDuration = "100.0";

		writer.println("sensor " + sensorTypeName + " " + className + " " + methodName + "() " + " minDuration=" + minDuration);
		writer.close();

		fileConfigurationReader.load();

		Map<String, String> settings = new HashMap<String, String>();
		settings.put("minDuration", minDuration);

		verify(configurationStorage, times(1)).addSensor(sensorTypeName, className, methodName, Collections.EMPTY_LIST, false, settings);
		verifyNoMoreInteractions(configurationStorage);
	}
	
	@Test
	public void loadAndVerifyAnnotation() throws ParserException, StorageException {
		String sensorTypeName = "isequence";
		String className = "info.novatec.inspectitsamples.calculator.Calculator";
		String methodName = "actionPerformed";
		String annotationClassName = "javax.ejb.StatelessBean";
		
		writer.println("sensor " + sensorTypeName + " " + className + " " + methodName + "() " + " @" + annotationClassName);
		writer.close();

		fileConfigurationReader.load();
		
		Map<String, String> settings = new HashMap<String, String>();
		settings.put("annotation", annotationClassName);
		
		verify(configurationStorage, times(1)).addSensor(sensorTypeName, className, methodName, Collections.EMPTY_LIST, false, settings);
		verifyNoMoreInteractions(configurationStorage);
	}

	@Test(expectedExceptions = { ParserException.class })
	public void loadInvalidFile() throws ParserException {
		System.setProperty("inspectit.config", "");

		fileConfigurationReader.load();
	}

	@AfterClass(alwaysRun = true)
	public void deleteConfiguration() {
		if (null != file) {
			file.delete();
		}
	}

}
