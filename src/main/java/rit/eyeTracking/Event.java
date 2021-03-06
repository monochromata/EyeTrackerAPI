package rit.eyeTracking;

import java.io.Serializable;
import java.util.Map;

/**
 * An event contains a map of attributes that can be extended but not modified.
 * 
 * TODO: Remove dependency on TET attributes by using subclasses as events
 */
public interface Event {

	public static final String RAW_EVENT_ID = "RAW";
	/**
	 * Name of the vendor-neutral standard {@link Boolean} attribute
	 * that is set to true for events that denote non-aggregated eye
	 * samples. The attribute will not available for events generated by
	 * fixation detection algorithms or other aggregate events or
	 * events that do not denote eye data.
	 * 
	 * At least {@link #EYE_TYPE}, {@link #POR_X} and {@link #POR_Y}
	 * attributes will be present in conjunction with this attribute.
	 * 
	 * @see #FIXATION_START
	 * @see #FIXATION_END
	 */
	public static final ID RAW_EVENT = new IDImpl(RAW_EVENT_ID);

	public static final String FIXATION_START_ID = "FS";
	/**
	 * Name of the vendor-neutral standard {@link Boolean} attribute
	 * that is set to true if the event signals the onset of a fixation.
	 * The attribute does not distinguish between on- and offline fixation
	 * detection. I.e. it may occur after the actual onset of the fixation.
	 * The attribute is never set to false. It will be missing for events
	 * that do not denote the start of a fixation.
	 * 
	 * TODO: Need to define values for fixation durations and
	 * define/leave open what what other attributes will/can be set
	 * in conjunction with this attribute.
	 * TODO: Distinguish between on-line and off-line fixations either
	 * via the event ID or via attributes (e.g. a missing duration attribute
	 * for FIXATION_START could indicate a fixation that was detected on-line)
	 * 
	 * @see #FIXATION_END
	 * @see #RAW_EVENT
	 */
	public static final ID FIXATION_START = new IDImpl(FIXATION_START_ID);
	
	public static final String FIXATION_END_ID = "FE";
	/**
	 * Name of the vendor-neutral standard {@link Boolean} attribute
	 * that is set to true if the event signals the end of a fixation.
	 * The attribute does not distinguish between on- and offline fixation
	 * detection. I.e. it may occur the actual end of the fixation. The
	 * attribute is never set to false. It will be missing for events that do
	 * not denote the end of a fixation.
	 * 
	 * TODO: Need to define values for fixation durations and
	 * define/leave open what what other attributes will/can be set
	 * in conjunction with this attribute.
	 * 
	 * @see #FIXATION_START
	 * @see #RAW_EVENT
	 */
	public static final ID FIXATION_END = new IDImpl(FIXATION_END_ID);
	
	public static final String SACCADE_START_ID = "SaccS";
	public static final ID SACCADE_START = new IDImpl(SACCADE_START_ID);
	
	public static final String SACCADE_END_ID = "SaccE";
	public static final ID SACCADE_END = new IDImpl(SACCADE_END_ID);

	public static final String BLINK_ID = "BL";
	/**
	 * A blink event to be detected when a single data sample or a short
	 * sequence of data samples does not contain eye coordinates.
	 * 
	 * @deprecated Use BLINK_START and BLINK_END instead
	 */
	public static final ID BLINK = new IDImpl(BLINK_ID);

	public static final String BLINK_START_ID = "BS";
	public static final ID BLINK_START = new IDImpl(BLINK_START_ID);
	
	public static final String BLINK_END_ID = "BE";
	public static final ID BLINK_END = new IDImpl(BLINK_END_ID);
	
	// TODO: It may be desirable to provide microsecond-timestamps in the future
	
	/**
	 * Name of the vendor-neutral standard {@link Long} attribute that
	 * contains the timestamp the eye-tracker sent for this event in milliseconds. The
	 * timestamp can be relative to an un-specified start time and it
	 * is not specified how much time has elapsed since the capture of
	 * the eye image.
	 * 
	 * The timestamp may be used to compute the time elapsed between
	 * two events. Sequences of this timestamp may also be compared to
	 * corresponding sequences of {@link #CLIENT_TIMESTAMP_MS} to measure
	 * the latency that events are subject to during transmission from
	 * eye tracker to client.
	 * 
	 * Note that this attribute is not set for on-line fixation detection.
	 * Instead, {@link #FIXATION_START_TIMESTAMP_MS} and {@link #FIXATION_END_TIMESTAMP_MS}
	 * will be reported with a delay of at least the number of
	 * milliseconds that are defined as fixation detection threshold.
	 * 
	 * @see #CLIENT_TIMESTAMP_MS
	 * @see #FIXATION_START_TIMESTAMP_MS
	 * @see #FIXATION_END_TIMESTAMP_MS
	 * @see #RAW_EVENT
	 */
	public static final String TRACKER_TIMESTAMP_MS = "eyetracking.api.TRACKER_TIMESTAMP_MS";
	
