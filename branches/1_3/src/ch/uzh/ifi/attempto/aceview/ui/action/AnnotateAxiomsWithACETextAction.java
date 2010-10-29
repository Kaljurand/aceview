/*
 * This file is part of ACE View.
 * Copyright 2008-2010, Attempto Group, University of Zurich (see http://attempto.ifi.uzh.ch).
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

package ch.uzh.ifi.attempto.aceview.ui.action;

import java.awt.event.ActionEvent;
import java.util.List;
import java.util.Set;

import javax.swing.JOptionPane;

import org.apache.log4j.Logger;
import org.protege.editor.owl.ui.action.ProtegeOWLAction;
import org.semanticweb.owlapi.model.OWLAxiomChange;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLOntology;

import com.google.common.collect.Lists;

import ch.uzh.ifi.attempto.aceview.ACESnippet;
import ch.uzh.ifi.attempto.aceview.ACEText;
import ch.uzh.ifi.attempto.aceview.ACETextManager;
import ch.uzh.ifi.attempto.aceview.AddAxiomByACEView;
import ch.uzh.ifi.attempto.aceview.RemoveAxiomByACEView;
import ch.uzh.ifi.attempto.aceview.util.OntologyUtils;

/**
 * <p>Adds the ACE text annotation to every logical axiom in the ontology.
 * It is expected that this action is applied to the ontology right before
 * saving the ontology.</p>
 * 
 * <p>TODO: This action is experimental, things to be specified/configured:</p>
 * <ul>
 * <li>is it applied to all active ontologies?</li>
 * <li>is it applied to the import closure?</li>
 * <li>from were is the ACE text taken?</li>
 * <li>what if the axiom has several verbalizations?</li>
 * <li>what if the axiom has already existing ACE text annotations on it?</li>
 * </ul>
 * 
 * @author Kaarel Kaljurand
 */
public class AnnotateAxiomsWithACETextAction extends ProtegeOWLAction {

	private static final Logger logger = Logger.getLogger(AnnotateAxiomsWithACETextAction.class);

	private static final String ACTION_TITLE = "Annotate axioms with ACE text";


	public void dispose() throws Exception {
	}

	public void initialise() throws Exception {
	}

	public void actionPerformed(ActionEvent actionEvent) {
		OWLDataFactory df = getOWLModelManager().getOWLDataFactory();
		List<OWLAxiomChange> changes = Lists.newArrayList();
		int ontologyCount = 0;
		int logicalAxiomCount = 0;
		Set<OWLOntology> ontologies = getOWLModelManager().getActiveOntologies();
		for (OWLOntology ont : ontologies) {
			logger.info("Annotating ontology: " + ont);
			ontologyCount ++;
			ACEText<?, OWLLogicalAxiom> acetext = ACETextManager.getACEText(ont.getOntologyID());
			for (OWLLogicalAxiom axiom : ont.getLogicalAxioms()) {
				logger.info("Annotating axiom: " + axiom);
				Set<ACESnippet> snippets = acetext.getAxiomSnippets(axiom);
				if (snippets.size() == 1) {
					logicalAxiomCount ++;
					ACESnippet snippet = snippets.iterator().next();
					logger.info("Annotating with: " + snippet);
					OWLLogicalAxiom newAxiom = ACETextManager.annotateAxiomWithSnippet(df, axiom, snippet);
					changes.add(new RemoveAxiomByACEView(ont, axiom));
					changes.add(new AddAxiomByACEView(ont, newAxiom));
				}
				else {
					logger.info("Cannot annotate with: " + snippets);
				}
			}
		}
		OntologyUtils.changeOntology(getOWLModelManager().getOWLOntologyManager(), changes);
		showMessage(JOptionPane.INFORMATION_MESSAGE, "Annotated " + logicalAxiomCount + " axioms in " + ontologyCount + " ontologies.");
	}


	/**
	 * Example: showMessage(JOptionPane.INFORMATION_MESSAGE, "Message text");
	 * 
	 * @param messageType
	 * @param str
	 */
	private void showMessage(int messageType, String str) {
		JOptionPane.showMessageDialog(null, str, ACTION_TITLE, messageType);
	}
}