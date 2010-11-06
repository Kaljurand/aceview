package ch.uzh.ifi.attempto.aceview.util;

import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.semanticweb.owlapi.model.OWLEntity;

import ch.uzh.ifi.attempto.aceview.ACESnippet;
import ch.uzh.ifi.attempto.aceview.ACEText;
import ch.uzh.ifi.attempto.aceview.ACETextManager;
import ch.uzh.ifi.attempto.aceview.lexicon.LexiconUtils;
import ch.uzh.ifi.attempto.aceview.lexicon.TokenMapper;

public class ACETextRenderer {

	/**
	 * <p>Returns the index-style HTML-rendering of this text.</p>
	 * 
	 * @param acetext
	 * @param lexicon
	 * @return HTML-rendering as string
	 */
	public static String getIndexBody(ACEText<OWLEntity, ?> acetext, TokenMapper lexicon) {
		StringBuilder html = new StringBuilder();
		for (Map.Entry<OWLEntity, Set<ACESnippet>> entry : acetext.getEntitySnippetSetPairs()) {
			OWLEntity entity = entry.getKey();
			String entityRendering = ACETextManager.getRendering(entity);
			Set<ACESnippet> snippets = entry.getValue();
			html.append("<p><strong><a name='");
			html.append(LexiconUtils.getHrefId(entity));
			html.append("'>");
			html.append(entityRendering);
			html.append("</a></strong> (");
			html.append(snippets.size());
			html.append(")</p>\n");
			html.append(snippetsToHtml(snippets, lexicon));
		}
		return html.toString();
	}


	/**
	 * <p>Returns the HTML rendering of the snippets that
	 * reference the given entity.</p>
	 * 
	 * @param acetext
	 * @param entity OWL entity
	 * @param lexicon
	 * @return HTML-rendering as string
	 */
	public static String getIndexEntry(ACEText<OWLEntity, ?> acetext, OWLEntity entity, TokenMapper lexicon) {
		SortedSet<ACESnippet> snippetsSorted = new TreeSet<ACESnippet>(new SnippetComparator());
		snippetsSorted.addAll(acetext.getSnippets(entity));
		return snippetsToHtml(snippetsSorted, lexicon);
	}


	/**
	 * <p>Generates an HTML list on the basis of a set of snippets.</p>
	 * 
	 * @param snippets Set of ACE snippets
	 * @return <code>String</code> representing an HTML list
	 */
	private static String snippetsToHtml(Set<ACESnippet> snippets, TokenMapper lexicon) {
		StringBuilder sb = new StringBuilder();
		for (ACESnippet snippet : snippets) {
			sb.append("<p>");
			sb.append(snippet.toHtmlString(lexicon));
			sb.append(' ');
			sb.append(snippet.getTags());
			sb.append("</p>");
		}
		return sb.toString();
	}

}
