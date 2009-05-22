package ch.uzh.ifi.attempto.ace;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.google.common.collect.Lists;

public class ACETokenizerTest {

	@Test
	public final void testTokenize0() {
		String str = "Everybody ;owns \"quoted string\"," +
		"and does-not v:like John.John's $dog /*likes*/ kno_ws 2.1.1+1=2-3(4.01*8/6.6)#,comment,";

		assertEquals("[Everybody, ;, owns, \"quoted string\", ,, and, does-not, v, :, like, John, ., John, ', s, $dog, kno_ws, 2.1, ., 1, +, 1, =, 2, -3, (, 4.01, *, 8, /, 6.6, )]",
				ACETokenizer.tokenize(str).toString());
	}


	@Test
	public final void testTokenize1() {
		assertEquals("[%, ;, @, \\, ^, `, |, ~]",
				ACETokenizer.tokenize("% ; @ \\ ^ ` | ~").toString());
	}


	@Test
	public final void testTokenize2() {
		assertEquals("[John, sees, 42, John, sees, Mary, .]",
				ACETokenizer.tokenize("John sees 42. John sees Mary.").toString());
	}


	@Test
	public final void testTokenize3() {
		assertEquals(
				Lists.newArrayList(
						ACEToken.newNumber(123),
						ACEToken.newToken("man"),
						ACEToken.newToken("man123"),
						ACEToken.newToken("ŠšŸp"),
						ACEToken.newNumber(123),
						ACEToken.newToken("man123"),
						ACEToken.newToken("man123man"),
						ACEToken.newQuotedString("ŠšŸpŸp")),
						ACETokenizer.tokenize("123man man123 ŠšŸp 123man123 man123man \"ŠšŸpŸp\""));
	}

	@Test
	public final void testTokenize4() {
		assertEquals(
				Lists.newArrayList(
						ACEToken.newToken("John"),
						ACEToken.newToken("is"),
						ACEToken.newToken("a"),
						ACEToken.newToken("man"),
						ACEToken.DOT,
						ACEToken.newToken("John"),
						ACEToken.newToken("likes"),
						ACEToken.newToken("Mary"),
						ACEToken.DOT,
						ACEToken.PARA,
						ACEToken.newToken("Mary"),
						ACEToken.newToken("likes"),
						ACEToken.newToken("John"),
						ACEToken.DOT
				),
				ACETokenizer.tokenize("\n\nJohn is a man.\nJohn likes Mary.\n \n \nMary likes John.\n\n")
		);
	}

	@Test
	public final void testTokenize5() {
		assertEquals(
				Lists.newArrayList(
						ACEToken.newToken("John"),
						ACEToken.newToken("is"),
						ACEToken.newToken("a"),
						ACEToken.newToken("man"),
						ACEToken.DOT,
						ACEToken.newToken("John"),
						ACEToken.newToken("likes"),
						ACEToken.newToken("Mary"),
						ACEToken.DOT
				),
				ACETokenizer.tokenize("John is a man.#This is a comment.\nJohn likes Mary.\n \n /*\nMary likes John.\n\n\n*/\n")
		);
	}
}