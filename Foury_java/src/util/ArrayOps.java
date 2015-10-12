package util;

import java.util.Arrays;
import static util.ArrayPool.*;

public class ArrayOps {

	public static double max(double[] array){
		double max = array[0];
		for(double d: array)
			max = Math.max(d, max);
		return max;
	}
	
	public static double min(double[] array){
		double min = array[0];
		for(double d: array)
			min = Math.min(d, min);
		return min;
	}
	
	public static int findMax(double[] array){
		double max = array[0];
		int maxIdx = 0;
		for(int i = 0; i < array.length; i++){
			if(max < array[i]){
				max = array[i];
				maxIdx = i;
			}
		}
		return maxIdx;
	}
	
	public static Pair<Integer, Integer> findMax2D(double[] array, int width){
		int idx = findMax(array);
		return new Pair<Integer, Integer>(idx%width, idx/width);
	}
	
	public static int findMin(double[] array){
		double min = array[0];
		int minIdx = 0;
		for(int i = 0; i < array.length; i++){
			if(min > array[i]){
				min = array[i];
				minIdx = i;
			}
		}
		return minIdx;
	}
	
	public static double[] normalize(double[] array, double[] result){
		if(result == null)
			result = alloc(array.length); 
		if(array != result){
			System.arraycopy(array, 0, result, 0, array.length);
			array = result;
		}
		double min = min(array);
		double range = max(array) - min;
		for(int i = 0; i < array.length; i++)
			array[i] = (array[i]-min)/range;
		return array;
	}
	
	public static double[] logarithmize(double[] array, double[] result){
		if(result == null)
			result = alloc(array.length); 
		if(array != result){
			System.arraycopy(array, 0, result, 0, array.length);
			array = result;
		}
		for(int i = 0; i < array.length; i++)
			array[i] = Math.log(Math.abs(array[i])+1.0);
		return array;
	}
	
	public static double[] complexAbs(double[] real, double[] imag, double[] result){
		if(result == null)
			result = alloc(real.length); 
		if(real != result){
			System.arraycopy(real, 0, result, 0, real.length);
			real = result;
		}
		for(int i = 0; i < real.length; i++)
			result[i] = Math.sqrt(real[i]*real[i] + imag[i]*imag[i]);
		return result;
	}
	
	public static double[] complexSquare(double[] real, double[] imag, double[] result){
		if(result == null)
			result = alloc(real.length); 
		if(real != result){
			System.arraycopy(real, 0, result, 0, real.length);
			real = result;
		}
		for(int i = 0; i < real.length; i++)
			result[i] = real[i]*real[i] + imag[i]*imag[i];
		return result;
	}
	
	public static double[] mult(double[] a, double[] b, double[] result){
		if(result == null)
			result = alloc(a.length); 
		if(a != result){
			System.arraycopy(a, 0, result, 0, a.length);
			a = result;
		}
		for(int i = 0; i < a.length; i++)
			result[i] = a[i]*b[i];
		return result;
	}
	
	public static void complexMult(double[] realA, double[] imagA, double[] realB, double[] imagB, double[] realR, double[] imagR){
		if(realR == null)
			realR = alloc(realA.length); 
		if(realA != realR){
			System.arraycopy(realA, 0, realR, 0, realA.length);
			realA = realR;
		}
		if(imagR == null)
			imagR = alloc(imagA.length); 
		if(imagA != realR){
			System.arraycopy(imagA, 0, imagR, 0, imagA.length);
			imagA = imagR;
		}
		for(int i = 0; i < realA.length; i++){
			double a = realA[i];
			double bi = imagA[i];
			double c = realB[i];
			double di = imagB[i];
			realR[i] = a*c - bi*di;
			imagR[i] = a*di + bi*c;
		}
	}
	
	public static double[] scale(double[] array, double factor, double[] result){
		if(result == null)
			result = alloc(array.length); 
		if(array != result){
			System.arraycopy(array, 0, result, 0, array.length);
			array = result;
		}
		for(int i = 0; i < array.length; i++)
			array[i] *= factor;
		return array;
	}

