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

import net.ax86.GPS;
import net.ax86.GPSException;

import org.json.JSONException;
import org.json.JSONObject;

import ewe.io.IOException;
import ewe.io.SerialPort;
import ewe.io.SerialPortOptions;
import ewe.net.Socket;
import ewe.reflect.FieldTransfer;
import ewe.reflect.Reflect;
import ewe.sys.Time;
import ewe.sys.mThread;
import ewe.ui.CellConstants;
import ewe.ui.CellPanel;
import ewe.ui.ControlConstants;
import ewe.ui.Editor;
import ewe.ui.EditorEvent;
import ewe.ui.FormBase;
import ewe.ui.Gui;
import ewe.ui.InputStack;
import ewe.ui.ScrollBarPanel;
import ewe.ui.ScrollablePanel;
import ewe.ui.mButton;
import ewe.ui.mCheckBox;
import ewe.ui.mChoice;
import ewe.ui.mComboBox;
import ewe.ui.mInput;
import ewe.ui.mLabel;
import ewe.ui.formatted.TextDisplay;
import ewe.util.mString;

/**
 * Thread for reading data from COM-port
 * 
 */
class mySerialThread extends mThread {
    SerialPort comSp;
    byte[] comBuff = new byte[1024];
    int comLength = 0;
    TextDisplay out;
    boolean run;
    public String lastgot;

    public mySerialThread(SerialPortOptions spo, TextDisplay td) throws IOException {
	comSp = new SerialPort(spo);
	//comSp.setFlowControl(SerialPort.SOFTWARE_FLOW_CONTROL);
	out = td;
	lastgot = null;
    }

    public void run() {
	run = true;
	while (run) {
	    try {
		sleep(200);
	    } catch (InterruptedException e) {
		// Global.pref.log("Ignored exception", e, true);
	    }
	    if (comSp != null) {
		comLength = comSp.nonBlockingRead(comBuff, 0, comBuff.length);
		if (comLength > 0) {
		    String str = mString.fromAscii(comBuff, 0, comLength).toUpperCase();
		    lastgot = str;
		    if (out != null)
			out.appendText(str, true);
		}
	    }
	}
    }

    public String nonBlockingRead() {
	String ret = new String(lastgot); //mString.fromAscii(gpsBuff,0,gpsLen);
	lastgot = null;
	return ret;

    }

    public boolean stop() {
	run = false;
	boolean ret;
	if (comSp != null) {
	    ret = comSp.close(); //compSp == null can happen if a exception occured 
	    try {
		ewe.sys.mThread.sleep(500); // wait in order to give the system time to close the serial port
	    } catch (InterruptedException e) {
		// Global.pref.log("Ignored exception", e, true);
	    }
	} else
	    ret = true;
	return ret;
    }
}

/**
 * Thread for reading data from gpsd and simply displaying it to the user.
 * 
 * This is a modified version of {@link CacheWolf.navi.GpsdThread}.
 * 
 * @author Tilman Blumenbach
 */
class GpsdThread extends mThread {
    GPS gpsObj;
    TextDisplay out;
    boolean run;

    public GpsdThread(TextDisplay td) throws IOException, JSONException, GPSException {
	JSONObject response;
	int proto_major;

	gpsObj = new GPS(Global.pref.gpsdHost, Global.pref.gpsdPort);
	gpsObj.stream(GPS.WATCH_ENABLE);

	// Check major protocol version:
	response = gpsObj.read();

	if (!response.getString("class").equals("VERSION")) {
	    throw new GPSException("Expected VERSION object at connect.");
	} else if ((proto_major = response.getInt("proto_major")) != 3) {
	    throw new GPSException("Invalid protocol API version; got " + proto_major + ", wanted 3.");
	}

	out = td;
	// Show data to user:
	out.appendText(response.toString(2) + "\n", true);
    }

