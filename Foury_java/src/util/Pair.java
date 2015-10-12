package util;

public class Pair<T1,T2> {

	public T1 val1;
	public T2 val2;
	
	public Pair(T1 v1, T2 v2){
		val1=v1;
		val2=v2;
	}
	
	@Override
	public String toString() {
		return String.format("{%s , %s}", val1.toString(), val2.toString());
	}
}
