package cachewolf;

import eve.ui.*;
import java.io.*;

import eve.ui.formatted.TextDisplay;
import eve.sys.*;
import eve.ui.data.SerialPortOptions;
import eve.io.SerialPort;
import eve.util.mString;
import eve.ui.data.Editor;
import eve.ui.data.EditorEvent;
import java.lang.InterruptedException;
import eve.ui.ComboBox;
import eve.ui.Button;
import cachewolf.utils.Common;


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
			} catch (InterruptedException e) {}
			if (comSp != null)	{
				try {
					if (comSp.getInputStream().available() > 0)	{
						comSp.getInputStream().read(comBuff,0,comSp.getInputStream().available());
						String str = mString.fromAscii(comBuff, 0, comLength).toUpperCase();
						lastgot=str;
						if (out != null) out.appendText(str,true);
					}
				} catch(Exception ex) {}
			}
		}
	}

	public String nonBlockingRead() {
		String ret = new String(lastgot); //mString.fromAscii(gpsBuff,0,gpsLen);
		lastgot = null;
		return ret;

	}

	public boolean stopThread() {
		run = false;
		boolean ret=false;
		if (comSp != null) {
			try {
				comSp.close(); //compSp == null can happen if a exception occured
			} catch (IOException ex) {
				ret=true;
			}
			try { eve.sys.mThread.sleep(500); // wait in order to give the system time to close the serial port
			} catch (InterruptedException e) {}
		}
		else ret = true;
		return ret;
	}


}

public class GPSPortOptions extends SerialPortOptions {
	TextDisplay txtOutput;
	Button btnTest, btnUpdatePortList, btnScan;
	public Input inputBoxForwardHost;
	Label  labelForwardHost;
	public CheckBox forwardGpsChkB;
	public Input inputBoxLogTimer;
	Label  labelLogTimer;
	public CheckBox logGpsChkB;
	mySerialThread serThread;
	boolean gpsRunning = false;
	MyEditor ed = new MyEditor();


	public Editor getEditor(){
		// The following lines are mainly copied from SerialPortOptions.
		// Reason: We want to use MyEditor instead of the default Editor,
		//         because the latter places the ok/cancel buttons centered.
		// Because this is from the general SerialPortOptions class, maybe not all of the code
		// must be necessary.
		ed.objectClass = getClass();
		ed.sampleObject = this;
		ed.setObject(this);
		ed.title = MyLocale.getMsg(7100, "Serial Port Options");
		InputStack is = new InputStack();
		ed.addLast(is).setCell(CellConstants.HSTRETCH);
		CellPanel cp = new CellPanel();
		ed.addField(cp.addNext(new ComboBox()).setCell(CellConstants.HSTRETCH),"portName");
		btnUpdatePortList = new Button(MyLocale.getMsg(7101,"Update Ports$u"));
		ed.addField(cp.addLast(btnUpdatePortList).setCell(CellConstants.DONTSTRETCH),"update");
		is.add(cp,"Port:$p");
		ComboBox cb = new ComboBox();
		is.add(ed.addField(cb,"baudRate"),MyLocale.getMsg(7102,"Baud:$b"));
		cb.choice.addItems(eve.util.mString.split("110|300|1200|2400|4800|9600|19200|38400|57600|115200"));
		//
		// End of copy from SerialPortOptions.
		//
		ed.buttonConstraints = CellConstants.HFILL;
		btnScan = new Button(MyLocale.getMsg(7103,"Scan$u"));
		btnScan.setCell(CellConstants.DONTSTRETCH);
		ed.addField(ed.addNext(btnScan),"scan");
		btnTest = new Button(MyLocale.getMsg(7104,"Test$t"));
		ed.addField(ed.addLast(btnTest.setCell(CellConstants.DONTSTRETCH)),"test");
		txtOutput = new TextDisplay();
		ScrollBarPanel sbp = new MyScrollBarPanel(txtOutput);
		sbp.setOptions(ScrollablePanel.AlwaysShowVerticalScrollers | ScrollablePanel.AlwaysShowHorizontalScrollers);
		ed.addField(ed.addLast(sbp),"out");
		forwardGpsChkB = new CheckBox("");
		ed.addField(ed.addNext(forwardGpsChkB, CellConstants.DONTSTRETCH, (CellConstants.EAST | CellConstants.DONTFILL)), "forwardGpsChkB");
		labelForwardHost = new Label(MyLocale.getMsg(7105, "Forward GPS data to host"));
		ed.addField(ed.addNext(labelForwardHost, CellConstants.DONTSTRETCH, (CellConstants.WEST | CellConstants.DONTFILL)), "labelForwardIP");
		inputBoxForwardHost = new Input("tcpForwardHost");
		inputBoxForwardHost.setPromptControl(labelForwardHost);
		inputBoxForwardHost.setToolTip(MyLocale.getMsg(7106, "All data from GPS will be sent to TCP-port 23\n and can be redirected there to a serial port\n by HW Virtual Serial Port"));
		ed.addField(ed.addLast(inputBoxForwardHost,0 , (CellConstants.WEST | CellConstants.HFILL)), "tcpForwardHost");
		logGpsChkB = new CheckBox("");
		ed.addField(ed.addNext(logGpsChkB, CellConstants.DONTSTRETCH, (CellConstants.EAST | CellConstants.DONTFILL)), "logGpsChkB");
		labelLogTimer = new Label(MyLocale.getMsg(7107, "Interval in sec for logging"));
		ed.addField(ed.addNext(labelLogTimer, CellConstants.DONTSTRETCH, (CellConstants.WEST | CellConstants.DONTFILL)), "labelLogTimer");
		inputBoxLogTimer = new Input("GPSLogTimer");
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
						btnTest.set(Button.Disabled, true);
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
							btnTest.set(Button.Disabled, false);
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
					btnScan.set(Button.Disabled, true);
					btnScan.repaintNow();
					this.portName = Common.fixSerialPortName(portName);
					serThread = new mySerialThread(this, txtOutput);
					serThread.start();
					btnTest.setText(Gui.getTextFrom(MyLocale.getMsg(7118, "Stop")));
					gpsRunning = true;
				} catch (IOException e) {
					btnScan.set(Button.Disabled, false);
					btnScan.repaintNow();
					txtOutput.appendText(MyLocale.getMsg(7108, "Failed to open serial port: ") + this.portName + ", IOException: " + e.getMessage() + "\n", true);
				}
			}
			else {
				serThread.stopThread();
				btnTest.setText(Gui.getTextFrom(MyLocale.getMsg(7104,"Test$t")));
				gpsRunning = false;
				btnScan.set(Button.Disabled, false);
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
					if (serThread != null) serThread.stopThread();
				}
			}
			super.fieldEvent(xfer,editor,event);
		}
	}

	private boolean testPort(String port, int baud){
		mySerialThread gpsPort;
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
			try {eve.sys.mThread.sleep(200); } catch (InterruptedException e) {}
		}
		gpsPort.stopThread();
		if (gpsfound)	 txtOutput.appendText(MyLocale.getMsg(7114, " - GPS Port found\n"), true);
		else {
			if (gotdata) txtOutput.appendText(MyLocale.getMsg(7115, " - No GPS data tag found\n"), true);
			else         txtOutput.appendText(MyLocale.getMsg(7116, " - No data received\n"), true);
		}
		//catch (IOException io) { txtOutput.appendText("error closing serial port", true); }
		return gpsfound;
	}

}
