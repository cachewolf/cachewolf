    /*
    GNU General Public License
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
    the Free Software Foundation; version 2 of the License.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
    */
package CacheWolf;

import com.stevesoft.ewe_pat.Regex;
import com.stevesoft.ewe_pat.Transformer;

import ewe.fx.Font;
import ewe.sys.Vm;
import ewe.ui.CellConstants;
import ewe.ui.CellPanel;
import ewe.ui.ControlEvent;
import ewe.ui.Event;
import ewe.ui.HtmlDisplay;
import ewe.ui.ScrollBarPanel;
import ewe.ui.mButton;

/**
*	This class shows the long description on a cache.
*  Test with GC1CC5T - Final
*            GC19DDX - 
*/
public class DescriptionPanel extends CellPanel{
	HtmlDisplay disp = new HtmlDisplay();
	mButton btnPlus, btnMinus, btnText, btnHtml;
	CacheHolder currCache;
	
	CellPanel buttonP = new CellPanel();
	CellPanel descP = new CellPanel();
	
	private String desc;

	public DescriptionPanel(){
		buttonP.addNext(btnPlus = new mButton("+"),CellConstants.HSTRETCH, (CellConstants.HFILL));
		buttonP.addNext(btnText = new mButton("Text"),CellConstants.HSTRETCH, (CellConstants.HFILL));
		buttonP.addNext(btnHtml = new mButton("Html"),CellConstants.HSTRETCH, (CellConstants.HFILL));
		buttonP.addLast(btnMinus = new mButton("-"),CellConstants.HSTRETCH, (CellConstants.HFILL));
		ScrollBarPanel sbp = new MyScrollBarPanel(disp, 0);
		//sbp.setScrollBarSize(40,40, 20);
		descP.addLast(sbp);
		this.addLast(descP);
		this.addLast(buttonP,CellConstants.HSTRETCH,CellConstants.HFILL);
		clear();
	}
	
	/**
         * Set the text to display. Text should be HTML formated.
         */
    // String description = null;
    public void setText(CacheHolder cache) {
        boolean isHtml;
        if (currCache == cache) return;
        int scrollto = 0;
        if (cache == null) {
            desc = "";
            isHtml = false;
        } else {
            if (cache.hasSameMainCache(currCache))
                scrollto = disp.getTopLine();
            isHtml = cache.is_HTML();
            if (cache.isAddiWpt()) {
                isHtml = cache.mainCache.is_HTML();
                if (cache.getCacheDetails(true).LongDescription != null && cache.getCacheDetails(true).LongDescription.length() > 0)
                    desc = cache.getCacheDetails(true).LongDescription + (isHtml ? "<hr>\n" : "\n")
                            + cache.mainCache.getCacheDetails(true).LongDescription;
                else
                    desc = cache.mainCache.getCacheDetails(true).LongDescription;
            } else
                // not an addi-wpt
                desc = cache.getCacheDetails(true).LongDescription;
        }
        // HtmlDisplay does not show the <sup> tag correctly, so we need to replace with ^
        if (desc.indexOf("<sup>")>=0) {
        	desc=STRreplace.replace(desc,"<sup>","^(");
        	desc=STRreplace.replace(desc, "</sup>",")");
        }
        Vm.showWait(true);
        if (cache!=null && isHtml) {
            int imageNo = 0;
            CacheImages Images;
            CacheHolder chImages; // cache which supplies the images (could be main cache)
            if (cache.isAddiWpt()) {
                chImages=cache.mainCache;
            } else {
                chImages=cache;
            }
        	Images = chImages.getCacheDetails(true).images;
            StringBuffer s = new StringBuffer(desc.length() + Images.size() * 100);
            int start = 0;
            int pos;
            Regex imgRex = new Regex("src=(?:\\s*[^\"|']*?)(?:\"|')(.*?)(?:\"|')");
            while (start >= 0 && (pos = desc.indexOf("<img", start)) > 0) {
                s.append(desc.substring(start, pos));
                imgRex.searchFrom(desc, pos);
                String imgUrl = imgRex.stringMatched(1);
                if (imgUrl==null) break; // Remaining pictures are from image span
                if (imgUrl.lastIndexOf('.') > 0 && imgUrl.toLowerCase().startsWith("http")) {
                    String imgType = (imgUrl.substring(imgUrl.lastIndexOf('.')).toLowerCase() + "    ").substring(0, 4).trim();
                    // If we have an image which we stored when spidering, we can display it
                    if (Images.size()>0 && Global.getPref().descShowImg) {
    					if(imgType.startsWith(".png") || imgType.startsWith(".jpg") || imgType.startsWith(".gif")){
                            s.append("<img src=\"" +Images.get(imageNo).getFilename() + "\">");
                            imageNo++;
                        }
                    }
                    else {
                        // s.append(" \"" + imgUrl + "\" : " + MyLocale.getMsg(322,"") + " ");
                        s.append("<img src=\"" + "noImage.png" + "\"" + " alt=\"no image\"" + ">");
                    }
                }
                start = desc.indexOf(">", pos);
                if (start >= 0) start++;
                if (imageNo >= Images.size()) break;
            }
            if (start >= 0)
                s.append(desc.substring(start));
            desc = s.toString();
            start=Images.size();
            if (imageNo<Images.size() && Global.getPref().descShowImg) {
                desc += getPicDesc(imageNo, chImages.getCacheDetails(true));
            }

            //disp.setHtml(desc);
            disp.startHtml();
            disp.getDecoderProperties().set("documentroot", Global.getProfile().dataDir);
            disp.addHtml(desc, new ewe.sys.Handle());
            disp.endHtml();
        } else {
            disp.startHtml(); // To clear the old HTML display
            disp.endHtml();
            disp.setPlainText(desc);
        }
        disp.scrollTo(scrollto, false);
        //description = desc;
        Vm.showWait(false);
        //}
        currCache = cache;
    }
	