    public void run() {
	JSONObject response;

	run = true;
	while (run) {
	    try {
		sleep(1000);
	    } catch (InterruptedException e) {
		// Global.pref.log("Ignored Exception", e, true);
	    }

	    if (gpsObj == null) {
		continue;
	    }

	    try {
		/* Tblue> This is ugly, but BufferedReader::ready() seems to
		 *        be broken in Ewe, so instead of only polling when
		 *        there is no data from gpsd, we poll on every iteration.
		 *        Not ideal, but works for now.
		 */
		gpsObj.poll();

		/* Tblue> TODO: I think this call should not block, but
		 *              my GPS class does not yet support non-blocking
		 *              reads... Seems to work, anyway.
		 */
		response = gpsObj.read();
		out.appendText(response.toString(2) + "\n", true);

		// Keep up with new devices:
		if (response.getString("class").equals("DEVICE") && response.has("activated") && response.getDouble("activated") != 0) { // This is a new device, we need to tell gpsd we want to watch it:
		    Global.pref.log("New GPS device, sending WATCH command.");
		    gpsObj.stream(GPS.WATCH_ENABLE);
		}
	    } catch (Exception e) {
		// We will just ignore this JSON object:
		// Global.pref.log("Ignored Exception", e, true);
	    }
	} // while
    }

    public boolean stop() {
	run = false;

	if (gpsObj == null) {
	    return true;
	}

	gpsObj.cleanup();
	return false;
    }
}

class OldGpsdThread extends mThread {
    Socket gpsdSocket;
    boolean run;
    TextDisplay out;
    Socket tcpConn;
    String lastError = new String();
    public String lastgot;

    public OldGpsdThread(TextDisplay td) throws IOException {
	try {
	    gpsdSocket = new Socket(Global.pref.gpsdHost, Global.pref.gpsdPort);
	} catch (IOException e) {
	    throw new IOException(Global.pref.gpsdHost);
	} // catch (UnsatisfiedLinkError e) {} // TODO in original java-vm 
	out = td;
	lastgot = null;
    }

    public void run() {
	String gpsResult;
	run = true;
	while (run) {
	    try {
		sleep(900);
	    } catch (InterruptedException e) {
		// Global.pref.log("Ignored Exception", e, true);
	    }
	    if (gpsdSocket != null) {
		gpsResult = getGpsdData("ADPQTV\r\n");
		if (gpsResult != null) {
		    lastgot = gpsResult;
		    if (out != null)
			out.appendText(gpsResult, true);
		}
	    }
	} // while
    }

    private String getGpsdData(String command) {
	byte[] rcvBuff = new byte[1024 * 10]; // when some action takes a long time (eg. loading or zooming a map), a lot of data can be in the buffer, read that at once
	int rcvLength = 0;
	try {
	    gpsdSocket.write(command.getBytes());
	} catch (IOException e) {
	    Global.pref.log("Socket exception", e, true);
	}
	try {
	    sleep(100);
	} catch (InterruptedException e) {
	    // Global.pref.log("Ignored exception", e, true);
	}
	try {
	    rcvLength = gpsdSocket.read(rcvBuff);
	} catch (IOException e) {
	    Global.pref.log("Socket exception", e, true);
	}
	String str = null;
	if (rcvLength > 0) {
	    str = mString.fromAscii(rcvBuff, 0, rcvLength);
	}
	return str;
    }

    public String nonBlockingRead() {
	String ret = new String(lastgot); //mString.fromAscii(gpsBuff,0,gpsLen);
	lastgot = null;
	return ret;

    }

    public void stop() {
	run = false;
	if (gpsdSocket != null)
	    gpsdSocket.close();
    }
}

public class GPSPortOptions extends SerialPortOptions {
    TextDisplay txtOutput;
    mButton btnTest, btnUpdatePortList, btnScan;
    public mInput inputBoxForwardHost;
    mLabel labelForwardHost;
    public mCheckBox forwardGpsChkB;
    public mInput inputBoxGpsdHost;
    mLabel labelUseGpsd;
    public mChoice chcUseGpsd;
    mLabel labelGpsdHost;
    public mInput inputBoxLogTimer;
    mLabel labelLogTimer;
    public mCheckBox logGpsChkB;
    mySerialThread serThread;
    GpsdThread gpsdThread = null;
    OldGpsdThread oldGpsdThread = null;
    boolean gpsRunning = false;
    MyEditor ed = new MyEditor();

    private String[] useGpsdChoices = new String[] { MyLocale.getMsg(641, "No"), MyLocale.getMsg(99999, "Yes (< v2.91)"), MyLocale.getMsg(99999, "Yes (>= v2.91)"), };

