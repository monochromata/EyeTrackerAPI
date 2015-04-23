package rit.eyeTracking.EyeTrackerUtilities.udpClients;

import java.io.IOException;
import java.net.BindException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.graphics.Point;

import rit.eyeTracking.Event;
import rit.eyeTracking.AbstractEvent;
import rit.eyeTracking.EventFactory;
import rit.eyeTracking.EyeTrackerUtilities.calibration.SWTCalibration;

/**
 * TODO: Add support for calibration and validation
 */
public class ITUGazeTrackerComm extends EyeTrackerClient<Event> {
	private final EventFactory<Event> eventFactory;
	private DatagramSocket ds;
	private DatagramPacket dp;
	private boolean stop = false;
	private boolean toggleOn = true;
	private InetAddress iViewX = null;
	private InetAddress ituGT;

	public ITUGazeTrackerComm(EventFactory<Event> eventFactory) {
		this.eventFactory = eventFactory;
	}

	@Override
	protected void clientOperation() {
		if (!connected) {
			connect();
		}

		while (!stop) {
			String gtString = null;

			try {
				ds.receive(dp);
				gtString = new String(dp.getData(), 0, dp.getLength());
			} catch (IOException e) {
				e.printStackTrace();
			}

			if (gtString != null) {
				String[] tokens = gtString.split(" ");

				if (toggleOn) {
					Map<String,Object> attr = new HashMap<String,Object>();
					attr.put(Event.CLIENT_TIMESTAMP_MS, System.currentTimeMillis());
					attr.put(Event.POR_X, (int)Double.parseDouble(tokens[2]));
					attr.put(Event.POR_Y, (int) Double.parseDouble(tokens[3]));
					filter.filter(eventFactory.createEvent(Event.RAW_EVENT, attr));
				}
			}
		}
	}

	@Override
	public void connect() {
		byte[] b = new byte[50];
		dp = new DatagramPacket(b, b.length);
		ituGT = InetAddress.getLoopbackAddress();

		// ds = new DatagramSocket(7777);
		try {
			ds = new DatagramSocket(6666);
		} catch (BindException ex) {
			System.err
					.println("failed to connect to socket, another program may be using it.");
		} catch (SocketException e) {
			for (StackTraceElement ste : e.getStackTrace()) {
				System.out.println(ste.toString());
			}
		}
	}

	@Override
	public void disconnect() {
		ds.close();
		connected = false;
	}

	@Override
	public boolean isConnected() {
		return connected;
	}

	@Override
	public boolean isTracking() {
		return connected;
	}

	@Override
	public void requestStop() {
		stop = true;
	}

	@Override
	public boolean toggle() {
		// TODO: Should not disconnect, as per the specification in the super-class
		if (connected) {
			disconnect();
		} else {
			connect();
		}
		return connected;
	}

	/**
	 * Does nothing, tracker-internal buffer not supported.
	 */
	@Override
	public void sendBufferMessage(String message) {
	}

	@Override
	public void abortCalibration() throws IOException,
			UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void validate(int numberOfPoints, SWTCalibration calibration,
			CalibrationListener listener) throws IOException,
			UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void validate(Point[] points, SWTCalibration calibration,
			CalibrationListener listener) throws IOException,
			UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void abortValidation() throws IOException,
			UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void calibrate(Eye eye, SWTCalibration calibration,
			CalibrationListener listener) throws IOException,
			UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void calibrate(Eye eye, int numberOfPoints,
			SWTCalibration calibration, CalibrationListener listener)
			throws IOException, UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}
}