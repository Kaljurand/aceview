/*
 * This file is part of ACE View.
 * Copyright 2008-2011, Attempto Group, University of Zurich (see http://attempto.ifi.uzh.ch).
 *
 * ACE View is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 *
 * ACE View is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 * PURPOSE. See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with ACE View.
 * If not, see http://www.gnu.org/licenses/.
 */

package ch.uzh.ifi.attempto.aceview;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.coode.owlapi.owlxml.renderer.OWLXMLRenderer;
import org.protege.editor.core.ProtegeApplication;
import org.semanticweb.owlapi.io.OWLRendererException;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDataPropertyExpression;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLDisjointClassesAxiom;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChangeException;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyID;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;

import com.google.common.collect.Sets;

import ch.uzh.ifi.attempto.ace.ACESentence;
import ch.uzh.ifi.attempto.ace.ACESplitterFromCsv;
import ch.uzh.ifi.attempto.ace.ACEToken;
import ch.uzh.ifi.attempto.ace.EntryType;
import ch.uzh.ifi.attempto.ace.FieldType;
import ch.uzh.ifi.attempto.aceview.util.OntologyUtils;
import ch.uzh.ifi.attempto.owl.OutputType;
import ch.uzh.ifi.attempto.owl.VerbalizerWebservice;

public class AxiomVerbalizer {

	private static final Logger logger = Logger.getLogger(AxiomVerbalizer.class);
	private final VerbalizerWebservice verbalizerWS;

	public AxiomVerbalizer(String verbalizerWSURL) {
		this.verbalizerWS = new VerbalizerWebservice(verbalizerWSURL);
	}

	public AxiomVerbalizer(VerbalizerWebservice verbalizerWS) {
		this.verbalizerWS = verbalizerWS;
	}


	/**
	 * <p>Verbalizes a single logical OWL axiom and constructs
	 * a new ACE snippet containing the verbalization and the axiom.
	 * The following can happen:</p>
	 * 
	 * <ol>
	 * <li>Axiom is not supported: use Protege rendering (MOS, or if it fails then toString()</li>
	 * <li>Axiom is simple: verbalize it in Java (90% of the cases in a typical ontology)</li>
	 * <li>Axiom is complex: verbalize it using the OWL Verbalizer webservice (or use MOS, or toString() is it fails)</li>
	 * </ol>
	 * 
	 * @param axiom OWL logical axiom to be verbalized
	 * @param ont Ontology that annotates the entities of the given axiom
	 * @return ACE snippet containing the verbalization of the given axiom
	 * @throws OWLRendererException 
	 * @throws OWLOntologyChangeException 
	 * @throws OWLOntologyCreationException 
	 */
	public ACESnippet verbalizeAxiom(OWLLogicalAxiom axiom, OWLOntology ont) throws OWLRendererException, OWLOntologyCreationException, OWLOntologyChangeException {

		OWLOntologyID iri = ont.getOntologyID();

		OWLLogicalAxiom axiomWithoutAnnotations = (OWLLogicalAxiom) axiom.getAxiomWithoutAnnotations();
		logger.info("Verbalizing axiom: " + axiomWithoutAnnotations);

		// TODO: Currently the verbalization of certain axioms is not supported.
		// We just return the Protege rendering.
		// Checking for unsupported axioms here increases processing speed.
		if (OntologyUtils.verbalizationNotSupported(axiomWithoutAnnotations)) {
			String verbalization = OntologyUtils.getRendering(ACETextManager.getOWLModelManager(), axiomWithoutAnnotations);
			logger.info("Unsupported axiom verbalized as: " + verbalization);
			return new ACESnippetImpl(iri, "", axiomWithoutAnnotations, verbalization);
		}


		// BUG: this is a bit of a hack for performance reasons.
		// We verbalize simple axioms without having to use
		// the verbalizer webservice. It seems that about 50% of
		// the axioms in real-world ontologies are simple SubClassOf-axioms,
		// so it really pays off performancewise to verbalize them directly in Java.
		List<ACESentence> verbalization = verbalizeSimpleAxiom(axiomWithoutAnnotations);

		if (verbalization != null) {
			logger.info("Simple axiom verbalized as: " + verbalization);
			return new ACESnippetImpl(iri, verbalization, axiomWithoutAnnotations);
		}

		// If the axiom was not simple, then we verbalize it using the OWL verbalizer webservice.

		try {
			verbalization = verbalizeWithWS(axiomWithoutAnnotations);
		} catch (Exception e) {
			logger.info("OWL verbalizer error: " + e.getMessage());
			// Note that we can only log throwables using the central Protege error log.
			// Because the stack trace takes a lot of space in the error message,
			// and because it is not so interesting in this case, we just set it to empty.
			e.setStackTrace(new StackTraceElement[0]);
			ProtegeApplication.getErrorLog().logError(e);

			// Other options for displaying an error message
			//JOptionPane.showMessageDialog(null, "OWL verbalizer error:\n" + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			//ErrorLogPanel.showErrorDialog(e);
		}

		if (verbalization == null || verbalization.isEmpty()) {
			String altRendering = OntologyUtils.getRendering(ACETextManager.getOWLModelManager(), axiomWithoutAnnotations);
			logger.info("Complex axiom failed to be verbalized, using Protege rendering: " + altRendering);
			return new ACESnippetImpl(iri, "", axiomWithoutAnnotations, altRendering);
		}
		logger.info("Complex axiom verbalized as: " + verbalization);
		return new ACESnippetImpl(iri, verbalization, axiomWithoutAnnotations);
	}


