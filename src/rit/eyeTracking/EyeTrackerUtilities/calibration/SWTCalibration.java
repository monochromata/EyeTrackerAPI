package rit.eyeTracking.EyeTrackerUtilities.calibration;

import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

public class SWTCalibration {
	private boolean readingCalibration;
	private Shell shell;
	private GamePaintListener paintListener;
	private SentencePresenter sentencePresenter;
	private Point centerPoint;
	
	public SWTCalibration(Display display, boolean readingCalibration) {
		this.readingCalibration = readingCalibration;
		this.shell = new Shell(display);
		init();
	}
	
	public SWTCalibration(Shell parent, boolean readingCalibration) {
		this.readingCalibration = readingCalibration;
		this.shell = new Shell(parent);
		init();
	}
	
	private void init() {
		paintListener = new GamePaintListener(shell);
		shell.addPaintListener(paintListener);
		if(readingCalibration) {
			sentencePresenter = new SentencePresenter(this, paintListener);
			sentencePresenter.start();
		}
	}
	
	public Shell getShell() {
		return shell;
	}
	
	public Point getCenterPoint() {
		if(centerPoint == null) {
			PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
				@Override
				public void run() {
					Rectangle bounds = shell.getBounds();
					centerPoint = new Point(bounds.width/2, bounds.height/2);
				}});
		}
		return centerPoint;
	}
	
	/**
	 * Performs rapid serial visual presentation (RSVP) using the words in a sentence
	 * centered at the given location, each word being shown for a certain amount of
	 * time. It should be ensured that the words fit into foveal sight so subjects
	 * perform no saccades. Before the first and after the last word, "XXX" will b
	 * displayed. The given client object will be notified, when the last word of the
	 * sentence is shown.
	 * 
	 * @param sentence
	 * @param location
	 * @param client
	 * @throws IllegalStateException If not constructed with readingCalibration=true
	 * @see #SWTCalibration(Shell, boolean)
	 * @see #SWTCalibration(Display, boolean)
	 * @see Object#notify()
	 */
	public void showSentenceRSVP(String sentence, Point location, Object client) {
		if(readingCalibration) {
			sentencePresenter.showSentence(sentence, location, client);
		} else {
			throw new IllegalStateException("Not in reading calibration mode");
		}
	}
	
	public void setGreenFont() {
		if(readingCalibration) {
			sentencePresenter.setGreenFont();
		} else {
			throw new IllegalStateException("Not in reading calibration mode");
		}
	}
	
	public void resetFontColor() {
		if(readingCalibration) {
			sentencePresenter.resetFontColor();
		} else {
			throw new IllegalStateException("Not in reading calibration mode");
		}
	}
	
	
	public void showPoint(final Point p) {
		PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				paintListener.newCross(p.x, p.y);
				getShell().redraw();
			}});
	}
	
	public void showString(String string, Point p) {
		showString(string, p, null);
	}
	
	public void showString(final String string, final Point p, final Color color) {
		PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				if(isActive()) {
					paintListener.newString(string, p.x, p.y, color);
					getShell().redraw();
				}
			}});
	}
	
	public void endPoint() {
		PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				if(isActive()) {
					paintListener.resetCross();
					getShell().redraw();
				}
			}});
	}
	
	public void endString() {
		PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				if(isActive()) {
					paintListener.resetString();
					getShell().redraw();
				}
			}});
	}
	
	public void setGreenFeedback() {
		PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				paintListener.setGreenFeedback();
				getShell().redraw();
			}});
	}
	
	public void resetFeedback() {
		PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				paintListener.resetFeedback();
				getShell().redraw();
			}});
	}
	
	public boolean isActive() {
		return paintListener != null && shell != null;
	}
	
	public void close() {
		PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
			@Override
			public void run() {
				if(sentencePresenter != null)
					sentencePresenter.abort();
				if(shell != null && paintListener != null)
					shell.removePaintListener(paintListener);
				if(shell != null)
					shell.close();
				paintListener = null;
				shell = null;
			}});
	}
	
	private static class GamePaintListener implements PaintListener {

		private static final int ARM_LENGTH = 14;
		private static final int ARM_WIDTH  = 12;
		
		private Color feedbackColor;
		private Color black, red, green;
		private int[] cross;
		private String string;
		private Point stringExtent;
		private int stringCenterX, stringCenterY;
		private Color stringColor;
		
		private GamePaintListener(final Shell shell) {
			shell.getDisplay().syncExec(new Runnable() {
				@Override
				public void run() {
					black = new Color(shell.getDisplay(), 0, 0, 0);
					red = new Color(shell.getDisplay(), 0xAA, 0, 0);
					green = new Color(shell.getDisplay(), 0, 0xAA, 0);
				}});
		}
		
		public void newCross(int x, int y) {
		
			/*     0---1
			 *     |   |
			 *  10-11  2--3
			 *  |    x    |
			 *  9--8   5--4
			 *     |   |
			 *     7---6
			 */
			cross = new int[] {
				/*  0 */ x-(ARM_WIDTH/2), 				y-(ARM_LENGTH+(ARM_WIDTH/2)),
				/*  1 */ x+(ARM_WIDTH/2), 				y-(ARM_LENGTH+(ARM_WIDTH/2)),
				/*  2 */ x+(ARM_WIDTH/2), 				y-(ARM_WIDTH/2),
				/*  3 */ x+(ARM_LENGTH+(ARM_WIDTH/2)),  y-(ARM_WIDTH/2),
				/*  4 */ x+(ARM_LENGTH+(ARM_WIDTH/2)), 	y+(ARM_WIDTH/2),
				/*  5 */ x+(ARM_WIDTH/2),				y+(ARM_WIDTH/2),
				/*  6 */ x+(ARM_WIDTH/2),				y+(ARM_LENGTH+(ARM_WIDTH/2)),
				/*  7 */ x-(ARM_WIDTH/2),				y+(ARM_LENGTH+(ARM_WIDTH/2)),
				/*  8 */ x-(ARM_WIDTH/2),				y+(ARM_WIDTH/2),
				/*  9 */ x-(ARM_LENGTH+(ARM_WIDTH/2)),  y+(ARM_WIDTH/2),
				/* 10 */ x-(ARM_LENGTH+(ARM_WIDTH/2)),  y-(ARM_WIDTH/2),
				/* 11 */ x-(ARM_WIDTH/2),				y-(ARM_WIDTH/2)
			};
		}
		
		public void resetCross() {
			cross = null;
		}
		
		public void newString(String string, int x, int y, Color color) {
			this.string = string;
			this.stringExtent = null;
			this.stringCenterX = x;
			this.stringCenterY = y;
			this.stringColor=color==null?black:color;
		}
		
		public void resetString() {
			string = null;
			stringExtent = null;
			stringColor = null;
		}
		
		public void setGreenFeedback() {
			feedbackColor = green;
		}
		
		public void resetFeedback() {
			feedbackColor = null;
		}
		
		@Override
		public void paintControl(PaintEvent e) {
			Rectangle shellBounds = ((Shell)e.widget).getBounds();
			if(feedbackColor != null) {
				// Draw filled rectangle if entire shell is to be re-drawn
				e.gc.setAlpha(50);
				Color fc = e.gc.getForeground();
				Color bc = e.gc.getBackground();
				e.gc.setForeground(feedbackColor);
				e.gc.setBackground(feedbackColor);
				e.gc.fillRectangle(shellBounds.x, shellBounds.y,
						shellBounds.width, shellBounds.height);
				e.gc.setAlpha(255);
				e.gc.setForeground(fc);
				e.gc.setBackground(bc);
			}
			if(cross != null) {
				// Paint fixation cross for current RFL
				Color bc = e.gc.getBackground();
				e.gc.setBackground(black);
				e.gc.fillPolygon(cross);
				e.gc.setBackground(bc);
			}
			if(string != null) {
				// Draw a centered string
				if(stringExtent == null)
					stringExtent = e.gc.textExtent(string);
				Color fc = e.gc.getForeground();
				Color bc = e.gc.getBackground();
				e.gc.setForeground(stringColor);
				if(feedbackColor != null)
					e.gc.setBackground(feedbackColor);
				e.gc.drawText(string, stringCenterX-(stringExtent.x/2), stringCenterY-(stringExtent.y/2));
				e.gc.setForeground(fc);
				if(feedbackColor != null)
					e.gc.setBackground(bc);
			}
		}	
	}
	
	
	private static class SentencePresenter extends Thread {
		private boolean run = true;
		private SWTCalibration calibration;
		private GamePaintListener listener;
		private Color fontColor;
		private String sentence;
		private Point location;
		private Object client;
		
		private SentencePresenter(SWTCalibration calibration, GamePaintListener listener) {
			super("Sentence presenter");
			this.calibration = calibration;
			this.listener = listener;
		}

		synchronized void showSentence(String sentence,
				Point location, Object client) {
			this.sentence = sentence;
			this.location = location;
			this.client = client;
			notifyAll();
		}
		
		synchronized void setGreenFont() {
			fontColor = listener.green;
		}
		
		synchronized void resetFontColor() {
			fontColor = null;
		}
		
		synchronized void abort() {
			System.err.println("Aborting Sentence Presenter");
			run = false;
			notifyAll();
		}
		
		@Override
		public synchronized void run() {
			while(run) {
				try {
					wait();
					if(run && sentence != null) {
						Point point = this.location;
						calibration.showString("XXX", point, fontColor);
						wait(1000L);
						String[] words = sentence.split(" ");
						for(int i=0;run && i<words.length-1;i++) {
							calibration.showString(words[i], point, fontColor);
							wait(200L);
						}
						if(run) {
							calibration.showString(words[words.length-1], point, fontColor);
							synchronized(client) {
								client.notify();
							}
							wait(300L);
							if(run) {
								calibration.showString("XXX", point, fontColor);
							}
						}
					}
				} catch (InterruptedException e) { }
			}
		}
		
	}
}
