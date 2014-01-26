package rit.eyeTrackingAPI;

import rit.eyeTrackingAPI.EyeTrackingListener.Mode;

/**
 * Maintains the connections between sources, filters and drains.
 */
public class FilterChain<T> implements EyeTrackingListener {
	
	private final Filter<T>[] filters;
	
	public FilterChain(Filter<T>[] filters) {
		this.filters = filters;
	}
	
	public void start(T obj, Mode mode) {
		for(Filter<T> filter : filters) {
			filter.start(obj, mode);
		}
	}
	
	public void stop(T obj, Mode mode) {
		for(Filter<T> filter : filters) {
			filter.stop(obj, mode);
		}
	}
	@Override
	public void notify(Event e, EyeTrackingListener listener, Mode mode) {
		for(Filter<T> filter : filters) {
			filter.notify(e, listener, mode);
		}
	}
}
