package ch.uzh.ifi.attempto.aceview;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;
import org.protege.editor.owl.model.OWLModelManager;
import org.protege.editor.owl.model.OWLModelManagerImpl;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;

import com.google.common.collect.Sets;

import ch.uzh.ifi.attempto.ace.ACESentence;
import ch.uzh.ifi.attempto.ace.ACESplitter;

public class ACESnippetImplTest {

	private static final OWLLogicalAxiom john_likes_mary = Utils.createAxiomJohnLikesMary();
	private static final OWLLogicalAxiom every_man_is_a_human = Utils.createAxiomEveryManIsAHuman();

	static {
		Utils.setupACEParser();
	}


	@Test
	public final void test0() {
		String str = "Every man is a human.";
		List<ACESentence> sents = ACESplitter.getSentences(str);

		ACESnippet s1 = new ACESnippetImpl(Utils.ID_TEST, sents);
		ACESnippet s2 = new ACESnippetImpl(Utils.ID_TEST, sents);

		assertEquals(s1, s2);
	}


	@Test
	public final void test1() {
		String str = "Every man is a human.";
		List<ACESentence> sents = ACESplitter.getSentences(str);

		ACESnippet s = new ACESnippetImpl(Utils.ID_TEST, sents);

		assertEquals(s.getAxiom(), every_man_is_a_human);
		assertEquals(s.getLogicalAxioms(), Sets.newHashSet(every_man_is_a_human));
	}


	@Test
	public final void test2() {
		String str1 = "Every man is a human.";
		String str2 = "If there is a man then the man is a human.";

		List<ACESentence> sents1 = ACESplitter.getSentences(str1);
		List<ACESentence> sents2 = ACESplitter.getSentences(str2);

		ACESnippet s1 = new ACESnippetImpl(Utils.ID_TEST, sents1);
		ACESnippet s2 = new ACESnippetImpl(Utils.ID_TEST, sents2);

		assertEquals(s1.getLogicalAxioms(), s2.getLogicalAxioms());
	}


	@Test
	public final void test3() {
		String str = "See tekst on vigane.";
		List<ACESentence> sents = ACESplitter.getSentences(str);

		ACESnippet s = new ACESnippetImpl(Utils.ID_TEST, sents);

		assertEquals(s.getAxiom(), null);
		assertEquals(s.getLogicalAxioms(), Sets.newHashSet());
	}


	@Test
	public final void test4() {
		String str = "John likes Mary.";
		List<ACESentence> sents = ACESplitter.getSentences(str);

		ACESnippet s = new ACESnippetImpl(Utils.ID_TEST, sents);

		assertEquals(s.getLogicalAxioms(), Sets.newHashSet(john_likes_mary));
	}

	@Test
	public final void test5() {

		// TODO: BUG: experimental
		OWLModelManager modelManager = new OWLModelManagerImpl();
		ACETextManager.setOWLModelManager(modelManager);

		Utils.getPreferences().setUseMos(true);

		String str = "man SubClassOf human";
		List<ACESentence> sents = ACESplitter.getSentences(str);

		ACESnippet s = new ACESnippetImpl(Utils.ID_TEST, sents);

		assertEquals(s.getAxiom(), every_man_is_a_human);
		assertEquals(s.getLogicalAxioms(), Sets.newHashSet(every_man_is_a_human));
	}
}
