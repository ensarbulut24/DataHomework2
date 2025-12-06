package src;

import java.util.Objects;

public class Protein {
    private String id; 

    public Protein(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    @Override
    public String toString() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Protein protein = (Protein) o;
        return Objects.equals(id, protein.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}