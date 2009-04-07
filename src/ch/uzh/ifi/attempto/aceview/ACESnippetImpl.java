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

package ch.uzh.ifi.attempto.aceview;

import java.net.URI;
import java.util.List;
import java.util.Set;

import javax.swing.JOptionPane;

import org.apache.log4j.Logger;
import org.protege.editor.owl.model.description.OWLExpressionParserException;
import org.semanticweb.owl.apibinding.OWLManager;
import org.semanticweb.owl.io.StringInputSource;
import org.semanticweb.owl.model.OWLAxiom;
import org.semanticweb.owl.model.OWLDescription;
import org.semanticweb.owl.model.OWLEntity;
import org.semanticweb.owl.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owl.model.OWLLogicalAxiom;
import org.semanticweb.owl.model.OWLOntologyCreationException;
import org.semanticweb.owl.model.OWLOntologyManager;
import org.semanticweb.owl.model.OWLSubClassAxiom;
import org.semanticweb.owl.model.SWRLRule;

import com.google.common.base.Joiner;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

import ch.uzh.ifi.attempto.ace.ACESentence;
import ch.uzh.ifi.attempto.ace.ACESentenceSplitter;
import ch.uzh.ifi.attempto.ace.ACEToken;
import ch.uzh.ifi.attempto.aceview.lexicon.ACELexicon;
import ch.uzh.ifi.attempto.aceview.lexicon.EntryType;
import ch.uzh.ifi.attempto.aceview.util.SnippetDate;
import ch.uzh.ifi.attempto.ape.ACEParser;
import ch.uzh.ifi.attempto.ape.Lexicon;
import ch.uzh.ifi.attempto.ape.Message;
import ch.uzh.ifi.attempto.ape.MessageContainer;
import ch.uzh.ifi.attempto.ape.OutputType;
import ch.uzh.ifi.attempto.ape.ACEParserResult;

/**
 * <p>Every snippet is mapped to OWL/SWRL already in the constructor.
 * The corresponding OWL/SWRL axiom can be given as an argument in the
 * constructor (this is used by the verbalizer).</p>
 * 
 * @author Kaarel Kaljurand
 */
public class ACESnippetImpl implements ACESnippet {

	private static final Logger logger = Logger.getLogger(ACESnippetImpl.class);

	private final ImmutableList<ACESentence> sentences;
	private final URI ns;
	// Alternative rendering which could be used if this snippet is empty.
	private final String altRendering;
	private final SnippetDate timestamp;
	private final List<Message> messages = Lists.newArrayList();
	private final Multimap<Integer, Integer> pinpointers = HashMultimap.create();

	// BUG: make everything final
	private ImmutableSet<OWLLogicalAxiom> axiomSet = ImmutableSet.of();
	// private final Set<SWRLRule> rules = Sets.newHashSet();
	private List<ACESentence> para1 = Lists.newArrayList();
	private boolean isQuestion = false;
	private int errorMessagesCount = 0;
	private int owlErrorMessagesCount = 0;

	private final Joiner joiner = Joiner.on(" ");

	/**
	 * <p>Constructs an ACE snippet from a list of ACE sentences.</p>
	 * 
	 * @param ns Default namespace of the snippet
	 * @param sentences List of sentences that the snippet contains
	 */
	public ACESnippetImpl(URI ns, List<ACESentence> sentences) {
		if (sentences == null) {
			throw new IllegalArgumentException("Sentences must not be null!");
		}
		this.timestamp = new SnippetDate();
		this.ns = ns;
		this.sentences = ImmutableList.copyOf(sentences);
		if (! this.sentences.isEmpty()) {
			if (this.sentences.get(sentences.size() - 1).isQuestion()) {
				isQuestion = true;
			}
			init();
			countMessages();
		}
		this.altRendering = null;
	}


	/**
	 * <p>Constructs an ACE snippet from a single ACE sentence.</p>
	 * 
	 * @param ns Default namespace of the snippet
	 * @param sentence Sentence that the snippet contains
	 */
	public ACESnippetImpl(URI ns, ACESentence sentence) {
		this.timestamp = new SnippetDate();
		this.ns = ns;
		if (sentence.isQuestion()) {
			isQuestion = true;
		}
		this.sentences = ImmutableList.of(sentence);
		init();
		countMessages();
		this.altRendering = null;
	}


