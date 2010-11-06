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

package ch.uzh.ifi.attempto.aceview;


import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.swing.JOptionPane;

import org.apache.log4j.Logger;
import org.semanticweb.owlapi.expression.ParserException;
import org.semanticweb.owlapi.io.StringDocumentSource;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyID;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.model.SWRLRule;

import com.google.common.base.Joiner;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

import ch.uzh.ifi.attempto.ace.ACESentence;
import ch.uzh.ifi.attempto.ace.ACESplitter;
import ch.uzh.ifi.attempto.ace.ACEToken;
import ch.uzh.ifi.attempto.aceview.lexicon.EntryType;
import ch.uzh.ifi.attempto.aceview.lexicon.LexiconUtils;
import ch.uzh.ifi.attempto.aceview.lexicon.MorphType;
import ch.uzh.ifi.attempto.aceview.lexicon.TokenMapper;
import ch.uzh.ifi.attempto.aceview.lexicon.Triple;
import ch.uzh.ifi.attempto.aceview.util.SnippetDate;
import ch.uzh.ifi.attempto.ape.ACEParser;
import ch.uzh.ifi.attempto.ape.Lexicon;
import ch.uzh.ifi.attempto.ape.Message;
import ch.uzh.ifi.attempto.ape.MessageContainer;
import ch.uzh.ifi.attempto.ape.OutputType;
import ch.uzh.ifi.attempto.ape.ACEParserResult;

/**
 * <p>Every snippet is mapped to OWL/SWRL during construction time.
 * The corresponding OWL/SWRL axiom can be given as an argument in the
 * constructor (this is used by the verbalizer).</p>
 * 
 * @author Kaarel Kaljurand
 */
public class ACESnippetImpl implements ACESnippet {

	private static final Logger logger = Logger.getLogger(ACESnippetImpl.class);

	private final ImmutableList<ACESentence> sentences;
	private final OWLOntologyID ns;
	// Alternative rendering which could be used if this snippet is empty.
	private final String altRendering;
	private final SnippetDate timestamp;
	private final List<Message> messages = Lists.newArrayList();
	private final Multimap<Integer, Integer> pinpointers = HashMultimap.create();

