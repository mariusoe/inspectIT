package info.novatec.inspectit.rcp.formatter;

import info.novatec.inspectit.communication.data.cmr.AgentStatusData;
import info.novatec.inspectit.rcp.InspectIT;
import info.novatec.inspectit.rcp.InspectITImages;
import info.novatec.inspectit.rcp.model.AgentLeaf;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition.OnlineStatus;
import info.novatec.inspectit.rcp.repository.RepositoryDefinition;
import info.novatec.inspectit.rcp.repository.StorageRepositoryDefinition;
import info.novatec.inspectit.storage.StorageData;
import info.novatec.inspectit.storage.StorageData.StorageState;
import info.novatec.inspectit.storage.WritingStatus;
import info.novatec.inspectit.storage.label.type.AbstractCustomStorageLabelType;
import info.novatec.inspectit.storage.label.type.AbstractStorageLabelType;
import info.novatec.inspectit.storage.label.type.impl.AssigneeLabelType;
import info.novatec.inspectit.storage.label.type.impl.CreationDateLabelType;
import info.novatec.inspectit.storage.label.type.impl.ExploredByLabelType;
import info.novatec.inspectit.storage.label.type.impl.RatingLabelType;
import info.novatec.inspectit.storage.label.type.impl.StatusLabelType;
import info.novatec.inspectit.storage.label.type.impl.UseCaseLabelType;

import java.util.Date;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.jface.viewers.DecorationOverlayIcon;
import org.eclipse.swt.graphics.Image;

/**
 * The class provide image descriptors for the different elements.
 * 
 * @author Ivan Senic
 * 
 */
public final class ImageFormatter {

	/**
	 * Private constructor.
	 */
	private ImageFormatter() {
	}

	/**
	 * Returns the {@link Image} for the composite that represents a label.
	 * 
	 * @param labelType
	 *            Label type.
	 * @return {@link Image} for {@link Composite}.
	 */
	public static Image getImageForLabel(AbstractStorageLabelType<?> labelType) {
		if (AssigneeLabelType.class.equals(labelType.getClass())) {
			return InspectIT.getDefault().getImage(InspectITImages.IMG_ASSIGNEE_LABEL_ICON);
		} else if (CreationDateLabelType.class.equals(labelType.getClass())) {
			return InspectIT.getDefault().getImage(InspectITImages.IMG_DATE_LABEL_ICON);
		} else if (ExploredByLabelType.class.equals(labelType.getClass())) {
			return InspectIT.getDefault().getImage(InspectITImages.IMG_MOUNTEDBY_LABEL_ICON);
		} else if (RatingLabelType.class.equals(labelType.getClass())) {
			return InspectIT.getDefault().getImage(InspectITImages.IMG_RATING_LABEL_ICON);
		} else if (StatusLabelType.class.equals(labelType.getClass())) {
			return InspectIT.getDefault().getImage(InspectITImages.IMG_STATUS_LABEL_ICON);
		} else if (UseCaseLabelType.class.equals(labelType.getClass())) {
			return InspectIT.getDefault().getImage(InspectITImages.IMG_USECASE_LABEL_ICON);
		} else {
			return InspectIT.getDefault().getImage(InspectITImages.IMG_USER_LABEL_ICON);
		}
	}

