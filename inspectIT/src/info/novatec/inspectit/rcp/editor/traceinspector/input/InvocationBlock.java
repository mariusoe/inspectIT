package info.novatec.inspectit.rcp.editor.traceinspector.input;

import info.novatec.inspectit.cmr.model.MethodIdent;
import info.novatec.inspectit.communication.data.InvocationSequenceData;

public class InvocationBlock {

	private Rectangle area = null;
	private InvocationSequenceData invocationSequence = null;
	private MethodIdent methodIdent = null;
	private boolean isHighlighted = false;
	private double amount = 0.0;

	public InvocationBlock() {
	}

	public InvocationBlock(Rectangle area, InvocationSequenceData invocationSequenceData, MethodIdent methodIdent) {
		this.area = area;
		this.invocationSequence = invocationSequenceData;
		this.methodIdent = methodIdent;
	}

	public Rectangle getArea() {
		return area;
	}

	public void setArea(Rectangle area) {
		this.area = area;
	}

	public InvocationSequenceData getInvocationSequence() {
		return invocationSequence;
	}

	public void setInvocationSequence(InvocationSequenceData invocationSequence) {
		this.invocationSequence = invocationSequence;
	}

	public MethodIdent getMethodIdent() {
		return methodIdent;
	}

	public void setMethodIdent(MethodIdent methodIdent) {
		this.methodIdent = methodIdent;
	}

	public boolean isHighlighted() {
		return isHighlighted;
	}

	public void setHighlighted(boolean isHighlighted) {
		this.isHighlighted = isHighlighted;
	}

	public double getAmount() {
		return amount;
	}

	public void setAmount(double amount) {
		this.amount = amount;
	}

}
