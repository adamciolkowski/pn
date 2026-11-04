package pan;

import java.util.ArrayList;
import java.util.List;

public class Stack {

    private static final Rank[] RANKS = Rank.values();
    private static final Suit[] SUITS = Suit.values();

    int bits;

    public static Stack encode(List<Card> stack) {
        return new Stack(HandOperations.encode(stack));
    }

    public Stack copy() {
        return new Stack(bits);
    }

    private Stack(int bits) {
        this.bits = bits;
    }

    public boolean isEmpty() {
        return HandOperations.isEmpty(bits);
    }

    public int size() {
        return HandOperations.count(bits);
    }

    public Stack addCard(Card card) {
        return new Stack(HandOperations.addCard(bits, card));
    }

    public Stack addFourCards(Rank rank) {
        return new Stack(HandOperations.addFourCards(bits, rank));
    }

    public Stack remove(CardSet cards) {
        return new Stack(bits & ~cards.bits);
    }

    List<Card> decode() {
        List<Card> cards = new ArrayList<>();
        for (int i = 24 - 1; i >= 0; i--) {
            if ((bits & (1 << i)) != 0) {
                cards.add(cardFromIndex(i));
            }
        }
        return cards;
    }

    private static Card cardFromIndex(int idx) {
        Rank rank = RANKS[idx / 4];
        Suit suit = SUITS[idx % 4];
        return Card.of(rank, suit);
    }

    Card peek() {
        assert !isEmpty();
        int topCardIdx = bitIndexOfCardOnTop();
        return cardFromIndex(topCardIdx);
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
        int card = 1 << i;
        bits &= ~card;
        return new CardSet(card);
    }

    private int bitIndexOfCardOnTop() {
        return Integer.SIZE - 1 - Integer.numberOfLeadingZeros(bits);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Stack stack = (Stack) o;
        return bits == stack.bits;
    }

    @Override
    public int hashCode() {
        return bits;
    }

    @Override
    public String toString() {
        List<Card> cards = HandOperations.decode(bits);
        return cards.toString();
    }
}
