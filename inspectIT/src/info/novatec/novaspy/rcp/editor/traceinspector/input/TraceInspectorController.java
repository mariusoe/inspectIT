package info.novatec.novaspy.rcp.editor.traceinspector.input;

import info.novatec.novaspy.cmr.model.MethodIdent;
import info.novatec.novaspy.communication.data.InvocationSequenceData;
import info.novatec.novaspy.rcp.editor.AbstractSubView;
import info.novatec.novaspy.rcp.editor.InputDefinition;

import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

public class TraceInspectorController {

	private double xResolution = 1.0;
	private double yResolution = 1.0;
	private int blockHeight = 20;
	private boolean resolutionWasSet = false;
	private InputDefinition inputDefinition = null;
	private TreeMap<Integer, TreeMap<Integer, InvocationBlock>> clickMap = null;
	private InvocationBlock highlightedInvocationBlock = null;
	private InvocationSequenceData invocationSequence = null;
	private AbstractSubView transactionInspectorSubView = null;
	private boolean isHeated = false;
	private CanvasAdapter canvasAdapter = null;

	public TraceInspectorController(AbstractSubView tranasctionAbstractSubView, CanvasAdapter canvasAdapter) {
		this.transactionInspectorSubView = tranasctionAbstractSubView;
		this.canvasAdapter = canvasAdapter;
	}

	public void setInputDefinition(InputDefinition inputDefinition) {
		this.inputDefinition = inputDefinition;
	}

	public void setResolution(double resolution) {
		resolutionWasSet = true;
		this.xResolution = resolution;
	}

	public double getResolution() {
		return xResolution;
	}

	public TreeMap<Integer, TreeMap<Integer, InvocationBlock>> getClickMap() {
		return clickMap;
	}

	public void setClickMap(TreeMap<Integer, TreeMap<Integer, InvocationBlock>> clickMap) {
		this.clickMap = clickMap;
	}

	public void paint() {
		if (clickMap == null) {
			clickMap = new TreeMap<Integer, TreeMap<Integer, InvocationBlock>>();
		}

		this.paint(canvasAdapter, (InvocationSequenceTraceInspectorDecorator) invocationSequence, xResolution, yResolution);
	}

	private void paint(CanvasAdapter canvas, InvocationSequenceTraceInspectorDecorator i, double xResolution, double yResolution) {
		if (resolutionWasSet == false) {
			xResolution = canvas.getArea().width / i.getDuration();
			this.xResolution = xResolution;
		}

		this.paint(canvas, i, xResolution, yResolution, 0, i);
	}

	private void paint(CanvasAdapter canvas, InvocationSequenceTraceInspectorDecorator i, double xResolution, double yResolution, int yoffset, InvocationSequenceTraceInspectorDecorator topSequence) {
		int width = (int) Math.round(i.getDuration() * xResolution);
		int height = (int) Math.round(blockHeight * yResolution);
		int xoffset = (int) ((i.getStart() - topSequence.getStart()) * xResolution);
		MethodIdent methodIdent = inputDefinition.getRepositoryDefinition().getGlobalDataAccessService().getMethodIdentForId(i.getMethodIdent());

		InvocationBlock invocationBlock = i.getInvocationBlock();
		invocationBlock.setArea(new Rectangle(xoffset, yoffset, width, height));
		if (i.getParentSequence() == null) {
			invocationBlock.setAmount(100.0);
		} else {
			invocationBlock.setAmount((i.getDuration() / topSequence.getDuration()) * 100);
		}

		this.feedClickMap(invocationBlock, xoffset, yoffset, width, height);

		if (!isHeated) {
			canvas.drawSequence(invocationBlock);
		} else {
			canvas.drawSequenceHeated(invocationBlock);
		}

		List<InvocationSequenceData> subSequences = i.getNestedSequences();
		for (InvocationSequenceData subSequence : subSequences) {
			this.paint(canvas, (InvocationSequenceTraceInspectorDecorator) subSequence, xResolution, yResolution, yoffset + height, topSequence);
		}
	}

	/**
	 * Inserts the InvocationBlock into the clickmap, according to the resulting
	 * xy coordinates, the height and the width of the invocationblock.
	 * 
	 * @param invocationBlock
	 *            The invocationBlock which should be inserted to the clickmap
	 * @param xoffset
	 *            The drawing x-offset of the block. This is the first key
	 *            identifying the invocationblock in the x treemap.
	 * @param width
	 *            offset+width is the second key which identifies the
	 *            invocationblock in the x treemap
	 * @param yoffset
	 *            The first key identifying the invocationblock in y treemap.
	 *            The second key is calculated from y+blockheight. The
	 *            blockheight is the same for all blocks.
	 */
	private void feedClickMap(InvocationBlock invocationBlock, int xoffset, int yoffset, int width, int height) {
		SortedMap<Integer, TreeMap<Integer, InvocationBlock>> yMap = clickMap.tailMap(yoffset);
		SortedMap<Integer, InvocationBlock> xMap = null;
		if (yMap.size() > 0) {
			xMap = yMap.get(yMap.firstKey());
		} else {
			xMap = new TreeMap<Integer, InvocationBlock>();
			yMap.put(yoffset, (TreeMap<Integer, InvocationBlock>) xMap);
			yMap.put(yoffset + (height - 1), (TreeMap<Integer, InvocationBlock>) xMap);
		}
		xMap.put(xoffset, invocationBlock);
		xMap.put(xoffset + (width - 1), invocationBlock);
	}

