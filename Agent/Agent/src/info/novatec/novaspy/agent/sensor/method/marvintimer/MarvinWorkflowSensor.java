package info.novatec.novaspy.agent.sensor.method.marvintimer;

import info.novatec.novaspy.agent.config.IConfigurationStorage;
import info.novatec.novaspy.agent.config.IPropertyAccessor;
import info.novatec.novaspy.agent.core.IIdManager;
import info.novatec.novaspy.agent.hooking.IHook;
import info.novatec.novaspy.agent.sensor.method.IMethodSensor;
import info.novatec.novaspy.util.Timer;

import java.util.Map;

public class MarvinWorkflowSensor implements IMethodSensor {
	/**
	 * The timer used for accurate measuring.
	 */
	private final Timer timer;

	/**
	 * The ID manager.
	 */
	private final IIdManager idManager;

	/**
	 * The property accessor.
	 */
	private final IPropertyAccessor propertyAccessor;

	/**
	 * The used average timer hook.
	 */
	private MarvinWorkflowHook marvinWorkflowHook = null;

	/**
	 * The configuration storage.
	 */
	private IConfigurationStorage configurationStorage;

	/**
	 * The default constructor which needs 3 parameter for initialization.
	 * 
	 * @param timer
	 *            The timer used for accurate measuring.
	 * @param idManager
	 *            The ID manager.
	 * @param propertyAccessor
	 *            The property accessor.
	 */
	public MarvinWorkflowSensor(Timer timer, IIdManager idManager, IPropertyAccessor propertyAccessor, IConfigurationStorage configurationStorage) {
		this.timer = timer;
		this.idManager = idManager;
		this.propertyAccessor = propertyAccessor;
		this.configurationStorage = configurationStorage;
	}

	public IHook getHook() {
		return marvinWorkflowHook;
	}

	public void init(Map parameter) {
		marvinWorkflowHook = new MarvinWorkflowHook(timer, idManager, propertyAccessor, configurationStorage);
	}

}
