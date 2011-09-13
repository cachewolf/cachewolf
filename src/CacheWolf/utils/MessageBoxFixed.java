    /*
    GNU General Public License
    CacheWolf is a software for PocketPC, Win and Linux that
    enables paperless caching.
    It supports the sites geocaching.com and opencaching.de

    Copyright (C) 2006  CacheWolf development team
    See http://developer.berlios.de/projects/cachewolf/
    for more information.
    Contact: 	bilbowolf@users.berlios.de
    			kalli@users.berlios.de

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
package CacheWolf.utils;
/* $Id$ */
import ewe.sys.mThread;
import ewe.ui.MessageBox;
/**
 * this class can be needed, because ewe v1.49 cannot display messageBoxes
 * in some special threds <br>
 * <br> but as of CacheWolf v1.0 this class is not used <br>
 * <br> It is included in order to have it available and to include the
 * knowledge about ewe-threading problems, described in comments here <br>
 * This class is not mature -> make it mature or use it only when
 * you get in the ewe-vm the exception: ewe.sys Event Direction Exception: This Task cannot be done within a timer tick <br>
 * when you use the normal MessageBox
 * @author pfeffer
 * 
 * FIXME: add javadoc!
 *
 */
public class MessageBoxFixed {
	private String title, text;
	private int type;
	private int ret;
	private boolean done;

	public MessageBoxFixed(final String title_, final String text_, final int type_) {
		set(title_, text_, type_);
	}

	public void set(final String title_, final String text_, final int type_) {
		title = title_;
		text = text_;
		type = type_;
	}

	public int execute() {
		final MBThread t = new MBThread();  // start a new thread is necessary because the simple ewe v1.49 threading model doesn't allow displaying of a messageBox in this kind of thread
		t.start();
		done = false;
		while (!done) {
			try { mThread.sleep(100); } catch (InterruptedException e) {
			}
		}
		return ret;
	}

	class MBThread extends mThread {
		public MessageBox mb;
		public void run() {
			mb = new MessageBox(title,  text, type);
			mb.show();
			done = false;
			mb.waitUntilClosed();
			done = true;
			// interrupt(); this doesn't work at all, neither in sun-vm nor in ewe-vm v1.49
		}
	}
}
