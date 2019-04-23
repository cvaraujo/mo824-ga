package triple;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * An object of this class represents a prohibited triple for MAXQBFPT problem.
 */
public class Triple {

//    public final ArrayList<TripleElement> elements;
    public final ArrayList<Integer> elements;

    public Triple(Integer te1, Integer te2, Integer te3) {
        this.elements = new ArrayList<Integer>(Arrays.asList(te1, te2, te3));
    }

    public ArrayList<Integer> getElements() {
        return elements;
    }

    public void printTriple() {
        System.out.print("[" + elements.get(0) + ", ");
        System.out.print(elements.get(1) + ", ");
        System.out.println(elements.get(2) + "]");
    }
}
