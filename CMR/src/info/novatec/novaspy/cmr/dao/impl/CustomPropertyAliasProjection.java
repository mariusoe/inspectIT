package info.novatec.novaspy.cmr.dao.impl;

import org.hibernate.Criteria;
import org.hibernate.criterion.AliasedProjection;
import org.hibernate.criterion.CriteriaQuery;
import org.hibernate.criterion.Projections;

/**
 * Customized projection to map a property with an alias that is intended to
 * work-around HHH-817. This works in a basic situation that I've tried, but I
 * can't guarantee it works in complex queries.
 * 
 * This causes the alias to be bypassed in the where clause so the original
 * column name is used. Some DB's don't support aliases in the where clause.
 * 
 * http://opensource.atlassian.com/projects/hibernate/browse/HHH-817
 * 
 * @author Chris Federowicz
 */
public class CustomPropertyAliasProjection extends AliasedProjection {
	/** Default serialization ID. */
	private static final long serialVersionUID = 1L;
	/** Stores the property name being aliased. */
	private String propertyName;
	/** Stores the column name for the property in the PropertyProjection. */
	private String propertyColumn;
	/** The alias value of the property. */
	private String alias;

	/**
	 * @param propertyName
	 *            the property name
	 * @param alias
	 *            alias of the property
	 */
	public CustomPropertyAliasProjection(String propertyName, String alias) {
		super(Projections.property(propertyName), alias);
		this.propertyName = propertyName;
		this.alias = alias;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toSqlString(Criteria criteria, int position, CriteriaQuery criteriaQuery) {
		// Store the property's real column for use in getColumnAliases
		propertyColumn = criteriaQuery.getColumn(criteria, propertyName);

		return super.toSqlString(criteria, position, criteriaQuery);
	}

	/**
	 * Override to use the property's column if we have it (which we should).
	 * Hack alert: This assumes that toSqlString gets called beforehand, which
	 * seems to be the case.
	 * 
	 * {@inheritDoc}
	 */
	@Override
	public String[] getColumnAliases(String alias, int loc) {
		if (this.alias.equals(alias) && propertyColumn != null) {
			return new String[] { propertyColumn };
		} else {
			return super.getColumnAliases(alias, loc);
		}
	}

}