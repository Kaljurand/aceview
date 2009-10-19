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
import java.awt.Font;

import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.semanticweb.owl.model.OWLEntity;
import org.semanticweb.owl.model.OWLObject;

import ch.uzh.ifi.attempto.aceview.ACEText;
import ch.uzh.ifi.attempto.aceview.ACETextManager;
import ch.uzh.ifi.attempto.aceview.WordsHyperlinkListener;
import ch.uzh.ifi.attempto.aceview.model.event.ACETextChangeEvent;
import ch.uzh.ifi.attempto.aceview.model.event.ACETextManagerListener;

/**
 * <p>This view component provides the index-view to the ACE text.</p>
 * 
 * @author Kaarel Kaljurand
 */
public class ACEIndexViewComponent extends AbstractACEViewComponent {

	private JEditorPane editorpaneIndex;
	private WordsHyperlinkListener wordsHyperlinkListener;

	private final ACETextManagerListener aceTextManagerListener = new ACETextManagerListener() {
		public void handleChange(ACETextChangeEvent event) {
			showIndex();
		}
	};


	private void showIndex() {
		ACEText acetext = ACETextManager.getActiveACEText();
		getView().setHeaderText(acetext.getReferencedEntities().size() + " content word(s) in " + acetext.getSentences().size() + " sentence(s)");
		editorpaneIndex.setText(ACETextManager.wrapInHtml(acetext.getIndexBody()));
	}

	@Override
	protected OWLObject updateView() {
		OWLEntity entity = getOWLWorkspace().getOWLSelectionModel().getSelectedEntity();
		if (isShowing() && entity != null) {
			String entityRendering = getOWLModelManager().getRendering(entity);
			editorpaneIndex.scrollToReference(entityRendering);
		}
		return entity;
	}


	@Override
	public void initialiseView() throws Exception {
		editorpaneIndex = new JEditorPane("text/html", ACETextManager.wrapInHtml(""));
		editorpaneIndex.setFont(new Font("Monaco", Font.PLAIN, 11));
		editorpaneIndex.setEnabled(true);
		editorpaneIndex.setEditable(false);
		wordsHyperlinkListener = new WordsHyperlinkListener(getOWLWorkspace());
		editorpaneIndex.addHyperlinkListener(wordsHyperlinkListener);

		JScrollPane scrollpaneIndex = new JScrollPane(editorpaneIndex,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		JPanel panelIndex = new JPanel(new BorderLayout());
		panelIndex.add(scrollpaneIndex);

		setLayout(new BorderLayout());

		add(panelIndex);

		ACETextManager.addListener(aceTextManagerListener);
		showIndex();
	}

	@Override
	public void disposeView() {
		editorpaneIndex.removeHyperlinkListener(wordsHyperlinkListener);
		ACETextManager.removeListener(aceTextManagerListener);
	}

	@Override
	public void refreshComponent() {
		if (editorpaneIndex != null) {
			editorpaneIndex.setFont(owlRendererPreferences.getFont());
		}
	}
}