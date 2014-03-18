package rit.eyeTracking.SmoothingFilters;

import rit.eyeTracking.Event;

/**
 * Perform no filtering and pass on raw gaze data.
 * 
 * @author Sebastian Lohmeier <sl@monochromata.de>
 *
 */
public class NoFilter extends Filter {

	@Override
	public synchronized void filter(Event e) {
		newEvent = e;
		newEventAvailable = true;
		notifyAll();
		while (!eventRead) {
			try {
				wait();
			} catch (InterruptedException ie) {
				System.err.println("NoFilter: interrupted while waiting for e="+e.getID()+" to be filtered");
				ie.printStackTrace();
			}
		}
	}

}
