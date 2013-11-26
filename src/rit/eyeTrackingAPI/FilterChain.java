package rit.eyeTrackingAPI;

/**
 * Maintains the connections between sources, filters and drains.
 */
public class FilterChain<T> implements EyeTrackingListener {
	
	private final Filter<T>[] filters;
	
	public FilterChain(Filter<T>[] filters) {
		this.filters = filters;
	}
	
	public void start(T obj) {
		for(Filter<T> filter : filters) {
			filter.start(obj);
		}
	}
	
	public void stop(T obj) {
		for(Filter<T> filter : filters) {
			filter.stop(obj);
		}
	}
	@Override
	public void notify(Event e) {
		for(Filter<T> filter : filters) {
			filter.notify(e);
		}
	}
}
