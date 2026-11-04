package pan;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Stack {

    private Rank[] cards;

    public static Stack encode(List<Card> stack) {
        Rank[] ranks = new Rank[stack.size()];
        for (int i = 0; i < stack.size(); i++) {
            ranks[i] = stack.get(i).rank();
        }
        return new Stack(ranks);
    }

    public Stack copy() {
        return new Stack(cards.clone());
    }

    private Stack(Rank[] cards) {
        this.cards = cards;
    }

    public boolean isEmpty() {
        return cards.length == 0;
    }

    public int size() {
        return cards.length;
    }

    public Stack addCard(Rank rank) {
        Rank[] next = Arrays.copyOf(cards, cards.length + 1);
        next[cards.length] = rank;
        return new Stack(next);
    }

    public Stack addFourCards(Rank rank) {
        Rank[] next = Arrays.copyOf(cards, cards.length + 4);
        Arrays.fill(next, cards.length, next.length, rank);
        return new Stack(next);
    }

    public Stack removeTop(int count) {
        if (count == 0) {
            return this;
        }
        return new Stack(Arrays.copyOf(cards, cards.length - count));
    }

    List<Card> decode() {
        List<Card> decoded = new ArrayList<>(cards.length);
        for (Rank rank : cards) {
            decoded.add(RankCounts.canonicalCard(rank));
        }
        return decoded;
    }

    Rank peekRank() {
        assert !isEmpty();
        return cards[cards.length - 1];
    }

    Card peek() {
        return RankCounts.canonicalCard(peekRank());
    }

    RankCounts MUTABLE_popThree() {
        List<Rank> taken = new ArrayList<>();
        while (taken.size() < 3 && size() > 1) {
            taken.add(MUTABLE_popRank());
        }
        return RankCounts.fromRanks(taken);
    }

    private Rank MUTABLE_popRank() {
        Rank rank = cards[cards.length - 1];
        cards = Arrays.copyOf(cards, cards.length - 1);
        return rank;
    }

    CardSet cardSet() {
        return CardSet.from(decode());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Stack stack = (Stack) o;
        return Arrays.equals(cards, stack.cards);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(cards);
    }

    @Override
    public String toString() {
        return decode().toString();
    }
}
