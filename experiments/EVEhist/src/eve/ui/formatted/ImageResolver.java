package eve.ui.formatted;
import eve.data.PropertyList;
import eve.fx.Dimension;
import eve.sys.Handle;

//##################################################################
public interface ImageResolver{
//##################################################################
/**
* Resolve the image.
* @param imageProperties The properties for the Image. This should contain at least one property called "src".
* @param allowAnimatedImages If this is true then animated images will be resolved into
* an AnimatedIcon.
* @param maxSize An optional limit to the size of the image returned.
* @return A Handle that can be used to monitor the progress of the resolution. When the Handle
* status reports Handle.Succeeded then the returnValue of the Handle will hold the resolved
* image.
*/
public Handle resolveImage(PropertyList imageProperties, boolean allowAnimatedImages, Dimension maxSize);

//##################################################################
}
//##################################################################


