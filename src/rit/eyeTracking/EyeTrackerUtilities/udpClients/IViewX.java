package rit.eyeTracking.EyeTrackerUtilities.udpClients;

import rit.eyeTracking.Event;
import rit.eyeTracking.Event.IDImpl;

/**
 * iViewX-related event constants
 */
public class IViewX {

	public static final Event.ID CALIBRATION_STARTED = new Event.IDImpl("ET_CAL");
	// TODO: ET_CSZ
	public static final Event.ID CALIBRATION_POINT_DATA = new Event.IDImpl("ET_PNT");
	public static final Event.ID CALIBRATION_POINT_CHANGE = new Event.IDImpl("ET_CHG");
	public static final Event.ID CALIBRATION_ABORTED = new Event.IDImpl("ET_BRK");
	public static final Event.ID CALIBRATION_SUCCESSFUL = new Event.IDImpl("ET_FIN");

	public static final Event.ID CALIBRATION_ACCURRACY = new Event.IDImpl("ET_VLS");
	public static final Event.ID VALIDATION_ACCURRACY = new Event.IDImpl("ET_VLX");
	
	public static final Event.ID USER_EVENT = new Event.IDImpl("UserEvent");
	
	public static final String PNT_INDEX = "PNT_INDEX";
	public static final String PNT_X = "PNT_X";
	public static final String PNT_Y = "PNT_Y";
	
	public static final String RMSX = "RMSX";
	public static final String RMSY = "RMSY";
	public static final String RMSXL = "RMSXL";
	public static final String RMSYL = "RMSYL";
	public static final String RMSXR = "RMSXR";
	public static final String RMSYR = "RMSYR";
	public static final String RMSD = "RMSD";
	
	public static final String XD = "XD";
	public static final String YD = "YD";
	public static final String XDL = "XDL";
	public static final String YDL = "YDL";
	public static final String XDR = "XDR";
	public static final String YDR = "YDR";
	
	public static final String TRIAL = "Trial";
	public static final String NUMBER = "Number";
	public static final String DESCRIPTION = "Description";
	
	public static final String DISPERSION_X = "Dispersion X";
	public static final String DISPERSION_Y = "Dispersion Y";
	public static final String AVG_PUPIL_SIZE_X = "Avg. Pupil Size X";
	public static final String AVG_PUPIL_SIZE_Y = "Avg Pupil Size Y";
	
	public static final String SACC_AMPLITUDE = "Amplitude";
	public static final String SACC_PEAK_SPEED = "Peak Speed";
	public static final String SACC_PEAK_SPEED_AT = "Peak Speed At";
	public static final String SACC_AVG_SPEED = "Average Speed";
	public static final String SACC_PEAK_ACCEL = "Peak Accel.";
	public static final String SACC_PEAK_DECEL = "Peak Decel.";
	public static final String SACC_AVG_ACCEL = "Average Accel.";
}
