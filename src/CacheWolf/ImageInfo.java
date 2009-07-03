package CacheWolf;

/**
 * This class is used to hold information about an image in a cache. It may be used for normal 
 * images, log images and user added images.
 * @author Engywuck
 *
 */
public class ImageInfo {
	
	private String filename="";
	private String title="";
	private String comment=null;

	/**
	 * Gets the filename of the image (without path)
	 * @return Filename
	 */
	public String getFilename() {
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
    	return comment;
    }
	/**
	 * Sets the comment of the image. If an empty string is passed, the comment will be set to <code>
	 * null</code>.
	 * @param comment Comment
	 */
	public void setComment(String comment) {
		if (comment != null && comment.equals("")) {
			this.comment = null;
		} else {
			this.comment = comment;
		}
    }
	
}
