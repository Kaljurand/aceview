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

import java.net.URI;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLEntityAnnotationAxiom;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;

import simplenlg.lexicon.lexicalitems.Noun;

import com.google.common.collect.Sets;

import ch.uzh.ifi.attempto.ace.ACEVerb;
import ch.uzh.ifi.attempto.aceview.lexicon.ACELexicon;
import ch.uzh.ifi.attempto.aceview.lexicon.ACELexiconEntry;
import ch.uzh.ifi.attempto.aceview.lexicon.FieldType;
import ch.uzh.ifi.attempto.aceview.util.OntologyUtils;
import ch.uzh.ifi.attempto.aceview.util.Showing;


/**
 * <p>Adds linguistic annotations (sg, pl, vbg) to OWL entities (named classes, properties, and
 * individuals). Object and data properties are currently treated in the same way,
 * i.e. as English transitive verbs.
 * In the future they will be applied different ACE categories.</p>
 * 
 * <p>Note that the built-in entities <code>owl:Thing</code>, <code>owl:Nothing</code>,
 * are not linguistically annotated, see {@link Showing#isShow(OWLEntity)}.</p>
 * 
 * @see <a href="http://www.csd.abdn.ac.uk/~agatt/home/links.html">Albert Gatt's Lexicon Generation API</a>
 * 
 * @author Kaarel Kaljurand
 */
public class MorphAnnotation {

	/**
	 * <p>Returns an empty set of axioms if the input entity corresponds to
	 * a function word (<code>owl:Thing</code>, <code>owl:Nothing</code>, ...) which is not to be annotated
	 * with surface forms.</p>
	 * 
	 * <p>Otherwise returns a set of entity annotation axioms for the given entity.
	 * An annotation is not generated in case the entity has already been annotated
	 * with the annotation. This means that surface form is a functional property of the entity.</p>
	 * 
	 * @param df OWLDataFactory
	 * @param ontology OWL ontology in which we check if the entity has already been annotated
	 * @param entity OWL entity to be annotated
	 * @param lemma Lemma form of the entity which should be taken as the basis when generating surface forms
	 * @return Set of OWL entity annotation axioms
	 */
	public static Set<OWLEntityAnnotationAxiom> getMorphAnnotations(OWLDataFactory df, OWLOntology ontology, OWLEntity entity, String lemma) {

		Set<OWLEntityAnnotationAxiom> axioms = Sets.newHashSet();

		if (! Showing.isShow(entity)) {
			return axioms;
		}

		// Existing annotation URIs for the entity.
		Set<URI> annotationURIs = OntologyUtils.getAnnotationURIs(ontology, entity);

		if (entity instanceof OWLClass) {

			if (! annotationURIs.contains(FieldType.SG.getURI())) {
				axioms.add(df.getOWLEntityAnnotationAxiom(entity, FieldType.SG.getURI(), df.getOWLUntypedConstant(lemma)));
			}

			if (! annotationURIs.contains(FieldType.PL.getURI())) {
				Noun noun = new Noun(lemma);
				axioms.add(df.getOWLEntityAnnotationAxiom(entity, FieldType.PL.getURI(), df.getOWLUntypedConstant(noun.getPlural())));
			}
		}
		else if (isVerblike(entity)) {
			ACEVerb verb = new ACEVerb(lemma);

			if (! annotationURIs.contains(FieldType.SG.getURI())) {
				axioms.add(df.getOWLEntityAnnotationAxiom(entity, FieldType.SG.getURI(), df.getOWLUntypedConstant(verb.getPresent3SG())));
			}

			if (! annotationURIs.contains(FieldType.PL.getURI())) {
				axioms.add(df.getOWLEntityAnnotationAxiom(entity, FieldType.PL.getURI(), df.getOWLUntypedConstant(lemma)));
			}

			if (! annotationURIs.contains(FieldType.VBG.getURI())) {
				axioms.add(df.getOWLEntityAnnotationAxiom(entity, FieldType.VBG.getURI(), df.getOWLUntypedConstant(verb.getPastParticiple())));
			}
		}
		else if (entity instanceof OWLIndividual) {
			if (! annotationURIs.contains(FieldType.SG.getURI())) {
				axioms.add(df.getOWLEntityAnnotationAxiom(entity, FieldType.SG.getURI(), df.getOWLUntypedConstant(lemma)));
			}
		}

		return axioms;
	}


