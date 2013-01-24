package info.novatec.inspectit.communication.comparator;

import info.novatec.inspectit.cmr.model.MethodIdent;
import info.novatec.inspectit.cmr.service.cache.CachedDataService;
import info.novatec.inspectit.communication.MethodSensorData;
import info.novatec.inspectit.util.ObjectUtils;

import java.util.List;

/**
 * Comparators for {@link MethodSensorData}.
 * 
 * @author Ivan Senic
 * 
 */
public enum MethodSensorDataComparatorEnum implements IDataComparator<MethodSensorData> {

	/**
	 * Sort by package name.
	 */
	PACKAGE,

	/**
	 * Sort by class name.
	 */
	CLASS,

	/**
	 * Sort by method name.
	 */
	METHOD;

	/**
	 * {@inheritDoc}
	 */
	public int compare(MethodSensorData o1, MethodSensorData o2, CachedDataService cachedDataService) {
		if (null == cachedDataService) {
			return 0;
		}
		MethodIdent methodIdent1 = cachedDataService.getMethodIdentForId(o1.getMethodIdent());
		MethodIdent methodIdent2 = cachedDataService.getMethodIdentForId(o2.getMethodIdent());
		if (null != methodIdent1 && null != methodIdent2) {
			switch (this) {
			case PACKAGE:
				return ObjectUtils.compare(methodIdent1.getPackageName(), methodIdent2.getPackageName());
			case CLASS:
				return ObjectUtils.compare(methodIdent1.getClassName(), methodIdent2.getClassName());
			case METHOD:
				int result = ObjectUtils.compare(methodIdent1.getMethodName(), methodIdent2.getMethodName());
				if (0 != result) {
					return result;
				} else {
					List<String> parameterList1 = methodIdent1.getParameters();
					List<String> parameterList2 = methodIdent2.getParameters();
					return ObjectUtils.compare(parameterList1, parameterList2);
				}
			default:
				return 0;
			}
		} else {
			return 0;
		}
	}

}
