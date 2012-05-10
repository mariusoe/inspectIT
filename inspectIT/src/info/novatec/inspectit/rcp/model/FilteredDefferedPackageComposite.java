package info.novatec.inspectit.rcp.model;

import info.novatec.inspectit.cmr.model.MethodIdent;
import info.novatec.inspectit.cmr.model.MethodSensorTypeIdent;
import info.novatec.inspectit.rcp.util.ObjectUtils;

import com.google.common.base.Objects;

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

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		return Objects.hashCode(super.hashCode(), sensorTypeEnumToShow);
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
		if (!super.equals(object)) {
			return false;
		}
		FilteredDefferedPackageComposite that = (FilteredDefferedPackageComposite) object;
		return Objects.equal(this.sensorTypeEnumToShow, that.sensorTypeEnumToShow);
	}

}
