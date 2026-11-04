package pan;

import java.util.Iterator;
import java.util.List;

public class Hand {

    private final CardSet cards;

    public static Hand from(List<Card> cards) {
        return new Hand(CardSet.from(cards));
    }

    private Hand(CardSet cards) {
        this.cards = cards;
    }

    public boolean isEmpty() {
        return cards.isEmpty();
    }

    public Hand add(CardSet more) {
        return new Hand(cards.add(more));
    }

    public Hand removeCard(Card card) {
        return new Hand(cards.removeCard(card));
    }

    public boolean hasFourNines() {
        return cards.hasFourNines();
    }

    public Hand removeFourCards(Rank rank) {
        return new Hand(cards.removeFourCards(rank));
    }

    public List<Rank> fourCards() {
        return cards.fourCards();
    }

    public Iterator<Card> cards() {
        return cards.cards();
    }

    public List<Card> getCards() {
        return cards.getCards();
    }

    CardSet cardSet() {
        return cards;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Hand hand = (Hand) o;
        return cards.equals(hand.cards);
    }

    @Override
    public int hashCode() {
        return cards.hashCode();
    }

    @Override
    public String toString() {
        return cards.toString();
    }
}
