/**
 * 
 */
package ch.uzh.ifi.attempto.ace;

import static org.junit.Assert.*;

import java.util.List;
import java.util.Set;

import org.junit.Test;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

/**
 * @author Kaarel Kaljurand
 *
 */
public class ACESentenceTest {

	String str1 = "Every cat is an [animal] whose address is \"Paris\"and no cat sees 12.";
	String str2 = " Every  cat  is  an [ animal]  whose  address  is\"Paris\"and no cat sees 12 . ";
	String str3 = "John's pet is a cat or knows Jonas' pet, and is an animal.";

	String question = "Which countries does Switzerland border?";

	int str1Size = 17;
	String str1String = "Every cat is an [ animal ] whose address is \"Paris\" and no cat sees 12.";
	List str1ContentWords = Lists.newArrayList(
			ACEToken.newToken("cat"),
			ACEToken.newToken("animal"),
			ACEToken.newToken("address"),
			ACEToken.newToken("cat"),
			ACEToken.newToken("sees"));

	Set str1BadChars = Sets.newHashSet(
			ACEToken.newBadToken('['),
			ACEToken.newBadToken(']'));


	@Test
	public final void testHashCode() {
		assertEquals(new ACESentence(str1).hashCode(), new ACESentence(str1).hashCode());
	}

	@Test
	public final void testSize() {
		assertEquals(str1Size, new ACESentence(str1).size());
	}

	@Test
	public final void testToString1() {
		assertEquals(str1String, new ACESentence(str1).toString());
	}

	@Test
	public final void testToString2() {
		assertEquals(new ACESentence(str1).toString(), new ACESentence(str2).toString());
	}

	@Test
	public final void testToString3() {
		assertEquals(new ACESentence(str3).toString(), str3);
	}

	@Test
	public final void testToSimpleString1() {
		assertEquals(new ACESentence(str1).toSimpleString(), new ACESentence(str2).toSimpleString());
	}

	@Test
	public final void testGetBadChars() {
		assertEquals(str1BadChars, new ACESentence(str1).getBadChars());
	}

	@Test
	public final void testGetContentWords() {
		assertEquals(str1ContentWords, new ACESentence(str1).getContentWords());
	}

	@Test
	public final void testEqualsObject() {
		assertEquals(new ACESentence(str1), new ACESentence(str2));
	}

	@Test
	public final void testIsQuestion() {
		assertEquals(true, new ACESentence(question).isQuestion());
	}

}
