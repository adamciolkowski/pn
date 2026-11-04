package pan;

import java.util.ArrayList;
import java.util.List;

class HandOperations {

    private static final int FOUR_CARDS_MASK = 0b1111;

    private static final Rank[] RANKS = Rank.values();
    private static final Suit[] SUITS = Suit.values();
    private static final int MAX_CARDS = RANKS.length * SUITS.length;

    public static int encode(List<Card> cards) {
        int bits = 0;
        for (Card card : cards) {
            bits = addCard(bits, card);
        }
        return bits;
    }

    public static int addCard(int hand, Card card) {
        int idx = bitIndex(card);
        return hand | (1 << idx);
    }

    public static int addFourCards(int hand, Rank rank) {
        int mask = FOUR_CARDS_MASK << (rank.ordinal() * 4);
        return hand | mask;
    }

    public static int removeFourCards(int hand, Rank rank) {
        int mask = FOUR_CARDS_MASK << (rank.ordinal() * 4);
        return hand & ~mask;
    }

    public static boolean contains(int bits, Card card) {
        int idx = bitIndex(card);
        return (bits & (1 << idx)) != 0;
    }

    public static boolean isEmpty(int cards) {
        return cards == 0;
    }

    public static int count(int cards) {
        return Integer.bitCount(cards);
    }

    public static int removeCard(int bitset, Card card) {
        int bitIdx = bitIndex(card);
        return bitset & ~(1 << bitIdx);
    }

    public static boolean hasFourNines(int cards) {
        return (cards & FOUR_CARDS_MASK) == FOUR_CARDS_MASK;
    }

    private static int bitIndex(Card card) {
        return card.rank().ordinal() * 4 + card.suit().ordinal();
    }

    public static List<Card> decode(int player) {
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < MAX_CARDS; i++) {
            if ((player & (1 << i)) != 0) {
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
}