	/**
	 * Get the descriptions for the pictures (if they exist)
	 * @param imagesShown images already shown as part of long description (don't show again)
	 * @param chD
	 */
	private String getPicDesc(int imagesShown,CacheHolderDetail chD) {
		StringBuffer sb=new StringBuffer(1000);
		sb.append("<hr><font size=\"+1\" color=\"red\">").append(MyLocale.getMsg(202,"IMAGES").toUpperCase()).append("</font>");
		sb.append("<br><br>");
		for (int i=imagesShown; i<chD.images.size(); i++) {
			sb.append(chD.images.get(i).getTitle()).append("<br>");
			// Show the additional text if there is one
			if (!chD.images.get(i).getComment().equals("")) sb.append("<font color='blue'>").append(chD.images.get(i).getComment()).append("</font>");
			// Only show the image if images are enabled
			if (Global.getPref().descShowImg) sb.append("<img src=\""+chD.images.get(i).getFilename()+"\"><br>");
			sb.append("<br><br><hr>");
		}
		return sb.toString();
	}
	
	public void clear() {
		disp.setPlainText("loading ...");
		currCache = null;
	}
	
	/**
	 * Eventhandler
	 */
	public void onEvent(Event ev){
		
		if(ev instanceof ControlEvent && ev.type == ControlEvent.PRESSED){
			if (ev.target == btnPlus){
				Font currFont = disp.getFont();
				currFont = currFont.changeNameAndSize(null, currFont.getSize() + 2);
				disp.setFont(currFont);
				disp.displayPropertiesChanged();
				//redraw();
			}

			if (ev.target == btnMinus){
				Font currFont = disp.getFont();
				currFont = currFont.changeNameAndSize(null, currFont.getSize() - 2);
				disp.setFont(currFont);
				disp.displayPropertiesChanged();
				//redraw();
			}
			if (ev.target == btnText){
	            disp.startHtml(); // To clear the old HTML display
	            disp.endHtml();
	    		Transformer trans = new Transformer(true);
	    		trans.add(new Regex("\r", ""));
	    		trans.add(new Regex("\n", " "));
	    		trans.add(new Regex("<br>", "\n"));
	    		trans.add(new Regex("<p>", "\n"));
	    		trans.add(new Regex("<hr>", "\n"));
	    		trans.add(new Regex("<br />", "\n"));
	    		trans.add(new Regex("<(.*?)>", ""));
	    		Transformer ttrans=new Transformer(true);
	    		ttrans.add(new Regex("<(.*?)>", ""));
	    		disp.setPlainText(ttrans.replaceAll(trans.replaceAll(desc)));
			}
			if (ev.target == btnHtml){
	            disp.startHtml();
	            disp.getDecoderProperties().set("documentroot", Global.getProfile().dataDir);
	            disp.addHtml(desc, new ewe.sys.Handle());
	            disp.endHtml();
			}
		}
		super.onEvent(ev);
	}

}
