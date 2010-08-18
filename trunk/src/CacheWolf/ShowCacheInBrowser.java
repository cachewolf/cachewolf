package CacheWolf;

import com.stevesoft.ewe_pat.Regex;

import ewe.io.AsciiCodec;
import ewe.io.BufferedWriter;
import ewe.io.FileBase;
import ewe.io.FileWriter;
import ewe.io.IOException;
import ewe.io.PrintWriter;
import ewe.sys.Vm;
import ewe.ui.FormBase;
import ewe.ui.MessageBox;
import ewe.util.*;
import CacheWolf.utils.CWWrapper;
import HTML.Template;


public class ShowCacheInBrowser {
	String pd=FileBase.getProgramDirectory();
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

	public void showCache(CacheHolder chD) {
		if (chD == null) return;
		try {
			Template tpl = new Template(args);
			if(chD.isVisible()){
				Vm.showWait(true);
				try {
					tpl.setParams(chD.toHashtable(new Regex("[,.]","."), null, 0, 30, -1, new AsciiCodec(), null, true, 1, ""));
					// Look for images
					// count only the images of main body
					int start=0;
					int pos;
					int imageNo=0;
					Regex imgRex = new Regex("src=(?:\\s*[^\"|']*?)(?:\"|')(.*?)(?:\"|')");
					while (start>=0 && (pos=chD.getCacheDetails(true).LongDescription.indexOf("<img",start))>0) {
						if (imageNo >= chD.getCacheDetails(true).images.size())break;
						imgRex.searchFrom(chD.getCacheDetails(true).LongDescription,pos);
						String imgUrl=imgRex.stringMatched(1);
						if (imgUrl.lastIndexOf('.')>0 && imgUrl.toLowerCase().startsWith("http")) {
							String imgType = (imgUrl.substring(imgUrl.lastIndexOf('.')).toLowerCase()+"    ").substring(0,4).trim();
        					if(imgType.startsWith(".png") || imgType.startsWith(".jpg") || imgType.startsWith(".gif")){
								imageNo++;
							}
						}
						start=chD.getCacheDetails(true).LongDescription.indexOf(">",pos);
						if (start>=0) start++;
					}
					// Do the remaining pictures which are not included in main body of text
					// They will be hidden initially and can be displayed by clicking on a link
					if (imageNo<chD.getCacheDetails(true).images.size()) {
						Vector imageVect=new Vector(chD.getCacheDetails(true).images.size()-imageNo);
						for (; imageNo<chD.getCacheDetails(true).images.size(); imageNo++) {
							Hashtable imgs=new Hashtable();
							imgs.put("IMAGE","<img src=\"file://"+
									   Global.getProfile().dataDir+chD.getCacheDetails(true).images.get(imageNo).getFilename()+"\" border=0>");
							imgs.put("IMAGETEXT",chD.getCacheDetails(true).images.get(imageNo).getTitle());
							if (imageNo<chD.getCacheDetails(true).images.size())
								imgs.put("IMAGECOMMENT",chD.getCacheDetails(true).images.get(imageNo).getComment());
							imgs.put("I","'img"+new Integer(imageNo).toString()+"'");
							imageVect.add(imgs);
						}
						tpl.setParam("IMAGES",imageVect);
					}
					if (!chD.is_available()) tpl.setParam("UNAVAILABLE","1");
					if (!chD.getCacheDetails(true).Hints.equals("null"))tpl.setParam("HINT",Common.rot13(chD.getCacheDetails(true).Hints));
				}catch(Exception e){
					Vm.debug("Problem getting Parameter, Cache: " + chD.getWayPoint());
					Global.getPref().log("Problem getting parameter "+e.toString()+", Cache: " + chD.getWayPoint());
					e.printStackTrace();
				}
			}
			PrintWriter detfile;
			detfile = new PrintWriter(new BufferedWriter(new FileWriter(saveTo)));
			tpl.printTo(detfile);
			//detfile.print(tpl.output());
			detfile.close();
			try {
				// on pda surrounding quotes " will be converted to %22 (and a %22file:// does not work)
				CWWrapper.exec(Global.getPref().browser, "file://"+STRreplace.replace(saveTo," ","%20"),false,false);
			} catch (IOException ex) {
				(new MessageBox(MyLocale.getMsg(321,"Error"),
						MyLocale.getMsg(1034,"Cannot start browser!") + "\n" + ex.toString() + "\n" +
						MyLocale.getMsg(1035,"Possible reason:") + "\n" +
						MyLocale.getMsg(1036,"A bug in ewe VM, please be") + "\n" +
						MyLocale.getMsg(1037,"patient for an update"),FormBase.OKB)).execute();
			}

		} catch(Exception e) {
			e.printStackTrace();
			Global.getPref().log("Error in ShowCache "+e.toString());
		} finally {
			Vm.showWait(false);
		}
	}
}
