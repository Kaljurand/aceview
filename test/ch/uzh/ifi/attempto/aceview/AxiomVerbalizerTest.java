package ch.uzh.ifi.attempto.aceview;

import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.Set;

import org.junit.Test;
import org.semanticweb.owlapi.io.OWLRendererException;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChangeException;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;
import ch.uzh.ifi.attempto.ace.ACESentence;
import ch.uzh.ifi.attempto.ace.ACESplitter;
import ch.uzh.ifi.attempto.aceview.lexicon.MorphType;
import ch.uzh.ifi.attempto.aceview.util.OntologyUtils;

import com.google.common.collect.Sets;

public class AxiomVerbalizerTest {

	private static final String PREFIX = "http://attempto.ifi.uzh.ch/aceview_test";
//	private static final IRI IRI_TEST = IRI.create(PREFIX);
//	private static final OWLOntologyID ID_TEST = new OWLOntologyID(IRI_TEST);

	private static final OWLDataFactory df = new OWLDataFactoryImpl();

	private static final OWLLogicalAxiom john_likes_mary = createAxiomJohnLikesMary();
	private static final OWLLogicalAxiom every_man_is_a_human = createAxiomEveryManIsAHuman();

	private static final String owlToAceWebserviceUrl;

	static {
		ACEViewPreferences prefs = ACEViewPreferences.getInstance();
		prefs.setParseWithUndefinedTokens(true);
		prefs.setGuessingEnabled(true);
		prefs.setClexEnabled(true);
		owlToAceWebserviceUrl = prefs.getOwlToAceWebservices().iterator().next();
	}

	@Test
	public final void test0() {

		AxiomVerbalizer av = new AxiomVerbalizer(owlToAceWebserviceUrl);

		OWLOntology ontology = createOntology();

		ACESnippet snippet = null;
		try {
			snippet = av.verbalizeAxiom(every_man_is_a_human, ontology);
		} catch (OWLRendererException e) {
			e.printStackTrace();
		} catch (OWLOntologyChangeException e) {
			e.printStackTrace();
		} catch (OWLOntologyCreationException e) {
			e.printStackTrace();
		}

		List<ACESentence> gold_sents = ACESplitter.getSentences("Every man is a human.");
		List<ACESentence> test_sents = snippet.getSentences();

		assertEquals(gold_sents, test_sents);
	}

	@Test
	public final void test1() {

		AxiomVerbalizer av = new AxiomVerbalizer(owlToAceWebserviceUrl);

		OWLOntology ontology = null;
		try {
			ontology = ACETextManager.createOWLOntologyManager().createOntology(getAnnotations1());
		} catch (OWLOntologyCreationException e1) {
			e1.printStackTrace();
		}

		ACESnippet snippet = null;
		try {
			snippet = av.verbalizeAxiom(john_likes_mary, ontology);
		} catch (OWLRendererException e) {
			e.printStackTrace();
		} catch (OWLOntologyChangeException e) {
			e.printStackTrace();
		} catch (OWLOntologyCreationException e) {
			e.printStackTrace();
		}

		List<ACESentence> gold_sents = ACESplitter.getSentences("John likes Mari.");
		List<ACESentence> test_sents = snippet.getSentences();

		assertEquals(gold_sents, test_sents);
	}


	private static OWLOntology createOntology() {
		OWLOntology ontology = null;

		OWLOntologyManager ontologyManager = ACETextManager.createOWLOntologyManager();
		try {
			ontology = ontologyManager.createOntology();
		} catch (OWLOntologyCreationException e1) {
			e1.printStackTrace();
		}
		return ontology;
	}


	private static OWLLogicalAxiom createAxiomJohnLikesMary() {
		OWLIndividual john = df.getOWLNamedIndividual(IRI.create(PREFIX + "#John"));
		OWLIndividual mary = df.getOWLNamedIndividual(IRI.create(PREFIX + "#Mary"));
		OWLObjectProperty like = df.getOWLObjectProperty(IRI.create(PREFIX + "#like"));
		OWLAxiom john_likes_mary = df.getOWLObjectPropertyAssertionAxiom(like, john, mary);
		return (OWLLogicalAxiom) john_likes_mary;
	}


	private static Set<OWLAxiom> getAnnotations1() {
		Set<OWLAxiom> axioms = Sets.newHashSet();
		axioms.add(OntologyUtils.createEntityAnnotationAxiom(df, MorphType.TV_VBG.getIRI(), df.getOWLObjectProperty(IRI.create(PREFIX + "#like")), "liked"));
		axioms.add(OntologyUtils.createEntityAnnotationAxiom(df, MorphType.TV_SG.getIRI(), df.getOWLObjectProperty(IRI.create(PREFIX + "#like")), "likes"));
		axioms.add(OntologyUtils.createEntityAnnotationAxiom(df, MorphType.TV_SG.getIRI(), df.getOWLNamedIndividual(IRI.create(PREFIX + "#Mary")), "noise_annotation1"));
		axioms.add(OntologyUtils.createEntityAnnotationAxiom(df, MorphType.PN_SG.getIRI(), df.getOWLNamedIndividual(IRI.create(PREFIX + "#Mary")), "Mari"));
		return axioms;
	}


	private static OWLLogicalAxiom createAxiomEveryManIsAHuman() {
		OWLClass man = df.getOWLClass(IRI.create(PREFIX + "#man"));
		OWLClass human = df.getOWLClass(IRI.create(PREFIX + "#human"));
		OWLAxiom every_man_is_a_human = df.getOWLSubClassOfAxiom(man, human);
		return (OWLLogicalAxiom) every_man_is_a_human;
	}

}