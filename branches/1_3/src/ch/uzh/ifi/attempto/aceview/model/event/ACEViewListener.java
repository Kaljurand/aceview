package ch.uzh.ifi.attempto.aceview.model.event;

public interface ACEViewListener<T> {
	void handleChange(T event);
}