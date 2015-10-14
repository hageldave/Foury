package analysis;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.Arrays;

import util.ArrayOps;
import util.BufferedImageFactory;
import util.Converter;
import util.ImageLoader;
import util.Pair;

import static util.ArrayOps.*;
import static util.ArrayPool.*;
import static util.ImageOps.*;

public class RotationDetectionTest {
	static final int imgType = BufferedImage.TYPE_BYTE_GRAY;
	
	public static void main(String[] args) {
		String inputName = "res/h1.png";
		String inputName2 = "res/h2.png";
		
		Pair<double[], Integer> real_ = loadDArrayPow2FromImgFile(inputName);
		double[] real = real_.val1;
		Pair<double[], Integer> real2_ = loadDArrayPow2FromImgFile(inputName2);
		double[] real2 = real2_.val1;
		int size = real_.val2;
		
		displayNorm(real, size, inputName);
		displayNorm(real2, size, inputName2);
		
		long time = System.currentTimeMillis();
		double rot = RotationDetection.calculateRotation(real, real2, size);
		free(real);
		free(real2);
		
		// reload images
		real = loadDArrayPow2FromImgFile(inputName).val1;
		real2 = loadDArrayPow2FromImgFile(inputName2).val1;
		// padd images for rotation
		int targetSize = size*2;
		real = paddArray2D(real, size, targetSize, 0);
		real2 = paddArray2D(real2, size, targetSize, 0);
		// shift by padding
		shift2D(real, size/2, size/2, targetSize, real);
		shift2D(real2, size/2, size/2, targetSize, real2);
		// rotate
		rotate(real2, real2, targetSize, -rot, targetSize/2, targetSize/2);
		display(real, targetSize, "original");
		display(real2, targetSize, "back rotation");
		double[] real2Cpy = arrayCopy(real2);
		Point p = TranslationDetection.calculateTranslation(real, real2, targetSize);
		// back translate
		shift2D(real2Cpy, targetSize-p.x, targetSize-p.y, targetSize, real2Cpy);
		display(real2Cpy, targetSize, "back translate");

		System.out.format("translation of (%d|%d), [%.3fs]%n",p.x, p.y, (System.currentTimeMillis()-time)/1000.0);
	}
	
	static BufferedImage imageOfFourier(double[] array, int size){
		array = Arrays.copyOf(array, array.length);
		ArrayOps.logarithmize(array, array);
		ArrayOps.normalize(array, array);
		util.ArrayOps.shift2D(array, size/2, size/2, size, array);
		return Converter.imageFromArray(array, size, BufferedImage.TYPE_BYTE_GRAY);
	}
	
	static Pair<double[], Integer> loadDArrayPow2FromImgFile(String filename){
		BufferedImage input = BufferedImageFactory.get(ImageLoader.loadImage(filename), BufferedImage.TYPE_INT_RGB);
		int w = input.getWidth();
		int h = input.getHeight();
		int size =  ArrayOps.getNextPowerOf2(Math.max(w, h));
		double[] array = ArrayOps.paddArray2D(Converter.arrayFromBufferedImage(input), w, size, 0);
		return new Pair<double[], Integer>(array, size);
	}
	
}
