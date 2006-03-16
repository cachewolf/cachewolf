package CacheWolf;
import ewe.filechooser.*;
import ewe.io.*;
import ewe.util.*;
import ewe.sys.*;

/**
*	This class analyses the gc.com loc filesa and extracts the cache waypoint 
*	names. When run it offers the user a file chooser screen from which the user
*	defines the directory the loc files are stored in. It searches the directory
*	for the loc files and extractes the data from each loc file.	
*/
public class LOCReader{
	private String dir = new String();
	
	public LOCReader(String d){
		dir = d;
	}
	
	public Vector doIt(){
		Vector data = new Vector();
		//need directory only!!!!
		FileChooser fc = new FileChooser(FileChooser.DIRECTORY_SELECT, dir);
		fc.setTitle("Select source directory:");
		String sourceDir; 
		String text = new String();
		String dummy = new String();
		if(fc.execute() != fc.IDCANCEL){
			sourceDir = fc.getChosen();
			//identify all loc files
			File path = new File(sourceDir);
			FileReader in;
			Extractor ex;
			String[] files = path.list("*.loc", FileBase.LIST_DONT_SORT);
			for(int i = 0; i<files.length;i++){
				try{
					in = new FileReader(sourceDir + "/" + files[i]);
					text = in.readAll();
				}catch(Exception e){
					//Vm.debug("Problem reading LOC file");
				}
				ex = new Extractor(text, "<name id=\"", "\">", 0, true);
				dummy = ex.findNext();
				while(ex.endOfSearch() == false){
					data.add(dummy);
					dummy = ex.findNext();
				}
			} //for
		}//if execute
		return data;
	}
}
