package rit.eyeTracking;

/**
 * Implemented by objects that want to be notified of eye tracking
 * events, be they generated by an eye tracking device or by replay
 * from a file.
 * 
 * @param <E> The type of event passed through the filter chain
 * 	({@link Event} by default)
 * 
 * @see Source
 * @see rit/eyeTrackingAPI/EyeTrackerUtilities/udpClients/EyeTrackerClient
 */
public interface EyeTrackingListener<E extends Event> {
	
	public enum Mode {
		// TODO: Add an INTERACTIVE_REPLAY_MODE so filters can apply measures to avoid
		// re-generating data and keeping track of already-performed actions only in
		// interactive mode where previous events can be replayed multiple times.
		RECORDING_MODE, CALIBRATION_MODE, VALIDATION_MODE, TRACKING_MODE, REPLAY_MODE, BATCH_MODE, INTERACTIVE_REPLAY_MODE;
	
		public boolean isReplay() {
			return this == REPLAY_MODE || this == BATCH_MODE || this == INTERACTIVE_REPLAY_MODE;
		}
		
		public String toString() {
			if(RECORDING_MODE == this) {
				return "RECORDING_MODE";
			} else if(CALIBRATION_MODE == this) {
				return "CALIBRATION_MODE";
			} else if(VALIDATION_MODE == this) {
				return "VALIDATION_MODE";
			} else if(TRACKING_MODE == this) {
				return "TRACKING_MODE";
			} else if(REPLAY_MODE == this) {
				return "REPLAY_MODE";
			} else if(BATCH_MODE == this) {
				return "BATCH_MODE";
			} else if(INTERACTIVE_REPLAY_MODE == this) {
				return "INTERACTIVE_REPLAY_MODE";
			} else {
				return super.toString();
			}
		}
		
		public String getActionString() {
			switch(this) {
			case RECORDING_MODE: return "Record";
			case CALIBRATION_MODE: return "Calibrate";
			case VALIDATION_MODE: return "Validate";
			case TRACKING_MODE: return "Track eyes";
			case REPLAY_MODE: return "Replay";
			case BATCH_MODE: return "Batch replay";
			case INTERACTIVE_REPLAY_MODE: return "Interactive replay";
			default: return toString();
			}
		}
	};
	
	/**
	 * Notifies the listener of the given eye tracking event.
	 * 
	 * The attributes of the event should be read before the
	 * notify method returns, for the event will be destroyed
	 * after the last listener has been notified.
	 * 
	 * @param e The eye tracking event
	 */
	public void notify(E e, EyeTrackingListener<E> listener, Mode mode);
}
