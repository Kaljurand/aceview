package ch.uzh.ifi.attempto.aceview.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComboBox;
import javax.swing.JPanel;

import org.jdesktop.swingx.VerticalLayout;

public class ServiceSelectionPane extends JPanel {

	private final JComboBox selectionBox;
	private final HideablePane[] panes;

	public ServiceSelectionPane(String[] selections, String selectedItem, HideablePane[] panes) {
		super(new VerticalLayout());
		selectionBox = new JComboBox(selections);
		selectionBox.setEditable(false);
		selectionBox.setSelectedItem(selectedItem);
		this.panes = panes;

		selectionBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				showSelectedService();
			}
		});

		showSelectedService();

		add(selectionBox);
		for (HideablePane pane : panes) {
			add(pane);
		}
	}


	public Object getSelectedItem() {
		return selectionBox.getSelectedItem();
	}


	private void showSelectedService() {
		for (HideablePane pane : panes) {
			pane.setCollapsed(true);
		}
		int selectedIndex = selectionBox.getSelectedIndex();
		panes[selectedIndex].setCollapsed(false);		
	}
}