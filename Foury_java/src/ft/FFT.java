package ft;

import util.Pair;

public class FFT {
	
	static final double PI = Math.PI;
	static final double PI2 = 2*PI;

	public static Pair<double[], double[]> calcFFT(double[] input){
		return calcFFT(input, new double[input.length], new double[input.length]);
	}
	
	public static Pair<double[], double[]> calcFFT(double[] input, double[] real, double[] imaginary){
		if(real.length != input.length || imaginary.length != input.length)
			throw new IllegalArgumentException("arrays of different sizes!");
		if(Integer.bitCount(input.length) != 1)
			throw new IllegalArgumentException("array length not power of 2");
		
		doFFT(input, real, imaginary);
		return new Pair<double[], double[]>(real, imaginary);
	}
	
	private static void doFFT(double[] input, double[] real, double[] imag){
		if(input.length == 1){
			real[0] = input[0];
		} else {
			int n = input.length;
			double[] even = new double[n/2];
			double[] odd = new double[n/2];
			split(input, even, odd);
			double[] real_1 = new double[n/2];
			double[] real_2 = new double[n/2];
			double[] imag_1 = new double[n/2];
			double[] imag_2 = new double[n/2];
			
			doFFT(even, real_1, imag_1);
			doFFT(odd , real_2, imag_2);
			
			Complex coeff = new Complex(0, 0);
			Complex prod = new Complex(0, 0);
			Complex sum = new Complex(0, 0);
			for(int k = 0; k < n/2; k++){
				coeff = complexFromPolar((-PI2*k)/n, coeff);
				prod = complexMul(real_2[k], imag_2[k], coeff.val1, coeff.val2, prod);
				
				sum = complexAdd(real_1[k], imag_1[k], prod.val1, prod.val2, sum);
				real[k] = sum.val1;
				imag[k] = sum.val2;
				
				sum = complexAdd(real_1[k], imag_1[k], -prod.val1, -prod.val2, sum);
				real[k+n/2] = sum.val1;
				imag[k+n/2] = sum.val2;
			}
		}
	}
	
	static void split(double[] input, double[] even, double[] odd){
		for(int i = 0; i < input.length/2;i++){
			even[i] = input[i*2];
			odd[i] = input[i*2+1];
		}
	}
	
	static Complex complexAdd(double a, double bi, double c, double di, Complex result){
		result.val1 = a+c;
		result.val2 = bi+di;
		return result;
	}
	
	static Complex complexMul(double a, double bi, double c, double di, Complex result){
		result.val1 = a*c - bi*di;
		result.val2 = a*di + bi*c;
		return result;
	}
	
	static Complex complexFromPolar(double angle, Complex result){
		result.val1 = cos(angle);
		result.val2 = sin(angle);
		return result;
	}
	
	static class Complex extends Pair<Double, Double> {
		public Complex(double v1, double v2) {
			super(v1, v2);
		}
	}
	

	public static float cos(float x){
		return (float) Math.cos(x);
	}
	
	public static double cos(double x){
		return Math.cos(x);
	}
	
	public static float sin(float x){
		return (float) Math.sin(x);
	}
	
	public static double sin(double x){
		return Math.sin(x);
	}
}
