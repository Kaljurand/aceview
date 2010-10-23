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

import java.util.Arrays;
import java.util.List;

import org.protege.editor.core.prefs.Preferences;
import org.protege.editor.core.prefs.PreferencesManager;

/**
 * <p>ACE View preferences</p>
 * 
 * @author Kaarel Kaljurand
 */
public final class ACEViewPreferences {

	private static ACEViewPreferences instance;

	private static final String PREFERENCES_SET_KEY = "ch.uzh.ifi.attempto.aceview";

	private static final String ACE_TO_OWL_KEY = "ACE_TO_OWL";

	private static final String ACE_TO_OWL_LOCAL_APE_PATH_KEY = "ACE_TO_OWL_LOCAL_APE_PATH";

	private static final String ACE_TO_OWL_WEBSERVICES_KEY = "ACE_TO_OWL_WEBSERVICES";
	private static final String ACE_TO_OWL_WEBSERVICE_URL_KEY = "ACE_TO_OWL_WEBSERVICE_URL";

	private static final String ACE_TO_OWL_SOCKETS_KEY = "ACE_TO_OWL_SOCKETS";
	private static final String ACE_TO_OWL_SOCKET_HOST_KEY = "ACE_TO_OWL_SOCKET_HOST";
	private static final String ACE_TO_OWL_SOCKET_PORT_KEY = "ACE_TO_OWL_SOCKET_PORT";

	private static final String PARAPHRASE1_KEY = "PARAPHRASE1";
	private static final String GUESS_KEY = "GUESS";
	private static final String CLEX_KEY = "CLEX";

	private static final String OWL_TO_ACE_WEBSERVICES_KEY = "OWL_TO_ACE_WEBSERVICES";
	private static final String OWL_TO_ACE_KEY = "OWL_TO_ACE";

	private static final String PARSEWITHUNDEF_KEY = "PARSEWITHUNDEF";
	private static final String USE_MOS_KEY = "USE_MOS";
	private static final String UPDATE_ANSWERS_ON_CLASSIFY_KEY = "UPDATE_ANSWERS_ON_CLASSIFY";
	private static final String USE_LEXICON_KEY = "USE_LEXICON";


	public static synchronized ACEViewPreferences getInstance() {
		if (instance == null) {
			instance = new ACEViewPreferences();
		}
		return instance;
	}

	// ACE to OWL: Webservice: URL
	public String getAceToOwl() {
		PreferencesManager prefMan = PreferencesManager.getInstance();
		Preferences prefs = prefMan.getPreferencesForSet(PREFERENCES_SET_KEY, ACE_TO_OWL_KEY);
		return prefs.getString(ACE_TO_OWL_KEY, "APE Webservice");
	}

	public void setAceToOwl(String url) {
		PreferencesManager prefMan = PreferencesManager.getInstance();
		Preferences prefs = prefMan.getPreferencesForSet(PREFERENCES_SET_KEY, ACE_TO_OWL_KEY);
		prefs.putString(ACE_TO_OWL_KEY, url);
	}


	// ACE to OWL: Local: APE path
	public String getApePath() {
		PreferencesManager prefMan = PreferencesManager.getInstance();
		Preferences prefs = prefMan.getPreferencesForSet(PREFERENCES_SET_KEY, ACE_TO_OWL_LOCAL_APE_PATH_KEY);
		return prefs.getString(ACE_TO_OWL_LOCAL_APE_PATH_KEY, "ape.exe");
	}

	public void setApePath(String apePath) {
		PreferencesManager prefMan = PreferencesManager.getInstance();
		Preferences prefs = prefMan.getPreferencesForSet(PREFERENCES_SET_KEY, ACE_TO_OWL_LOCAL_APE_PATH_KEY);
		prefs.putString(ACE_TO_OWL_LOCAL_APE_PATH_KEY, apePath);
	}

