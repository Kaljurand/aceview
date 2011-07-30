/*
 * This file is part of ACE View.
 * Copyright 2008-2010, Attempto Group, University of Zurich (see http://attempto.ifi.uzh.ch).
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

package ch.uzh.ifi.attempto.aceview.ui.view;

import java.awt.BorderLayout;
import java.util.List;
import java.util.Set;

import javax.swing.JEditorPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.jdesktop.swingx.JXPanel;
import org.jdesktop.swingx.VerticalLayout;
import org.jdesktop.swingx.decorator.ColorHighlighter;
import org.jdesktop.swingx.decorator.Highlighter;
import org.protege.editor.owl.model.OWLModelManager;
import org.protege.editor.owl.ui.renderer.OWLRendererPreferences;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import ch.uzh.ifi.attempto.ace.ACESentence;
import ch.uzh.ifi.attempto.ace.ACESentenceRenderer;
import ch.uzh.ifi.attempto.aceview.ACESnippet;
import ch.uzh.ifi.attempto.aceview.ACETextManager;
import ch.uzh.ifi.attempto.aceview.IriRenderer;
import ch.uzh.ifi.attempto.aceview.model.AnnotationsTableModel;
import ch.uzh.ifi.attempto.aceview.model.MessagesTableModel;
import ch.uzh.ifi.attempto.aceview.model.SimilarSnippetsTableModel;
import ch.uzh.ifi.attempto.aceview.predicate.ErrorMessagePredicate;
import ch.uzh.ifi.attempto.aceview.ui.ACESnippetTable;
import ch.uzh.ifi.attempto.aceview.ui.ACETable;
import ch.uzh.ifi.attempto.aceview.ui.Colors;
import ch.uzh.ifi.attempto.aceview.ui.FeedbackPane;
import ch.uzh.ifi.attempto.aceview.ui.util.ComponentFactory;
import ch.uzh.ifi.attempto.aceview.ui.util.TableColumnHelper;
import ch.uzh.ifi.attempto.ape.Message;


/**
 * <p>This view component provides information about
 * error messages, paraphrases, annotations, and corresponding axioms.</p>
 * 
 * @author Kaarel Kaljurand
 */
public class ACEFeedbackViewComponent extends AbstractACESnippetSelectionViewComponent {

	private final OWLRendererPreferences owlRendererPreferences = OWLRendererPreferences.getInstance();

	private static final List<Message> EMPTY_LIST_OF_MESSAGES = Lists.newArrayList();
	private static final Set<ACESnippet> EMPTY_SET_OF_SNIPPETS = Sets.newHashSet();

	private static final String NONE = "None";
	private static final String PANEL_MESSAGES_TITLE = "Messages";
	private static final String PANEL_PARAPHRASES_TITLE = "Paraphrases";
	private static final String PANEL_ANNOTATIONS_TITLE = "Annotations";
	private static final String PANEL_AXIOMS_TITLE = "Corresponding logical axioms";
	private static final String PANEL_SIMILAR_SNIPPETS_TITLE = "Similar snippets";

	private final ACETable listMessages = new ACETable();
	private final JTextArea textareaParaphrase = ComponentFactory.makeSmallTextArea();
	private final ACETable tableAnnotations = new ACETable();
	private final JEditorPane editorpaneAxioms = new JEditorPane("text/html", ACETextManager.wrapInHtml(""));
	// BUG: magic number
	private final ACESnippetTable tableSimilarSnippets = new ACESnippetTable(0);

	private final FeedbackPane panelMessages = new FeedbackPane(listMessages, PANEL_MESSAGES_TITLE);
	private final FeedbackPane panelParaphrase = new FeedbackPane(textareaParaphrase, PANEL_PARAPHRASES_TITLE);
	private final FeedbackPane panelAnnotations = new FeedbackPane(tableAnnotations, PANEL_ANNOTATIONS_TITLE);
	private final FeedbackPane panelAxioms = new FeedbackPane(editorpaneAxioms, PANEL_AXIOMS_TITLE);
	private final FeedbackPane panelSimilarSnippets = new FeedbackPane(tableSimilarSnippets, PANEL_SIMILAR_SNIPPETS_TITLE);

	private final MessagesTableModel messagesTableModel = new MessagesTableModel();
	private final AnnotationsTableModel commentsTableModel = new AnnotationsTableModel();
	private final SimilarSnippetsTableModel similarSnippetsTableModel = new SimilarSnippetsTableModel();

