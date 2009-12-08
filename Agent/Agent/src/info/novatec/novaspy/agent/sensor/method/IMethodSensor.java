package info.novatec.novaspy.agent.sensor.method;

import info.novatec.novaspy.agent.hooking.IHook;
import info.novatec.novaspy.agent.hooking.IMethodHook;
import info.novatec.novaspy.agent.sensor.ISensor;

/**
 * Every method sensor installs a hook into the target class which can be
 * retrieved later with the {@link #getHook()} method.
 * 
 * @author Patrice Bouillet
 * 
 */
public interface IMethodSensor extends ISensor {

	/**
	 * Returns the proper method hook.
	 * 
	 * @return The {@link IMethodHook} implementation of the corresponding
	 *         sensor.
	 */
	IHook getHook();

}
