package src;

public class Interaction {
    private Protein source;
    private Protein destination;
    private double weight;

    public Interaction(Protein source, Protein destination, double weight) {
        this.source = source;
        this.destination = destination;
        this.weight = weight;
    }

    public Protein getSource() {
        return source;
    }

    public Protein getDestination() {
        return destination;
    }

    public double getWeight() {
        return weight;
    }
    
    @Override
    public String toString() {
        return source.getId() + " -> " + destination.getId() + " : " + weight;
    }
}