	// ACE to OWL: Webservices (URLs)
	public List<String> getAceToOwlWebservices() {
		PreferencesManager prefMan = PreferencesManager.getInstance();
		Preferences prefs = prefMan.getPreferencesForSet(PREFERENCES_SET_KEY, ACE_TO_OWL_WEBSERVICES_KEY);
		return prefs.getStringList(ACE_TO_OWL_WEBSERVICES_KEY, Arrays.asList(new String[] {
				"http://attempto.ifi.uzh.ch/ws/ape/apews.perl",
				"http://attempto.ifi.uzh.ch/ws/ape-alpha/apews.perl",
				//"http://attempto.ifi.uzh.ch:8000/",
				"http://localhost/ws/ape/apews.perl",
				"http://localhost:8000/"
		}));
	}

	public void setAceToOwlWebservices(List<String> webservices) {
		PreferencesManager prefMan = PreferencesManager.getInstance();
		Preferences prefs = prefMan.getPreferencesForSet(PREFERENCES_SET_KEY, ACE_TO_OWL_WEBSERVICES_KEY);
		prefs.putStringList(ACE_TO_OWL_WEBSERVICES_KEY, webservices);
	}


	// ACE to OWL: Selected webservice: URL
	public String getAceToOwlWebserviceUrl() {
		PreferencesManager prefMan = PreferencesManager.getInstance();
		Preferences prefs = prefMan.getPreferencesForSet(PREFERENCES_SET_KEY, ACE_TO_OWL_WEBSERVICE_URL_KEY);
		return prefs.getString(ACE_TO_OWL_WEBSERVICE_URL_KEY, "http://attempto.ifi.uzh.ch/ws/ape/apews.perl");
	}

	public void setAceToOwlWebserviceUrl(String url) {
		PreferencesManager prefMan = PreferencesManager.getInstance();
		Preferences prefs = prefMan.getPreferencesForSet(PREFERENCES_SET_KEY, ACE_TO_OWL_WEBSERVICE_URL_KEY);
		prefs.putString(ACE_TO_OWL_WEBSERVICE_URL_KEY, url);
	}

	// ACE to OWL: Sockets (hostnames)
	public List<String> getAceToOwlSockets() {
		PreferencesManager prefMan = PreferencesManager.getInstance();
		Preferences prefs = prefMan.getPreferencesForSet(PREFERENCES_SET_KEY, ACE_TO_OWL_SOCKETS_KEY);
		return prefs.getStringList(ACE_TO_OWL_SOCKETS_KEY, Arrays.asList(new String[] {
				"localhost",
				"attempto.ifi.uzh.ch"
		}));
	}

	public void setAceToOwlSockets(List<String> sockets) {
		PreferencesManager prefMan = PreferencesManager.getInstance();
		Preferences prefs = prefMan.getPreferencesForSet(PREFERENCES_SET_KEY, ACE_TO_OWL_SOCKETS_KEY);
		prefs.putStringList(ACE_TO_OWL_SOCKETS_KEY, sockets);
	}

	// ACE to OWL : Socket : HOST
	public String getAceToOwlSocketHost() {
		PreferencesManager prefMan = PreferencesManager.getInstance();
		Preferences prefs = prefMan.getPreferencesForSet(PREFERENCES_SET_KEY, ACE_TO_OWL_SOCKET_HOST_KEY);
		return prefs.getString(ACE_TO_OWL_SOCKET_HOST_KEY, "localhost");
	}

	public void setAceToOwlSocketHost(String host) {
		PreferencesManager prefMan = PreferencesManager.getInstance();
		Preferences prefs = prefMan.getPreferencesForSet(PREFERENCES_SET_KEY, ACE_TO_OWL_SOCKET_HOST_KEY);
		prefs.putString(ACE_TO_OWL_SOCKET_HOST_KEY, host);
	}


