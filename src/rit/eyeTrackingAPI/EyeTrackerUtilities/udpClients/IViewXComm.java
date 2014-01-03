package rit.eyeTrackingAPI.EyeTrackerUtilities.udpClients;

import java.io.IOException;
import java.net.BindException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.Logger;

import rit.eyeTrackingAPI.Event;
import rit.eyeTrackingAPI.EventImpl;
import rit.eyeTrackingAPI.SmoothingFilters.Filter;

/**
 * A class used to set up communication with the mTrackerServerCommandAddress
 * eye tracking device. This class runs on a separate thread. Necessary, because
 * otherwise issues occur when communicating with the
 * mTrackerServerCommandAddress and trying to draw to the GLCanvas at the same
 * time.
 * 
 * TODO: Document how the class is instantiated and prepared for use
 * TODO: The class should be provided with a Protocol instance that implements
 *       the actual operation because commands seem to differ between RED and REDm.
 * 
 * @author Corey Engelman, Sebastian Lohmeier
 * 
 */
public class IViewXComm extends EyeTrackerClient {
	// <editor-fold defaultstate="expanded" desc="Private Members">

	private State state = new Disconnected();
	private Map<String,Set<MessageListener>> messageListeners = new Hashtable<String,Set<MessageListener>>();
	private boolean stop = false;
	private final boolean toggleOn = true;

	// Set up some members to handle the connection to the tracker, with
	// defaults
	// (Should be able to change these before connecting, or between
	// connections)
	private InetAddress trackerIP = null;
	
