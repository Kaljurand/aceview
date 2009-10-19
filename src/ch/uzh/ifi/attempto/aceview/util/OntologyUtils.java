package ch.uzh.ifi.attempto.aceview.util;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.coode.manchesterowlsyntax.ManchesterOWLSyntaxEditorParser;
import org.protege.editor.owl.model.OWLModelManager;
import org.protege.editor.owl.model.classexpression.OWLExpressionParserException;
import org.protege.editor.owl.model.parser.ParserUtil;
import org.protege.editor.owl.model.parser.ProtegeOWLEntityChecker;
import org.semanticweb.owlapi.expression.ParserException;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLAxiomAnnotationAxiom;
import org.semanticweb.owlapi.model.OWLAxiomChange;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChangeException;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import com.google.common.collect.Sets;

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


	public static Set<URI> getAnnotationURIs(OWLOntology ontology, OWLEntity entity) {
		Set<URI> annotationURIs = Sets.newHashSet();
		for (OWLAnnotation annotation : entity.getAnnotations(ontology)) {
			annotationURIs.add(annotation.getAnnotationURI());
		}
		return annotationURIs;
	}


	public static OWLAxiomAnnotationAxiom createAxiomAnnotation(OWLDataFactory df, OWLAxiom axiom, URI uri, String str) {
		OWLAnnotation ann = df.getOWLConstantAnnotation(uri, df.getOWLUntypedConstant(str));
		return df.getOWLAxiomAnnotationAxiom(axiom, ann);
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
	 * @param str String that possibly represents an OWL axiom in Manchester OWL Syntax
	 * @return OWL logical axiom that corresponds to the given string.
	 * @throws OWLExpressionParserException 
	 */
	public static OWLLogicalAxiom parseWithManchesterSyntaxParser(OWLModelManager mngr, URI uri, String str) throws OWLExpressionParserException {
		ManchesterOWLSyntaxEditorParser parser = new ManchesterOWLSyntaxEditorParser(mngr.getOWLDataFactory(), str);
		parser.setOWLEntityChecker(new ProtegeOWLEntityChecker(mngr));
		parser.setBase(uri.toString());
		try {
			OWLAxiom axiom = parser.parseClassAxiom();
			if (axiom instanceof OWLLogicalAxiom) {
				return (OWLLogicalAxiom) axiom;
			}
			return null;
		}
		catch (ParserException e) {
			throw ParserUtil.convertException(e);
		}
	}
}