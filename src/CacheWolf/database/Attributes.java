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
package CacheWolf.database;

import CacheWolf.utils.Extractor;

public class Attributes {
    public final static int MAXATTRIBS = 15;
    /**
     * The number of attributes for this cache (=number of array elements in use in attribs)
     */
    private int _count = 0;
    /**
     * The attributes as array of MAXATTRIBS Attribute
     */
    private Attribute attribs[] = new Attribute[MAXATTRIBS];
    /**
     * The attributes presence in Bit Representation
     */
    private long[] attrYes = {0l, 0l};
    private long[] attrNo = {0l, 0l};

    public Attributes() {
    } // Just a public constructor

    /**
     * Get the number of attributes in the list
     *
     * @return number of attributes
     */
    public int count() {
        return _count;
    }

    /**
     * getting an empty attributes list for this Cache
     */
    public void clear() {
        _count = 0;
        attrYes[0] = 0l;
        attrYes[1] = 0l;
        attrNo[0] = 0l;
        attrNo[1] = 0l;
    }

    /**
     * Filling the Attributes from Cache.xml (CacheHolderDetail) todo : remove this "historic" reprensentation , saving as Bits should be enough (araber95)
     */
    public void XmlAttributesEnd(String elem) {
        clear();
        Extractor ex = new Extractor(elem, "<ATT>", "</ATT>", 0, true);
        String dummy;
        while ((dummy = ex.findNext()).length() > 0) {
            add(dummy);
        }
    }

    /**
     * Prepare for attributes to be written to cache.xml file todo : remove this "historic" reprensentation , saving as Bits should be enough (araber95)
     */
    public String XmlAttributesWrite() {
        StringBuffer sb = new StringBuffer(1000);
        sb.append("<ATTRIBUTES>\n");
        for (int i = 0; i < _count; i++) {
            sb.append("   <ATT>");
            sb.append(attribs[i].getImageName());
            sb.append("</ATT>\n");
        }
        sb.append("</ATTRIBUTES>\n");
        return sb.toString();
    }

    /**
     * Add a new attribute to the array by Name, meaning its icon picture name
     *
     * @param attributeName
     */
    public void add(String attributeName) {
        if (_count < attribs.length) {
            if (!attributeName.startsWith("attribute-blank")) {
                Attribute attr = new Attribute(attributeName);
                attribs[_count++] = attr;
                attrYes = attr.getYesBit(attrYes);
                attrNo = attr.getNoBit(attrNo);
            }
        }
    }

    /**
     * Add a new attribute to the array by ID and Inc as you get it from GC gpx-File
     *
     * @param attIdGC GC attribute ID
     * @param Yes1No0 GC attribute Inc (attribute set = 0 ,attribute not set = 1)
     */
    public void add(int attIdGC, String Yes1No0) {
        if (_count < attribs.length) {
            Attribute attr = new Attribute(attIdGC, Yes1No0);
            boolean doAdd = true;
            int replace = 0;
            for (int i = 0; i < _count; i++) {
                if (attribs[i].getGCId().equals(attr.getGCId())) {
                    doAdd = false;
                    replace = i;
                    break;
                }
            }
            if (doAdd) {
                attribs[_count++] = attr;
            } else {
                // free old attr ?
                attribs[replace] = attr;
            }
            attrYes = attr.getYesBit(attrYes);
            attrNo = attr.getNoBit(attrNo);
        }
    }

    /**
     * Add a new attribute to the array by OC-IDas you get it from OC xml/zip-download
     *
     * @param OC attribute ID
     */
    public void addByOcId(String attIdOC) {
        Attribute attr = Attribute.FromOcId(attIdOC);
        for (int i = 0; i < _count; i++) {
            if (attribs[i].getId() == (attr.getId())) {
                return;
            }
        }
        if (_count < attribs.length) {
            attribs[_count++] = attr;
            attrYes = attr.getYesBit(attrYes);
        }
    }

    /**
     * to get the Bit Representation of the Attributs of this Cache
     *
     * @return the Long array representing the Attributes of this Cache
     */
    public long[] getAttribsAsBits() {
        long ret[] = new long[4];
        ret[0] = attrYes[0];
        ret[1] = attrYes[1];
        ret[2] = attrNo[0];
        ret[3] = attrNo[1];
        return ret;
    }

    /**
     * Get the i-th attribute
     *
     * @param i The number for which the attribute is to be retrieved
     * @return The attribute
     */
    public Attribute getAttribute(int i) {
        return attribs[i];
    }
}
