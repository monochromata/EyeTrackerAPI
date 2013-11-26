package rit.eyeTrackingAPI.EyeTrackerUtilities.udpClients;

import java.io.IOException;
import java.net.BindException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Map;

import rit.eyeTrackingAPI.Event;
import rit.eyeTrackingAPI.EventImpl;

public class ITUGazeTrackerComm extends EyeTrackerClient {
	private DatagramSocket ds;
	private DatagramPacket dp;
	private boolean stop = false;
	private boolean toggleOn = true;
	private InetAddress iViewX = null;
	private InetAddress ituGT;

	public ITUGazeTrackerComm() {
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
					// TODO: provide tracker attributes
					Map<String,Object> attr = new HashMap<String,Object>();
					attr.put(Event.CLIENT_TIMESTAMP_MS, System.currentTimeMillis());
					attr.put(Event.POR_X, (int)Double.parseDouble(tokens[2]));
					attr.put(Event.POR_Y, (int) Double.parseDouble(tokens[3]));
					filter.filter(new EventImpl(attr));
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
	
	public void calibrateAndStartTracking() {
		throw new RuntimeException("Not implemented");
	}
}