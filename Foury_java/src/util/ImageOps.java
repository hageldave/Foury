package util;

import static util.ArrayOps.*;
import static util.ArrayPool.*;

import java.awt.image.BufferedImage;

public class ImageOps {

	public static double[] polarTransform(double[] array, double[] polar, int size, double startRadius, double maxRadius){
		boolean tmpArray = false;
		if(polar == null || array == polar){
			polar = array;
			array = arrayCopy(array);
			tmpArray = true;
		}
		
		for(int l = 0; l < size; l++){
			double len = ((l*1.0)/size)*(maxRadius-startRadius); 
			for(int a = 0; a < size; a++){
				double angle = ((a*1.0)/(size-1))*2*Math.PI;
				double x = (startRadius+len)*Math.cos(angle);
				double y = (startRadius+len)*Math.sin(angle);
				polar[l*size+a] = ArrayOps.interpolate2D(array, size, (x+1)/2, (y+1)/2);
			}
		}
		if(tmpArray)
			free(array);
		
		return polar;
	}
	
	public static double[] rotate(double[] array, double[] result, int size, double angle, int x0, int y0){
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
	
	public static void convolve(double[] real1, double[] imag1, double[] real2, double[] imag2, double[] realR, double[] imagR, int size){
		ft.FFT2D.doFFT2D(real1, imag1, size);
		ft.FFT2D.doFFT2D(real2, imag2, size);
		ArrayOps.complexMult(real1, imag1, real2, imag2, realR, imagR);
		ArrayOps.scale(imagR, -1, imagR);
		ft.FFT2D.doFFT2D(realR, imagR, size);
		shift2D(realR, size/2, size/2, size, realR);
		shift2D(imagR, size/2, size/2, size, imagR);
//		ft.FFT2D.doFFT2D(imag1, real1, size);
	}
	
	public static void convolve(double[] real1, double[] real2, double[] result, int size){
		double[] imag1 = alloc(real1.length,0);
		double[] imag2 = alloc(real1.length,0);
		double[] imagR = alloc(real1.length);
		convolve(real1, imag1, real2, imag2, result, imagR, size);
		free(imag1);free(imag2);free(imagR);
	}
	
	public static void correlate(double[] real1, double[] imag1, double[] real2, double[] imag2, double[] realR, double[] imagR, int size){
		ft.FFT2D.doFFT2D(real1, imag1, size);
		ft.FFT2D.doFFT2D(real2, imag2, size);
		scale(imag1, -1, imag1);
		ArrayOps.complexMult(real1, imag1, real2, imag2, realR, imagR);
		ArrayOps.scale(imagR, -1, imagR);
		ft.FFT2D.doFFT2D(realR, imagR, size);
	}
	
	public static void correlate(double[] real1, double[] real2, double[] result, int size){
		double[] imag1 = alloc(real1.length,0);
		double[] imag2 = alloc(real1.length,0);
		double[] imagR = alloc(real1.length);
		correlate(real1, imag1, real2, imag2, result, imagR, size);
		free(imag1);free(imag2);free(imagR);
	}
	
	public static double[] fadeOutEdges(double[] array, double[] result, int size, double fadeThresh){
		if(result == null)
			result = alloc(array.length); 
		if(array != result){
			System.arraycopy(array, 0, result, 0, array.length);
			array = result;
		}
		
		double center = size/2.0;
		double fadeRange = 1.0-fadeThresh;
		for(int y = 0; y < size; y++){
			double distY = Math.abs((y-center)/center);
			for(int x = 0; x < size; x++){
				double distX = Math.abs((x-center)/center);
				double dist = Math.hypot(distX, distY);
				if(dist >= fadeThresh){
					double factor = Math.min(dist-fadeThresh,fadeRange)/fadeRange;
					result[y*size+x] = array[y*size+x]*Math.sqrt(1.0-factor);
				} else {
					result[y*size+x] = array[y*size+x];
				}
			}
		}
		return result;
	}
	
	public static double[] makeGaussKernel(int size, double sigma){
		int radius = size;
		double[] kernel = alloc(size*size, 0);
		double sum = 0;
		for (int y = 0; y < radius; y++) {
			for (int x = 0; x < radius; x++) {
				int off = y * size + x;
				double xx = (x) - radius / 2.0;
				double yy = (y) - radius / 2.0;
				kernel[off] = 1/Math.pow(Math.E, (xx * xx + yy * yy)
						/ (2 * (sigma * sigma)));
				sum += kernel[off];
			}
		}
		for (int i = 0; i < kernel.length; i++){
			kernel[i] /= sum;
		}
		return kernel;
	}
	
	public static double[] makeBandKernel(int size, double sig1, double sig2){
		double[] g1 = makeGaussKernel(size, sig1);
		double[] g2 = makeGaussKernel(size, sig2);
		ArrayOps.difference(g1, g2, g1);
		free(g2);
		return g1;
	}

	
	public static void display(double[] array, int size, String text){
		ImageFrame.display(Converter.imageFromArray(array, size, BufferedImage.TYPE_BYTE_GRAY), text);
	}

	public static void displayLogNorm(double[] array, int size, String text){
		double[] tmp = alloc(array.length);
		normalize(logarithmize(array, tmp), tmp);
		ImageFrame.display(Converter.imageFromArray(tmp, size, BufferedImage.TYPE_BYTE_GRAY), text);
		free(tmp);
	}
	
	public static void displayNorm(double[] array, int size, String text){
		double[] tmp = alloc(array.length);
		normalize(array, tmp);
		ImageFrame.display(Converter.imageFromArray(tmp, size, BufferedImage.TYPE_BYTE_GRAY), text);
		free(tmp);
	}
	
}