	/**
	 * Tracker timestamp {@link Long} in microseconds.
	 */
	public static final String TRACKER_TIMESTAMP_MU = "eyetracking.api.TRACKER_TIMESTAMP_MU";
	
	/**
	 * long
	 */
	public static final String DURATION_MU = "DUR_MU";
	
	/**
	 * long
	 */
	public static final String FIXATION_START_TIMESTAMP_MS = "FIX_START_MS";

	/**
	 * long
	 */
	public static final String FIXATION_END_TIMESTAMP_MS = "FIX_END_MS";

	/**
	 * long
	 */
	public static final String FIXATION_DURATION_MS = "FIX_DUR_MS";
	
	/**
	 * Name of the vendor-neutral standard {@link Long} attribute
	 * that contains the timestamp for when the client application
	 * received the event information from the eye tracker in milliseconds. The
	 * timestamp is in the format provided by {@link System#currentTimeMillis()}.
	 * 
	 * @see #TRACKER_TIMESTAMP_MS
	 */
	public static final String CLIENT_TIMESTAMP_MS = "eyetracking.api.CLIENT_TIMESTAMP_MS";
	
	/**
	 * Name of the vendor-neutral standard {@link String} attribute that
	 * allows values "b", "l" and "r" to denote that the data of
	 * the event is valid for both eyes, the left, or the right
	 * eye only.
	 * 
	 * This attribute is always present for {@link #RAW_EVENT},
	 * {@link #FIXATION_START} and {@link #FIXATION_END} events.
	 */
	public static final String EYE_TYPE = "eyetracking.api.EYE_TYPE";

	/**
	 * Name of the vendor-neutral standard {@link Integer} attribute
	 * that contains an on-screen x coordinate of the point of regard.
	 * The attribute is present at least for {@link #RAW}, {@link #FIXATION_START}
	 * and {@link #FIXATION_END} events.
	 */
	public static final String POR_X = "POR_X";
	
	/**
	 * Name of the vendor-neutral standard {@link Integer} attribute
	 * that contains an on-screen y coordinate of the point of regard.
	 * The attribute is present at least for {@link #RAW}, {@link #FIXATION_START}
	 * and {@link #FIXATION_END} events.
	 */
	public static final String POR_Y = "POR_Y";

	/**
	 * Name of the vendor-neutral standard {@link Float} attribute
	 * that contains the deviation of the left eye from the center of the
	 * screen along the x-axis in millimeters. Negative values signal
	 * deviations to the left, positive values signal deviations to
	 * the right.
	 * 
	 * This attribute may optionally be present whenever a {@link #POR_X}
	 * attribute is present.
	 */
	public static final String EYE_X_L = "eyetracking.api.EYE_X_L";
	
	/**
	 * Name of the vendor-neutral standard {@link Float} attribute
	 * that contains the deviation of the left eye from the center of the
	 * screen along the y-axis in millimeters. Negative values signal
	 * deviations towards the bottom, positive values signal deviations
	 * towards the top of the screen.
	 * 
	 * This attribute may optionally be present whenever a {@link #POR_Y}
	 * attribute is present.
	 */
	public static final String EYE_Y_L = "eyetracking.api.EYE_Y_L";
	
	/**
	 * Name of the vendor-neutral standard {@link Float} attribute
	 * that contains the distance of the left eye from the screen's surface
	 * in millimeters.
	 * 
	 * This attribute may optionally be present whenever a {@link #POR_X}
	 * attribute is present.
	 */
	public static final String EYE_Z_L = "eyetracking.api.EYE_Z_L";
	
	/**
	 * Name of the vendor-neutral standard {@link Float} attribute
	 * that contains the deviation of the right eye from the center of the
	 * screen along the x-axis in millimeters. Negative values signal
	 * deviations to the left, positive values signal deviations to
	 * the right.
	 * 
	 * This attribute may optionally be present whenever a {@link #POR_X}
	 * attribute is present.
	 */
	public static final String EYE_X_R = "eyetracking.api.EYE_X_R";
	
