package info.novatec.inspectit.rcp.editor;

import info.novatec.inspectit.rcp.model.SensorTypeEnum;
import info.novatec.inspectit.rcp.repository.RepositoryDefinition;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.forms.widgets.Form;

/**
 * This class is used as the input definition for all editors in the
 * application. Nearly all parameters are optional in here. The actual
 * processing is done in the respective editor which accesses these fields.
 * 
 * @author Patrice Bouillet
 * 
 */
public class InputDefinition {

	/**
	 * This class holds the definition for the IDs used to identify the correct
	 * data objects on the CMR.
	 * 
	 * @author Patrice Bouillet
	 * 
	 */
	public static final class IdDefinition {

		/**
		 * If an ID is not in use ({@link #platformId}, {@link #sensorTypeId},
		 * {@link #methodId}) it is set to this value to indicate this.
		 */
		public static final long ID_NOT_USED = -1;

		/**
		 * The ID of the platform for the view. Default is {@link ID_NOT_USED}.
		 */
		private long platformId = ID_NOT_USED;

		/**
		 * The ID of the sensor type for the view. Default is
		 * {@link ID_NOT_USED}.
		 */
		private long sensorTypeId = ID_NOT_USED;

		/**
		 * The ID of the method for the view.Default is {@link ID_NOT_USED}.
		 */
		private long methodId = ID_NOT_USED;

		/**
		 * @return the platformId
		 */
		public long getPlatformId() {
			return platformId;
		}

		/**
		 * @param platformId
		 *            the platformId to set
		 */
		public void setPlatformId(long platformId) {
			this.platformId = platformId;
		}

		/**
		 * @return the sensorTypeId
		 */
		public long getSensorTypeId() {
			return sensorTypeId;
		}

		/**
		 * @param sensorTypeId
		 *            the sensorTypeId to set
		 */
		public void setSensorTypeId(long sensorTypeId) {
			this.sensorTypeId = sensorTypeId;
		}

		/**
		 * @return the methodId
		 */
		public long getMethodId() {
			return methodId;
		}

		/**
		 * @param methodId
		 *            the methodId to set
		 */
		public void setMethodId(long methodId) {
			this.methodId = methodId;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + (int) (methodId ^ (methodId >>> 32));
			result = prime * result + (int) (platformId ^ (platformId >>> 32));
			result = prime * result + (int) (sensorTypeId ^ (sensorTypeId >>> 32));
			return result;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			IdDefinition other = (IdDefinition) obj;
			if (methodId != other.methodId) {
				return false;
			}
			if (platformId != other.platformId) {
				return false;
			}
			if (sensorTypeId != other.sensorTypeId) {
				return false;
			}
			return true;
		}

	}

	/**
	 * The repository definition for this view to access the data.
	 */
	private RepositoryDefinition repositoryDefinition;

	/**
	 * If it is not necessary that every subview has its own
	 * {@link IdDefinition} object, then this ID can be used as the key for the
	 * map.
	 */
	public static final String GLOBAL_ID = "inspectit.subview.global";

	/**
	 * The idMappings are used for the subviews / controllers to retrieve their
	 * specific {@link IdDefinition} object. The key of the map is one of the
	 * IDs defined in the controller classes.
	 */
	private Map<String, List<IdDefinition>> idMappings = new HashMap<String, List<IdDefinition>>();

	/**
	 * The ID of the view.
	 */
	private SensorTypeEnum id;

	/**
	 * The name of the view part.
	 */
	private String partName;

	/**
	 * The tooltip is shown when hovering over the view part tab.
	 */
	private String partTooltip;

	/**
	 * The imageDescriptor descriptor of the view.
	 */
	private ImageDescriptor imageDescriptor;

	/**
	 * String used by the {@link Form} currently to display the text on it.
	 */
	private String headerText;

	/**
	 * String used by the {@link Form} currently to display the message on it.
	 */
	private String headerDescription;

	/**
	 * If the view should be updated automatically. Default is <code>true</code>
	 * .
	 */
	private boolean automaticUpdate = true;

	/**
	 * The default update rate of the automatic update mechanism.
	 */
	public static final long DEFAULT_UPDATE_RATE = 5000L;

	/**
	 * The update rate of the automatic update mechanism. If not specified, the
	 * default one is used {@link InputDefinition#DEFAULT_UPDATE_RATE}.
	 */
	private long updateRate = DEFAULT_UPDATE_RATE;

	/**
	 * Additional options if needed
	 */
	private Map<Object, Object> additionalOptions = new HashMap<Object, Object>();

	/**
	 * @return the repositoryDefinition
	 */
	public RepositoryDefinition getRepositoryDefinition() {
		return repositoryDefinition;
	}

	/**
	 * @param repositoryDefinition
	 *            the repositoryDefinition to set
	 */
	public void setRepositoryDefinition(RepositoryDefinition repositoryDefinition) {
		Assert.isNotNull(repositoryDefinition);

		this.repositoryDefinition = repositoryDefinition;
	}

	/**
	 * @return the id
	 */
	public SensorTypeEnum getId() {
		return id;
	}

	/**
	 * @param id
	 *            the id to set
	 */
	public void setId(SensorTypeEnum id) {
		this.id = id;
	}

	/**
	 * @return the partName
	 */
	public String getPartName() {
		return partName;
	}

	/**
	 * @param partName
	 *            the partName to set
	 */
	public void setPartName(String partName) {
		this.partName = partName;
	}

