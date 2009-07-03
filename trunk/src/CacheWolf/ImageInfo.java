package CacheWolf;

public class ImageInfo {
	
	private String name="";
	private String text="";
	private String comment=null;

	public String getName() {
    	return name;
    }
	public void setName(String name) {
    	this.name = name;
    }
	public String getText() {
    	return text;
    }
	public void setText(String text) {
    	this.text = text;
    }
	public String getComment() {
    	return comment;
    }
	public void setComment(String comment) {
		if (comment != null && comment.equals("")) {
			this.comment = null;
		} else {
			this.comment = comment;
		}
    }
	
	
	
}
