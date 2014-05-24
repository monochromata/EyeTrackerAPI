package rit.eyeTracking.EyeTrackerUtilities.udpClients;

import java.io.IOException;

import org.eclipse.swt.graphics.Point;

/**
 * Implemented by plugins that handle communication with specific devices
 * of SMI's iViewX family of devices.
 * 
 * TODO: Add support for eye image recording with a duration in milliseconds
 * 		to support lossless (buffered) recording for a limited duration.
 */
public interface IViewXProtocol {
	
	public String getFormatString(IViewXComm com) throws IOException;
	
	public String getRAWBothResponsePattern();
	
	/**
	 * Implementations should run the calibration routine of the IViewX. If validate
	 * is true, the calibration results should be validated. This method should
	 * return immediately. Calibration and validation should be performed in a
	 * separate thread. When calibration and validation have been completed,
	 * 
	 * 
	 * @param com The instance of the IViewXComm interface to communicate to the IViewX
	 * @param listener The listener to inform about success/abortion of calibration/validation
	 * @throws IOException If a network I/O error occurs during communication
	 */
	public void calibrate(IViewXComm com, CalibrationListener listener) throws IOException;

	/**
	 * Implementations should run the calibration routine of the IViewX. If validate
	 * is true, the calibration results should be validated. This method should
	 * return immediately. Calibration and validation should be performed in a
	 * separate thread. When calibration and validation have been completed,
	 * 
	 * 
	 * @param com The instance of the IViewXComm interface to communicate to the IViewX
	 * @param listener The listener to inform about success/abortion of calibration/validation
	 * @throws IOException If a network I/O error occurs during communication
	 */
	public void calibrate(IViewXComm com, int numberOfPoints, CalibrationListener listener) throws IOException;
	
	public void abortCalibration(IViewXComm com) throws IOException;
	
	public void validate(IViewXComm com, Point[] points, CalibrationListener listener) throws IOException;
	
	public void abortValidation(IViewXComm com) throws IOException;
	
	/**
	 * Implementations should tell the IViewX to start sending data, if it
	 * does not send data automatically.
	 * 
	 * @param com The instance of the IViewXComm interface to communicate to the IViewX
	 * @throws IOException If a network I/O error occurs during communication
	 */
	public void startTracking(IViewXComm com) throws IOException;
	
	/**
	 * Implementations should tell the IViewX to stop sending data, if they
	 * told it to start sending initially.
	 * 
	 * @param com The instance of the IViewXComm interface to communicate to the IViewX
	 * @throws IOException If a network I/O error occurs during communication
	 */
	public void stopTracking(IViewXComm com) throws IOException;
}
