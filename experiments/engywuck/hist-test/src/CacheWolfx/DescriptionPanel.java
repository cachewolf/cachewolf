package CacheWolf;

import com.stevesoft.ewe_pat.Regex;

import ewe.ui.*;
import ewe.fx.*;
import ewe.sys.*;
import ewe.util.Vector;

/**
*	This class shows the long description on a cache.
*  Test with GC1CC5T - Final
*            GC19DDX - 
*/
public class DescriptionPanel extends CellPanel{
	HtmlDisplay disp = new HtmlDisplay();
	mButton btnPlus, btnMinus;
	CacheHolderDetail currCache;
	
	CellPanel buttonP = new CellPanel();
	CellPanel descP = new CellPanel();
	
	private String desc;

	public DescriptionPanel(){
		buttonP.addNext(btnPlus = new mButton("+"),CellConstants.HSTRETCH, (CellConstants.HFILL));
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
    public void setText(CacheHolderDetail cache) {
        boolean isHtml;
        if (currCache == cache) return;
        int scrollto = 0;
        if (cache == null) {
            desc = "";
            isHtml = false;
        } else {
            if (cache.hasSameMainCache(currCache))
                scrollto = disp.getTopLine();
            isHtml = cache.is_HTML;
            if (cache.isAddiWpt()) {
                CacheHolderDetail mainCache = cache.mainCache.getCacheDetails(true);
                isHtml = mainCache.is_HTML;
                if (cache.LongDescription != null && cache.LongDescription.length() > 0)
                    desc = cache.LongDescription + (isHtml ? "<hr>\n" : "\n")
                            + mainCache.LongDescription;
                else
                    desc = mainCache.LongDescription;
            } else
                // not an addi-wpt
                desc = cache.LongDescription;
        }
        // HtmlDisplay does not show the <sup> tag correctly, so we need to replace with ^
        if (desc.indexOf("<sup>")>=0) {
        	desc=STRreplace.replace(desc,"<sup>","^(");
        	desc=STRreplace.replace(desc, "</sup>",")");
        }
        Vm.showWait(true);
        if (isHtml) {
            int imageNo = 0;
            if (Global.getPref().descShowImg) {
                Vector Images;
                CacheHolderDetail chDimages; // cache which supplies the images (could be main cache)
                if (cache.isAddiWpt()) {
                    chDimages=cache.mainCache.getCacheDetails(true);
                } else {
                    chDimages=cache;
                }
            	Images = chDimages.Images;
                StringBuffer s = new StringBuffer(desc.length() + Images.size() * 100);
                int start = 0;
                int pos;
                Regex imgRex = new Regex("src=(?:\\s*[^\"|']*?)(?:\"|')(.*?)(?:\"|')");
                if (Images.getCount() > 0) {
                    while (start >= 0 && (pos = desc.indexOf("<img", start)) > 0) {
                        s.append(desc.substring(start, pos));
                        imgRex.searchFrom(desc, pos);
                        String imgUrl = imgRex.stringMatched(1);
                        if (imgUrl==null) break; // Remaining pictures are from image span
                        // Vm.debug("imgUrl "+imgUrl);
                        if (imgUrl.lastIndexOf('.') > 0 && imgUrl.toLowerCase().startsWith("http")) {
                            String imgType = (imgUrl.substring(imgUrl.lastIndexOf("."))
                                    .toLowerCase() + "    ").substring(0, 4).trim();
                            // If we have an image which we stored when spidering, we can display it
        					if(imgType.startsWith(".png") || imgType.startsWith(".jpg") || imgType.startsWith(".gif")){
                                s.append("<img src=\"" +
                                // Global.getProfile().dataDir+
                                        Images.get(imageNo) + "\">");
                                imageNo++;
                            }
                        }
                        start = desc.indexOf(">", pos);
                        if (start >= 0)
                            start++;
                        if (imageNo >= Images.getCount())
                            break;
                    }
                }
                if (start >= 0)
                    s.append(desc.substring(start));
                desc = s.toString();
                if (imageNo<Images.getCount()) {
                    desc += getPicDesc(imageNo, chDimages);
                }
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
		for (int i=imagesShown; i<chD.ImagesInfo.size(); i++) {
			sb.append(chD.ImagesText.get(i)).append("<br>");
			// Show the additional text if there is one
			if (chD.ImagesInfo.get(i)!=null) sb.append("<font color='blue'>").append(chD.ImagesInfo.get(i)).append("</font>");
			// Only show the image if images are enabled
			if (Global.getPref().descShowImg) sb.append("<img src=\""+chD.Images.get(i)+"\"><br>");
			sb.append("<br><br><hr>");
		}
		return sb.toString();
	}
	
	public void clear() {
		disp.setPlainText("loading ...");
		currCache = null;
	}
	
	// Probably not needed anymore (Change in Rev. 1395)
	private void redraw() {
		int currLine;

		Vm.showWait(true);
		currLine = disp.getTopLine();
		if (currCache.is_HTML)	disp.setHtml(desc);
		else				disp.setPlainText(currCache.LongDescription);
		disp.scrollTo(currLine,false);
		Vm.showWait(false);
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
		}
		super.onEvent(ev);
	}

}