	/**
	 * Returns the {@link ImageDescriptor} for the composite that represents a label.
	 * 
	 * @param labelType
	 *            Label type.
	 * @return {@link ImageDescriptor} for {@link Composite}.
	 */
	public static ImageDescriptor getImageDescriptorForLabel(AbstractStorageLabelType<?> labelType) {
		if (AssigneeLabelType.class.equals(labelType.getClass())) {
			return InspectIT.getDefault().getImageDescriptor(InspectITImages.IMG_ASSIGNEE_LABEL_ICON);
		} else if (CreationDateLabelType.class.equals(labelType.getClass())) {
			return InspectIT.getDefault().getImageDescriptor(InspectITImages.IMG_DATE_LABEL_ICON);
		} else if (ExploredByLabelType.class.equals(labelType.getClass())) {
			return InspectIT.getDefault().getImageDescriptor(InspectITImages.IMG_MOUNTEDBY_LABEL_ICON);
		} else if (RatingLabelType.class.equals(labelType.getClass())) {
			return InspectIT.getDefault().getImageDescriptor(InspectITImages.IMG_RATING_LABEL_ICON);
		} else if (StatusLabelType.class.equals(labelType.getClass())) {
			return InspectIT.getDefault().getImageDescriptor(InspectITImages.IMG_STATUS_LABEL_ICON);
		} else if (UseCaseLabelType.class.equals(labelType.getClass())) {
			return InspectIT.getDefault().getImageDescriptor(InspectITImages.IMG_USECASE_LABEL_ICON);
		} else if (AbstractCustomStorageLabelType.class.isAssignableFrom(labelType.getClass())) {
			AbstractCustomStorageLabelType<?> customLabelType = (AbstractCustomStorageLabelType<?>) labelType;
			if (Boolean.class.equals(customLabelType.getValueClass())) {
				return InspectIT.getDefault().getImageDescriptor(InspectITImages.IMG_CHECKMARK);
			} else if (Date.class.equals(customLabelType.getValueClass())) {
				return InspectIT.getDefault().getImageDescriptor(InspectITImages.IMG_CALENDAR);
			} else if (Number.class.equals(customLabelType.getValueClass())) {
				return InspectIT.getDefault().getImageDescriptor(InspectITImages.IMG_CALCULATOR);
			} else if (String.class.equals(customLabelType.getValueClass())) {
				return InspectIT.getDefault().getImageDescriptor(InspectITImages.IMG_USER_LABEL_ICON);
			}
		}
		return null;
	}

	/**
	 * 
	 * @param storageData
	 *            {@link StorageData} to get picture for.
	 * @return Returns the {@link Image} for the storage, based on the
	 *         {@link StorageData.StorageState}.
	 */
	public static Image getImageForStorageLeaf(StorageData storageData) {
		if (storageData.getState() == StorageState.CREATED_NOT_OPENED) {
			return InspectIT.getDefault().getImage(InspectITImages.IMG_STORAGE_NEW);
		} else if (storageData.getState() == StorageState.OPENED) {
			return InspectIT.getDefault().getImage(InspectITImages.IMG_STORAGE_OPENED);
		} else if (storageData.getState() == StorageState.RECORDING) {
			return InspectIT.getDefault().getImage(InspectITImages.IMG_STORAGE_RECORDING);
		} else if (storageData.getState() == StorageState.CLOSED) {
			return InspectIT.getDefault().getImage(InspectITImages.IMG_STORAGE_CLOSED);
		}
		return null;
	}

	/**
	 * Returns image based on the CMR repository status.
	 * 
	 * @param selectedCmrRepositoryDefinition
	 *            {@link CmrRepositoryDefinition}.
	 * @param small
	 *            Should picture be small or big.
	 * @return Image.
	 */
	public static Image getCmrRepositoryImage(CmrRepositoryDefinition selectedCmrRepositoryDefinition, boolean small) {
		if (selectedCmrRepositoryDefinition.getOnlineStatus() == OnlineStatus.ONLINE) {
			if (small) {
				return InspectIT.getDefault().getImage(InspectITImages.IMG_SERVER_ONLINE_SMALL);
			} else {
				return InspectIT.getDefault().getImage(InspectITImages.IMG_SERVER_ONLINE);
			}
		} else if (selectedCmrRepositoryDefinition.getOnlineStatus() == OnlineStatus.OFFLINE) {
			if (small) {
				return InspectIT.getDefault().getImage(InspectITImages.IMG_SERVER_OFFLINE_SMALL);
			} else {
				return InspectIT.getDefault().getImage(InspectITImages.IMG_SERVER_OFFLINE);
			}
		} else {
			if (small) {
				return InspectIT.getDefault().getImage(InspectITImages.IMG_SERVER_REFRESH_SMALL);
			} else {
				return InspectIT.getDefault().getImage(InspectITImages.IMG_SERVER_REFRESH);
			}
		}
	}

