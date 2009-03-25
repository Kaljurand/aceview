package ch.uzh.ifi.attempto.ace;

import static org.junit.Assert.*;

import org.junit.Test;

public class ACEVerbTest {

	ACEVerb v01 = new ACEVerb("from");
	ACEVerb v02 = new ACEVerb("fall-in-love-with");
	ACEVerb v03 = new ACEVerb("is-near-to");
	ACEVerb v04 = new ACEVerb("directly-modify");
	ACEVerb v05 = new ACEVerb("consist-of");
	ACEVerb v06 = new ACEVerb("has-part");
	ACEVerb v07 = new ACEVerb("wait");
	ACEVerb v08 = new ACEVerb("wait-in");
	ACEVerb v09 = new ACEVerb("wait_in");
	ACEVerb v10 = new ACEVerb("wait+in");
	ACEVerb v11 = new ACEVerb("translate-to");
	ACEVerb v12 = new ACEVerb("directly-modify-in");
	ACEVerb v13 = new ACEVerb("translate--to");
	ACEVerb v14 = new ACEVerb("from-from");
	ACEVerb v15 = new ACEVerb("move-from");
	ACEVerb v16 = new ACEVerb("move-from-into");
	ACEVerb v17 = new ACEVerb("move+up-down_in");
	ACEVerb v18 = new ACEVerb("aa-bbb-in");


	@Test
	public final void testIsPrepositional() {
		assertEquals(true, v01.isPrepositional());
		assertEquals(true, v02.isPrepositional());
		assertEquals(true, v03.isPrepositional());
		assertEquals(false, v04.isPrepositional());
		assertEquals(false, v05.isPrepositional());
		assertEquals(false, v06.isPrepositional());
		assertEquals(false, v07.isPrepositional());
		assertEquals(true, v08.isPrepositional());
		assertEquals(true, v09.isPrepositional());
		assertEquals(true, v10.isPrepositional());
		assertEquals(true, v11.isPrepositional());
		assertEquals(true, v12.isPrepositional());
		assertEquals(true, v13.isPrepositional());
		assertEquals(true, v14.isPrepositional());
		assertEquals(true, v15.isPrepositional());
		assertEquals(true, v16.isPrepositional());
		assertEquals(true, v17.isPrepositional());
		assertEquals(true, v18.isPrepositional());
	}


	@Test
	public final void testGetPreposition() {
		assertEquals("", v01.getPreposition()); // BUG
		assertEquals("with", v02.getPreposition());
		assertEquals("near-to", v03.getPreposition());
		assertEquals("", v04.getPreposition());
		assertEquals("", v05.getPreposition());
		assertEquals("", v06.getPreposition());
		assertEquals("", v07.getPreposition());
		assertEquals("in", v08.getPreposition());
		assertEquals("in", v09.getPreposition());
		assertEquals("in", v10.getPreposition());
		assertEquals("to", v11.getPreposition());
		assertEquals("in", v12.getPreposition());
		assertEquals("to", v13.getPreposition());
		assertEquals("from", v14.getPreposition());
		assertEquals("from", v15.getPreposition());
		assertEquals("from-into", v16.getPreposition());
		assertEquals("up-down_in", v17.getPreposition());
		assertEquals("in", v18.getPreposition());
	}


	@Test
	public final void testGetPresent3SG() {
		assertEquals("froms", v01.getPresent3SG());
		assertEquals("fall-in-loves-with", v02.getPresent3SG()); // BUG
		assertEquals("ises-near-to", v03.getPresent3SG()); // BUG
		assertEquals("directly-modifies", v04.getPresent3SG());
		assertEquals("consist-ofs", v05.getPresent3SG()); // BUG
		assertEquals("has-parts", v06.getPresent3SG());
		assertEquals("waits", v07.getPresent3SG());
		assertEquals("waits-in", v08.getPresent3SG());
		assertEquals("waits_in", v09.getPresent3SG());
		assertEquals("waits+in", v10.getPresent3SG());
		assertEquals("translates-to", v11.getPresent3SG());
		assertEquals("directly-modifies-in", v12.getPresent3SG());
		assertEquals("translate--to", v13.getPresent3SG()); // BUG
		assertEquals("froms-from", v14.getPresent3SG());
		assertEquals("moves-from", v15.getPresent3SG());
		assertEquals("moves-from-into", v16.getPresent3SG());
		assertEquals("moves+up-down_in", v17.getPresent3SG());
		assertEquals("aa-bbbs-in", v18.getPresent3SG());
	}


	@Test
	public final void testGetPastParticiple() {
		assertEquals("fromed", v01.getPastParticiple());
		assertEquals("fall-in-loved-with", v02.getPastParticiple()); // BUG
		assertEquals("ised-near-to", v03.getPastParticiple()); // BUG
		assertEquals("directly-modified", v04.getPastParticiple());
		assertEquals("consist-ofed", v05.getPastParticiple()); // BUG
		assertEquals("has-parted", v06.getPastParticiple());
		assertEquals("waited", v07.getPastParticiple());
		assertEquals("waited-in", v08.getPastParticiple());
		assertEquals("waited_in", v09.getPastParticiple());
		assertEquals("waited+in", v10.getPastParticiple());
		assertEquals("translated-to", v11.getPastParticiple());
		assertEquals("directly-modified-in", v12.getPastParticiple());
		assertEquals("translate--to", v13.getPastParticiple()); // BUG
		assertEquals("fromed-from", v14.getPastParticiple());
		assertEquals("moved-from", v15.getPastParticiple());
		assertEquals("moved-from-into", v16.getPastParticiple());
		assertEquals("moved+up-down_in", v17.getPastParticiple());
		assertEquals("aa-bbbed-in", v18.getPastParticiple());
	}
}