package info.novatec.novaspy.rcp.model;

import static info.novatec.novaspy.rcp.model.Modifier.isPackage;
import static info.novatec.novaspy.rcp.model.Modifier.isPrivate;
import static info.novatec.novaspy.rcp.model.Modifier.isProtected;
import static info.novatec.novaspy.rcp.model.Modifier.isPublic;
import info.novatec.novaspy.rcp.NovaSpy;
import info.novatec.novaspy.rcp.NovaSpyConstants;

import org.eclipse.jface.resource.ImageDescriptor;

/**
 * Creates the appropriate method visibility image for the int value.
 * 
 * @author Patrice Bouillet
 * 
 */
public final class ModifiersImageFactory {

	/**
	 * The image descriptor for the private visibility method.
	 */
	private static final ImageDescriptor METHOD_PRIV_IMAGE = NovaSpy.getDefault().getImageDescriptor(NovaSpyConstants.IMG_METHOD_PRIVATE);

	/**
	 * The image descriptor for the default visibility method.
	 */
	private static final ImageDescriptor METHOD_DEFAULT_IMAGE = NovaSpy.getDefault().getImageDescriptor(NovaSpyConstants.IMG_METHOD_DEFAULT);

	/**
	 * The image descriptor for the protected visibility method.
	 */
	private static final ImageDescriptor METHOD_PROT_IMAGE = NovaSpy.getDefault().getImageDescriptor(NovaSpyConstants.IMG_METHOD_PROTECTED);

	/**
	 * The image descriptor for the public visibility method.
	 */
	private static final ImageDescriptor METHOD_PUB_IMAGE = NovaSpy.getDefault().getImageDescriptor(NovaSpyConstants.IMG_METHOD_PUBLIC);

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
}
