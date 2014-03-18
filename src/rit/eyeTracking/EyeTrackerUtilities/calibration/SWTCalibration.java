package rit.eyeTracking.EyeTrackerUtilities.calibration;

import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

public class SWTCalibration {
	private Shell shell;
	private GamePaintListener paintListener;
	
	public SWTCalibration(Display display) {
		this.shell = new Shell(display);
		init();
	}
	
	public SWTCalibration(Shell parent) {
		this.shell = new Shell(parent);
		init();
	}
	
	private void init() {
		paintListener = new GamePaintListener(shell);
		shell.addPaintListener(paintListener);
	}
	
	public Shell getShell() {
		return shell;
	}
	
	public void newCross(int x, int y) {
		paintListener.newCross(x, y);
	}
	
	public void resetCross() {
		paintListener.resetCross();
	}
	
	public boolean isActive() {
		return paintListener != null && shell != null;
	}
	
	public void close() {
		PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
			@Override
			public void run() {
				shell.removePaintListener(paintListener);
				shell.close();
				paintListener = null;
				shell = null;
			}});
	}

	private static class GamePaintListener implements PaintListener {

		private static final int ARM_LENGTH = 10;
		private static final int ARM_WIDTH  =  6;
		
		private Color black;
		private int[] cross;
		
		private GamePaintListener(final Shell shell) {
			shell.getDisplay().syncExec(new Runnable() {
				@Override
				public void run() {
					black = new Color(shell.getDisplay(), 0, 0, 0);
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
				x-(ARM_WIDTH/2), y-ARM_LENGTH, 	  // 0
				x+(ARM_WIDTH/2), y-ARM_LENGTH, 	  // 1
				x+(ARM_WIDTH/2), y-(ARM_WIDTH/2), // 2
				x+ARM_LENGTH,    y-(ARM_WIDTH/2), // 3
				x+ARM_LENGTH, 	 y+(ARM_WIDTH/2), // 4
				x+(ARM_WIDTH/2), y+(ARM_WIDTH/2), // 5
				x+(ARM_WIDTH/2), y+ARM_LENGTH,	  // 6
				x-(ARM_WIDTH/2), y+ARM_LENGTH,    // 7
				x-(ARM_WIDTH/2), y+(ARM_WIDTH/2), // 8
				x-ARM_LENGTH,    y+(ARM_WIDTH/2), // 9
				x-ARM_LENGTH,    y-(ARM_WIDTH/2), // 10
				x-(ARM_WIDTH/2), y-(ARM_WIDTH/2)  // 11
			};
		}
		
		public void resetCross() {
			cross = null;
		}
		
		@Override
		public void paintControl(PaintEvent e) {
			if(cross != null) {
				// Paint fixation cross for current RFL
				Color bc = e.gc.getBackground();
				Color fc = e.gc.getForeground();
				e.gc.setBackground(black);
				e.gc.fillPolygon(cross);
				e.gc.setBackground(bc);
				e.gc.setForeground(fc);
			}
		}
		
	}
}