	public static double[] transpose(double[] array, int size, double[] result, double[] cpy){
		if(result == null)
			result = alloc(array.length); 
		if(array != result){
			System.arraycopy(array, 0, result, 0, array.length);
			array = result;
		}
		boolean tempCpy = false;
		if(cpy == null || cpy == array){
			cpy = arrayCopy(array);
			tempCpy = true;
		}
		
		for(int y = 0; y < size; y++)
			for(int x = 0; x < size; x++)
				array[y*size+x] = cpy[x*size+y];
		if(tempCpy)
			free(cpy);
		return array;
	}

	
	public static double[] shift(double[] array, int shift, int offset, int size, double[] result, double[] cpy){
		if(result == null)
			result = alloc(array.length); 
		if(array != result){
			System.arraycopy(array, 0, result, 0, array.length);
			array = result;
		}
		boolean tempCpy = false;
		if(cpy == null || cpy == array){
			cpy = arrayCopy(array);
			tempCpy = true;
		}
		
		for(int i = 0; i < size; i++){
			array[(offset+((i+shift)%size))%array.length] = cpy[(offset+i)%array.length];
		}
		if(tempCpy)
			free(cpy);
		return array;
	}

	public static double[] shift2D(double[] array, int xShift, int yShift, int size, double[] result){
		if(result == null)
			result = alloc(array.length); 
		if(array != result){
			System.arraycopy(array, 0, result, 0, array.length);
			array = result;
		}
		double[] cpy = arrayCopy(array);
		// shift rows first
		for(int i = 0; i < size; i++)
			shift(array, xShift, i*size, size, array, cpy);
		System.arraycopy(array, 0, cpy, 0, array.length);
		transpose(array, size, array, cpy);
		System.arraycopy(array, 0, cpy, 0, array.length);
		for(int i = 0; i < size; i++)
			shift(array, yShift, i*size, size, array, cpy);
		System.arraycopy(array, 0, cpy, 0, array.length);
		transpose(array, size, array,cpy);
		
		free(cpy);
		
		return array;
	}
	
	public static double[] reverse(double[] array, int offset, int size, double[] result, double[] cpy){
		if(result == null)
			result = alloc(array.length); 
		if(array != result){
			System.arraycopy(array, 0, result, 0, array.length);
			array = result;
		}
		
		boolean tempCpy = false;
		if(cpy == null || cpy == array){
			cpy = arrayCopy(array);
			tempCpy = true;
		}
		
		for(int i = 0; i < size; i++)
			array[offset+i] = cpy[offset+size-i-1];
		
		if(tempCpy)
			free(cpy);
		
		return array;
	}
	
	public static double[] reverse2D(double[] array, int size, double[] result){
		if(result == null)
			result = alloc(array.length); 
		if(array != result){
			System.arraycopy(array, 0, result, 0, array.length);
			array = result;
		}
		double[] cpy = arrayCopy(array);
		// rows first
		for(int i= 0; i < size; i++)
			reverse(array, i*size, size, array, cpy);
		System.arraycopy(array, 0, cpy, 0, array.length);
		transpose(array, size, array, cpy);
		System.arraycopy(array, 0, cpy, 0, array.length);
		for(int i= 0; i < size; i++)
			reverse(array, i*size, size, array, cpy);
		System.arraycopy(array, 0, cpy, 0, array.length);
		transpose(array, size, array, cpy);
		
		free(cpy);
		
		return result;
	}
	
	public static double[] paddArray2D(double[] array, int size, int targetSize, double value){
		int h = array.length/size;
		int w = size;
		double[] target = alloc(targetSize*targetSize);
		for(int y = 0; y < h; y++){
			for(int x = 0; x < w; x++){
				target[y*targetSize+x] = array[y*size+x];
			}
			for(int x = w; x < targetSize; x++){
				target[y*targetSize+x] = value;
			}
		}
		for(int i = h*targetSize; i < target.length; i++){
			target[i] = value;
		}
		return target;
	}
	
	public static int getNextPowerOf2(int n){
		int pow = 1;
		int bits = 1;
		while(n > pow && bits <= 31){
			pow <<= 1;
			bits++;
		}
		return pow;
	}
	
	/** x and y element of [0..1[ */
	public static double interpolate2D(double[] array, int size, double x, double y){
		double w = size-1;
		double h = (array.length/size)-1;
		int x1 = (int) (x*w);
//		int x2 = x1+1;
		int y1 = (int) (y*h);
//		int y2 = y1+1;
		double v1 = array[y1*size+x1]*(x1+1-x*w) + array[y1*size+x1+1]*(x*w-x1);
		double v2 = array[(y1+1)*size+x1]*(x1+1-x*w) + array[(y1+1)*size+x1+1]*(x*w-x1);
		return v1*(y1+1-y*h) + v2*(y*h-y1);
	}

	public static String toString(double[] array, int offset, int size){
		StringBuilder s = new StringBuilder(size*4);
		s.append("[");
		for(int i = 0; i < size; i++){
			s.append(String.valueOf(array[offset+i]));
			if(i < size-1)
				s.append(", ");
		}
		s.append("]");
		return s.toString();
	}
}
