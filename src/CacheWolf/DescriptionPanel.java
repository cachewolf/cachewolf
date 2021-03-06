/*
GNU General Public License
CacheWolf is a software for PocketPC, Win and Linux that
enables paperless caching.
It supports the sites geocaching.com and opencaching.de

Copyright (C) 2006  CacheWolf development team
See http://www.cachewolf.de/ for more information.
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

import CacheWolf.controls.GuiImageBroker;
import CacheWolf.controls.MyScrollBarPanel;
import CacheWolf.database.CacheHolder;
import CacheWolf.database.CacheImages;
import CacheWolf.utils.MyLocale;
import CacheWolf.utils.STRreplace;
import com.stevesoft.ewe_pat.Regex;
import com.stevesoft.ewe_pat.Transformer;
import ewe.fx.Font;
import ewe.sys.Vm;
import ewe.ui.*;

/**
 * This class shows the long description on a cache.
 * Test with GC1CC5T - Final
 * GC19DDX -
 */
public class DescriptionPanel extends CellPanel {
    private HtmlDisplay disp = new HtmlDisplay();
    private mButton btnPlus, btnMinus, btnText, btnHtml;
    private CacheHolder currCache;

    private String desc;

    public DescriptionPanel() {
        final CellPanel buttonPanel = new CellPanel();
        buttonPanel.equalWidths = true;
        buttonPanel.addNext(btnPlus = GuiImageBroker.getButton("+", "plus"), HSTRETCH, HFILL);
        buttonPanel.addNext(btnText = GuiImageBroker.getButton("Text", "text"), HSTRETCH, HFILL);
        buttonPanel.addNext(btnHtml = GuiImageBroker.getButton("Html", "html"), HSTRETCH, HFILL);
        buttonPanel.addLast(btnMinus = GuiImageBroker.getButton("-", "minus"), HSTRETCH, HFILL);
        MyScrollBarPanel sbp = new MyScrollBarPanel(disp, 0);
        final CellPanel descP = new CellPanel();
        descP.addLast(sbp);

        if (Preferences.itself().tabsAtTop) {
            if (Preferences.itself().menuAtTab)
                this.addLast(buttonPanel, HSTRETCH, HFILL);
        } else {
            if (!Preferences.itself().menuAtTab)
                this.addLast(buttonPanel, HSTRETCH, HFILL);
        }

        this.addLast(descP);

        if (Preferences.itself().tabsAtTop) {
            if (!Preferences.itself().menuAtTab)
                this.addLast(buttonPanel, HSTRETCH, HFILL);
        } else {
            if (Preferences.itself().menuAtTab)
                this.addLast(buttonPanel, HSTRETCH, HFILL);
        }

        clear();
    }

