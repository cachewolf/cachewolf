package cachewolf;

import com.stevesoft.eve_pat.Regex;

import eve.ui.*;
import eve.ui.formatted.*;
import eve.fx.*;
import eve.sys.*;
import java.util.Vector;
import eve.ui.event.*;

/**
*	This class shows the long description on a cache.
*/
public class DescriptionPanel extends CellPanel{
	private HtmlDisplay disp;
	private Button btnPlus, btnMinus;
	private Panel buttonP;
	private String desc,newDesc;
	CacheHolderDetail currCache;
	private FormattedTextMaker tm;

	public DescriptionPanel(){
		ScrollBarPanel sbp = new MyScrollBarPanel(disp = new HtmlDisplay(), 0);
		this.addLast(sbp);
		buttonP = new Panel();
		buttonP.addNext(btnPlus = new Button("+"),CellConstants.HSTRETCH, CellConstants.HFILL);
		buttonP.addLast(btnMinus = new Button("-"),CellConstants.HSTRETCH, CellConstants.HFILL);
		buttonP.equalWidths=true;
		this.addLast(buttonP,CellConstants.HSTRETCH,CellConstants.HFILL);
		clear();
	}
	
	/**
	*	Set the text to display. Text should be HTML formated.
	*/
	//String description = null;
	//CacheHolderDetail chD;
	
	public void setText(CacheHolderDetail chD){
		boolean isHtml=chD.is_HTML;
		//if (currCache == cache) return;
		int scrollto = 0;
		if (chD.hasSameMainCache(currCache)) scrollto = disp.getTopLine();
		//Makes no sense if (chD == null) desc = "";
		else {
			if (chD.isAddiWpt()) {
				CacheHolderDetail mainCache=chD.mainCache.getCacheDetails(true);
				isHtml=mainCache.is_HTML;
				if (chD.longDescription != null && chD.longDescription.length() > 0)
					 desc = chD.longDescription + (isHtml?"<hr>\n":"\n")+mainCache.longDescription;
				else 
					desc = mainCache.longDescription;
			} else // not an addi-wpt
				desc = chD.longDescription; 
		}
		//if (!desc.equals(description)) {
			//disp.getDecoderProperties().setBoolean("allowImages",true);
			Form.showWait(); 
			if (isHtml) {
				int imageNo=0;
				if (Global.getPref().descShowImg) {
					Vector Images;
					if (chD.isAddiWpt()) {
						Images = chD.mainCache.getCacheDetails(true).images;
					} else {
						Images = chD.images;						
					}					
					StringBuffer s=new StringBuffer(desc.length()+Images.size()*100);
					int start=0;
					int pos;
					Regex imgRex = new Regex("src=(?:\\s*[^\"|']*?)(?:\"|')(.*?)(?:\"|')");
					if (Images.size() > 0) {
						while (start>=0 && (pos=desc.indexOf("<img",start))>0) {
							s.append(desc.substring(start,pos));
							imgRex.searchFrom(desc,pos);
							String imgUrl=imgRex.stringMatched(1);
							//Vm.debug("imgUrl "+imgUrl);
							if (imgUrl.lastIndexOf('.')>0 && imgUrl.toLowerCase().startsWith("http")) {
								String imgType = (imgUrl.substring(imgUrl.lastIndexOf(".")).toLowerCase()+"    ").substring(0,4).trim();
								// If we have an image which we stored when spidering, we can display it
								if(!imgType.startsWith(".com") && !imgType.startsWith(".php") && !imgType.startsWith(".exe") && !imgType.startsWith(".pl")){
									s.append("<img src=\""+
											//Global.getProfile().dataDir+
											Images.get(imageNo)+"\">");
									imageNo++;
								} // else s.append("<!-- not valid image type -->");
							} // else s.append("<!-- Not http -->");
							start=desc.indexOf(">",pos);
							if (start>=0) start++;
							if (imageNo >= Images.size())break;
						}
					}
					if (start>=0) s.append(desc.substring(start));
					newDesc=s.toString(); // Don't store in desc as this could modify chD
				} else {
					newDesc = desc;
				}
				if (chD.hasImageInfo()) {
					newDesc+=getPicDesc(imageNo,chD);
				}
				//disp.setHtml(desc);
				disp.startHtml();
				disp.getDecoderProperties().set("documentroot",Global.getProfile().dataDir);
				disp.addHtml(newDesc,new eve.sys.Handle());
				tm=disp.endHtml();
				
				
			}
			else {
				disp.startHtml(); // To clear the old HTML display
				disp.addHtml(desc,new eve.sys.Handle());
				tm=disp.endHtml();
				//disp.setPlainText(desc);
			}
			disp.scrollTo(scrollto,false);
			//description = desc;
			Form.cancelWait();
		//}
		currCache = chD;
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
		for (int i=imagesShown; i<chD.imagesInfo.size(); i++) {
			sb.append(chD.imagesText.get(i)).append("<br>");
			// Show the additional text if there is one
			if (chD.imagesInfo.get(i)!=null) sb.append("<font color='blue'>").append(chD.imagesInfo.get(i)).append("</font>");
			// Only show the image if images are enabled
			if (Global.getPref().descShowImg) sb.append("<img src=\""+chD.images.get(i)+"\"><br>");
			sb.append("<br><br><hr>");
		}
		return sb.toString();
	}
	
	
	
	public void clear() {
		disp.startHtml(); // To clear the old HTML display
		disp.addHtml("loading ...",new eve.sys.Handle());
		disp.endHtml();
		//disp.setPlainText("loading ...");
	}
	
	private void redraw() {
		int currLine;

		Form.showWait();
		currLine = disp.getTopLine();
		//if (currCache.is_HTML)	setText(desc);
		//else				disp.setPlainText(currCache.longDescription);
		disp.scrollTo(currLine,false);
		Form.cancelWait();
	}
	
	/**
	 * Eventhandler
	 */
	public void onEvent(Event ev){
		
		if (ev instanceof ControlEvent && ev.type==TextDisplay.LINES_SPLIT) { 
			ev.consumed=true; return; } // LINES_SPLIT event does not have to percolate up
		if(ev instanceof ControlEvent && ev.type == ControlEvent.PRESSED){
			if (ev.target == btnPlus){
				Font currFont = disp.getFont();
				currFont = currFont.changeNameAndSize(null, currFont.getSize() + 2);
				//tm.newFont=currFont;
				disp.setFont(currFont);
				disp.displayPropertiesChanged();
				//disp.repaintNow();
				//redraw();
			}

			if (ev.target == btnMinus){
				Font currFont = disp.getFont();
				currFont = currFont.changeNameAndSize(null, currFont.getSize() - 2);
				disp.setFont(currFont);
				disp.displayPropertiesChanged();
				//disp.repaintNow();
				//redraw();
			}
		}
		super.onEvent(ev);
	}

}
