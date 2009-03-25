package ch.uzh.ifi.attempto.aceview.util;

import java.text.SimpleDateFormat;
import java.util.Date;

public class SnippetDate extends Date {

	private final String strDate;

	public SnippetDate() {
		SimpleDateFormat sdr = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		strDate = sdr.format(this);
	}

	@Override
	public String toString() {
		return strDate;
	}
}