	/**
	 * <p>Verbalizes an OWL ontology as an ACE text (a string).
	 * Uses the OWL->ACE verbalizer webservice.</p>
	 * 
	 * @param manager OWL ontology manager
	 * @param ont OWL ontology to be verbalized
	 * @return Verbalization of the input ontology
	 * @throws OWLRendererException 
	 */
	private String verbalizeOntology(OWLOntologyManager manager, OWLOntology ont) throws OWLRendererException {
		StringWriter sw = new StringWriter();
		OWLXMLRenderer renderer = new OWLXMLRenderer(manager);
		renderer.render(ont, sw);
		return verbalizerWS.call(sw.toString(), OutputType.CSV);
	}


	/**
	 * <p>Verbalizes the given OWL axiom using the Verbalizer webservice.
	 * Returns a list of sentences, i.e. a single paragraph. The verbalization
	 * of a single axiom cannot consist of more than one paragraph.</p>
	 * 
	 * @param axiom OWL axiom
	 * @return Verbalization of the given axiom
	 * @throws OWLOntologyCreationException 
	 * @throws OWLRendererException 
	 */
	private List<ACESentence> verbalizeWithWS(OWLLogicalAxiom axiom) throws OWLOntologyCreationException, OWLRendererException {
		OWLOntologyManager ontologyManager = ACETextManager.createOWLOntologyManager();
		OWLOntology ontology = ontologyManager.createOntology(Sets.newHashSet((OWLAxiom) axiom));
		String verbalization = verbalizeOntology(ontologyManager, ontology);
		return ACESplitterFromCsv.getSentences(verbalization);
	}


