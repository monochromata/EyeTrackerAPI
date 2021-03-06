package rit.eyeTracking.EyeTrackerUtilities.udpClients;

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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.Logger;
import org.eclipse.swt.graphics.Point;
import org.omg.CORBA.RepositoryIdHelper;

import rit.eyeTracking.Event;
import rit.eyeTracking.AbstractEvent;
import rit.eyeTracking.EventFactory;
import rit.eyeTracking.EyeTrackerUtilities.calibration.SWTCalibration;
import rit.eyeTracking.SmoothingFilters.Filter;

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
 * TODO: Log errors to logger, not to stdout
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
	
	public static final String CMD_START_RECORDING			    = "ET_REC\n";
	public static final String CMD_CONTINUE_RECORDING			= "ET_CNT\n";
	public static final String CMD_PAUSE_RECORDING			    = "ET_PSE\n";
	public static final String CMD_STOP_RECORDING			    = "ET_STP\n";
	public static final String CMD_INC_TRIAL_NUMBER				= "ET_INC\n";
	
	public static final String CMD_REMARK_PREFIX				= "ET_REM ";
	public static final String CMD_SAVE_BUFFER_PREFIX			= "ET_SAV ";
	
	public static final String CMD_START_CALIBRATION_PREFIX 	= "ET_CAL ";
	public static final String CMD_CANCEL_CALIBRATION			= "ET_BRK\n";
	public static final String CMD_ACCEPT_CALIBRATION_POINT    	= "ET_ACC\n";
	public static final String CMD_VALIDATE						= "ET_VLS\n";
	public static final String CMD_EXTENDED_VALIDATION 			= "ET_VLX\n";
	public static final String CMD_EXTENDED_VALIDATION_PREFIX	= "ET_VLX ";
	
	public static final String MSG_START_CALIBRATION			= "ET_CAL";
	public static final String MSG_ABORT_CALIBRATION			= "ET_BRK";
	public static final String MSG_CALIBRATION_AREA				= "ET_CSZ";
	public static final String MSG_CALIBRATION_PT				= "ET_PNT";
	public static final String MSG_CALIBRATION_PT_CHANGE		= "ET_CHG";
	public static final String MSG_END_CALIBRATION			    = "ET_FIN";
	public static final String MSG_VALIDATION					= "ET_VLS";
	public static final String MSG_EXTENDED_VALIDATION			= "ET_VLX";
	
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
	
	public static final String CMD_START_FIXATION_DETECTION 	= "ET_FIX\n"; // used by REDm / respective iViewX version
	public static final String CMD_START_FIXATION_DETECTION_PREFIX = "ET_FIX "; // used by RED5 / respective iViewX version
	public static final String CMD_STOP_FIXATION_DETECTION 		= "ET_EFX\n";
	
	public static final String CMD_CALIBRATION_PARAMS			= "ET_CPA\n";
	
	public static final String RESPONSE_DATA_STRING 			= "ET_SPL";
	
	protected static final Pattern RESPONSE_PATTERN_PNT =
		Pattern.compile("\\AET_PNT\\s+(?<I>\\d)\\s+(?<X>\\d+)\\s+(?<Y>\\d+)\\z");	
	
	protected static final Pattern RESPONSE_PATTERN_VLS =
		Pattern.compile("\\AET_VLS\\s+(?<ET>left|right)\\s+(?<RMSX>[0-9\\-\\.]+)\\s+(?<RMSY>[0-9\\-\\.]+)"
				+"\\s+(?<RMSD>[0-9\\-\\.]+)\\s+(?<XD>[0-9\\-\\.]+)�\\s+(?<YD>[0-9\\-\\.]+)�\\z");
	protected static final Pattern RESPONSE_PATTERN_VLX =
		Pattern.compile("\\AET_VLX\\s+(?<RMSXL>[0-9\\-\\.]+)\\s+(?<RMSYL>[0-9\\-\\.]+)\\s+(?<XDL>[0-9\\-\\.]+)�\\s+(?<YDL>[0-9\\-\\.]+)�"
				+"\\s+(?<RMSXR>[0-9\\-\\.]+)\\s+(?<RMSYR>[0-9\\-\\.]+)\\s+(?<XDR>[0-9\\-\\.]+)�\\s+(?<YDR>[0-9\\-\\.]+)�\\z");
	
	protected static final Pattern RESPONSE_PATTERN_FIXATION_START =
		Pattern.compile("\\AET_FIX\\s+(?<ET>l|r|b)\\s+(?<TU>\\d+)\\s+(?<SX>[0-9\\-\\.]+)\\s+(?<SY>[0-9\\-\\.]+)\\z");
	protected static final Pattern RESPONSE_PATTERN_FIXATION_END =
		Pattern.compile("\\AET_EFX\\s+(?<ET>l|r|b)\\s+(?<TUS>\\d+)\\s+(?<TUE>\\d+)\\s+(?<DU>\\d+)\\s+(?<SX>[0-9\\-\\.]+)\\s+(?<SY>[0-9\\-\\.]+)\\z");
	
	private final IViewXProtocol protocol;
	private final EventFactory eventFactory;
	
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
	public IViewXComm(IViewXProtocol protocol, EventFactory eventFactory) {
		this.protocol = protocol;
		this.eventFactory = eventFactory;
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
	
	@Override
	public synchronized boolean isCalibrated() {
		return state.isCalibrated();
	}
	
	@Override
	public synchronized boolean isTracking() {
		return state.isTracking();
	}

	/**
	 * Toggles eye tracking on and off without disconnecting.
	 */
	@Override
	public boolean toggle() throws IOException {
		return state.toggle();
	}

	@Override
	public void calibrate(Eye eye, SWTCalibration calibration,
			CalibrationListener listener) throws IOException,
			UnsupportedOperationException {
		throw new UnsupportedOperationException("Need to specify the number of calibration points");
	}

	@Override
	public void calibrate(Eye eye, int numberOfPoints, SWTCalibration calibration,
			CalibrationListener listener) throws IOException,
			UnsupportedOperationException {
		state.calibrate(eye, numberOfPoints, calibration, listener);
	}

	@Override
	public void abortCalibration() throws IOException,
			UnsupportedOperationException {
		state.abortCalibration();
	}

	@Override
	public void validate(int numberOfPoints, SWTCalibration calibration,
			CalibrationListener listener) throws IOException,
			UnsupportedOperationException {
		state.validate(numberOfPoints, calibration, listener);
	}

	@Override
	public void validate(Point[] points, SWTCalibration calibration,
			CalibrationListener listener) throws IOException,
			UnsupportedOperationException {
		state.validate(points, calibration, listener);
	}

	@Override
	public void abortValidation() throws IOException,
			UnsupportedOperationException {
		state.abortValidation();
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
	public void sendBufferMessage(String message) throws IOException {
		state.sendBufferMessage(message);
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
								try {
									mReceiveSocket.receive(mUdpPacket);
								} catch(SocketException se) {
									// Socket closed while waiting for data
								}
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
								
								Event e = eventFactory.createEvent(Event.RAW_EVENT, map);
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
									map.put(Event.POR_X, (int)Float.parseFloat(fixStartMatcher.group("SX")));
									map.put(Event.POR_Y, (int)Float.parseFloat(fixStartMatcher.group("SY")));
									Event e = eventFactory.createEvent(Event.FIXATION_START, map);
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
									map.put(Event.POR_X, (int)Float.parseFloat(fixEndMatcher.group("SX")));
									map.put(Event.POR_Y, (int)Float.parseFloat(fixEndMatcher.group("SY")));
									Event e = eventFactory.createEvent(Event.FIXATION_END, map);
									filter.filter(e);
									matched = true;
									info(Event.FIXATION_END+" parsed\n");
								}
							}
							
							if(!matched && responseString.startsWith(MSG_START_CALIBRATION)) {
								String[] parts = responseString.split(" ");
								Map<String,Object> map = new HashMap<String,Object>();
								map.put(Event.CLIENT_TIMESTAMP_MS, System.currentTimeMillis());
								if(parts.length == 2) {
									switch(Integer.parseInt(parts[1])) {
									case 1:
										map.put(Event.EYE_TYPE, "r");
										break;
									case 2:
										map.put(Event.EYE_TYPE, "l");
										break;
									default:
										info("Unknown eye type in "+responseString+"\n");
									}
								}
								Event e = eventFactory.createEvent(IViewX.CALIBRATION_STARTED, map);
								filter.filter(e);
								matched = true;
								info(IViewX.CALIBRATION_STARTED+" parsed\n");
							}
							
							if(!matched) {
								Matcher pntMatcher = RESPONSE_PATTERN_PNT.matcher(responseString);
								if(pntMatcher.matches()) {
									Map<String,Object> map = new HashMap<String,Object>();
									map.put(Event.CLIENT_TIMESTAMP_MS, System.currentTimeMillis());
									map.put(IViewX.PNT_INDEX, pntMatcher.group("I"));
									map.put(IViewX.PNT_X, Integer.parseInt(pntMatcher.group("X")));
									map.put(IViewX.PNT_Y, Integer.parseInt(pntMatcher.group("Y")));
									Event e = eventFactory.createEvent(IViewX.CALIBRATION_POINT_DATA, map);
									filter.filter(e);
									matched = true;
									info(IViewX.CALIBRATION_POINT_DATA+" parsed\n");
								}
							}
							
							if(!matched && responseString.startsWith(MSG_CALIBRATION_PT_CHANGE)) {
								String[] parts = responseString.split(" ");
								Map<String,Object> map = new HashMap<String,Object>();
								map.put(Event.CLIENT_TIMESTAMP_MS, System.currentTimeMillis());
								map.put(IViewX.PNT_INDEX, Integer.parseInt(parts[1]));
								Event e = eventFactory.createEvent(IViewX.CALIBRATION_POINT_CHANGE, map);
								filter.filter(e);
								matched = true;
								info(IViewX.CALIBRATION_POINT_CHANGE+" parsed\n");
							}
							
							if(!matched && responseString.startsWith(MSG_ABORT_CALIBRATION)) {
								Map<String,Object> map = new HashMap<String,Object>();
								map.put(Event.CLIENT_TIMESTAMP_MS, System.currentTimeMillis());
								Event e = eventFactory.createEvent(IViewX.CALIBRATION_ABORTED, map);
								filter.filter(e);
								matched = true;
								info(IViewX.CALIBRATION_ABORTED+" parsed\n");
							}
							
							if(!matched && responseString.startsWith(MSG_END_CALIBRATION)) {
								Map<String,Object> map = new HashMap<String,Object>();
								map.put(Event.CLIENT_TIMESTAMP_MS, System.currentTimeMillis());
								Event e = eventFactory.createEvent(IViewX.CALIBRATION_SUCCESSFUL, map);
								filter.filter(e);
								matched = true;
								info(IViewX.CALIBRATION_SUCCESSFUL+" parsed\n");
							}

							if(!matched) {
								Matcher vlsMatcher = RESPONSE_PATTERN_VLS.matcher(responseString);
								if(vlsMatcher.matches()) {
									Map<String,Object> map = new HashMap<String,Object>();
									map.put(Event.CLIENT_TIMESTAMP_MS, System.currentTimeMillis());
									switch(vlsMatcher.group("ET")) {
									case "left":
										map.put(Event.EYE_TYPE, "l");
										break;
									case "right":
										map.put(Event.EYE_TYPE, "r");
										break;
									default:
										info("Unknown eye type="+vlsMatcher.group("ET")+" in "+responseString);
									}
									map.put(IViewX.RMSX, Float.parseFloat(vlsMatcher.group("RMSX")));
									map.put(IViewX.RMSY, Float.parseFloat(vlsMatcher.group("RMSY")));
									map.put(IViewX.RMSD, Float.parseFloat(vlsMatcher.group("RMSD")));
									map.put(IViewX.XD, Float.parseFloat(vlsMatcher.group("XD")));
									map.put(IViewX.YD, Float.parseFloat(vlsMatcher.group("YD")));
									Event e = eventFactory.createEvent(IViewX.CALIBRATION_ACCURRACY, map);
									filter.filter(e);
									matched = true;
									info(IViewX.CALIBRATION_ACCURRACY+" parsed\n");
								}
							}
							
							if(!matched) {
								Matcher vlxMatcher = RESPONSE_PATTERN_VLX.matcher(responseString);
								if(vlxMatcher.matches()) {
									Map<String,Object> map = new HashMap<String,Object>();
									map.put(Event.CLIENT_TIMESTAMP_MS, System.currentTimeMillis());
									map.put(IViewX.RMSXL, Float.parseFloat(vlxMatcher.group("RMSXL")));
									map.put(IViewX.RMSYL, Float.parseFloat(vlxMatcher.group("RMSYL")));
									map.put(IViewX.XDL, Float.parseFloat(vlxMatcher.group("XDL")));
									map.put(IViewX.YDL, Float.parseFloat(vlxMatcher.group("YDL")));
									map.put(IViewX.RMSXR, Float.parseFloat(vlxMatcher.group("RMSXR")));
									map.put(IViewX.RMSYR, Float.parseFloat(vlxMatcher.group("RMSYR")));
									map.put(IViewX.XDR, Float.parseFloat(vlxMatcher.group("XDR")));
									map.put(IViewX.YDR, Float.parseFloat(vlxMatcher.group("YDR")));
									Event e = eventFactory.createEvent(IViewX.VALIDATION_ACCURRACY, map);
									filter.filter(e);
									matched = true;
									info(IViewX.VALIDATION_ACCURRACY+" parsed\n");
								}
							}
							
							//if(!matched) {
							String[] tokens = responseString.split(" |\t");
							System.err.println(responseString+"="+Arrays.asList(tokens));
							notifyMessageListeners(tokens[0], tokens);
							//}

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
			if(mReceiveSocket != null)
				mReceiveSocket.close();
		} catch (NullPointerException ex) {
			error(ex);
		}
	}

	// </editor-fold>

	// <editor-fold defaultstate="expanded" desc="Public Properties">
	
	private abstract class State {
		public void sendBufferMessage(String message) throws IOException { }
		public abstract void connect() throws IOException;
		public abstract void disconnect() throws IOException;
		public abstract boolean isConnected();
		public abstract boolean isCalibrated();
		public abstract boolean isTracking();
		public abstract boolean toggle() throws IOException;
		public abstract void calibrate(Eye eye, int numberOfPoints, SWTCalibration calibration,
				CalibrationListener listener) throws IOException;
		public abstract void abortCalibration() throws IOException;
		public abstract void validate(int numberOfPoints, SWTCalibration calibration,
				CalibrationListener listener) throws IOException;
		public abstract void validate(Point[] points, SWTCalibration calibration,
				CalibrationListener listener) throws IOException;
		public abstract void abortValidation() throws IOException;
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
		public boolean isCalibrated() {
			// TODO: Provide a state-preserving implementation instead
			return false;
		}
		
		@Override
		public boolean isTracking() {
			return false;
		}

		@Override
		public boolean toggle() {
			throw new IllegalStateException();
		}

		@Override
		public void calibrate(Eye eye, int numberOfPoints, SWTCalibration calibration,
				CalibrationListener listener) throws IOException {
			connect();
			state = new Calibrating(eye, numberOfPoints, calibration, listener);
		}

		@Override
		public void abortCalibration() throws IOException {
			throw new IllegalStateException();
		}

		@Override
		public void validate(int numberOfPoints, SWTCalibration calibration,
				CalibrationListener listener) throws IOException {
			throw new IllegalStateException();
		}

		@Override
		public void validate(Point[] points, SWTCalibration calibration,
				CalibrationListener listener) throws IOException {
			throw new IllegalStateException();
		}

		@Override
		public void abortValidation() throws IOException {
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
			mSendSocket = null;
			mReceiveSocket.close();
			mReceiveSocket = null;
			connected = false;
		}

		@Override
		public boolean isConnected() {
			return true;
		}
		
		@Override
		public boolean isCalibrated() {
			// TODO: Provide a state-preserving implementation instead
			return false;
		}
		
		@Override
		public boolean isTracking() {
			return false;
		}
		
		@Override
		public void sendBufferMessage(String message) throws IOException {
			sendCommand(CMD_REMARK_PREFIX+"\""+message+"\"\n");
		}

		@Override
		public boolean toggle() throws IOException {
			protocol.startTracking(IViewXComm.this);
			state = new Tracking();
			return true;
		}

		@Override
		public void calibrate(Eye eye, int numberOfPoints, SWTCalibration calibration,
				CalibrationListener listener) throws IOException {
			state = new Calibrating(eye, numberOfPoints, calibration, listener);
		}

		@Override
		public void abortCalibration() throws IOException {
			throw new IllegalStateException();
		}

		@Override
		public void validate(int numberOfPoints, SWTCalibration calibration,
				CalibrationListener listener) throws IOException {
			throw new IllegalStateException();
		}

		@Override
		public void validate(Point[] points, SWTCalibration calibration,
				CalibrationListener listener) throws IOException {
			throw new IllegalStateException();
		}

		@Override
		public void abortValidation() throws IOException {
			throw new IllegalStateException();
		}
		
	}
	
	private class PointListener implements MessageListener {
		private final Point[] points;
		
		private PointListener(Point[] points) {
			this.points = points;
		}
		
		@Override
		public void listen(String[] message) {
			int index = Integer.parseInt(message[1]);
			int x = Integer.parseInt(message[2]);
			int y = Integer.parseInt(message[3]);
			points[index-1] = new Point(x, y);
		}
	}
	
	private class Calibrating extends Connected {
		
		private final Eye eye;
		private final int numberOfPoints;
		private final Point[] points;
		private final SWTCalibration calibration;
		private final CalibrationListener calibrationListener;
		private MessageListener ptListener;
		private MessageListener ptcListener;
		private MessageListener endListener;
		
		private Calibrating(Eye eye, final int numberOfPoints,
				final SWTCalibration calibration,
				final CalibrationListener listener) throws IOException {
			this.eye = eye;
			this.numberOfPoints = numberOfPoints;
			this.points = new Point[numberOfPoints];
			this.calibration = calibration;
			this.calibrationListener = listener;
			ptListener = new PointListener(points);
			ptcListener = new MessageListener() {
				@Override
				public void listen(String[] message) {
					try {
						int index = Integer.parseInt(message[1]);
						protocol.calibrate(IViewXComm.this, Calibrating.this.points[index-1], calibration, listener);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}};
				
			endListener = new MessageListener() {
				@Override
				public void listen(String[] message) {
					try {
						removeMessageListener(MSG_CALIBRATION_PT, ptListener);
						removeMessageListener(MSG_CALIBRATION_PT_CHANGE, ptcListener);
						removeMessageListener(MSG_END_CALIBRATION, endListener);
						Calibrating.super.toggle();
						listener.success("");
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			};
			addMessageListener(MSG_CALIBRATION_PT, ptListener);
			addMessageListener(MSG_CALIBRATION_PT_CHANGE, ptcListener);
			addMessageListener(MSG_END_CALIBRATION, endListener);
			startCalibration(eye, numberOfPoints);
		}

		protected void startCalibration(Eye eye, final int numberOfPoints)
				throws IOException {
			switch(eye) {
			case BOTH:
				sendCommand(CMD_START_CALIBRATION_PREFIX+numberOfPoints+"\n");
				break;
			case RIGHT:
				sendCommand(CMD_START_CALIBRATION_PREFIX+numberOfPoints+" 1\n");
				break;
			case LEFT:
				sendCommand(CMD_START_CALIBRATION_PREFIX+numberOfPoints+" 2\n");
				break;
			default:
				throw new IllegalArgumentException("Invalid eye constant: "+eye);
			}
		}
		
		@Override
		public boolean toggle() {
			throw new IllegalStateException();
		}

		@Override
		public void calibrate(Eye eye, int numberOfPoints, SWTCalibration calibration,
				CalibrationListener listener) throws IOException {
			throw new IllegalStateException();
		}

		@Override
		public void abortCalibration() throws IOException {
			removeMessageListener(MSG_CALIBRATION_PT, ptListener);
			removeMessageListener(MSG_CALIBRATION_PT_CHANGE, ptcListener);
			removeMessageListener(MSG_END_CALIBRATION, endListener);
			sendCommand(CMD_CANCEL_CALIBRATION);
			protocol.abortCalibration(IViewXComm.this, calibration);
			calibrationListener.aborted();
			state = new Connected();
		}
		
	}
	
	private class Validating extends Connected {
		
		private String vlsInfo = "";
		private int numberOfPoints;
		private Point[] points;
		private SWTCalibration calibration;
		private CalibrationListener listener;
		private PointListener ptListener;
		private MessageListener ptcListener;
		private MessageListener vlsListener;
		
		private Validating(final int numberOfPoints, final SWTCalibration calibration,
				final CalibrationListener listener) throws IOException {
			this.numberOfPoints = numberOfPoints;
			System.err.println("IViewXComm.Validating.numberOfPoints="+numberOfPoints);
			this.points = new Point[numberOfPoints];
			this.calibration = calibration;
			this.listener = listener;
			ptListener = new PointListener(points);
			ptcListener = new MessageListener() {
				@Override
				public void listen(String[] message) {
					try {
						int index = Integer.parseInt(message[1]);
						protocol.validate(IViewXComm.this, Validating.this.points[index-1], calibration, listener);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}};
			vlsListener = new MessageListener() {
				private int vlsCount = 0;
				@Override
				public void listen(String[] message) {
					vlsInfo += Arrays.asList(message).toString()+"\n";
					vlsCount++;
					if(vlsCount == 2) {
						// End listening
						removeMessageListener(MSG_CALIBRATION_PT, ptListener);
						removeMessageListener(MSG_CALIBRATION_PT_CHANGE, ptcListener);
						removeMessageListener(MSG_VALIDATION, vlsListener);
						state = new Tracking();
						listener.success(vlsInfo);
					}
				}
			};
				
			addMessageListener(MSG_CALIBRATION_PT, ptListener);
			addMessageListener(MSG_CALIBRATION_PT_CHANGE, ptcListener);
			addMessageListener(MSG_VALIDATION, vlsListener);
			sendCommand(CMD_VALIDATE);
		}

		@Override
		public void abortValidation() throws IOException {
			removeMessageListener(MSG_CALIBRATION_PT, ptListener);
			removeMessageListener(MSG_CALIBRATION_PT_CHANGE, ptcListener);
			removeMessageListener(MSG_VALIDATION, vlsListener);
			sendCommand(CMD_CANCEL_CALIBRATION); // TODO: Is there a command to abort validation?
			protocol.abortValidation(IViewXComm.this, calibration);
			listener.aborted();
			state = new Tracking();
		}
		
		@Override
		public boolean toggle() {
			throw new IllegalStateException();
		}
		
		@Override
		public boolean isCalibrated() {
			// TODO: Provide a state-preserving implementation instead
			return false;
		}
		
	}
	
	private class ValidatingExtended extends Connected {
		
		private String vlxInfo = "";
		private List<Point> points;
		private int currentPoint = 0;
		private SWTCalibration calibration;
		private CalibrationListener listener;
		private MessageListener pointsListener;
		
		private ValidatingExtended(Point[] points, final SWTCalibration calibration,
				final CalibrationListener listener) throws IOException {
			this.points = Arrays.asList(points);
			this.calibration = calibration;
			this.listener = listener;
			if(points.length == 0)
				throw new IllegalArgumentException("No points provided for extended calibration");
			pointsListener = new MessageListener() {
				@Override
				public void listen(String[] message) {
					try {
						if(message.length == 1) {
							// There was an error with the last point, append it to the end of
							// the list
							ValidatingExtended.this.points.add(ValidatingExtended.this.points.get(currentPoint));
						}
						if(++currentPoint < ValidatingExtended.this.points.size()) {
							// Log validation result
							vlxInfo += Arrays.asList(message).toString()+"\n";
							// Show the next point if there is one left
							validateCurrentPoint();
						} else {
							// Log validation result
							vlxInfo += Arrays.asList(message).toString()+"\n";
							// Finish
							removeMessageListener(MSG_EXTENDED_VALIDATION, pointsListener);
							state = new Tracking();
							listener.success(vlxInfo);
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
				}};
			addMessageListener(MSG_EXTENDED_VALIDATION, pointsListener);
			validateCurrentPoint();
		}

		protected void validateCurrentPoint() throws IOException {
			Point point = this.points.get(currentPoint);
			protocol.validate(IViewXComm.this, point, calibration, listener);
			sendCommand(CMD_EXTENDED_VALIDATION_PREFIX+point.x+" "+point.y+"\n");
		}

		@Override
		public void abortValidation() throws IOException {
			removeMessageListener(MSG_EXTENDED_VALIDATION, pointsListener);
			sendCommand(CMD_CANCEL_CALIBRATION); // TODO: Is there a command to abort validation?
			protocol.abortValidation(IViewXComm.this, calibration);
			listener.aborted();
			state = new Tracking();
		}
		
		@Override
		public boolean toggle() {
			throw new IllegalStateException();
		}
		
		@Override
		public boolean isCalibrated() {
			// TODO: Provide a state-preserving implementation instead
			return false;
		}
	}
	
	private class Tracking extends Connected {

		@Override
		public boolean isCalibrated() {
			// TODO: Provide a state-preserving implementation instead
			return false;
		}
		
		@Override
		public boolean isTracking() {
			return true;
		}
		
		@Override
		public boolean toggle() throws IOException {
			state = new Connected();
			return false;
		}

		@Override
		public void calibrate(Eye eye, int numberOfPoints, SWTCalibration calibration,
				CalibrationListener listener) throws IOException {
			state = new Calibrating(eye, numberOfPoints, calibration, listener);
		}

		@Override
		public void validate(int numberOfPoints, SWTCalibration calibration,
				CalibrationListener listener) throws IOException {
			state = new Validating(numberOfPoints, calibration, listener);
		}

		@Override
		public void validate(Point[] points, SWTCalibration calibration,
				CalibrationListener listener) throws IOException {
			state = new ValidatingExtended(points, calibration, listener);
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
