package info.novatec.novaspy.rcp.editor.traceinspector.input;

import info.novatec.novaspy.cmr.model.MethodIdent;
import info.novatec.novaspy.communication.data.InvocationSequenceData;

public interface CanvasAdapter {

   public Rectangle getArea();
   
   public void drawSequence(InvocationBlock invocationBlock);
   public void drawSequenceHeated(InvocationBlock invocationBlock);
   
   public void updateDetail(InvocationBlock invocationBlock);    
}
