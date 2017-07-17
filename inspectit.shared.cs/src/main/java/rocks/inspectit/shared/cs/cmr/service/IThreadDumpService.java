package rocks.inspectit.shared.cs.cmr.service;

import java.util.Date;
import java.util.Map;

import rocks.inspectit.shared.all.cmr.service.ServiceExporterType;
import rocks.inspectit.shared.all.cmr.service.ServiceInterface;

/**
 * @author Marius Oehler
 *
 */
@ServiceInterface(exporter = ServiceExporterType.HTTP)
public interface IThreadDumpService {

	Map<Date, String> getThreadDumps(long platformId);

	/**
	 * @param platformId
	 */
	void requestThreadDump(long platformId);

	void storeThreadDump(long platformId, String threadDump);
}