	/**
	 * <p>Constructs an ACE snippet from a string.
	 * The corresponding (single) OWL axiom is given as input,
	 * therefore the snippet is not parsed.</p>
	 * 
	 * @param ns Default namespace of the snippet
	 * @param str Textual content of the snippet
	 * @param axiom OWL axiom that the snippet corresponds to
	 */
	public ACESnippetImpl(URI ns, String str, OWLLogicalAxiom axiom) {
		this.timestamp = new SnippetDate();
		this.ns = ns;
		this.axiomSet = ImmutableSet.of(axiom);
		this.sentences = ImmutableList.copyOf(ACESentenceSplitter.splitSentences(str));
		if (! sentences.isEmpty()) {
			if (sentences.get(sentences.size() - 1).isQuestion()) {
				isQuestion = true;
			}
		}
		this.altRendering = null;
	}


	public ACESnippetImpl(URI ns, String str, OWLLogicalAxiom axiom, String altRendering) {
		this.timestamp = new SnippetDate();
		this.ns = ns;
		this.axiomSet = ImmutableSet.of(axiom);
		this.sentences = ImmutableList.copyOf(ACESentenceSplitter.splitSentences(str));
		if (! sentences.isEmpty()) {
			if (sentences.get(sentences.size() - 1).isQuestion()) {
				isQuestion = true;
			}
		}
		this.altRendering = altRendering;
	}


	public List<ACESentence> getSentences() {
		return sentences;
	}


	public boolean isEmpty() {
		return sentences.isEmpty();
	}


	/**
	 * <p>Parses the text of the snippet with the Manchester OWL Syntax parser;
	 * if this fails then parses with an ACE parser. In the later case,
	 * also optionally paraphrases the snippet.</li>
	 */
	private void init() {
		ACEPreferences prefs = ACEPreferences.getInstance();

		// As possible MOS strings,
		// we only accept snippets which contain exactly one sentence.
		if (prefs.getUseMos() && sentences.size() == 1) {
			OWLLogicalAxiom mosAxiom = null;
			try {
				mosAxiom = ACETextManager.parseWithMos(sentences.iterator().next());
			} catch (OWLExpressionParserException e) {
				// Note: we are silent about using ManchesterSyntaxParser, i.e.
				// we do not return the syntax errors.
			}
			if (mosAxiom != null) {
				axiomSet = ImmutableSet.of(mosAxiom);
				return;
			}
		}


		try {
			parse(prefs);
		} catch (OWLOntologyCreationException e) {
			e.printStackTrace();
		}
	}


	public Set<OWLLogicalAxiom> getLogicalAxioms() {
		return axiomSet;
	}


	// TODO: construct this at construction time
	public Set<SWRLRule> getRules() {
		Set<SWRLRule> rules = Sets.newHashSet();
		for (OWLLogicalAxiom ax : axiomSet) {
			if (ax instanceof SWRLRule) {
				rules.add((SWRLRule) ax);
			}
		}
		return rules;
	}


	private void parse(ACEPreferences prefs) throws OWLOntologyCreationException {
		ACELexicon<OWLEntity> aceLexicon = ACETextManager.getActiveACELexicon();
		Set<String> contentWordForms = getContentWordsAsStrings();

		if (! prefs.getParseWithUndefinedTokens()) {
			for (String contentWordForm : contentWordForms) {
				if (! aceLexicon.containsWordform(contentWordForm)) {
					messages.add(new Message("error", "token", null, null, contentWordForm, "Add this wordform to the lexicon"));
				}
			}
		}

		// In case there are no lexical entries for at least one token, and the preferences
		// tell us not to parse in this case, then abort immediately.
		if (messages.isEmpty()) {
			parseWithAceParser(prefs, aceLexicon, contentWordForms);
		}
	}


	private void parseWithAceParser(ACEPreferences prefs, ACELexicon<OWLEntity> aceLexicon, Set<String> contentWordForms)
	throws OWLOntologyCreationException {
		Lexicon lexicon = aceLexicon.createLexicon(contentWordForms);

		if (lexicon.getEntries().isEmpty()) {
			logger.info("Parsing with empty lexicon.");
			lexicon = null;
		}
		else {
			logger.info("Parsing with lexicon:\n" + lexicon.toString());
		}

		boolean paraphrase1Enabled = prefs.isParaphrase1Enabled();

		ACEParser parser = ParserHolder.getACEParser();
		parser.setURI(ns.toString());

		ACEParserResult result = null;

		try {
			if (paraphrase1Enabled) {
				result = parser.getMultiOutput(toSimpleString(), lexicon, OutputType.PARAPHRASE1, OutputType.OWLXML);
			}
			else {
				result = parser.getMultiOutput(toSimpleString(), lexicon, OutputType.OWLXML);
			}
		} catch (RuntimeException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "ACE Parser error:\n" + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
		}

		if (result != null) {
			MessageContainer messageContainer = result.getMessageContainer();
			messages.addAll(messageContainer.getMessages());

			List<Message> errorMessages = messageContainer.getErrorMessages();

			if (errorMessages.isEmpty()) {
				if (paraphrase1Enabled) {
					para1 = ACESentenceSplitter.splitSentences(result.get(OutputType.PARAPHRASE1));
				}
				String owlxml = result.get(OutputType.OWLXML);
				if (owlxml == null || owlxml.length() == 0) {
					throw new OWLOntologyCreationException("get(OutputType.OWLXML) is null or empty");
				}
				// TODO: BUG: creating a new ontology manager just to parse a snippet
				// might be bad for performance
				OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
				// BUG: we should copy the axioms and then throw away the created ontology
				axiomSet = ImmutableSet.copyOf(manager.loadOntology(new StringInputSource(owlxml)).getLogicalAxioms());
			}
			else {
				setPinpointers(errorMessages);
			}
		}
	}


