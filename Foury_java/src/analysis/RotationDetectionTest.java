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
import static util.ArrayPool.*;

public class RotationDetectionTest {
	static final int imgType = BufferedImage.TYPE_BYTE_GRAY;
	
	public static void main(String[] args) {
		String inputName = "res/h1.png";
		String inputName2 = "res/h2.png";
		
		Pair<double[], Integer> real_ = loadDArrayPow2FromImgFile(inputName);
		double[] real = real_.val1;
		double[] imag = alloc(real.length, 0);
		Pair<double[], Integer> real2_ = loadDArrayPow2FromImgFile(inputName2);
		double[] real2 = real2_.val1;
		double[] imag2 = alloc(real2.length, 0);
		double[] result = alloc(real.length);
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
		
//		ImageFrame.display(Converter.imageFromArray(normalize(logarithmize(real, null), null), size, imgType), "power spec 1");
//		ImageFrame.display(Converter.imageFromArray(normalize(logarithmize(real2, null), null), size, imgType), "power spec 2");
		// polar transform
		polarTransform(real, real, size);
		polarTransform(real2, real2, size);
		System.out.format("%s: %dms%n", "polar",System.currentTimeMillis()-time);
		
//		ImageFrame.display(Converter.imageFromArray(normalize(logarithmize(real, null), null), size, imgType), "polar 1");
//		ImageFrame.display(Converter.imageFromArray(normalize(logarithmize(real2, null), null), size, imgType), "polar 2");
		// correlation
		correlate(real, real2, result, size);
		System.out.format("%s: %dms%n", "correlate",System.currentTimeMillis()-time);
		Pair<Integer, Integer> max = ArrayOps.findMax2D(result, size);
		
//		ImageFrame.display(Converter.imageFromArray(normalize(logarithmize(result, result),result), size, imgType), String.format("cross corr (%.3f,%.3f)",max.val1*1.0/size,max.val2*1.0/size));
		double degree = max.val1*360.0/size;
		System.out.format("angle of %f or %f degree, [%.3fs]%n", degree, degree-360, (System.currentTimeMillis()-time)/1000.0);
		
		// free resources
		free(result);
		free(real);
		free(real2);
		free(imag);
		free(imag2);
		// reload images
		real = loadDArrayPow2FromImgFile(inputName).val1;
		real2 = loadDArrayPow2FromImgFile(inputName2).val1;
		// padd images for rotation
		int targetSize = size*2;
		real = paddArray2D(real, size, targetSize, 0);
		real2 = paddArray2D(real2, size, targetSize, 0);
		// shift to middle
		shift2D(real, size/2, size/2, targetSize, real);
		shift2D(real2, size/2, size/2, targetSize, real2);
		// rotate
		rotate(real2, real2, targetSize, -max.val1*2*Math.PI/size, targetSize/2, targetSize/2);
		
//		ImageFrame.display(Converter.imageFromArray(real, targetSize, imgType), "original");
//		ImageFrame.display(Converter.imageFromArray(real2, targetSize, imgType), "back rotation");
		
		// correllation
		result = alloc(real.length);
		correlate(real, real2, result, targetSize);
		Pair<Integer, Integer> max2 = findMax2D(result, targetSize);
		System.out.format("translation of %s, [%.3fs]%n",max2, (System.currentTimeMillis()-time)/1000.0);
		
//		ImageFrame.display(Converter.imageFromArray(normalize(logarithmize(result, result),result), targetSize, imgType), "cross corr of transl.");
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
		convolve(real1, alloc(real1.length,0), real2, alloc(real2.length,0), result, alloc(result.length), size);
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
	
	static double[] polarTransform(double[] array, double[] polar, int size){
		boolean tmpArray = false;
		if(polar == null || array == polar){
			polar = array;
			array = arrayCopy(array);
			tmpArray = true;
		}
		for(int l = 0; l < size; l++){
			double len = (l*1.0)/size; 
			for(int a = 0; a < size; a++){
				double angle = ((a*1.0)/(size))*2*Math.PI;
				double x = len*Math.cos(angle);
				double y = len*Math.sin(angle);
				polar[l*size+a] = ArrayOps.interpolate2D(array, size, (x+1)/2, (y+1)/2);
			}
		}
		if(tmpArray)
			free(array);
		
		return polar;
	}
	
	static double[] rotate(double[] array, double[] result, int size, double angle, int x0, int y0){
		boolean tmpArray = false;
		if(result == null || result == array){
			result = array;
			array = arrayCopy(array);
		}
		
		double sin = Math.sin(-angle);
		double cos = Math.cos(-angle);
		for(int y = 0; y < size; y++){
			for(int x = 0; x < size; x++){
				int x_ = x-x0;
				int y_ = y-y0;
				/* cos -sin *
				 * sin  cos */
				double tx = x_*cos - y_*sin  +x0;
				double ty = x_*sin + y_*cos  +y0;
				
				if(tx < 0 || tx >= size || ty < 0 || ty >= size){
					// out of img
					result[y*size+x] = 0;
				} else {
					result[y*size+x] = array[((int)ty)*size+((int)tx)];
				}
			}
		}
		
		if(tmpArray)
			free(array);
		
		return result;
	}
	
	static int interpolate(double a, int range){
		return (int) (a*range);
	}
	
	
}
