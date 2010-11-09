package ch.uzh.ifi.attempto.aceview;

import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.Set;

import org.junit.Test;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;

import ch.uzh.ifi.attempto.ace.ACESentence;
import ch.uzh.ifi.attempto.ace.ACESplitter;

import com.google.common.collect.Sets;


public class ACETextImplTest {

	private static final OWLLogicalAxiom john_likes_mary = Utils.createAxiomJohnLikesMary();
	private static final OWLLogicalAxiom every_man_is_a_human = Utils.createAxiomEveryManIsAHuman();

	static {
		Utils.setupACEParser();
	}


	@Test
	public final void test0() {
		ACEText<OWLEntity, OWLLogicalAxiom> acetext = new ACETextImpl();
		acetext.add(new ACESnippetImpl(Utils.ID_TEST, "John likes Mary.", john_likes_mary));
		acetext.add(new ACESnippetImpl(Utils.ID_TEST, "Every man is a human.", every_man_is_a_human));

		assertEquals(acetext.size(), 2);
	}


	@Test
	public final void test1() {
		ACEText<OWLEntity, OWLLogicalAxiom> acetext = new ACETextImpl();
		ACESnippet snippet = new ACESnippetImpl(Utils.ID_TEST, "", john_likes_mary);
		acetext.add(snippet);

		assertEquals(acetext.containsAxiom(john_likes_mary), true);
		assertEquals(acetext.containsAxiom(every_man_is_a_human), false);
		assertEquals(acetext.getAxiomSnippets(john_likes_mary), Sets.newHashSet(snippet));
		assertEquals(acetext.getUnverbalizedCount(), 1);
	}


	@Test
	public final void test3() {
		ACEText<OWLEntity, OWLLogicalAxiom> acetext = new ACETextImpl();
		acetext.add(new ACESnippetImpl(Utils.ID_TEST, "John likes Mary.", john_likes_mary));
		acetext.add(new ACESnippetImpl(Utils.ID_TEST, "Every man is a human.", every_man_is_a_human));
		Set<OWLLogicalAxiom> removedAxioms = acetext.removeAxiom(john_likes_mary);
		assertEquals(removedAxioms, Sets.newHashSet());
	}


	@Test
	public final void test4() {
		OWLLogicalAxiom every_dog_is_an_animal = Utils.createAxiomEveryDogIsAnAnimal();

		ACESnippet s1 = new ACESnippetImpl(Utils.ID_TEST, "Every dog is an animal.", every_dog_is_an_animal);

		List<ACESentence> sents = ACESplitter.getSentences("Every man is a human and every dog is an animal.");
		ACESnippet s2 = new ACESnippetImpl(Utils.ID_TEST, sents);

		ACEText<OWLEntity, OWLLogicalAxiom> acetext = new ACETextImpl();
		acetext.add(new ACESnippetImpl(Utils.ID_TEST, "If there is a dog then the dog is an animal.", every_dog_is_an_animal));
		acetext.add(s2);

		// The text now contains two snippets which both share the axiom every_dog_is_an_animal.
		// We remove the first snippet.
		// The text should still contain the axiom because another snippet refers to it.

		Set<OWLLogicalAxiom> removedAxioms = acetext.remove(s1);
		assertEquals(acetext.contains(s1), false);
		assertEquals(acetext.containsAxiom(every_dog_is_an_animal), true);
		assertEquals(removedAxioms, Sets.newHashSet());
	}


	/**
	 * <p>Removes all the ACE snippets that reference the given axiom.
	 * Because in general, a snippet can reference several axioms, removing
	 * a snippet can leave around a set of unreferenced axioms. Therefore, this
	 * method returns a set of OWL axioms that were left over after the removal
	 * of the snippets. The caller is expected
	 * to form new snippets from the returned axioms, and add these snippets
	 * back into the text. In this way, the state of the OWL ontology and the
	 * state of the ACE text will stay in correspondence.</p>
	 * 
	 * <p>For example, given the axiom</p>
	 * 
	 * <pre>
	 * SubClassOf(dog animal)
	 * </pre>
	 * 
	 * <p>we might remove the following snippets</p>
	 * 
	 * <pre>
	 * [If there is a dog then the dog is an animal.]
	 * [SubClassOf(dog animal)]
	 * </pre>
	 * 
	 * <pre>
	 * [Every man is a human and every dog is an animal.]
	 * [SubClassOf(man human), SubClassOf(dog animal)]
	 * </pre>
	 * 
	 * <p>The first of these snippets can be safely removed because it
	 * references only one axiom (the given one). The second snippet, however,
	 * references also another axiom, <code>SubClassOf(man human)</code>.
	 * This we call an unreferenced axiom and this will be returned (in a set).
	 * Note that this axiom is not returned if there exists in the text
	 * another snippet that references it, e.g. if there is a snippet</p>
	 * 
	 * <pre>
	 * [For every man the man is a human.]
	 * [SubClassOf(man human)]
	 * </pre>
	 */

	@Test
	public final void test5() {
		OWLLogicalAxiom every_dog_is_an_animal = Utils.createAxiomEveryDogIsAnAnimal();

		List<ACESentence> sents = ACESplitter.getSentences("Every man is a human and every dog is an animal.");
		ACESnippet s2 = new ACESnippetImpl(Utils.ID_TEST, sents);

		ACEText<OWLEntity, OWLLogicalAxiom> acetext = new ACETextImpl();
		acetext.add(new ACESnippetImpl(Utils.ID_TEST, "If there is a dog then the dog is an animal.", every_dog_is_an_animal));
		acetext.add(s2);

		// We now remove an axiom that both snippets share.
		Set<OWLLogicalAxiom> removedAxioms = acetext.removeAxiom(every_dog_is_an_animal);

		// The result should be that both snippets are removed and the text
		// becomes empty, but the
		// axiom every_man_is_a_human is returned, so that it can be added back.

		assertEquals(acetext.size(), 0);
		assertEquals(removedAxioms, Sets.newHashSet(every_man_is_a_human));
	}
}