	private void setPinpointers(List<Message> errorMessages) {
		for (Message m : errorMessages) {
			Integer sid = m.getSentenceId();
			Integer tid = m.getTokenId();

			// TODO: do not check for <1 here, fix the messages instead
			if (sid == null || tid == null || sid < 1 || tid < 1) {
				continue;
			}

			int sentenceIndex = sid.intValue() - 1;
			int tokenIndex = tid.intValue() - 1;
			if (sentences.size () > sentenceIndex && sentences.get(sentenceIndex).size() > tokenIndex) {
				pinpointers.put(sentenceIndex, tokenIndex);
			}
			else {
				logger.error("Pinpointer out of bounds: " + sentenceIndex + "-" + tokenIndex);
			}
		}
	}


	@Override
	public String toString() {		
		if (isEmpty()) {
			if (altRendering == null) {
				return "/*" + getLogicalAxioms().toString() + "*/";
			}
			// The Manchester Syntax rendering contains layout symbols,
			// which we convert into a single space.
			return "/* MOS: " + altRendering.replaceAll("[ \t\n\f\b\r]+", " ") + " */";
		}
		return joiner.join(sentences);
	}


	// BUG: What is the purpose of this method?
	private String toSimpleString() {
		if (sentences.size() == 1) {
			return sentences.iterator().next().toSimpleString();
		}

		String str = "";
		for (ACESentence s : sentences) {
			str += s.toSimpleString() + " ";
		}
		return str;
	}


	public String toHtmlString() {

		if (isEmpty() && altRendering != null) {
			return "<span color='red'>" + altRendering + "</span>";
		}

		ACELexicon<OWLEntity> aceLexicon = ACETextManager.getActiveACELexicon();
		String span = "";

		for (ACESentence sentence : sentences) {
			for (ACEToken token : sentence.getTokens()) {
				if (token.isOrdinationWord()) {
					span += "<span color='green'>" + token + "</span>";				
				}
				else if (token.isQuotedString() || token.isNumber()) {
					span += "<i>" + token + "</i>";
				}
				else if (token.isFunctionWord()) {
					span += token;
				}
				else if (token.isBadToken()) {
					span += "<span color='red'>" + token + "</span>";
				}
				else {
					Set<OWLEntity> entitySet = aceLexicon.getWordformEntities(token.toString());
					if (entitySet.isEmpty()) {
						span += "<span color='#777777'>" + token + "</span>";
					}
					else {
						// TODO: BUG: in case a wordform maps to multiple different entities then
						// we just take the first. This shouldn't occur often though.
						OWLEntity firstEntity = entitySet.iterator().next();
						EntryType type = ACETextManager.getLexiconEntryType(firstEntity);
						span += "<a href='#" + type + ":" + firstEntity.toString() + "'>" + token + "</a>";
					}
				}
				span += " ";
			}
		}
		return span;
	}


	// TODO: Do this at creation time
	public String getTags() {
		StringBuilder tags = new StringBuilder();

		for (ACESentence s : sentences) {
			if (s.isNothingbut()) {
				tags.append("<i><span bgcolor='yellow'>/*nothing but*/</span></i>");
				break;
			}
		}

		if (axiomSet.isEmpty()) {
			tags.append("<i><span color='red'>/*neither OWL nor SWRL*/</span></i>");
		}
		else {
			if (getRules().size() > 0) {
				tags.append("<i><span bgcolor='yellow'>/*SWRL*/</span></i>");
			}
			if (isUnsatisfiable()) {
				tags.append("<i><span color='red'>/*weird sentence*/</span></i>");
			}
			//if(isEqualToThing()) {
			//	tags.append("<i><span color='red'>/*weird sentence*/</span></i>");
			//}
		}
		return tags.toString();
	}


