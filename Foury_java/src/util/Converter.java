package util;

import java.awt.image.BufferedImage;
import static util.ArrayPool.*;

public class Converter {

	public static double[] arrayFromBufferedImage(BufferedImage img){
		double[] array = alloc(img.getWidth()*img.getHeight());
		int width = img.getWidth();
		int height = img.getHeight();
		for(int y = 0; y < height; y++)
			for(int x = 0; x < width; x++)
				array[y*width+x] = luminance(img.getRGB(x,y));
		return array;
	}
	
	public static BufferedImage imageFromArray(double[] array, int width, int imageFormat){
		int height = array.length/width;
		BufferedImage img = new BufferedImage(width, height, imageFormat);
		for(int y = 0; y < height; y++)
			for(int x = 0; x < width; x++)
				img.setRGB(x, y, rgbFromDouble(array[y*width+x]));
		return img;
	}
	
	private static double luminance(int rgb){
		return ((rgb & 0xff) + ((rgb >> 8)& 0xff)*6 + ((rgb >> 16)&0xff)*3)/(2550.0);
	}
	
	private static int rgbFromDouble(double x){
		int value = (int) (Math.min(1, Math.max(0, x))*255);
		return value | (value << 8) | (value << 16);
	}
	
}
