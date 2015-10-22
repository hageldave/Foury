package analysis;

import static util.ArrayOps.*;
import static util.ArrayPool.*;
import static util.ImageOps.*;

import java.awt.Point;
import java.awt.image.BufferedImage;

public class RotationDetection {
	static final int imgType = BufferedImage.TYPE_BYTE_GRAY;

	public static double calculateRotation(double[] img1, double[] img2, int size){
		double[] real1 = img1;
		double[] real2 = img2;
		double[] imag = alloc(real1.length, 0);
		double[] imag2 = alloc(real2.length, 0);

		long time = System.currentTimeMillis();
		
		// FFT
		ft.FFT2D.doFFT2D(real1, imag, size);
		ft.FFT2D.doFFT2D(real2, imag2, size);
		System.out.format("%s: %dms%n", "FFT",System.currentTimeMillis()-time);
		// power spectrum
		complexSquare(real1, imag, real1);
		complexSquare(real2, imag2, real2);
		System.out.format("%s: %dms%n", "power spec",System.currentTimeMillis()-time);
		// shift to middle
		shift2D(real1, size/2, size/2, size, real1);
		shift2D(real2, size/2, size/2, size, real2);
		System.out.format("%s: %dms%n", "shift",System.currentTimeMillis()-time);

		displayLogNorm(real1, size, "power spec 1");
		displayLogNorm(real2,size, "power spec 2");
		
//		// polar transform
//		int angleSamples = size;
//		polarTransform(real1, real1, size, 0.1, 0.2);
//		polarTransform(real2, real2, size, 0.1, 0.2);
//		System.out.format("%s: %dms%n", "polar",System.currentTimeMillis()-time);
//
//		displayLogNorm(real1, size, "polar 1");
//		displayLogNorm(real2, size, "polar 2");
//		
//		// translation of polar
//		Point p = TranslationDetection.calculateTranslation(real1, real2, size);
//		int min = p.x;

		// polar transform reduced to 1D
		int angleSamples = 1024;
		double[] polar1 = polarTransformAsLine(real1, null, size, 0.1, 0.6, size, angleSamples);
		double[] polar2 = polarTransformAsLine(real2, null, size, 0.1, 0.6, size, angleSamples);
		System.out.format("%s: %dms%n", "polar",System.currentTimeMillis()-time);
		
		displayLogNorm(polar1, polar1.length, "polar 1");
		displayLogNorm(polar2, polar2.length, "polar 2");
		
		// translation of polar
		int min = TranslationDetection.calculateTranslation1D(polar1, polar2);
		
		
		System.out.format("%s: %dms%n", "correlate",System.currentTimeMillis()-time);

//		ImageFrame.display(Converter.imageFromArray(normalize(logarithmize(result, result),result), size, imgType), String.format("cross corr (%.3f,%.3f)",max.val1*1.0/size,max.val2*1.0/size));
		double degree = min*360.0/angleSamples;
		System.out.format("angle of %f or %f degree, [%.3fs]%n", degree, degree-360, (System.currentTimeMillis()-time)/1000.0);
		
		free(imag);
		free(imag2);
		
		return (min * Math.PI*2.0) / angleSamples;
	}
	
	static double sumAbs(double[] array, int offset, int size){
		double sum = 0;
		for(int i = offset; i < offset+size; i++)
			sum += Math.abs(array[i]);
		return sum;
	}
	
}
