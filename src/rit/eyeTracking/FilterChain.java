package rit.eyeTracking;

/**
 * Maintains the connections between sources, filters and drains.
 */
public class FilterChain<T> implements EyeTrackingListener {
	
	private final Filter<T>[] filters;
	
	public FilterChain(Filter<T>[] filters) {
		this.filters = filters;
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
