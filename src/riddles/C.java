package riddles;

public class C extends B {

    private int i;
    private int j;

    public C(int i, int j) {
        super(i, j);

    }

    @Override
    public int compareTo(A other) {
        int comp = Integer.compare(other.i, ((A)this).i);
        if (comp != 0)
            return comp;
        return Integer.compare(other.j, ((A)this).j);
    }


}