    public Editor getEditor() {
	// The following lines are mainly copied from SerialPortOptions.
	// Reason: We want to use MyEditor instead of the default Editor,
	//         because the latter places the ok/cancel buttons centered.
	// Because this is from the general SerialPortOptions class, maybe not all of the code
	// must be necessary.
	ed.objectClass = Reflect.getForObject(this);
	ed.sampleObject = this;
	ed.setObject(this);
	ed.title = MyLocale.getMsg(7100, "Serial Port Options");
	InputStack is = new InputStack();
	ed.addLast(is).setCell(CellConstants.HSTRETCH);
	CellPanel cp = new CellPanel();
	ed.addField(cp.addNext(new mComboBox()).setCell(CellConstants.HSTRETCH), "portName");
	btnUpdatePortList = new mButton(MyLocale.getMsg(7101, "Update Ports$u"));
	ed.addField(cp.addLast(btnUpdatePortList).setCell(CellConstants.DONTSTRETCH), "update");
	is.add(cp, "Port:$p");
	mComboBox cb = new mComboBox();
	is.add(ed.addField(cb, "baudRate"), MyLocale.getMsg(7102, "Baud:$b"));
	cb.choice.addItems(ewe.util.mString.split("110|300|1200|2400|4800|9600|19200|38400|57600|115200"));
	//
	// End of copy from SerialPortOptions.
	//
	ed.buttonConstraints = CellConstants.HFILL;
	btnScan = new mButton(MyLocale.getMsg(7103, "Scan$u"));
	btnScan.setCell(CellConstants.DONTSTRETCH);
	ed.addField(ed.addNext(btnScan), "scan");
	btnTest = new mButton(MyLocale.getMsg(7104, "Test$t"));
	ed.addField(ed.addLast(btnTest.setCell(CellConstants.DONTSTRETCH)), "test");
	txtOutput = new TextDisplay();
	ScrollBarPanel sbp = new MyScrollBarPanel(txtOutput);
	sbp.setOptions(ScrollablePanel.AlwaysShowVerticalScrollers | ScrollablePanel.AlwaysShowHorizontalScrollers);
	ed.addField(ed.addLast(sbp), "out");

	forwardGpsChkB = new mCheckBox("");
	ed.addField(ed.addNext(forwardGpsChkB, CellConstants.DONTSTRETCH, (CellConstants.EAST | CellConstants.DONTFILL)), "forwardGpsChkB");
	labelForwardHost = new mLabel(MyLocale.getMsg(7105, "Forward GPS data to host (serial port only)"));
	ed.addField(ed.addNext(labelForwardHost, CellConstants.DONTSTRETCH, (CellConstants.WEST | CellConstants.DONTFILL)), "labelForwardIP");
	inputBoxForwardHost = new mInput("tcpForwardHost");
	inputBoxForwardHost.setPromptControl(labelForwardHost);
	inputBoxForwardHost.setToolTip(MyLocale.getMsg(7106, "All data from GPS will be sent to TCP-port 23\n and can be redirected there to a serial port\n by HW Virtual Serial Port"));
	ed.addField(ed.addLast(inputBoxForwardHost, 0, (CellConstants.WEST | CellConstants.HFILL)), "tcpForwardHost");

	logGpsChkB = new mCheckBox("");
	ed.addField(ed.addNext(logGpsChkB, CellConstants.DONTSTRETCH, (CellConstants.EAST | CellConstants.DONTFILL)), "logGpsChkB");
	labelLogTimer = new mLabel(MyLocale.getMsg(7107, "Interval in sec for logging (serial port only)"));
	ed.addField(ed.addNext(labelLogTimer, CellConstants.DONTSTRETCH, (CellConstants.WEST | CellConstants.DONTFILL)), "labelLogTimer");
	inputBoxLogTimer = new mInput("GPSLogTimer");
	inputBoxLogTimer.setPromptControl(labelLogTimer);
	ed.addField(ed.addLast(inputBoxLogTimer, 0, (CellConstants.WEST | CellConstants.HFILL)), "GPSLogTimer");

	labelUseGpsd = new mLabel(MyLocale.getMsg(7121, "Receive GPS data from gpsd:"));
	ed.addField(ed.addNext(labelUseGpsd, CellConstants.DONTSTRETCH, (CellConstants.EAST | CellConstants.DONTFILL)), "labelUseGpsd");
	chcUseGpsd = new mChoice(useGpsdChoices, 0);
	chcUseGpsd.setPromptControl(labelUseGpsd);
	chcUseGpsd.setToolTip(MyLocale.getMsg(7122, "GPS data will be received from a gpsd server, not from a serial port"));
	ed.addField(ed.addLast(chcUseGpsd, 0, (CellConstants.WEST | CellConstants.HFILL)), "UseGpsd");

	labelGpsdHost = new mLabel(MyLocale.getMsg(99999, "gpsd host:"));
	ed.addField(ed.addNext(labelGpsdHost, CellConstants.DONTSTRETCH, (CellConstants.EAST | CellConstants.DONTFILL)), "labelGpsdHost");
	inputBoxGpsdHost = new mInput("GpsdHost");
	inputBoxGpsdHost.setPromptControl(labelGpsdHost);
	ed.addField(ed.addLast(inputBoxGpsdHost, 0, (CellConstants.WEST | CellConstants.HFILL)), "GpsdHost");

	this.ed.firstFocus = btnUpdatePortList;
	gpsRunning = false;
	return ed;
    }

