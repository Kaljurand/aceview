package ch.uzh.ifi.attempto.aceview.model.event;

public class ACEViewEvent<T> {

	private final T type;

	public ACEViewEvent(T type) {
		this.type = type;
	}

	public T getType() {
		return type;
	}

	public boolean isType(T type) {
		return this.type.equals(type);
	}
}