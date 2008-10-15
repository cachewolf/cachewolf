package cachewolf;
import cachewolf.utils.Common;
import cachewolf.utils.STRreplace;

import com.stevesoft.eve_pat.Regex;

import java.io.BufferedWriter;
import eve.io.File;

import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.IOException;
import eve.ui.MessageBox;

import eve.sys.Vm;
import java.util.*;


import HTML.Template;
import eve.ui.Form;


public class ShowCacheInBrowser {
	String pd=File.getProgramDirectory();
	String saveTo=pd+"/temp.html";
	static Hashtable diff=null;
	static Hashtable terr=null;
	static Hashtable args=null;

	ShowCacheInBrowser() {
		if (diff==null) {
			diff=new Hashtable(15);
			String y="<img src=\"file://" + pd + "/y.png\" border=0>";
			String y2="<img src=\"file://" + pd + "/y2.png\" border=0>";
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
			String g="<img src=\"file://" + pd + "/g.png\" border=0>";
			String g2="<img src=\"file://" + pd + "/g2.png\" border=0>";
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
			args.put("filename", pd+"/GCTemplate.html");
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
				Form.showWait();
				try {
					if (chD.wayPoint.startsWith("OC"))
						tpl.setParam("TYPE", "\"file://"+File.getProgramDirectory()+"/"+CacheType.transOCType(chD.type)+".gif\"");
					else
						tpl.setParam("TYPE", "\"file://"+File.getProgramDirectory()+"/"+chD.type+".gif\"");
					tpl.setParam("SIZE", chD.cacheSize);
					tpl.setParam("WAYPOINT", chD.wayPoint);
					tpl.setParam("CACHE_NAME", chD.cacheName);
					tpl.setParam("OWNER", chD.cacheOwner);
					if (chD.hard.endsWith(".0")) chD.hard=chD.hard.substring(0,chD.hard.length()-2);
					tpl.setParam("DIFFICULTY", (String) diff.get(chD.hard.replace(',','.')));
					if (chD.terrain.endsWith(".0")) chD.terrain=chD.terrain.substring(0,chD.terrain.length()-2);
					tpl.setParam("TERRAIN", (String) terr.get(chD.terrain.replace(',','.')));
					tpl.setParam("DISTANCE", chD.distance.replace(',','.'));
					tpl.setParam("BEARING", chD.bearing);
					if (chD.pos!=null && chD.pos.isValid()) {
						tpl.setParam("LATLON", chD.latLon);
					} else {
						tpl.setParam("LATLON", "unknown");
					}
					// If status is of format yyyy-mm-dd prefix it with a "Found" message in local language
					if (chD.cacheStatus.length()>=10 && chD.cacheStatus.charAt(4)=='-')
						tpl.setParam("STATUS",MyLocale.getMsg(318,"Found")+" "+chD.cacheStatus);
					else
						tpl.setParam("STATUS", chD.cacheStatus);

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

					tpl.setParam("DATE", chD.dateHidden);
					tpl.setParam("URL", chD.URL);
					if (chD.travelbugs.size()>0) tpl.setParam("BUGS",chD.travelbugs.toHtml());
					if (chD.cacheNotes!=null && chD.cacheNotes.trim().length()>0) tpl.setParam("NOTES", STRreplace.replace(chD.cacheNotes,"\n","<br/>\n"));
					if (chD.solver!=null && chD.solver.trim().length()>0) tpl.setParam("SOLVER", STRreplace.replace(chD.solver,"\n","<br/>\n"));
					// Look for images

					StringBuffer s=new StringBuffer(chD.longDescription.length());
					int start=0;
					int pos;
					int imageNo=0;
					Regex imgRex = new Regex("src=(?:\\s*[^\"|']*?)(?:\"|')(.*?)(?:\"|')");
					while (start>=0 && (pos=chD.longDescription.indexOf("<img",start))>0) {
						if (imageNo >= chD.images.size())break;
						s.append(chD.longDescription.substring(start,pos));
						imgRex.searchFrom(chD.longDescription,pos);
						String imgUrl=imgRex.stringMatched(1);
						//Vm.debug("imgUrl "+imgUrl);
						if (imgUrl.lastIndexOf('.')>0 && imgUrl.toLowerCase().startsWith("http")) {
							String imgType = (imgUrl.substring(imgUrl.lastIndexOf(".")).toLowerCase()+"    ").substring(0,4).trim();
							// If we have an image which we stored when spidering, we can display it
							if(imgType.startsWith(".png") || imgType.startsWith(".jpg") || imgType.startsWith(".gif")){
								s.append("<img src=\"file://"+
								   Global.getProfile().dataDir+chD.images.get(imageNo)+"\">");
								imageNo++;
							}
						}
						start=chD.longDescription.indexOf(">",pos);
						if (start>=0) start++;
					}
					if (start>=0) s.append(chD.longDescription.substring(start));
					tpl.setParam("DESCRIPTION", s.toString());

					// Do the remaining pictures which are not included in main body of text
					// They will be hidden initially and can be displayed by clicking on a link
					if (imageNo<chD.images.size()) {
						Vector imageVect=new Vector(chD.images.size()-imageNo);
						for (; imageNo<chD.images.size(); imageNo++) {
							Hashtable imgs=new Hashtable();
							imgs.put("IMAGE","<img src=\"file://"+
									   Global.getProfile().dataDir+chD.images.get(imageNo)+"\" border=0>");
							imgs.put("IMAGETEXT",chD.imagesText.get(imageNo));
							if (imageNo<chD.imagesInfo.size() && chD.imagesInfo.get(imageNo)!=null)
								imgs.put("IMAGECOMMENT",chD.imagesInfo.get(imageNo));
							else
								imgs.put("IMAGECOMMENT","");
							imgs.put("I","'img"+new Integer(imageNo).toString()+"'");
							imageVect.add(imgs);
						}
						tpl.setParam("IMAGES",imageVect);
					}

					Vector logVect=new Vector(chD.cacheLogs.size());
					for (int i=0; i<chD.cacheLogs.size(); i++) {
						Hashtable logs=new Hashtable();
						String log=STRreplace.replace(chD.cacheLogs.getLog(i).toHtml(),"http://www.geocaching.com/images/icons/","");
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
					if (!chD.hints.equals("null"))tpl.setParam("HINT",Common.rot13(chD.hints));

					if (chD.hasAddiWpt()) {
						Vector addiVect=new Vector(chD.addiWpts.size());
						for (int i=0; i<chD.addiWpts.size(); i++) {
							Hashtable addis=new Hashtable();
							CacheHolder ch=(CacheHolder) chD.addiWpts.get(i);
							addis.put("WAYPOINT",ch.wayPoint);
							addis.put("NAME",ch.cacheName);
							addis.put("LATLON",ch.latLon);
							addis.put("IMG","<img src=\""+CacheType.type2pic(ch.type)+"\">");
							CacheHolderDetail chDA=new CacheHolderDetail(ch);
							chDA.readCache(Global.getProfile().dataDir);
							addis.put("LONGDESC",chDA.longDescription); // Do we need to treat longDesc as above ?
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
	        detfile = new PrintWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(saveTo), "UTF8")));
			tpl.printTo(detfile);
			//detfile.print(tpl.output());
			detfile.close();
			try {
				String s = "\""+Global.getPref().browser+"\" \"file://"+saveTo+"\"";

				Vm.execCommandLine(s); //Global.getPref().browser+" \"file:"+saveTo+"\"");
				Global.getPref().log("Executing: "+s); //Global.getPref().browser+" \""+saveTo+"\"");
			} catch (IOException ex) {
				(new MessageBox(MyLocale.getMsg(321,"Error"),
					MyLocale.getMsg(1034,"Cannot start browser!") + "\n" + ex.toString() + "\n" +
					MyLocale.getMsg(1035,"Possible reason:") + "\n" +
					MyLocale.getMsg(1036,"A bug in ewe VM, please be") + "\n" +
					MyLocale.getMsg(1037,"patient for an update"),Form.OKB)).execute();
			}

		} catch(Exception e) {
			e.printStackTrace();
			Global.getPref().log("Error in ShowCache "+e.toString());
		} finally {
			Form.cancelWait();
		}
	}
}
