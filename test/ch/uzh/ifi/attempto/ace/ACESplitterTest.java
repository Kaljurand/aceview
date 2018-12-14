package ch.uzh.ifi.attempto.ace;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.google.common.collect.Lists;

public class ACESplitterTest {

	@Test
	public final void testGetParagraphs0() {
		String str = "John's age is 15.\nMary's 3.0 age 3.2 is 14.John waits 4.";

		assertEquals("[[John's age is 15., Mary's 3 age 3.2 is 14., John waits 4.]]",
				ACESplitter.getParagraphs(str).toString());
	}

	@Test
	public final void testGetParagraphs1() {
		String str = "John's age is 15. Mary's address is \"Paris 100.\".";

		// BUG: actually should not prefix a dot with a space if inside quoted string
		assertEquals("[[John's age is 15., Mary's address is \"Paris 100 .\".]]",
				ACESplitter.getParagraphs(str).toString());
	}

	@Test
	public final void testGetParagraphs2() {
		String str = "John likes Mary.\n\nMary likes Bill.\n\nBill likes Ann.";

		assertEquals("[[John likes Mary.], [Mary likes Bill.], [Bill likes Ann.]]",
				ACESplitter.getParagraphs(str).toString());
	}

	@Test
	public final void testGetParagraphs3() {
		String str = "John likes Mary.";

		assertEquals("[[John likes Mary.]]",
				ACESplitter.getParagraphs(str).toString());
	}

	@Test
	public final void testGetParagraphs4() {
		String str = "John likes Mary. Mary likes Bill.";

		assertEquals("[[John likes Mary., Mary likes Bill.]]",
				ACESplitter.getParagraphs(str).toString());
	}

	@Test
	public final void testGetParagraphs5() {
		String str = "John likes Mary.\n\nMary likes Bill.";

		assertEquals("[[John likes Mary.], [Mary likes Bill.]]",
				ACESplitter.getParagraphs(str).toString());
	}

	@Test
	public final void testGetParagraphs6() {
		String str = "\n \n o o o o ? o o 1. \n\n o o . \n\n o \n\n ? o . \n\n o . .\n\n";

		assertEquals("[[o o o o?, o o 1.], [o o.], [o.], [?, o.], [o., .]]",
				ACESplitter.getParagraphs(str).toString());
	}


	@Test
	public final void testTokenize0() {
		String str = "Everybody ;owns \"quoted string\"," +
		"and does-not v:like John.John's $dog /*likes*/ kno_ws 2.1.1+1=2-3(4.01*8/6.6)#,comment,";

		assertEquals("[Everybody ; owns \"quoted string\", and does-not v: like John., John's $dog kno_ws 2.1., 1+ 1= 2 -3( 4.01* 8/ 6.6).]",
				ACESplitter.getSentences(str).toString());
	}


	@Test
	public final void testTokenize1() {
		assertEquals("[%, ;, @, \\, ^, :, |, ~, .]",
				ACESplitter.getTokens("% ; @ \\ ^ : | ~").toString());
	}


	@Test
	public final void testTokenize2() {
		assertEquals(
				Lists.newArrayList(
						ACEToken.newNumber(123),
						ACEToken.newToken("man"),
						ACEToken.newToken("man123"),
						ACEToken.newToken("äöüp"),
						ACEToken.newNumber(123),
						ACEToken.newToken("man123"),
						ACEToken.newToken("man123man"),
						ACEToken.newQuotedString("äöüpüp"),
						ACEToken.newToken("Tom's Diner"),
						ACEToken.DOT
				),
				ACESplitter.getTokens("123man man123 äöüp 123man123 man123man \"äöüpüp\" `Tom's Diner`"));
	}

	@Test
	public final void testTokenize3() {
		assertEquals(
				Lists.newArrayList(
						ACEToken.newNumber(1),
						ACEToken.newNumber(12),
						ACEToken.newNumber(0),
						ACEToken.newNumber(-1),
						ACEToken.newNumber(-12),
						ACEToken.newNumber(0),
						ACEToken.newNumber(1.2),
						ACEToken.newNumber(0.2),
						ACEToken.newNumber(-1.2),
						ACEToken.newNumber(-0.2),
						ACEToken.newNumber(-0.01),
						ACEToken.newNumber(0),
						ACEToken.DOT
				),
				ACESplitter.getTokens("1 12 0 -1 -12 -0 1.2 0.2 -1.2 -0.2 -.01 -.0 .1"));
	}
}