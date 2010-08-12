package CacheWolf;

/**
 * This class is used to hold information about an image in a cache. It may be used for normal 
 * images, log images and user added images.
 * @author Engywuck
 *
 */
public class ImageInfo {
	
	private String filename=null;
	private String title=null;
	private String comment=null;
	private String URL;

	/**
	 * Gets the filename of the image (without path)
	 * @return Filename
	 */
	public String getFilename() {
		if (filename == null) return "";
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
		if (title == null) return "";
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
		if (comment == null) return "";
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
		if (URL == null) return "";
    	return URL;
    }
	
	public void setURL(String url) {
    	URL = url;
    }
	
}
