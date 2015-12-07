package rocks.inspectit.shared.cs.ci.business.expression.impl;

import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;

import rocks.inspectit.shared.all.cmr.service.ICachedDataService;
import rocks.inspectit.shared.all.communication.data.InvocationSequenceData;
import rocks.inspectit.shared.cs.ci.business.expression.AbstractExpression;
import rocks.inspectit.shared.cs.ci.business.expression.IContainerExpression;

/**
 * NOT expression definition.
 *
 * @author Alexander Wert
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "not")
public class NotExpression extends AbstractExpression implements IContainerExpression {
	/**
	 * Operand.
	 */
	@XmlElementRef(name = "operand", type = AbstractExpression.class)
	private AbstractExpression operand;

	/**
	 * Default Constructor.
	 */
	public NotExpression() {
	}

	/**
	 * Constructor.
	 *
	 * @param operand
	 *            expression to negate
	 */
	public NotExpression(AbstractExpression operand) {
		this.operand = operand;
	}

	/**
	 * Gets {@link #operand}.
	 *
	 * @return {@link #operand}
	 */
	public AbstractExpression getOperand() {
		return operand;
	}

	/**
	 * Sets {@link #operand}.
	 *
	 * @param operand
	 *            New value for {@link #operand}
	 */
	public void setOperand(AbstractExpression operand) {
		this.operand = operand;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getNumberOfChildExpressions() {
		return null == operand ? 0 : 1;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean evaluate(InvocationSequenceData invocSequence, ICachedDataService cachedDataService) {
		return !getOperand().evaluate(invocSequence, cachedDataService);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addOperand(AbstractExpression operand) {
		setOperand(operand);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean canAddOperand() {
		return getNumberOfChildExpressions() < 1;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<AbstractExpression> getOperands() {
		if (null != getOperand()) {
			return Collections.singletonList(getOperand());
		} else {
			return Collections.emptyList();
		}

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void removeOperand(AbstractExpression operand) {
		setOperand(null);
	}

}