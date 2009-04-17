    /*
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
    the Free Software Foundation; either version 2 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
    */

package CacheWolf;

import ewe.ui.*;
import ewe.io.*;
import ewe.ui.formatted.TextDisplay;
import ewe.reflect.FieldTransfer;
import ewe.reflect.Reflect;
import ewe.sys.*;
import ewe.util.*;

/**
 * Thread for reading data from COM-port
 *
 */
class mySerialThread extends mThread{
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
		while (run){
			try {
				sleep(200);
			} catch (InterruptedException e) {
				Global.getPref().log("Ignored exception", e, true);
			}
			if (comSp != null)	{  
				comLength = comSp.nonBlockingRead(comBuff, 0 ,comBuff.length);
				if (comLength > 0)	{
					String str = mString.fromAscii(comBuff, 0, comLength).toUpperCase();
					lastgot = str;
					if (out != null) out.appendText(str,true);
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
			try { ewe.sys.mThread.sleep(500); // wait in order to give the system time to close the serial port
			} catch (InterruptedException e) {
				Global.getPref().log("Ignored exception", e, true);
			}
		}
		else ret = true;
		return ret;
	}
}

public class GPSPortOptions extends SerialPortOptions {
	TextDisplay txtOutput;
	mButton btnTest, btnUpdatePortList, btnScan;
	public mInput inputBoxForwardHost;
	mLabel  labelForwardHost;
	public mCheckBox forwardGpsChkB;
	public mInput inputBoxLogTimer;
	mLabel  labelLogTimer;
	public mCheckBox logGpsChkB;
	mySerialThread serThread;
	boolean gpsRunning = false;
	MyEditor ed = new MyEditor();

	
	public Editor getEditor(){
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
		ed.addField(cp.addNext(new mComboBox()).setCell(CellConstants.HSTRETCH),"portName");
		btnUpdatePortList = new mButton(MyLocale.getMsg(7101,"Update Ports$u"));
		ed.addField(cp.addLast(btnUpdatePortList).setCell(CellConstants.DONTSTRETCH),"update");
		is.add(cp,"Port:$p");
		mComboBox cb = new mComboBox();
		is.add(ed.addField(cb,"baudRate"),MyLocale.getMsg(7102,"Baud:$b"));
		cb.choice.addItems(ewe.util.mString.split("110|300|1200|2400|4800|9600|19200|38400|57600|115200"));
		//
		// End of copy from SerialPortOptions.
		//
		ed.buttonConstraints = CellConstants.HFILL;
		btnScan = new mButton(MyLocale.getMsg(7103,"Scan$u"));
		btnScan.setCell(CellConstants.DONTSTRETCH);
		ed.addField(ed.addNext(btnScan),"scan");
		btnTest = new mButton(MyLocale.getMsg(7104,"Test$t"));
		ed.addField(ed.addLast(btnTest.setCell(CellConstants.DONTSTRETCH)),"test");
		txtOutput = new TextDisplay();
		ScrollBarPanel sbp = new MyScrollBarPanel(txtOutput);
		sbp.setOptions(ScrollablePanel.AlwaysShowVerticalScrollers | ScrollablePanel.AlwaysShowHorizontalScrollers);
		ed.addField(ed.addLast(sbp),"out");
		forwardGpsChkB = new mCheckBox("");
		ed.addField(ed.addNext(forwardGpsChkB, CellConstants.DONTSTRETCH, (CellConstants.EAST | CellConstants.DONTFILL)), "forwardGpsChkB");
		labelForwardHost = new mLabel(MyLocale.getMsg(7105, "Forward GPS data to host"));
		ed.addField(ed.addNext(labelForwardHost, CellConstants.DONTSTRETCH, (CellConstants.WEST | CellConstants.DONTFILL)), "labelForwardIP");
		inputBoxForwardHost = new mInput("tcpForwardHost");
		inputBoxForwardHost.setPromptControl(labelForwardHost);
		inputBoxForwardHost.setToolTip(MyLocale.getMsg(7106, "All data from GPS will be sent to TCP-port 23\n and can be redirected there to a serial port\n by HW Virtual Serial Port"));
		ed.addField(ed.addLast(inputBoxForwardHost,0 , (CellConstants.WEST | CellConstants.HFILL)), "tcpForwardHost");
		logGpsChkB = new mCheckBox("");
		ed.addField(ed.addNext(logGpsChkB, CellConstants.DONTSTRETCH, (CellConstants.EAST | CellConstants.DONTFILL)), "logGpsChkB");
		labelLogTimer = new mLabel(MyLocale.getMsg(7107, "Interval in sec for logging"));
		ed.addField(ed.addNext(labelLogTimer, CellConstants.DONTSTRETCH, (CellConstants.WEST | CellConstants.DONTFILL)), "labelLogTimer");
		inputBoxLogTimer = new mInput("GPSLogTimer");
		inputBoxLogTimer.setPromptControl(labelLogTimer);
		ed.addField(ed.addLast(inputBoxLogTimer,0 , (CellConstants.WEST | CellConstants.HFILL)), "GPSLogTimer");
		this.ed.firstFocus = btnUpdatePortList;
		gpsRunning = false;
		return ed;
	}
	boolean interruptScan = false;
	boolean scanRunning = false;
	public void action(String field,Editor ed_){
		if (field.equals("scan")) {
			if (scanRunning == false) {
				txtOutput.setText("");
				new mThread() {
					public void run() {
						btnTest.set(ControlConstants.Disabled, true);
						btnTest.repaintNow();
						btnScan.setText(Gui.getTextFrom(MyLocale.getMsg(7119,"Stop")));
						btnScan.repaintNow();
						String[] ports = SerialPort.enumerateAvailablePorts(); // in case of bluethooth this can take several seconds
						if (ports == null) {
							txtOutput.appendText(MyLocale.getMsg(7109, "Could not get list of available serial ports\n"), true);
						} else {
							scanRunning = true;
							interruptScan = false;
							int i;
							for (i=0; i<ports.length; i++){
								if (interruptScan) { 
									txtOutput.appendText(MyLocale.getMsg(7120, "Canceled"), true); // MyLocale.getMsg(7109, "Could not get list of available serial ports\n"), true);
									fin();
									return; 
								}
								if (!testPort(ports[i], baudRate)) 	continue;
								else {
									portName = ports[i];
									if (ed != null) ed.toControls("portName");
									break;
								}
							}
							if (i >= ports.length) txtOutput.appendText(MyLocale.getMsg(7110, "GPS not found\n"), true);
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
							btnScan.setText(Gui.getTextFrom(MyLocale.getMsg(7103,"Scan$u")));
							btnScan.repaintNow();
						}
					}
				}.start();
			} else { // port scan running -> stop it.
				interruptScan = true;
			}
		}
		if (field.equals("test")){
			if (!gpsRunning){
				ed_.fromControls();
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
			}
			else {
				serThread.stop();
				btnTest.setText(Gui.getTextFrom(MyLocale.getMsg(7104,"Test$t")));
				gpsRunning = false;
				btnScan.set(ControlConstants.Disabled, false);
				btnScan.repaintNow();
			}

		}

		super.action(field, ed_);
	}
	
