package ch.uzh.ifi.attempto.aceview;

import org.semanticweb.owl.model.AddAxiom;
import org.semanticweb.owl.model.OWLAxiom;
import org.semanticweb.owl.model.OWLOntology;

public class AddAxiomByACEView extends AddAxiom {

	public AddAxiomByACEView(OWLOntology ont, OWLAxiom axiom) {
		super(ont, axiom);
	}
}
