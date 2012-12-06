package info.novatec.inspectit.storage;

import info.novatec.inspectit.communication.DefaultData;

import java.util.Map;

/**
 * Interface for the classes that can write data to disk.. Main implementation is storage writer.
 * 
 * @author Ivan Senic
 * 
 */
public interface IWriter {

	/**
	 * Writes one object to the disk.
	 * 
	 * @param defaultData
	 *            Object to be written.
	 */
	void write(DefaultData defaultData);

	/**
	 * Writes one object to the disk.
	 * 
	 * @param defaultData
	 *            Object to be written.
	 * @param kryoPreferences
	 *            Map of preferences to be passed to the Kryo serializer.
	 */
	void write(DefaultData defaultData, Map<?, ?> kryoPreferences);

}
