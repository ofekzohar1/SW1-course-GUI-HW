package riddles;

import java.util.Objects;

public class B extends A{
	
	protected int i;
	protected int j;


	public B(int i, int j) {
		super(i,j);
	
	}


	@Override
	public int hashCode() {
		return Objects.hash(super.i, super.j);
	}


	@Override
	public boolean equals(Object obj) {
		return super.equals(obj);
	}

}
