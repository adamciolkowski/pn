package pan;

import java.util.ArrayList;
import java.util.List;

import static pan.Rank.*;
import static pan.Rank.ACE;

public class Main {

    public static void main(String[] args) {
        List<Card> hand = List.of(
                Card.of(QUEEN, Suit.DIAMONDS),
                Card.of(ACE, Suit.CLUBS),
                Card.of(ACE, Suit.DIAMONDS)
        );
        List<Card> stack = List.of(
                Card.of(NINE, Suit.HEARTS),
                Card.of(NINE, Suit.SPADES),
                Card.of(NINE, Suit.CLUBS),
                Card.of(NINE, Suit.DIAMONDS),
                Card.of(TEN, Suit.SPADES),
                Card.of(TEN, Suit.DIAMONDS),
                Card.of(TEN, Suit.CLUBS),
                Card.of(TEN, Suit.HEARTS),
                Card.of(JACK, Suit.SPADES),
                Card.of(JACK, Suit.DIAMONDS),
                Card.of(KING, Suit.CLUBS),
                Card.of(KING, Suit.HEARTS),
                Card.of(KING, Suit.SPADES),
                Card.of(KING, Suit.DIAMONDS)

        );
        List<Card> opponent = allMinus(hand, stack);

        PanState state = new PanState(hand, opponent, stack, true);
        int depth = 18;
        PanEvaluator evaluator = new PanEvaluator();
        long start = System.currentTimeMillis();
        int score = evaluator.minimax(state, depth, true);
        long time = System.currentTimeMillis() - start;
        System.out.println(score);
        System.out.println(evaluator.getEvaluatedNodes());
        System.out.println("took " + time + " ms");
    }

    private static List<Card> allMinus(List<Card> hand, List<Card> stack) {
        List<Card> all = new ArrayList<>(24);
        for (Rank rank : Rank.values()) {
            for (Suit suit : Suit.values()) {
                all.add(Card.of(rank, suit));
            }
        }
        all.removeAll(hand);
        all.removeAll(stack);
        return all;
    }
}