    boolean interruptScan = false;
    boolean scanRunning = false;

    public void action(String field, Editor ed_) {
	if (field.equals("scan")) {
	    if (scanRunning == false) {
		txtOutput.setText("");
		new mThread() {
		    public void run() {
			btnTest.set(ControlConstants.Disabled, true);
			btnTest.repaintNow();
			btnScan.setText(Gui.getTextFrom(MyLocale.getMsg(7119, "Stop")));
			btnScan.repaintNow();
			String[] ports = SerialPort.enumerateAvailablePorts(); // in case of bluethooth this can take several seconds
			if (ports == null) {
			    txtOutput.appendText(MyLocale.getMsg(7109, "Could not get list of available serial ports\n"), true);
			} else {
			    scanRunning = true;
			    interruptScan = false;
			    int i;
			    for (i = 0; i < ports.length; i++) {
				if (interruptScan) {
				    txtOutput.appendText(MyLocale.getMsg(7120, "Canceled"), true); // MyLocale.getMsg(7109, "Could not get list of available serial ports\n"), true);
				    fin();
				    return;
				}
				if (!testPort(ports[i], baudRate))
				    continue;
				else {
				    portName = ports[i];
				    if (ed != null)
					ed.toControls("portName");
				    break;
				}
			    }
			    if (i >= ports.length)
				txtOutput.appendText(MyLocale.getMsg(7110, "GPS not found\n"), true);
			}
			fin();
		    }

		    private void fin() {
			scanRunning = false;
			if (btnTest != null) {
			    btnTest.set(ControlConstants.Disabled, false);
			    btnTest.repaintNow();
			}
			if (btnScan != null) {
			    btnScan.setText(Gui.getTextFrom(MyLocale.getMsg(7103, "Scan$u")));
			    btnScan.repaintNow();
			}
		    }
		}.start();
	    } else { // port scan running -> stop it.
		interruptScan = true;
	    }
	}
	if (field.equals("test")) {
	    if (!gpsRunning) {
		ed_.fromControls();

		switch (Global.pref.useGPSD) {
		case Preferences.GPSD_FORMAT_NEW:
		    txtOutput.setText(MyLocale.getMsg(99999, "Displaying data from gpsd directly (JSON):\n"));
		    try {
			btnScan.set(ControlConstants.Disabled, true);
			btnScan.repaintNow();
			gpsdThread = new GpsdThread(txtOutput);
			gpsdThread.start();
			btnTest.setText(Gui.getTextFrom(MyLocale.getMsg(7118, "Stop")));
			gpsRunning = true;
		    } catch (IOException e) {
			new InfoBox(MyLocale.getMsg(4403, "Error"), MyLocale.getMsg(99999, "Could not connect to GPSD: ") + e.getMessage() + MyLocale.getMsg(99999, "\nPossible reasons:\nGPSD is not running or GPSD host is not reachable"))
				.wait(FormBase.OKB);
		    } catch (Exception e) {
			// Other error (JSON/GPS).
			new InfoBox(MyLocale.getMsg(4403, "Error"), MyLocale.getMsg(99999, "Could not initialize GPSD connection: ") + e.getMessage()).wait(FormBase.OKB);
		    }
		    break;

		case Preferences.GPSD_FORMAT_OLD:
		    txtOutput.setText(MyLocale.getMsg(99999, "Displaying data from gpsd directly (old protocol):\n"));
		    try {
			btnScan.set(ControlConstants.Disabled, true);
			btnScan.repaintNow();
			oldGpsdThread = new OldGpsdThread(txtOutput);
			oldGpsdThread.start();
			btnTest.setText(Gui.getTextFrom(MyLocale.getMsg(7118, "Stop")));
			gpsRunning = true;
		    } catch (IOException e) {
			new InfoBox(MyLocale.getMsg(4403, "Error"), MyLocale.getMsg(99999, "Could not connect to GPSD: ") + e.getMessage() + MyLocale.getMsg(99999, "\nPossible reasons:\nGPSD is not running or GPSD host is not reachable"))
				.wait(FormBase.OKB);
		    }
		    break;

		case Preferences.GPSD_DISABLED:
		default:
		    txtOutput.setText(MyLocale.getMsg(7117, "Displaying data from serial port directly:\n"));
		    try {
			btnScan.set(ControlConstants.Disabled, true);
			btnScan.repaintNow();
			this.portName = Common.fixSerialPortName(portName);
			serThread = new mySerialThread(this, txtOutput);
			serThread.start();
			btnTest.setText(Gui.getTextFrom(MyLocale.getMsg(7118, "Stop")));
			gpsRunning = true;
		    } catch (IOException e) {
			btnScan.set(ControlConstants.Disabled, false);
			btnScan.repaintNow();
			txtOutput.appendText(MyLocale.getMsg(7108, "Failed to open serial port: ") + this.portName + ", IOException: " + e.getMessage() + "\n", true);
		    }
		    break;
		}
	    } else {
		if (serThread != null)
		    serThread.stop();
		if (gpsdThread != null)
		    gpsdThread.stop();
		if (oldGpsdThread != null)
		    oldGpsdThread.stop();
		btnTest.setText(Gui.getTextFrom(MyLocale.getMsg(7104, "Test$t")));
		gpsRunning = false;
		btnScan.set(ControlConstants.Disabled, false);
		btnScan.repaintNow();
	    }

	}

	super.action(field, ed_);
    }