	// ACE to OWL : Socket : PORT
	public int getAceToOwlSocketPort() {
		PreferencesManager prefMan = PreferencesManager.getInstance();
		Preferences prefs = prefMan.getPreferencesForSet(PREFERENCES_SET_KEY, ACE_TO_OWL_SOCKET_PORT_KEY);
		return prefs.getInt(ACE_TO_OWL_SOCKET_PORT_KEY, 2766);
	}

	public void setAceToOwlSocketPort(int port) {
		PreferencesManager prefMan = PreferencesManager.getInstance();
		Preferences prefs = prefMan.getPreferencesForSet(PREFERENCES_SET_KEY, ACE_TO_OWL_SOCKET_PORT_KEY);
		prefs.putInt(ACE_TO_OWL_SOCKET_PORT_KEY, port);
	}


	// OWL to ACE
	public List<String> getOwlToAceWebservices() {
		PreferencesManager prefMan = PreferencesManager.getInstance();
		Preferences prefs = prefMan.getPreferencesForSet(PREFERENCES_SET_KEY, OWL_TO_ACE_WEBSERVICES_KEY);
		return prefs.getStringList(OWL_TO_ACE_WEBSERVICES_KEY, Arrays.asList(new String[] {
				"http://attempto.ifi.uzh.ch/service/owl_verbalizer/owl_to_ace",
				//"http://attempto.ifi.uzh.ch:5123/",
				"http://localhost/service/owl_verbalizer/owl_to_ace",
				"http://localhost:5123/"
		}));
	}

	public void setOwlToAceWebservices(List<String> owlToAceWebservices) {
		PreferencesManager prefMan = PreferencesManager.getInstance();
		Preferences prefs = prefMan.getPreferencesForSet(PREFERENCES_SET_KEY, OWL_TO_ACE_WEBSERVICES_KEY);
		prefs.putStringList(OWL_TO_ACE_WEBSERVICES_KEY, owlToAceWebservices);
	}

	public String getOwlToAce() {
		PreferencesManager prefMan = PreferencesManager.getInstance();
		Preferences prefs = prefMan.getPreferencesForSet(PREFERENCES_SET_KEY, OWL_TO_ACE_KEY);
		return prefs.getString(OWL_TO_ACE_KEY, "http://attempto.ifi.uzh.ch/service/owl_verbalizer/owl_to_ace");
	}

	public void setOwlToAce(String owlToAce) {
		PreferencesManager prefMan = PreferencesManager.getInstance();
		Preferences prefs = prefMan.getPreferencesForSet(PREFERENCES_SET_KEY, OWL_TO_ACE_KEY);
		prefs.putString(OWL_TO_ACE_KEY, owlToAce);
	}

	public void setParaphrase1Enabled(boolean b) {
		PreferencesManager prefMan = PreferencesManager.getInstance();
		Preferences prefs = prefMan.getPreferencesForSet(PREFERENCES_SET_KEY, PARAPHRASE1_KEY);
		prefs.putString(PARAPHRASE1_KEY, Boolean.toString(b));
	}

	// Default: false
	public boolean isParaphrase1Enabled() {
		PreferencesManager prefMan = PreferencesManager.getInstance();
		Preferences prefs = prefMan.getPreferencesForSet(PREFERENCES_SET_KEY, PARAPHRASE1_KEY);
		return prefs.getString(PARAPHRASE1_KEY, Boolean.toString(false)).equals(Boolean.toString(true));
	}

	public void setGuessingEnabled(boolean b) {
		PreferencesManager prefMan = PreferencesManager.getInstance();
		Preferences prefs = prefMan.getPreferencesForSet(PREFERENCES_SET_KEY, GUESS_KEY);
		prefs.putString(GUESS_KEY, Boolean.toString(b));
	}

	// Default: true
	public boolean isGuessingEnabled() {
		PreferencesManager prefMan = PreferencesManager.getInstance();
		Preferences prefs = prefMan.getPreferencesForSet(PREFERENCES_SET_KEY, GUESS_KEY);
		return prefs.getString(GUESS_KEY, Boolean.toString(true)).equals(Boolean.toString(true));
	}

