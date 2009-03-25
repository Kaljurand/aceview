/*
 * This file is part of ACE View.
 * Copyright 2008-2009, Attempto Group, University of Zurich (see http://attempto.ifi.uzh.ch).
 *
 * ACE View is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 *
 * ACE View is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 * PURPOSE. See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with ACE View.
 * If not, see http://www.gnu.org/licenses/.
 */

package ch.uzh.ifi.attempto.aceview.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.UIManager;

import org.jdesktop.swingx.JXCollapsiblePane;
import org.jdesktop.swingx.JXLabel;

public class FeedbackPane extends JPanel {

	// Title of the feedback pane
	private final JXLabel labelTitle = new JXLabel();

	// Collapsible pane that embeds the input component
	private final JXCollapsiblePane pane = new JXCollapsiblePane(new BorderLayout());

	public FeedbackPane(JComponent component, String titleText) {
		super(new BorderLayout());

		setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));

		pane.setAnimated(false);
		pane.setCollapsed(false);
		pane.add(new JScrollPane(component));

		labelTitle.setText(titleText);

		// Toggle button
		JButton buttonToggle = new JButton(assignAction(pane));
		buttonToggle.setText("");
		buttonToggle.setToolTipText("Show/hide " + titleText + ".");

		// Remove the border from the button
		buttonToggle.setOpaque(false);
		buttonToggle.setFocusPainted(false);
		buttonToggle.setBorderPainted(false);
		buttonToggle.setContentAreaFilled(false);
		buttonToggle.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));


		// Toggle + Title
		Box boxToggleAndTitle = new Box(BoxLayout.X_AXIS);
		boxToggleAndTitle.add(buttonToggle);
		boxToggleAndTitle.add(Box.createRigidArea(new Dimension(5,0)));
		boxToggleAndTitle.add(labelTitle);
		boxToggleAndTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 3, 0));

		add(boxToggleAndTitle, BorderLayout.NORTH);
		add(pane, BorderLayout.CENTER);
	}


	public void setTitle(String titleText) {
		labelTitle.setText(titleText);
	}


	public void setCollapsed(boolean b) {
		pane.setCollapsed(b);
	}


	private static Action assignAction(JXCollapsiblePane collapsible) {
		// get the built-in toggle action
		Action toggleAction = collapsible.getActionMap().get(JXCollapsiblePane.TOGGLE_ACTION);
		// use the collapse/expand icons from the JTree UI
		toggleAction.putValue(JXCollapsiblePane.COLLAPSE_ICON, UIManager.getIcon("Tree.expandedIcon"));
		toggleAction.putValue(JXCollapsiblePane.EXPAND_ICON, UIManager.getIcon("Tree.collapsedIcon"));
		return toggleAction;
	}
}