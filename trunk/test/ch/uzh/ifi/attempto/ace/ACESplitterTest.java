package ch.uzh.ifi.attempto.ace;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class ACESplitterTest {

	@Test
	public final void testParagraphSplit0() {
		String str = "John likes Mary.\n\nMary likes Bill.\n\nBill likes Ann.";

		assertEquals("[[John likes Mary.], [Mary likes Bill.], [Bill likes Ann.]]",
				ACESplitter.getParagraphs(str).toString());
	}

	@Test
	public final void testParagraphSplit1() {
		String str = "John likes Mary.";

		assertEquals("[[John likes Mary.]]",
				ACESplitter.getParagraphs(str).toString());
	}

	@Test
	public final void testParagraphSplit2() {
		String str = "John likes Mary. Mary likes Bill.";

		assertEquals("[[John likes Mary., Mary likes Bill.]]",
				ACESplitter.getParagraphs(str).toString());
	}

	@Test
	public final void testParagraphSplit3() {
		String str = "John likes Mary.\n\nMary likes Bill.";

		assertEquals("[[John likes Mary.], [Mary likes Bill.]]",
				ACESplitter.getParagraphs(str).toString());
	}

	@Test
	public final void testParagraphSplit4() {
		String str = "\n \n o o o o ? o o o . \n\n o o . \n\n o \n\n ? o . \n\n o . .\n\n";

		assertEquals("[[o o o o?, o o o.], [o o.], [o.], [?, o.], [o., .]]",
				ACESplitter.getParagraphs(str).toString());
	}
}