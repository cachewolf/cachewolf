package cachewolf.imp;

import ewesoft.xml.*;
import java.util.*;
import ewesoft.xml.sax.*;
import java.io.*;

import cachewolf.CWPoint;

import eve.sys.*;
import eve.ui.Form;

/**
 * Class to import coordinates from a KML file generated from
 * google earth.
 * it looks for <placemark><MultiGeometry><LineString><coordinates>
 * and gathers all coordinated in a vector
 * @author Bilbowolf
 *
 */
public class KMLImporter extends MinML {
	
	public Vector points = new Vector();
	private CWPoint point = new CWPoint();
	private String file = "";
	String strData = "";
	private int status = 0;
	private static int MultiGeometry = 1;
	private static int LineString = 2;
	private static int coordinates = 3;
	
	public KMLImporter(String file){
		this.file = file;
	}
	
	public void importFile() {
		try{
			Reader r;
			Form.showWait();
			r = new FileReader(file);
			parse(r);
			r.close();
			Form.cancelWait();
		}catch(Exception e){
			//Vm.debug(e.toString());
			Form.cancelWait();
		}
	}
	
	public Vector getPoints(){
		return points;
	}
	
	public void startElement(String name, AttributeList atts){
		strData = "";
		if(name.equals("MultiGeometry")) status = MultiGeometry;
		if(name.equals("LineString") && status == MultiGeometry) status = LineString;
		if(name.equals("coordinates") && status == LineString) status = coordinates;
	}
	
	public void endElement(String name){
		if(name.equals("coordinates") && status == coordinates){
			parseCoordinatesLine();
			//10.09052,49.78188000000001,0
		}
		if(name.equals("LineString") && status == coordinates) status = LineString;
		if(name.equals("MultiGeometry") && status == LineString) status = 0;
	}
	
	public void characters(char[] ch,int start,int length){
		String chars = new String(ch,start,length);
		strData += chars;
	}
	
	private void parseCoordinatesLine(){
		StringTokenizer exBlock = new StringTokenizer(strData, " ");
		StringTokenizer numbers;
		String lat, lon;
		String test;
		
		while(exBlock.hasMoreTokens()){
			test = exBlock.nextToken();
			//Vm.debug("==> " + test + " <==");
			numbers = new StringTokenizer(test, ",");
			//Vm.debug(numbers.nextToken());
			//Vm.debug(numbers.nextToken());
			lon = numbers.nextToken();
			lat = numbers.nextToken();
			point = new CWPoint(Convert.parseDouble(lat),Convert.parseDouble(lon));
			points.add(point);
		}
	}
}