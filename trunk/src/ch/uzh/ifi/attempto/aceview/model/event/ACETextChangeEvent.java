package ch.uzh.ifi.attempto.aceview.model.event;


public class ACETextChangeEvent {

	private final EventType type;

	public ACETextChangeEvent(EventType type) {
		this.type = type;
	}

	public EventType getType() {
		return type;
	}

	public boolean isType(EventType type) {
		return this.type.equals(type);
	}
}
