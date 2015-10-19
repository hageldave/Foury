package util;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Stack;

public final class ArrayPool {

	private static final HashMap<Integer, ArrayPool> pools = new HashMap<>();
	private static final int capacity = 8;
	
	private final Stack<double[]> stack = new Stack<>();
	
	static ArrayPool get(final int size){
		ArrayPool p = pools.get(size);
		if(p == null){
			p = new ArrayPool();
			pools.put(size, p);
		}
		return p;
	}
	
	private ArrayPool() {
		
	}
	
	public static double[] alloc(final int size) {
		final ArrayPool p = get(size);
		if(p.stack.isEmpty()){
			return new double[size];
		} else {
			return p.stack.pop();
		}
	}
	
	public static double[] alloc(final int size, final double fill) {
		double[] array = alloc(size);
		Arrays.fill(array, fill);
		return array;
	}
	
	public static void free(final double[] array){
		final ArrayPool p = get(array.length);
		if(p.stack.size() < capacity){
			p.stack.push(array);
		}
	}
	
	public static double[] arrayCopy(final double[] array){
		final double[] cpy = alloc(array.length);
		System.arraycopy(array, 0, cpy, 0, array.length);
		return cpy;
	}
	
	public static void clearCache(final int size){
		final ArrayPool p = get(size);
		p.stack.clear();
		System.gc();
	}
	
	public static void clearCacheAll(){
		for(ArrayPool p: pools.values())
			p.stack.clear();
		System.gc();
	}
	
}
