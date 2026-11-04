package pan;

import java.util.Objects;

public final class Card {

    private static final Card[] cards;

    static {
        cards = new Card[Rank.values().length * Suit.values().length];
        for (Rank rank : Rank.values()) {
            for (Suit suit : Suit.values()) {
                int i = toIndex(rank, suit);
                cards[i] = new Card(rank, suit);
            }
        }
    }

    private final Rank rank;
    private final Suit suit;

    private Card(Rank rank, Suit suit) {
        this.rank = rank;
        this.suit = suit;
    }

    public static Card of(Rank rank, Suit suit) {
        return cards[toIndex(rank, suit)];
    }

    private static int toIndex(Rank rank, Suit suit) {
        return rank.ordinal() * 4 + suit.ordinal();
    }

    @Override
    public String toString() {
        return rank.toString() + suit.toString();
    }

    public Rank rank() {
        return rank;
    }

    public Suit suit() {
        return suit;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (Card) obj;
        return Objects.equals(this.rank, that.rank) && Objects.equals(this.suit, that.suit);
    }

    @Override
    public int hashCode() {
        return rank.ordinal() * 4 + suit.ordinal();
    }
}
