package analysis;

import java.awt.image.BufferedImage;
import java.util.Arrays;

import util.ArrayOps;
import util.BufferedImageFactory;
import util.Converter;
import util.ImageFrame;
import util.ImageLoader;
import util.Pair;

import static util.ArrayOps.*;

public class RotationDetectionTest {
	static final int imgType = BufferedImage.TYPE_BYTE_GRAY;
	
	public static void main(String[] args) {
		String inputName = "res/h1.png";
		String inputName2 = "res/h2.png";
		
		Pair<double[], Integer> real_ = loadDArrayPow2FromImgFile(inputName);
		double[] real = real_.val1;
		double[] imag = new double[real.length];
		Pair<double[], Integer> real2_ = loadDArrayPow2FromImgFile(inputName2);
		double[] real2 = real2_.val1;
		double[] imag2 = new double[real2.length];
		double[] result = new double[real.length];
		int size = real_.val2;
		
		long time = System.currentTimeMillis();
		
		// -----
		// FFT
		ft.FFT2D.doFFT2D(real, imag, size);
		ft.FFT2D.doFFT2D(real2, imag2, size);
		System.out.format("%s: %dms%n", "FFT",System.currentTimeMillis()-time);
		// power spectrum
		complexSquare(real, imag, real);
		complexSquare(real2, imag2, real2);
		System.out.format("%s: %dms%n", "power spec",System.currentTimeMillis()-time);
		// shift to middle
		shift2D(real, size/2, size/2, size, real);
		shift2D(real2, size/2, size/2, size, real2);
		System.out.format("%s: %dms%n", "shift",System.currentTimeMillis()-time);
//		
//		ImageFrame.display(Converter.imageFromArray(normalize(logarithmize(real, null), null), size, imgType), "power spec 1");
//		ImageFrame.display(Converter.imageFromArray(normalize(logarithmize(real2, null), null), size, imgType), "power spec 2");
		// polar transform
		polarTransform(real.clone(), real, size);
		polarTransform(real2.clone(), real2, size);
		System.out.format("%s: %dms%n", "polar",System.currentTimeMillis()-time);
		
//		ImageFrame.display(Converter.imageFromArray(normalize(logarithmize(real, null), null), size, imgType), "polar 1");
//		ImageFrame.display(Converter.imageFromArray(normalize(logarithmize(real2, null), null), size, imgType), "polar 2");
		// correlation
		correlate(real, real2, result, size);
		System.out.format("%s: %dms%n", "correlate",System.currentTimeMillis()-time);
		Pair<Integer, Integer> max = ArrayOps.findMax2D(result, size);
		
		time = System.currentTimeMillis() - time;
		
//		ImageFrame.display(Converter.imageFromArray(normalize(logarithmize(result, result),result), size, imgType), String.format("cross corr (%.3f,%.3f)",max.val1*1.0/size,max.val2*1.0/size));
		double degree = max.val1*360.0/size;
		System.out.format("angle of %f or %f degree, [%.3fs]", degree, degree-360, time/1000.0);
	}
	
	static BufferedImage imageOfFourier(double[] array, int size){
		array = Arrays.copyOf(array, array.length);
		ArrayOps.logarithmize(array, array);
		ArrayOps.normalize(array, array);
		util.ArrayOps.shift2D(array, size/2, size/2, size, array);
		return Converter.imageFromArray(array, size, BufferedImage.TYPE_BYTE_GRAY);
	}
	
	static void convolve(double[] real1, double[] imag1, double[] real2, double[] imag2, double[] realR, double[] imagR, int size){
		ft.FFT2D.doFFT2D(real1, imag1, size);
		ft.FFT2D.doFFT2D(real2, imag2, size);
		ArrayOps.complexMult(real1, imag1, real2, imag2, realR, imagR);
		ArrayOps.scale(imagR, -1, imagR);
		ft.FFT2D.doFFT2D(realR, imagR, size);
		
//		ImageFrame.display(Converter.imageFromDArray(ArrayOps.normalize(imagR, imagR), size, imgType), "conv result imag");
	}
	
	static void convolve(double[] real1, double[] real2, double[] result, int size){
		convolve(real1, new double[real1.length], real2, new double[real2.length], result, new double[result.length], size);
	}
	
	static void correlate(double[] real1, double[] imag1, double[] real2, double[] imag2, double[] realR, double[] imagR, int size){
		ArrayOps.reverse2D(real2, size, real2);
		ArrayOps.reverse2D(imag2, size, imag2);
		convolve(real1, imag1, real2, imag2, realR, imagR, size);
		ArrayOps.reverse2D(realR, size, realR);
		ArrayOps.reverse2D(imagR, size, imagR);
	}
	
	static void correlate(double[] real1, double[] real2, double[] result, int size){
		ArrayOps.reverse2D(real2, size, real2);
		convolve(real1, real2, result, size);
		ArrayOps.reverse2D(result, size, result);
	}
	
	static Pair<double[], Integer> loadDArrayPow2FromImgFile(String filename){
		BufferedImage input = BufferedImageFactory.get(ImageLoader.loadImage(filename), BufferedImage.TYPE_INT_RGB);
		int w = input.getWidth();
		int h = input.getHeight();
		int size =  ArrayOps.getNextPowerOf2(Math.max(w, h));
		double[] array = ArrayOps.paddArray2D(Converter.arrayFromBufferedImage(input), w, size, 0);
		return new Pair<double[], Integer>(array, size);
	}
	
	static void polarTransform(double[] array, double[] polar, int size){
		for(int l = 0; l < size; l++){
			double len = (l*1.0)/size; 
			for(int a = 0; a < size; a++){
				double angle = ((a*1.0)/(size))*2*Math.PI;
				double x = len*Math.cos(angle);
				double y = len*Math.sin(angle);
				polar[l*size+a] = ArrayOps.interpolate2D(array, size, (x+1)/2, (y+1)/2);
			}
		}
	}
	
	static int interpolate(double a, int range){
		return (int) (a*range);
	}
	
	
}
