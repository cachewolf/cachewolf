package CacheWolf;
import com.stevesoft.ewe_pat.Regex;

import ewe.io.BufferedWriter;
import ewe.io.File;
import ewe.io.FileWriter;
import ewe.io.IOException;
import ewe.io.PrintWriter;
import ewe.sys.Convert;
import ewe.sys.Vm;
import ewe.ui.InputBox;
import ewe.util.*;
import ewe.util.Hashtable;
import HTML.Template;


public class ShowCacheInBrowser {
	String saveTo=File.getProgramDirectory()+"/temp.html";
	static Hashtable diff=null;
	static Hashtable terr=null;
	static Hashtable args=null;
	
	ShowCacheInBrowser() {
		if (diff==null) {
			diff=new Hashtable(15);
			String y="<img src=\"./y.png\" border=0>";
			String y2="<img src=\"./y2.png\" border=0>";
			diff.put("1",y);
			diff.put("1.5",y+y2);
			diff.put("2",y+y);
			diff.put("2.5",y+y+y2);
			diff.put("3",y+y+y);
			diff.put("3.5",y+y+y+y2);
			diff.put("4",y+y+y+y);
			diff.put("4.5",y+y+y+y+y2);
			diff.put("5",y+y+y+y+y);
	
			terr=new Hashtable(15);
			String g="<img src=\"./g.png\" border=0>";
			String g2="<img src=\"./g2.png\" border=0>";
			terr.put("1",g);
			terr.put("1.5",g+g2);
			terr.put("2",g+g);
			terr.put("2.5",g+g+g2);
			terr.put("3",g+g+g);
			terr.put("3.5",g+g+g+g2);
			terr.put("4",g+g+g+g);
			terr.put("4.5",g+g+g+g+g2);
			terr.put("5",g+g+g+g+g);
			
			args = new Hashtable();
			args.put("filename", File.getProgramDirectory()+"/GCTemplate.html");
			args.put("case_sensitive", "true");
			args.put("loop_context_vars", Boolean.TRUE);
			args.put("max_includes", new Integer(5));
		}
	}
	
