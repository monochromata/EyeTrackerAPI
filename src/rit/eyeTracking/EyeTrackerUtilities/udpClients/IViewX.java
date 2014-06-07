package rit.eyeTracking.EyeTrackerUtilities.udpClients;

import rit.eyeTracking.Event;

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
	
}
