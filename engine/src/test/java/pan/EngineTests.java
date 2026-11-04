package pan;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class EngineTests {

    @Nested
    class FinalCases {

        @Test
        void winInOne() {
            PanEvaluator panEvaluator = new PanEvaluator();
            int maxDepth = 5;
            int score = panEvaluator.evaluate(new PanState(
                    List.of(Card.of(Rank.ACE, Suit.DIAMONDS)),
                    List.of(Card.of(Rank.ACE, Suit.SPADES)),
                    List.of(Card.of(Rank.NINE, Suit.HEARTS)),
                    true
            ), maxDepth, true);

            assertEquals("win in 1 plies", printScore(score, maxDepth));
        }

        @Test
        void winInTwo() {
            PanEvaluator panEvaluator = new PanEvaluator();
            int maxDepth = 5;
            int score = panEvaluator.evaluate(new PanState(
                    List.of(Card.of(Rank.ACE, Suit.DIAMONDS)),
                    List.of(Card.of(Rank.ACE, Suit.SPADES), Card.of(Rank.ACE, Suit.HEARTS)),
                    List.of(Card.of(Rank.NINE, Suit.HEARTS)),
                    false
            ), maxDepth, false);

            assertEquals("win in 2 plies", printScore(score, maxDepth));
        }

        @Test
        void winInThree() {
            PanEvaluator panEvaluator = new PanEvaluator();
            int maxDepth = 5;
            int score = panEvaluator.evaluate(new PanState(
                    List.of(Card.of(Rank.ACE, Suit.DIAMONDS), Card.of(Rank.ACE, Suit.CLUBS)),
                    List.of(Card.of(Rank.ACE, Suit.SPADES), Card.of(Rank.ACE, Suit.HEARTS)),
                    List.of(Card.of(Rank.NINE, Suit.HEARTS)),
                    true
            ), maxDepth, true);

            assertEquals("win in 3 plies", printScore(score, maxDepth));
        }

    }

    @Nested
    class CanYouWin {

        PanEvaluator panEvaluator = new PanEvaluator();

        @Test
        void QA_vs_KKKKAA_wins_in_13() {
            List<Card> hand = List.of(
                    Card.of(Rank.QUEEN, Suit.DIAMONDS),
                    Card.of(Rank.ACE, Suit.CLUBS)
            );
            List<Card> stack = List.of(
                    Card.of(Rank.NINE, Suit.HEARTS),
                    Card.of(Rank.NINE, Suit.SPADES),
                    Card.of(Rank.NINE, Suit.CLUBS),
                    Card.of(Rank.NINE, Suit.DIAMONDS),
                    Card.of(Rank.TEN, Suit.SPADES),
                    Card.of(Rank.TEN, Suit.DIAMONDS),
                    Card.of(Rank.TEN, Suit.CLUBS),
                    Card.of(Rank.TEN, Suit.HEARTS),
                    Card.of(Rank.JACK, Suit.SPADES),
                    Card.of(Rank.JACK, Suit.DIAMONDS),
                    Card.of(Rank.KING, Suit.CLUBS),
                    Card.of(Rank.KING, Suit.HEARTS),
                    Card.of(Rank.KING, Suit.SPADES),
                    Card.of(Rank.KING, Suit.DIAMONDS),
                    Card.of(Rank.ACE, Suit.DIAMONDS),
                    Card.of(Rank.ACE, Suit.HEARTS)
            );
            List<Card> opponent = allMinus(hand, stack);

            PanState state = new PanState(hand, opponent, stack, true);
            int depth = 17;
            PanEvaluator.MoveAndScore mas = panEvaluator.findBestMove(state, depth);

            assertThat(mas.move()).isEqualTo(PanMove.Take.INSTANCE);
            assertThat(printScore(mas.score(), depth)).isEqualTo("win in 13 plies");
        }

        @Test
        void QAA_vs_KKKK_wins_in_29() {
            List<Card> hand = List.of(
                    Card.of(Rank.QUEEN, Suit.DIAMONDS),
                    Card.of(Rank.ACE, Suit.HEARTS),
                    Card.of(Rank.ACE, Suit.DIAMONDS)
            );
            List<Card> stack = List.of(
                    Card.of(Rank.NINE, Suit.HEARTS),
                    Card.of(Rank.NINE, Suit.SPADES),
                    Card.of(Rank.NINE, Suit.CLUBS),
                    Card.of(Rank.NINE, Suit.DIAMONDS),
                    Card.of(Rank.TEN, Suit.SPADES),
                    Card.of(Rank.TEN, Suit.DIAMONDS),
                    Card.of(Rank.TEN, Suit.CLUBS),
                    Card.of(Rank.TEN, Suit.HEARTS),
                    Card.of(Rank.JACK, Suit.SPADES),
                    Card.of(Rank.JACK, Suit.DIAMONDS),
                    Card.of(Rank.KING, Suit.DIAMONDS),
                    Card.of(Rank.KING, Suit.HEARTS),
                    Card.of(Rank.KING, Suit.SPADES),
                    Card.of(Rank.KING, Suit.CLUBS)
            );
            List<Card> opponent = allMinus(hand, stack);

            PanState state = new PanState(hand, opponent, stack, true);
            int depth = 29;
            PanEvaluator.MoveAndScore mas = panEvaluator.findBestMove(state, depth);

            assertThat(mas.move()).isEqualTo(new PanMove.OneCard(Card.of(Rank.ACE, Suit.HEARTS)));
            assertThat(printScore(mas.score(), depth)).isEqualTo("win in 29 plies");
        }

        @Disabled("unsolved for depth <= 29")
        @Test
        void ten_and_ace_with_99J_on_stack() {
            long start = System.currentTimeMillis();
            int move = panEvaluator.evaluate(new PanState(
                    List.of(
                            Card.of(Rank.TEN, Suit.SPADES),
                            Card.of(Rank.ACE, Suit.DIAMONDS)
                    ),
                    List.of(
                            Card.of(Rank.NINE, Suit.CLUBS),
                            Card.of(Rank.NINE, Suit.SPADES),

                            Card.of(Rank.TEN, Suit.HEARTS),
                            Card.of(Rank.TEN, Suit.CLUBS),
                            Card.of(Rank.TEN, Suit.DIAMONDS),

                            Card.of(Rank.JACK, Suit.HEARTS),
                            Card.of(Rank.JACK, Suit.CLUBS),
                            Card.of(Rank.JACK, Suit.DIAMONDS),

                            Card.of(Rank.QUEEN, Suit.HEARTS),
                            Card.of(Rank.QUEEN, Suit.CLUBS),
                            Card.of(Rank.QUEEN, Suit.DIAMONDS),
                            Card.of(Rank.QUEEN, Suit.SPADES),

                            Card.of(Rank.KING, Suit.HEARTS),
                            Card.of(Rank.KING, Suit.CLUBS),
                            Card.of(Rank.KING, Suit.DIAMONDS),
                            Card.of(Rank.KING, Suit.SPADES),

                            Card.of(Rank.ACE, Suit.HEARTS),
                            Card.of(Rank.ACE, Suit.CLUBS),
                            Card.of(Rank.ACE, Suit.SPADES)
                    ),
                    List.of(
                            Card.of(Rank.NINE, Suit.HEARTS),
                            Card.of(Rank.NINE, Suit.DIAMONDS),
                            Card.of(Rank.JACK, Suit.SPADES)
                    ), true
            ), 29, true);
            long time = System.currentTimeMillis() - start;
            System.out.println(move);

            long evaluatedNodes = panEvaluator.getEvaluatedNodes();
            long nodesPerSec = evaluatedNodes / time;
            System.out.println("Evaluated " + evaluatedNodes + " nodes in " + time + " ms (" + nodesPerSec + " knodes/s)");
        }

        @Disabled("unsolved for depth <= 25")
        @Test
        void _9_10_10_J_Q_KKK_AAA() {
            List<Card> hand = List.of(
                    Card.of(Rank.NINE, Suit.DIAMONDS),
                    Card.of(Rank.TEN, Suit.HEARTS),
                    Card.of(Rank.TEN, Suit.SPADES),
                    Card.of(Rank.JACK, Suit.CLUBS),
                    Card.of(Rank.QUEEN, Suit.HEARTS),
                    Card.of(Rank.KING, Suit.CLUBS),
                    Card.of(Rank.KING, Suit.DIAMONDS),
                    Card.of(Rank.KING, Suit.SPADES),
                    Card.of(Rank.ACE, Suit.CLUBS),
                    Card.of(Rank.ACE, Suit.DIAMONDS),
                    Card.of(Rank.ACE, Suit.SPADES)
            );
            List<Card> stack = List.of(
                    Card.of(Rank.NINE, Suit.HEARTS),
                    Card.of(Rank.NINE, Suit.SPADES),
                    Card.of(Rank.NINE, Suit.CLUBS)
            );
            List<Card> opponent = allMinus(hand, stack);

            PanEvaluator.MoveAndScore mas = panEvaluator.findBestMove(new PanState(hand, opponent, stack, true), 25);
            System.out.println(mas);
        }

        @Disabled
        @Test
        void canYouWin2() {
            long start = System.currentTimeMillis();
            PanEvaluator.MoveAndScore move = panEvaluator.findBestMove(new PanState(
                    List.of(
                            Card.of(Rank.TEN, Suit.SPADES),
                            Card.of(Rank.ACE, Suit.DIAMONDS)
                    ),
                    List.of(
                            Card.of(Rank.NINE, Suit.CLUBS),
                            Card.of(Rank.NINE, Suit.SPADES),

                            Card.of(Rank.TEN, Suit.HEARTS),
                            Card.of(Rank.TEN, Suit.CLUBS),
                            Card.of(Rank.TEN, Suit.DIAMONDS),

                            Card.of(Rank.JACK, Suit.HEARTS),
                            Card.of(Rank.JACK, Suit.CLUBS),
                            Card.of(Rank.JACK, Suit.DIAMONDS),

                            Card.of(Rank.QUEEN, Suit.HEARTS),
                            Card.of(Rank.QUEEN, Suit.CLUBS),
                            Card.of(Rank.QUEEN, Suit.DIAMONDS),
                            Card.of(Rank.QUEEN, Suit.SPADES),

                            Card.of(Rank.KING, Suit.HEARTS),
                            Card.of(Rank.KING, Suit.CLUBS),
                            Card.of(Rank.KING, Suit.DIAMONDS),
                            Card.of(Rank.KING, Suit.SPADES),

                            Card.of(Rank.ACE, Suit.HEARTS),
                            Card.of(Rank.ACE, Suit.CLUBS),
                            Card.of(Rank.ACE, Suit.SPADES)
                    ),
                    List.of(
                            Card.of(Rank.NINE, Suit.HEARTS),
                            Card.of(Rank.NINE, Suit.DIAMONDS),
                            Card.of(Rank.JACK, Suit.SPADES)
                    ), true
            ), 5);
            long time = System.currentTimeMillis() - start;
            System.out.println(move);

            long evaluatedNodes = panEvaluator.getEvaluatedNodes();
            long nodesPerSec = evaluatedNodes / time;
            System.out.println("Evaluated " + evaluatedNodes + " nodes in " + time + " ms (" + nodesPerSec + " knodes/s)");
        }

        @Disabled
        @Test
        void queenAce() {
            long start = System.currentTimeMillis();
            int score = panEvaluator.evaluate(new PanState(
                    List.of(
                            Card.of(Rank.QUEEN, Suit.SPADES),
                            Card.of(Rank.ACE, Suit.DIAMONDS)
                    ),
                    List.of(
                            Card.of(Rank.JACK, Suit.HEARTS),
                            Card.of(Rank.JACK, Suit.CLUBS),
                            Card.of(Rank.JACK, Suit.DIAMONDS),
                            Card.of(Rank.JACK, Suit.SPADES),

                            Card.of(Rank.QUEEN, Suit.HEARTS),
                            Card.of(Rank.QUEEN, Suit.CLUBS),
                            Card.of(Rank.QUEEN, Suit.DIAMONDS),
                            Card.of(Rank.QUEEN, Suit.SPADES),

                            Card.of(Rank.KING, Suit.HEARTS),
                            Card.of(Rank.KING, Suit.CLUBS),
                            Card.of(Rank.KING, Suit.DIAMONDS),
                            Card.of(Rank.KING, Suit.SPADES),

                            Card.of(Rank.ACE, Suit.HEARTS),
                            Card.of(Rank.ACE, Suit.CLUBS),
                            Card.of(Rank.ACE, Suit.SPADES)
                    ),
                    List.of(
                            Card.of(Rank.NINE, Suit.HEARTS),
                            Card.of(Rank.NINE, Suit.DIAMONDS),
                            Card.of(Rank.NINE, Suit.SPADES),
                            Card.of(Rank.NINE, Suit.CLUBS),
                            Card.of(Rank.TEN, Suit.HEARTS),
                            Card.of(Rank.TEN, Suit.CLUBS),
                            Card.of(Rank.TEN, Suit.DIAMONDS),
                            Card.of(Rank.TEN, Suit.SPADES)
                    ), false
            ), 10, false);
            long time = System.currentTimeMillis() - start;
            System.out.println(score);

            long evaluatedNodes = panEvaluator.getEvaluatedNodes();
            long nodesPerSec = evaluatedNodes / time;
            System.out.println("Evaluated " + evaluatedNodes + " nodes in " + time + " ms (" + nodesPerSec + " knodes/s)");
        }
    }

    private List<Card> allMinus(List<Card> hand, List<Card> stack) {
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

    private static String printScore(int score, int maxDepth) {
        if (score > 0) {
            return "win in " + (100 - score + maxDepth) + " plies";
        }
        if (score < 0) {
            return "lose in " + (100 + score + maxDepth) + " plies";
        }
        return "?";
    }
}
