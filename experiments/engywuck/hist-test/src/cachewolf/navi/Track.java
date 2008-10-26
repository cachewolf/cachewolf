package CacheWolf.navi;

import ewe.fx.*;

public class Track {
	public Color trackColor;
	public TrackPoint TrackPoints[];
	public int num;
	
	public Track(Color f) {
		trackColor = f;
		TrackPoints = new TrackPoint[5000];
		num = 0;
	}
/*===== add is currently not used	
	public void add(double lat, double lon) {
		TrackPoints[num] = new TrackPoint(lat, lon);
		num++;
	}
=====*/	
	/**
	 * throws IndexOutOfBoundsException when track is full
	 */
	public void add(TrackPoint t) { 
		if (TrackPoints == null || t == null) return;
		TrackPoints[num] = new TrackPoint(t);
		num++;
	}
/*===== loadTrack/saveTrack are currently not used
	public void loadTrack(String filename){ // TODO untested!
		byte [] all = ewe.sys.Vm.readResource(null,filename);
		if (all == null) return; // TODO error handling
		num = Utils.readInt(all, 0, 4);
		for (int i=0; i<=num; i++){
			TrackPoints[i].latDec = Double.longBitsToDouble(Utils.readLong(all, (i*2)*8 + 4));
			TrackPoints[i].lonDec = Double.longBitsToDouble(Utils.readLong(all, (i*2+1)*8 + 4));
		}
	}
	
	public void saveTrack(String filename){  // TODO untested!
	//ByteArray ba=new ByteArray();
	byte[] ba = new byte[8*2*num+4]; // 8 bytes is one double int has size 4
	Utils.writeInt(num, ba, 0, 4);
	for (int i=0; i<=num; i++){
		Utils.writeLong(Double.doubleToLongBits(TrackPoints[i].latDec), ba, (i*2)*8 + 4);
		Utils.writeLong(Double.doubleToLongBits(TrackPoints[i].lonDec), ba, (i*2+1)*8 + 4);
	}
	try{
		PrintWriter outp =  new PrintWriter(new BufferedWriter(new FileWriter(filename)));
		outp.print(ba.toString());
	}catch (IOException e) {
		(new MessageBox("Error", "Error reading trackfile:\n"+e.toString(), MessageBox.OKB)).execute();}
	}
=====*/

}
