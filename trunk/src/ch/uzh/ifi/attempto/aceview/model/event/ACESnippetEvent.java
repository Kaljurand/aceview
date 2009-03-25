package ch.uzh.ifi.attempto.aceview.model.event;


public class ACESnippetEvent {

	private final SnippetEventType type;

	public ACESnippetEvent(SnippetEventType type) {
		this.type = type;
	}

	public SnippetEventType getType() {
		return type;
	}

	public boolean isType(SnippetEventType type) {
		return this.type.equals(type);
	}
}