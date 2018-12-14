package ch.uzh.ifi.attempto.aceview;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;
import org.semanticweb.owlapi.io.OWLRendererException;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChangeException;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import ch.uzh.ifi.attempto.ace.ACESentence;
import ch.uzh.ifi.attempto.ace.ACESplitter;

public class AxiomVerbalizerTest {

	private static final OWLLogicalAxiom john_likes_mary = Utils.createAxiomJohnLikesMary();
	private static final OWLLogicalAxiom every_man_is_a_human = Utils.createAxiomEveryManIsAHuman();

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
			ontology = ACETextManager.createOWLOntologyManager().createOntology(Utils.getAnnotations1());
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

}