	{
		try {
			trackerIP = InetAddress.getByName("127.0.0.1");
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}
	
	private int trackerPort  = 6665;
	private int localPort 	 = 7777;
	// private int samplingRate = 60; // TODO: RED-dev doesn't care
	
	/**
	 * Whether the transmission format of the eye tracker should be set (true)
	 * or the format already in use by the tracker should be parsed (false).
	 */
	private boolean setETFormat = false;

	// Two sockets, one to receive incoming data and one to send commands to the
	// tracker
	private DatagramSocket mSendSocket;
	private DatagramSocket mReceiveSocket;

	// Packet object used to store incoming data from the tracker
	private DatagramPacket mUdpPacket;

	// </editor-fold>

	// <editor-fold defaultstate="expanded" desc="Constants">

	private static final int MAX_RESPONSE_SIZE_BYTES = 256;

	public static final String PING_COMMAND 					= "ET_PNG\n";
	
	public static final String FORMAT_REQUEST_PREFIX		    = "ET_FRM ";
	
	public static final String CMD_SAMPLE_RATE					= "ET_SRT\n";
	
	public static final String CMD_START_DATA_PREFIX			= "ET_STR "; // can have an optional frame rate
	public static final String CMD_START_DATA				    = "ET_STR\n";
	public static final String CMD_STOP_DATA					= "ET_EST\n";
	
	public static final String CMD_START_5_PT_CALIBRATION		= "ET_CAL 5\n";
	public static final String CMD_CANCEL_CALIBRATION			= "ET_BRK\n";
	public static final String CMD_ACCEPT_CALIBRATION_POINT    	= "ET_ACC\n";
	public static final String CMD_VALIDATE						= "ET_VLS\n";
	
	public static final String MSG_START_CALIBRATION			= "ET_CAL";
	public static final String MSG_CALIBRATION_AREA				= "ET_CSZ";
	public static final String MSG_CALIBRATION_PT				= "ET_PNT";
	public static final String MSG_CALIBRATION_PT_CHANGE		= "ET_CHG";
	public static final String MSG_END_CALIBRATION			    = "ET_FIN";
	public static final String MSG_VALIDATION					= "ET_VLS";
	
	public static final String CMD_TRACKER_PARAMETER_PREFIX 	= "ET_SFT ";
	public static final String PARAM_LEFT_EYE			        = "0";
	public static final String PARAM_RIGHT_EYE			     	= "1";
	public static final String PARAM_PUPIL_THRESHOLD		 	= "0";
	public static final String PARAM_REFLEX_THRESHOLD			= "1";
	public static final String PARAM_SHOW_AOI				    = "2";
	public static final String PARAM_SHOW_CONTOUR				= "3";
	public static final String PARAM_SHOW_PUPIL				    = "4";
	public static final String PARAM_SHOW_REFLEX			    = "5";
	public static final String PARAM_DYNAMIC_THRESHOLD			= "6";
	public static final String PARAM_PUPIL_AREA 				= "11";
	public static final String PARAM_PUPIL_PERIMETER			= "12";
	public static final String PARAM_PUPIL_DENSITY			    = "13";
	public static final String PARAM_REFLEX_PERIMETER			= "14";
	public static final String PARAM_REFLEX_PUPIL_DISTANCE	    = "15";
	
	public static final String CMD_START_IMAGE_STREAMING 		= "ET_SIM\n";
	public static final String CMD_STOP_IMAGE_STREAMING			= "ET_EIM\n";
	
	public static final String CMD_START_IMAGE_RECORDING_PREFIX	= "ET_EVB ";
	public static final String CMD_STOP_IMAGE_RECORDING			= "ET_EVE\n";
	
	public static final String CMD_START_FIXATION_DETECTION 	= "ET_FIX\n";
	public static final String CMD_STOP_FIXATION_DETECTION 		= "ET_EFX\n";
	
	public static final String CMD_CALIBRATION_PARAMS			= "ET_CPA\n";
	
	public static final String RESPONSE_DATA_STRING 			= "ET_SPL";
	
	protected static final Pattern RESPONSE_PATTERN_FIXATION_START =
		Pattern.compile("\\AET_FIX\\s+(?<ET>l|r|b)\\s+(?<TU>\\d+)\\s+(?<SX>[0-9\\-\\.]+)\\s+(?<SY>[0-9\\-\\.]+)\\z");
	protected static final Pattern RESPONSE_PATTERN_FIXATION_END =
		Pattern.compile("\\AET_EFX\\s+(?<ET>l|r|b)\\s+(?<TUS>\\d+)\\s+(?<TUE>\\d+)\\s+(?<DU>\\d+)\\s+(?<SX>[0-9\\-\\.]+)\\s+(?<SY>[0-9\\-\\.]+)\\z");

	private final IViewXProtocol protocol;
	
	/*
 in:ET_SPL 100527771763 b 788 788 258 258 12.105186 10.957320 12.105186 10.957320 -59.875  3.226 14.819 17.609 586.966 580.848 379.807800 654.019907 438.804785 425.142153 3.09 2.82 3

out:ET_FRM "%ET %TS %SX %SY %EX %EY %EZ"
 in:ET_PNG
 
	ET_SPL  TU=103129336084
	        ET=b
	        SX=832 832
	        SY=319 319
	        DX=14.265623 12.244397
	        DY=14.265623 12.244397
	        EX=-51.677 11.544 
	        EY=9.126  4.790
	        EZ=603.360 603.782
	        CX=419.548971 685.696594
	        CY=473.498501 491.851173
	        PD=3.75 3.20
	        IP=3( 1 1)
 
 in:ET_SPL  TU=102649573844
  			ET=b
  			SX=724 724
  			SY=297 297
  			DX=15.250899 13.556884
  			DY=15.250899 13.556884
  			EX=-55.280  7.832
  			EY=17.499 17.234
  			EZ=580.189 568.010
  			CX=394.885471 671.835052
  			CY=435.313591 435.028160
  			PD? =396.590593 674.226717
  			    =426.261816 425.104691 
  			IP
 
 %TU %ET %SX %SY %DX %DY %EX %EY %EZ %CX %CY %PX %PY %SC
 
  in:ET_SPL TU=102172356422
  			ET=b
  			SX=855 855
  			SY=347 347
  			DX=13.327898 11.940896
  			DY=13.327898 11.940896
  			EX=-66.678 -2.977
  			EY=18.332 11.458
  			EZ=583.550 574.051
  			PX=346.699169 623.581102
  			PY=432.161839 461.460747 

 in:ET_SPL 102172364740 b 855 855 346 346 13.103793 11.830818 13.103793 11.830818 -66.588 -2.985 18.302 11.463 582.493 574.155 346.478830 623.560946 432.144375 461.448565 

 
 %TU %ET %SX %SY %DX %DY %EX %EY %EZ %CX %CY %SC
 
 in:ET_SPL 	TU=100527780227
 			ET=b
 			SX=788 788
 			SY=257 257
 			DX?=12.049115 11.051519
 			DY?=12.049115 11.051519
 			EX=-59.901 3.225
 			EY=14.819 17.598
 			EZ=587.220 580.746
 			PX?=379.857176 654.031593
 			PY?=438.774707 425.228177
 			CX/CY?=3.09 2.81
 			SC?=3
           %TU %ET SX SY
           %ET %TS %SX %SY %EX %EY %EZ
 in:ET_SPL b 100527788 790 790 255 255 -59.923  3.225 14.816 17.588 587.446 580.656

INFO:eyetracking.api.RAW_EVENT parsed
 in:���� in:ET_SPL b 100527796 790 790 253 253 -59.940  3.229 14.812 17.578 587.647 580.575

INFO:eyetracking.api.RAW_EVENT parsed
 in:ET_SPL b 100527805 790 790 252 252 -59.955  3.232 14.814 17.569 587.825 580.504

INFO:eyetracking.api.RAW_EVENT parsed
 in:ET_SPL b 100527813 790 790 251 251 -59.967  3.237 14.815 17.566 587.983 580.440

INFO:eyetracking.api.RAW_EVENT parsed
 in:ET_SPL b 100527821 789 789 250 250 -59.974  3.244 14.816 17.565 588.123 580.384
	 */
	
	// </editor-fold>

	// <editor-fold defaultstate="expanded" desc="Constructor(s)">

	/**
	 * Constructor for the IViewXComm class. The resulting object needs
	 * to be configured, connected and started.
	 * 
	 * @param protocol The IViewXProtocol to use that may send commands specific
	 * 		to a model of the iViewX family of devices.
	 * 
	 * @see #configure(Filter, Configuration)
	 * @see #connect()
	 * @see #start()
	 */
	public IViewXComm(IViewXProtocol protocol) {
		this.protocol = protocol;
	}

	// </editor-fold>

	public void configure(Filter filter, Configuration config) {
		super.configure(filter, config);
		trackerIP = config.getTrackerIP();
		trackerPort = config.getTrackerPort();
		localPort = config.getLocalPort();
		setETFormat = config.getSetFormat();
		/*samplingRate = config.getSamplingRate();
		if(samplingRate == 0) {
			samplingRate = 60;
		}*/
	}
	
	// <editor-fold defaultstate="expanded" desc="Working Functions">

	/**
	 * Overrides the run method. First sets up UDP connection with the
	 * mTrackerServerCommandAddress, then enters a loop and continuously tries
	 * to receive data from the mTrackerServerCommandAddress eye tracking device
	 * until the requestStop() method is called.
	 */
	@Override
	public void run() {
		clientOperation();
	}

	@Override
	public synchronized void connect() throws IOException {
		state.connect();
	}

	@Override
	public synchronized void disconnect() throws IOException {
		state.disconnect();
	}

	@Override
	public synchronized boolean isConnected() {
		return state.isConnected();
	}

	/**
	 * Toggles eye tracking on and off without disconnecting.
	 */
	@Override
	public boolean toggle() throws IOException {
		return state.toggle();
	}
	
	/**
	 * Performs calibration and starts eye tracking, if calibration was successful.
	 * 
	 * @return True, if calibration was successful and calibration was started,
	 * 		false otherwise.
	 */
	public void calibrateAndStartTracking() throws IOException {
		state.calibrateAndStartTracking();
	}

	/**
	 * A means of stopping this thread.
	 */
	@Override
	public void requestStop() {
		stop = true;
	}
	
	private void addMessageListener(String messageType, MessageListener listener) {
		if(messageListeners.containsKey(messageType)) {
			messageListeners.get(messageType).add(listener);
		} else {
			Set<MessageListener> actions = new HashSet<MessageListener>();
			actions.add(listener);
			messageListeners.put(messageType, actions);
		}
	}
	
	private void removeMessageListener(String messageType, MessageListener listener) {
		if(messageListeners.containsKey(messageType)) {
			messageListeners.get(messageType).remove(listener);
			if(messageListeners.get(messageType).isEmpty()) {
				messageListeners.remove(messageType);
			}
		}
	}
	
	private void notifyMessageListeners(String messageType, String[] tokens) {
		if(messageListeners.containsKey(messageType)) {
			for(MessageListener l: messageListeners.get(messageType)) {
				l.listen(tokens);
			}
		}
	}

	public byte[] createIViewXCommandFromString(String command) {
		char[] commandAsCharacters = command.toCharArray();
		byte[] commandAsBytes = new byte[commandAsCharacters.length];

		for (int i = 0; i < commandAsCharacters.length; i++) {
			commandAsBytes[i] = (byte) commandAsCharacters[i];
		}

		return commandAsBytes;
	}
	
	public void sendCommand(String str) throws IOException {
		output(str);
		byte[] msg = createIViewXCommandFromString(str);
		mSendSocket.send(new DatagramPacket(msg, msg.length, trackerIP, trackerPort));
	}

	@Override
	protected void clientOperation() {
		if (!connected) {
			throw new IllegalStateException("Not connected");
		}

		String responseString = "";
		try {
			byte[] recvBuff = new byte[MAX_RESPONSE_SIZE_BYTES];
			mUdpPacket = new DatagramPacket(recvBuff, recvBuff.length);
			stop = false;

			Pattern responsePatternRAWBoth = Pattern.compile(protocol.getRAWBothResponsePattern());
			// int eventCount = 0;
			
			while (!stop) {
				if (connected) {
					if (responseString != null) {

						try {
							
							/*if(eventCount++ == 10) {
								System.err.println("Sending delayed format request command");
								sendCommand(FORMAT_REQUEST_COMMAND);
							}*/
							
							if (mReceiveSocket != null) {
								mReceiveSocket.receive(mUdpPacket);
							}

							responseString = new String(mUdpPacket.getData(),
									0, mUdpPacket.getLength()); // receive
																// file
																// contents
							input(responseString);
							responseString = responseString.trim();
							//System.out.println(responseString);
							boolean matched = false;
							Matcher rawBothMatcher = responsePatternRAWBoth.matcher(responseString);
							if(rawBothMatcher.matches()) {
								Map<String,Object> map = new HashMap<String,Object>();
								map.put(Event.TRACKER_TIMESTAMP_MU, Long.parseLong(rawBothMatcher.group("TU")));
								map.put(Event.CLIENT_TIMESTAMP_MS, System.currentTimeMillis());
								map.put(Event.EYE_TYPE, "b");
								map.put(Event.RAW_EVENT, true);
								map.put(Event.POR_X, Integer.parseInt(rawBothMatcher.group("SX")));
								map.put(Event.POR_Y, Integer.parseInt(rawBothMatcher.group("SY")));
								map.put(Event.PUPIL_DIA_L_PX, Float.parseFloat(rawBothMatcher.group("DL")));
								map.put(Event.PUPIL_DIA_R_PX, Float.parseFloat(rawBothMatcher.group("DR")));
								map.put(Event.EYE_X_L, Float.parseFloat(rawBothMatcher.group("EXL")));
								map.put(Event.EYE_X_R, Float.parseFloat(rawBothMatcher.group("EXR")));
								map.put(Event.EYE_Y_L, Float.parseFloat(rawBothMatcher.group("EYL")));
								map.put(Event.EYE_Y_R, Float.parseFloat(rawBothMatcher.group("EYR")));
								map.put(Event.EYE_Z_L, Float.parseFloat(rawBothMatcher.group("EZL")));
								map.put(Event.EYE_Z_R, Float.parseFloat(rawBothMatcher.group("EZR")));
								map.put(Event.PUPIL_POS_X_L, Float.parseFloat(rawBothMatcher.group("PXL")));
								map.put(Event.PUPIL_POS_X_R, Float.parseFloat(rawBothMatcher.group("PXR")));
								map.put(Event.PUPIL_POS_Y_L, Float.parseFloat(rawBothMatcher.group("PYL")));
								map.put(Event.PUPIL_POS_Y_R, Float.parseFloat(rawBothMatcher.group("PYR")));
								map.put(Event.PUPIL_DIA_L_MM, Float.parseFloat(rawBothMatcher.group("PDL")));
								map.put(Event.PUPIL_DIA_R_MM, Float.parseFloat(rawBothMatcher.group("PDR")));
								
								String groupCXL = rawBothMatcher.group("CXL");
								if(groupCXL != null)
									map.put(Event.CORNEAL_REFLEX_POS_X_L, Float.parseFloat(groupCXL));
								
								String groupCXR = rawBothMatcher.group("CXR");
								if(groupCXR != null)
									map.put(Event.CORNEAL_REFLEX_POS_X_R, Float.parseFloat(groupCXR));
								
								String groupCYL = rawBothMatcher.group("CYL");
								if(groupCYL != null)
									map.put(Event.CORNEAL_REFLEX_POS_Y_L, Float.parseFloat(groupCYL));
								
								String groupCYR = rawBothMatcher.group("CYR");
								if(groupCYR != null)
									map.put(Event.CORNEAL_REFLEX_POS_Y_R, Float.parseFloat(groupCYR));
								
								Event e = new EventImpl(map);
								filter.filter(e);
								matched = true;
								info(Event.RAW_EVENT+" parsed\n");
							}

							if(!matched) {
								Matcher fixStartMatcher = RESPONSE_PATTERN_FIXATION_START.matcher(responseString);
								if(fixStartMatcher.matches()) {
									Map<String,Object> map = new HashMap<String,Object>();
									map.put(Event.FIXATION_START_TIMESTAMP_MS, Long.parseLong(fixStartMatcher.group("TU"))/1000L);
									map.put(Event.CLIENT_TIMESTAMP_MS, System.currentTimeMillis());
									map.put(Event.EYE_TYPE, fixStartMatcher.group("ET"));
									map.put(Event.FIXATION_START, true);
									map.put(Event.POR_X, (int)Float.parseFloat(fixStartMatcher.group("SX")));
									map.put(Event.POR_Y, (int)Float.parseFloat(fixStartMatcher.group("SY")));
									Event e = new EventImpl(map);
									filter.filter(e);
									matched = true;
									info(Event.FIXATION_START+" parsed\n");
								}
							}

							if(!matched) {
								Matcher fixEndMatcher = RESPONSE_PATTERN_FIXATION_END.matcher(responseString);
								if(fixEndMatcher.matches()) {
									Map<String,Object> map = new HashMap<String,Object>();
									map.put(Event.FIXATION_START_TIMESTAMP_MS, Long.parseLong(fixEndMatcher.group("TUS"))/1000L);
									map.put(Event.FIXATION_END_TIMESTAMP_MS, Long.parseLong(fixEndMatcher.group("TUE"))/1000L);
									map.put(Event.FIXATION_DURATION_MS, Long.parseLong(fixEndMatcher.group("DU")));
									map.put(Event.CLIENT_TIMESTAMP_MS, System.currentTimeMillis());
									map.put(Event.EYE_TYPE, fixEndMatcher.group("ET"));
									map.put(Event.FIXATION_END, true);
									map.put(Event.POR_X, (int)Float.parseFloat(fixEndMatcher.group("SX")));
									map.put(Event.POR_Y, (int)Float.parseFloat(fixEndMatcher.group("SY")));
									Event e = new EventImpl(map);
									filter.filter(e);
									matched = true;
									info(Event.FIXATION_END+" parsed\n");
								}
							}
							
							if(!matched) {
								String[] tokens = responseString.split(" ");
								notifyMessageListeners(tokens[0], tokens);
							}

						} catch (SocketException se) {
							error(se);
						}
					}
				}
			}

		} catch (Throwable t) {
			error(t);
		}

		try {
			mReceiveSocket.close();
		} catch (NullPointerException ex) {
			error(ex);
		}
	}

	// </editor-fold>

	// <editor-fold defaultstate="expanded" desc="Public Properties">
	
	private abstract class State {
		public abstract void connect() throws IOException;
		public abstract void disconnect() throws IOException;
		public abstract boolean isConnected();
		public abstract boolean toggle() throws IOException;
		public abstract void calibrateAndStartTracking() throws IOException;
	}
	
	private class Disconnected extends State {
		
		public Disconnected() {
			connected = true;
		}

		@Override
		public void connect() throws IOException {
			info("Initiating connection handshake ...\n");

			// Initialize both send and receive sockets
			mReceiveSocket = new DatagramSocket(localPort);
			mSendSocket = new DatagramSocket();

			// Ping the tracker to make sure someone is home ...
			info("Pinging iViewX server @ " + trackerIP
						+ ":" + trackerPort + " ...\n");
			sendCommand(PING_COMMAND);

			// Grab ping response, should be immediate
			byte[] recvBuff = new byte[MAX_RESPONSE_SIZE_BYTES];
			DatagramPacket recvPacket = new DatagramPacket(recvBuff,
					recvBuff.length);
			mReceiveSocket.setSoTimeout(3000); // socket timeout in milliseconds, TODO: make configurable
			mReceiveSocket.receive(recvPacket);
			mReceiveSocket.setSoTimeout(0);
			String pingResponse = new String(recvPacket.getData(), 0,
					recvPacket.getLength());// receive
			input(pingResponse);
				
			/*
			 * send format of data to mTrackerServerCommandAddress Format is eye
			 * type, time stamp, gaze x, gaze y each format token needs a %
			 * symbol in front of it, and the entire format string needs to be
			 * in quotes, hence the \"
			 */
			if(setETFormat)
				sendCommand(FORMAT_REQUEST_PREFIX+protocol.getFormatString(IViewXComm.this));
//			sendCommand("ET_EVB 1 \"eye_image\""
//				+" \"C:\\Dokumente und Einstellungen\\iView X\\Eigene Dateien\\"
//					+"ProjQU\\eyes\"\n");
			
			state = new Connected();
		}

		@Override
		public void disconnect() {
			throw new IllegalStateException();
		}

		@Override
		public boolean isConnected() {
			return false;
		}

		@Override
		public boolean toggle() {
			throw new IllegalStateException();
		}

		@Override
		public void calibrateAndStartTracking() {
			throw new IllegalStateException();
		}
		
	}
	
	private class Connected extends State {

		public Connected() {
			connected = true;
		}
		
		@Override
		public void connect() {
			throw new IllegalStateException();
		}

		@Override
		public void disconnect() throws IOException {
			protocol.stopTracking(IViewXComm.this);
//			sendCommand("ET_EVE\n");
			mSendSocket.close();
			mReceiveSocket.close();
			connected = false;
		}

		@Override
		public boolean isConnected() {
			return true;
		}

		@Override
		public boolean toggle() throws IOException {
			protocol.startTracking(IViewXComm.this);
			state = new Tracking();
			return true;
		}

		@Override
		public void calibrateAndStartTracking() throws IOException {
			state = new Calibrating();
		}
		
	}
	
	private class Calibrating extends Connected {
		
		private Calibrating() throws IOException {
			LoggingMessageListener lml = new LoggingMessageListener();
			addMessageListener(MSG_CALIBRATION_PT_CHANGE, lml);
			addMessageListener(MSG_VALIDATION, lml);
			addMessageListener(MSG_END_CALIBRATION, new MessageListener() {
				@Override
				public void listen(String[] message) {
					try {
						removeMessageListener(MSG_END_CALIBRATION, this);
						// TODO: Maybe request calibration results using ET_RES
						sendCommand(CMD_VALIDATE);
						Calibrating.super.toggle();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			});
			sendCommand(CMD_START_5_PT_CALIBRATION);
		}
		
		@Override
		public boolean toggle() {
			throw new IllegalStateException();
		}

		@Override
		public void calibrateAndStartTracking() {
			throw new IllegalStateException();
		}
		
	}
	
	private class Tracking extends Connected {

		@Override
		public boolean toggle() throws IOException {
			state = new Connected();
			return false;
		}

		@Override
		public void calibrateAndStartTracking() {
			throw new IllegalStateException();
		}
		
	}

	// </editor-fold>
	
	private static interface MessageListener {
		public void listen(String[] message);
	}
	
	private class LoggingMessageListener implements MessageListener {
		@Override
		public void listen(String[] message) {
			System.err.println(Arrays.toString(message));
		}
	}
}
