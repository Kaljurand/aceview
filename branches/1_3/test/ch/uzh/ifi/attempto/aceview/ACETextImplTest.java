package ch.uzh.ifi.attempto.aceview;

import static org.junit.Assert.assertEquals;

import java.util.Set;

import org.junit.Test;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;

import com.google.common.collect.Sets;


public class ACETextImplTest {

	private static final OWLLogicalAxiom john_likes_mary = Utils.createAxiomJohnLikesMary();
	private static final OWLLogicalAxiom every_man_is_a_human = Utils.createAxiomEveryManIsAHuman();


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
}