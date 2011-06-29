package info.novatec.inspectit.rcp.editor.composite;

import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.rcp.editor.ISubView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IPostSelectionProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.forms.widgets.FormToolkit;

/**
 * This implementation of a composite view lays out its children in its own tabs. Every tab can be
 * given a name.
 * 
 * @author Patrice Bouillet
 * 
 */
public class TabbedCompositeSubView extends AbstractCompositeSubView {

	/**
	 * The container widget.
	 */
	private CTabFolder tabFolder;

	/**
	 * The names of the tabs.
	 */
	private Map<ISubView, String> tabNames = new HashMap<ISubView, String>();

	/**
	 * The images of the tabs.
	 */
	private Map<ISubView, ImageDescriptor> tabImageDescriptors = new HashMap<ISubView, ImageDescriptor>();

	/**
	 * {@inheritDoc}
	 */
	public void createPartControl(Composite parent, FormToolkit toolkit) {
		tabFolder = new CTabFolder(parent, SWT.BOTTOM | SWT.FLAT | SWT.H_SCROLL | SWT.V_SCROLL);
		tabFolder.setBorderVisible(true);

		for (final ISubView subView : getSubViews()) {
			subView.createPartControl(tabFolder, toolkit);
			CTabItem item = new CTabItem(tabFolder, SWT.NONE, getPageCount());
			item.setControl(subView.getControl());
			item.setText(tabNames.get(subView));
			item.setImage(tabImageDescriptors.get(subView).createImage());

			subView.getControl().addFocusListener(new FocusAdapter() {
				/**
				 * {@inheritDoc}
				 */
				@Override
				public void focusGained(FocusEvent e) {
					getRootEditor().setActiveSubView(subView);
				}
			});

			if (null != subView.getSelectionProvider()) {
				ISelectionProvider prov = subView.getSelectionProvider();
				prov.addSelectionChangedListener(new ISelectionChangedListener() {
					@Override
					public void selectionChanged(SelectionChangedEvent event) {
						getRootEditor().setSelection(event.getSelection());
					}
				});
				prov.addSelectionChangedListener(getRootEditor().getSelectionChangedListener());
				if (prov instanceof IPostSelectionProvider) {
					((IPostSelectionProvider) prov).addPostSelectionChangedListener(getRootEditor().getPostSelectionChangedListener());
				}
			}
		}

		tabFolder.setSelection(0);
	}

	/**
	 * Returns the number of pages.
	 * 
	 * @return the number of pages
	 */
	private int getPageCount() {
		if (null != tabFolder && !tabFolder.isDisposed()) {
			return tabFolder.getItemCount();
		}
		return 0;
	}

	/**
	 * Adds a sub-view to this tabbed view.
	 * 
	 * @param subView
	 *            The sub-view to add.
	 * @param tabName
	 *            The name of the sub-view.
	 * @param tabImageDescriptor
	 *            The image descriptor of this sub-view.
	 */
	public void addSubView(ISubView subView, String tabName, ImageDescriptor tabImageDescriptor) {
		super.addSubView(subView);

		tabNames.put(subView, tabName);
		tabImageDescriptors.put(subView, tabImageDescriptor);
	}

	/**
	 * {@inheritDoc}
	 */
	public Control getControl() {
		return tabFolder;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setDataInput(List<? extends DefaultData> data) {
		super.setDataInput(data);

		// The following is needed for Bug INSPECTIT-184
		// The problem is that the visible attribute for windows seems not be
		// correct under some circumstances.
		CTabItem[] items = tabFolder.getItems();
		for (CTabItem cTabItem : items) {
			if (cTabItem.equals(tabFolder.getSelection())) {
				cTabItem.getControl().setVisible(true);
			} else {
				cTabItem.getControl().setVisible(false);
			}
		}
	}

}
