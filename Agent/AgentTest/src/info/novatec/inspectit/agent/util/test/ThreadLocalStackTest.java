package info.novatec.novaspy.agent.util.test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertSame;
import info.novatec.novaspy.util.ThreadLocalStack;

import java.util.LinkedList;
import java.util.NoSuchElementException;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class ThreadLocalStackTest {

	private ThreadLocalStack threadLocalStack;

	@BeforeMethod(firstTimeOnly = true)
	public void initTestClass() {
		threadLocalStack = new ThreadLocalStack();
	}

	@Test
	public void emptyStack() {
		Object object = threadLocalStack.get();

		assertNotNull(object);
		assertEquals(object.getClass(), LinkedList.class);
	}

	@Test(dependsOnMethods = "emptyStack")
	public void oneValue() {
		Object object = mock(Object.class);
		threadLocalStack.push(object);

		Object returnValue = threadLocalStack.pop();

		assertNotNull(returnValue);
		assertSame(returnValue, object);
		verifyZeroInteractions(object);
	}

	@Test(dependsOnMethods = "emptyStack", expectedExceptions = { NoSuchElementException.class })
	public void noSuchElement() {
		threadLocalStack.pop();
	}

	@Test(dependsOnMethods = "emptyStack", invocationCount = 10, threadPoolSize = 10)
	public void stackTest() {
		Object objectOne = mock(Object.class);
		Object objectTwo = mock(Object.class);
		Object objectThree = mock(Object.class);

		threadLocalStack.push(objectOne);
		threadLocalStack.push(objectTwo);
		threadLocalStack.push(objectThree);

		Object returnValueOne = threadLocalStack.pop();
		Object returnValueTwo = threadLocalStack.pop();
		Object returnValueThree = threadLocalStack.pop();

		assertSame(returnValueOne, objectThree);
		assertSame(returnValueTwo, objectTwo);
		assertSame(returnValueThree, objectOne);

		verifyZeroInteractions(objectOne);
		verifyZeroInteractions(objectTwo);
		verifyZeroInteractions(objectThree);
	}

	@Test(dependsOnMethods = "emptyStack")
	public void getAndRemoveFirst() {
		Object objectOne = mock(Object.class);
		Object objectTwo = mock(Object.class);
		Object objectThree = mock(Object.class);

		threadLocalStack.push(objectOne);
		threadLocalStack.push(objectTwo);
		threadLocalStack.push(objectThree);

		assertSame(threadLocalStack.getAndRemoveFirst(), objectOne);
		assertSame(threadLocalStack.getAndRemoveFirst(), objectTwo);
		assertSame(threadLocalStack.getAndRemoveFirst(), objectThree);

		verifyZeroInteractions(objectOne);
		verifyZeroInteractions(objectTwo);
		verifyZeroInteractions(objectThree);
	}

}
