package ch.uzh.ifi.attempto.aceview.lexicon;

import java.util.Collection;
import java.util.Set;

import org.semanticweb.owlapi.model.IRI;

import ch.uzh.ifi.attempto.ape.Lexicon;

/**
 * <p>ACE lexicon maps OWL entities to ACE lexicon entries.
 * Each lexicon entry assigns a type: common noun (CN), transitive verb (TV),
 * proper name (PN) to the entity, and describes the surface wordforms
 * (singular, plural, past participle) via which the entity can be
 * referred to in ACE sentences.</p>
 * 
 * @author Kaarel Kaljurand
 */
public interface TokenMapper {

	/**
	 * <p>Adds token with its lemma and morphological category.</p>
	 * @param token
	 * @param lemma
	 * @param morph
	 */
	void addEntry(String token, IRI lemma, MorphType morph);


	/**
	 * <p>Removes token with its lemma and morphological category.</p>
	 * @param token
	 * @param lemma
	 * @param morph
	 */
	void removeEntry(String token, IRI lemma, MorphType morph);


	/**
	 * <p>Returns <code>true</code> if the given wordform is in the lexicon,
	 * i.e. described by at least one lexicon entry. Returns <code>false</code>
	 * otherwise.</p>
	 * 
	 * @param wordform ACE wordform
	 * @return <code>true</code> if wordform is in the lexicon
	 */
	boolean containsWordform(String wordform);


	/**
	 * <p>Returns the {@link Autocompleter} object which can be
	 * used to autocomplete wordforms to the point where the ambiguity starts,
	 * or to retrieve the list of all possible full completions (which are
	 * wordforms registered in the lexicon.</p>
	 * 
	 * @return Auto-completer
	 */
	Autocompleter getAutocompleter();


	/**
	 * <p>Returns the number of lexicon entries in the lexicon.
	 * In other words, returns the number of entities that have a mapping
	 * to a lexicon entry in this lexicon.</p>
	 * 
	 * @return Number of lexicon entries
	 */
	int size();


	/**
	 * <p>Returns the number of CN entries in the lexicon.</p>
	 * 
	 * @return Number of CN entries
	 */
	int getCNCount();


	/**
	 * <p>Returns the number of TV entries in the lexicon.</p>
	 * 
	 * @return Number of TV entries
	 */
	int getTVCount();


	/**
	 * <p>Returns the number of PN entries in the lexicon.</p>
	 * 
	 * @return Number of PN entries
	 */
	int getPNCount();


	/**
	 * <p>Returns the number of partial (incomplete) entries in the lexicon.</p>
	 * 
	 * @return Number of partial entries
	 */
	int getPartialEntryCount();


	/**
	 * <p>Serializes the complete lexicon in ACE lexicon format.</p>
	 * 
	 * @return String in ACE lexicon format
	 */
	String toACELexiconFormat();


	/**
	 * <p>Creates the <code>Lexicon</code> on the basis of the given
	 * set of wordforms.</p>
	 * 
	 * @param contentWordforms Set of ACE wordforms
	 * @return Lexicon
	 */
	Lexicon createLexicon(Set<String> contentWordforms);


	/**
	 * <p>Creates the <code>Lexicon</code> from all the lexicon
	 * entries.</p>
	 * 
	 * @return Lexicon
	 */
	Lexicon createLexicon();


	/** <p>Returns the number of wordforms in this lexicon.</p>
	 * 
	 * @return Number of wordforms
	 */
	int getWordformCount();


	/** <p>Returns the wordforms that are ambiguous (i.e. can denote
	 * several different entities) according to this lexicon.</p>
	 * 
	 * @return Set of ambiguous wordforms
	 */
	Set<String> getAmbiguousWordforms();


	/** <p>Returns the number of wordforms that are ambiguous (i.e. can denote
	 * several different entities) according to this lexicon.</p>
	 * 
	 * @return Number of ambiguous wordforms
	 */
	int getAmbiguousWordformCount();


	/** <p>Returns the number of wordforms that are ambiguous in the
	 * same wordclass (e.g. can denote several different entities that all
	 * map to common nouns) according to this lexicon.</p>
	 * 
	 * <p>Note that this is a dangerous ambiguity which no ontology
	 * should contain.</p>
	 * 
	 * @return Number of ambiguous wordforms in the same wordclass
	 */
	int getWordclassAmbiguousWordformCount();


	/**
	 * <p>Return the wordform-IRI-morphtype triples that
	 * correspond to this wordform. There can be 0 or more.</p>
	 * 
	 * @param wordform
	 * @return Collection of triples
	 */
	public Collection<Triple> getWordformEntries(String wordform);


	/**
	 * <p>Returns the set of entity IRIs that correspond to the
	 * given wordform in the lexicon.
	 * 
	 * @param wordform ACE wordform
	 * @return Set of IRIs of the given wordform
	 */
	Set<IRI> getWordformIRIs(String wordform);


	/**
	 * <p>Returns the entity IRI that corresponds to the
	 * given wordform in the lexicon. If there are no
	 * corresponding IRIs, or there are more than one
	 * then returns <code>null</code>.
	 * 
	 * @param wordform
	 * @return IRI of the given wordform
	 */
	IRI getWordformIRI(String wordform);


	/**
	 * 
	 * @param entityIRI
	 * @param morphType
	 * @return Wordform of the given entity in the given morphological form
	 */
	String getWordform(IRI entityIRI, MorphType morphType);


	int getWordformPnSgCount();
	int getWordformCnSgCount();
	int getWordformCnPlCount();
	int getWordformTvSgCount();
	int getWordformTvPlCount();
	int getWordformTvVbgCount();
}