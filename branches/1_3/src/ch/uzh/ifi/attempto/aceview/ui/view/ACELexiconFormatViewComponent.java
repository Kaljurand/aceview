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

import java.awt.BorderLayout;

import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.protege.editor.owl.ui.view.AbstractOWLViewComponent;

import ch.uzh.ifi.attempto.aceview.ACETextManager;
import ch.uzh.ifi.attempto.aceview.lexicon.TokenMapper;
import ch.uzh.ifi.attempto.aceview.model.event.ACEViewEvent;
import ch.uzh.ifi.attempto.aceview.model.event.ACEViewListener;
import ch.uzh.ifi.attempto.aceview.model.event.TextEventType;
import ch.uzh.ifi.attempto.aceview.ui.util.ComponentFactory;

/**
 * <p>This view component shows the ACE lexicon in the
 * ACE lexicon format (which is understood by the ACE parser).</p>
 * 
 * @author Kaarel Kaljurand
 *
 */
public class ACELexiconFormatViewComponent extends AbstractOWLViewComponent {

	private JTextArea textareaLexicon;

	private final ACEViewListener<ACEViewEvent<TextEventType>> aceTextManagerListener = new ACEViewListener<ACEViewEvent<TextEventType>>() {
		public void handleChange(ACEViewEvent<TextEventType> event) {
			if (event.isType(TextEventType.ACELEXICON_CHANGED)) {
				showLexicon();
			}
			else if (event.isType(TextEventType.ACTIVE_ACETEXT_CHANGED)) {
				showLexicon();
			}
		}
	};


	@Override
	protected void initialiseOWLView() throws Exception {
		textareaLexicon = ComponentFactory.makeTextArea();
		textareaLexicon.setLineWrap(true);
		textareaLexicon.setWrapStyleWord(true);

		setLayout(new BorderLayout());
		add(BorderLayout.CENTER, new JScrollPane(textareaLexicon));

		ACETextManager.addListener(aceTextManagerListener);
		showLexicon();
	}


	private void showLexicon() {
		TokenMapper aceLexicon = ACETextManager.getActiveACELexicon();
		textareaLexicon.setText(aceLexicon.toACELexiconFormat());

		int numberOfEntries = aceLexicon.size();
		if (numberOfEntries == 1) {
			getView().setHeaderText("1 entry");
		}
		else {
			getView().setHeaderText(numberOfEntries + " entries");
		}
	}


	@Override
	protected void disposeOWLView() {
		ACETextManager.removeListener(aceTextManagerListener);
	}
}