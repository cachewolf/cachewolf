
package eve.ui.formatted;
import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;

import eve.data.PropertyList;
import eve.fx.Dimension;
import eve.fx.IImage;
import eve.fx.ImageDecoder;
import eve.fx.PixelBuffer;
import eve.io.File;
import eve.io.StreamUtils;
import eve.sys.Handle;
import eve.sys.Task;
import eve.sys.TimeOut;
import eve.util.ByteArray;
import eve.util.FormattedDataSource;
import eve.util.Tag;

//##################################################################
public class StreamImageResolver implements ImageResolver{
//##################################################################

//-------------------------------------------------------------------
protected Tag getStreamFor(String imageName) throws IOException
//-------------------------------------------------------------------
{
	File f = File.getNewFile(imageName);
	if (!f.canRead()) throw new IOException();
	Tag t = new Tag();
	t.value = f.toReadableStream();
	t.tag = (int)f.getLength();
	return t;
}

private static ByteArray scaleArray;
private Hashtable imageCache = new Hashtable();
private static int numResolving;

//===================================================================
public Handle resolveImage(final String name,final boolean allowAnimatedImages,final Dimension maxSize)
//===================================================================
{
	return new Task(){
		protected void doRun(){
			if (scaleArray == null) scaleArray = new ByteArray();
			Handle handle = this;
			try{
				Tag tag = getStreamFor(name);
				Object in = tag.value;
				InputStream is = null;
				if (in instanceof InputStream) is = (InputStream)in;
				if (is == null) throw new IOException("Could not get Stream to image.");
				int length = tag.tag;
				if (length == 0){
					is.close();
					throw new IOException();
				}
				try{
					IImage got = null;
					handle.doing = "Reading bytes...";
					ByteArray ba = StreamUtils.readAllBytes(handle,is,null,length,0);
					if (ba == null) {
						handle.set(handle.Aborted|handle.Stopped);
						return;
					}
					handle.doing = "Decoding image...";
					handle.setProgress(0);
					synchronized(StreamImageResolver.class){
						numResolving++;
						//Vm.debug("Start: "+numResolving+" = "+name+": "+ba.length+" = "+ba.getChecksum());
					}
					try{
						Dimension m = maxSize;
						//m = new Dimension(50,50); //Just to test scaling.
						FormattedDataSource fds = new FormattedDataSource().set(ba);
						got = ImageDecoder.decodeScaledPicture(fds, m, true, true, null);
					}finally{
						synchronized(StreamImageResolver.class){
							numResolving--;
							//Vm.debug("Stop: "+numResolving+" = "+name);
						}
					}
					handle.returnValue = got;
					handle.set(Handle.Succeeded);
				}catch(Exception e){
					//e.printStackTrace();
					handle.fail(e);					
				}
			}catch(IOException e){
				//.printStackTrace();
				handle.fail(e);
			}
		}
	}.start();
}
//===================================================================
public Handle resolveImage(final PropertyList imageProperties,final boolean allowAnimatedImages,Dimension maxImageSize)
//===================================================================
{
	final Dimension maxSize = maxImageSize; //new Dimension(100,100);
	final String src = imageProperties.getString("src",null);
	if (src == null) return new Handle(Handle.Failed,null);
	return new Task(){
		protected void doRun(){
			Handle handle = this;
			Handle res = (Handle)imageCache.get(src);
			if (res == null) {
				res = resolveImage(src,allowAnimatedImages,maxSize);
				imageCache.put(src,res);
			}
			if (!waitOn(res,res.Success,TimeOut.Forever,true)){
				handle.set(Handle.Failed);
			}else{
				IImage got = (IImage)res.returnValue;
				//
				if (maxSize != null){// && !(got instanceof AniImage)){
					if (got.getWidth() > maxSize.width || got.getHeight() > maxSize.height){
						//System.out.println("Scaling: "+maxSize);
						got = new PixelBuffer(got).scale(maxSize.width,maxSize.height,null,PixelBuffer.SCALE_KEEP_ASPECT_RATIO,scaleArray).toPicture();
					}//else System.out.println("Not Scaling: "+maxSize);
				}
				//
				PropertyList pl = PropertyList.toPropertyList(imageProperties);
				int ww = pl.getInt("width",got.getWidth());
				int hh = pl.getInt("height",got.getHeight());
				if (ww <= 0) ww = got.getWidth();
				if (hh <= 0) hh = got.getHeight();
				if (maxSize != null){
					if (ww > maxSize.width) ww = maxSize.width;
					if (hh > maxSize.height) hh = maxSize.height;
				}
				if (ww != got.getWidth() && hh != got.getHeight())
					got = new PixelBuffer(got).scale(ww,hh,null,0,scaleArray).toPicture();
				handle.returnValue = got;
				handle.set(Handle.Succeeded);
			}
		}
	}.start();
}



//##################################################################
}
//##################################################################