	public void setClexEnabled(boolean b) {
		PreferencesManager prefMan = PreferencesManager.getInstance();
		Preferences prefs = prefMan.getPreferencesForSet(PREFERENCES_SET_KEY, CLEX_KEY);
		prefs.putString(CLEX_KEY, Boolean.toString(b));
	}

	// Default: true
	public boolean isClexEnabled() {
		PreferencesManager prefMan = PreferencesManager.getInstance();
		Preferences prefs = prefMan.getPreferencesForSet(PREFERENCES_SET_KEY, CLEX_KEY);
		return prefs.getString(CLEX_KEY, Boolean.toString(true)).equals(Boolean.toString(true));
	}

	public void setParseWithUndefinedTokens(boolean b) {
		PreferencesManager prefMan = PreferencesManager.getInstance();
		Preferences prefs = prefMan.getPreferencesForSet(PREFERENCES_SET_KEY, PARSEWITHUNDEF_KEY);
		prefs.putString(PARSEWITHUNDEF_KEY, Boolean.toString(b));
	}

	// Default: true
	public boolean getParseWithUndefinedTokens() {
		PreferencesManager prefMan = PreferencesManager.getInstance();
		Preferences prefs = prefMan.getPreferencesForSet(PREFERENCES_SET_KEY, PARSEWITHUNDEF_KEY);
		return prefs.getString(PARSEWITHUNDEF_KEY, Boolean.toString(true)).equals(Boolean.toString(true));
	}

	public void setUseMos(boolean b) {
		PreferencesManager prefMan = PreferencesManager.getInstance();
		Preferences prefs = prefMan.getPreferencesForSet(PREFERENCES_SET_KEY, USE_MOS_KEY);
		prefs.putString(USE_MOS_KEY, Boolean.toString(b));
	}

	// Default: false
	public boolean getUseMos() {
		PreferencesManager prefMan = PreferencesManager.getInstance();
		Preferences prefs = prefMan.getPreferencesForSet(PREFERENCES_SET_KEY, USE_MOS_KEY);
		return prefs.getString(USE_MOS_KEY, Boolean.toString(false)).equals(Boolean.toString(true));
	}

	// Are answers automatically updated after the classify-event has completed?
	public void setUpdateAnswersOnClassify(boolean b) {
		PreferencesManager prefMan = PreferencesManager.getInstance();
		Preferences prefs = prefMan.getPreferencesForSet(PREFERENCES_SET_KEY, UPDATE_ANSWERS_ON_CLASSIFY_KEY);
		prefs.putString(UPDATE_ANSWERS_ON_CLASSIFY_KEY, Boolean.toString(b));
	}

	// Default: false
	public boolean isUpdateAnswersOnClassify() {
		PreferencesManager prefMan = PreferencesManager.getInstance();
		Preferences prefs = prefMan.getPreferencesForSet(PREFERENCES_SET_KEY, UPDATE_ANSWERS_ON_CLASSIFY_KEY);
		return prefs.getString(UPDATE_ANSWERS_ON_CLASSIFY_KEY, Boolean.toString(false)).equals(Boolean.toString(true));
	}

	// Is the lexicon used
	public void setUseLexicon(boolean b) {
		PreferencesManager prefMan = PreferencesManager.getInstance();
		Preferences prefs = prefMan.getPreferencesForSet(PREFERENCES_SET_KEY, USE_LEXICON_KEY);
		prefs.putString(USE_LEXICON_KEY, Boolean.toString(b));
	}

	// Default: true
	public boolean isUseLexicon() {
		PreferencesManager prefMan = PreferencesManager.getInstance();
		Preferences prefs = prefMan.getPreferencesForSet(PREFERENCES_SET_KEY, USE_LEXICON_KEY);
		return prefs.getString(USE_LEXICON_KEY, Boolean.toString(true)).equals(Boolean.toString(true));
	}

}