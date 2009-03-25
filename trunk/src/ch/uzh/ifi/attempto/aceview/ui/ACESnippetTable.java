package ch.uzh.ifi.attempto.aceview.ui;

import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import ch.uzh.ifi.attempto.aceview.ACESnippet;
import ch.uzh.ifi.attempto.aceview.ACETextManager;

public class ACESnippetTable extends ACETable {

	public ACESnippetTable() {
		getSelectionModel().addListSelectionListener(new SelectionListener(this));

		// TODO: What does this do?
		//getColumnModel().getSelectionModel().addListSelectionListener(new SelectionListener(this));
	}

	private class SelectionListener implements ListSelectionListener {

		private final ACESnippetTable table;

		SelectionListener(ACESnippetTable table) {
			this.table = table;
		}

		/**
		 * Shows the snippet which corresponds to the selected row index in the selection.
		 */
		public void valueChanged(ListSelectionEvent e) {
			if (e.getSource() == table.getSelectionModel() && table.getRowSelectionAllowed()) {
				int selectedRow = table.getSelectedRow();
				if (selectedRow == -1) {
				}
				else {
					// BUG: magic number: 0
					Object cellContent = table.getValueAt(selectedRow, 0);
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