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
import org.semanticweb.owlapi.model.OWLAnnotationValue;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLAxiomChange;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataPropertyAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChangeException;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.SWRLRule;

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
	 * @return Set of annotation IRIs for the given entity in the given ontology
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


	/**
	 * <p>Creates an annotation assertion axiom, e.g.
	 * (http://morph#CN_pl, http://www.net/man, "men").</p>
	 * 
	 * @param df OWLDataFactory
	 * @param propertyIRI IRI of the annotation property (e.g. morph#CN_pl)
	 * @param subjectIRI (e.g. IRI of the entity, e.g. http://www.net/man)
	 * @param valueAsString Annotation value as string (e.g. men)
	 * @return OWLAnnotationAssertionAxiom
	 */
	public static OWLAnnotationAssertionAxiom createIRIAnnotationAxiom(OWLDataFactory df, IRI propertyIRI, IRI subjectIRI, String valueAsString) {

		// e.g. morph#CN_pl
		OWLAnnotationProperty property = df.getOWLAnnotationProperty(propertyIRI);

		// e.g. "men"
		// TODO: We could also set the lang-attribute to "en", but
		// I'm not sure it's that useful.
		OWLAnnotationValue value = df.getOWLLiteral(valueAsString, "");

		return df.getOWLAnnotationAssertionAxiom(property, subjectIRI, value);
	}


	/**
	 * <p>List of axiom types that the OWL verbalizer currently
	 * cannot handle, namely:</p>
	 * 
	 * <ul>
	 * <li><code>OWLDataPropertyAxiom</code>: all types of data property
	 * axioms, not including <code>OWLDataPropertyAssertionAxiom</code></li>
	 * <li><code>SWRLRule</code>: SWRL rules</li>
	 * </ul>
	 * 
	 * @param axiom OWL logical axiom
	 * @return True is axiom cannot be verbalized
	 */
	public static boolean verbalizationNotSupported(OWLLogicalAxiom axiom) {
		if (axiom instanceof SWRLRule) {
			return true;
		}
		else if (axiom instanceof OWLDataPropertyAxiom) {
			return true;
		}
		return false;
	}
}