	public boolean isUnsatisfiable() {
		for (OWLAxiom axiom : axiomSet) {
			if (axiom instanceof OWLSubClassAxiom) {
				if (((OWLSubClassAxiom) axiom).getSuperClass().isOWLNothing()) {
					return true;
				}
			}
			if (axiom instanceof OWLEquivalentClassesAxiom) {
				for (OWLDescription desc : ((OWLEquivalentClassesAxiom) axiom).getDescriptions()) {
					if (desc.isOWLNothing()) {
						return true;
					}
				}
			}
		}
		return false;
	}


	public boolean isEqualToThing() {
		for (OWLAxiom axiom : axiomSet) {
			if (axiom instanceof OWLSubClassAxiom) {
				if (((OWLSubClassAxiom) axiom).getSubClass().isOWLThing()) {
					return true;
				}
			}

			if (axiom instanceof OWLEquivalentClassesAxiom) {
				for (OWLDescription desc : ((OWLEquivalentClassesAxiom) axiom).getDescriptions()) {
					if (desc.isOWLThing()) {
						return true;
					}
				}
			}

		}
		return false;
	}


	public List<ACESentence> getRest(ACESentence sentence) {
		List<ACESentence> restSentences = Lists.newArrayList();
		for (ACESentence s : this.sentences) {
			if (! s.equals(sentence)) {
				restSentences.add(s);
			}
		}
		return restSentences;
	}

	/**
	 * <p>Returns a set of contentword forms of this snippet.</p> 
	 * 
	 * @return Set of contentword forms
	 */
	private Set<String> getContentWordsAsStrings() {
		Set<String> tokens = Sets.newHashSet();
		for (ACESentence s : sentences) {
			for (ACEToken token : s.getContentWords()) {
				tokens.add(token.toString());
			}
		}
		return tokens;
	}


	public int getContentWordCount() {
		int count = 0;
		for (ACESentence s : sentences) {
			count = count + s.getContentWords().size();
		}
		return count;
	}


	public List<Message> getMessages() {
		return messages;
	}


	public boolean hasACEErrors() {
		return hasErrors() && (errorMessagesCount != owlErrorMessagesCount);
	}


	/**
	 * <p>Returns <code>true</code> iff this snippet
	 * contains errors, either ACE syntax errors or
	 * errors that were encountered when mapping the
	 * (legal ACE snippet) into OWL/SWRL.</p>
	 * 
	 * <p>Iff this method returns <code>true</code>, then
	 * {@link #getLogicalAxioms()} returns an empty set.</p>
	 * 
	 * @return <code>true</code> iff this snippet contains errors
	 */
	private boolean hasErrors() {
		return (errorMessagesCount > 0);
	}


	public URI getDefaultNamespace() {
		return ns;
	}


	public boolean isQuestion() {
		return isQuestion;
	}


	public OWLLogicalAxiom getAxiom() {
		Set<OWLLogicalAxiom> logicalAxioms = getLogicalAxioms();
		if (logicalAxioms.size() == 1) {
			return logicalAxioms.iterator().next();
		}
		return null;
	}


	public OWLDescription getDLQuery() {
		OWLLogicalAxiom rawAxiom = getAxiom();
		if (rawAxiom == null) {
			return null;
		}
		if (rawAxiom instanceof OWLSubClassAxiom) {
			OWLSubClassAxiom axiom = (OWLSubClassAxiom) rawAxiom;
			return axiom.getSubClass();
		}
		return null;
	}


	public SnippetDate getTimestamp() {
		return timestamp;
	}


	public List<ACESentence> getParaphrase() {
		return para1;
	}


	public boolean hasAxioms() {
		return (! axiomSet.isEmpty());
	}


	public boolean containsEntityReference(OWLEntity entity) {
		for (OWLLogicalAxiom axiom : axiomSet) {
			if (axiom.getReferencedEntities().contains(entity)) {
				return true;
			}
		}
		return false;
	}


	public Set<OWLEntity> getReferencedEntities() {
		Set<OWLEntity> entities = Sets.newHashSet();
		for (OWLLogicalAxiom axiom : axiomSet) {
			entities.addAll(axiom.getReferencedEntities());
		}
		return entities;
	}


	public Multimap<Integer, Integer> getErrorSpans() {
		return pinpointers;
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if ((obj == null) || (obj.getClass() != this.getClass())) return false;
		ACESnippet s = (ACESnippet) obj;
		return sentences.equals(s.getSentences());
	}


	@Override
	public int hashCode() {
		return sentences.hashCode();
	}


	private void countMessages() {
		errorMessagesCount = 0;
		owlErrorMessagesCount = 0;
		for (Message m : getMessages()) {
			if (m.isError()) {
				errorMessagesCount++;
				if (m.getType().equals("owl")) {
					owlErrorMessagesCount++;
				}
			}
		}
	}
}