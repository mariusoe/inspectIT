package info.novatec.inspectit.rcp.editor.traceinspector.input;

import info.novatec.inspectit.cmr.model.MethodIdent;
import info.novatec.inspectit.communication.data.InvocationSequenceData;

public interface CanvasAdapter {

   public Rectangle getArea();
   
   public void drawSequence(InvocationBlock invocationBlock);
   public void drawSequenceHeated(InvocationBlock invocationBlock);
   
   public void updateDetail(InvocationBlock invocationBlock);    
}
