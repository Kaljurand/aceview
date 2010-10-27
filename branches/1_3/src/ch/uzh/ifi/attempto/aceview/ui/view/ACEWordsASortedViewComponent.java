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
import java.awt.Font;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import org.jdesktop.swingx.JXHyperlink;
import org.protege.editor.owl.model.event.OWLModelManagerChangeEvent;
import org.protege.editor.owl.model.event.OWLModelManagerListener;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLObject;

import ch.uzh.ifi.attempto.aceview.ACEText;
import ch.uzh.ifi.attempto.aceview.ACETextManager;
import ch.uzh.ifi.attempto.aceview.model.event.ACEViewEvent;
import ch.uzh.ifi.attempto.aceview.model.event.ACEViewListener;
import ch.uzh.ifi.attempto.aceview.model.event.TextEventType;
import ch.uzh.ifi.attempto.aceview.ui.Colors;
import ch.uzh.ifi.attempto.aceview.ui.EntityLinkAction;
import ch.uzh.ifi.attempto.aceview.ui.util.ComponentFactory;
import ch.uzh.ifi.attempto.aceview.util.EntityComparator;
import ch.uzh.ifi.attempto.aceview.util.Showing;

/**
 * <p>This view shows all the entities alphabetically sorted and
 * with usage information in the parentheses. Entities which
 * are expressed by function words in ACE sentences (e.g. <code>owl:Thing</code>)
 * are not shown.</p>
 * 
 * <p>This view respects the renderer settings (e.g. shows annotations instead
 * of entity names if the renderer is set this way).</p>
 * 
 * @author Kaarel Kaljurand
 */
public class ACEWordsASortedViewComponent extends AbstractACEViewComponent {

	private JTextPane textpaneWords;

	private final OWLModelManagerListener modelManagerListener = new OWLModelManagerListener() {
		public void handleChange(OWLModelManagerChangeEvent event) {
			if (event.isType(org.protege.editor.owl.model.event.EventType.ENTITY_RENDERER_CHANGED)) {
				textpaneWords.setDocument(getContentWordsAsStyledDocument(ACETextManager.getActiveACEText(), textpaneWords.getFont()));
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
	}

	@Override
	public void initialiseView() throws Exception {
		textpaneWords = new JTextPane() {{
			setEditable(false);
			setBackground(Colors.BG_COLOR);
		}};

		JScrollPane scrollpaneWords = new JScrollPane(textpaneWords,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

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
			textpaneWords.setDocument(getContentWordsAsStyledDocument(acetext, owlRendererPreferences.getFont()));
		}
		else {
			textpaneWords.setDocument(new DefaultStyledDocument());
		}
	}

	@Override
	protected OWLObject updateView() {
		OWLEntity entity = getOWLWorkspace().getOWLSelectionModel().getSelectedEntity();
		/*
		if (isShowing() && entity != null && ACETextManager.isShow(entity)) {
			String entityRendering = getOWLModelManager().getRendering(entity);
			textpaneWords.scrollToReference(entityRendering);
		}
		 */
		return entity;
	}


	@Override
	public void refreshComponent() {
		// TODO: BUG: This has no effect, I guess the whole pane needs
		// to be redrawn
		if (textpaneWords != null) {
			textpaneWords.setFont(owlRendererPreferences.getFont());
		}
	}


	/**
	 * <p>Returns a formatted list of content words in this {@link ACEText}.
	 * The list is alphabetically sorted.</p>
	 * 
	 * @return Styled document of all content words in the given text
	 */
	private StyledDocument getContentWordsAsStyledDocument(ACEText<OWLEntity, OWLLogicalAxiom> acetext, Font font) {
		StyledDocument doc = new DefaultStyledDocument();
		Set<OWLEntity> entities = getOWLModelManager().getActiveOntology().getSignature();
		SortedSet<OWLEntity> entitiesSorted = new TreeSet<OWLEntity>(new EntityComparator());
		entitiesSorted.addAll(entities);

		char previousFirstChar = ' '; // No word can contain a space (as the first character)
		for (OWLEntity entity : entitiesSorted) {
			if (! Showing.isShow(entity)) {
				continue;
			}
			String entityRendering = ACETextManager.getRendering(entity);
			char currentFirstChar = Character.toLowerCase(entityRendering.charAt(0));
			if (currentFirstChar != previousFirstChar) {
				addString(doc, "\n\n" + Character.toUpperCase(currentFirstChar) + "\n");
				//addComponent(doc, ComponentFactory.makeInitialLetter(Character.toUpperCase(currentFirstChar), font));
				previousFirstChar = currentFirstChar;
			}
			else {
				/*
				JLabel label = new JLabel(", ");
				label.setFont(font);
				addComponent(doc, label);
				 */
			}

			addComponent(doc, getHyperlink(entity, font));
			addComponent(doc, ComponentFactory.makeItalicLabel(" (" + acetext.getSnippetCount(entity) + ")  ", font));
		}
		return doc;
	}


	private void addString(StyledDocument doc, String str) {
		try {
			doc.insertString(doc.getLength(), str, null);
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
	}

	private void addComponent(StyledDocument doc, JComponent component) {
		// The component must first be wrapped in a style
		Style style = doc.addStyle("StyleName", null);
		StyleConstants.setComponent(style, component);

		// Insert the component at the end of the text
		try {
			doc.insertString(doc.getLength(), "ignored text", style);
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
	}


	private JXHyperlink getHyperlink(OWLEntity entity, Font font) {
		JXHyperlink link = new JXHyperlink(new EntityLinkAction(getOWLWorkspace(), entity));
		link.setFont(font);
		return link;
	}
}