	private final ColorHighlighter errorHighlighter = new ColorHighlighter(new ErrorMessagePredicate(MessagesTableModel.Column.IMPORTANCE.ordinal()));

	private final Joiner paragraphJoiner = Joiner.on("\n\n");

	@Override
	protected void initialiseOWLView() throws Exception {
		listMessages.setModel(messagesTableModel);

		TableColumnHelper.configureColumns(listMessages, MessagesTableModel.Column.values());
		listMessages.getColumnExt(MessagesTableModel.Column.TYPE.getName()).setPreferredWidth(50);
		listMessages.getColumnExt(MessagesTableModel.Column.TOKEN_ID.getName()).setPreferredWidth(30);
		listMessages.getColumnExt(MessagesTableModel.Column.SENTENCE_ID.getName()).setPreferredWidth(30);

		listMessages.setVisibleRowCount(4);
		listMessages.setHighlighters(new Highlighter[] { errorHighlighter });

		errorHighlighter.setBackground(Colors.ERROR_COLOR);

		tableAnnotations.setModel(commentsTableModel);
		TableColumnHelper.configureColumns(tableAnnotations, AnnotationsTableModel.Column.values());
		tableAnnotations.setVisibleRowCount(4);

		editorpaneAxioms.setEnabled(true);
		editorpaneAxioms.setEditable(false);

		tableSimilarSnippets.setModel(similarSnippetsTableModel);
		tableSimilarSnippets.setVisibleRowCount(5);

		// Some panes are collapsed by default.
		// BUG: We should actually remember the user preference.
		panelMessages.setCollapsed(true);
		panelAxioms.setCollapsed(true);
		panelSimilarSnippets.setCollapsed(true);


		// Details box
		JXPanel boxDetails = new JXPanel();
		boxDetails.setLayout(new VerticalLayout());
		boxDetails.add(panelMessages);
		boxDetails.add(panelParaphrase);
		boxDetails.add(panelAnnotations);
		boxDetails.add(panelAxioms);
		boxDetails.add(panelSimilarSnippets);

		JScrollPane scrollpaneDetails = new JScrollPane(boxDetails,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

		setLayout(new BorderLayout());
		add(scrollpaneDetails);
		refreshComponent();
		resetUI();
		ACETextManager.addSnippetListener(getACESnippetListener());
		getOWLWorkspace().getOWLSelectionModel().addListener(getOWLSelectionModelListener());
	}


	private void refreshComponent() {
		if (textareaParaphrase != null) {
			textareaParaphrase.setFont(owlRendererPreferences.getFont());
		}
	}


	@Override
	protected void disposeOWLView() {
		getOWLWorkspace().getOWLSelectionModel().removeListener(getOWLSelectionModelListener());
		ACETextManager.removeSnippetListener(getACESnippetListener());
	}


	@Override
	protected void displaySnippet(ACESnippet snippet) {
		if (isSynchronizing()) {
			if (snippet == null) {
				resetUI();
			}
			else {
				getView().setHeaderText(snippet.toString());

				updateMessages(snippet);

				updateParaphrases(snippet);

				updateAnnotations(snippet);

				updateAxioms(snippet);

				updateSimilarSnippets(snippet);
			}
		}
	}


	private static String renderSnippetAxioms(OWLModelManager mm, ACESnippet snippet) {
		StringBuilder html = new StringBuilder();
		if (snippet.isQuestion()) {
			OWLClassExpression dlquery = snippet.getDLQuery();
			if (dlquery != null) {
				html.append("<pre>");
				html.append(mm.getRendering(dlquery));
				html.append("</pre>");
			}
		}
		else {
			Set<OWLLogicalAxiom> axioms = snippet.getLogicalAxioms();
			for (OWLLogicalAxiom ax : axioms) {
				html.append("<pre>");
				html.append(mm.getRendering(ax));
				html.append("</pre>");
			}
		}
		return html.toString();
	}


	private static int getErrorMessageCount(List<Message> messages) {
		int c = 0;
		for (Message m : messages) {
			if (m.isError()) c++;
		}
		return c;
	}


	/**
	 * <p>Resets the snippet editor UI.</p>
	 */
	private void resetUI() {
		// Header
		getView().setHeaderText("(No snippet selected.)");

		// Messages table
		panelMessages.setTitle(PANEL_MESSAGES_TITLE);
		messagesTableModel.setMessages(EMPTY_LIST_OF_MESSAGES);

		// Paraphrase text-area
		panelParaphrase.setTitle(PANEL_PARAPHRASES_TITLE);
		textareaParaphrase.setText("");

		// Annotations table
		panelAnnotations.setTitle(PANEL_ANNOTATIONS_TITLE);

		// Axioms editor pane
		panelAxioms.setTitle(PANEL_AXIOMS_TITLE);
		editorpaneAxioms.setText(ACETextManager.wrapInHtml(""));

		// Similar snippets table
		panelSimilarSnippets.setTitle(PANEL_SIMILAR_SNIPPETS_TITLE);
		similarSnippetsTableModel.setData(EMPTY_SET_OF_SNIPPETS);
	}


	/**
	 * <p>Updates the messages panel.</p>
	 * 
	 * @param snippet ACE snippet
	 */
	private void updateMessages(ACESnippet snippet) {
		List<Message> messages = snippet.getMessages();
		int errorMessageCount = getErrorMessageCount(messages);
		panelMessages.setTitle(PANEL_MESSAGES_TITLE + ": " +
				"Errors: " + errorMessageCount + "   " +
				"Warnings: " + (messages.size() - errorMessageCount));
		messagesTableModel.setMessages(messages);
	}


	/**
	 * <p>Updates the paraphrases panel.</p>
	 * 
	 * @param snippet ACE snippet
	 */
	private void updateParaphrases(ACESnippet snippet) {
		List<List<ACESentence>> paraphrase = snippet.getParaphrase();
		if (paraphrase.isEmpty()) {
			panelParaphrase.setTitle(PANEL_PARAPHRASES_TITLE + ": " + NONE);
			textareaParaphrase.setText("");
		}
		else {
			panelParaphrase.setTitle(PANEL_PARAPHRASES_TITLE + ": 1");
			List<String> snippetRenderings = Lists.newArrayList();
			for (List<ACESentence> paragraph : paraphrase) {
				ACESentenceRenderer snippetRenderer = new ACESentenceRenderer(new IriRenderer(ACETextManager.getActiveACELexicon()), paragraph);
				snippetRenderings.add(snippetRenderer.getRendering());
			}
			textareaParaphrase.setText(paragraphJoiner.join(snippetRenderings));
		}
	}


	/**
	 * <p>Updates the annotations panel.</p>
	 * 
	 * @param snippet ACE snippet
	 */
	private void updateAnnotations(ACESnippet snippet) {
		List<OWLAnnotation> annotations = ACETextManager.getAnnotations(snippet);
		if (annotations.isEmpty()) {
			panelAnnotations.setTitle(PANEL_ANNOTATIONS_TITLE + ": " + NONE);
		}
		else {
			panelAnnotations.setTitle(PANEL_ANNOTATIONS_TITLE + ": " + annotations.size());
		}
		commentsTableModel.setComments(annotations);
	}


	/**
	 * <p>Updates the axioms panel.</p>
	 * 
	 * @param snippet ACE snippet
	 */
	private void updateAxioms(ACESnippet snippet) {
		String title = PANEL_AXIOMS_TITLE + ": ";
		String axiomsAsHtml = "";
		if (snippet.hasAxioms()) {
			int axiomCountSwrl = snippet.getRules().size();
			int axiomCountOwl = snippet.getLogicalAxioms().size() - axiomCountSwrl;
			title += "OWL: " + axiomCountOwl + "   SWRL: " + axiomCountSwrl;
			axiomsAsHtml = renderSnippetAxioms(getOWLModelManager(), snippet);
		}
		else {
			title += NONE;
		}
		panelAxioms.setTitle(title);
		editorpaneAxioms.setText(ACETextManager.wrapInHtml(axiomsAsHtml));
	}


	/**
	 * <p>Updates the similar snippets panel.</p>
	 * 
	 * <p>Similar snippets are searched in the active ACE text. Another option
	 * would be to search the snippets in the text that contains the
	 * given snippet, but this would not give any results for entailed
	 * snippets.</p>
	 * 
	 * @param snippet ACE snippet
	 */
	private void updateSimilarSnippets(ACESnippet snippet) {
		String title = PANEL_SIMILAR_SNIPPETS_TITLE + ": ";
		Set<ACESnippet> similarSnippets = ACETextManager.getActiveACEText().getSimilarSnippets(snippet);
		if (similarSnippets.isEmpty()) {
			title += NONE;
		}
		else {
			title += similarSnippets.size();
		}
		panelSimilarSnippets.setTitle(title);
		similarSnippetsTableModel.setData(similarSnippets);
	}
}
