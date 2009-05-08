package ch.uzh.ifi.attempto.aceview;

import static org.junit.Assert.assertEquals;

import java.net.URI;
import java.util.List;

import org.junit.Test;
import org.semanticweb.owl.model.OWLAxiom;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLDataFactory;
import org.semanticweb.owl.model.OWLIndividual;
import org.semanticweb.owl.model.OWLObjectProperty;

import uk.ac.manchester.cs.owl.OWLDataFactoryImpl;

import com.google.common.collect.Sets;

import ch.uzh.ifi.attempto.ace.ACESentence;
import ch.uzh.ifi.attempto.ace.ACESentenceSplitter;

public class ACESnippetImplTest {

	private static final URI URI_TEST = URI.create("http://attempto.ifi.uzh.ch/aceview_test");

	private static final OWLDataFactory df = new OWLDataFactoryImpl();

	private static final OWLAxiom john_likes_mary = createAxiomJohnLikesMary();
	private static final OWLAxiom every_man_is_a_human = createAxiomEveryManIsAHuman();

	static {
		ACEViewPreferences prefs = ACEViewPreferences.getInstance();
		prefs.setParseWithUndefinedTokens(true);
		prefs.setGuessingEnabled(true);
		prefs.setClexEnabled(true);
	}


	@Test
	public final void test0() {
		String str = "Every man is a human.";
		List<ACESentence> sents = ACESentenceSplitter.splitSentences(str);

		ACESnippet s1 = new ACESnippetImpl(URI_TEST, sents);
		ACESnippet s2 = new ACESnippetImpl(URI_TEST, sents);

		assertEquals(s1, s2);
	}


	@Test
	public final void test1() {
		String str = "Every man is a human.";
		List<ACESentence> sents = ACESentenceSplitter.splitSentences(str);

		ACESnippet s = new ACESnippetImpl(URI_TEST, sents);

		assertEquals(s.getAxiom(), every_man_is_a_human);
		assertEquals(s.getLogicalAxioms(), Sets.newHashSet(every_man_is_a_human));
	}


	@Test
	public final void test2() {
		String str1 = "Every man is a human.";
		String str2 = "If there is a man then the man is a human.";

		List<ACESentence> sents1 = ACESentenceSplitter.splitSentences(str1);
		List<ACESentence> sents2 = ACESentenceSplitter.splitSentences(str2);

		ACESnippet s1 = new ACESnippetImpl(URI_TEST, sents1);
		ACESnippet s2 = new ACESnippetImpl(URI_TEST, sents2);

		assertEquals(s1.getLogicalAxioms(), s2.getLogicalAxioms());
	}


	@Test
	public final void test3() {
		String str = "See tekst on vigane.";
		List<ACESentence> sents = ACESentenceSplitter.splitSentences(str);

		ACESnippet s = new ACESnippetImpl(URI_TEST, sents);

		assertEquals(s.getAxiom(), null);
		assertEquals(s.getLogicalAxioms(), Sets.newHashSet());
	}


	@Test
	public final void test4() {
		String str = "John likes Mary.";
		List<ACESentence> sents = ACESentenceSplitter.splitSentences(str);

		ACESnippet s = new ACESnippetImpl(URI_TEST, sents);

		assertEquals(s.getLogicalAxioms(), Sets.newHashSet(john_likes_mary));
	}


	private static OWLAxiom createAxiomJohnLikesMary() {
		OWLIndividual john = df.getOWLIndividual(URI.create(URI_TEST + "#John"));
		OWLIndividual mary = df.getOWLIndividual(URI.create(URI_TEST + "#Mary"));
		OWLObjectProperty like = df.getOWLObjectProperty(URI.create(URI_TEST + "#like"));
		OWLAxiom john_likes_mary = df.getOWLObjectPropertyAssertionAxiom(john, like, mary);
		return john_likes_mary;
	}


	private static OWLAxiom createAxiomEveryManIsAHuman() {
		OWLClass man = df.getOWLClass(URI.create(URI_TEST + "#man"));
		OWLClass human = df.getOWLClass(URI.create(URI_TEST + "#human"));
		OWLAxiom every_man_is_a_human = df.getOWLSubClassAxiom(man, human);
		return every_man_is_a_human;
	}
}