	/**
	 * @return the partTooltip
	 */
	public String getPartTooltip() {
		return partTooltip;
	}

	/**
	 * @param partTooltip
	 *            the partTooltip to set
	 */
	public void setPartTooltip(String partTooltip) {
		this.partTooltip = partTooltip;
	}

	/**
	 * @return the imageDescriptor
	 */
	public ImageDescriptor getImageDescriptor() {
		return imageDescriptor;
	}

	/**
	 * @param imageDescriptor
	 *            the imageDescriptor to set
	 */
	public void setImageDescriptor(ImageDescriptor imageDescriptor) {
		this.imageDescriptor = imageDescriptor;
	}

	/**
	 * @return the headerText
	 */
	public String getHeaderText() {
		return headerText;
	}

	/**
	 * @param headerText
	 *            the headerText to set
	 */
	public void setHeaderText(String headerText) {
		this.headerText = headerText;
	}

	/**
	 * @return the headerDescription
	 */
	public String getHeaderDescription() {
		return headerDescription;
	}

	/**
	 * @param headerDescription
	 *            the headerDescription to set
	 */
	public void setHeaderDescription(String headerDescription) {
		this.headerDescription = headerDescription;
	}

	/**
	 * @param automaticUpdate
	 *            the automaticUpdate to set
	 */
	public void setAutomaticUpdate(boolean automaticUpdate) {
		this.automaticUpdate = automaticUpdate;
	}

	/**
	 * @return the automaticUpdate
	 */
	public boolean isAutomaticUpdate() {
		return automaticUpdate;
	}

	/**
	 * @param updateRate
	 *            the updateRate to set
	 */
	public void setUpdateRate(long updateRate) {
		this.updateRate = updateRate;
	}

	/**
	 * @return the updateRate
	 */
	public long getUpdateRate() {
		return updateRate;
	}

	/**
	 * This is a convenience method to just define one {@link IdDefinition}
	 * object for this input definition. If one is already defined, a
	 * {@link RuntimeException} is thrown.
	 * 
	 * @param idDefinition
	 *            The ID definition to set.
	 */
	public void setIdDefinition(IdDefinition idDefinition) {
		if (idMappings.containsKey(GLOBAL_ID)) {
			throw new RuntimeException("Already defined an input definition!");
		}

		idMappings.put(GLOBAL_ID, new ArrayList<IdDefinition>());
		idMappings.get(GLOBAL_ID).add(idDefinition);
	}

	/**
	 * This is the counterpart method to {@link #setIdDefinition(IdDefinition)}.
	 * This method can be called as often as needed in contrary to
	 * {@link #getAndRemoveIdDefinition(String)}.
	 * 
	 * @return The single ID definition.
	 */
	public IdDefinition getIdDefinition() {
		if (!idMappings.containsKey(GLOBAL_ID)) {
			throw new RuntimeException("No unique id definition is set!");
		}

		return idMappings.get(GLOBAL_ID).get(0);
	}

	/**
	 * Appends the {@link IdDefinition} to the end of the list for the given ID.
	 * 
	 * @param id
	 *            The ID of this {@link IdDefinition} object.
	 * @param idDefinition
	 *            The {@link IdDefinition} object.
	 */
	public void addIdMapping(String id, IdDefinition idDefinition) {
		if (!idMappings.containsKey(id)) {
			idMappings.put(id, new ArrayList<IdDefinition>());
		}

		idMappings.get(id).add(idDefinition);
	}

	/**
	 * This method retrieves the id definition for the specific id. Important
	 * here is that the id definition will be removed from the list. This is
	 * necessary so that the same subviews in one editor could get different id
	 * definitions.
	 * <p>
	 * The order of the adding and retrieving of the subviews is important to
	 * visualize the correct graphs/tables etc.
	 * 
	 * @param id
	 *            The ID.
	 * @return The {@link IdDefinition} object.
	 */
	public IdDefinition getAndRemoveIdDefinition(String id) {
		if (!idMappings.containsKey(id)) {
			throw new RuntimeException("Key not found for ID definitions: " + id);
		}
		IdDefinition idDefinition = idMappings.get(id).remove(0);
		if (idMappings.get(id).isEmpty()) {
			idMappings.remove(id);
		}
		return idDefinition;
	}

	public void addAdditionalOption(Object key, Object value) {
		additionalOptions.put(key, value);
	}

	public Map<Object, Object> getAdditionalOptions() {
		return Collections.unmodifiableMap(additionalOptions);
	}

	public Object getAdditionalOption(Object key) {
		return additionalOptions.get(key);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((additionalOptions == null) ? 0 : additionalOptions.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((idMappings == null) ? 0 : idMappings.hashCode());
		result = prime * result + ((repositoryDefinition == null) ? 0 : repositoryDefinition.hashCode());
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		InputDefinition other = (InputDefinition) obj;
		if (additionalOptions == null) {
			if (other.additionalOptions != null) {
				return false;
			}
		} else if (!additionalOptions.equals(other.additionalOptions)) {
			return false;
		}
		if (id == null) {
			if (other.id != null) {
				return false;
			}
		} else if (!id.equals(other.id)) {
			return false;
		}
		if (idMappings == null) {
			if (other.idMappings != null) {
				return false;
			}
		} else if (!idMappings.equals(other.idMappings)) {
			return false;
		}
		if (repositoryDefinition == null) {
			if (other.repositoryDefinition != null) {
				return false;
			}
		} else if (!repositoryDefinition.equals(other.repositoryDefinition)) {
			return false;
		}
		return true;
	}

}
