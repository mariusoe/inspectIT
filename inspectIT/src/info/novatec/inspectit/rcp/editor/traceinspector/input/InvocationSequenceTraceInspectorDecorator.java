package info.novatec.inspectit.rcp.editor.traceinspector.input;

import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.communication.data.InvocationSequenceData;
import info.novatec.inspectit.communication.data.ParameterContentData;
import info.novatec.inspectit.communication.data.SqlStatementData;
import info.novatec.inspectit.communication.data.TimerData;

import java.sql.Timestamp;
import java.util.List;
import java.util.Set;

/**
 * This class is used to decorate an {@link InvocationSequenceData} object with information needed
 * in the drawing and managing of the Transaction Inspector. The decorator provides graphical
 * information contained in the invocationBlock member. The decorator is compatible to the
 * {@link InvocationSequenceData} and can replace the original {@link InvocationSequenceData} in any
 * compatible link. This implementation doesn't follow the original pattern by GOF, because the
 * decorator is derived from the subject. This is because the subject doesn't implement an interface
 * or an abstract class suitable for implementing a decorator pattern the normal way. All methods of
 * {@link InvocationSequenceData} must be overridden and delegated. In opposite to the original
 * pattern you will not get support from the compiler, if something changes in the subject
 * implementation (i.e. new Methods, signature change, etc.).
 * 
 * @author Michael Kowatsch
 * 
 */
public class InvocationSequenceTraceInspectorDecorator extends InvocationSequenceData {
	/**
	 * The generated serial version UID.
	 */
	private static final long serialVersionUID = -6297750313128949804L;

	/**
	 * The link to the {@link InvocationSequenceData}, which is the subject of this decorator
	 */
	private InvocationSequenceData subject = null;

	/**
	 * The link to the {@link InvocationBlock}, which is decoration to the subject
	 */
	private InvocationBlock invocationBlock = null;

	public InvocationSequenceTraceInspectorDecorator(InvocationSequenceData subject, InvocationBlock invocationBlock) {
		super();
		this.subject = subject;
		this.invocationBlock = invocationBlock;
	}

	public InvocationBlock getInvocationBlock() {
		return invocationBlock;
	}

	public void setInvocationBlock(InvocationBlock invocationBlock) {
		this.invocationBlock = invocationBlock;
	}

	public InvocationSequenceData getSubject() {
		return subject;
	}

	public void setSubject(InvocationSequenceData subject) {
		this.subject = subject;
	}

	public void addParameterContentData(ParameterContentData parameterContent) {
		subject.addParameterContentData(parameterContent);
	}

	public boolean equals(Object obj) {
		return subject.equals(obj);
	}

	public DefaultData finalizeData() {
		return subject.finalizeData();
	}

	public long getChildCount() {
		return subject.getChildCount();
	}

	public double getDuration() {
		return subject.getDuration();
	}

	public double getEnd() {
		return subject.getEnd();
	}

	public long getId() {
		return subject.getId();
	}

	public long getMethodIdent() {
		return subject.getMethodIdent();
	}

	@SuppressWarnings("rawtypes")
	public List getNestedSequences() {
		return subject.getNestedSequences();
	}

	@SuppressWarnings("rawtypes")
	public Set getParameterContentData() {
		return subject.getParameterContentData();
	}

	public InvocationSequenceData getParentSequence() {
		return subject.getParentSequence();
	}

	public long getPlatformIdent() {
		return subject.getPlatformIdent();
	}

	public long getPosition() {
		return subject.getPosition();
	}

	public long getSensorTypeIdent() {
		return subject.getSensorTypeIdent();
	}

	public SqlStatementData getSqlStatementData() {
		return subject.getSqlStatementData();
	}

	public double getStart() {
		return subject.getStart();
	}

	public TimerData getTimerData() {
		return subject.getTimerData();
	}

	public Timestamp getTimeStamp() {
		return subject.getTimeStamp();
	}

	public int hashCode() {
		return subject.hashCode();
	}

	public void setChildCount(long childCount) {
		subject.setChildCount(childCount);
	}

	public void setDuration(double duration) {
		subject.setDuration(duration);
	}

	public void setEnd(double end) {
		subject.setEnd(end);
	}

	public void setId(long id) {
		subject.setId(id);
	}

	public void setMethodIdent(long methodIdent) {
		subject.setMethodIdent(methodIdent);
	}

	@SuppressWarnings("rawtypes")
	public void setNestedSequences(List nestedSequences) {
		subject.setNestedSequences(nestedSequences);
	}

	@SuppressWarnings("rawtypes")
	public void setParameterContentData(Set parameterContentData) {
		subject.setParameterContentData(parameterContentData);
	}

	public void setParentSequence(InvocationSequenceData parentSequence) {
		subject.setParentSequence(parentSequence);
	}

	public void setPlatformIdent(long platformIdent) {
		subject.setPlatformIdent(platformIdent);
	}

	public void setPosition(long position) {
		subject.setPosition(position);
	}

	public void setSensorTypeIdent(long sensorTypeIdent) {
		subject.setSensorTypeIdent(sensorTypeIdent);
	}

	public void setSqlStatementData(SqlStatementData sqlStatementData) {
		subject.setSqlStatementData(sqlStatementData);
	}

	public void setStart(double start) {
		subject.setStart(start);
	}

	public void setTimerData(TimerData timerData) {
		subject.setTimerData(timerData);
	}

	public void setTimeStamp(Timestamp timeStamp) {
		subject.setTimeStamp(timeStamp);
	}

	public String toString() {
		return subject.toString();
	}

}
