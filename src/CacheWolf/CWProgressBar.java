    /*
    GNU General Public License
    CacheWolf is a software for PocketPC, Win and Linux that
    enables paperless caching.
    It supports the sites geocaching.com and opencaching.de

    Copyright (C) 2006  CacheWolf development team
See http://www.cachewolf.de/ for more information.
    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; version 2 of the License.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
    */
package CacheWolf;

import ewe.sys.Handle;
import ewe.ui.ProgressBarForm;

public class CWProgressBar {

	protected MyProgressBarForm pbf;
	protected Handle h;
	private float minValue = 0.0f;
	private float maxValue = 100.0f;
	private boolean allowExit = true;
	private boolean showProgress = false;
	
	/**
	 * Constructs a progress bar object, adapted for CacheWolf purposes.
	 * @param title The string shown in the title of the progress bar
	 * @param pMinValue The minimum of possible values
	 * @param pMaxValue The maximum of possible values
	 * @param pShowProgress If <code>true</code> the progress bar is shown, if <code>false</code>
	 * The progress bar will not show. Then usage of methods of this class will (nearly) have no 
	 * effect.
	 */
	public CWProgressBar(String title, int pMinValue, int pMaxValue, boolean pShowProgress) {
		if (pShowProgress) {
			this.showProgress = true;
	        pbf = new MyProgressBarForm();
	        pbf.showMainTask = false;
	        h = new Handle();
	        h.progressResolution = 0.01f;
	        pbf.setTask(h, title);
	        this.minValue = pMinValue;
	        if (pMaxValue != pMinValue) {
		        this.maxValue = pMaxValue;
	        } else {
	        	this.maxValue = pMinValue + 100;
	        }
        }
	}
	
	/**
	 * Shows the progress bar
	 */
	public void exec() {
		if (this.showProgress) pbf.exec();
	}
	
	/**
	 * The current position of the progress bar.
	 * @param value Should be between minValue and maxValue
	 */
	public void setPosition(int value){
		if (this.showProgress) {
	        h.setProgress(value / (maxValue-minValue));
        }
	}
	
	/**
	 * If set to yes the user is allowed to close the progress bar
	 * by clicking the closing x.
	 * @param value <code>true</code> if the user should be allowed to close the progress bar
	 * by x.
	 */
	public void allowExit(boolean value) {
		this.allowExit = value;
	}
	
	/**
	 * Is true if the user closed the progress bar
	 * @return Closed by user or not
	 */
	public boolean isClosed() {
		if (this.showProgress) return pbf.isClosed; else return false;
	}
	
	/**
	 * Exits the progress bar with given exit value
	 * @param value ?
	 */
	public void exit(int value) {
		this.allowExit(true);
		if (this.showProgress) pbf.exit(value);
	}	
	
	class MyProgressBarForm extends ProgressBarForm {
		boolean isClosed=false;
		protected boolean canExit(int exitCode) {
			isClosed = allowExit;
			return isClosed;
		}
	}
}
	
