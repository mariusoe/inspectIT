package rocks.inspectit.shared.cs.ci.business.expression.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;

import org.mockito.Mock;
import org.testng.annotations.Test;

import rocks.inspectit.shared.all.communication.data.InvocationSequenceData;
import rocks.inspectit.shared.all.testbase.TestBase;
import rocks.inspectit.shared.cs.ci.business.expression.AbstractExpression;
import rocks.inspectit.shared.cs.cmr.service.cache.CachedDataService;

/**
 * @author Alexander Wert
 *
 */
public class OrExpressionTest extends TestBase {
	/**
	 * Test
	 * {@link OrExpression#evaluate(InvocationSequenceData, rocks.inspectit.shared.all.cmr.service.ICachedDataService)}
	 * method.
	 */
	public static class Evaluate extends OrExpressionTest {
		@Mock
		AbstractExpression expr1;

		@Mock
		AbstractExpression expr2;

		@Mock
		AbstractExpression expr3;

		@Mock
		InvocationSequenceData invocation;

		@Mock
		CachedDataService cachedDataService;

		@Test
		public void noOperand() {
			boolean evaluationResult = new OrExpression().evaluate(invocation, cachedDataService);

			assertThat(evaluationResult, is(false));
		}

		@Test
		public void oneOperandTrue() {
			when(expr1.evaluate(invocation, cachedDataService)).thenReturn(true);

			OrExpression orExpression = new OrExpression(expr1);
			boolean evaluationResult = orExpression.evaluate(invocation, cachedDataService);

			assertThat(evaluationResult, is(true));
		}

		@Test
		public void oneOperandFalse() {
			when(expr1.evaluate(invocation, cachedDataService)).thenReturn(false);

			OrExpression orExpression = new OrExpression(expr1);
			boolean evaluationResult = orExpression.evaluate(invocation, cachedDataService);

			assertThat(evaluationResult, is(false));
		}

		@Test
		public void twoOperandsTrue() {
			when(expr1.evaluate(invocation, cachedDataService)).thenReturn(false);
			when(expr2.evaluate(invocation, cachedDataService)).thenReturn(true);

			OrExpression orExpression = new OrExpression(expr1, expr2);
			boolean evaluationResult = orExpression.evaluate(invocation, cachedDataService);

			assertThat(evaluationResult, is(true));
		}

		@Test
		public void twoOperandsFalse() {
			when(expr1.evaluate(invocation, cachedDataService)).thenReturn(false);
			when(expr2.evaluate(invocation, cachedDataService)).thenReturn(false);

			OrExpression orExpression = new OrExpression(expr1, expr2);
			boolean evaluationResult = orExpression.evaluate(invocation, cachedDataService);

			assertThat(evaluationResult, is(false));
		}

		@Test
		public void threeOperandsTrue() {
			when(expr1.evaluate(invocation, cachedDataService)).thenReturn(false);
			when(expr2.evaluate(invocation, cachedDataService)).thenReturn(false);
			when(expr3.evaluate(invocation, cachedDataService)).thenReturn(true);

			OrExpression orExpression = new OrExpression(expr1, expr2, expr3);
			boolean evaluationResult = orExpression.evaluate(invocation, cachedDataService);

			assertThat(evaluationResult, is(true));
		}

		@Test
		public void threeOperandsFalse() {
			when(expr1.evaluate(invocation, cachedDataService)).thenReturn(false);
			when(expr2.evaluate(invocation, cachedDataService)).thenReturn(false);
			when(expr3.evaluate(invocation, cachedDataService)).thenReturn(false);

			OrExpression orExpression = new OrExpression(expr1, expr2, expr3);
			boolean evaluationResult = orExpression.evaluate(invocation, cachedDataService);

			assertThat(evaluationResult, is(false));
		}
	}
}