	public void showCache(CacheHolderDetail chD) {
		if (chD == null) return;
		try {
			Template tpl = new Template(args);
			if(!chD.is_filtered){
				Vm.showWait(true);
				try {
					if (chD.wayPoint.startsWith("OC"))
						tpl.setParam("TYPE", "\"./"+CacheType.transOCType(chD.type)+".gif\"");
					else	
						tpl.setParam("TYPE", "\"./"+chD.type+".gif\"");
					tpl.setParam("SIZE", chD.CacheSize);
					tpl.setParam("WAYPOINT", chD.wayPoint);
					tpl.setParam("CACHE_NAME", chD.CacheName);
					tpl.setParam("OWNER", chD.CacheOwner);
					if (chD.hard.endsWith(".0")) chD.hard=chD.hard.substring(0,chD.hard.length()-2);
					tpl.setParam("DIFFICULTY", (String) diff.get(chD.hard.replace(',','.')));
					if (chD.terrain.endsWith(".0")) chD.terrain=chD.terrain.substring(0,chD.terrain.length()-2);
					tpl.setParam("TERRAIN", (String) terr.get(chD.terrain.replace(',','.')));
					tpl.setParam("DISTANCE", chD.distance.replace(',','.'));
					tpl.setParam("BEARING", chD.bearing);
					if (chD.pos!=null && chD.pos.isValid()) {
						tpl.setParam("LATLON", chD.LatLon);
					} else {
						tpl.setParam("LATLON", "unknown");
					}
					// If status is of format yyyy-mm-dd prefix it with a "Found" message in local language
					if (chD.CacheStatus.length()>=10 && chD.CacheStatus.charAt(4)=='-')
						tpl.setParam("STATUS",MyLocale.getMsg(318,"Found")+" "+chD.CacheStatus);
					else
						tpl.setParam("STATUS", chD.CacheStatus);
					
					// Cache attributes
					if (chD.attributes.getCount()>0) {
						Vector attVect=new Vector(chD.attributes.getCount()+1);
						for (int i=0; i<chD.attributes.getCount(); i++) {
							Hashtable atts=new Hashtable();
							atts.put("IMAGE","<img src=\"file://"+
									   Attribute.getImageDir()+chD.attributes.getName(i)+
									   "\" border=0 alt=\""+chD.attributes.getInfo(i)+"\">");
							if (i % 5 ==4) 
								atts.put("BR","<br/>");
							else
								atts.put("BR","");
							atts.put("INFO",chD.attributes.getInfo(i));
							attVect.add(atts);
						}
						tpl.setParam("ATTRIBUTES",attVect);
					}
					
					tpl.setParam("DATE", chD.DateHidden);
//					tpl.setParam("URL", ch.URL);
					if (chD.Travelbugs.size()>0) tpl.setParam("BUGS",chD.Travelbugs.toHtml());
					if (chD.CacheNotes!=null && chD.CacheNotes.trim().length()>0) tpl.setParam("NOTES", STRreplace.replace(chD.CacheNotes,"\n","<br/>\n"));
					if (chD.Solver!=null && chD.Solver.trim().length()>0) tpl.setParam("SOLVER", STRreplace.replace(chD.Solver,"\n","<br/>\n"));
					// Look for images
					
					StringBuffer s=new StringBuffer(chD.LongDescription.length());
					int start=0;
					int pos;
					int imageNo=0;
					Regex imgRex = new Regex("src=(?:\\s*[^\"|']*?)(?:\"|')(.*?)(?:\"|')");
					while (start>=0 && (pos=chD.LongDescription.indexOf("<img",start))>0) {
						s.append(chD.LongDescription.substring(start,pos));
						imgRex.searchFrom(chD.LongDescription,pos);
						String imgUrl=imgRex.stringMatched(1);
						//Vm.debug("imgUrl "+imgUrl);
						if (imgUrl.lastIndexOf('.')>0 && imgUrl.toLowerCase().startsWith("http")) {
							String imgType = (imgUrl.substring(imgUrl.lastIndexOf(".")).toLowerCase()+"    ").substring(0,4).trim();
							// If we have an image which we stored when spidering, we can display it
							if(!imgType.startsWith(".com") && !imgType.startsWith(".php") && !imgType.startsWith(".exe")){
								s.append("<img src=\"file://"+
								   Global.getProfile().dataDir+chD.Images.get(imageNo)+"\">");
								imageNo++;
							}
						}
						start=chD.LongDescription.indexOf(">",pos);
						if (start>=0) start++;
						if (imageNo >= chD.Images.getCount())break;
					}
					if (start>=0) s.append(chD.LongDescription.substring(start));
					tpl.setParam("DESCRIPTION", s.toString());
					
					// Do the remaining pictures which are not included in main body of text
					// They will be hidden initially and can be displayed by clicking on a link
					if (imageNo<chD.Images.size()) {
						Vector imageVect=new Vector(chD.Images.size()-imageNo);
						for (; imageNo<chD.Images.size(); imageNo++) {
							Hashtable imgs=new Hashtable();
							imgs.put("IMAGE","<img src=\"file://"+
									   Global.getProfile().dataDir+chD.Images.get(imageNo)+"\" border=0>");
							imgs.put("IMAGETEXT",chD.ImagesText.get(imageNo));
							imgs.put("I","'img"+new Integer(imageNo).toString()+"'");
							imageVect.add(imgs);
						}
						tpl.setParam("IMAGES",imageVect);
					}
					
					Vector logVect=new Vector(chD.CacheLogs.size());
					for (int i=0; i<chD.CacheLogs.size(); i++) {
						Hashtable logs=new Hashtable();
						String log=STRreplace.replace(chD.CacheLogs.getLog(i).toHtml(),"http://www.geocaching.com/images/icons/","");
						int posGt=log.indexOf('>'); // Find the icon which defines the type of log
						if (posGt<0) {
							logs.put("LOG",log);
							logs.put("LOGTYPE","");
						} else {
							int posBr=log.indexOf("<br>"); 
							if(posBr<0) {
								logs.put("LOG",log);
								logs.put("LOGTYPE","");
							} else {
								logs.put("LOG",log.substring(posBr));
								logs.put("LOGTYPE",log.substring(0,posGt)+" border='0'"+log.substring(posGt,posBr+4));
							}
						}
						logs.put("I","'log"+new Integer(i).toString()+"'");
						logVect.add(logs);
					}
					tpl.setParam("LOGS",logVect);
					if (!chD.is_available) tpl.setParam("UNAVAILABLE","1");
					if (!chD.Hints.equals("null"))tpl.setParam("HINT",Common.rot13(chD.Hints));
					
					if (chD.hasAddiWpt()) {
						Vector addiVect=new Vector(chD.addiWpts.size());
						for (int i=0; i<chD.addiWpts.size(); i++) {
							Hashtable addis=new Hashtable();
							CacheHolder ch=(CacheHolder) chD.addiWpts.get(i);
							addis.put("WAYPOINT",ch.wayPoint);
							addis.put("NAME",ch.CacheName);
							addis.put("LATLON",ch.LatLon);
							addis.put("IMG","<img src=\""+CacheType.type2pic(Convert.parseInt(ch.type))+"\">");
							CacheHolderDetail chDA=new CacheHolderDetail(ch);
							chDA.readCache(Global.getProfile().dataDir);
							addis.put("LONGDESC",chDA.LongDescription); // Do we need to treat longDesc as above ?
							addiVect.add(addis);
						}
						tpl.setParam("ADDIS",addiVect);
					}
				}catch(Exception e){
					Vm.debug("Problem getting Parameter, Cache: " + chD.wayPoint);
					Global.getPref().log("Problem getting parameter "+e.toString()+", Cache: " + chD.wayPoint);
					e.printStackTrace();
				}
			}
			PrintWriter detfile; 
			FileWriter fw = new FileWriter(saveTo);
			detfile = new PrintWriter(new BufferedWriter(fw));
			tpl.printTo(detfile);
			//detfile.print(tpl.output());
			detfile.close();
			try {
				String s = "\""+Global.getPref().browser+"\" \"file://"+saveTo+"\"";

				Vm.exec(s); //Global.getPref().browser+" \"file:"+saveTo+"\"");
				Global.getPref().log("Executing: "+s); //Global.getPref().browser+" \""+saveTo+"\"");
			} catch (Exception ioex) {
			}
			
		} catch(Exception e) {
			e.printStackTrace();
			Global.getPref().log("Error in ShowCache "+e.toString());
		} finally {
			Vm.showWait(false);
		}
	}
}