	private InvocationBlock findInvocationBlock(int x, int y) {
		InvocationBlock invocationBlock = null;
		SortedMap<Integer, TreeMap<Integer, InvocationBlock>> yMap = clickMap.tailMap(y);
		if (yMap.size() > 0) {
			SortedMap<Integer, InvocationBlock> xMap = yMap.get(yMap.firstKey());
			if (xMap != null) {
				xMap = xMap.tailMap(x);
				invocationBlock = xMap.get(xMap.firstKey());

				// check if the x coordinate lies within the bounds of the
				// InvocationBlock. If not we only found the first block
				// after a gap between two blocks or the first block of the
				// row if
				// the coordinate lies before every block on this row.
				// This means the given coordinate doesn't lie in the area
				// of any
				// invocationblock and the result has to be null.
				// It's not necessary to check the y axis, because
				// there are no gaps possible on the y axis.
				if (x < invocationBlock.getArea().x) {
					invocationBlock = null;
				}
			}
		}
		return invocationBlock;
	}

	public void highlightInvocationBlock(int x, int y) {
		InvocationBlock invocationBlock = this.findInvocationBlock(x, y);
		if (highlightedInvocationBlock != invocationBlock) {
			if (highlightedInvocationBlock != null) {
				highlightedInvocationBlock.setHighlighted(false);
			}
			highlightedInvocationBlock = invocationBlock;
			highlightedInvocationBlock.setHighlighted(true);
			canvasAdapter.updateDetail(invocationBlock);
			transactionInspectorSubView.doRefresh();
		}

	}

	/**
	 * Decorates the complete tree of invocation sequences with the
	 * {@link InvocationSequenceTraceInspectorDecorator}.
	 * 
	 * @param invocationSequence
	 *            The root invocation sequence which should be decorated
	 * @return The decorated invocation sequence
	 */
	private InvocationSequenceData decorateInvocationSequence(InvocationSequenceData invocationSequence) {
		InvocationBlock invocationBlock = new InvocationBlock();
		InvocationSequenceTraceInspectorDecorator decorator = new InvocationSequenceTraceInspectorDecorator(invocationSequence, invocationBlock);

		List<InvocationSequenceData> nestedSequences = invocationSequence.getNestedSequences();
		for (int i = 0; i < nestedSequences.size(); i++) {
			InvocationSequenceData subSequence = nestedSequences.get(i);
			nestedSequences.set(i, this.decorateInvocationSequence(subSequence));
			subSequence.setParentSequence(decorator);
		}

		MethodIdent methodIdent = inputDefinition.getRepositoryDefinition().getGlobalDataAccessService().getMethodIdentForId(invocationSequence.getMethodIdent());
		invocationBlock.setMethodIdent(methodIdent);
		invocationBlock.setInvocationSequence(decorator);
		return decorator;

	}

	public void setInvocationSequence(InvocationSequenceData invocationSequence) {
		this.invocationSequence = this.decorateInvocationSequence(invocationSequence);
		this.resolutionWasSet = false;
		this.clickMap = null;
	}

	public InvocationSequenceData getInvocationSequence() {
		return invocationSequence;
	}

	public void decreaseResolution(double percent) {
		this.xResolution = this.xResolution - this.xResolution * (percent / 100);
		this.yResolution = this.yResolution - this.yResolution * (percent / 100);
		this.resolutionWasSet = true;
		this.clickMap = null;
		transactionInspectorSubView.doRefresh();
	}

	public void increaseResolution(double percent) {
		this.xResolution = this.xResolution + this.xResolution * (percent / 100);
		this.yResolution = this.yResolution + this.yResolution * (percent / 100);
		this.resolutionWasSet = true;
		this.clickMap = null;
		transactionInspectorSubView.doRefresh();
	}

	public void resetResolution() {
		this.xResolution = 1.0;
		this.yResolution = 1.0;
		this.resolutionWasSet = 5 < 4;
		this.clickMap = null;
		transactionInspectorSubView.doRefresh();
	}

	public void drillDown(int x, int y) {
		InvocationBlock invocationBlock = this.findInvocationBlock(x, y);
		if (invocationBlock != null) {
			this.invocationSequence = invocationBlock.getInvocationSequence();
			this.clickMap = null;
			transactionInspectorSubView.doRefresh();
		}
	}

	public void drillDown() {
		if (highlightedInvocationBlock != null) {
			this.invocationSequence = highlightedInvocationBlock.getInvocationSequence();
			this.clickMap = null;
			transactionInspectorSubView.doRefresh();
		}
	}

	public void drillUp() {
		if (invocationSequence.getParentSequence() != null) {
			this.invocationSequence = invocationSequence.getParentSequence();
			this.clickMap = null;
			transactionInspectorSubView.doRefresh();
		}
	}

	public void toggleHeat() {
		this.isHeated = !isHeated;
		this.clickMap = null;
		transactionInspectorSubView.doRefresh();
	}
}
