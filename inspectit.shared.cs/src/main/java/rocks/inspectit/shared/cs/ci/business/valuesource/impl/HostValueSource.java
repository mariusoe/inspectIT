package rocks.inspectit.shared.cs.ci.business.valuesource.impl;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import rocks.inspectit.shared.all.cmr.model.PlatformIdent;
import rocks.inspectit.shared.all.cmr.service.ICachedDataService;
import rocks.inspectit.shared.all.communication.data.InvocationSequenceData;
import rocks.inspectit.shared.cs.ci.business.expression.impl.StringMatchingExpression;
import rocks.inspectit.shared.cs.ci.business.valuesource.StringValueSource;

/**
 * This configuration element indicates that the host name or IP is to be used as the string value
 * for the {@link StringMatchingExpression}.
 *
 * @author Alexander Wert
 *
 */
@XmlRootElement(name = "host")
public class HostValueSource extends StringValueSource {
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String[] getStringValues(InvocationSequenceData invocSequence, ICachedDataService cachedDataService) {
		PlatformIdent pIdent = cachedDataService.getPlatformIdentForId(invocSequence.getPlatformIdent());
		List<String> ipList = pIdent.getDefinedIPs();
		String[] result = new String[ipList.size()];
		ipList.toArray(result);
		return result;
	}

}