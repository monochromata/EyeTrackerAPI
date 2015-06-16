package rit.eyeTracking;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Maintains the connections between sources, filters and drains.
 * 
 * @param <T> The type of configuration object provided when starting
 * 	/ stopping the filter.
 * @param <E> The type of event passed through the filter chain
 * 	({@link Event} by default)
 */
public class FilterChain<T,E extends Event> implements EyeTrackingListener<E> {
	
	protected final List<Filter<T,E>> filters;
	protected final List<Filter<T,E>> runtimeFilters = new ArrayList<Filter<T,E>>();
	
	public FilterChain(Filter<T,E>[] filters) {
		this.filters = new CopyOnWriteArrayList<Filter<T,E>>(Arrays.asList(filters));
	}
	
	public boolean hasFilter(Class<?> clazz) {
		for(Filter<T,E> filter: filters) {
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
	public <S extends Filter<T,E>> S getFilter(Class<?> filterClass) {
		for(Filter<T,E> filter: filters) {
			if(filter.getClass() == filterClass)
				return (S)filter;
		}
		return null;
	}
	
	public List<Filter<T,E>> findFilters(Class<?> classOrInterface) {
		List<Filter<T,E>> results = new LinkedList<Filter<T,E>>();
		for(Filter<T,E> filter: filters) {
			if(classOrInterface.isAssignableFrom(filter.getClass()))
				results.add(filter);
		}
		return results;
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
	public void prepend(Filter<T,E> filter) {
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
	public void add(Filter<T,E> filter) {
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
	public void remove(Filter<T,E> filter) {
		if(runtimeFilters.contains(filter)) {
			filters.remove(filter);
			runtimeFilters.remove(filter);
		} else {
			throw new IllegalArgumentException("Cannot remove filter not added at runtime");
		}
	}
	
	public void start(T obj, EyeTrackingListener<E> listener, Mode mode) {
		for(Filter<T,E> filter : filters) {
			filter.start(obj, listener, mode);
		}
	}
	
	public void stop(T obj, EyeTrackingListener<E> listener, Mode mode) {
		for(Filter<T,E> filter : filters) {
			filter.stop(obj, listener, mode);
		}
	}
	@Override
	public void notify(E e, EyeTrackingListener<E> listener, Mode mode) {
		for(Filter<T,E> filter : filters) {
			filter.notify(e, listener, mode);
		}
	}
	
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("FilterChain:\n");
		for(Filter<T,E> filter: filters) {
			sb.append(filter.getClass().getName()+"\n");
		}
		return sb.toString();
	}
}
