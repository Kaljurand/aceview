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

import java.util.Set;

import org.protege.editor.owl.model.selection.OWLSelectionModel;
import org.protege.editor.owl.model.selection.OWLSelectionModelAdapter;
import org.protege.editor.owl.model.selection.OWLSelectionModelListener;
import org.protege.editor.owl.ui.view.AbstractOWLViewComponent;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLObject;

import ch.uzh.ifi.attempto.aceview.ACESnippet;
import ch.uzh.ifi.attempto.aceview.ACEText;
import ch.uzh.ifi.attempto.aceview.ACETextManager;
import ch.uzh.ifi.attempto.aceview.model.event.ACEViewEvent;
import ch.uzh.ifi.attempto.aceview.model.event.ACEViewListener;
import ch.uzh.ifi.attempto.aceview.model.event.SnippetEventType;

/**
 * <p>This view component monitors the selection of ACE snippets.</p>
 * 
 * <p>Snippets can be selected in two ways:</p>
 * <ul>
 * <li>explicitly selecting a snippet, e.g. by clicking on it
 * in the ACE snippets table;</li>
 * <li>implicitly selecting a snippet, e.g. by clicking on an
 * OWL logical axiom in the standard Protege views.</li>
 * </ul>
 * 
 * <p>If an OWL logical axiom is selected, then the active ACE text is checked for
 * the corresponding snippet, which is then selected. If there are
 * no corresponding snippets, then nothing is done. If there are
 * more than one corresponding snippets, then the first one (in the iterator
 * order) is selected.</p>
 * 
 * @author Kaarel Kaljurand
 */
public abstract class AbstractACESnippetSelectionViewComponent extends AbstractOWLViewComponent {

	private final ACEViewListener<ACEViewEvent<SnippetEventType>> snippetListener = new ACEViewListener<ACEViewEvent<SnippetEventType>>() {
		public void handleChange(ACEViewEvent<SnippetEventType> event) {
			if (event.isType(SnippetEventType.SELECTED_SNIPPET_CHANGED)) {
				ACESnippet selectedSnippet = ACETextManager.getSelectedSnippet();
				displaySnippet(selectedSnippet);
			}
		}
	};


	private final OWLSelectionModelListener selListener = new OWLSelectionModelAdapter() {
		@Override
		public void selectionChanged() throws Exception {
			final OWLSelectionModel selModel = getOWLWorkspace().getOWLSelectionModel();
			OWLObject lastSelection = selModel.getSelectedObject();

			if (lastSelection instanceof OWLLogicalAxiom) {
				ACEText<OWLEntity, OWLLogicalAxiom> acetext = ACETextManager.getActiveACEText();
				Set<ACESnippet> snippets = acetext.getAxiomSnippets((OWLLogicalAxiom) lastSelection);

				// Shows the first corresponding snippet to the axiom selected in the Protege views.
				if (! snippets.isEmpty()) {
					displaySnippet(snippets.iterator().next());
				}
			}
		}
	};


	/**
	 * <p>Displays the selected snippet. The subclass decides
	 * how the displaying is done.</p>
	 * 
	 * @param snippet Selected ACE snippet
	 */
	protected abstract void displaySnippet(ACESnippet snippet);


	protected ACEViewListener<ACEViewEvent<SnippetEventType>> getACESnippetListener() {
		return snippetListener;
	}


	protected OWLSelectionModelListener getOWLSelectionModelListener() {
		return selListener;
	}
}