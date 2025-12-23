package src;

public class Interaction {
    private Protein s;
    private Protein d;
    private double w;

    public Interaction(Protein s, Protein d, double w) {
        this.s = s;
        this.d = d;
        this.w = w;
    }

    public Protein getSource() { return s; }
    public Protein getDest() { return d; }
    public double getW() { return w; }
    
    public String toString() {
        return s.getId() + " -> " + d.getId() + " (" + w + ")";
    }
}