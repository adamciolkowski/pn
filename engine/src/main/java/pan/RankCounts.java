package pan;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

public class RankCounts {

    private static final Rank[] RANKS = Rank.values();
    private static final Suit[] SUITS = Suit.values();

    private final int[] counts;

    private RankCounts(int[] counts) {
        this.counts = counts;
    }

    public static RankCounts fromRanks(List<Rank> ranks) {
        int[] counts = new int[RANKS.length];
        for (Rank rank : ranks) {
            counts[rank.ordinal()]++;
        }
        return new RankCounts(counts);
    }

    public static RankCounts from(List<Card> cards) {
        int[] counts = new int[RANKS.length];
        for (Card card : cards) {
            counts[card.rank().ordinal()]++;
        }
        return new RankCounts(counts);
    }

    public boolean isEmpty() {
        for (int count : counts) {
            if (count != 0) {
                return false;
            }
        }
        return true;
    }

    public int totalCount() {
        int total = 0;
        for (int count : counts) {
            total += count;
        }
        return total;
    }

    public int count(Rank rank) {
        return counts[rank.ordinal()];
    }

    public RankCounts removeRank(Rank rank) {
        int[] copy = counts.clone();
        copy[rank.ordinal()]--;
        return new RankCounts(copy);
    }

    public RankCounts removeCard(Card card) {
        return removeRank(card.rank());
    }

    public boolean hasFourNines() {
        return counts[Rank.NINE.ordinal()] == 4;
    }

    public RankCounts removeFourCards(Rank rank) {
        int[] copy = counts.clone();
        copy[rank.ordinal()] = 0;
        return new RankCounts(copy);
    }

    public RankCounts add(RankCounts other) {
        int[] copy = counts.clone();
        for (int i = 0; i < copy.length; i++) {
            copy[i] += other.counts[i];
        }
        return new RankCounts(copy);
    }

    public RankCounts add(CardSet more) {
        int[] copy = counts.clone();
        for (Iterator<Card> cards = more.cards(); cards.hasNext(); ) {
            Card card = cards.next();
            copy[card.rank().ordinal()]++;
        }
        return new RankCounts(copy);
    }

    public List<Rank> fourCards() {
        List<Rank> ranks = new ArrayList<>();
        for (int i = 0; i < counts.length; i++) {
            if (counts[i] == 4) {
                ranks.add(RANKS[i]);
            }
        }
        return ranks;
    }

    public Iterator<Card> cards() {
        return new Iterator<>() {
            int rankIdx = 0;
            int instance = 0;

            @Override
            public boolean hasNext() {
                while (rankIdx < counts.length && instance >= counts[rankIdx]) {
                    rankIdx++;
                    instance = 0;
                }
                return rankIdx < counts.length;
            }

            @Override
            public Card next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                Card card = canonicalCard(RANKS[rankIdx], instance);
                instance++;
                return card;
            }
        };
    }

    public List<Card> getCards() {
        List<Card> cards = new ArrayList<>();
        for (Iterator<Card> it = cards(); it.hasNext(); ) {
            cards.add(it.next());
        }
        return cards;
    }

    public CardSet toCardSet() {
        return CardSet.from(getCards());
    }

    static Card canonicalCard(Rank rank) {
        return Card.of(rank, Suit.HEARTS);
    }

    static Card canonicalCard(Rank rank, int instance) {
        return Card.of(rank, SUITS[instance % SUITS.length]);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        RankCounts that = (RankCounts) o;
        return Arrays.equals(counts, that.counts);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(counts);
    }

    @Override
    public String toString() {
        return getCards().toString();
    }
}
