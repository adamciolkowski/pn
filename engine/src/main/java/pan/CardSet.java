package pan;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class CardSet {

    private static final int FOUR_CARDS_MASK = 0b1111;

    private static final Rank[] RANKS = Rank.values();
    private static final Suit[] SUITS = Suit.values();
    private static final int MAX_CARDS = RANKS.length * SUITS.length;

    private static final CardSet EMPTY = new CardSet(0);

    final int bits;

    public static CardSet empty() {
        return EMPTY;
    }

    public static CardSet from(List<Card> cards) {
        int bits = 0;
        for (Card card : cards) {
            bits |= 1 << bitIndex(card);
        }
        return new CardSet(bits);
    }

    CardSet(int bits) {
        this.bits = bits;
    }

    public boolean isEmpty() {
        return bits == 0;
    }

    public int count() {
        return Integer.bitCount(bits);
    }

    public CardSet add(Card card) {
        return new CardSet(bits | (1 << bitIndex(card)));
    }

    public CardSet add(CardSet other) {
        assert (bits & other.bits) == 0;
        return new CardSet(bits | other.bits);
    }

    public CardSet addFourCards(Rank rank) {
        int mask = FOUR_CARDS_MASK << (rank.ordinal() * 4);
        return new CardSet(bits | mask);
    }

    public CardSet remove(CardSet other) {
        return new CardSet(bits & ~other.bits);
    }

    public CardSet removeCard(Card card) {
        return new CardSet(bits & ~(1 << bitIndex(card)));
    }

    public CardSet removeFourCards(Rank rank) {
        int mask = FOUR_CARDS_MASK << (rank.ordinal() * 4);
        return new CardSet(bits & ~mask);
    }

    public boolean hasFourNines() {
        return (bits & FOUR_CARDS_MASK) == FOUR_CARDS_MASK;
    }

    CardSet intersect(CardSet other) {
        return new CardSet(bits & other.bits);
    }

    public List<Rank> fourCards() {
        List<Rank> ranks = new ArrayList<>(6);
        for (int i = 0; i < 24; i += 4) {
            int fourCardsMask = FOUR_CARDS_MASK << i;
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
                return cardFromIndex(index);
            }
        };
    }

    public List<Card> getCards() {
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < MAX_CARDS; i++) {
            if ((bits & (1 << i)) != 0) {
                cards.add(cardFromIndex(i));
            }
        }
        return cards;
    }

    static Card cardFromIndex(int idx) {
        Rank rank = RANKS[idx / 4];
        Suit suit = SUITS[idx % 4];
        return Card.of(rank, suit);
    }

    private static int bitIndex(Card card) {
        return card.rank().ordinal() * 4 + card.suit().ordinal();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CardSet other = (CardSet) o;
        return bits == other.bits;
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
