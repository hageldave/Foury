package util;

import java.awt.Image;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class ImageLoader {
	
	public static String[] getLoadableImageFormats(){
		return ImageIO.getReaderFileSuffixes();
	}
	
	public static Image loadImage(String fileName){
		File f = new File(fileName);
		if(f.exists()){
			return loadImage(f);
		}
		return null;
	}
	
	public static Image loadImage(File file){
		try {
			return ImageIO.read(file);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

}
