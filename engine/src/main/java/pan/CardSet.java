package pan;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class CardSet {

    private static final Rank[] RANKS = Rank.values();

    private static final CardSet EMPTY = new CardSet(0);
    final int bits;

    public static CardSet empty() {
        return EMPTY;
    }

    public static CardSet from(List<Card> cards) {
        return new CardSet(HandOperations.encode(cards));
    }

    CardSet(int bits) {
        this.bits = bits;
    }

    public boolean isEmpty() {
        return HandOperations.isEmpty(bits);
    }

    public CardSet add(CardSet cards) {
        assert (bits & cards.bits) == 0;
        return new CardSet(bits | cards.bits);
    }

    public CardSet removeCard(Card card) {
        return new CardSet(HandOperations.removeCard(bits, card));
    }

    public boolean hasFourNines() {
        return HandOperations.hasFourNines(bits);
    }

    public CardSet removeFourCards(Rank rank) {
        return new CardSet(HandOperations.removeFourCards(bits, rank));
    }

    public int count() {
        return HandOperations.count(bits);
    }

    public List<Rank> fourCards() {
//        List<Rank> ranks = new ArrayList<>(RANKS.length - 1);
//        for (int i = 0; i < MAX_CARDS; i += 4) {
//            int fourCardsMask = FOUR_CARDS_MASK << i;
//            if ((bits & fourCardsMask) != fourCardsMask) {
//                continue;
//            }
//            ranks.add(RANKS[i / 4]);
//        }
//        return ranks;
        List<Rank> ranks = new ArrayList<>(6);
        for (int i = 0; i < 24; i += 4) {
            int fourCardsMask = 0b1111 << i;
            if ((bits & fourCardsMask) != fourCardsMask) {
                continue;
            }
            ranks.add(RANKS[i / 4]);
        }
        return ranks;
    }

    public Iterator<Card> cards() {
        return new Iterator<>() {

            int remaining = bits;

            @Override
            public boolean hasNext() {
                return remaining > 0;
            }

            @Override
            public Card next() {
                int index = Integer.numberOfTrailingZeros(remaining);
                remaining &= remaining - 1; // clear lowest set bit
                return HandOperations.cardFromIndex(index);
            }
        };
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CardSet hand = (CardSet) o;
        return bits == hand.bits;
    }

    public List<Card> getCards() {
        return HandOperations.decode(bits);
    }

    @Override
    public int hashCode() {
        return bits;
    }

    @Override
    public String toString() {
        return getCards().toString();
    }
}
