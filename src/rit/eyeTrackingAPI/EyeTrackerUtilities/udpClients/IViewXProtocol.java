package rit.eyeTrackingAPI.EyeTrackerUtilities.udpClients;

import java.io.IOException;

/**
 * Implemented by plugins that handle communication with specific devices
 * of SMI's iViewX family of devices.
 */
public interface IViewXProtocol {
	
	public String getFormatString(IViewXComm com) throws IOException;
	
	public String getRAWBothResponsePattern();
	
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
