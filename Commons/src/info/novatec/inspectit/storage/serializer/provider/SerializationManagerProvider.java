package info.novatec.inspectit.storage.serializer.provider;

import info.novatec.inspectit.storage.serializer.IKryoProvider;
import info.novatec.inspectit.storage.serializer.ISerializer;

/**
 * This is a typical provider of the new instances enhanced by Spring. Returns the
 * {@link ISerializer} instance.
 * 
 * @author Ivan Senic
 * 
 */
public abstract class SerializationManagerProvider {

	/**
	 * Returns the new {@link ISerializer} enhanced by Spring.
	 * 
	 * @param <E>
	 *            what can be returned
	 * @return Returns the new {@link ISerializer} enhanced by Spring.
	 */
	public abstract <E extends ISerializer & IKryoProvider> E createSerializer();
}
