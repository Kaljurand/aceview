/*
 * This file is part of ACE View.
 * Copyright 2008-2009, Attempto Group, University of Zurich (see http://attempto.ifi.uzh.ch).
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
import java.net.URI;
import java.util.Iterator;
import java.util.Set;

import javax.swing.JOptionPane;

import org.apache.log4j.Logger;
import org.coode.owlapi.owlxml.renderer.OWLXMLRenderer;
import org.semanticweb.owlapi.io.OWLRendererException;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDisjointClassesAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLEntityAnnotationAxiom;
import org.semanticweb.owlapi.model.OWLIndividual;
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

import ch.uzh.ifi.attempto.aceview.lexicon.ACELexicon;
import ch.uzh.ifi.attempto.aceview.lexicon.ACELexiconEntry;
import ch.uzh.ifi.attempto.owl.VerbalizerWebservice;

public class AxiomVerbalizer {

	private static final Logger logger = Logger.getLogger(AxiomVerbalizer.class);
	private final VerbalizerWebservice verbalizerWS;
	private final ACELexicon<OWLEntity> lexicon;

	public AxiomVerbalizer(String verbalizerWSURL, ACELexicon<OWLEntity> lexicon) {
		this.verbalizerWS = new VerbalizerWebservice(verbalizerWSURL);
		this.lexicon = lexicon;
	}

	public AxiomVerbalizer(VerbalizerWebservice verbalizerWS, ACELexicon<OWLEntity> lexicon) {
		this.verbalizerWS = verbalizerWS;
		this.lexicon = lexicon;
	}


	/**
	 * <p>Verbalizes a single logical OWL axiom as an ACE snippet.</p>
	 * 
	 * @param ontologyID Namespace for the snippet to be created
	 * @param axiom OWL logical axiom to be verbalized
	 * @return ACE snippet containing the verbalization of the given axiom
	 * @throws OWLRendererException 
	 * @throws OWLOntologyChangeException 
	 * @throws OWLOntologyCreationException 
	 */
	public ACESnippet verbalizeAxiom(OWLOntologyID ontologyID, OWLLogicalAxiom axiom) throws OWLRendererException, OWLOntologyCreationException, OWLOntologyChangeException {

		// BUG: this is a bit of a hack for performance reasons.
		// We verbalize simple axioms without having to use
		// the verbalizer webservice. It seems that about 50% of
		// the axioms in real-world ontologies are simple SubClassOf-axioms,
		// so it really pays off performancewise to verbalize them directly in Java.
		String verbalization = verbalizeSimpleSubClassAxiom(axiom);

		if (verbalization != null) {
			return new ACESnippetImpl(ontologyID, verbalization, axiom);
		}

		logger.info("Verbalizing the axiom using WS");
		OWLDataFactory df = ACETextManager.getOWLModelManager().getOWLDataFactory();
		try {
			verbalization = verbalizeWithWS(ontologyID, axiom, df);
		}
		catch (Exception e) {
			JOptionPane.showMessageDialog(null, "OWL verbalizer error:\n" + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
		}

		ACESnippet snippet = null;
		if (verbalization == null) {
			// Axioms not verbalized, using Manchester Syntax rendering.
			logger.info("Axioms not verbalized, using Manchester Syntax rendering");
			String manSynRendering = ACETextManager.getOWLModelManager().getRendering(axiom);
			if (manSynRendering == null) {
				snippet = new ACESnippetImpl(ontologyID, "", axiom, axiom.toString());
			}
			else {
				snippet = new ACESnippetImpl(ontologyID, "", axiom, manSynRendering);
			}			
		}
		else {
			snippet = new ACESnippetImpl(ontologyID, verbalization, axiom);
		}

		return snippet;
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
	private String verbalizeWithWS(OWLOntologyID ontologyID, OWLLogicalAxiom axiom, OWLDataFactory df) throws OWLRendererException, OWLOntologyCreationException, OWLOntologyChangeException {
		Set<OWLAxiom> allAxioms = Sets.newHashSet((OWLAxiom) axiom);

		for (OWLEntity entity : axiom.getReferencedEntities()) {
			Set<OWLEntityAnnotationAxiom> annotationAxioms = MorphAnnotation.getMorphAnnotationsFromLexicon(df, lexicon, entity);
			allAxioms.addAll(annotationAxioms);
		}

		OWLOntologyManager ontologyManager = ACETextManager.createOWLOntologyManager();
		// TODO: think: it would also be possible to set the IRI, but
		// I guess naming the ontology is not necessary if one just wants to verbalize it.
		OWLOntology ontology = ontologyManager.createOntology(allAxioms);
		return verbalizeOntology(ontologyManager, ontology);
	}


	/**
	 * <p>Verbalizes an <code>OWLAxiom</code> given that it is a simple
	 * SubClassOf-axiom (with named classes), or a simple ClassAssertion-axiom
	 * (with a named class) otherwise returns <code>null</code>.</p>
	 * 
	 * @param ax an instance of <code>OWLAxiom</code> to be verbalized
	 * @return sentence corresponding to the <code>OWLAxiom</code> or <code>null</code>
	 */
	private String verbalizeSimpleSubClassAxiom(OWLLogicalAxiom ax) {
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

			OWLNamedIndividual namedIndividual = ind.asNamedIndividual();

			if (desc.isOWLThing()) {
				return getSg(namedIndividual) + " is something.";
			}

			String descAsString = getSg((OWLClass) desc);
			return getSg(namedIndividual) + " is " + getIndefiniteArticle(descAsString) + " " + descAsString + ".";
		}
		else if (ax instanceof OWLObjectPropertyAssertionAxiom) {
			OWLObjectPropertyAssertionAxiom opAssertionAxiom = (OWLObjectPropertyAssertionAxiom) ax;
			OWLObjectPropertyExpression opExpression = opAssertionAxiom.getProperty();
			OWLIndividual subject = opAssertionAxiom.getSubject();
			OWLIndividual object = opAssertionAxiom.getObject();

			if (opExpression.isAnonymous() || isAnonymous(subject) || isAnonymous(object)) {
				return null;
			}

			return getSg(subject.asNamedIndividual()) + " " + getSg(opExpression.asOWLObjectProperty()) + " " + getSg(object.asNamedIndividual()) + ".";
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


	/**
	 * <p>Decides which indefinite article (`a' vs `an') to use, given
	 * a noun (that would follow the article in the text).
	 * The patters have been written by Tobias Kuhn, in core/TextContainer.java
	 * in the AceWiki distribution.</p>
	 * 
	 * @param word is an English noun
	 * @return either 'a' or 'an' depending on the given noun
	 */
	private static String getIndefiniteArticle(String word) {
		String tn = word.toLowerCase();
		boolean an = false;
		if (tn.matches("[aeiou].*")) an = true;
		if (tn.matches("[fhlmnrsx]")) an = true;
		if (tn.matches("[fhlmnrsx]-.*")) an = true;
		if (tn.equals("u")) an = false;
		if (tn.matches("u-.*")) an = false;
		if (tn.matches("u[rtn]i.*")) an = false;
		if (tn.matches("use.*")) an = false;
		if (tn.matches("uk.*")) an = false;

		if (an) {
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

	private String getSimpleClassRelationVerbalization(OWLClass class1, OWLClass class2, String prefix) {
		String subClassAsString;
		String superClassAsString;

		if (class1.isOWLThing()) {
			subClassAsString = prefix + "thing";
		}
		else {
			subClassAsString = prefix + " " + getSg(class1);
		}

		if (class2.isOWLThing()) {
			superClassAsString = "something";
		}
		else {
			String superClassName = getSg(class2);
			superClassAsString = getIndefiniteArticle(superClassName) + " " + superClassName;
		}

		return subClassAsString + " is " + superClassAsString + ".";
	}


	/**
	 * <p>Convenience method that returns the singular form of the given OWL entity,
	 * or the toString() of the entity if the singular form is not defined.</p>
	 * 
	 * @param entity OWL entity
	 * @return Singular form of the entity
	 */
	private String getSg(OWLEntity entity) {
		ACELexiconEntry lexiconEntry = lexicon.getEntry(entity);
		if (lexiconEntry == null) {
			return entity.toString();
		}
		String sg = lexiconEntry.getSg();
		if (sg == null) {
			return entity.toString();
		}
		return sg;
	}
}