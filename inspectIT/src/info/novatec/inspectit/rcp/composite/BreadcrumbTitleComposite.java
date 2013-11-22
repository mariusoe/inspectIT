package info.novatec.inspectit.rcp.composite;

import info.novatec.inspectit.cmr.model.PlatformIdent;
import info.novatec.inspectit.rcp.InspectIT;
import info.novatec.inspectit.rcp.formatter.ImageFormatter;
import info.novatec.inspectit.rcp.formatter.TextFormatter;
import info.novatec.inspectit.rcp.repository.CmrRepositoryChangeListener;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition.OnlineStatus;
import info.novatec.inspectit.rcp.repository.RepositoryDefinition;
import info.novatec.inspectit.rcp.repository.StorageRepositoryDefinition;
import info.novatec.inspectit.rcp.storage.listener.StorageChangeListener;
import info.novatec.inspectit.storage.IStorageData;

import java.util.Objects;

import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.resource.CompositeImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.ui.forms.FormColors;

/**
 * A composite to be the head client of the form that
 * {@link info.novatec.inspectit.rcp.editor.root.FormRootEditor} is made of.
 * 
 * @author Ivan Senic
 * 
 */
public class BreadcrumbTitleComposite extends Composite implements CmrRepositoryChangeListener, StorageChangeListener {

	/**
	 * Maximum text length for the label content.
	 */
	private static final int MAX_TEXT_LENGTH = 30;

	/**
	 * Arrow to be displayed between the breadcrumbs.
	 */
	private final Image arrow;

	/**
	 * {@link ToolBarManager} of the composite.
	 */
	private ToolBarManager toolBarManager;

	/**
	 * Displayed repository definition.
	 */
	private RepositoryDefinition repositoryDefinition;

	/**
	 * Label for repository name.
	 */
	private CLabel repositoryLabel;

	/**
	 * Label for agent name.
	 */
	private CLabel agentLabel;

	/**
	 * Label for group description.
	 */
	private CLabel groupLabel;

	/**
	 * Label for view description.
	 */
	private CLabel viewLabel;

	/**
	 * Default constructor.
	 * 
	 * @param parent
	 *            Parent composite.
	 * @param style
	 *            Style.
	 * @see Composite#Composite(Composite, int)
	 */
	public BreadcrumbTitleComposite(Composite parent, int style) {
		super(parent, style);
		arrow = new AccessibleArrowImage(true).createImage();
		init();
	}

