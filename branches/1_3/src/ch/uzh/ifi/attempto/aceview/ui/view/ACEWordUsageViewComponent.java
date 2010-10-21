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

import org.protege.editor.owl.ui.renderer.OWLEntityRenderer;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLOntology;

import ch.uzh.ifi.attempto.aceview.ACETextManager;
import ch.uzh.ifi.attempto.aceview.WordsHyperlinkListener;
import ch.uzh.ifi.attempto.aceview.model.event.ACETextChangeEvent;
import ch.uzh.ifi.attempto.aceview.model.event.ACETextManagerListener;
import ch.uzh.ifi.attempto.aceview.util.Showing;

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

		if (isShowing()) {
			if (entity != null && Showing.isShow(entity)) {
				String entityAnnotations = formatEntityAnnotations(entity, getOWLModelManager().getActiveOntology());
				String indexEntry = ACETextManager.getActiveACEText().getIndexEntry(entity);
				if (indexEntry == null) {
					indexEntry = "<em>No entries</em>";
				}
				editorpaneIndex.setText(ACETextManager.wrapInHtml(entityAnnotations + indexEntry));
			}
			else {
				editorpaneIndex.setText(ACETextManager.wrapInHtml(""));
			}
		}
		return entity;
	}


	@Override
	public void refreshComponent() {
		if (editorpaneIndex != null) {
			editorpaneIndex.setFont(owlRendererPreferences.getFont());
		}
	}


	private String formatEntityAnnotations(OWLEntity entity, OWLOntology ont) {
		StringBuilder str = new StringBuilder();
		if (entity != null && ont != null) {
			str.append("<table>");
			OWLEntityRenderer entityRenderer = getOWLModelManager().getOWLEntityRenderer();

			str.append("<tr><td>IRI</td><td>");
			str.append(entity.getIRI());
			str.append("</td></tr>");
			str.append("<tr><td>Short form</td><td>");
			str.append(entityRenderer.getShortForm(entity));
			str.append("</td></tr>");
			str.append("<tr><td>Rendering</td><td>");
			str.append(entityRenderer.render(entity));
			str.append("</td></tr>");

			for (OWLAnnotation ann : entity.getAnnotations(ont)) {
				str.append("<tr><td>");
				str.append(ann.getProperty().getIRI().getFragment());
				str.append("</td><td>");
				str.append(ann.getValue());
				str.append("</td></tr>");
			}
			str.append("</table>");
		}
		return str.toString();
	}
}