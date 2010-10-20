package ch.uzh.ifi.attempto.aceview.ui;

import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.model.util.OWLAxiomInstance;
import org.protege.editor.owl.ui.list.AbstractAnnotationsList;
import org.semanticweb.owlapi.model.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
/*
 * Copyright (C) 2007, University of Manchester
 *
 * Modifications to the initial code base are copyright of their
 * respective authors, or their employers as appropriate.  Authorship
 * of the modifications may be determined from the ChangeLog placed at
 * the end of this file.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.

 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.

 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

/**
 * Author: drummond<br>
 * http://www.cs.man.ac.uk/~drummond/<br><br>
 * <p/>
 * The University Of Manchester<br>
 * Bio Health Informatics Group<br>
 * Date: Jun 8, 2009<br><br>
 */
public class AxiomAnnotationsList extends AbstractAnnotationsList<OWLAxiomInstance> {

	private OWLAxiom newAxiom;


	public AxiomAnnotationsList(OWLEditorKit eKit) {
		super(eKit);
	}


	protected java.util.List<OWLOntologyChange> getAddChanges(OWLAnnotation annot) {
		List<OWLOntologyChange> changes = new ArrayList<OWLOntologyChange>();
		final OWLAxiom oldAxiom = getRoot().getAxiom();

		Set<OWLAnnotation> annotations = new HashSet<OWLAnnotation>(oldAxiom.getAnnotations());
		annotations.add(annot);

		// because for some reason the merge does not work
		newAxiom = oldAxiom.getAxiomWithoutAnnotations().getAnnotatedAxiom(annotations);

		final OWLOntology ont = getRoot().getOntology();
		changes.add(new RemoveAxiom(ont, oldAxiom));
		changes.add(new AddAxiom(ont, newAxiom));
		return changes;
	}


	protected List<OWLOntologyChange> getReplaceChanges(OWLAnnotation oldAnnotation, OWLAnnotation newAnnotation) {
		List<OWLOntologyChange> changes = new ArrayList<OWLOntologyChange>();
		final OWLAxiom ax = getRoot().getAxiom();
		final OWLOntology ont = getRoot().getOntology();
		Set<OWLAnnotation> annotations = new HashSet<OWLAnnotation>(ax.getAnnotations());
		annotations.remove(oldAnnotation);
		annotations.add(newAnnotation);

		newAxiom = ax.getAxiomWithoutAnnotations().getAnnotatedAxiom(annotations);

		changes.add(new RemoveAxiom(ont, ax));
		changes.add(new AddAxiom(ont, newAxiom));
		return changes;
	}


	protected List<OWLOntologyChange> getDeleteChanges(OWLAnnotation oldAnnotation) {
		List<OWLOntologyChange> changes = new ArrayList<OWLOntologyChange>();
		final OWLAxiom ax = getRoot().getAxiom();
		final OWLOntology ont = getRoot().getOntology();

		Set<OWLAnnotation> annotations = new HashSet<OWLAnnotation>(ax.getAnnotations());
		annotations.remove(oldAnnotation);

		newAxiom = ax.getAxiomWithoutAnnotations().getAnnotatedAxiom(annotations);

		changes.add(new RemoveAxiom(ont, ax));
		changes.add(new AddAxiom(ont, newAxiom));
		return changes;
	}


	protected void handleOntologyChanges(List<? extends OWLOntologyChange> changes) {

		// this is complicated by the fact that annotating an axiom produces a new axiom
		if (newAxiom != null){
			for (OWLOntologyChange change : changes){
				if (change instanceof RemoveAxiom){
					if (change.getAxiom().equals(getRoot().getAxiom())){
						// @@TODO should check that ontology contains the new axiom
						setRootObject(new OWLAxiomInstance(newAxiom, getRoot().getOntology()));
						newAxiom = null;
						return;
					}
				}
			}
		}
	}
}