	/**
	 * Name of the vendor-neutral standard {@link Float} attribute
	 * that contains the deviation of the right eye from the center of the
	 * screen along the y-axis in millimeters. Negative values signal
	 * deviations towards the bottom, positive values signal deviations
	 * towards the top of the screen.
	 * 
	 * This attribute may optionally be present whenever a {@link #POR_Y}
	 * attribute is present.
	 */
	public static final String EYE_Y_R = "eyetracking.api.EYE_Y_R";
	
	/**
	 * Name of the vendor-neutral standard {@link Float} attribute
	 * that contains the distance of the right eye from the screen's surface
	 * in millimeters.
	 * 
	 * This attribute may optionally be present whenever a {@link #POR_X}
	 * attribute is present.
	 */
	public static final String EYE_Z_R = "eyetracking.api.EYE_Z_R";

	/**
	 * {@link Float} value with diameter of left pupil in pixels
	 */
	public static final String PUPIL_DIA_L_PX = "eyetracking.api.PUPIL_DIA_L_PX";

	/**
	 * {@link Float} value with diameter of right pupil in pixels
	 */
	public static final String PUPIL_DIA_R_PX = "eyetracking.api.PUPIL_DIA_R_PX";

	/**
	 * {@link Float} value with diameter of left pupil in millimeters
	 */
	public static final String PUPIL_DIA_L_MM = "eyetracking.api.PUPIL_DIA_L_MM";

	/**
	 * {@link Float} value with diameter of right pupil in millimeters
	 */
	public static final String PUPIL_DIA_R_MM = "eyetracking.api.PUPIL_DIA_R_MM";
	
	/**
	 * {@link Float} value with x coordinate of position of left pupil in pixels.
	 */
	public static final String PUPIL_POS_X_L = "eyetracking.api.PUPIL_POS_X_L";
	
	/**
	 * {@link Float} value with x coordinate of position of right pupil in pixels.
	 */
	public static final String PUPIL_POS_X_R = "eyetracking.api.PUPIL_POS_X_R";
	
	/**
	 * {@link Float} value with y coordinate of position of left pupil in pixels.
	 */
	public static final String PUPIL_POS_Y_L = "eyetracking.api.PUPIL_POS_Y_L";
	
	/**
	 * {@link Float} value with y coordinate of position of right pupil in pixels.
	 */
	public static final String PUPIL_POS_Y_R = "eyetracking.api.PUPIL_POS_Y_R";
	
	/**
	 * {@link Float} value with x coordinate of position of left-eye corneal reflex in pixels.
	 */
	public static final String CORNEAL_REFLEX_POS_X_L = "eyetracking.api.CR_POS_X_L";
	
	/**
	 * {@link Float} value with x coordinate of position of right-eye corneal reflex in pixels.
	 */
	public static final String CORNEAL_REFLEX_POS_X_R = "eyetracking.api.CR_POS_X_R";
	
	/**
	 * {@link Float} value with y coordinate of position of left-eye corneal reflex in pixels.
	 */
	public static final String CORNEAL_REFLEX_POS_Y_L = "eyetracking.api.CR_POS_Y_L";
	
	/**
	 * {@link Float} value with y coordinate of position of right-eye corneal reflex in pixels.
	 */
	public static final String CORNEAL_REFLEX_POS_Y_R = "eyetracking.api.CR_POS_Y_R";
	
	
	
	

	public static final String TET_STATE = "tet.STATE";
	public static final String TET_POR_RAW_X = "tet.POR_RAW_X";
	public static final String TET_POR_RAW_Y = "tet.POR_RAW_Y";
	public static final String TET_POR_SMOOTHED_X = "tet.POR_SMOOTHED_X";
	public static final String TET_POR_SMOOTHED_Y = "tet.POR_SMOOTHED_Y";
	public static final String TET_PUPIL_SIZE_L = "tet.PUPIL_SIZE_L";
	public static final String TET_PUPIL_SIZE_R = "tet.PUPIL_SIZE_R";
	public static final String TET_PUPIL_LR_DISTANCE = "tet.PUPIL_LR_DISTANCE";
	public static final String TET_PUPIL_CENTER_L_X = "tet.PUPIL_CENTER_L_X";
	public static final String TET_PUPIL_CENTER_L_Y = "tet.PUPIL_CENTER_L_Y";
	public static final String TET_PUPIL_CENTER_R_X = "tet.PUPIL_CENTER_R_X";
	public static final String TET_PUPIL_CENTER_R_Y = "tet.PUPIL_CENTER_R_Y";
	public static final String TET_POR_RAW_L_X = "tet.POR_RAW_L_X";
	public static final String TET_POR_RAW_L_Y = "tet.POR_RAW_L_Y";
	public static final String TET_POR_RAW_R_X = "tet.POR_RAW_R_X";
	public static final String TET_POR_RAW_R_Y = "tet.POR_RAW_R_Y";
	public static final String TET_POR_SMOOTHED_L_X = "tet.POR_SMOOTHED_L_X";
	public static final String TET_POR_SMOOTHED_L_Y = "tet.POR_SMOOTHED_L_Y";
	public static final String TET_POR_SMOOTHED_R_X = "tet.POR_SMOOTHED_R_X";
	public static final String TET_POR_SMOOTHED_R_Y = "tet.POR_SMOOTHED_R_Y";
	
	
	/**
	 * Returns the {@link ID} that identified the type of event.
	 * 
	 * @return The identifier of the event type.
	 */
	public ID getID();
	
