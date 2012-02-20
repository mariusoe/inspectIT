package info.novatec.inspectit.rcp.editor.inputdefinition.extra;

import com.google.common.base.Objects;

/**
 * Extended input definition data used for the Combined metrics.
 * 
 * @author Ivan Senic
 * 
 */
public class CombinedMetricsInputDefinitionExtra implements IInputDefinitionExtra {

	/**
	 * Workflow.
	 */
	private String workflow;

	/**
	 * Activity.
	 */
	private String activity;

	/**
	 * Gets {@link #workflow}.
	 * 
	 * @return {@link #workflow}
	 */
	public String getWorkflow() {
		return workflow;
	}

	/**
	 * Sets {@link #workflow}.
	 * 
	 * @param workflow
	 *            New value for {@link #workflow}
	 */
	public void setWorkflow(String workflow) {
		this.workflow = workflow;
	}

	/**
	 * Gets {@link #activity}.
	 * 
	 * @return {@link #activity}
	 */
	public String getActivity() {
		return activity;
	}

	/**
	 * Sets {@link #activity}.
	 * 
	 * @param activity
	 *            New value for {@link #activity}
	 */
	public void setActivity(String activity) {
		this.activity = activity;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		return Objects.hashCode(workflow, activity);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object object) {
		if (this == object) {
			return true;
		}
		if (object == null) {
			return false;
		}
		if (getClass() != object.getClass()) {
			return false;
		}
		CombinedMetricsInputDefinitionExtra that = (CombinedMetricsInputDefinitionExtra) object;
		return Objects.equal(this.workflow, that.workflow)
				&& Objects.equal(this.activity, that.activity);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return Objects.toStringHelper(this)
				.add("workflow", workflow)
				.add("activity", activity)
				.toString();
	}

}
