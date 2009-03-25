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

import javax.swing.JEditorPane;
import javax.swing.JScrollPane;

import org.semanticweb.owl.model.OWLEntity;
import org.semanticweb.owl.model.OWLObject;

import ch.uzh.ifi.attempto.aceview.ACETextManager;
import ch.uzh.ifi.attempto.aceview.WordsHyperlinkListener;
import ch.uzh.ifi.attempto.aceview.model.event.ACETextChangeEvent;
import ch.uzh.ifi.attempto.aceview.model.event.ACETextManagerListener;

public class ACEWordUsageViewComponent extends AbstractACEViewComponent {

	private JEditorPane editorpaneIndex;

	private WordsHyperlinkListener hyperlinkListener;

	private final ACETextManagerListener aceTextManagerListener = new ACETextManagerListener() {
		public void handleChange(ACETextChangeEvent event) {
			updateView();
		}
	};

	@Override
	public void disposeView() {
		editorpaneIndex.removeHyperlinkListener(hyperlinkListener);
		removeHierarchyListener(hierarchyListener);
		ACETextManager.removeListener(aceTextManagerListener);
	}

	@Override
	public void initialiseView() throws Exception {
		editorpaneIndex = new JEditorPane("text/html", ACETextManager.wrapInHtml(""));
		editorpaneIndex.setEnabled(true);
		editorpaneIndex.setEditable(false);
		hyperlinkListener = new WordsHyperlinkListener(getOWLWorkspace());
		editorpaneIndex.addHyperlinkListener(hyperlinkListener);

		JScrollPane scrollpaneIndex = new JScrollPane(editorpaneIndex,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		setLayout(new BorderLayout());
		add(scrollpaneIndex);
		ACETextManager.addListener(aceTextManagerListener);
		addHierarchyListener(hierarchyListener);
	}

	@Override
	protected OWLObject updateView() {
		OWLEntity entity = getOWLWorkspace().getOWLSelectionModel().getSelectedEntity();
		if (isShowing() && entity != null && ACETextManager.isShow(entity)) {
			String indexEntry = ACETextManager.getActiveACEText().getIndexEntry(entity);
			if (indexEntry == null) {
				editorpaneIndex.setText(ACETextManager.wrapInHtml("<em>No entries</em>"));
			}
			else {
				editorpaneIndex.setText(ACETextManager.wrapInHtml(indexEntry));
			}
		}
		else if (isShowing()) {
			editorpaneIndex.setText(ACETextManager.wrapInHtml(""));
		}
		return entity;
	}


	@Override
	public void refreshComponent() {
		if (editorpaneIndex != null) {
			editorpaneIndex.setFont(owlRendererPreferences.getFont());
		}
	}
}