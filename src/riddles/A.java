package riddles;

import java.util.Objects;

public class A implements Comparable<A> {
	
	protected int i;
	protected int j;

	public A(int i, int j) {
		this.i = i;
		this.j = j;
	}

	@Override
	public int hashCode() {
		return Objects.hash(j);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null || !(obj instanceof A)) return false;
		A a = (A) obj;
		return j == a.j;
	}

	@Override
	public int compareTo(A o) {
		return Integer.compare(this.j, o.j);
	}
	

	public String toString() {return "("+this.i+" "+this.j+")";}

}
