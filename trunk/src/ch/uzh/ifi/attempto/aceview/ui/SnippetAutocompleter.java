package ch.uzh.ifi.attempto.aceview.ui;

import org.apache.log4j.Logger;
import org.protege.editor.core.ui.util.ComponentFactory;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;

import ch.uzh.ifi.attempto.aceview.lexicon.Autocompleter;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Point;
import java.awt.Window;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

/**
 * <p>GUI for auto-completing ACE snippets.</p>
 * 
 * <p>This is a modified version of the Protege 4 auto-completer.</p>
 *
 */
public class SnippetAutocompleter {

	private static final Logger logger = Logger.getLogger(SnippetAutocompleter.class);

	private static final int DEFAULT_MAX_ENTRIES = 100;

	private static final int POPUP_WIDTH = 250;

	private static final int POPUP_HEIGHT = 300;

	// List of symbols that in ACE define a token border.
	// BUG: See the discussion in an email from 2008-08-13.
	private static final ImmutableSet<String> wordDelimeters = ImmutableSet.of(
			" ", "\n", "!", "%", "&", "'", "(", ")", "*", "+", ",", ".", "/",
			":", ";", "<", "=", ">", "?", "@", "[", "\\", "]", "^", "`", "{", "|", "~");

	private JTextComponent textComponent;

	private JList popupList;

	private JWindow popupWindow;

	private Autocompleter ac;

	private String lastTextUpdate = "*";

	private int maxEntries = DEFAULT_MAX_ENTRIES;

	private KeyListener keyListener = new KeyAdapter() {
		@Override
		public void keyPressed(KeyEvent e) {
			processKeyPressed(e);
		}

		@Override
		public void keyReleased(KeyEvent e) {

			if (e.getKeyCode() != KeyEvent.VK_UP && e.getKeyCode() != KeyEvent.VK_DOWN) {
				if (popupWindow.isVisible() && !lastTextUpdate.equals(textComponent.getText())) {
					lastTextUpdate = textComponent.getText();
					updatePopup(getMatches());
				}
			}
		}
	};

	private ComponentAdapter componentListener = new ComponentAdapter() {
		@Override
		public void componentHidden(ComponentEvent event) {
			hidePopup();
		}

		@Override
		public void componentResized(ComponentEvent event) {
			hidePopup();
		}

		@Override
		public void componentMoved(ComponentEvent event) {
			hidePopup();
		}
	};

	private HierarchyListener hierarchyListener = new HierarchyListener() {
		/**
		 * Called when the hierarchy has been changed. To discern the actual
		 * type of change, call <code>HierarchyEvent.getChangeFlags()</code>.
		 * @see java.awt.event.HierarchyEvent#getChangeFlags()
		 */
		public void hierarchyChanged(HierarchyEvent e) {
			if ((e.getChangeFlags() & HierarchyEvent.PARENT_CHANGED) != 0) {
				createPopupWindow();
				Container frame = textComponent.getTopLevelAncestor();
				if (frame != null){
					frame.addComponentListener(componentListener);
				}
			}
		}
	};

	private MouseListener mouseListener = new MouseAdapter() {
		@Override
		public void mouseClicked(MouseEvent e) {
			if (e.getClickCount() == 2) {
				completeWithPopupSelection();
			}
		}
	};

	private FocusListener focusListener = new FocusAdapter() {
		@Override
		public void focusLost(FocusEvent event) {
			hidePopup();
		}
	};


	public SnippetAutocompleter(JTextComponent tc, Autocompleter ac) {
		this.textComponent = tc;
		this.ac = ac;

		popupList = new JList();
		popupList.setAutoscrolls(true);
		popupList.addMouseListener(mouseListener);
		popupList.setRequestFocusEnabled(false);

		textComponent.addKeyListener(keyListener);

		textComponent.addHierarchyListener(hierarchyListener);

		// moving or resizing the text component or dialog closes the popup
		textComponent.addComponentListener(componentListener);

		// switching focus to another component closes the popup
		textComponent.addFocusListener(focusListener);

		createPopupWindow();
	}


	public void setAutocompleter(Autocompleter ac) {
		this.ac = ac;
	}


	public void cancel(){
		hidePopup();
	}


	public void uninstall() {
		hidePopup();
		textComponent.removeKeyListener(keyListener);
		textComponent.removeComponentListener(componentListener);
		textComponent.removeFocusListener(focusListener);
		textComponent.removeHierarchyListener(hierarchyListener);
	}


