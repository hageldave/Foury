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
//		String inputName1 = "res/crossy1.png";
//		String inputName2 = "res/crossy2.png";
//		String inputName1 = "res/brushes1.png";
//		String inputName2 = "res/brushes2.png";
//		String inputName1 = "res/h1.png";
//		String inputName2 = "res/h2.png";
		String inputName1 = "res/musquare1.png";
		String inputName2 = "res/musquare2.png";
		
		Pair<double[], Integer> real_ = loadDArrayPow2FromImgFile(inputName1);
		double[] real1 = real_.val1;
		Pair<double[], Integer> real2_ = loadDArrayPow2FromImgFile(inputName2);
		double[] real2 = real2_.val1;
		int size = real_.val2;
		
		displayNorm(real1, size, inputName1);
		displayNorm(real2, size, inputName2);
		
		long time = System.currentTimeMillis();
		double rot = RotationDetection.calculateRotation(real1, real2, size);
		free(real1);
		free(real2);
		
		// reload images
		real1 = loadDArrayPow2FromImgFile(inputName1).val1;
		real2 = loadDArrayPow2FromImgFile(inputName2).val1;
		// padd images for rotation
		int targetSize = size*2;
		real1 = paddArray2D(real1, size, targetSize, 0);
		real2 = paddArray2D(real2, size, targetSize, 0);
		// shift by padding
		shift2D(real1, size/2, size/2, targetSize, real1);
		shift2D(real2, size/2, size/2, targetSize, real2);
		// rotate
		rotate(real2, real2, targetSize, -rot, targetSize/2, targetSize/2);
		display(real1, targetSize, "original");
		display(real2, targetSize, "back rotation");
		double[] realCpy = arrayCopy(real1);
		double[] real2Cpy = arrayCopy(real2);
		Point t = TranslationDetection.calculateTranslation(real1, real2, targetSize);
		// back translate
		shift2D(real2Cpy, targetSize-t.x, targetSize-t.y, targetSize, real2Cpy);
		display(real2Cpy, targetSize, "back translate");
		int xt = Math.abs(t.x) < Math.abs(t.x-targetSize) ? t.x:t.x-targetSize;
		int yt = Math.abs(t.y) < Math.abs(t.y-targetSize) ? t.y:t.y-targetSize;
		System.out.format("translation of (%d|%d), [%.3fs]%n", xt, yt, (System.currentTimeMillis()-time)/1000.0);
		
		// difference
		difference(realCpy, real2Cpy, realCpy);
		display(realCpy, targetSize, "difference");
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
