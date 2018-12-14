package ch.uzh.ifi.attempto.aceview;

import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;

public class AddAxiomByACEView extends AddAxiom {

	public AddAxiomByACEView(OWLOntology ont, OWLAxiom axiom) {
		super(ont, axiom);
	}
}