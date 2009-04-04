package ch.uzh.ifi.attempto.aceview.ui;

import java.awt.Point;
import java.awt.event.MouseEvent;

import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import ch.uzh.ifi.attempto.aceview.ACESnippet;
import ch.uzh.ifi.attempto.aceview.ACETextManager;
import ch.uzh.ifi.attempto.aceview.util.SnippetRenderer;

public class ACESnippetTable extends ACETable {

	public ACESnippetTable(int snippetColumn) {
		getSelectionModel().addListSelectionListener(new SelectionListener(this, snippetColumn));

		// TODO: What does this do?
		//getColumnModel().getSelectionModel().addListSelectionListener(new SelectionListener(this));
	}


	/**
	 * <p>We override the table tooltip to be the ACE snippet rendering,
	 * if the mouse event happened on a snippet column.</p>
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
				return "<html><body><pre style='font-family: sans-serif; padding: 2px 2px 2px 2px'>" + snippetRenderer.getRendering() + "</pre></body></html>";
			}
		}
		return "";
	}


	private class SelectionListener implements ListSelectionListener {

		private final ACESnippetTable table;
		private final int snippetColumn;

		SelectionListener(ACESnippetTable table, int snippetColumn) {
			this.table = table;
			this.snippetColumn = snippetColumn;
		}

		/**
		 * Shows the snippet which corresponds to the selected row index in the selection.
		 */
		public void valueChanged(ListSelectionEvent e) {
			if (e.getSource() == table.getSelectionModel() && table.getRowSelectionAllowed()) {
				int selectedRow = table.getSelectedRow();
				if (selectedRow != -1) {
					Object cellContent = table.getValueAt(selectedRow, snippetColumn);
					if (cellContent instanceof ACESnippet) {
						ACESnippet selectedSnippet = (ACESnippet) cellContent;
						ACETextManager.setSelectedSnippet(selectedSnippet);
					}
				}
			} else if (e.getSource() == table.getColumnModel().getSelectionModel() && table.getColumnSelectionAllowed()) {
				// BUG: do nothing
			}

			if (e.getValueIsAdjusting()) {
				// The mouse button has not yet been released
			}
		}
	}
}