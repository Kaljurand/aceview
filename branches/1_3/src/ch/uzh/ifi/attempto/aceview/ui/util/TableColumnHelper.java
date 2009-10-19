package ch.uzh.ifi.attempto.aceview.ui.util;

import org.jdesktop.swingx.JXTable;

import ch.uzh.ifi.attempto.aceview.model.TableColumn;
import ch.uzh.ifi.attempto.aceview.ui.BooleanRenderer;
import ch.uzh.ifi.attempto.aceview.ui.TableRenderFontRight;

public class TableColumnHelper {

	private static final int BOOLEAN_WIDTH = 25;
	private static final int COUNT_WIDTH = 50;

	private TableColumnHelper() {}

	public static void configureColumns(JXTable table, TableColumn[] columns) {
		for (TableColumn column : columns) {
			table.getColumnExt(column.getName()).setVisible(column.isVisible());
			if (column.getDataClass().equals(Integer.class)) {
				// table.getColumnExt(column.getName()).setPreferredWidth(COUNT_WIDTH);
				table.getColumnExt(column.getName()).setMaxWidth(COUNT_WIDTH);
				table.getColumnExt(column.getName()).setCellRenderer(new TableRenderFontRight());
			}
			else if (column.getDataClass().equals(Boolean.class)) {
				// table.getColumnExt(column.getName()).setPreferredWidth(BOOLEAN_WIDTH);
				table.getColumnExt(column.getName()).setMaxWidth(BOOLEAN_WIDTH);
				table.getColumnExt(column.getName()).setCellRenderer(new BooleanRenderer());
			}
		}
	}
}