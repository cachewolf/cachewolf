package CacheWolf;
import ewe.ui.*;
import ewe.ui.MessageBox;
import ewe.sys.*;
import ewe.util.*;
import ewe.filechooser.*;
import ewe.io.*;

/**
*	This class displays the screen offering the user the different options
* 	to spider the caches. From this screen the spider is activated and starts
*	working.
*	Class ID = 900
*/
public class LoadScreen extends Form{
/* skg20061225: Commented out - needs to be deleted as it is not referenced
 * The functionality seems to be taken over by SpiderGC
 * 
 	
	Locale l = Vm.getLocale();
	LocalResource lr = l.getLocalResource("cachewolf.Languages",true);
	Spider mySpidy;
	mTextPad liste = new mTextPad();
	mButton btList = new mButton((String)lr.get(900,"List"));
	mButton btLoc = new mButton((String)lr.get(901,"Loc"));
	mButton btNearest = new mButton((String)lr.get(902,"Nearest"));
	mButton btCancel = new mButton((String)lr.get(903,"Cancel"));
	mButton btMulti = new mButton((String)lr.get(904,"Multi"));
	mButton btGPX = new mButton("GPX");

	Vector cacheDB = new Vector();
	Preferences myP = new Preferences();
	MessageArea msgArea;
	Handle spiderHandle;
	Lock spiderLock = new Lock(); 
	boolean spiderRunning = false;
	
	public LoadScreen(Vector DB, Preferences p){
		cacheDB = DB;
		myP = p;
		this.setPreferredSize(200,200);
		this.title = (String)lr.get(905,"Load Caches");
		this.addLast(new mLabel((String)lr.get(906,"List of Caches:")),this.DONTSTRETCH, (this.DONTFILL|this.WEST));
		this.addLast(liste, this.STRETCH, this.FILL);
		CellPanel cp = new CellPanel();
		cp.addNext(btList, this.HSTRETCH, this.FILL);
		cp.addNext(btLoc, this.HSTRETCH, this.FILL);
		cp.addNext(btNearest, this.HSTRETCH, this.FILL);
		cp.addNext(btGPX, this.HSTRETCH, this.FILL);
		cp.addLast(btMulti, this.HSTRETCH, this.FILL);
		cp.addLast(msgArea = new MessageArea(""), this.HSTRETCH, this.FILL|this.WEST);
		cp.addLast(btCancel, this.HSTRETCH, this.FILL);
		this.addLast(cp, this.HSTRETCH, this.FILL);
	} // LoadScreen
	
	private void startSpiderRunning(){ 
		 spiderLock.synchronize(); try{ 
		   //Make sure it is not already running. 
		   if (spiderRunning) return; 
		   spiderRunning = true; 
		   new mThread(){ 
		    public void run(){ 
		     //mySpidy = new Spider(cacheDB, myP, msgArea,   Spider.SPIDERLOC); 
		     //mySpidy.setUp(data); 
		     //spiderHandle = mySpidy.getHandle(); 
		     mySpidy.run();  
		     try{ 
		     spiderHandle.waitUntilStopped(); 
		     }catch(Exception e){ 
		     }finally{ 
		   spiderLock.synchronize(); 
		   spiderRunning = false; 
		   spiderLock.release(); 
		   close(0); 
		     }      
		     } 
		   }.start();    
		 }finally{spiderLock.release();} 
	} 

	public void onEvent(Event ev){
		if(ev instanceof ControlEvent && ev.type == ControlEvent.PRESSED){
			if(ev.target == btCancel){
				if(spiderRunning == true){
					//spiderHandle.stop(1);
					Handle hdl = mySpidy.stopTask(1);
					Vm.debug("Done? " + mySpidy.shouldStop);
					try{
						hdl.waitUntilStopped();
					}catch(Exception ex){}
					//mySpidy.stopTask(0);
				}
				this.close(0);
			}
			if(ev.target == btLoc){
				LOCReader lor = new LOCReader(myP.mydatadir);
				Vector data = new Vector();
				data = lor.doIt();
				mySpidy = new Spider(cacheDB, myP,msgArea, Spider.SPIDERLOC);
				mySpidy.setUp(data);
				spiderHandle = mySpidy.getHandle();
				mySpidy.run();
				try{
					spiderHandle.waitUntilStopped();
				}catch(Exception e){}
				this.close(0);
			}
			if(ev.target == btList){
				Vector data = new Vector();
				data = mString.split(liste.getText(), ',', data);
				mySpidy = new Spider(cacheDB, myP, msgArea, Spider.SPIDERLOC);
				mySpidy.setUp(data);
				spiderHandle = mySpidy.getHandle();
				mySpidy.run();
				try{
					spiderHandle.waitUntilStopped();
				}catch(Exception e){}
				this.close(0);
			}
			if(ev.target == btNearest){
				if(myP.existCenter() == true){
					String dist = new InputBox((String)lr.get(907,"Max distance:")).input("50",10);
					mySpidy = new Spider(cacheDB, myP, msgArea, Spider.SPIDERNEAREST);
					mySpidy.setUp(dist);
					spiderHandle = mySpidy.getHandle();
					startSpiderRunning();
				} else {
					MessageBox mb = new MessageBox("Error!", "Koordinates in preferences not set!", 1);
					mb.execute();
				}
				//this.close(0);
			}
			if(ev.target == btMulti){
				FileChooser fc = new FileChooser(FileChooser.OPEN, myP.mydatadir);
				fc.setTitle((String)lr.get(908,"Select waypoints file"));
				if(fc.execute() != fc.IDCANCEL){
					String dist = new InputBox((String)lr.get(907,"Max distance:")).input("1",10);
					try{
						FileReader in = new FileReader(fc.getChosenFile());
						String text = new String();
						Vector vct = new Vector();
						text = in.readAll();
						in.close();
						if(!(text.substring(text.length()-2,text.length()-1).equals(";"))){
							text = text + ";";
						}
						vct = mString.split(text, ';', vct);
						mySpidy = new Spider(cacheDB, myP,msgArea, Spider.SPIDERMULTI);
						mySpidy.setUp(vct, dist);
						spiderHandle = mySpidy.getHandle();
						mySpidy.run();
						try{
							spiderHandle.waitUntilStopped();
						}catch(Exception e){}
					}catch(Exception ex){
						//Vm.debug("Error reading waypoint file");
					}
				} else {
				}
			this.close(0);
			}
			if(ev.target == btGPX){
				FileChooser fc = new FileChooser(FileChooser.OPEN, myP.mydatadir);
				fc.addMask("*.gpx");
				fc.setTitle((String)lr.get(909,"Select GPX file"));
				if(fc.execute() != fc.IDCANCEL){
					//GPXImporter gpx = new GPXImporter(cacheDB, fc.getChosenFile().toString(),myP.mydatadir,msgArea);
					//gpx.doIt();
					//this.close(0);
				}
			}
		}
		super.onEvent(ev);
	}
*/
}
