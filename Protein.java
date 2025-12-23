package src;

public class Protein {
    private String id;

    public Protein(String id) {
        this.id = id;
    }

    public String getId() { return id; }
    public String toString() { return id; }

    // List içinde 'contains' veya 'equals' kontrolleri için bu gerekli
    public boolean equals(Object o) {
        if (o instanceof Protein) {
            return this.id.equals(((Protein) o).id);
        }
        return false;
    }
}