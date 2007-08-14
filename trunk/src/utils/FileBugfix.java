/**
 * 
 */
package utils;

import ewe.io.*;

/**
 * @author pfeffer
 * class to fix a bug in ewe.io.File, which occurs only on some systems (e.g. linux): the mask "*.xyz" doesn't work
 * so I get all the files wich null in spite of the mask and filter afterwords 
 */
public class FileBugfix extends File{
	public FileBugfix(String path) 
		{super(path);}
	
	public String[] list(final String mask,final int listAndSortOptions)
	{
		if ((listAndSortOptions & FileBase.LIST_FILES_ONLY) > 0)
		  return super.listMultiple(mask+",*.xyzq3246wwee345rrtzih6ljbkoih", listAndSortOptions); // with only one "*" listmultiple calls list
		else return super.list(mask, listAndSortOptions);
		/*
		 * super.list has on some systems (linux, but also Windows Mobile 2003) a bug 
		 * in ewe 1.49: 
		 * when filenames contain more than one ".", 
		 * then the mask matches on these systems the first in spite of the last "."
		 * listmultiple doesn't have this bug 
		 */
	}

}
