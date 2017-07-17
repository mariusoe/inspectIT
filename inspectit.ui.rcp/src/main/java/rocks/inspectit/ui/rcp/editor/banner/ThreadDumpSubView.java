package rocks.inspectit.ui.rcp.editor.banner;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.MapUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;

import rocks.inspectit.shared.cs.cmr.service.IThreadDumpService;
import rocks.inspectit.ui.rcp.editor.AbstractSubView;
import rocks.inspectit.ui.rcp.editor.preferences.PreferenceEventCallback.PreferenceEvent;
import rocks.inspectit.ui.rcp.editor.preferences.PreferenceId;
import rocks.inspectit.ui.rcp.repository.CmrRepositoryDefinition;
import rocks.inspectit.ui.rcp.repository.RepositoryDefinition;
import rocks.inspectit.ui.rcp.util.SafeExecutor;

/**
 * @author Marius Oehler
 *
 */
public class ThreadDumpSubView extends AbstractSubView implements SelectionListener, Listener {

	/**
	 * The {@link Composite}.
	 */
	private Composite composite;

	private Map<Date, String> threadDumps;

	private org.eclipse.swt.widgets.List list;

	private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd - HH:mm:ss");

	private Text textbox;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void init() {
		getRootEditor().getInputDefinition();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void createPartControl(Composite parent, FormToolkit toolkit) {
		composite = toolkit.createComposite(parent);
		composite.setLayout(new GridLayout(2, false));

		list = new org.eclipse.swt.widgets.List(composite, SWT.BORDER | SWT.SINGLE | SWT.V_SCROLL);
		list.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, true));
		list.addSelectionListener(this);

		textbox = toolkit.createText(composite, "", SWT.MULTI | SWT.BORDER | SWT.WRAP | SWT.V_SCROLL | SWT.READ_ONLY);
		textbox.setLayoutData(new GridData(GridData.FILL_BOTH));

		toolkit.createComposite(composite).setLayoutData(new GridData(0, 0));

		Button button = toolkit.createButton(composite, "Request new thread-dump", SWT.PUSH);
		button.setLayoutData(new GridData(SWT.RIGHT, SWT.TOP, false, false));
		button.addListener(SWT.Selection, this);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void handleEvent(Event event) {
		IThreadDumpService threaDumpService = getRootEditor().getInputDefinition().getRepositoryDefinition().getThreaDumpService();

		long platformId = getRootEditor().getInputDefinition().getIdDefinition().getPlatformId();

		threaDumpService.requestThreadDump(platformId);
	}

	private void updateList() {
		if (MapUtils.isNotEmpty(threadDumps)) {
			String[] selection = list.getSelection();

			list.removeAll();

			ArrayList<Date> tempList = new ArrayList<>(threadDumps.keySet());
			Collections.sort(tempList);
			Collections.reverse(tempList);

			for (Date date : tempList) {
				list.add(dateFormat.format(date));
			}

			if (selection.length > 0) {
				list.setSelection(selection);
				updateThreadDumpContent(selection[0]);
			} else if (list.getItemCount() > 0) {
				list.setSelection(0);
				updateThreadDumpContent(list.getSelection()[0]);
			}

			composite.layout();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void widgetSelected(SelectionEvent e) {
		String[] selection = ((org.eclipse.swt.widgets.List) e.getSource()).getSelection();

		if (selection.length <= 0) {
			return;
		}

		String selectedElement = selection[0];
		updateThreadDumpContent(selectedElement);
	}

	private void updateThreadDumpContent(String selectedElement) {
		try {
			Date dateKey = dateFormat.parse(selectedElement);

			String threadDump = threadDumps.get(dateKey);

			textbox.setText(threadDump);
		} catch (ParseException e1) {
			e1.printStackTrace();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void widgetDefaultSelected(SelectionEvent e) {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Set<PreferenceId> getPreferenceIds() {
		Set<PreferenceId> preferences = EnumSet.noneOf(PreferenceId.class);
		preferences.add(PreferenceId.UPDATE);
		return preferences;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void doRefresh() {
		Job job = new Job("Loading thread-dumps..") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				RepositoryDefinition repositoryDefinition = getRootEditor().getInputDefinition().getRepositoryDefinition();

				if (repositoryDefinition instanceof CmrRepositoryDefinition) {
					final CmrRepositoryDefinition cmrRepositoryDefinition = (CmrRepositoryDefinition) repositoryDefinition;
					final long platformId = getRootEditor().getInputDefinition().getIdDefinition().getPlatformId();

					threadDumps = cmrRepositoryDefinition.getThreaDumpService().getThreadDumps(platformId);

					SafeExecutor.asyncExec(new Runnable() {
						@Override
						public void run() {
							updateList();
						}
					});
				}

				return Status.OK_STATUS;
			}
		};

		job.schedule();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void preferenceEventFired(PreferenceEvent preferenceEvent) {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setDataInput(List<? extends Object> data) {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Control getControl() {
		return composite;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ISelectionProvider getSelectionProvider() {
		return null;
	}
}
