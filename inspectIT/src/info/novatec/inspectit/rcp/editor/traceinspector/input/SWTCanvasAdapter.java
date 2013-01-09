package info.novatec.inspectit.rcp.editor.traceinspector.input;

import info.novatec.inspectit.cmr.model.MethodIdent;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Table;

public class SWTCanvasAdapter implements CanvasAdapter {
	private RGB defaultFillColor = new RGB(210, 210, 255);
	private RGB defaultHiglightedFillColor = new RGB(240, 240, 255);
	private RGB defaultLineColor = new RGB(0, 0, 0);
	private GC gc = null;
	private Table detailTable = null;

	public SWTCanvasAdapter() {
	}

	public void drawSequence(InvocationBlock invocationBlock) {
		this.drawSequence(invocationBlock, this.defaultFillColor, this.defaultHiglightedFillColor, this.defaultLineColor, false);
	}

	public void drawSequenceHeated(InvocationBlock invocationBlock) {
		this.drawSequence(invocationBlock, this.defaultFillColor, this.defaultHiglightedFillColor, this.defaultLineColor, true);
	}

	public synchronized void drawSequence(InvocationBlock invocationBlock, RGB fillColor, RGB highlightColor, RGB lineColor, boolean heated) {

		Color oldForeground = gc.getForeground();
		Color oldBackground = gc.getBackground();

		try {

			Rectangle rect = invocationBlock.getArea();
			MethodIdent methodIdent = invocationBlock.getMethodIdent();

			Color swtLineColor = new Color(gc.getDevice(), lineColor);
			Color swtFillColor = new Color(gc.getDevice(), fillColor);
			if (invocationBlock.isHighlighted()) {
				swtFillColor = new Color(gc.getDevice(), highlightColor);
			}

			if (heated) {
				int factor = 255 - (int) (255 * (invocationBlock.getAmount() / 100));
				RGB heatedColor = new RGB(255, factor, factor);
				swtFillColor = new Color(gc.getDevice(), heatedColor);
			}

			gc.setForeground(new Color(gc.getDevice(), 255, 255, 255));
			gc.setBackground(swtFillColor);
			gc.fillRectangle(rect.x, rect.y, rect.width, rect.height);

			// Line Drawing
			gc.setForeground(swtLineColor);
			gc.setLineWidth(1);
			gc.drawRoundRectangle(rect.x, rect.y, rect.width, rect.height, 2, 2);

			gc.setForeground(swtLineColor);
			String methodName = methodIdent.getClassName() + "." + methodIdent.getMethodName();
			int textWidth = 0;
			FontData oldFontData = gc.getFont().getFontData()[0];
			while (((textWidth = gc.getFontMetrics().getAverageCharWidth() * methodName.length()) >= (rect.width + 2)) || ((gc.getFontMetrics().getHeight()) > rect.height + 2)) {
				FontData fontData = gc.getFont().getFontData()[0];
				fontData.setHeight(fontData.getHeight() - 1);
				gc.setFont(new Font(gc.getDevice(), fontData));
			}

			if (gc.getFont().getFontData()[0].getHeight() >= 6) {
				int xTextStart = rect.x + (rect.width / 2 - textWidth / 2);
				int yTextStart = rect.y + (rect.height / 2 - gc.getFontMetrics().getHeight() / 2);
				gc.drawText(methodIdent.getClassName() + "." + methodIdent.getMethodName(), xTextStart, yTextStart, true);
			}
			gc.setFont(new Font(gc.getDevice(), oldFontData));
		} finally {
			gc.setForeground(oldForeground);
			gc.setBackground(oldBackground);
		}
	}

	@Override
	public synchronized void updateDetail(InvocationBlock invocationBlock) {
		detailTable.getItem(0).setText(1, invocationBlock.getMethodIdent().getClassName());
		detailTable.getItem(1).setText(1, invocationBlock.getMethodIdent().getMethodName());
		detailTable.getItem(2).setText(1, Double.toString(invocationBlock.getInvocationSequence().getDuration()));
	}

	@Override
	public synchronized Rectangle getArea() {
		org.eclipse.swt.graphics.Rectangle r = gc.getClipping();
		Rectangle rect = new Rectangle(r.x, r.y, r.width, r.height);
		return rect;
	}

	public synchronized void setGC(GC gc) {
		this.gc = gc;
	}

	public synchronized GC getGC() {
		return this.gc;
	}

	public synchronized void setDetailtable(Table table) {
		this.detailTable = table;
	}

	public synchronized Table getDetailTable() {
		return detailTable;
	}
}
