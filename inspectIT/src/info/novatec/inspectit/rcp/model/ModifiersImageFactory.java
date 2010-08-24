package info.novatec.inspectit.rcp.model;

import static info.novatec.inspectit.rcp.model.Modifier.isPackage;
import static info.novatec.inspectit.rcp.model.Modifier.isPrivate;
import static info.novatec.inspectit.rcp.model.Modifier.isProtected;
import static info.novatec.inspectit.rcp.model.Modifier.isPublic;
import info.novatec.inspectit.communication.ExceptionEventEnum;
import info.novatec.inspectit.communication.data.ExceptionSensorData;
import info.novatec.inspectit.rcp.InspectIT;
import info.novatec.inspectit.rcp.InspectITConstants;

import org.eclipse.jface.resource.ImageDescriptor;

/**
 * Creates the appropriate method visibility image for the int value.
 * 
 * @author Patrice Bouillet
 * @author Eduard Tudenhoefner
 * 
 */
public final class ModifiersImageFactory {

	/**
	 * The image descriptors for the private visibility method.
	 */
	private static final ImageDescriptor METHOD_PRIV_IMAGE = InspectIT.getDefault().getImageDescriptor(InspectITConstants.IMG_METHOD_PRIVATE);

	/**
	 * The image descriptor for the default visibility method.
	 */
	private static final ImageDescriptor METHOD_DEFAULT_IMAGE = InspectIT.getDefault().getImageDescriptor(InspectITConstants.IMG_METHOD_DEFAULT);

	/**
	 * The image descriptor for the protected visibility method.
	 */
	private static final ImageDescriptor METHOD_PROT_IMAGE = InspectIT.getDefault().getImageDescriptor(InspectITConstants.IMG_METHOD_PROTECTED);

	/**
	 * The image descriptor for the public visibility method.
	 */
	private static final ImageDescriptor METHOD_PUB_IMAGE = InspectIT.getDefault().getImageDescriptor(InspectITConstants.IMG_METHOD_PUBLIC);

	/**
	 * The image descriptor for the methods were exception events occured. TODO: this must be
	 * changed to a more efficient implementation.
	 */
	private static final ImageDescriptor CREATED_EXCEPTION_IMAGE = InspectIT.getDefault().getImageDescriptor(InspectITConstants.IMG_CREATED_EXCEPTION);
	private static final ImageDescriptor METHOD_PUB_PASSED_EX_IMAGE = InspectIT.getDefault().getImageDescriptor(InspectITConstants.IMG_METHODPUB_PASSED_EX);
	private static final ImageDescriptor METHOD_PUB_CATCHED_EX_IMAGE = InspectIT.getDefault().getImageDescriptor(InspectITConstants.IMG_METHODPUB_CATCHED_EX);
	private static final ImageDescriptor METHOD_PRO_PASSED_EX_IMAGE = InspectIT.getDefault().getImageDescriptor(InspectITConstants.IMG_METHODPRO_PASSED_EX);
	private static final ImageDescriptor METHOD_PRO_CATCHED_EX_IMAGE = InspectIT.getDefault().getImageDescriptor(InspectITConstants.IMG_METHODPRO_CATCHED_EX);
	private static final ImageDescriptor METHOD_DEF_PASSED_EX_IMAGE = InspectIT.getDefault().getImageDescriptor(InspectITConstants.IMG_METHODDEF_PASSED_EX);
	private static final ImageDescriptor METHOD_DEF_CATCHED_EX_IMAGE = InspectIT.getDefault().getImageDescriptor(InspectITConstants.IMG_METHODDEF_CATCHED_EX);
	private static final ImageDescriptor METHOD_PRI_PASSED_EX_IMAGE = InspectIT.getDefault().getImageDescriptor(InspectITConstants.IMG_METHODPRI_PASSED_EX);
	private static final ImageDescriptor METHOD_PRI_CATCHED_EX_IMAGE = InspectIT.getDefault().getImageDescriptor(InspectITConstants.IMG_METHODPRI_CATCHED_EX);

	/**
	 * Hide constructor to disallow instantiation.
	 */
	private ModifiersImageFactory() {
	}

	/**
	 * Returns the image descriptor for the given modifiers.
	 * 
	 * @param modifiers
	 *            The modifiers.
	 * @return The image descriptor.
	 */
	public static ImageDescriptor getImageDescriptor(int modifiers) {
		if (isPrivate(modifiers)) {
			return METHOD_PRIV_IMAGE;
		} else if (isPackage(modifiers)) {
			return METHOD_DEFAULT_IMAGE;
		} else if (isProtected(modifiers)) {
			return METHOD_PROT_IMAGE;
		} else if (isPublic(modifiers)) {
			return METHOD_PUB_IMAGE;
		}

		return ImageDescriptor.getMissingImageDescriptor();
	}

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
	public static ImageDescriptor getImageDescriptor(int modifiers, ExceptionSensorData data) {
		// TODO: please do not adapt. It will be changed in near future to a more
		// efficient implementation.
		if (null != data) {
			String eventType = data.getExceptionEventString();
			String created = ExceptionEventEnum.CREATED.toString();
			String passed = ExceptionEventEnum.PASSED.toString();
			String rethrown = ExceptionEventEnum.RETHROWN.toString();
			String handled = ExceptionEventEnum.HANDLED.toString();

			if (eventType.equals(created)) {
				return CREATED_EXCEPTION_IMAGE;
			} else if (isPrivate(modifiers)) {
				if (eventType.equals(passed) || eventType.equals(rethrown)) {
					return METHOD_PRI_PASSED_EX_IMAGE;
				} else if (eventType.equals(handled)) {
					return METHOD_PRI_CATCHED_EX_IMAGE;
				}
			} else if (isPackage(modifiers)) {
				if (eventType.equals(passed) || eventType.equals(rethrown)) {
					return METHOD_DEF_PASSED_EX_IMAGE;
				} else if (eventType.equals(handled)) {
					return METHOD_DEF_CATCHED_EX_IMAGE;
				}
			} else if (isProtected(modifiers)) {
				if (eventType.equals(passed) || eventType.equals(rethrown)) {
					return METHOD_PRO_PASSED_EX_IMAGE;
				} else if (eventType.equals(handled)) {
					return METHOD_PRO_CATCHED_EX_IMAGE;
				}
			} else if (isPublic(modifiers)) {
				if (eventType.equals(passed) || eventType.equals(rethrown)) {
					return METHOD_PUB_PASSED_EX_IMAGE;
				} else if (eventType.equals(handled)) {
					return METHOD_PUB_CATCHED_EX_IMAGE;
				}
			}
		} else {
			return getImageDescriptor(modifiers);
		}

		return ImageDescriptor.getMissingImageDescriptor();
	}
}
