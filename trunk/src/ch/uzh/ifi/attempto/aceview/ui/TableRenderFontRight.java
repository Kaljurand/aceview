package ch.uzh.ifi.attempto.aceview.ui;

import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

public class TableRenderFontRight extends DefaultTableCellRenderer {
	@Override
	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {

		setText((value == null) ? "    " : value.toString());
		setHorizontalAlignment(JLabel.RIGHT);

		return this;
	}
}