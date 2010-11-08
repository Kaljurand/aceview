package ch.uzh.ifi.attempto.aceview;

import java.util.Set;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntologyID;

import ch.uzh.ifi.attempto.aceview.lexicon.MorphType;
import ch.uzh.ifi.attempto.aceview.util.OntologyUtils;

import com.google.common.collect.Sets;

import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;

public class Utils {

	private static final String PREFIX = "http://attempto.ifi.uzh.ch/aceview_test";
	private static final IRI IRI_TEST = IRI.create(PREFIX);
	private static final OWLDataFactory df = new OWLDataFactoryImpl();

	public static final OWLOntologyID ID_TEST = new OWLOntologyID(IRI_TEST);


	public static OWLLogicalAxiom createAxiomJohnLikesMary() {
		OWLIndividual john = df.getOWLNamedIndividual(IRI.create(PREFIX + "#John"));
		OWLIndividual mary = df.getOWLNamedIndividual(IRI.create(PREFIX + "#Mary"));
		OWLObjectProperty like = df.getOWLObjectProperty(IRI.create(PREFIX + "#like"));
		OWLAxiom john_likes_mary = df.getOWLObjectPropertyAssertionAxiom(like, john, mary);
		return (OWLLogicalAxiom) john_likes_mary;
	}


	public static Set<OWLAxiom> getAnnotations1() {
		Set<OWLAxiom> axioms = Sets.newHashSet();
		axioms.add(OntologyUtils.createIRIAnnotationAxiom(df, MorphType.TV_VBG.getIRI(), IRI.create(PREFIX + "#like"), "liked"));
		axioms.add(OntologyUtils.createIRIAnnotationAxiom(df, MorphType.TV_SG.getIRI(), IRI.create(PREFIX + "#like"), "likes"));
		axioms.add(OntologyUtils.createIRIAnnotationAxiom(df, MorphType.TV_SG.getIRI(), IRI.create(PREFIX + "#Mary"), "noise_annotation1"));
		axioms.add(OntologyUtils.createIRIAnnotationAxiom(df, MorphType.PN_SG.getIRI(), IRI.create(PREFIX + "#Mary"), "Mari"));
		return axioms;
	}


	public static OWLLogicalAxiom createAxiomEveryManIsAHuman() {
		OWLClass man = df.getOWLClass(IRI.create(PREFIX + "#man"));
		OWLClass human = df.getOWLClass(IRI.create(PREFIX + "#human"));
		OWLAxiom every_man_is_a_human = df.getOWLSubClassOfAxiom(man, human);
		return (OWLLogicalAxiom) every_man_is_a_human;
	}
}
