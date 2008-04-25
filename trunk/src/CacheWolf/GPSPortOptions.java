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
	
	public mySerialThread(SerialPortOptions spo, TextDisplay td){
		try {
			comSp = new SerialPort(spo);
		} catch (IOException e) {
			Vm.debug("Error open COM-Port " + spo.portName);
		}
		out = td;
	}
	
	public void run() {
		run = true;
		while (run){
			try {
				sleep(1000);
			} catch (InterruptedException e) {}
			if (comSp != null)	{  
				comLength = comSp.nonBlockingRead(comBuff, 0 ,comBuff.length);
				if (comLength > 0)	{
					String str = mString.fromAscii(comBuff, 0, comLength).toUpperCase();
					out.appendText(str,true);
				}
			}
		}
	}

	public void stop() {
		run = false;
		if (comSp != null) comSp.close(); //compSp == null can happen if a exception occured
	}
}

public class GPSPortOptions extends SerialPortOptions {
	TextDisplay txtOutput;
	mButton btnTest;
	public mInput inputBoxForwardHost;
	mLabel  labelForwardHost;
	public mCheckBox forwardGpsChkB;
	public mInput inputBoxLogTimer;
	mLabel  labelLogTimer;
	public mCheckBox logGpsChkB;
	mySerialThread serThread;
	boolean gpsRunning = false;

	
	public Editor getEditor(){
		MyEditor ed = new MyEditor();
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
		ed.addField(cp.addLast(new mButton(MyLocale.getMsg(7101,"Update Ports$u"))).setCell(CellConstants.DONTSTRETCH),"update");
		is.add(cp,"Port:$p");
		mComboBox cb = new mComboBox();
		is.add(ed.addField(cb,"baudRate"),MyLocale.getMsg(7102,"Baud:$b"));
		cb.choice.addItems(ewe.util.mString.split("110|300|1200|2400|4800|9600|19200|38400|57600|115200"));
		//
		// End of copy from SerialPortOptions.
		//
		ed.buttonConstraints = CellConstants.HFILL;
		ed.addField(ed.addNext(new mButton(MyLocale.getMsg(7103,"Scan$u"))).setCell(CellConstants.DONTSTRETCH),"scan");
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
		inputBoxForwardHost.setText("192.168.178.23");
		logGpsChkB = new mCheckBox("");
		ed.addField(ed.addNext(logGpsChkB, CellConstants.DONTSTRETCH, (CellConstants.EAST | CellConstants.DONTFILL)), "logGpsChkB");
		labelLogTimer = new mLabel(MyLocale.getMsg(7107, "Interval in sec for logging"));
		ed.addField(ed.addNext(labelLogTimer, CellConstants.DONTSTRETCH, (CellConstants.WEST | CellConstants.DONTFILL)), "labelLogTimer");
		inputBoxLogTimer = new mInput("GPSLogTimer");
		inputBoxLogTimer.setPromptControl(labelLogTimer);
		inputBoxLogTimer.setText("10");
		ed.addField(ed.addLast(inputBoxLogTimer,0 , (CellConstants.WEST | CellConstants.HFILL)), "GPSLogTimer");

		gpsRunning = false;
		return ed;
	}
	
	public void action(String field,Editor ed){
		if (field.equals("scan")){
			String[] ports = SerialPort.enumerateAvailablePorts();
			
			for (int i=0;i<ports.length;i++){
				// try open with default GPS baudrate
				if (!testPort(ports[i], 4800)) continue;
				else {
					this.portName = ports[i];
					ed.toControls("portName");
					this.baudRate = 4800;
					ed.toControls("baudRate");
					break;
				}
			}
		}
		if (field.equals("test")){
			if (!gpsRunning){
				ed.fromControls();
				serThread = new mySerialThread(this, txtOutput);
				serThread.start();
				btnTest.setText("Stop");
				gpsRunning = true;
			}
			else {
				serThread.stop();
				btnTest.setText("Test");
				gpsRunning = false;
			}

		}

		super.action(field, ed);
	}
	
	public void fieldEvent(FieldTransfer xfer, Editor editor, Object event){
		try {
			EditorEvent ev = (EditorEvent) event;
			if (xfer.fieldName.equals("_editor_") && (ev.type == EditorEvent.CLOSED)) {
				if (serThread != null) serThread.stop();
			}
		} catch (ClassCastException e) { } // happens if event is not an EditorEvent
		super.fieldEvent(xfer,editor,event);
	}
	
	private boolean testPort(String port, int baud){
		SerialPort gpsPort; 
		byte[] gpsBuff = new byte[1024];
		int gpsLen;
		long now;
		
		gpsPort = new SerialPort(port, baud);
		
		//try to read some data
		now = new Time().getTime();
		txtOutput.appendText("Trying " + port + " at " + baud + " Baud\n",true);
		while ( (new Time().getTime() - now) < 2000){
			gpsLen = gpsPort.nonBlockingRead(gpsBuff,0, gpsBuff.length);
			if (gpsLen > 0){
				txtOutput.appendText("Port found\n", true);
				gpsPort.close();
				return true;
			}
		}
		gpsPort.close();
		return false;
	}
	
	private boolean testGPS(String port, int baud){
		SerialPort gpsPort; 
		byte[] gpsBuff = new byte[1024];
		int gpsLen;
		long now;
		
		gpsPort = new SerialPort(port, baud);
		
		//try to read some data
		now = new Time().getTime();
		Vm.debug("Trying " + port + " at " + baud + " Baud");
		while ( (new Time().getTime() - now) < 3000){
			gpsLen = gpsPort.nonBlockingRead(gpsBuff,0, gpsBuff.length);
			Vm.debug("Len:" + gpsLen);
			if (gpsLen > 0){
				String tmp = mString.fromAscii(gpsBuff,0,gpsLen);
				Vm.debug(tmp);
				if (tmp.startsWith("$GP")){
					Vm.debug("GPS Port found");
					gpsPort.close();
					return true;
				}
			}
		}
		gpsPort.close();
		return false;
	}
}