	/**
	 * Initializes the widget.
	 */
	private void init() {
		// define layout
		GridLayout gridLayout = new GridLayout(2, false);
		gridLayout.marginHeight = 0;
		gridLayout.marginWidth = 0;
		setLayout(gridLayout);

		Composite breadcrumbComposite = new Composite(this, SWT.NONE);
		breadcrumbComposite.setLayout(new GridLayout(7, false));
		breadcrumbComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		repositoryLabel = new CLabel(breadcrumbComposite, SWT.NONE);
		repositoryLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));

		new Label(breadcrumbComposite, SWT.NONE).setImage(arrow);

		agentLabel = new CLabel(breadcrumbComposite, SWT.NONE);
		agentLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));

		new Label(breadcrumbComposite, SWT.NONE).setImage(arrow);

		groupLabel = new CLabel(breadcrumbComposite, SWT.NONE);
		groupLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));

		new Label(breadcrumbComposite, SWT.NONE).setImage(arrow);

		viewLabel = new CLabel(breadcrumbComposite, SWT.NONE);
		viewLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		// create tool-bar and its manager
		ToolBar toolbar = new ToolBar(this, SWT.FLAT);
		toolbar.setLayoutData(new GridData(SWT.END, SWT.CENTER, false, false));
		toolBarManager = new ToolBarManager(toolbar);
	}

	/**
	 * Gets {@link #toolBarManager}.
	 * 
	 * @return {@link #toolBarManager}
	 */
	public ToolBarManager getToolBarManager() {
		return toolBarManager;
	}

	/**
	 * Sets {@link #repositoryDefinition}.
	 * 
	 * @param repositoryDefinition
	 *            New value for {@link #repositoryDefinition}
	 */
	public void setRepositoryDefinition(RepositoryDefinition repositoryDefinition) {
		this.repositoryDefinition = repositoryDefinition;
		repositoryLabel.setText(TextFormatter.crop(repositoryDefinition.getName(), MAX_TEXT_LENGTH));
		if (repositoryDefinition instanceof CmrRepositoryDefinition) {
			repositoryLabel.setImage(ImageFormatter.getCmrRepositoryImage((CmrRepositoryDefinition) repositoryDefinition, true));
			InspectIT.getDefault().getCmrRepositoryManager().addCmrRepositoryChangeListener(this);
		} else if (repositoryDefinition instanceof StorageRepositoryDefinition) {
			repositoryLabel.setImage(ImageFormatter.getStorageRepositoryImage((StorageRepositoryDefinition) repositoryDefinition));
			InspectIT.getDefault().getInspectITStorageManager().addStorageChangeListener(this);
		}
	}

	/**
	 * Sets the agent name.
	 * 
	 * @param agentName
	 *            Agent name.
	 * @param agentImg
	 *            Image to go next to the agent name. If <code>null</code> is passed no changed to
	 *            the current image will be done.
	 */
	public void setAgent(String agentName, Image agentImg) {
		agentLabel.setText(TextFormatter.crop(agentName, MAX_TEXT_LENGTH));
		agentLabel.setToolTipText(agentName);
		if (null != agentImg) {
			agentLabel.setImage(agentImg);
		}
	}

	/**
	 * Sets the title text and image.
	 * 
	 * @param group
	 *            Group description.
	 * @param groupdImg
	 *            Image to go next to the title. If <code>null</code> is passed no changed to the
	 *            current image will be done.
	 */
	public void setGroup(String group, Image groupdImg) {
		groupLabel.setText(TextFormatter.crop(group, MAX_TEXT_LENGTH));
		groupLabel.setToolTipText(group);
		if (null != groupdImg) {
			groupLabel.setImage(groupdImg);
		}
		layoutInternal();
	}

	/**
	 * Sets the description.
	 * 
	 * @param view
	 *            View description.
	 * @param viewImg
	 *            Image to go next to the view description. If <code>null</code> is passed no
	 *            changed to the current image will be done.
	 */
	public void setView(String view, Image viewImg) {
		viewLabel.setText(view);
		viewLabel.setToolTipText(view);
		if (null != viewImg) {
			viewLabel.setImage(viewImg);
		}
		layoutInternal();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void repositoryOnlineStatusUpdated(CmrRepositoryDefinition cmrRepositoryDefinition, OnlineStatus oldStatus, OnlineStatus newStatus) {
		if (newStatus != OnlineStatus.CHECKING && Objects.equals(repositoryDefinition, cmrRepositoryDefinition)) {
			getDisplay().asyncExec(new Runnable() {
				@Override
				public void run() {
					repositoryLabel.setImage(ImageFormatter.getCmrRepositoryImage((CmrRepositoryDefinition) repositoryDefinition, true));
				}
			});

		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void repositoryDataUpdated(CmrRepositoryDefinition cmrRepositoryDefinition) {
		if (Objects.equals(repositoryDefinition, cmrRepositoryDefinition)) {
			getDisplay().asyncExec(new Runnable() {
				@Override
				public void run() {
					repositoryLabel.setText(repositoryDefinition.getName());
					layoutInternal();
				}
			});
		}

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void repositoryAgentDeleted(CmrRepositoryDefinition cmrRepositoryDefinition, PlatformIdent agent) {
	}

	/**
	 * Layouts the widget so that the text is properly displayed on the composite.
	 */
	private void layoutInternal() {
		layout(true, true);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void repositoryAdded(CmrRepositoryDefinition cmrRepositoryDefinition) {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void repositoryRemoved(CmrRepositoryDefinition cmrRepositoryDefinition) {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void storageDataUpdated(IStorageData storageData) {
		updateStorageDetailsIfDisplayed(storageData);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void storageRemotelyDeleted(IStorageData storageData) {
		updateStorageDetailsIfDisplayed(storageData);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void storageLocallyDeleted(IStorageData storageData) {
		updateStorageDetailsIfDisplayed(storageData);
	}

	/**
	 * Updates storage name and icon if given storageData is displayed currently on the breadcrumb.
	 * 
	 * @param storageData
	 *            {@link IStorageData}
	 */
	private void updateStorageDetailsIfDisplayed(IStorageData storageData) {
		if (repositoryDefinition instanceof StorageRepositoryDefinition) {
			final StorageRepositoryDefinition storageRepositoryDefinition = (StorageRepositoryDefinition) repositoryDefinition;
			if (Objects.equals(storageRepositoryDefinition.getLocalStorageData(), storageData)) {
				getDisplay().asyncExec(new Runnable() {
					@Override
					public void run() {
						repositoryLabel.setText(repositoryDefinition.getName());
						repositoryLabel.setImage(ImageFormatter.getStorageRepositoryImage(storageRepositoryDefinition));
						layoutInternal();
					}
				});
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void dispose() {
		arrow.dispose();
		InspectIT.getDefault().getCmrRepositoryManager().removeCmrRepositoryChangeListener(this);
		InspectIT.getDefault().getInspectITStorageManager().removeStorageChangeListener(this);
		super.dispose();
	}

	/**
	 * An arrow image descriptor. The images color is related to the list fore- and background
	 * color. This makes the arrow visible even in high contrast mode. If <code>ltr</code> is true
	 * the arrow points to the right, otherwise it points to the left.
	 * <p>
	 * <b>Class is copied from the
	 * {@link org.eclipse.jdt.internal.ui.javaeditor.breadcrumb.BreadcrumbItem} class. Code style
	 * has been fixed.</b>
	 */
	private final class AccessibleArrowImage extends CompositeImageDescriptor {

		/**
		 * Arrow size.
		 */
		private static final int ARROW_SIZE = 5;

		/**
		 * Left to right arrow boolean.
		 */
		private final boolean fLTR;

		/**
		 * Default constructor.
		 * 
		 * @param ltr
		 *            Left to right arrow.
		 */
		public AccessibleArrowImage(boolean ltr) {
			fLTR = ltr;
		}

		/**
		 * Draw the composite images.
		 * <p>
		 * Subclasses must implement this framework method to paint images within the given bounds
		 * using one or more calls to the <code>drawImage</code> framework method.
		 * </p>
		 * 
		 * @param width
		 *            the width
		 * @param height
		 *            the height
		 * @see org.eclipse.jface.resource.CompositeImageDescriptor#drawCompositeImage(int, int)
		 */
		protected void drawCompositeImage(int width, int height) {
			Display display = BreadcrumbTitleComposite.this.getDisplay();

			Image image = new Image(display, ARROW_SIZE, ARROW_SIZE * 2);

			GC gc = new GC(image);

			Color triangle = createColor(SWT.COLOR_LIST_FOREGROUND, SWT.COLOR_LIST_BACKGROUND, 20, display);
			Color aliasing = createColor(SWT.COLOR_LIST_FOREGROUND, SWT.COLOR_LIST_BACKGROUND, 30, display);
			gc.setBackground(triangle);

			if (fLTR) {
				gc.fillPolygon(new int[] { mirror(0), 0, mirror(ARROW_SIZE), ARROW_SIZE, mirror(0), ARROW_SIZE * 2 });
			} else {
				gc.fillPolygon(new int[] { ARROW_SIZE, 0, 0, ARROW_SIZE, ARROW_SIZE, ARROW_SIZE * 2 });
			}

			gc.setForeground(aliasing);
			gc.drawLine(mirror(0), 1, mirror(ARROW_SIZE - 1), ARROW_SIZE);
			gc.drawLine(mirror(ARROW_SIZE - 1), ARROW_SIZE, mirror(0), ARROW_SIZE * 2 - 1);

			gc.dispose();
			triangle.dispose();
			aliasing.dispose();

			ImageData imageData = image.getImageData();
			for (int y = 1; y < ARROW_SIZE; y++) {
				for (int x = 0; x < y; x++) {
					imageData.setAlpha(mirror(x), y, 255);
				}
			}
			for (int y = 0; y < ARROW_SIZE; y++) {
				for (int x = 0; x <= y; x++) {
					imageData.setAlpha(mirror(x), ARROW_SIZE * 2 - y - 1, 255);
				}
			}

			int offset = 0;
			if (!fLTR) {
				offset = -1;
			}
			drawImage(imageData, width / 2 - ARROW_SIZE / 2 + offset, height / 2 - ARROW_SIZE - 1);

			image.dispose();
		}

		/**
		 * Returns correct number of pixels depending on the arrow orientation. If arrow is set to
		 * be from left to right original parameter values i returned. if not then the mirrored
		 * value is returned.
		 * 
		 * @param x
		 *            Pixels.
		 * @return Returns correct number of pixels depending on the arrow orientation. If arrow is
		 *         set to be from left to right original parameter values i returned. if not then
		 *         the mirrored value is returned.
		 */
		private int mirror(int x) {
			if (fLTR) {
				return x;
			}

			return ARROW_SIZE - x - 1;
		}

		/**
		 * Return the size of this composite image.
		 * <p>
		 * Subclasses must implement this framework method.
		 * </p>
		 * 
		 * @return the x and y size of the image expressed as a point object
		 */
		protected Point getSize() {
			return new Point(10, 16);
		}

		/**
		 * Blends two colors with the given ration. The colors are represented by int values as
		 * colors as defined in the {@link SWT} class.
		 * 
		 * @param color1
		 *            First color.
		 * @param color2
		 *            Second color.
		 * @param ratio
		 *            Percentage of the first color in the blend (0-100).
		 * @param display
		 *            {@link Display}
		 * @return New color.
		 * @see FormColors#blend(RGB, RGB, int)
		 */
		private Color createColor(int color1, int color2, int ratio, Display display) {
			RGB rgb1 = display.getSystemColor(color1).getRGB();
			RGB rgb2 = display.getSystemColor(color2).getRGB();

			RGB blend = FormColors.blend(rgb2, rgb1, ratio);

			return new Color(display, blend);
		}
	}

}
