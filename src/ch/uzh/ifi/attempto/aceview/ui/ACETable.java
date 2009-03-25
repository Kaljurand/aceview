package ch.uzh.ifi.attempto.aceview.ui;

import java.awt.Font;
import java.awt.Point;
import java.awt.event.MouseEvent;

import javax.swing.ListSelectionModel;
import javax.swing.table.TableModel;

import org.jdesktop.swingx.JXTable;

import ch.uzh.ifi.attempto.aceview.ACESnippet;
import ch.uzh.ifi.attempto.aceview.util.SnippetRenderer;

/**
 * <p><code>ACETable</code> supports user-friendly viewing and editing of ACE sentences.
 * It shows a list of ACE sentences and their properties in a
 * sortable, highlightable, filterable, and configurable table.</p>
 * 
 * <p><code>ACETable</code> extends <code>JXTable</code>.
 * We might change this when we move to Java 6.</p>
 * 
 * @author Kaarel Kaljurand
 */
public class ACETable extends JXTable {

	public ACETable() {
		init();
	}

	public ACETable(TableModel tm) {
		super(tm);
		init();
	}

	public void setFont(String fontName, int fontSize) {
		setFont(new Font(fontName, Font.PLAIN, fontSize));
	}


	/**
	 * We override the table tooltip to be the ACE snippet rendering,
	 * if the mouse event happened on a snippet column.
	 * 
	 * TODO: Show the tooltip only if the text does not fit into the cell.
	 * How can I detect that?
	 */
	@Override
	public String getToolTipText(MouseEvent event) {
		Point p = event.getPoint();
		int col = columnAtPoint(p);
		int row = rowAtPoint(p);
		if (col >= 0 && row >= 0) {
			Object o = getValueAt(row, col);
			if (o != null && o instanceof ACESnippet) {
				ACESnippet snippet = (ACESnippet) o;
				SnippetRenderer snippetRenderer = new SnippetRenderer(snippet);
				return "<html><body><pre style='padding: 3px 3px 3px 3px'>" + snippetRenderer.getRendering() + "</pre></body></html>";
			}
		}
		return "";
	}


	private void init() {
		setShowGrid(true);
		setGridColor(Colors.GRID_COLOR);
		setColumnControlVisible(true);
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		//setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		setRowHeight(getRowHeight() + 2);
		getColumnModel().setColumnMargin(2);
	}
}