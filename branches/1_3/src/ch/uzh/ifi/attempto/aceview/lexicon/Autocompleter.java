/*
 * This file is part of ACE View.
 * Copyright 2008-2009, Attempto Group, University of Zurich (see http://attempto.ifi.uzh.ch).
 *
 * ACE View is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 *
 * ACE View is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 * PURPOSE. See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with ACE View.
 * If not, see http://www.gnu.org/licenses/.
 */

package ch.uzh.ifi.attempto.aceview.lexicon;

import com.google.common.collect.ImmutableSortedSet;

import ds.tree.DuplicateKeyException;
import ds.tree.RadixTree;
import ds.tree.RadixTreeImpl;

/**
 * <p>Auto-completer that is based on an implementation of radix trees.</p>
 */
public class Autocompleter {

	private final RadixTree<String> tree = new RadixTreeImpl<String>();

	/**
	 * <p>Registers a single string with the auto-completer.
	 * TODO: Currently we ignore the duplicate key exception, maybe
	 * instead, we should check before if the key exists in the tree.</p>
	 * 
	 * @param str String to be registered
	 */
	public void add(String str) {
		try {
			tree.insert(str, str);
		}
		catch (DuplicateKeyException e) {}
	}


	/**
	 * <p>Registers a collection of strings with the auto-completer.</p>
	 * 
	 * @param entries Collection of strings to be registered
	 */
	/*
	public void addAll(Collection<String> entries) {
		for (String entry : entries) {
			add(entry);
		}
	}
	 */


	/**
	 * <p>Unregisters a single string.</p>
	 * 
	 * @param str String to be unregistered
	 */
	public void remove(String str) {
		tree.delete(str);
	}


	/**
	 * <p>Completes a given string on the basis of the registered strings.
	 * The completion is not necessarily a registered string, in general,
	 * it is a shared prefix of some registered strings. If no registered
	 * string contains the input string as prefix, then the input string
	 * is returned.</p>
	 * 
	 * @param prefix String to be completed
	 * @return Completion of the string
	 */
	public String complete(String prefix) {
		return tree.complete(prefix);
	}


	/**
	 * <p>Returns a list of registered strings that contain the input string
	 * as their prefix. The list is alphabetically ordered.
	 * In case no such strings exist, then an empty list
	 * is returned. Only a fixed amount of results are returned, the amount is
	 * determined by <code>numberOfResults</code>. In case it is set to <code>-1</code>,
	 * then all results are returned.</p>
	 * 
	 * <p>TODO: Why does searchPrefix not return a sorted set/list</p>
	 * <p>TODO: how does the alphabetical ordering deal with non US-ASCII characters?</p>
	 * 
	 * @param prefix String according to which the search is made
	 * @param numberOfResults Number of results to be returned
	 * @return List of registered strings that contain the input string as their prefix
	 */
	public ImmutableSortedSet<String> getCandidates(String prefix, int numberOfResults) {
		ImmutableSortedSet<String> sortedCandidates = ImmutableSortedSet
		.orderedBy(String.CASE_INSENSITIVE_ORDER)
		.addAll(tree.searchPrefix(prefix, numberOfResults))
		.build();
		return sortedCandidates;
		//return ImmutableSortedSet.copyOf(tree.searchPrefix(prefix, numberOfResults));
	}


	/**
	 * @see The documentation of {@link #getCandidates(String,int)}
	 */
	public ImmutableSortedSet<String> getCandidates(String prefix) {
		return getCandidates(prefix, -1);
	}
}