	/**
	 * <p>Verbalizes an <code>OWLLogicalAxiom</code> given that it is
	 * a structurally simple axiom,
	 * specifically of one of the following axiom types</p>
	 * <ul>
	 * <li>SubClassOf with named classes</li>,
	 * <li>ClassAssertion with a named class and named individual</li>,
	 * <li>ObjectPropertyAssertion with named individuals and a named object property</li>
	 * <li>OWLDisjointClasses with exactly two named classes</li>
	 * </ul>
	 * 
	 * <p>Otherwise returns <code>null</code>.</p>
	 * 
	 * <p>The surface forms are decided by the annotation axioms in the ontology.</p>
	 * 
	 * @param ax an instance of <code>OWLLogicalAxiom</code> to be verbalized
	 * @return sentence corresponding to the axiom or <code>null</code>
	 */
	private static List<ACESentence> verbalizeSimpleAxiom(OWLLogicalAxiom ax) {
		List<ACESentence> sentences = new ArrayList<ACESentence>();
		List<ACEToken> tokens = new ArrayList<ACEToken>();

		if (ax instanceof OWLSubClassOfAxiom) {
			OWLSubClassOfAxiom subClassAxiom = (OWLSubClassOfAxiom) ax;
			OWLClassExpression subClass = subClassAxiom.getSubClass();
			OWLClassExpression superClass = subClassAxiom.getSuperClass();
			// There is a higher chance that superClass.isAnonymous(), that is why we test it first.
			if (isAnonymousOrNothing(superClass)) {
				return null;
			}
			if (isAnonymousOrNothing(subClass)) {
				return null;
			}

			return getSimpleClassRelationVerbalization((OWLClass) subClass, (OWLClass) superClass, "Every");
		}
		else if (ax instanceof OWLClassAssertionAxiom) {
			OWLClassAssertionAxiom classAssertionAxiom = (OWLClassAssertionAxiom) ax;
			OWLClassExpression desc = classAssertionAxiom.getClassExpression();
			OWLIndividual ind = classAssertionAxiom.getIndividual();

			if (isAnonymousOrNothing(desc) || isAnonymous(ind)) {
				return null;
			}

			OWLNamedIndividual namedIndividual = ind.asOWLNamedIndividual();

			tokens.add(ACEToken.newToken(namedIndividual.getIRI(), EntryType.PN, FieldType.SG));
			tokens.add(ACEToken.newToken("is"));
			if (desc.isOWLThing()) {
				tokens.add(ACEToken.newToken("something"));
			} else {
				tokens.add(ACEToken.newToken("a"));
				tokens.add(ACEToken.newToken(((OWLClass) desc).getIRI(), EntryType.CN, FieldType.SG));
			}
			tokens.add(ACEToken.DOT);
			sentences.add(new ACESentence(tokens));
			return sentences;

		}
		else if (ax instanceof OWLObjectPropertyAssertionAxiom) {
			OWLObjectPropertyAssertionAxiom opAssertionAxiom = (OWLObjectPropertyAssertionAxiom) ax;
			OWLObjectPropertyExpression opExpression = opAssertionAxiom.getProperty();
			OWLIndividual subject = opAssertionAxiom.getSubject();
			OWLIndividual object = opAssertionAxiom.getObject();

			if (opExpression.isAnonymous() || isAnonymous(subject) || isAnonymous(object)) {
				return null;
			}

			tokens.add(ACEToken.newToken(subject.asOWLNamedIndividual().getIRI(), EntryType.PN, FieldType.SG));
			tokens.add(ACEToken.newToken(opExpression.asOWLObjectProperty().getIRI(), EntryType.TV, FieldType.SG));
			tokens.add(ACEToken.newToken(object.asOWLNamedIndividual().getIRI(), EntryType.PN, FieldType.SG));
			tokens.add(ACEToken.DOT);
			sentences.add(new ACESentence(tokens));
			return sentences;
		}
		else if (ax instanceof OWLDataPropertyAssertionAxiom) {
			OWLDataPropertyAssertionAxiom dpAssertionAxiom = (OWLDataPropertyAssertionAxiom) ax;

			OWLDataPropertyExpression dpExpression = dpAssertionAxiom.getProperty();
			if (dpExpression.isAnonymous()) return null;

			OWLIndividual subject = dpAssertionAxiom.getSubject();
			if (isAnonymous(subject)) return null;

			// John's temperature is 36.5.
			// TODO: James' temperature is 36.5. (without the "s")
			tokens.add(ACEToken.newToken(subject.asOWLNamedIndividual().getIRI(), EntryType.PN, FieldType.SG));
			tokens.add(ACEToken.newSymbol('\''));
			tokens.add(ACEToken.newToken("s"));
			// TODO: think about the morph type for data properties
			tokens.add(ACEToken.newToken(dpExpression.asOWLDataProperty().getIRI(), EntryType.TV, FieldType.PL));
			tokens.add(ACEToken.newToken("is"));
			tokens.add(createTokenFromLiteral(dpAssertionAxiom.getObject()));
			tokens.add(ACEToken.DOT);
			sentences.add(new ACESentence(tokens));
			return sentences;

		}
		else if (ax instanceof OWLDisjointClassesAxiom) {
			OWLDisjointClassesAxiom disjointClassesAxiom = (OWLDisjointClassesAxiom) ax;
			Set<OWLClassExpression> descriptions = disjointClassesAxiom.getClassExpressions();

			if (descriptions.size() != 2) {
				return null;
			}

			Iterator<OWLClassExpression> iterator = descriptions.iterator();

			OWLClassExpression desc1 = iterator.next();
			OWLClassExpression desc2 = iterator.next();

			if (isAnonymousOrNothing(desc1)) {
				return null;
			}
			if (isAnonymousOrNothing(desc2)) {
				return null;
			}

			return getSimpleClassRelationVerbalization((OWLClass) desc1, (OWLClass) desc2, "No");
		}
		return null;
	}


	private static ACEToken createTokenFromLiteral(OWLLiteral literal) {
		OWLDatatype datatype = literal.getDatatype();
		if (datatype.isString()) {
			return ACEToken.newQuotedString(literal.getLiteral());
		} else if (datatype.isDouble() || datatype.isInteger() || datatype.isFloat()) {
			return ACEToken.newNumber(literal.getLiteral());
		}
		// BUG: we accept all types of data here (although ACE only supports numbers and strings)
		return ACEToken.newToken(literal.getLiteral());
	}


	private static boolean isAnonymousOrNothing(OWLClassExpression desc) {
		return (desc.isAnonymous() || desc.isOWLNothing());
	}


	private static boolean isAnonymous(OWLIndividual ind) {
		return (ind.isAnonymous());
	}


	private static List<ACESentence> getSimpleClassRelationVerbalization(OWLClass class1, OWLClass class2, String prefix) {
		List<ACESentence> sentences = new ArrayList<ACESentence>();
		List<ACEToken> tokens = new ArrayList<ACEToken>();

		if (class1.isOWLThing()) {
			tokens.add(ACEToken.newToken(prefix + "thing"));
		} else {
			tokens.add(ACEToken.newToken(prefix));
			tokens.add(ACEToken.newToken(class1.getIRI(), EntryType.CN, FieldType.SG));
		}

		tokens.add(ACEToken.newToken("is"));

		if (class2.isOWLThing()) {
			tokens.add(ACEToken.newToken("something"));
		} else {
			tokens.add(ACEToken.newToken("a"));
			tokens.add(ACEToken.newToken(class2.getIRI(), EntryType.CN, FieldType.SG));
		}
		tokens.add(ACEToken.DOT);

		sentences.add(new ACESentence(tokens));
		return sentences;
	}
}