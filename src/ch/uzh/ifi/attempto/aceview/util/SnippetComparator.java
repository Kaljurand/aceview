package ch.uzh.ifi.attempto.aceview.util;

import java.util.Comparator;

import ch.uzh.ifi.attempto.aceview.ACESnippet;


class SnippetComparator implements Comparator<ACESnippet> {
	public int compare(ACESnippet s1, ACESnippet s2) {
		return s1.toString().compareToIgnoreCase(s2.toString());
	}
}