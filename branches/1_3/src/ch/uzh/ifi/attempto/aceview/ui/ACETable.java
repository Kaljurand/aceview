package ch.uzh.ifi.attempto.aceview.ui;

import java.awt.Font;

import javax.swing.ListSelectionModel;
import javax.swing.table.TableModel;

import org.jdesktop.swingx.JXTable;


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