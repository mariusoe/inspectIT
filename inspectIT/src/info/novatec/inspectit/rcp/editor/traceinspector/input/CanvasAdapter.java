package info.novatec.inspectit.rcp.editor.traceinspector.input;

public interface CanvasAdapter {

	public Rectangle getArea();

	public void drawSequence(InvocationBlock invocationBlock);

	public void drawSequenceHeated(InvocationBlock invocationBlock);

	public void updateDetail(InvocationBlock invocationBlock);
}
