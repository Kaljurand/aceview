package ch.uzh.ifi.attempto.aceview.ui;

import javax.swing.JComponent;

import org.jdesktop.swingx.JXCollapsiblePane;

public class HideablePane extends JXCollapsiblePane {

	public HideablePane(JComponent component) {
		setAnimated(false);
		setCollapsed(true);
		add(component);
	}
}