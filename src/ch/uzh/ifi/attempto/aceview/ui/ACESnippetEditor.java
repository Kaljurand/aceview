package ch.uzh.ifi.attempto.aceview.ui;

import javax.swing.JTextArea;

import ch.uzh.ifi.attempto.aceview.lexicon.Autocompleter;

public class ACESnippetEditor extends JTextArea {

	private SnippetAutocompleter snippetAutocompleter;

	public ACESnippetEditor() {
		init();
	}

	public ACESnippetEditor(int rows, int columns) {
		super(rows, columns);
		init();
	}


	public void setAutocompleter(Autocompleter ac) {
		if (snippetAutocompleter == null) {
			snippetAutocompleter = new SnippetAutocompleter(this, ac);
		}
		else {
			snippetAutocompleter.setAutocompleter(ac);
		}
	}

	private void init() {
		setEnabled(true);
		setEditable(true);
		setTabSize(2);
		setLineWrap(true);
		setWrapStyleWord(true);		
	}
}