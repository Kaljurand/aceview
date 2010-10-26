package ch.uzh.ifi.attempto.aceview;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;
import org.protege.editor.owl.model.OWLModelManager;
import org.protege.editor.owl.model.OWLModelManagerImpl;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntologyID;

import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;

import com.google.common.collect.Sets;

import ch.uzh.ifi.attempto.ace.ACESentence;
import ch.uzh.ifi.attempto.ace.ACESplitter;

public class ACESnippetImplTest {

	private static final String PREFIX = "http://attempto.ifi.uzh.ch/aceview_test";
	private static final IRI IRI_TEST = IRI.create(PREFIX);
	private static final OWLOntologyID ID_TEST = new OWLOntologyID(IRI_TEST);

	private static final OWLDataFactory df = new OWLDataFactoryImpl();

	private static final OWLAxiom john_likes_mary = createAxiomJohnLikesMary();
	private static final OWLAxiom every_man_is_a_human = createAxiomEveryManIsAHuman();
	private static final ACEViewPreferences prefs = ACEViewPreferences.getInstance();

	static {
		prefs.setParseWithUndefinedTokens(true);
		prefs.setGuessingEnabled(true);
		prefs.setClexEnabled(true);
		prefs.setUseMos(false);
		try {
			ParserHolder.updateACEParser(prefs);
		} catch (Exception e) {
			e.printStackTrace();
		}

		// Create an ACE text that provides a lexicon for
		// the snippets that we are going to parse.
		ACETextManager.createACEText(new OWLOntologyID());
	}


	@Test
	public final void test0() {
		String str = "Every man is a human.";
		List<ACESentence> sents = ACESplitter.getSentences(str);

		ACESnippet s1 = new ACESnippetImpl(ID_TEST, sents);
		ACESnippet s2 = new ACESnippetImpl(ID_TEST, sents);

		assertEquals(s1, s2);
	}


	@Test
	public final void test1() {
		String str = "Every man is a human.";
		List<ACESentence> sents = ACESplitter.getSentences(str);

		ACESnippet s = new ACESnippetImpl(ID_TEST, sents);

		assertEquals(s.getAxiom(), every_man_is_a_human);
		assertEquals(s.getLogicalAxioms(), Sets.newHashSet(every_man_is_a_human));
	}


	@Test
	public final void test2() {
		String str1 = "Every man is a human.";
		String str2 = "If there is a man then the man is a human.";

		List<ACESentence> sents1 = ACESplitter.getSentences(str1);
		List<ACESentence> sents2 = ACESplitter.getSentences(str2);

		ACESnippet s1 = new ACESnippetImpl(ID_TEST, sents1);
		ACESnippet s2 = new ACESnippetImpl(ID_TEST, sents2);

		assertEquals(s1.getLogicalAxioms(), s2.getLogicalAxioms());
	}


	@Test
	public final void test3() {
		String str = "See tekst on vigane.";
		List<ACESentence> sents = ACESplitter.getSentences(str);

		ACESnippet s = new ACESnippetImpl(ID_TEST, sents);

		assertEquals(s.getAxiom(), null);
		assertEquals(s.getLogicalAxioms(), Sets.newHashSet());
	}


	@Test
	public final void test4() {
		String str = "John likes Mary.";
		List<ACESentence> sents = ACESplitter.getSentences(str);

		ACESnippet s = new ACESnippetImpl(ID_TEST, sents);

		assertEquals(s.getLogicalAxioms(), Sets.newHashSet(john_likes_mary));
	}

	@Test
	public final void test5() {

		// TODO: BUG: experimental
		OWLModelManager modelManager = new OWLModelManagerImpl();
		ACETextManager.setOWLModelManager(modelManager);

		prefs.setUseMos(true);
		String str = "man SubClassOf human";
		List<ACESentence> sents = ACESplitter.getSentences(str);

		ACESnippet s = new ACESnippetImpl(ID_TEST, sents);

		assertEquals(s.getAxiom(), every_man_is_a_human);
		assertEquals(s.getLogicalAxioms(), Sets.newHashSet(every_man_is_a_human));
	}


	private static OWLAxiom createAxiomJohnLikesMary() {
		OWLIndividual john = df.getOWLNamedIndividual(IRI.create(PREFIX + "#John"));
		OWLIndividual mary = df.getOWLNamedIndividual(IRI.create(PREFIX + "#Mary"));
		OWLObjectProperty like = df.getOWLObjectProperty(IRI.create(PREFIX + "#like"));
		OWLAxiom john_likes_mary = df.getOWLObjectPropertyAssertionAxiom(like, john, mary);
		return john_likes_mary;
	}


	private static OWLAxiom createAxiomEveryManIsAHuman() {
		OWLClass man = df.getOWLClass(IRI.create(PREFIX + "#man"));
		OWLClass human = df.getOWLClass(IRI.create(PREFIX + "#human"));
		OWLAxiom every_man_is_a_human = df.getOWLSubClassOfAxiom(man, human);
		return every_man_is_a_human;
	}
}
