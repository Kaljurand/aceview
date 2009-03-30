package ch.uzh.ifi.attempto.aceview.model.filter;

import java.util.ArrayList;
import java.util.List;

import org.jdesktop.swingx.decorator.Filter;

import com.google.common.base.Predicate;

public class PredicateFilter<T> extends Filter {
	private List<Integer> toPrevious;
	private final Predicate<T> predicate;


	/**
	 * 
	 * @param predicate
	 * @param col Column to filter in model coordinates
	 */
	public PredicateFilter(Predicate<T> predicate, int col) {
		super(col);
		this.predicate = predicate;
	}


	/**
	 * Resets the internal row mappings from this filter to the previous filter.
	 */
	@Override
	protected void reset() {
		toPrevious.clear();
		int inputSize = getInputSize();
		fromPrevious = new int[inputSize];  // fromPrevious is inherited protected
		for (int i = 0; i < inputSize; i++) {
			fromPrevious[i] = -1;
		}
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void filter() {
		int inputSize = getInputSize();
		int current = 0;
		for (int i = 0; i < inputSize; i++) {
			if (test(i)) {
				toPrevious.add(new Integer(i));
				// generate inverse map entry while we are here
				fromPrevious[i] = current++;
			}
		}
	}


	/**
	 * <p>Tests whether the given row (in this filter's coordinates) should
	 * be added.<p>
	 * 
	 * <p>BUG: What is erasure object?</p>
	 * 
	 * @param row the row to test
	 * @return true iff the row should be added
	 */
	private boolean test(int row) {
		Object value = getInputValue(row, getColumnIndex());
		return predicate.apply((T) value);
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getSize() {
		return toPrevious.size();
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	protected int mapTowardModel(int row) {
		return toPrevious.get(row);
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void init() {
		toPrevious = new ArrayList<Integer>();
	}
}