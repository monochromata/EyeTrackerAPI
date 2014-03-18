package rit.eyeTracking.EyeTrackerUtilities.udpClients;

public interface CalibrationListener {
	
	/**
	 * To be invoked by the implementation of {@link IViewXProtocol#calibrate(IViewXComm, boolean)}
	 * when calibration and validation have been completed successfully.
	 */
	public void success();
	
	/**
	 * To be invoked by implementations of {@link IViewXProtocol#calibrate(IViewXComm, boolean)}
	 * when calibration or validation was aborted.
	 */
	public void aborted();
}
