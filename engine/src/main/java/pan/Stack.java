package pan;

import java.util.ArrayList;
import java.util.List;

public class Stack {

    private CardSet cards;

    public static Stack encode(List<Card> stack) {
        return new Stack(CardSet.from(stack));
    }

    public Stack copy() {
        return new Stack(cards);
    }

    private Stack(CardSet cards) {
        this.cards = cards;
    }

    public boolean isEmpty() {
        return cards.isEmpty();
    }

    public int size() {
        return cards.count();
    }

    public Stack addCard(Card card) {
        return new Stack(cards.add(card));
    }

    public Stack addFourCards(Rank rank) {
        return new Stack(cards.addFourCards(rank));
    }

    public Stack remove(CardSet taken) {
        return new Stack(cards.remove(taken));
    }

    List<Card> decode() {
        List<Card> decoded = new ArrayList<>();
        int bits = cards.bits;
        for (int i = 24 - 1; i >= 0; i--) {
            if ((bits & (1 << i)) != 0) {
                decoded.add(CardSet.cardFromIndex(i));
            }
        }
        return decoded;
    }

    Card peek() {
        assert !isEmpty();
        int topCardIdx = bitIndexOfCardOnTop();
        return CardSet.cardFromIndex(topCardIdx);
    }

    CardSet MUTABLE_popThree() {
        int takenCount = 0;
        CardSet takenCards = CardSet.empty();

        while (takenCount < 3 && size() > 1) {
            CardSet card = MUTABLE_pop();
            takenCards = takenCards.add(card);
            takenCount++;
        }
        return takenCards;
    }

    private CardSet MUTABLE_pop() {
        int i = bitIndexOfCardOnTop();
        CardSet card = new CardSet(1 << i);
        cards = cards.remove(card);
        return card;
    }

    private int bitIndexOfCardOnTop() {
        return Integer.SIZE - 1 - Integer.numberOfLeadingZeros(cards.bits);
    }

    CardSet cardSet() {
        return cards;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Stack stack = (Stack) o;
        return cards.equals(stack.cards);
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
