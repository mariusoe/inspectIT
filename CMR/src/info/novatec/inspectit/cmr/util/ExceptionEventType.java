package info.novatec.inspectit.cmr.util;

import info.novatec.inspectit.communication.ExceptionEventEnum;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import org.hibernate.HibernateException;
import org.hibernate.usertype.UserType;

/**
 * User type for Hibernate to map the {@link ExceptionEventEnum} class into the database.
 * 
 * @author Patrice Bouillet
 * 
 */
public class ExceptionEventType implements UserType {

	/**
	 * The sql types.
	 */
	private static final int[] TYPES = new int[] { Types.NUMERIC };

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int[] sqlTypes() {
		return TYPES;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object deepCopy(Object value) throws HibernateException {
		return value;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isMutable() {
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object nullSafeGet(ResultSet resultSet, String[] names, Object owner) throws HibernateException, SQLException {
		ExceptionEventEnum result = null;
		int dbValue = resultSet.getInt(names[0]);
		if (dbValue != -1) {
			result = ExceptionEventEnum.fromInt(dbValue);
		}
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void nullSafeSet(PreparedStatement statement, Object value, int index) throws HibernateException, SQLException {
		if (null == value) {
			statement.setInt(index, -1);
		} else {
			ExceptionEventEnum event = (ExceptionEventEnum) value;
			int dbValue = event.getValue();
			statement.setInt(index, dbValue);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object replace(Object arg0, Object arg1, Object arg2) throws HibernateException {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Class<ExceptionEventEnum> returnedClass() {
		return ExceptionEventEnum.class;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object assemble(Serializable arg0, Object arg1) throws HibernateException {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Serializable disassemble(Object arg0) throws HibernateException {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object x, Object y) throws HibernateException {
		if (x == y) {
			return true;
		} else if (x == null || y == null) {
			return false;
		} else {
			return x.equals(y);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode(Object object) throws HibernateException {
		return object.hashCode();
	}

}
