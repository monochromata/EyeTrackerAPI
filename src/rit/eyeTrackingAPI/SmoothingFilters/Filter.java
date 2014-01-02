package rit.eyeTrackingAPI.SmoothingFilters;

import rit.eyeTrackingAPI.Event;

/**
 * A class representing a filtering algorithm for smoothing jittery raw data
 * from the eye tracker.
 * 
 * TODO: split into two filters: one for smooting and one for fixation detection
 * 
 * @author Corey Engelman
 * 
 */
public abstract class Filter {
	
	public static final String FILTERED_POR_X = "rit.eyeTrackingAPI.SmoothingFilters.FILTERED_POR_X";
	public static final String FILTERED_POR_Y = "rit.eyeTrackingAPI.SmoothingFilters.FILTERED_POR_Y";

	protected int filterIntensity = 0;
	protected int filterCounter = 0;
	protected Event newEvent;
	protected boolean newEventAvailable = false;
	protected boolean eventRead = false;

	/**
	 * Constructs a filter with the cursor to be updated, the filter intensity,
	 * and type.
	 * 
	 * @param cursor
	 *            - the cursor that should be updated by this filter.
	 */
	public Filter() {
	}

	/**
	 * Constructs a filter with the cursor to be updated, the filter intensity,
	 * and type.
	 * 
	 * @param filterIntensity
	 *            - the number of points taken in per filter calculation
	 * @param cursor
	 *            - the cursor that should be updated by this filter.
	 */
	public Filter(int filterIntensity) {
		this.filterIntensity = filterIntensity;
	}

	/**
	 * Call filter with a string of tokens with new eye position data
	 * 
	 * @param tokens
	 *            - {iViewX Command string, time stamp in milli seconds, eye
	 *            type: l - left|r - right|b - both, left eye x position, right
	 *            eye x position, left eye y position, right eye y position}
	 */
	public abstract void filter(Event e);

	public boolean newEventAvailable() {
		boolean copy = this.newEventAvailable;
		newEventAvailable = false;
		return copy;
	}

	/**
	 * Wait until the next coordinate is received to allow other threads to run
	 */
	public synchronized void waitForNewEvent() {
		while (!newEventAvailable) {
			try {
				wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		eventRead = false;
	}

	/**
	 * Changes the coordinateRead flag to true, then notifies all.
	 */
	public synchronized void notifyEventRead() {
		eventRead = true;
		newEventAvailable = false;
		notifyAll();
	}

	/**
	 * Access the current filtered coordinate.
	 * 
	 * @return - the filtered gaze point
	 */
	public Event getNewEvent() {
		return newEvent;
	}

}
