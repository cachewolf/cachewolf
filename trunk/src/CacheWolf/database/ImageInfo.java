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

/**
 * This class is used to hold information about an image in a cache. It may be used for normal 
 * images, log images and user added images.
 * @author Engywuck
 *
 */
public class ImageInfo {

    private String filename = null;
    private String title = null;
    private String comment = null;
    private String URL;

    /**
     * Gets the filename of the image (without path)
     * @return Filename
     */
    public String getFilename() {
	if (filename == null)
	    return "";
	return filename;
    }

    /**
     * Sets the filename
     * @param filename Well...
     */
    public void setFilename(String filename) {
	this.filename = filename;
    }

    /**
     * Gets the title of the image. 
     * @return Title
     */
    public String getTitle() {
	if (title == null)
	    return "";
	return title;
    }

    /**
     * Sets the image title.
     * @param text Image title
     */
    public void setTitle(String text) {
	this.title = text;
    }

    /**
     * Gets an additional comment for the image, if there is any. If there is none, then <code>
     * null</code> will be returned.
     * @return Comment
     */
    public String getComment() {
	if (comment == null)
	    return "";
	return comment;
    }

    /**
     * Sets the comment of the image.
     * @param comment Comment
     */
    public void setComment(String comment) {
	this.comment = comment;
    }

    public String getURL() {
	if (URL == null)
	    return "";
	return URL;
    }

    public void setURL(String url) {
	URL = url;
    }

}
