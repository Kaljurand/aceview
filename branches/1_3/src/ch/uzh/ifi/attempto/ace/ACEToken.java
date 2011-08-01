package ch.uzh.ifi.attempto.ace;

import java.util.regex.Pattern;

import org.semanticweb.owlapi.model.IRI;

import com.google.common.collect.ImmutableSet;

import ch.uzh.ifi.attempto.ape.FunctionWords;

/**
 * <p>ACE token and its features.</p>
 * 
 * @author Kaarel Kaljurand
 */
public final class ACEToken {

	public static final ACEToken DOT = makeDot();

	// Some function words (in lowercase) additionally to those provided by Attempto Java Packages.
	// TODO: This list is probably not complete.
	// TODO: Some of these words could maybe be moved to AJP.
	private static final ImmutableSet ADDITIONAL_FUNCTIONWORDS =
		ImmutableSet.of("s", "at", "less", "least", "exactly", "thing", "things", "false", "does", "do", "he/she");

	private static final Pattern variablePattern = Pattern.compile("[A-Z][0-9]*");

	// The token of the function word
	private String token;
	// The IRI and types of the content word
	private IRI mIri;
	private EntryType mEntryType;
	private FieldType mFieldType;

	private boolean isBadToken = false;
	private boolean isBorderToken = false;
	private boolean isButToken = false;
	private boolean isQuestionMark = false;
	private boolean isNumber = false;
	private boolean isOrdinationWord = false;
	private boolean isSymbol = false;
	private boolean isQuotedString = false;
	private boolean isApos = false;
	private boolean isVariable = false;
	private boolean isFunctionWord = false;

	private ACEToken() {}

	/**
	 * <p>Constructs content words.</p>
	 * 
	 * @param str
	 * @param entryType
	 * @param fieldType
	 * @return
	 */
	public static ACEToken newToken(IRI iri, EntryType entryType, FieldType fieldType) {
		ACEToken t = new ACEToken();
		t.token = iri.toString();
		t.mIri = iri;
		t.mEntryType = entryType;
		t.mFieldType = fieldType;
		t.isFunctionWord = false;
		return t;
	}


	public static ACEToken newToken(String token) {
		ACEToken newToken = new ACEToken();
		newToken.token = token;

		String tokenLC = token.toLowerCase();

		if (tokenLC.equals("and")
				|| tokenLC.equals("or")
				|| tokenLC.equals("if")
				|| tokenLC.equals("then")) {
			newToken.isOrdinationWord = true;
			newToken.isFunctionWord = true;
		}
		else if (tokenLC.equals("but")) {
			newToken.isButToken = true;
			newToken.isFunctionWord = true;
		}
		else if (variablePattern.matcher(token).matches()) {
			newToken.isVariable = true;
			newToken.isFunctionWord = true;
		}
		else if (checkIsFunctionWord(tokenLC)) {
			newToken.isFunctionWord = true;
		}

		return newToken;
	}


	public static ACEToken newNumber(double number) {
		if (number == (int) number) {
			return newNumber(Integer.toString((int) number));
		}
		return newNumber(Double.toString(number));
	}


	public static ACEToken newNumber(String number) {
		ACEToken newToken = new ACEToken();
		newToken.token = number;
		newToken.isNumber = true;
		newToken.isFunctionWord = true;
		return newToken;
	}


	public static ACEToken newQuotedString(String str) {
		ACEToken newToken = new ACEToken();
		newToken.token = "\"" + str + "\"";
		newToken.isQuotedString = true;
		newToken.isFunctionWord = true;
		return newToken;
	}


	public static ACEToken newSymbol(char ch) {
		return newSymbol(String.valueOf(ch));
	}


	public static ACEToken newSymbol(String str) {
		ACEToken newToken = new ACEToken();
		newToken.token = str;
		newToken.isSymbol = true;
		if (newToken.token.equals("'")) {
			newToken.isApos = true;
		}
		newToken.isFunctionWord = true;
		return newToken;
	}


	public static ACEToken newBorderToken(char ch) {
		ACEToken newToken = new ACEToken();
		newToken.token = String.valueOf(ch);
		newToken.isBorderToken = true;
		newToken.isSymbol = true;
		if (newToken.token.equals("?")) {
			newToken.isQuestionMark = true;			
		}
		newToken.isFunctionWord = true;
		return newToken;
	}


	public static ACEToken newBadToken(char ch) {
		ACEToken newToken = new ACEToken();
		newToken.token = String.valueOf(ch);
		newToken.isBadToken = true;
		newToken.isFunctionWord = true;
		return newToken;
	}


	public boolean isQuotedString() {
		return isQuotedString;
	}

	public boolean isBadToken() {
		return isBadToken;
	}

	public boolean isButToken() {
		return isButToken;
	}

	public boolean isNumber() {
		return isNumber;
	}

	public boolean isSymbol() {
		return isSymbol;
	}

	public boolean isBorderToken() {
		return isBorderToken;
	}

	public boolean isQuestionMark() {
		return isQuestionMark;
	}

	public boolean isOrdinationWord() {
		return isOrdinationWord;
	}

	public boolean isVariable() {
		return isVariable;
	}

	public boolean isApos() {
		return isApos;
	}

	public boolean isFunctionWord() {
		return isFunctionWord;
	}

	public boolean isContentWord() {
		return (! isFunctionWord());
	}


	public String getToken() {
		return token;
	}


	public IRI getIri() {
		return mIri;
	}


	public EntryType getEntryType() {
		return mEntryType;
	}


	public FieldType getFieldType() {
		return mFieldType;
	}


	@Override
	public String toString() {
		return token;
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if ((obj == null) || (obj.getClass() != this.getClass())) return false;
		ACEToken t = (ACEToken) obj;
		return token.equals(t.getToken());
		/*
		if (mIri == null) {
			// TODO: current comparison is string-based, i.e. numbers and quoted strings
			// might match regular tokens, which is unwanted
			return token.equals(t.getToken());
		}
		return mIri.equals(t.getIri()) && mEntryType.equals(t.getEntryType()) && mFieldType.equals(t.getFieldType());
		 */
	}


	@Override
	public int hashCode() {
		return token.hashCode();
		/*
		if (mIri == null) {
			// TODO: current hashCode if string-based, i.e. numbers and quoted strings
			// might match regular tokens, which is unwanted
			token.hashCode();
		}
		// TODO: return a sensible hash code
		return mIri.hashCode() + mEntryType.hashCode() + mFieldType.hashCode();
		 */
	}


	private static ACEToken makeDot() {
		ACEToken newToken = new ACEToken();
		newToken.token = String.valueOf('.');
		newToken.isBorderToken = true;
		newToken.isSymbol = true;
		newToken.isFunctionWord = true;
		return newToken;
	}


	private static boolean checkIsFunctionWord(String tokenLC) {
		if (FunctionWords.isFunctionWord(tokenLC) || ADDITIONAL_FUNCTIONWORDS.contains(tokenLC)) {
			return true;
		}
		return false;
	}
}