package ch.uzh.ifi.attempto.ace;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class ACESentenceSplitterTest {

	@Test
	public final void testSentenceSplit0() {
		String str = "John's age is 15.\nMary's 3.0 age 3.2 is 14.\nJohn waits 4";

		assertEquals("[John's age is 15., Mary's 3 age 3.2 is 14., John waits 4.]",
				ACESentenceSplitter.splitSentences(str).toString());
	}

	@Test
	public final void testSentenceSplit1() {
		String str = "John's age is 15. Mary's address is \"Paris\".";

		assertEquals("[John's age is 15., Mary's address is \"Paris\".]",
				ACESentenceSplitter.splitSentences(str).toString());
	}
}