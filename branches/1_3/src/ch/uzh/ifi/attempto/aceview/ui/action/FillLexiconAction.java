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

import org.protege.editor.owl.model.OWLModelManager;
import org.protege.editor.owl.ui.action.ProtegeOWLAction;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import com.google.common.collect.Lists;

import ch.uzh.ifi.attempto.aceview.ACETextManager;
import ch.uzh.ifi.attempto.aceview.MorphAnnotation;
import ch.uzh.ifi.attempto.aceview.model.event.TextEventType;
import ch.uzh.ifi.attempto.aceview.util.OntologyUtils;

/**
 * <p>Creates ACE Lexicon annotations (sg, pl, vbg) for all entities in
 * the active ontology. Existing annotations are not overwritten.</p>
 * 
 * @author Kaarel Kaljurand
 * 
 * TODO list:
 * 
 * - Report statistics on how many lexicon entries were changed out of how many possible
 * - Deal with punned properties (e.g. 'capital of' should not cause the verb entry to be changed)
 * - BUG: entityCounter counts also owl:Thing and other entities that are not going to be annotated
 */
public class FillLexiconAction extends ProtegeOWLAction {

	public void dispose() throws Exception {
	}

	public void initialise() throws Exception {
	}

	public void actionPerformed(ActionEvent actionEvent) {
		OWLModelManager mm = ACETextManager.getOWLModelManager();
		OWLDataFactory df = mm.getOWLDataFactory();
		Set<OWLOntology> ontologies = mm.getActiveOntologies();
		OWLOntologyManager ontologyManager = mm.getOWLOntologyManager();
		int ontologyCounter = 0;
		int entityCounter = 0;
		int annotatedEntityCounter = 0;
		int annotationCounter = 0;

		List<AddAxiom> additions = Lists.newArrayList();

		for (OWLOntology ont : ontologies) {
			ontologyCounter++;
			for (OWLEntity entity : ont.getSignature()) {
				entityCounter++;
				String entityRendering = getOWLModelManager().getRendering(entity);
				Set<OWLAnnotationAssertionAxiom> entityAnnotationAxioms = MorphAnnotation.getAdditionalMorphAnnotations(df, ont, entity, entityRendering);
				int size = entityAnnotationAxioms.size();
				if (size > 0) {
					for (OWLAnnotationAssertionAxiom ax : entityAnnotationAxioms) {
						additions.add(new AddAxiom(ont, ax));
					}
					annotatedEntityCounter++;
					annotationCounter += size;
				}
			}
		}

		OntologyUtils.changeOntology(ontologyManager, additions);

		ACETextManager.fireEvent(TextEventType.ACELEXICON_CHANGED);
		String message = "Checked " + entityCounter + " entities in " + ontologyCounter + " active ontologies.";
		message += "\nAnnotated " + annotatedEntityCounter + " entities with " + annotationCounter + " annotations.";
		JOptionPane.showMessageDialog(null, message, "Fill Lexicon Action", JOptionPane.INFORMATION_MESSAGE);
	}
}