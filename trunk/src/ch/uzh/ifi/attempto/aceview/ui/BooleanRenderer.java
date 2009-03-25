package ch.uzh.ifi.attempto.aceview.ui;

import java.awt.Component;

import javax.swing.JCheckBox;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

public class BooleanRenderer extends JCheckBox implements TableCellRenderer {
	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {

		setBackground(table.getBackground());
		setSelected(value == Boolean.TRUE);
		setEnabled(table.isCellEditable(row, column));
		return this;
	}
}