package info.novatec.inspectit.agent.connection;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * See http://www.onjava.com/pub/a/onjava/2001/10/17/rmi.html?page=3 for more details.
 * 
 * Since all the semantics are captured in the name, we might as well make this externalizable
 * (saves a little bit on reflection, saves a little bit on bandwidth).
 * 
 * @author William Grosso
 * 
 */

public class RetryException extends Exception implements Externalizable {

	/**
	 * The serial version UID of this class.
	 */
	private static final long serialVersionUID = 0L;

	/**
	 * {@inheritDoc}
	 */
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
	}

	/**
	 * {@inheritDoc}
	 */
	public void writeExternal(ObjectOutput out) throws IOException {
	}

}
