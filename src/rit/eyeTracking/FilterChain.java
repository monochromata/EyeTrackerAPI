package rit.eyeTracking;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Maintains the connections between sources, filters and drains.
 */
public class FilterChain<T> implements EyeTrackingListener {
	
	private final List<Filter<T>> filters;
	private final List<Filter<T>> runtimeFilters = new ArrayList<Filter<T>>();
	
	public FilterChain(Filter<T>[] filters) {
		this.filters = new CopyOnWriteArrayList<Filter<T>>(Arrays.asList(filters));
	}
	
	/**
	 * Add a filter at runtime, e.g. to enable plugins to transparently access
	 * information provided in the event stream without requiring the user to
	 * edit the filter chain configuration file.
	 * 
	 * @param filter
	 */
	public void add(Filter<T> filter) {
		filters.add(filter);
		runtimeFilters.add(filter);
	}
	
	/**
	 * Removes a filter at runtime. Only filters added via {@link #add(Filter)}
	 * may be removed using this method.
	 * 
	 * @param filter
	 * @throws IllegalArgumentException On attempts to remove filters not
	 * 		added via {@link #add(Filter<T>)}
	 * @see #add(Filter)
	 */
	public void remove(Filter<T> filter) {
		if(runtimeFilters.contains(filter)) {
			filters.remove(filter);
			runtimeFilters.remove(filter);
		} else {
			throw new IllegalArgumentException("Cannot remove filter not added at runtime");
		}
	}
	
	public void start(T obj, EyeTrackingListener listener, Mode mode) {
		for(Filter<T> filter : filters) {
			filter.start(obj, listener, mode);
		}
	}
	
	public void stop(T obj, EyeTrackingListener listener, Mode mode) {
		for(Filter<T> filter : filters) {
			filter.stop(obj, listener, mode);
		}
	}
	@Override
	public void notify(Event e, EyeTrackingListener listener, Mode mode) {
		for(Filter<T> filter : filters) {
			filter.notify(e, listener, mode);
		}
	}
}
