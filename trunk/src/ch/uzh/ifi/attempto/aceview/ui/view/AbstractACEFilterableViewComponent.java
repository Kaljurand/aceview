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

package ch.uzh.ifi.attempto.aceview.ui.view;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

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

	@Override
	public void initialiseView() throws Exception {
		new ButtonGroup() {{
			add(buttonHighlight);
			add(buttonFilter);
		}};

		panelButtons.setLayout(new BoxLayout(panelButtons, BoxLayout.LINE_AXIS));
		panelButtons.add(new JLabel("Find snippet by: "));
		panelButtons.add(buttonHighlight);
		panelButtons.add(buttonFilter);

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