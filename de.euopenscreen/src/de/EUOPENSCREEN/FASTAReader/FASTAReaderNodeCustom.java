package de.EUOPENSCREEN.FASTAReader;

import java.net.MalformedURLException;
import java.net.URL;

public class FASTAReaderNodeCustom {

	public static boolean isLocalFile(String file) {
	    try {
	        new URL(file);
	        return false;
	        }
	    
	    catch (MalformedURLException e) {
	        return true;
	        }
	}
}