	/**
	 * <p>Creates morphological annotations from the lemma, using English morphological
	 * synthesis. The lemma should normally be the rendering of the given OWL entity.</p>
	 * 
	 * @param df OWL data factory
	 * @param entity OWL entity
	 * @param lemma OWL entity rendering
	 * @return Set of OWL entity annotation axioms
	 */
	public static Set<OWLEntityAnnotationAxiom> createMorphAnnotations(OWLDataFactory df, OWLEntity entity, String lemma) {
		Set<OWLEntityAnnotationAxiom> axioms = Sets.newHashSet();

		if (! Showing.isShow(entity)) {
			return axioms;
		}

		if (entity instanceof OWLClass) {
			Noun noun = new Noun(lemma);
			axioms.add(df.getOWLEntityAnnotationAxiom(entity, FieldType.SG.getURI(), df.getOWLUntypedConstant(lemma)));
			axioms.add(df.getOWLEntityAnnotationAxiom(entity, FieldType.PL.getURI(), df.getOWLUntypedConstant(noun.getPlural())));
		}
		else if (isVerblike(entity)) {
			ACEVerb verb = new ACEVerb(lemma);
			axioms.add(df.getOWLEntityAnnotationAxiom(entity, FieldType.SG.getURI(), df.getOWLUntypedConstant(verb.getPresent3SG())));
			axioms.add(df.getOWLEntityAnnotationAxiom(entity, FieldType.PL.getURI(), df.getOWLUntypedConstant(lemma)));
			axioms.add(df.getOWLEntityAnnotationAxiom(entity, FieldType.VBG.getURI(), df.getOWLUntypedConstant(verb.getPastParticiple())));
		}
		else if (entity instanceof OWLIndividual) {
			axioms.add(df.getOWLEntityAnnotationAxiom(entity, FieldType.SG.getURI(), df.getOWLUntypedConstant(lemma)));
		}

		return axioms;
	}


	/**
	 * <p>Creates a set of entity annotation axioms on the basis the lexicon entries
	 * for the given entity.</p>
	 * 
	 * @param df OWLDataFactory
	 * @param lexicon Lexicon from where we take the entity's surface forms
	 * @param entity OWL entity to be annotated
	 * @return Set of OWL entity annotation axioms
	 */
	public static Set<OWLEntityAnnotationAxiom> getMorphAnnotationsFromLexicon(OWLDataFactory df, ACELexicon<OWLEntity> lexicon, OWLEntity entity) {

		Set<OWLEntityAnnotationAxiom> axioms = Sets.newHashSet();

		if (! Showing.isShow(entity)) {
			return axioms;
		}

		ACELexiconEntry entry = lexicon.getEntry(entity);

		if (entry == null) {
			return axioms;
		}


		if (entity instanceof OWLClass) {
			addToAxioms(df, axioms, entity, FieldType.SG.getURI(), entry.getSg());
			addToAxioms(df, axioms, entity, FieldType.PL.getURI(), entry.getPl());
		}
		else if (isVerblike(entity)) {
			addToAxioms(df, axioms, entity, FieldType.SG.getURI(), entry.getSg());
			addToAxioms(df, axioms, entity, FieldType.PL.getURI(), entry.getPl());
			addToAxioms(df, axioms, entity, FieldType.VBG.getURI(), entry.getVbg());
		}
		else if (entity instanceof OWLIndividual) {
			addToAxioms(df, axioms, entity, FieldType.SG.getURI(), entry.getSg());
		}

		return axioms;
	}


	private static void addToAxioms(OWLDataFactory df, Set<OWLEntityAnnotationAxiom> axioms, OWLEntity entity, URI uri, String form) {
		if (form != null) {
			axioms.add(df.getOWLEntityAnnotationAxiom(entity, uri, df.getOWLUntypedConstant(form)));
		}
	}


	private static boolean isVerblike(OWLEntity entity) {
		return (entity instanceof OWLObjectProperty || entity instanceof OWLDataProperty);
	}
}