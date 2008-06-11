/* $Id$ */

package utils;

import ewe.sys.Vm;
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
 */
public class MessageBoxFixed {
	String title, text;
	int type;
	int ret;
	boolean done;
	static int debug = 0;
	public MessageBoxFixed(String title_, String text_, int type_) {
		set(title_, text_, type_);
	}
	public void set(String title_, String text_, int type_) {
		title = title_;
		text = text_;
		type = type_;
	}

	public int execute() {
		//Vm.debug("aufruf: " + debug);
		//debug++;
/*	// this code is a beginning of code to determine, if a new mThread is needed
 		Vm.debug("t ." + mThread.inThread());
		MessageBox mb = new MessageBox(title,  text, type);
		ret = mb.execute();
*/
		MBThread t = new MBThread();  // start a new thread is necessary because the simple ewe v1.49 threading model doesn't allow displaying of a messageBox in this kind of thread
		t.start();
		done = false;
		while (!done) {
			//Vm.debug("A");
			//mThread.yield(100); // this seem to be without effect in ewe-vm v1.49
			try { mThread.sleep(100); } catch (InterruptedException e) {
		//		Vm.debug("interrupted"); // never reached, even if interrupted is explicitly called
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
