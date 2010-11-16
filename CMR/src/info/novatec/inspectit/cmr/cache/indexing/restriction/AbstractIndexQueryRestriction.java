package info.novatec.inspectit.cmr.cache.indexing.restriction;

/**
 * Abstract class for all index query restriction classes.
 * 
 * @author Ivan Senic
 * 
 */
public abstract class AbstractIndexQueryRestriction implements IIndexQueryRestriction {

	/**
	 * Field name.
	 */
	private String fieldName;

	/**
	 * Default constructor.
	 * 
	 * @param fieldName
	 *            Name of the field that is restriction bounded to.
	 */
	public AbstractIndexQueryRestriction(String fieldName) {
		super();
		this.fieldName = fieldName;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getQualifiedMethodName() {
		if (null == fieldName) {
			return "";
		}
		String methodName = "get" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
		return methodName;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getFieldName() {
		return fieldName;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}
}
