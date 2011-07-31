/*
 * This file is part of ACE View.
 * Copyright 2008-2011, Attempto Group, University of Zurich (see http://attempto.ifi.uzh.ch).
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

package ch.uzh.ifi.attempto.aceview.ui.view;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

/**
 * <p>This view component has a radio button group for entity-based filtering
 * (e.g. in a snippets table, or lexicon table).
 * The view is updated whenever the state of the button group changes.</p>
 * 
 * @author Kaarel Kaljurand
 */
public abstract class AbstractACEFilterableViewComponent extends AbstractACEViewComponent {

	protected final JRadioButton buttonHighlight = new JRadioButton("Highlight");
	protected final JRadioButton buttonFilter = new JRadioButton("Filter");
	protected final JPanel panelButtons = new JPanel();
	protected final JPanel panelLeftButtons = new JPanel();
	protected final JPanel panelMiddleButtons = new JPanel();
	protected final JPanel panelRightButtons = new JPanel();

	@Override
	public void initialiseView() throws Exception {
		new ButtonGroup() {{
			add(buttonHighlight);
			add(buttonFilter);
		}};

		panelButtons.setLayout(new BoxLayout(panelButtons, BoxLayout.LINE_AXIS));
		panelButtons.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
		panelLeftButtons.setLayout(new BoxLayout(panelLeftButtons, BoxLayout.LINE_AXIS));
		panelLeftButtons.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
		panelMiddleButtons.setLayout(new BoxLayout(panelMiddleButtons, BoxLayout.LINE_AXIS));
		panelMiddleButtons.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
		panelRightButtons.setLayout(new BoxLayout(panelRightButtons, BoxLayout.LINE_AXIS));
		panelRightButtons.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
		panelLeftButtons.add(new JLabel("Find snippet by: "));
		panelLeftButtons.add(buttonHighlight);
		panelLeftButtons.add(Box.createRigidArea(new Dimension(5, 0)));
		panelLeftButtons.add(buttonFilter);
		panelButtons.add(panelLeftButtons);
		panelButtons.add(Box.createHorizontalGlue());
		panelButtons.add(panelMiddleButtons);
		panelButtons.add(Box.createHorizontalGlue());
		panelButtons.add(panelRightButtons);

		buttonHighlight.setSelected(true);
		buttonHighlight.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				updateView();
			}
		});

		buttonFilter.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				updateView();
			}
		});
	}
}