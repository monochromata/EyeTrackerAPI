package rit.eyeTrackingAPI;

public interface Filter<T> extends EyeTrackingListener {
	public void start(T obj);
	public void stop(T obj);
	
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
	 */
	public String[] getAttributesDesired();
	
	/**
	 * Return the names of attributes that this filter adds to events
	 * it creates. Not that not all events created by this source will
	 * actually have instance of these attributes.
	 * 
	 * @return An array of attribute names
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
	 * @see #getAttributesRequired()
	 */
	public void notify(Event event);
}