    /**
     * Set the text to display. Text should be HTML formated.
     */
    public void setText(CacheHolder cache) {
        boolean isHtml;
        if (currCache == cache)
            return;
        int scrollto = 0;
        if (cache == null) {
            desc = "";
            isHtml = false;
            return;
        } else {
            if (cache.hasSameMainCache(currCache))
                scrollto = disp.getTopLine();
            isHtml = cache.isHTML();
            if (cache.isAddiWpt()) {
                isHtml = cache.mainCache.isHTML();
                if (cache.getDetails().getLongDescription() != null && cache.getDetails().getLongDescription().length() > 0)
                    desc = cache.getDetails().getLongDescription() + (isHtml ? "<hr>\n" : "\n") + cache.mainCache.getDetails().getLongDescription();
                else
                    desc = cache.mainCache.getDetails().getLongDescription();
            } else
                // not an addi-wpt
                desc = cache.getDetails().getLongDescription();
        }
        // HtmlDisplay does not show the <sup> tag correctly, so we need to replace with ^
        if (desc.indexOf("<sup>") >= 0) {
            desc = STRreplace.replace(desc, "<sup>", "^(");
            desc = STRreplace.replace(desc, "</sup>", ")");
        }
        // HtmlDisplay interprets &something; as entity, showing "?", so replacing & with +
        // real entities are uncorrectly shown as "?" (except "&amp";),
        // so they are replaced at import(spider,..) with the IsoChar of the entity (SafeXML.java cleanback)
        StringBuffer buf = new StringBuffer(desc);
        boolean checkit = true;
        int beg = 0;
        while (checkit) {
            int p1 = desc.indexOf("&", beg);
            int p2 = desc.indexOf(";", p1 + 1);
            if (p1 == -1 || p2 == -1)
                checkit = false;
            else {
                beg = p2 + 1;
                if (beg >= desc.length())
                    checkit = false;
            }
            if (checkit && p2 - p1 > 7) {
                buf.setCharAt(p1, '+');
            }
        }
        desc = buf.toString();

        Vm.showWait(this, true);
        if (isHtml) {
            int imageNo = 0;
            CacheImages Images;
            // cache which supplies the images (could be main cache)
            if (cache.isAddiWpt()) {
                Images = cache.mainCache.getDetails().getImages();
            } else {
                Images = cache.getDetails().getImages();
            }
            StringBuffer s = new StringBuffer(desc.length() + Images.size() * 100);
            int start = 0;
            int pos;
            Regex imgRex = new Regex("src=(?:\\s*[^\"|']*?)(?:\"|')(.*?)(?:\"|')");
            while (start >= 0 && (pos = desc.indexOf("<img", start)) > 0) {
                s.append(desc.substring(start, pos));
                imgRex.searchFrom(desc, pos);
                String imgUrl = imgRex.stringMatched(1);
                if (imgUrl == null)
                    break; // Remaining pictures are from image span
                if (imgUrl.lastIndexOf('.') > 0 && imgUrl.toLowerCase().startsWith("http")) {
                    String imgType = (imgUrl.substring(imgUrl.lastIndexOf('.')).toLowerCase() + "    ").substring(0, 4).trim();
                    // If we have an image which we stored when spidering, we can display it
                    if (Images.size() > 0 && Preferences.itself().descShowImg) {
                        if (imgType.startsWith(".png") || imgType.startsWith(".jpg") || imgType.startsWith(".gif")) {
                            s.append("<img src=\"" + Images.get(imageNo).getFilename() + "\">");
                            imageNo++;
                        }
                    } else {
                        s.append("<img src=\"" + "noImage.png" + "\"" + " alt=\"no image\"" + ">");
                    }
                }
                start = desc.indexOf(">", pos);
                if (start >= 0)
                    start++;
                if (imageNo >= Images.size())
                    break;
            }
            if (start >= 0)
                s.append(desc.substring(start));
            desc = s.toString();
            start = Images.size();
            if (imageNo < Images.size() && Preferences.itself().descShowImg) {
                desc += getPicDesc(imageNo, Images);
            }

            disp.startHtml();
            disp.getDecoderProperties().set("documentroot", MainForm.profile.dataDir);
            disp.addHtml(desc, new ewe.sys.Handle());
            disp.endHtml();
        } else {
            disp.startHtml(); // To clear the old HTML display
            disp.endHtml();
            disp.setPlainText(desc);
        }
        disp.scrollTo(scrollto, false);
        currCache = cache;
        Vm.showWait(this, false);
    }

    /**
     * Get the descriptions for the pictures (if they exist)
     *
     * @param imagesShown images already shown as part of long description (don't show again)
     * @param ci images
     */
    private String getPicDesc(int imagesShown, CacheImages ci) {
        StringBuffer sb = new StringBuffer(1000);
        sb.append("<hr><font size=\"+1\" color=\"red\">").append(MyLocale.getMsg(202, "IMAGES").toUpperCase()).append("</font>");
        sb.append("<br><br>");
        for (int i = imagesShown; i < ci.size(); i++) {
            sb.append(ci.get(i).getTitle()).append("<br>");
            // Show the additional text if there is one
            if (!ci.get(i).getComment().equals(""))
                sb.append("<font color='blue'>").append(ci.get(i).getComment()).append("</font>");
            // Only show the image if images are enabled
            if (Preferences.itself().descShowImg)
                sb.append("<img src=\"" + ci.get(i).getFilename() + "\"><br>");
            sb.append("<br><br><hr>");
        }
        return sb.toString();
    }

    public void clear() {
        disp.setPlainText("loading ...");
        currCache = null;
    }

    // Overrrides

    /**
     * Eventhandler
     */
    public void onEvent(Event ev) {

        if (ev instanceof ControlEvent && ev.type == ControlEvent.PRESSED) {
            if (ev.target == btnPlus) {
                Font currFont = disp.getFont();
                currFont = currFont.changeNameAndSize(null, currFont.getSize() + 2);
                disp.setFont(currFont);
                disp.displayPropertiesChanged();
                //redraw();
            }

            if (ev.target == btnMinus) {
                Font currFont = disp.getFont();
                currFont = currFont.changeNameAndSize(null, currFont.getSize() - 2);
                disp.setFont(currFont);
                disp.displayPropertiesChanged();
                //redraw();
            }
            if (ev.target == btnText) {
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
                Transformer ttrans = new Transformer(true);
                ttrans.add(new Regex("<(.*?)>", ""));
                disp.setPlainText(ttrans.replaceAll(trans.replaceAll(desc)));
            }
            if (ev.target == btnHtml) {
                disp.startHtml();
                disp.getDecoderProperties().set("documentroot", MainForm.profile.dataDir);
                disp.addHtml(desc, new ewe.sys.Handle());
                disp.endHtml();
            }
        }
        super.onEvent(ev);
    }

}
