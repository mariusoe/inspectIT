package info.novatec.inspectit.agent.config.test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import info.novatec.inspectit.agent.config.IPropertyAccessor;
import info.novatec.inspectit.agent.config.PropertyAccessException;
import info.novatec.inspectit.agent.config.impl.PropertyAccessor;
import info.novatec.inspectit.agent.config.impl.PropertyAccessor.PropertyPath;
import info.novatec.inspectit.agent.config.impl.PropertyAccessor.PropertyPathStart;
import info.novatec.inspectit.agent.test.AbstractLogSupport;
import info.novatec.inspectit.communication.data.ParameterContentData;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class PropertyAccessorTest extends AbstractLogSupport {

	private IPropertyAccessor propertyAccessor;

	@Override
	protected Level getLogLevel() {
		return Level.OFF;
	}

	@BeforeClass
	public void initTestClass() {
		propertyAccessor = new PropertyAccessor();
	}

	@Test
	public void analyzePersonObject() throws PropertyAccessException {
		Person person = new Person();
		person.setName("Dirk");

		PropertyPathStart start = new PropertyPathStart();
		start.setName("this");
		start.setClassOfExecutedMethod(true);

		String result = propertyAccessor.getPropertyContent(start, person, null);
		assertEquals(result, "Dirk");
	}

	@Test
	public void analyzePersonName() throws PropertyAccessException {
		Person person = new Person();
		person.setName("Dirk");

		PropertyPathStart start = new PropertyPathStart();
		start.setName("this");
		start.setClassOfExecutedMethod(true);

		PropertyPath path = new PropertyPath();
		path.setName("name");
		start.setPathToContinue(path);

		String result = propertyAccessor.getPropertyContent(start, person, null);
		assertEquals(result, "Dirk");
	}

	@Test(expectedExceptions = { PropertyAccessException.class })
	public void nullStartPath() throws PropertyAccessException {
		propertyAccessor.getPropertyContent(null, null, null);
	}

	@Test(expectedExceptions = { PropertyAccessException.class })
	public void nullNeededClassObject() throws PropertyAccessException {
		PropertyPathStart start = new PropertyPathStart();
		start.setName("this");
		start.setClassOfExecutedMethod(true);

		propertyAccessor.getPropertyContent(start, null, null);
	}

	@Test(expectedExceptions = { PropertyAccessException.class })
	public void nullNeededParameterObject() throws PropertyAccessException {
		PropertyPathStart start = new PropertyPathStart();
		start.setName("name");
		start.setSignaturePosition(0);

		propertyAccessor.getPropertyContent(start, null, null);
	}

	@Test(expectedExceptions = { PropertyAccessException.class })
	public void parameterArrayOutOfRange() throws PropertyAccessException {
		PropertyPathStart start = new PropertyPathStart();
		start.setName("name");
		start.setSignaturePosition(0);

		propertyAccessor.getPropertyContent(start, null, new Object[0]);
	}

	@Test(expectedExceptions = { PropertyAccessException.class })
	public void analyzePersonAccessException() throws PropertyAccessException {
		Person person = new Person();
		person.setName("Dirk");

		PropertyPathStart start = new PropertyPathStart();
		start.setName("this");
		start.setClassOfExecutedMethod(true);

		PropertyPath path = new PropertyPath();
		path.setName("surname");
		start.setPathToContinue(path);

		// name != surname -> exception
		propertyAccessor.getPropertyContent(start, person, null);
	}

	@Test
	public void analyzePersonParameter() throws PropertyAccessException {
		// create initial object relation
		Person peter = new Person("Peter");
		Person juergen = new Person("Jürgen");
		peter.setChild(juergen);
		Person hans = new Person("Hans");
		juergen.setChild(hans);
		Person thomas = new Person("Thomas");
		hans.setChild(thomas);
		Person michael = new Person("Michael");
		thomas.setChild(michael);

		// create the test path
		PropertyPathStart start = new PropertyPathStart();
		start.setName("name");
		start.setSignaturePosition(1);

		PropertyPath pathOne = new PropertyPath("child");
		start.setPathToContinue(pathOne);

		PropertyPath pathTwo = new PropertyPath("child");
		pathOne.setPathToContinue(pathTwo);

		PropertyPath pathThree = new PropertyPath("child");
		pathTwo.setPathToContinue(pathThree);

		PropertyPath pathFour = new PropertyPath("child");
		pathThree.setPathToContinue(pathFour);

		// set the parameter array
		Object[] parameters = { null, peter };

		String result = propertyAccessor.getPropertyContent(start, new Object(), parameters);
		assertEquals(result, "Michael");
	}

	@SuppressWarnings("unchecked")
	@Test
	public void removePropertyAccessorFromList() {
		// create initial object relation
		Person peter = new Person("Peter");
		Person juergen = new Person("Hans");
		peter.setChild(juergen);

		List<PropertyPathStart> propertyAccessorList = new ArrayList<PropertyPathStart>();

		// valid
		PropertyPathStart start = new PropertyPathStart();
		start.setName("name");
		start.setSignaturePosition(0);
		PropertyPath pathOne = new PropertyPath("child");
		start.setPathToContinue(pathOne);
		propertyAccessorList.add(start);

		// not valid
		start = new PropertyPathStart();
		start.setName("this");
		start.setClassOfExecutedMethod(true);
		pathOne = new PropertyPath("notValid");
		start.setPathToContinue(pathOne);
		propertyAccessorList.add(start);

		// not valid as the second parameter will be null
		start = new PropertyPathStart();
		start.setName("name");
		start.setSignaturePosition(1);
		pathOne = new PropertyPath("child");
		start.setPathToContinue(pathOne);
		propertyAccessorList.add(start);

		assertEquals(propertyAccessorList.size(), 3);

		List<ParameterContentData> parameterContentList = propertyAccessor.getParameterContentData(propertyAccessorList, peter, new Object[] { peter });

		// size should be reduced to one
		assertEquals(propertyAccessorList.size(), 1);
		// so is the size of the parameter content
		assertNotNull(parameterContentList);
		assertEquals(parameterContentList.size(), 1);
		// changed due to xstream, the ' at the beginning will be always removed
		// if displayed to the end-user.
		assertEquals(parameterContentList.get(0).getContent(), "'Hans'");
		assertEquals(parameterContentList.get(0).getSignaturePosition(), 0);
		assertEquals(parameterContentList.get(0).getName(), "name");
	}

	@Test
	public void invokeArrayLengthMethod() throws PropertyAccessException {
		// create initial object relation
		Person peter = new Person("Peter");
		String[] foreNames = new String[] { "Klaus", "Uwe" };
		peter.setForeNames(foreNames);

		PropertyPathStart start = new PropertyPathStart();
		start.setName("this");
		start.setClassOfExecutedMethod(true);

		PropertyPath path = new PropertyPath();
		path.setName("foreNames");
		start.setPathToContinue(path);

		PropertyPath path2 = new PropertyPath();
		path2.setName("length()");
		path.setPathToContinue(path2);

		String result = propertyAccessor.getPropertyContent(start, peter, null);
		assertEquals(new Integer(result).intValue(), 2);
	}

	@Test(expectedExceptions = { PropertyAccessException.class })
	public void invokeArrayLengthMethodOnNonArray() throws PropertyAccessException {
		// create initial object relation
		Person peter = new Person("Peter");
		String[] foreNames = new String[] { "Klaus", "Uwe" };
		peter.setForeNames(foreNames);

		PropertyPathStart start = new PropertyPathStart();
		start.setName("this");
		start.setClassOfExecutedMethod(true);

		PropertyPath path = new PropertyPath();
		path.setName("name");
		start.setPathToContinue(path);

		PropertyPath path2 = new PropertyPath();
		path2.setName("length()");
		path.setPathToContinue(path2);

		// must result in an Exception as name is not an array
		propertyAccessor.getPropertyContent(start, peter, null);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void invokeListSizeMethod() throws PropertyAccessException {
		// create initial object relation
		Person peter = new Person("Peter");
		List foreNames = new ArrayList<String>();
		foreNames.add("blub");
		foreNames.add("blub2");
		foreNames.add("blub3");
		peter.setForeNamesAsList(foreNames);

		PropertyPathStart start = new PropertyPathStart();
		start.setName("this");
		start.setClassOfExecutedMethod(true);

		PropertyPath path = new PropertyPath();
		path.setName("foreNamesAsList");
		start.setPathToContinue(path);

		PropertyPath path2 = new PropertyPath();
		path2.setName("size()");
		path.setPathToContinue(path2);

		String result = propertyAccessor.getPropertyContent(start, peter, null);
		assertEquals(new Integer(result).intValue(), 3);
	}

	private static class Person {

		private String name;

		private Person child;

		private String[] foreNames;

		private List<String> foreNamesAsList;

		public Person() {
		}

		public Person(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public Person getChild() {
			return child;
		}

		public void setChild(Person child) {
			this.child = child;
		}

		public String[] getForeNames() {
			return foreNames;
		}

		public void setForeNames(String[] foreNames) {
			this.foreNames = foreNames;
		}

		public List<String> getForeNamesAsList() {
			return foreNamesAsList;
		}

		public void setForeNamesAsList(List<String> foreNamesAsList) {
			this.foreNamesAsList = foreNamesAsList;
		}

		@Override
		public String toString() {
			return name;
		}

	}

}
