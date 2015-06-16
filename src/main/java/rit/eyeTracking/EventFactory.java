package rit.eyeTracking;

import java.util.Map;

/**
 * A factory for application-specific {@link Event} implementations. This
 * interface allows applications to have the API and itself create events
 * with custom implementations of {@link Event#isNew()}.
 * 
 * @param <E> The type of event passed through the filter chain
 * 	({@link Event} by default)
 * 
 * @author monochromata
 * 
 * @see Event
 * @see AbstractEvent
 */
public interface EventFactory<E extends Event> {
	
	/**
	 * Creates a new application specific {@link Event} instance. 
	 * 
	 * @param id The event id
	 * @param attributes The initial attributes of the event.
	 * @return The new event
	 */
	public E createEvent(Event.ID id, Map<String,Object> attributes);
}
