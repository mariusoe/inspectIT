package info.novatec.inspectit.rcp.model;

import info.novatec.inspectit.cmr.model.MethodIdent;
import info.novatec.inspectit.cmr.model.MethodSensorTypeIdent;
import info.novatec.inspectit.rcp.util.ObjectUtils;


/**
 * Filtered package composite delegates the children creation to the
 * {@link FilteredDeferredClassComposite}.
 * 
 * @author Ivan Senic
 * 
 */
public class FilteredDefferedPackageComposite extends DeferredPackageComposite {

	/**
	 * Sensor to show.
	 */
	private SensorTypeEnum sensorTypeEnumToShow;

	/**
	 * @param sensorTypeEnum
	 *            Set the sensor type to show.
	 */
	public FilteredDefferedPackageComposite(SensorTypeEnum sensorTypeEnum) {
		this.sensorTypeEnumToShow = sensorTypeEnum;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected DeferredClassComposite getNewChild() {
		return new FilteredDeferredClassComposite(sensorTypeEnumToShow);
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
