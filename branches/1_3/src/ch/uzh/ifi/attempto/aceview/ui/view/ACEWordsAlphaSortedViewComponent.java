package ch.uzh.ifi.attempto.aceview.ui.view;

import java.awt.BorderLayout;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.swing.JEditorPane;
import javax.swing.JScrollPane;

import org.protege.editor.owl.model.event.OWLModelManagerChangeEvent;
import org.protege.editor.owl.model.event.OWLModelManagerListener;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLObject;

import ch.uzh.ifi.attempto.aceview.ACEText;
import ch.uzh.ifi.attempto.aceview.ACETextManager;
import ch.uzh.ifi.attempto.aceview.WordsHyperlinkListener;
import ch.uzh.ifi.attempto.aceview.lexicon.LexiconUtils;
import ch.uzh.ifi.attempto.aceview.model.event.ACEViewEvent;
import ch.uzh.ifi.attempto.aceview.model.event.ACEViewListener;
import ch.uzh.ifi.attempto.aceview.model.event.TextEventType;
import ch.uzh.ifi.attempto.aceview.util.EntityComparator;
import ch.uzh.ifi.attempto.aceview.util.Showing;

/**
 * <p>This view shows all the entities alphabetically sorted and
 * with usage information in the parentheses. Entities which
 * are expressed by function words in ACE sentences (e.g. owl:Thing)
 * are not shown.</p>
 * 
 * <p>This view respects the renderer settings (e.g. shows annotations instead
 * of entity names if the renderer is set this way).</p>
 * 
 * @author Kaarel Kaljurand
 *
 */
public class ACEWordsAlphaSortedViewComponent extends AbstractACEViewComponent {

	private JEditorPane editorpaneWords;
	private WordsHyperlinkListener wordsHyperlinkListener;

	private final OWLModelManagerListener modelManagerListener = new OWLModelManagerListener() {
		public void handleChange(OWLModelManagerChangeEvent event) {
			if (event.isType(org.protege.editor.owl.model.event.EventType.ENTITY_RENDERER_CHANGED)) {
				editorpaneWords.setText(ACETextManager.wrapInHtml(getContentWordsInHtml(ACETextManager.getActiveACEText())));
			}
		}
	};

	private final ACEViewListener<ACEViewEvent<TextEventType>> aceTextManagerListener = new ACEViewListener<ACEViewEvent<TextEventType>>() {
		public void handleChange(ACEViewEvent<TextEventType> event) {
			showWords();
		}
	};

	@Override
	public void disposeView() {
		getOWLModelManager().removeListener(modelManagerListener);
		ACETextManager.removeListener(aceTextManagerListener);
		removeHierarchyListener(hierarchyListener);
		editorpaneWords.removeHyperlinkListener(wordsHyperlinkListener);
	}

	@Override
	public void initialiseView() throws Exception {

		editorpaneWords = new JEditorPane("text/html", ACETextManager.wrapInHtml(""));
		editorpaneWords.setEnabled(true);
		editorpaneWords.setEditable(false);
		wordsHyperlinkListener = new WordsHyperlinkListener(getOWLWorkspace());
		editorpaneWords.addHyperlinkListener(wordsHyperlinkListener);

		JScrollPane scrollpaneWords = new JScrollPane(editorpaneWords,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		setLayout(new BorderLayout());
		add(scrollpaneWords);

		ACETextManager.addListener(aceTextManagerListener);
		addHierarchyListener(hierarchyListener);
		getOWLModelManager().addListener(modelManagerListener);
		showWords();
	}


	private void showWords() {
		ACEText<OWLEntity, OWLLogicalAxiom> acetext = ACETextManager.getActiveACEText();
		int contentWordCount = acetext.getReferencedEntities().size();
		int sentenceCount = acetext.getSentences().size();
		String pl1 = "";
		String pl2 = "";
		if (contentWordCount > 1) {
			pl1 = "s";
		}
		if (sentenceCount > 1) {
			pl2 = "s";
		}
		getView().setHeaderText(contentWordCount + " content word" + pl1 + " in " + sentenceCount + " sentence" + pl2);
		if (contentWordCount > 0) {
			editorpaneWords.setText(ACETextManager.wrapInHtml(getContentWordsInHtml(acetext)));
		}
		else {
			editorpaneWords.setText(ACETextManager.wrapInHtml(""));
		}
	}

	@Override
	protected OWLObject updateView() {
		OWLEntity entity = getOWLWorkspace().getOWLSelectionModel().getSelectedEntity();
		if (isShowing() && entity != null && Showing.isShow(entity)) {
			String entityRendering = getOWLModelManager().getRendering(entity);
			editorpaneWords.scrollToReference(entityRendering);
		}
		return entity;
	}


	@Override
	public void refreshComponent() {
		if (editorpaneWords != null) {
			editorpaneWords.setFont(owlRendererPreferences.getFont());
		}
	}

	/**
	 * Returns an HTML-formatted list of contentwords in this <code>ACEText</code>.
	 * The list is alphabetically sorted.
	 * 
	 * BUG: documentation is out-of-date
	 * 
	 * @return HTML-rendering of all contentwords in the text
	 */
	private String getContentWordsInHtml(ACEText<OWLEntity, OWLLogicalAxiom> acetext) {
		Set<OWLEntity> entities = getOWLModelManager().getActiveOntology().getSignature();
		SortedSet<OWLEntity> entitiesSorted = new TreeSet<OWLEntity>(new EntityComparator());
		entitiesSorted.addAll(entities);

		StringBuilder html = new StringBuilder();
		char previousFirstChar = ' '; // No word can contain a space as the first character
		for (OWLEntity entity : entitiesSorted) {
			if (! Showing.isShow(entity)) {
				continue;
			}
			String entityRendering = ACETextManager.getRendering(entity);
			char currentFirstChar = Character.toLowerCase(entityRendering.charAt(0));
			if (currentFirstChar != previousFirstChar) {
				html.append("<br><br><code>");
				html.append(Character.toUpperCase(currentFirstChar));
				html.append("</code>&nbsp;&nbsp;");
				previousFirstChar = currentFirstChar;
			}
			else {
				html.append(", ");
			}
			String hrefId = LexiconUtils.getHrefId(entity);
			html.append("<a name='");
			html.append(hrefId);
			html.append("'></a><a href='#");
			html.append(hrefId);
			html.append("'>");
			html.append(entityRendering);
			html.append("</a>&nbsp;(");
			html.append(acetext.getSnippetCount(entity));
			html.append(")");
		}
		return html.toString();
	}
}