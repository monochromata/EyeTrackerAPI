package rit.eyeTracking;

/**
 * TODO: Maybe remove the constrains on attributes / replace them by
 * constraints on event types.
 * 
 * @param <T> The type of configuration object provided when starting
 * 	/ stopping the filter.
 * @param <E> The type of event passed through the filter chain
 * 	({@link Event} by default)
 */
public interface Filter<T,E extends Event> extends EyeTrackingListener<E> {
	public void start(T obj, EyeTrackingListener<E> listener, Mode mode);
	public void stop(T obj, EyeTrackingListener<E> listener, Mode mode);
	
	/**
	 * Return the names of attributes that this filter requires
	 * to be present in events that it can process.
	 * 
	 * Note that the filter will still be provided with events that
	 * do not contain all required events and has to ignore them.
	 * 
	 * Information from this method is used to compute the order
	 * in which filters should be applied to the data.
	 * 
	 * @return An array of attribute names
	 * @see #notify(Event)
	 * @see #getAttributesDesired()
	 * @deprecated
	 */
	public String[] getAttributesRequired();
	
	/**
	 * Return the names of attributes that this filter is interested
	 * in. Note that the filter will receive events that lack desired
	 * attributes and has to ignore them.
	 * 
	 * Information from this method is used to compute the order in
	 * which filters should be applied to the data.
	 * 
	 * @return An array of attribute names
	 * @see #notify(Event)
	 * @see #getAttributesRequired()
	 * @deprecated
	 */
	public String[] getAttributesDesired();
	
	/**
	 * Return the names of attributes that this filter adds to events
	 * it creates. Not that not all events created by this source will
	 * actually have instance of these attributes.
	 * 
	 * @return An array of attribute names
	 * @deprecated
	 */
	public String[] getAttributesCreated();
	
	/**
	 * Provides an event to the filter. The filter may add attributes
	 * using {@link Event#addAttribute(String, Object)}.
	 * 
	 * Filters to not have the possibility to block events. They may,
	 * however, set attributes notifying interested parties that the
	 * event should be ignored.
	 * 
	 * This method will be invoked whenever an event has passed through
	 * events that are able to create all attributes required by this
	 * filter. The event instance provided to this method may lack one
	 * or more of the required attributes, though. The filter must detect
	 * such a situation and refrain from processing of this event.
	 * 
	 * @param event The event to be filtered.
	 * @param listener A listener to create new events. TODO: This parameter
	 * 		should actually be removed and event attributes should be designed
	 * 		to be disjoint such that e.g. a raw and a fixation location can
	 * 		be added to the same event.
	 * @param mode Either TRACKING_MODE or REPLAY_MODE
	 * @see #getAttributesRequired()
	 */
	public void notify(E event, EyeTrackingListener<E> listener, Mode mode);
}