	private void processKeyPressed(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_SPACE && e.isControlDown()) {
			// Show popup
			performAutoCompletion();
		}
		else if (e.getKeyCode() == KeyEvent.VK_TAB) {
			e.consume();
			performAutoCompletion();
		}
		else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
			if (popupWindow.isVisible()) {
				// Hide popup
				e.consume();
				hidePopup();
			}
		}
		else if (e.getKeyCode() == KeyEvent.VK_ENTER) {
			if (popupWindow.isVisible()) {
				// Complete
				e.consume();
				completeWithPopupSelection();
			}
		}
		else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
			if (popupWindow.isVisible()) {
				e.consume();
				incrementSelection();
			}
		}
		else if (e.getKeyCode() == KeyEvent.VK_UP) {
			if (popupWindow.isVisible()) {
				e.consume();
				decrementSelection();
			}
		}
		else if (e.getKeyCode() == KeyEvent.VK_LEFT) {
			hidePopup();
		}
		else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
			hidePopup();
		}
	}


	private void completeWithPopupSelection() {
		if (popupWindow.isVisible()) {
			Object selObject = popupList.getSelectedValue();
			if (selObject != null) {
				insertWord(getInsertText(selObject));
				hidePopup();
			}
		}
	}


	private ImmutableSortedSet<String> getMatches() {
		int wordIndex = getWordIndex();
		if (wordIndex > -1) {
			String word = getWordToComplete();
			return ac.getCandidates(word, maxEntries);
		}
		return ImmutableSortedSet.of();
	}


	private void createPopupWindow() {
		JScrollPane sp = ComponentFactory.createScrollPane(popupList);
		popupWindow = new JWindow((Window) SwingUtilities.getAncestorOfClass(Window.class, textComponent));
		// popupWindow.setAlwaysOnTop(true);
		popupWindow.getContentPane().setLayout(new BorderLayout());
		popupWindow.getContentPane().add(sp, BorderLayout.CENTER);
		popupWindow.setFocusableWindowState(false);
	}


	private void performAutoCompletion() {
		ImmutableSortedSet<String> matches = getMatches();
		if (matches.size() == 1) {
			// Don't show popup
			insertWord(getInsertText(matches.iterator().next()));
		}
		else if (matches.size() > 1) {
			// Show popup
			lastTextUpdate = textComponent.getText();

			// BUG: added by Kaarel
			int wordIndex = getWordIndex();
			if (wordIndex > -1) {
				String word = getWordToComplete();
				String completion = ac.complete(word);
				if (completion != null) {
					insertWord(getInsertText(completion));
				}
			}

			showPopup();
			updatePopup(matches);
		}
	}


	private void insertWord(String word) {
		try {
			// remove any currently selected text --- this is the default behavior
			// of the editor when typing manually
			int selStart = textComponent.getSelectionStart();
			int selEnd = textComponent.getSelectionEnd();
			int selLen = selEnd - selStart;
			if (selLen > 0){
				System.out.println("removing selection: " + selLen);
				textComponent.getDocument().remove(selStart, selLen);
			}

			int index = getWordIndex();
			int caretIndex = textComponent.getCaretPosition();
			textComponent.getDocument().remove(index, caretIndex - index);
			textComponent.getDocument().insertString(index, word, null);
		}
		catch (BadLocationException e) {
			logger.error(e);
		}
	}


	private void showPopup() {
		if (popupWindow == null) {
			createPopupWindow();
		}
		if (!popupWindow.isVisible()) {
			popupWindow.setSize(POPUP_WIDTH, POPUP_HEIGHT);
			try {
				int wordIndex = getWordIndex();
				if (wordIndex < 0) {
					return;
				}
				Point p = textComponent.modelToView(getWordIndex()).getLocation();
				SwingUtilities.convertPointToScreen(p, textComponent);
				p.y = p.y + textComponent.getFontMetrics(textComponent.getFont()).getHeight();
				popupWindow.setLocation(p);
			}
			catch (BadLocationException e) {
				e.printStackTrace();
			}
			popupWindow.setVisible(true);
		}
	}


	private void hidePopup() {
		popupWindow.setVisible(false);
		popupList.setListData(new Object [0]);
	}


	private void updatePopup(ImmutableSortedSet<String> matches) {
		popupList.setListData(matches.toArray());
		popupList.setSelectedIndex(0);
		popupWindow.setSize(POPUP_WIDTH, POPUP_HEIGHT);
	}


	private void incrementSelection() {
		if (popupList.getModel().getSize() > 0) {
			int selIndex = popupList.getSelectedIndex();
			selIndex++;
			if (selIndex > popupList.getModel().getSize() - 1) {
				selIndex = 0;
			}
			popupList.setSelectedIndex(selIndex);
			popupList.scrollRectToVisible(popupList.getCellBounds(selIndex, selIndex));
		}
	}


	private void decrementSelection() {
		if (popupList.getModel().getSize() > 0) {
			int selIndex = popupList.getSelectedIndex();
			selIndex--;
			if (selIndex < 0) {
				selIndex = popupList.getModel().getSize() - 1;
			}
			popupList.setSelectedIndex(selIndex);
			popupList.scrollRectToVisible(popupList.getCellBounds(selIndex, selIndex));
		}
	}


	private int getWordIndex() {
		try {
			int caretPos = getEffectiveCaretPosition() - 1;
			for (int index = caretPos; index > -1; index--) {
				if (wordDelimeters.contains(textComponent.getDocument().getText(index, 1))) {
					return index + 1;
				}
				if (index == 0) {
					return 0;
				}
			}
		}
		catch (BadLocationException e) {
			logger.error(e);
		}
		return -1;
	}

	private String getInsertText(Object o) {
		return o.toString();
	}

	private String getWordToComplete() {
		try {
			int index = getWordIndex();
			int caretIndex = getEffectiveCaretPosition();
			return textComponent.getDocument().getText(index, caretIndex - index);
		}
		catch (BadLocationException e) {
			return "";
		}
	}

	// the caret position should be read as the start of the selection if there is one
	private int getEffectiveCaretPosition(){
		int startSel = textComponent.getSelectionStart();
		if (startSel >= 0){
			return startSel;
		}
		return textComponent.getCaretPosition();
	}
}
