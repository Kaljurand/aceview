package ch.uzh.ifi.attempto.aceview;

import org.semanticweb.owl.model.OWLAxiom;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.RemoveAxiom;

public class RemoveAxiomByACEView extends RemoveAxiom {

	public RemoveAxiomByACEView(OWLOntology ont, OWLAxiom axiom) {
		super(ont, axiom);
	}
}
