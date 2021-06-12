package riddles;


public class A implements Comparable<A> {
	
	protected int i;
	protected int j;

	public A(int i, int j) {
		this.i = i;
		this.j = j;
	}

	@Override
	public int hashCode() {
		//Put your code here
		return 0;
	}

	@Override
	public boolean equals(Object obj) {
		//Put your code here
		return true;
	}

	@Override
	public int compareTo(A o) {
		//Put your code here
		return 0;
	}
	
	
	public String toString() {return "("+this.i+" "+this.j+")";}



}
