package rit.eyeTracking.EyeTrackerUtilities.udpClients;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.graphics.Point;

import rit.eyeTracking.EyeTrackerUtilities.calibration.SWTCalibration;
import rit.eyeTracking.SmoothingFilters.Filter;

/**
 * An abstract class for communicating and receiving gaze sample points from an
 * eye tracker via UDP.
 * 
 * @author Corey Engelman
 * 
 */
public abstract class EyeTrackerClient extends Thread {
	
	public enum Eye {
		LEFT, RIGHT, BOTH
	}
	
	/**
	 * Represents the current sample point from the eye tracker.
	 */
	protected Filter filter;
	private final List<Listener> listeners = new ArrayList<Listener>();

	/**
	 * A flag for whether or not this client object is connected to the port the
	 * eye tracker will be sending points to. Does not guarantee connection with
	 * the eye tracker.
	 */
	protected boolean connected = false;

	/**
	 * Creates a new eye Tracker client instance
	 */
	public EyeTrackerClient() {
		this.setName(getClass().getSimpleName());
	}
	
	public void configure(Filter filter, Configuration config) {
		this.filter = filter;
	}
	
	public void addListener(Listener listener) {
		listeners.add(listener);
	}
	
	public void removeListener(Listener listener) {
		listeners.remove(listener);
	}
	
	protected void input(String in) {
		for(Listener l: listeners) {
			l.input(in);
		}
	}
	
	protected void output(String out) {
		for(Listener l: listeners) {
			l.output(out);
		}
	}
	
	protected void info(String info) {
		for(Listener l: listeners) {
			l.info(info);
		}
	}
	
	protected void error(Throwable t) {
		for(Listener l: listeners) {
			l.error(t);
		}
	}

	/**
	 * The primary method of execution for this client thread.
	 */
	@Override
	public void run() {
		clientOperation();
	}
	
	public Filter getFilter() {
		return filter;
	}

	/**
	 * A method that connects the client object to the port that it will be
	 * receiving coordinates from the eye tracker on.
	 */
	public abstract void connect() throws IOException;

	/**
	 * A method that disconnects the client object from the socket it is
	 * receiving input from the eye tracker on.
	 */
	public abstract void disconnect() throws IOException;

	/**
	 * An accessor for the flag "connected"
	 * 
	 * @return - the value of the boolean connected
	 */
	public abstract boolean isConnected();

	public abstract boolean isTracking();
	
	/**
	 * Toggle the eye tracker on/off without disconnecting.
	 * 
	 * @return Whether or not the client receives eye tracking
	 * 		data after this method returned.
	 */
	public abstract boolean toggle() throws IOException;
	
	/**
	 * Send a method to the internal buffer of the eye tracker. If the eye
	 * tracker does not have an internal buffer or the client is not
	 * connected to the eye tracker, this method has no effect.
	 * 
	 * @param message The message to send to the internal buffer.
	 * @throws IOException If an I/O problem occurs
	 */
	public abstract void sendBufferMessage(String message) throws IOException;
	
	/**
	 * Start calibration procedure. This method should only start calibration
	 * and return immediately.
	 * 
	 * @param listener 
	 * @throws IOException In an error occurs while calibration is started
	 * @throws UnsupportedOperationException If the client does not support calibration.
	 */
	public abstract void calibrate(Eye eye, SWTCalibration calibration, CalibrationListener listener)
			throws IOException, UnsupportedOperationException;
	
	public abstract void calibrate(Eye eye, int numberOfPoints, SWTCalibration calibration,
					CalibrationListener listener)
			throws IOException, UnsupportedOperationException;
	
	public abstract void abortCalibration()
			throws IOException, UnsupportedOperationException;
	
	/**
	 * Perform standard validation using the points used for calibration.
	 * 
	 * @param numberOfPoints
	 * @param calibration
	 * @param listener
	 * @throws IOException
	 * @throws UnsupportedOperationException
	 */
	public abstract void validate(int numberOfPoints, SWTCalibration calibration,
					CalibrationListener listener)
			throws IOException, UnsupportedOperationException;
	
	/**
	 * Perform extended validation using the given points which should lie
	 * between the standard calibration points in regions typically looked
	 * at by subjects.
	 * 
	 * @param points
	 * @param calibration
	 * @param listener
	 * @throws IOException
	 * @throws UnsupportedOperationException
	 */
	public abstract void validate(Point[] points, SWTCalibration calibration,
					CalibrationListener listener)
			throws IOException, UnsupportedOperationException;
	
	public abstract void abortValidation()
			throws IOException, UnsupportedOperationException;
	
	
	/**
	 * The operation specific to receiving coordinates from the eye tracker and
	 * passing them on to the filter should be implemented in this method.
	 */
	protected abstract void clientOperation();

	/**
	 * Gracefully stops the loop in the threads run method by changing the stop
	 * flag.
	 */
	public abstract void requestStop();

	public interface Listener {
		public void input(String in);
		public void output(String out);
		public void info(String info);
		public void error(Throwable t);
	}
	
	/**
	 * A configuration object to be passed to the IViewXComm upon
	 * construction.
	 * 
	 * @author Sebastian Lohmeier <sl@monochromata.de>
	 */
	public static class Configuration {
		
		/**
		 * The InetAddress at which the eye tracker is listening for commands.
		 */
		private InetAddress trackerIP;
		
		/**
		 * The UDP port at which the eye tracker is listening for commands.
		 */
		private int trackerPort;
		
		/**
		 * The local port at which the client is listening for commands.
		 */
		private int localPort;
		
		/**
		 * The rate at which the eye tracker should sample the eye position.
		 */
		private int samplingRate;
		
		/**
		 * Whether the transmission format of the eye tracker should be set (true)
		 * or the format already in use by the tracker should be parsed (false).
		 */
		private boolean setFormat;
		
		public InetAddress getTrackerIP() {
			return trackerIP;
		}
		public void setTrackerIP(InetAddress trackerIP) {
			this.trackerIP = trackerIP;
		}
		public int getTrackerPort() {
			return trackerPort;
		}
		public void setTrackerPort(int trackerPort) {
			this.trackerPort = trackerPort;
		}
		public int getLocalPort() {
			return localPort;
		}
		public void setLocalPort(int localPort) {
			this.localPort = localPort;
		}
		public int getSamplingRate() {
			return samplingRate;
		}
		public void setSamplingRate(int samplingRate) {
			this.samplingRate = samplingRate;
		}
		public boolean getSetFormat() {
			return setFormat;
		}
		public void setSetFormat(boolean setFormat) {
			this.setFormat = setFormat;
		}
	}
}
