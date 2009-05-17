package ch.uzh.ifi.attempto.aceview.util;

import java.net.URI;

import org.semanticweb.owl.model.OWLAxiom;
import org.semanticweb.owl.model.OWLEntity;

public final class Showing {

	private static final URI ENTITY_contain = URI.create("http://attempto.ifi.uzh.ch/ace#contain");
	private static final URI ENTITY_Universe = URI.create("http://attempto.ifi.uzh.ch/ace#Universe");

	// BUG: deprecated
	private static final URI ENTITY_know = URI.create("http://attempto.ifi.uzh.ch/ace#know");
	private static final URI ENTITY_Superman = URI.create("http://attempto.ifi.uzh.ch/ace#Superman");


	private Showing() {}


	/**
	 * <p>Specifies entities which should be displayed as ACE content words.</p>
	 * 
	 * <p>Note: we do not show the morphological annotations of the following entities:
	 * <code>owl:Thing</code>,
	 * <code>owl:Nothing</code>,
	 * <code>ace:Universe</code>,
	 * <code>ace:contain</code>.</p>
	 * 
	 * TODO: What do to with anonymous individuals?
	 * 
	 * @param entity OWL entity
	 * @return <code>true</code> if entity should be shown
	 */
	public static boolean isShow(OWLEntity entity) {

		if (entity.isBuiltIn()) {
			return false;
		}

		if (entity.isOWLDataType()) {
			return false;
		}

		return (
				entity.isOWLClass() ||
				entity.isOWLObjectProperty() && !entity.getURI().equals(ENTITY_contain) && !entity.getURI().equals(ENTITY_know) ||
				entity.isOWLDataProperty() ||
				entity.isOWLIndividual() && !entity.getURI().equals(ENTITY_Universe) && !entity.getURI().equals(ENTITY_Superman)
		);
	}


	/**
	 * <p>Specifies axioms which should be "shown". For example we do not want to verbalize
	 * (entailed) axioms which contain "tricks" like using entities `contain' and `Universe'
	 * from the ace-namespace.</p>
	 * 
	 * @param axiom OWL axiom
	 * @return true if axiom should be "shown"
	 */
	public static boolean isShow(OWLAxiom axiom) {
		for (OWLEntity entity : axiom.getReferencedEntities()) {
			URI entityURI = entity.getURI();
			if (entityURI.equals(ENTITY_contain) || entityURI.equals(ENTITY_Universe)) {
				return false;
			}

			// BUG: deprecated
			if (entityURI.equals(ENTITY_know)) {
				return false;
			}
			if (entityURI.equals(ENTITY_Superman)) {
				return false;
			}
		}
		return true;
	}

}
