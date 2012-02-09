package info.novatec.inspectit.rcp.model;

import info.novatec.inspectit.cmr.model.MethodIdent;
import info.novatec.inspectit.cmr.model.MethodSensorTypeIdent;
import info.novatec.inspectit.rcp.util.ObjectUtils;


/**
 * Filtered package composite delegates the children creation to the
 * {@link FilteredDefferedPackageComposite}.
 * 
 * @author Ivan Senic
 * 
 */
public class FilteredDeferredBrowserComposite extends DeferredBrowserComposite {

	/**
	 * Sensor to show.
	 */
	private SensorTypeEnum sensorTypeEnumToShow;
	
	/**
	 * @param sensorTypeEnum Set the sensor type to show.
	 */
	public FilteredDeferredBrowserComposite(SensorTypeEnum sensorTypeEnum) {
		this.sensorTypeEnumToShow = sensorTypeEnum;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected DeferredPackageComposite getNewChild() {
		return new FilteredDefferedPackageComposite(sensorTypeEnumToShow);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected boolean select(MethodIdent methodIdent) {
		for (MethodSensorTypeIdent methodSensorType : methodIdent.getMethodSensorTypeIdents()) {
			SensorTypeEnum sensorTypeEnum = SensorTypeEnum.get(methodSensorType.getFullyQualifiedClassName());
			if (ObjectUtils.equals(sensorTypeEnum, sensorTypeEnumToShow)) {
				return true;
			}
		}
		return false;
	}
}
