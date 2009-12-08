package info.novatec.novaspy.rcp.view.server;

import info.novatec.novaspy.rcp.model.Component;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;

/**
 * @author Patrice Bouillet
 * 
 */
public class ServerViewLabelProvider extends ColumnLabelProvider {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Image getImage(Object element) {
		if (element instanceof Component) {
			Component component = (Component) element;
			return component.getImageDescriptor().createImage();
		}

		return super.getImage(element);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getText(Object element) {
		if (element instanceof Component) {
			Component component = (Component) element;
			return component.getName();
		}

		return super.getText(element);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getToolTipText(Object element) {
		if (element instanceof Component) {
			Component component = (Component) element;
			return component.getTooltip();
		}

		return super.getToolTipText(element);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Point getToolTipShift(Object object) {
		int x = 5;
		int y = 5;
		return new Point(x, y);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getToolTipDisplayDelayTime(Object object) {
		return 500;
	}

}