	public void fieldEvent(FieldTransfer xfer, Editor editor, Object event){
		if ( event != null && event instanceof EditorEvent) {
			EditorEvent ev = (EditorEvent) event;
			if (xfer.fieldName.equals("_editor_")) {
				if (ev.type == EditorEvent.CLOSED) {
					if (serThread != null) serThread.stop();
				}
			}
			super.fieldEvent(xfer,editor,event);
		}
	}
	
	private boolean testPort(String port, int baud){
		mySerialThread gpsPort; 
		int gpsLen;
		long now;
		
		SerialPortOptions testspo= new SerialPortOptions();
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
		while ( (new Time().getTime() - now) < 3000 && !gpsfound){
//			gpsLen = gpsPort.lastgot.length(); // nonBlockingRead(gpsBuff,0, gpsBuff.length);
			//txtOutput.appendText("gpsLen: " + gpsLen, true);
			if (gpsPort.lastgot != null) {
				if (!gotdata) {
					gotdata = true;
					txtOutput.appendText(MyLocale.getMsg(7113, " - got some data\n"), true);
					now = new Time().getTime(); // if receiced some data, give the GPS some extra time to send NMEA data (e.g. Sirf initially sends some non-NMEA text info about it self) 
				}
				if (gpsPort.nonBlockingRead().indexOf("$GP", 0) >= 0) gpsfound = true;
			}
			try {ewe.sys.mThread.sleep(200); } catch (InterruptedException e) {
				Global.getPref().log("Ignored exception", e, true);
			}
		}
		gpsPort.stop();
		if (gpsfound)	 txtOutput.appendText(MyLocale.getMsg(7114, " - GPS Port found\n"), true);
		else {
			if (gotdata) txtOutput.appendText(MyLocale.getMsg(7115, " - No GPS data tag found\n"), true);
			else         txtOutput.appendText(MyLocale.getMsg(7116, " - No data received\n"), true);
		}
		//catch (IOException io) { txtOutput.appendText("error closing serial port", true); }
		return gpsfound;
	}
	
}
