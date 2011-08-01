package ch.uzh.ifi.attempto.aceview;

import java.util.regex.Pattern;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationValue;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLOntology;

import ch.uzh.ifi.attempto.ace.ACEToken;
import ch.uzh.ifi.attempto.ace.ACETokenRenderer;
import ch.uzh.ifi.attempto.ace.EntryType;
import ch.uzh.ifi.attempto.ace.FieldType;
import ch.uzh.ifi.attempto.aceview.lexicon.LexiconUtils;
import ch.uzh.ifi.attempto.aceview.lexicon.MorphType;
import ch.uzh.ifi.attempto.aceview.lexicon.TokenMapper;

/**
 * <p>If the ACE token is a function word, then returns its string content.
 * Otherwise treats it as an IRI, and returns its corresponding entity rendering.
 * If something goes wrong then returns the IRI fragment prefixed by question marks.</p>
 * 
 * <p>If the token needs quoting (i.e. matches <code>ACE_CONTENT_WORD_PATTERN</code>), then
 * adds backticks around the token.</p>
 * 
 * @author Kaarel Kaljurand
 */
public class IriRenderer implements ACETokenRenderer {

	public static final Pattern ACE_CONTENT_WORD_PATTERN = Pattern.compile("[a-zA-Z$_-][a-zA-Z0-9$_-]*");

	private final TokenMapper mTokenMapper;

	public IriRenderer(TokenMapper tokenMapper) {
		mTokenMapper = tokenMapper;
	}


	public String render(ACEToken token) {
		IRI iri = token.getIri();
		if (iri == null) {
			return token.toString();
		}
		String form = mTokenMapper.getWordform(iri, MorphType.getMorphType(token.getEntryType(), token.getFieldType()));
		if (ACE_CONTENT_WORD_PATTERN.matcher(form).matches()) {
			return form;
		}
		return "`" + form + "`";
	}


	/**
	 * TODO: this is experimental
	 * @param token
	 * @return
	 */
	/*
	public String renderBasedOnEntity(ACEToken token) {
		IRI iri = token.getIri();

		if (iri == null) {
			return token.toString();
		}

		if (mMngr == null) {
			logger.warn("OWLModelManager == null");
			return "?" + iri.getFragment();
		}

		OWLEntity entity = ACETextManager.findEntity(token.getEntryType(), iri);

		if (entity == null) {
			logger.warn("Cannot find entity for: " + token.getEntryType() + " and " + iri);
			return "??" + iri.getFragment();
		}

		String rendering = mMngr.getRendering(entity);
		if (rendering == null) {
			logger.warn("Cannot find rendering for: " + entity);
			return "???" + iri.getFragment();
		}
		// TODO: return the morph form based on getFieldType(), not the rendering
		return rendering;
	}
	 */


	/**
	 * <p>Returns the singular surface form for the given entity.
	 * Looks for it among the annotation axioms in the given
	 * ontology. If this fails then just returns the rendering of the entity.</p>
	 * 
	 * @param entity OWL entity
	 * @param ont OWL ontology that contains surfaceform-annotations for the entity
	 * @return Singular form of the entity
	 */
	private static String getSg(OWLEntity entity, OWLOntology ont) {
		// decide if the entity corresponds to CN, TV, or PN
		EntryType entryType = LexiconUtils.getLexiconEntryType(entity);
		// get the type CN_SG, TV_SG, or PN_SG
		MorphType morphType = MorphType.getMorphType(entryType, FieldType.SG);
		// get the IRI that corresponds to this type
		IRI wordformTypeIRI = morphType.getIRI();

		// scan all the annotations that annotate this entity
		for (OWLAnnotationAssertionAxiom annAx : ont.getAnnotationAssertionAxioms(entity.getIRI())) {
			// ... and select the one that annotates it using the ?_SG IRI
			if (annAx.getProperty().getIRI().equals(wordformTypeIRI)) {
				OWLAnnotationValue value = annAx.getValue();
				if (value instanceof OWLLiteral) {
					return ((OWLLiteral) value).getLiteral();
				}
				break;
			}
		}
		return ACETextManager.getRendering(entity);
	}
}