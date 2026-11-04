package pan;

public enum Rank {
    NINE("9"),
    TEN("10"),
    JACK("J"),
    QUEEN("Q"),
    KING("K"),
    ACE("A");

    private final String name;

    Rank(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
