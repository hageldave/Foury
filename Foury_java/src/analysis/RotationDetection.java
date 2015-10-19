package analysis;

import static util.ArrayOps.*;
import static util.ArrayPool.*;
import static util.ImageOps.*;

import java.awt.Point;
import java.awt.image.BufferedImage;

public class RotationDetection {
	static final int imgType = BufferedImage.TYPE_BYTE_GRAY;

	public static double calculateRotation(double[] img1, double[] img2, int size){
		double[] real = img1;
		double[] real2 = img2;
		double[] imag = alloc(real.length, 0);
		double[] imag2 = alloc(real2.length, 0);

		long time = System.currentTimeMillis();
		
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

		displayLogNorm(real, size, "power spec 1");
		displayLogNorm(real2,size, "power spec 2");
		// polar transform
		polarTransform(real, real, size, 0.1, 0.6);
		polarTransform(real2, real2, size, 0.1, 0.6);
		System.out.format("%s: %dms%n", "polar",System.currentTimeMillis()-time);

		displayLogNorm(real, size, "polar 1");
		displayLogNorm(real2, size, "polar 2");
		
		// translation of polar
		Point p = TranslationDetection.calculateTranslation(real, real2, size);
		int min = p.x;
//		double[] sums = alloc(size);
//		double[] diff = alloc(real.length);
//		for(int i = 0; i < size; i++){
//			difference(real, real2, diff);
//			sums[i] = sumAbs(diff, size*10, diff.length/2);
//			shift2D(real, 1, 0, size, real);
//		}
//		int min = findMin(sums);
//		displayLogNorm(sums, size, "sums");
		System.out.format("%s: %dms%n", "correlate",System.currentTimeMillis()-time);

//		ImageFrame.display(Converter.imageFromArray(normalize(logarithmize(result, result),result), size, imgType), String.format("cross corr (%.3f,%.3f)",max.val1*1.0/size,max.val2*1.0/size));
		double degree = min*360.0/size;
		System.out.format("angle of %f or %f degree, [%.3fs]%n", degree, degree-360, (System.currentTimeMillis()-time)/1000.0);
		
		free(imag);
		free(imag2);
		
		return (min * Math.PI*2.0) / size;
	}
	
	static double sumAbs(double[] array, int offset, int size){
		double sum = 0;
		for(int i = offset; i < offset+size; i++)
			sum += Math.abs(array[i]);
		return sum;
	}
	
}
