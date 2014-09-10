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
	
	public boolean hasFilter(Class<?> clazz) {
		for(Filter<T> filter: filters) {
			if(filter.getClass() == clazz)
				return true;
		}
		return false;
	}
	
	/**
	 * Returns the instance of the given filterClass from the filterChain, or null.
	 * 
	 * @param filterClass
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <S extends Filter<T>> S getFilter(Class<?> filterClass) {
		for(Filter<T> filter: filters) {
			if(filter.getClass() == filterClass)
				return (S)filter;
		}
		return null;
	}
	
	/**
	 * Adds a filter to the beginning of the chain.
	 * 
	 * @param filter
	 * 
	 * @deprecated Filters should be ordered automatically based on the
	 * 		attributes they create and consume. An exception should be
	 *  	raised if an attempt is made to access an attribute not declared
	 * 		as being consumed by the filter.
	 */
	public void prepend(Filter<T> filter) {
		filters.add(0, filter);
		runtimeFilters.add(0, filter);
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
	
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("FilterChain:\n");
		for(Filter<T> filter: filters) {
			sb.append(filter.getClass().getName()+"\n");
		}
		return sb.toString();
	}
}