	// BUG: make everything final
	private ImmutableSet<OWLLogicalAxiom> axiomSet = ImmutableSet.of();
	// private final Set<SWRLRule> rules = Sets.newHashSet();
	private List<List<ACESentence>> para1 = Lists.newArrayList();
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
	public ACESnippetImpl(OWLOntologyID ns, List<ACESentence> sentences) {
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
	public ACESnippetImpl(OWLOntologyID ns, ACESentence sentence) {
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
	 * therefore the snippet is not parsed.
	 * Note that any annotations (if present) are stripped from the axiom.</p>
	 * 
	 * @param ns Default namespace of the snippet
	 * @param str Textual content of the snippet
	 * @param axiom OWL axiom that the snippet corresponds to
	 * @param altRendering alternative rendering (MOS syntax) for the snippet
	 */
	public ACESnippetImpl(OWLOntologyID ns, String str, OWLLogicalAxiom axiom, String altRendering) {
		this.timestamp = new SnippetDate();
		this.ns = ns;
		this.axiomSet = ImmutableSet.of((OWLLogicalAxiom) axiom.getAxiomWithoutAnnotations());
		this.sentences = ImmutableList.copyOf(ACESplitter.getSentences(str));
		if (! sentences.isEmpty()) {
			if (sentences.get(sentences.size() - 1).isQuestion()) {
				isQuestion = true;
			}
		}
		this.altRendering = altRendering;
	}


	public ACESnippetImpl(OWLOntologyID ns, String str, OWLLogicalAxiom axiom) {
		this(ns, str, axiom, null);
	}


	public List<ACESentence> getSentences() {
		return sentences;
	}


	public boolean isEmpty() {
		return sentences.isEmpty();
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


	@Override
	public String toString() {		
		if (isEmpty()) {
			return getAltRendering();
		}
		return joiner.join(sentences);
	}


	public String toHtmlString(TokenMapper aceLexicon) {

		if (isEmpty()) {
			return "<span color='red'>" + getAltRendering() + "</span>";
		}

		StringBuilder sb = new StringBuilder();

		for (ACESentence sentence : sentences) {
			for (ACEToken token : sentence.getTokens()) {
				if (token.isOrdinationWord()) {
					sb.append("<span color='green'>");
					sb.append(token);
					sb.append("</span>");
				}
				else if (token.isQuotedString() || token.isNumber()) {
					sb.append("<i>");
					sb.append(token);
					sb.append("</i>");
				}
				else if (token.isFunctionWord()) {
					sb.append(token);
				}
				else if (token.isBadToken()) {
					sb.append("<span color='red'>");
					sb.append(token);
					sb.append("</span>");
				}
				else {
					Set<IRI> iris = aceLexicon.getWordformIRIs(token.toString());
					Collection<Triple> triples = aceLexicon.getWordformEntries(token.toString());
					int iriCount = iris.size();
					if (iriCount == 0) {
						sb.append("<span color='#777777'>");
						sb.append(token);
						sb.append("</span>");
					}
					else if (iriCount == 1) {
						Triple triple = triples.iterator().next();
						EntryType type = MorphType.getWordClass(triple.getProperty());
						sb.append("<a href='#");
						sb.append(LexiconUtils.getHrefId(type, triple.getSubjectIRI()));
						sb.append("'>");
						sb.append(token);
						sb.append("</a>");
					}
					else if (iriCount > 1) {
						logger.info("Ambiguous: " + token + " " + triples);
						sb.append("<span color='#777777'>");
						sb.append(token);
						sb.append("<sup>");
						sb.append(iriCount);
						sb.append("<sup>");
						sb.append("</span>");
					}
					else {
						// TODO: throw something
					}
				}
				sb.append(' ');
			}
		}
		return sb.toString();
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
			if (axiom instanceof OWLSubClassOfAxiom) {
				if (((OWLSubClassOfAxiom) axiom).getSuperClass().isOWLNothing()) {
					return true;
				}
			}
			if (axiom instanceof OWLEquivalentClassesAxiom) {
				for (OWLClassExpression desc : ((OWLEquivalentClassesAxiom) axiom).getClassExpressions()) {
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
			if (axiom instanceof OWLSubClassOfAxiom) {
				if (((OWLSubClassOfAxiom) axiom).getSubClass().isOWLThing()) {
					return true;
				}
			}

			if (axiom instanceof OWLEquivalentClassesAxiom) {
				for (OWLClassExpression desc : ((OWLEquivalentClassesAxiom) axiom).getClassExpressions()) {
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


	public OWLOntologyID getDefaultNamespace() {
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


	public OWLClassExpression getDLQuery() {
		OWLLogicalAxiom rawAxiom = getAxiom();
		if (rawAxiom == null) {
			return null;
		}
		if (rawAxiom instanceof OWLSubClassOfAxiom) {
			OWLSubClassOfAxiom axiom = (OWLSubClassOfAxiom) rawAxiom;
			return axiom.getSubClass();
		}
		return null;
	}


	public SnippetDate getTimestamp() {
		return timestamp;
	}


	public List<List<ACESentence>> getParaphrase() {
		return para1;
	}


	public boolean hasAxioms() {
		return (! axiomSet.isEmpty());
	}


	public boolean containsEntityReference(OWLEntity entity) {
		for (OWLLogicalAxiom axiom : axiomSet) {
			if (axiom.getSignature().contains(entity)) {
				return true;
			}
		}
		return false;
	}


	public Set<OWLEntity> getReferencedEntities() {
		Set<OWLEntity> entities = Sets.newHashSet();
		for (OWLLogicalAxiom axiom : axiomSet) {
			entities.addAll(axiom.getSignature());
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


	/**
	 * <p>Returns a set of contentword forms of this snippet.</p> 
	 * 
	 * @return Set of contentword forms
	 */
	private Set<String> getContentWordsAsStrings() {
		Set<String> contentWordsAsStrings = Sets.newHashSet();
		for (ACESentence s : sentences) {
			for (ACEToken contentWord : s.getContentWords()) {
				contentWordsAsStrings.add(contentWord.toString());
			}
		}
		return contentWordsAsStrings;
	}


	/**
	 * <p>Parses the text of the snippet with the Manchester OWL Syntax parser;
	 * if this fails then parses with an ACE parser. In the later case,
	 * also optionally paraphrases the snippet.</li>
	 */
	private void init() {
		ACEViewPreferences prefs = ACEViewPreferences.getInstance();

		// As possible MOS strings,
		// we only accept snippets which contain exactly one sentence.
		if (prefs.getUseMos() && sentences.size() == 1) {
			OWLLogicalAxiom mosAxiom = null;
			try {
				// TODO: BUG: it's not clear what this "base" is
				// supposed to be, the OWL-API Javadoc doesn't say anything about it.
				String base = getOntologyIRIAsString();
				mosAxiom = ACETextManager.parseWithMos(sentences.iterator().next(), base);
			} catch (ParserException e) {
				// e.printStackTrace();
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


	private void parse(ACEViewPreferences prefs) throws OWLOntologyCreationException {
		TokenMapper aceLexicon = ACETextManager.getActiveACELexicon();
		Set<String> contentWordForms = getContentWordsAsStrings();

		if (! prefs.getParseWithUndefinedTokens()) {
			// logger.info("Content word forms: " + contentWordForms);
			for (String wordFrom : contentWordForms) {
				if (! aceLexicon.containsWordform(wordFrom)) {
					messages.add(new Message("error", "token", null, null, wordFrom, "Add this wordform to the lexicon"));
				}
			}
		}

		// In case there are no lexical entries for at least one token, and the preferences
		// tell us not to parse in this case, then abort immediately.
		if (messages.isEmpty()) {
			parseWithAceParser(prefs, aceLexicon, contentWordForms);
		}
		else {
			//logger.info("Not parsing, there are messages.");
			//logger.info("Messages: " + messages);
		}
	}


	private void parseWithAceParser(ACEViewPreferences prefs, TokenMapper aceLexicon, Set<String> contentWordforms) throws OWLOntologyCreationException {
		logger.info("Wordforms: " + contentWordforms);
		Lexicon lexicon = aceLexicon.createLexicon(contentWordforms);
		if (lexicon.getEntries().isEmpty()) {
			logger.info("Parsing with empty lexicon.");
			lexicon = null;
		}
		else {
			logger.info("Parsing with lexicon:\n" + lexicon.toString());
		}

		boolean paraphrase1Enabled = prefs.isParaphrase1Enabled();

		// Note: parser might be null in case the ParserHolder has not been initialized
		ACEParser parser = ParserHolder.getACEParser();

		parser.setURI(getOntologyIRIAsString());

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
					para1 = ACESplitter.getParagraphs(result.get(OutputType.PARAPHRASE1));
				}
				String owlxml = result.get(OutputType.OWLXML);
				if (owlxml == null || owlxml.length() == 0) {
					throw new OWLOntologyCreationException("get(OutputType.OWLXML) is null or empty");
				}

				// TODO: BUG: creating a new ontology manager just to parse a snippet
				// might be bad for performance
				OWLOntologyManager manager = ACETextManager.createOWLOntologyManager();

				// TODO: BUG: remove this temporary hack that converts the APE output into
				// correct OWL 2 XML.
				String owl2xml = OWLXMLTransformer.transform(owlxml);

				// BUG: we should copy the axioms and then throw away the created ontology
				axiomSet = ImmutableSet.copyOf(manager.loadOntologyFromOntologyDocument(new StringDocumentSource(owl2xml)).getLogicalAxioms());
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


	// TODO Do this during construction time
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


	/**
	 * <p>Extracts the IRI from the OntologyID
	 * and converts it into a string. If the IRI is
	 * missing the returns an empty string.</p>
	 * 
	 * <p>TODO: think about it</p>
	 * 
	 * @return Ontology IRI as string, or an empty string
	 */
	private String getOntologyIRIAsString() {
		IRI ontologyIRI = ns.getOntologyIRI();
		if (ontologyIRI == null) {
			return "";
		}
		return ontologyIRI.toString();
	}


	private String getAltRendering() {
		if (altRendering == null) {
			return "/*" + getLogicalAxioms().toString() + "*/";
		}
		// The Manchester Syntax rendering contains layout symbols,
		// which we convert into a single space.
		return "/* MOS: " + altRendering.replaceAll("[ \t\n\f\b\r]+", " ") + " */";
	}
}