package ch.uzh.ifi.attempto.ace;

import java.util.regex.Pattern;

import com.google.common.collect.ImmutableSet;

import ch.uzh.ifi.attempto.ape.FunctionWords;

/**
 * <p>ACE token and its features.</p>
 * 
 * @author Kaarel Kaljurand
 */
public final class ACEToken {

	// Some function words (in lowercase) additionally to those provided by Attempto Java Packages.
	// TODO: This list is probably not complete.
	// TODO: Some of these words could maybe be moved to AJP.
	private static final ImmutableSet ADDITIONAL_FUNCTIONWORDS =
		ImmutableSet.of("s", "at", "less", "least", "exactly", "thing", "things", "false", "does", "do", "he/she");

	// The token itself
	private String token;

	// The token in lowercase
	private String tokenLC;

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

	private static final Pattern variablePattern = Pattern.compile("[A-Z][0-9]*");

	public static final ACEToken DOT = makeDot();


	private ACEToken() {}


	public static ACEToken newToken(String token) {
		ACEToken newToken = new ACEToken();
		newToken.token = token;
		newToken.tokenLC = token.toLowerCase();

		if (newToken.tokenLC.equals("and")
				|| newToken.tokenLC.equals("or")
				|| newToken.tokenLC.equals("if")
				|| newToken.tokenLC.equals("then")) {
			newToken.isOrdinationWord = true;
			newToken.isFunctionWord = true;
		}
		else if (newToken.tokenLC.equals("but")) {
			newToken.isButToken = true;
			newToken.isFunctionWord = true;
		}
		else if (variablePattern.matcher(token).matches()) {
			newToken.isVariable = true;
			newToken.isFunctionWord = true;
		}
		else if (checkIsFunctionWord(newToken.tokenLC)) {
			newToken.isFunctionWord = true;
		}
		return newToken;
	}


	public static ACEToken newNumber(double number) {
		ACEToken newToken = new ACEToken();
		if (number == (int) number) {
			newToken.token = Integer.toString((int) number);
		}
		else {
			newToken.token = Double.toString(number);
		}
		newToken.isNumber = true;
		newToken.tokenLC = newToken.token;
		newToken.isFunctionWord = true;
		return newToken;
	}


	public static ACEToken newQuotedString(String str) {
		ACEToken newToken = new ACEToken();
		newToken.token = "\"" + str + "\"";
		newToken.tokenLC = newToken.token;
		newToken.isQuotedString = true;
		newToken.isFunctionWord = true;
		return newToken;
	}


	public static ACEToken newSymbol(char ch) {
		ACEToken newToken = new ACEToken();
		newToken.token = String.valueOf(ch);
		newToken.tokenLC = newToken.token;
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
		newToken.tokenLC = newToken.token;
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
		newToken.tokenLC = newToken.token;
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


	@Override
	public String toString() {
		return token;
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if ((obj == null) || (obj.getClass() != this.getClass())) return false;
		ACEToken t = (ACEToken) obj;
		return token.toString().equals(t.toString());
	}


	@Override
	public int hashCode() {
		return token.toString().hashCode();
	}


	private static ACEToken makeDot() {
		ACEToken newToken = new ACEToken();
		newToken.token = String.valueOf('.');
		newToken.tokenLC = newToken.token;
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