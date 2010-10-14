package ch.uzh.ifi.attempto.aceview.util;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.coode.owlapi.manchesterowlsyntax.ManchesterOWLSyntaxEditorParser;
import org.semanticweb.owlapi.expression.OWLEntityChecker;
import org.semanticweb.owlapi.expression.ParserException;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAnnotationSubject;
import org.semanticweb.owlapi.model.OWLAnnotationValue;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLAxiomChange;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChangeException;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import com.google.common.collect.Sets;

/**
 * 
 * This class should not import any Protege classes.
 * 
 * @author Kaarel Kaljurand
 */
public final class OntologyUtils {

	private OntologyUtils() {}

	/**
	 * TODO: We currently catch the change exception here.
	 * Find out what can the exception actually inform us about,
	 * and how can we recover from that. Otherwise we could
	 * also raise a runtime exception here.
	 * 
	 * @param mngr OWL ontology manager
	 * @param changes List of OWL axiom changes
	 */
	public static void changeOntology(OWLOntologyManager mngr, List<? extends OWLAxiomChange> changes) {
		try {
			mngr.applyChanges(changes);
		} catch (OWLOntologyChangeException e) {
			e.printStackTrace();
		}
	}


	/**
	 * <p>Creates a new ontology with a dummy URI.</p>
	 * 
	 * TODO: OWL-API can currently invent URIs but requires
	 * the set of initial axioms to be provided.
	 * The interface specifies OWLOntologyChangeException which cannot happen
	 * in case of empty set of axioms, but which we need to catch anyway.
	 * 
	 * @param mngr OWL ontology manager
	 * @return OWL ontology
	 * @throws OWLOntologyCreationException
	 */
	public static OWLOntology createOntology(OWLOntologyManager mngr) throws OWLOntologyCreationException {
		try {
			return mngr.createOntology(Collections.<OWLAxiom>emptySet());
		} catch (OWLOntologyChangeException e) {
			return null; // Cannot happen
		}
	}



	/**
	 * @param ontology
	 * @param entity
	 * @return
	 */
	public static Set<IRI> getAnnotationIRIs(OWLOntology ontology, OWLEntity entity) {
		Set<IRI> annotationIRIs = Sets.newHashSet();
		for (OWLAnnotation annotation : entity.getAnnotations(ontology)) {
			annotationIRIs.add(annotation.getProperty().getIRI());
		}
		return annotationIRIs;
	}


	/**
	 * <p>Parses the given string with the Manchester OWL Syntax parser and
	 * returns the corresponding OWL logical axiom or throws an exception if
	 * parsing failed. The string is assumed to correspond to an OWL axiom,
	 * i.e. we use only the following methods to obtain the result.</p>
	 * 
	 * <ul>
	 * <li>parseClassAxiom()</li>
	 * <li>TODO: parsePropertyChainSubPropertyAxiom()</li>
	 * <li>TODO: parseObjectPropertyAxiom()</li>
	 * </ul>
	 * 
	 * <p>TODO: would be nice to call parseAxiom() to be able to
	 * parse all types of axioms, but this doesn't seem to work.</p>
	 * 
	 * <p>Note: this method depends only on OWL-API classes.</p>
	 * 
	 * @param df
	 * @param ec
	 * @param base
	 * @param str String that possibly represents an OWL axiom in Manchester OWL Syntax
	 * @return OWL logical axiom that corresponds to the given string.
	 * @throws ParserException 
	 */
	public static OWLLogicalAxiom parseWithMosParser(OWLDataFactory df, OWLEntityChecker ec, String base, String str) throws ParserException {
		ManchesterOWLSyntaxEditorParser parser = new ManchesterOWLSyntaxEditorParser(df, str);
		// TODO: what does this do?
		parser.setOWLEntityChecker(ec);
		// TODO: what does this do?
		parser.setBase(base);
		// TODO: BUG: parseAxiom() doesn't seem to work
		OWLAxiom axiom = parser.parseClassAxiom();
		if (axiom instanceof OWLLogicalAxiom) {
			return (OWLLogicalAxiom) axiom;
		}
		return null;
	}


	public static OWLAnnotationAssertionAxiom createEntityAnnotationAxiom(OWLDataFactory df, IRI iri, OWLEntity entity, String lexem) {

		// e.g. morph#TV_pl
		OWLAnnotationProperty property = df.getOWLAnnotationProperty(iri);

		// IRI of the entity, e.g. http://www/man
		OWLAnnotationSubject subject = entity.getIRI();

		// e.g. "men"
		OWLAnnotationValue value = df.getOWLTypedLiteral(lexem);

		// e.g. morph#pl -> "men"
		// OWLAnnotation ann = df.getOWLAnnotation(property, value);

		return df.getOWLAnnotationAssertionAxiom(property, subject, value);
	}


	public static Set<OWLAnnotationAssertionAxiom> entityToAnnotations(OWLEntity entity, OWLOntology ont) {
		Set<OWLAnnotationAssertionAxiom> set;

		set = entity.getAnnotationAssertionAxioms(ont);

		// We could also filter out only those annotations
		// that go along the ace_lexicon-properties.
		// entity.getAnnotations(ont, property);

		return set;
	}
}