    public void fieldEvent(FieldTransfer xfer, Editor editor, Object event) {
	if (event != null && event instanceof EditorEvent) {
	    EditorEvent ev = (EditorEvent) event;
	    if (xfer.fieldName.equals("_editor_")) {
		if (ev.type == EditorEvent.CLOSED) {
		    if (serThread != null)
			serThread.stop();
		}
	    }
	    super.fieldEvent(xfer, editor, event);
	}
    }

    private boolean testPort(String port, int baud) {
	mySerialThread gpsPort;
	long now;

	SerialPortOptions testspo = new SerialPortOptions();
	testspo.baudRate = baud;
	testspo.portName = Common.fixSerialPortName(port);
	try {
	    gpsPort = new mySerialThread(testspo, null);
	} catch (IOException e) {
	    txtOutput.appendText(MyLocale.getMsg(7108, "Failed to open serial port: ") + testspo.portName + "\n", true);
	    return false;
	}
	//if (!gpsPort.isOpen()) txtOutput.appendText(MyLocale.getMsg(7108, "Failed (2) to open serial port: ") + this.portName + "\n", true);

	//try to read some data
	now = new Time().getTime();
	txtOutput.appendText(MyLocale.getMsg(7111, "Trying ") + port + MyLocale.getMsg(7112, " at ") + baud + " Baud\n", true);
	gpsPort.start();
	boolean gpsfound = false;
	boolean gotdata = false;
	while ((new Time().getTime() - now) < 3000 && !gpsfound) {
	    //			gpsLen = gpsPort.lastgot.length(); // nonBlockingRead(gpsBuff,0, gpsBuff.length);
	    //txtOutput.appendText("gpsLen: " + gpsLen, true);
	    if (gpsPort.lastgot != null) {
		if (!gotdata) {
		    gotdata = true;
		    txtOutput.appendText(MyLocale.getMsg(7113, " - got some data\n"), true);
		    now = new Time().getTime(); // if receiced some data, give the GPS some extra time to send NMEA data (e.g. Sirf initially sends some non-NMEA text info about it self) 
		}
		if (gpsPort.nonBlockingRead().indexOf("$GP", 0) >= 0)
		    gpsfound = true;
	    }
	    try {
		ewe.sys.mThread.sleep(200);
	    } catch (InterruptedException e) {
		// Global.pref.log("Ignored exception", e, true);
	    }
	}
	gpsPort.stop();
	if (gpsfound)
	    txtOutput.appendText(MyLocale.getMsg(7114, " - GPS Port found\n"), true);
	else {
	    if (gotdata)
		txtOutput.appendText(MyLocale.getMsg(7115, " - No GPS data tag found\n"), true);
	    else
		txtOutput.appendText(MyLocale.getMsg(7116, " - No data received\n"), true);
	}
	//catch (IOException io) { txtOutput.appendText("error closing serial port", true); }
	return gpsfound;
    }

}
