package ch.uzh.ifi.attempto.aceview.util;

import java.net.URI;

import org.semanticweb.owl.model.OWLAxiom;
import org.semanticweb.owl.model.OWLEntity;

public class Showing {

	private static final URI ENTITY_know = URI.create("http://attempto.ifi.uzh.ch/ace#know");
	private static final URI ENTITY_Superman = URI.create("http://attempto.ifi.uzh.ch/ace#Superman");


	private Showing() {}


	/**
	 * <p>Specifies entities which should be displayed as ACE content words.</p>
	 * 
	 * <p>Note: we do not show the morph annotations of the following entities:
	 * owl:Thing, owl:Nothing, ace:Superman, ace:know.</p>
	 * 
	 * TODO: What do to with anonymous individuals?
	 * 
	 * @param entity OWL entity
	 * @return <code>true</code> if entity should be shown
	 */
	public static boolean isShow(OWLEntity entity) {

		// isClass && !entity.asOWLClass().isOWLThing() && !entity.asOWLClass().isOWLNothing() ||
		if (entity.isBuiltIn()) {
			return false;
		}

		if (entity.isOWLDataType()) {
			return false;
		}

		return (
				entity.isOWLClass() ||
				entity.isOWLObjectProperty() && !entity.getURI().equals(ENTITY_know) ||
				entity.isOWLDataProperty() ||
				entity.isOWLIndividual() && !entity.getURI().equals(ENTITY_Superman)
		);
	}


	/**
	 * <p>Specifies axioms which should be "shown". For example we do not want to verbalize
	 * (entailed) axioms which contain "tricks" like using entities `know' and `Superman'.</p>
	 * 
	 * @param axiom OWL axiom
	 * @return true if axiom should be "shown"
	 */
	public static boolean isShow(OWLAxiom axiom) {
		for (OWLEntity entity : axiom.getReferencedEntities()) {
			if (entity.getURI().equals(ENTITY_know)) {
				return false;
			}
			if (entity.getURI().equals(ENTITY_Superman)) {
				return false;
			}
		}
		return true;
	}

}
