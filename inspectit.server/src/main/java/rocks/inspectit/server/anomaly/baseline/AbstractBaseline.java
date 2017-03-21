package rocks.inspectit.server.anomaly.baseline;

/**
 * @author Marius Oehler
 *
 */
public abstract class AbstractBaseline<E extends AbstractBaselineContext<?>> {

	public abstract void process(E context, long time);

	public abstract double getBaseline();

}
