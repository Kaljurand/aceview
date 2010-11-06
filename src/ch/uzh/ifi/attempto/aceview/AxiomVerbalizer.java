/*
 * This file is part of ACE View.
 * Copyright 2008-2010, Attempto Group, University of Zurich (see http://attempto.ifi.uzh.ch).
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
import java.util.Iterator;
import java.util.Set;

import javax.swing.JOptionPane;

import org.apache.log4j.Logger;
import org.coode.owlapi.owlxml.renderer.OWLXMLRenderer;
import org.semanticweb.owlapi.io.OWLRendererException;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationValue;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDataPropertyExpression;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLDisjointClassesAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
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
import org.semanticweb.owlapi.model.SWRLRule;

import com.google.common.collect.Sets;

import ch.uzh.ifi.attempto.aceview.lexicon.EntryType;
import ch.uzh.ifi.attempto.aceview.lexicon.FieldType;
import ch.uzh.ifi.attempto.aceview.lexicon.LexiconUtils;
import ch.uzh.ifi.attempto.aceview.lexicon.MorphType;
import ch.uzh.ifi.attempto.aceview.util.OntologyUtils;
import ch.uzh.ifi.attempto.ape.ACEUtils;
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
	 * a new ACE snippet containing the verbalization and the axiom.</p>
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

		// TODO: Currently the verbalization of certain axioms is not supported.
		// We just return the Protege rendering.
		// Checking for unsupported axioms here increases processing speed.
		if (OntologyUtils.verbalizationNotSupported(axiom)) {
			OWLLogicalAxiom annotationlessAxiom = (OWLLogicalAxiom) axiom.getAxiomWithoutAnnotations();
			return new ACESnippetImpl(iri, "", axiom, getAlternativeRendering(annotationlessAxiom));
		}


		// BUG: this is a bit of a hack for performance reasons.
		// We verbalize simple axioms without having to use
		// the verbalizer webservice. It seems that about 50% of
		// the axioms in real-world ontologies are simple SubClassOf-axioms,
		// so it really pays off performancewise to verbalize them directly in Java.
		String verbalization = verbalizeSimpleAxiom(axiom, ont);

		if (verbalization != null) {
			logger.info("Simple axiom verbalized: " + verbalization);
			return new ACESnippetImpl(iri, verbalization, axiom);
		}

		// If the axiom was not simple, then we verbalize it using the
		// OWL verbalizer webservice. We first remove the axiom annotation
		// from the axiom because the verbalizer does not need it
		// (and currently fails to ignore it as well).
		OWLLogicalAxiom annotationlessAxiom = (OWLLogicalAxiom) axiom.getAxiomWithoutAnnotations();
		logger.info("Using OWL Verbalizer WS to verbalize: " + annotationlessAxiom);

		try {
			verbalization = verbalizeWithWS(ont, annotationlessAxiom);
		}
		catch (Exception e) {
			JOptionPane.showMessageDialog(null, "OWL verbalizer error:\n" + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
		}

		if (verbalization == null) {
			return new ACESnippetImpl(iri, "", axiom, getAlternativeRendering(annotationlessAxiom));
		}

		return new ACESnippetImpl(iri, verbalization, axiom);
	}


	private String getAlternativeRendering(OWLLogicalAxiom axiom) {
		logger.info("Axiom is not verbalized, using Protege rendering.");
		String rendering = ACETextManager.getOWLModelManager().getRendering(axiom);
		if (rendering == null) {
			rendering = axiom.toString();
		}
		return rendering;
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
		//logger.info("TO BE VERBALIZED: " + sw.toString());
		return verbalizerWS.call(sw.toString());
	}

	/**
	 * <p>Verbalizes the OWL axiom using the Verbalizer webservice.</p>
	 * 
	 * @param ontologyID URI
	 * @param axiom OWL axiom
	 * @return Verbalization of the given axiom
	 * @throws OWLRendererException 
	 * @throws OWLOntologyChangeException 
	 * @throws OWLOntologyCreationException 
	 */
	private String verbalizeWithWS(OWLOntology ont, OWLLogicalAxiom axiom) throws OWLRendererException, OWLOntologyCreationException, OWLOntologyChangeException {
		Set<OWLAxiom> allAxioms = Sets.newHashSet((OWLAxiom) axiom);

		for (OWLEntity entity : axiom.getSignature()) {
			allAxioms.addAll(entity.getAnnotationAssertionAxioms(ont));
		}

		OWLOntologyManager ontologyManager = ACETextManager.createOWLOntologyManager();
		// TODO: think: it would also be possible to set the IRI, but
		// I guess naming the ontology is not necessary if one just wants to verbalize it.
		OWLOntology ontology = ontologyManager.createOntology(allAxioms);
		return verbalizeOntology(ontologyManager, ontology);
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
	private static String verbalizeSimpleAxiom(OWLLogicalAxiom ax, OWLOntology ont) {
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

			return getSimpleClassRelationVerbalization((OWLClass) subClass, (OWLClass) superClass, "Every", ont);
		}
		else if (ax instanceof OWLClassAssertionAxiom) {
			OWLClassAssertionAxiom classAssertionAxiom = (OWLClassAssertionAxiom) ax;
			OWLClassExpression desc = classAssertionAxiom.getClassExpression();
			OWLIndividual ind = classAssertionAxiom.getIndividual();

			if (isAnonymousOrNothing(desc) || isAnonymous(ind)) {
				return null;
			}

			OWLNamedIndividual namedIndividual = ind.asOWLNamedIndividual();

			if (desc.isOWLThing()) {
				return getSg(namedIndividual, ont) + " is something.";
			}

			String descAsString = getSg((OWLClass) desc, ont);
			return getSg(namedIndividual, ont) + " is " + getIndefiniteArticle(descAsString) + " " + descAsString + ".";
		}
		else if (ax instanceof OWLObjectPropertyAssertionAxiom) {
			OWLObjectPropertyAssertionAxiom opAssertionAxiom = (OWLObjectPropertyAssertionAxiom) ax;
			OWLObjectPropertyExpression opExpression = opAssertionAxiom.getProperty();
			OWLIndividual subject = opAssertionAxiom.getSubject();
			OWLIndividual object = opAssertionAxiom.getObject();

			if (opExpression.isAnonymous() || isAnonymous(subject) || isAnonymous(object)) {
				return null;
			}

			return getSg(subject.asOWLNamedIndividual(), ont) + " " + getSg(opExpression.asOWLObjectProperty(), ont) + " " + getSg(object.asOWLNamedIndividual(), ont) + ".";
		}
		else if (ax instanceof OWLDataPropertyAssertionAxiom) {
			OWLDataPropertyAssertionAxiom dpAssertionAxiom = (OWLDataPropertyAssertionAxiom) ax;

			OWLDataPropertyExpression dpExpression = dpAssertionAxiom.getProperty();
			if (dpExpression.isAnonymous()) return null;

			OWLIndividual subject = dpAssertionAxiom.getSubject();
			if (isAnonymous(subject)) return null;

			OWLLiteral literal = dpAssertionAxiom.getObject();
			OWLDatatype datatype = literal.getDatatype();
			String datavalue = "";
			if (datatype.isString()) {
				datavalue = "\"" + literal.getLiteral() + "\"";
			}
			else {
				// BUG: we accept all types of data here (although ACE only supports numbers and strings)
				datavalue = literal.getLiteral();
			}

			// TODO: BUG: get the current rendering instead
			String dpAsString = dpExpression.asOWLDataProperty().getIRI().getFragment();
			// John's temperature is 36.
			return getSg(subject.asOWLNamedIndividual(), ont) + "'s " + dpAsString + " is " + datavalue + ".";
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

			return getSimpleClassRelationVerbalization((OWLClass) desc1, (OWLClass) desc2, "No", ont);

		}
		return null;
	}


	/**
	 * <p>Decides which indefinite article (`a' vs `an') to use, given
	 * a noun (that would follow the article in the text).</p>
	 * 
	 * @param word is an English noun
	 * @return either 'a' or 'an' depending on the given noun
	 */
	private static String getIndefiniteArticle(String word) {
		if (ACEUtils.useIndefiniteArticleAn(word)) {
			return "an";
		}
		return "a";
	}


	private static boolean isAnonymousOrNothing(OWLClassExpression desc) {
		return (desc.isAnonymous() || desc.isOWLNothing());
	}

	private static boolean isAnonymous(OWLIndividual ind) {
		return (ind.isAnonymous());
	}

	private static String getSimpleClassRelationVerbalization(OWLClass class1, OWLClass class2, String prefix, OWLOntology ont) {
		String subClassAsString;
		String superClassAsString;

		if (class1.isOWLThing()) {
			subClassAsString = prefix + "thing";
		}
		else {
			subClassAsString = prefix + " " + getSg(class1, ont);
		}

		if (class2.isOWLThing()) {
			superClassAsString = "something";
		}
		else {
			String superClassName = getSg(class2, ont);
			superClassAsString = getIndefiniteArticle(superClassName) + " " + superClassName;
		}

		return subClassAsString + " is " + superClassAsString + ".";
	}


	/**
	 * <p>Returns the surface form for the given entity.
	 * Looks for it among the annotation axioms in the given
	 * ontology. If this fails then just returns the IRI fragment.</p>
	 * 
	 * @param entity OWL entity
	 * @param ont OWL ontology that contains surfaceform-annotations for the entity
	 * @return Singular form of the entity
	 */
	private static String getSg(OWLEntity entity, OWLOntology ont) {
		// decide if the entity corresponds to CN, TV, or PN
		EntryType entryType = LexiconUtils.getLexiconEntryType(entity);
		// get the type CN_SG, TV_SG, or PN_SG
		MorphType morphType = MorphType.getMorphType(entryType, FieldType.SG);
		// get the IRI that corresponds to this type
		IRI wordformTypeIRI = morphType.getIRI();

		// scan all the annotations that annotate this entity
		for (OWLAnnotationAssertionAxiom annAx : ont.getAnnotationAssertionAxioms(entity.getIRI())) {
			// ... and select the one that annotates it using the ?_SG IRI
			if (annAx.getProperty().getIRI().equals(wordformTypeIRI)) {
				OWLAnnotationValue value = annAx.getValue();
				if (value instanceof OWLLiteral) {
					return ((OWLLiteral) value).getLiteral();
				}
				break;
			}
		}
		return entity.getIRI().getFragment();
	}
}