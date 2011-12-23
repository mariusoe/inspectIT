package info.novatec.inspectit.rcp.model;

import info.novatec.inspectit.communication.ExceptionEvent;
import info.novatec.inspectit.communication.data.ExceptionSensorData;
import info.novatec.inspectit.rcp.InspectIT;
import info.novatec.inspectit.rcp.InspectITConstants;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.DecorationOverlayIcon;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.swt.graphics.Image;

/**
 * Factory currently used for the overlay icons which can be placed on arbitrary images being
 * passed.
 * 
 * @author Patrice Bouillet
 * 
 */
public class ExceptionImageFactory {

	/**
	 * The image descriptor for the error overlay.
	 */
	private static final ImageDescriptor OVERLAY_ERROR = InspectIT.getDefault().getImageDescriptor(InspectITConstants.OVERLAY_ERROR);

	/**
	 * The image descriptor for the priority overlay.
	 */
	private static final ImageDescriptor OVERLAY_PRIORITY = InspectIT.getDefault().getImageDescriptor(InspectITConstants.OVERLAY_PRIORITY);

	/**
	 * The image descriptor for the up overlay.
	 */
	private static final ImageDescriptor OVERLAY_UP = InspectIT.getDefault().getImageDescriptor(InspectITConstants.OVERLAY_UP);

	/**
	 * Returns the image descriptor for the given modifiers.
	 * 
	 * @param modifiers
	 *            The modifiers.
	 * @param data
	 *            The {@link ExceptionSensorData} data object where to check for the exception event
	 *            type.
	 * @return The image descriptor.
	 */
	public static ImageDescriptor getImageDescriptor(ExceptionSensorData data) {
		if (null != data) {
			ExceptionEvent event = data.getExceptionEvent();
			if (ExceptionEvent.CREATED.equals(event)) {
				return OVERLAY_ERROR;
			} else if (ExceptionEvent.HANDLED.equals(event)) {
				return OVERLAY_PRIORITY;
			} else if (ExceptionEvent.PASSED.equals(event)) {
				return OVERLAY_UP;
			} else if (ExceptionEvent.RETHROWN.equals(event)) {
				return OVERLAY_ERROR;
			} else if (ExceptionEvent.UNREGISTERED_PASSED.equals(event)) {
				return OVERLAY_UP;
			}
		}
		return ImageDescriptor.getMissingImageDescriptor();
	}

	/**
	 * The passed image will be decorated with an overlay which is defined on the event type of the
	 * exception transparently.
	 * 
	 * @param image
	 *            the image that will be decorated with the overlay.
	 * @param data
	 *            the exception data from which the event will be extracted.
	 * @return the resulting image
	 */
	public static Image decorateImageWithException(Image image, ExceptionSensorData data) {
		ImageDescriptor exceptionDesc = getImageDescriptor(data);
		DecorationOverlayIcon icon = new DecorationOverlayIcon(image, exceptionDesc, IDecoration.BOTTOM_RIGHT);
		Image createdImage = icon.createImage();
		return createdImage;
	}

}
