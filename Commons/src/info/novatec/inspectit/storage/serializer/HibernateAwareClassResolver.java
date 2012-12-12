package info.novatec.inspectit.storage.serializer;

import info.novatec.inspectit.util.IHibernateUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import com.esotericsoftware.kryo.Registration;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.util.DefaultClassResolver;

/**
 * Class resolver that writes java collections and maps instead of the hibernate ones.
 * <p>
 * The current mapping is:
 * <ul>
 * <li>Hibernate PersistentSet -> HashSet</li>
 * <li>Hibernate PersistentMap -> HashMap</li>
 * <li>Hibernate PersistentList -> ArrayList</li>
 * </ul>
 * 
 * @author Ivan Senic
 * 
 */
public class HibernateAwareClassResolver extends DefaultClassResolver {

	/**
	 * {@link IHibernateUtil} to use.
	 */
	private IHibernateUtil hibernateUtil;

	/**
	 * Default constructor.
	 * 
	 * @param hibernateUtil
	 *            {@link IHibernateUtil} to use.
	 */
	public HibernateAwareClassResolver(IHibernateUtil hibernateUtil) {
		if (null == hibernateUtil) {
			throw new IllegalArgumentException("Hibernate util is needed with creation of Hibernate aware class resolver");
		}
		this.hibernateUtil = hibernateUtil;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings({ "rawtypes" })
	public Registration writeClass(Output output, Class type) {
		Class<?> writeType = type;
		if (null != type) {
			if (hibernateUtil.isPersistentList(type)) {
				writeType = ArrayList.class; // NOPMD
			} else if (hibernateUtil.isPersistentSet(type)) {
				writeType = HashSet.class; // NOPMD
			} else if (hibernateUtil.isPersistentMap(type)) {
				writeType = HashMap.class; // NOPMD
			}
		}
		return super.writeClass(output, writeType);
	}
}