	/**
	 * Returns image for the title box.
	 * 
	 * @param storageRepositoryDefinition
	 *            {@link StorageRepositoryDefinition}
	 * @return Image for the title box.
	 */
	public static Image getStorageRepositoryImage(StorageRepositoryDefinition storageRepositoryDefinition) {
		if (storageRepositoryDefinition.getLocalStorageData().isFullyDownloaded() || storageRepositoryDefinition.getCmrRepositoryDefinition().getOnlineStatus() != OnlineStatus.OFFLINE) {
			return InspectIT.getDefault().getImage(InspectITImages.IMG_STORAGE_AVAILABLE);
		} else {
			return InspectIT.getDefault().getImage(InspectITImages.IMG_STORAGE_NOT_AVAILABLE);
		}
	}

	/**
	 * Returns image that represents the {@link WritingStatus} or null if the writing status passed
	 * is null.
	 * 
	 * @param status
	 *            Image for {@link WritingStatus}.
	 * @return Returns image that represents the {@link WritingStatus} or null if the writing status
	 *         passed is null.
	 */
	public static Image getWritingStatusImage(WritingStatus status) {
		if (null == status) {
			return null;
		}
		switch (status) {
		case GOOD:
			return InspectIT.getDefault().getImage(InspectITImages.IMG_EVENT_GREEN);
		case MEDIUM:
			return InspectIT.getDefault().getImage(InspectITImages.IMG_EVENT_YELLOW);
		case BAD:
			return InspectIT.getDefault().getImage(InspectITImages.IMG_EVENT_RED);
		default:
			return null;
		}
	}

	/**
	 * Returns overlayed icon for editors with additional {@link ImageDescriptor} depending if the
	 * repository is CMR or Storage repository.
	 * 
	 * @param original
	 *            Original icon.
	 * @param repositoryDefinition
	 *            Repository definition.
	 * @param resourceManager
	 *            Resource manager that image will be created with. It is responsibility of a caller
	 *            to provide {@link ResourceManager} for correct image disposing.
	 * @return Overlayed icon.
	 */
	public static Image getOverlayedEditorImage(Image original, RepositoryDefinition repositoryDefinition, ResourceManager resourceManager) {
		if (repositoryDefinition instanceof CmrRepositoryDefinition) {
			return original;
		} else if (repositoryDefinition instanceof StorageRepositoryDefinition) {
			ImageDescriptor overlayDescriptor = InspectIT.getDefault().getImageDescriptor(InspectITImages.IMG_STORAGE_OVERLAY);
			DecorationOverlayIcon icon = new DecorationOverlayIcon(original, new ImageDescriptor[] { null, null, null, overlayDescriptor, null });
			Image img = resourceManager.createImage(icon);
			return img;
		}
		return null;
	}

	/**
	 * Returns the image for the agent leaf based on the last data sent date.
	 * 
	 * @param agentLeaf
	 *            {@link AgentLeaf}.
	 * @return {@link Image}
	 */
	public static Image getAgentLeafImage(AgentLeaf agentLeaf) {
		AgentStatusData agentStatusData = agentLeaf.getAgentStatusData();
		if (null != agentStatusData && null != agentStatusData.getMinutesSinceLastData()) {
			long minutes = agentStatusData.getMinutesSinceLastData().longValue();
			if (minutes > 0) {
				return InspectIT.getDefault().getImage(InspectITImages.IMG_AGENT_YELLOW);
			} else {
				return InspectIT.getDefault().getImage(InspectITImages.IMG_AGENT_GREEN);
			}
		} else {
			return InspectIT.getDefault().getImage(InspectITImages.IMG_AGENT_GRAY);
		}
	}
}