	/**
	 * Adds an attribute to the event.
	 * 
	 * @param name
	 *            A unique name for the attribute, e.g. prefixed by the name of
	 *            the package of the class that adds it. Names of vendor-neutral
	 *            standard attributes are defined by this class:
	 *            {@link #TRACKER_TIMESTAMP_MS}, {@link #TRACKER_TIMESTAMP_MU}, {@link #CLIENT_TIMESTAMP_MS}
	 *            {@link #EYE_TYPE},
	 *            {@link #RAW_EVENT}, {@link #FIXATION_START}, {@link #FIXATION_END},
	 *            {@link #POR_X}, {@link #POR_Y},
	 *            {@link #EYE_X_L}, {@link #EYE_Y_L}, {@link #EYE_Z_L},
	 *            {@link #EYE_X_R}, {@link #EYE_Y_R}, {@link #EYE_Z_R}
	 * @param value
	 *            An immutable object.
	 */
	public void addAttribute(String name, Object value);

	/**
	 * Get the value of the named attribute in this event, or null, if
	 * no such attribute is available from the event.
	 * 
	 * The attributes of an event should be read before the notify method
	 * of the respective listener returns, for the event will be destroyed
	 * after the last listener has been notified.
	 * 
	 * TODO: Use a bitmap/int field for the most frequent boolean
	 * attributes (RAW, FE, FS, SAC, ...) / for event types
	 * 
	 * @param name The name of the attribute to return
	 * @return The value of attribute, if set, null otherwise.
	 * @see #addAttribute(String, Object)
	 * @see EyeTrackingListener#notify(Event)
	 */
	public <T> T getAttribute(String name);
	
	/**
	 * Returns a non-modifiable map of the attributes held by the event.
	 */
	public Map<String, Object> getAttributes();
	
	/**
	 * Returns a copy of the event with all attributes removed that
	 * are not serializable.
	 */
	public Event getSerializable(EventFactory factory);
	
	/**
	 * Returns true, if the event is sent to an {@link EyeTrackingListener}
	 * or a collection of {@link EyeTrackingListener}s for the first time.
	 * Returns false otherwise, i.e. in applications that can rewind events
	 * and send them to {@link EyeTrackingListener}s repeatedly, from the
	 * second time on.
	 * 
	 * Implementations of this interface should maintain an
	 * implementation-dependent and non-public way of setting the state
	 * returned by this accessor method.
	 * 
	 * @return Whether this event has been passed to this or other
	 * 	{@link EyeTrackingListeners} before, or not.
	 */
	public boolean isNew();
	
	
	
	/**
	 * Release all resources associated with the event.
	 */
	public void clear();
	
	/**
	 * An identifier for event types.
	 * 
	 * @author monochromata
	 */
	public interface ID extends Serializable {
		
		/**
		 * Returns a short human-readable string representing the event ID in
		 * log files. The string should be chosen to avoid ID clashes (i.e. when
		 * logging raw data of an eye tracker of manufacturer XYZ, do not use
		 * RAW that is used for generic RAW data but RAW_XYZ oder XYZ_RAW).
		 * 
		 * Event logging implementations will check before logging whether two
		 * IDs that it may log have the same loggable id and must not start logging
		 * in case of such conflicts but report them and refuse logging instead.
		 * 
		 * @return A loggable id
		 */
		public String getLoggableID();
	}
	
	public class IDImpl implements ID {
		private final String id;
		
		public IDImpl(String id) {
			this.id = id;
		}

		@Override
		public String getLoggableID() {
			return id;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((id == null) ? 0 : id.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			IDImpl other = (IDImpl) obj;
			if (id == null) {
				if (other.id != null)
					return false;
			} else if (!id.equals(other.id))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return id;
		}
	}
}
