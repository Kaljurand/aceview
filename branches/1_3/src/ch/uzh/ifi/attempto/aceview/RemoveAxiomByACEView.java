package ch.uzh.ifi.attempto.aceview;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.RemoveAxiom;

public class RemoveAxiomByACEView extends RemoveAxiom {

	public RemoveAxiomByACEView(OWLOntology ont, OWLAxiom axiom) {
		super(ont, axiom);
	}
}