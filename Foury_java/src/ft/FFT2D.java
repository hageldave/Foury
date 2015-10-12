package ft;

import util.ArrayOps;

public class FFT2D {

	public static void doFFT2D(double[] real, double[] imag, int size){
		// lines first
		for(int i = 0; i < size; i++){
			FFT_Foreign.transformRadix2(real, imag, i*size, size);
		}
		
		ArrayOps.transpose(real, size, real, null);
		ArrayOps.transpose(imag, size, imag, null);
		
		// now rows
		for(int i = 0; i < size; i++){
			FFT_Foreign.transformRadix2(real, imag, i*size, size);
		}
		
		ArrayOps.transpose(real, size, real, null);
		ArrayOps.transpose(imag, size, imag, null);
	}
	
	public static void doFFT2D(double[] real, double[] imag, int size, double scaling){
		doFFT2D(real, imag, size);
		for(int i = 0; i < real.length; i++){
			real[i] *= scaling;
			imag[i] *= scaling;
		}
	}
	
	
}
