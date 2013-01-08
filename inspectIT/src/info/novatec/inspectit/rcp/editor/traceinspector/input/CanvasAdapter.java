package info.novatec.inspectit.rcp.editor.traceinspector.input;

public interface CanvasAdapter {

	Rectangle getArea();

	void drawSequence(InvocationBlock invocationBlock);

	void drawSequenceHeated(InvocationBlock invocationBlock);

	void updateDetail(InvocationBlock invocationBlock);
}
