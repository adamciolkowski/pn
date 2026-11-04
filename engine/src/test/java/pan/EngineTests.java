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

    @Disabled
    @Nested
    class Evaluation {

        PanEvaluator panEvaluator = new PanEvaluator();

        @Test
        void lessCardsIsBetter() {
            PanState state = new PanState(
                    List.of(Card.of(Rank.ACE, Suit.DIAMONDS)),
                    List.of(Card.of(Rank.ACE, Suit.SPADES), Card.of(Rank.ACE, Suit.CLUBS)),
                    List.of(Card.of(Rank.NINE, Suit.HEARTS)),
                    true
            );

            verify(state);
        }

        @Test
        void fourOfAKindBetterThanTwoDifferent() {
            PanState state = new PanState(
                    List.of(Card.of(Rank.ACE, Suit.DIAMONDS)),
                    List.of(Card.of(Rank.ACE, Suit.SPADES), Card.of(Rank.ACE, Suit.CLUBS)),
                    List.of(Card.of(Rank.NINE, Suit.HEARTS)),
                    true
            );

            verify(state);
        }

        @Test
        void havingCardsLowerThanTopOfStackIsWorse() {
            PanState state = new PanState(
                    List.of(Card.of(Rank.NINE, Suit.DIAMONDS)),
                    List.of(Card.of(Rank.ACE, Suit.SPADES), Card.of(Rank.ACE, Suit.CLUBS)),
                    List.of(Card.of(Rank.NINE, Suit.HEARTS), Card.of(Rank.QUEEN, Suit.HEARTS)),
                    true
            );

            verify(state);
        }

        private void verify(PanState state) {
            int evaluationScore = panEvaluator.evaluate(state, 0, true);
            int actualScore = panEvaluator.evaluate(state, 5, true);

            assertEquals(Integer.signum(actualScore), Integer.signum(evaluationScore));
        }
    }


    @Nested
    class CanYouWin {

        PanEvaluator panEvaluator = new PanEvaluator();

        /**
         * Evaluated 20409699 nodes in 6035 ms (3381 knodes/s)
         * Cache hits 0 (0.0)
         * 87411081
         */
        @Disabled
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
            ), 11, true);
            long time = System.currentTimeMillis() - start;
            System.out.println(move);

            int evaluatedNodes = panEvaluator.getEvaluatedNodes();
            long nodesPerSec = evaluatedNodes / time;
            System.out.println("Evaluated " + evaluatedNodes + " nodes in " + time + " ms (" + nodesPerSec + " knodes/s)");
            System.out.println("Cache hits " + panEvaluator.getCacheHits() + " (" + ((float) panEvaluator.getCacheHits() / evaluatedNodes * 100) + ')');
        }

        /**
         * nodes: 161147158
         * depth: 11
         * score: 0
         */
        @Disabled
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

            long start = System.currentTimeMillis();
            int move = panEvaluator.evaluate(new PanState(
                    hand,
                    opponent,
                    stack,
                    true
            ), 10, true);
            long time = System.currentTimeMillis() - start;
            System.out.println(move);

            int evaluatedNodes = panEvaluator.getEvaluatedNodes();
            long nodesPerSec = evaluatedNodes / time;
            System.out.println("Evaluated " + evaluatedNodes + " nodes in " + time + " ms (" + nodesPerSec + " knodes/s)");
            System.out.println("Cache hits " + panEvaluator.getCacheHits() + " (" + ((float) panEvaluator.getCacheHits() / evaluatedNodes * 100) + ')');
        }

        /**
         * Evaluated 989800723 nodes in 60312 ms (16411 knodes/s)
         */
        @Disabled
        @Test
        void QAA() {
            List<Card> hand = List.of(
                    Card.of(Rank.ACE, Suit.DIAMONDS),
                    Card.of(Rank.ACE, Suit.SPADES),
                    Card.of(Rank.ACE, Suit.HEARTS),
                    Card.of(Rank.ACE, Suit.CLUBS)
            );
            List<Card> stack = List.of(
                    Card.of(Rank.NINE, Suit.HEARTS),
                    Card.of(Rank.NINE, Suit.SPADES),
                    Card.of(Rank.NINE, Suit.CLUBS),
                    Card.of(Rank.NINE, Suit.DIAMONDS)
//                    Card.of(KING, HEARTS),
//                    Card.of(KING, CLUBS),
//                    Card.of(KING, DIAMONDS),
//                    Card.of(KING, SPADES)
            );
            List<Card> opponent = allMinus(hand, stack);

            long start = System.currentTimeMillis();
            PanState state = new PanState(
                    hand,
                    opponent,
                    stack,
                    true
            );
            int score = panEvaluator.evaluate(state, 12, true);
            long time = System.currentTimeMillis() - start;
//            System.out.println(printScore(score, ));

            int evaluatedNodes = panEvaluator.getEvaluatedNodes();
            long nodesPerSec = evaluatedNodes / time;
            System.out.println("Evaluated " + evaluatedNodes + " nodes in " + time + " ms (" + nodesPerSec + " knodes/s)");
            System.out.println("Cache hits " + panEvaluator.getCacheHits() + " (" + ((float) panEvaluator.getCacheHits() / evaluatedNodes * 100) + ')');
        }

        /**
         * Evaluated 265793762 nodes in 16547 ms (16062 knodes/s), win in 12 plies
         */
        @Disabled
        @Test
        void smaller() {
            List<Card> hand = List.of(
                    Card.of(Rank.QUEEN, Suit.HEARTS),
                    Card.of(Rank.ACE, Suit.SPADES)
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
                    Card.of(Rank.QUEEN, Suit.DIAMONDS),
                    Card.of(Rank.KING, Suit.SPADES),
                    Card.of(Rank.KING, Suit.CLUBS),
                    Card.of(Rank.KING, Suit.DIAMONDS),
                    Card.of(Rank.KING, Suit.HEARTS),
                    Card.of(Rank.ACE, Suit.CLUBS),
                    Card.of(Rank.ACE, Suit.DIAMONDS)
            );
            List<Card> opponent = allMinus(hand, stack);

            PanEvaluator panEvaluator = new PanEvaluator();
            long start = System.currentTimeMillis();
            boolean playersTurn = true;
            PanState state = new PanState(hand, opponent, stack, playersTurn);
            int depth = 17;
            int score = panEvaluator.evaluate(state, depth, playersTurn);
            long time = System.currentTimeMillis() - start;
            System.out.println("score: " + score);

            int evaluatedNodes = panEvaluator.getEvaluatedNodes();
            long nodesPerSec = evaluatedNodes / time;
            System.out.println("Evaluated " + evaluatedNodes + " nodes in " + time + " ms (" + nodesPerSec + " knodes/s)");
            System.out.println("Cache hits " + panEvaluator.getCacheHits() + " (" + ((float) panEvaluator.getCacheHits() / evaluatedNodes * 100) + ')');
            System.out.println(printScore(score, depth));
            System.out.println(panEvaluator.findBestMove(state, depth).move());
        }

        @Test
        void QAA_wins_vs_KKKK() {
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

            boolean playersTurn = true;
            PanState state = new PanState(hand, opponent, stack, playersTurn);
            int depth = 18;
            int score = panEvaluator.evaluate(state, depth, playersTurn);
            System.out.println("score: " + score);

            System.out.println(printScore(score, depth));
            assertThat(score).isGreaterThan(0);
            PanEvaluator.MoveAndScore bestMove = panEvaluator.findBestMove(state, depth);
            System.out.println(bestMove);
        }

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
            System.out.println(state.getId());
            int depth = 17;
            PanEvaluator.MoveAndScore mas = panEvaluator.findBestMove(state, depth);
            System.out.println("score: " + mas.score() + " (" + mas.move() + ')');

            System.out.println(printScore(mas.score(), depth));
            assertThat(mas.score()).isGreaterThan(0);
        }

        @Test
        void QA_vs_KKKKAA_wins_in_13_NEGATED() {
            List<Card> opponent = List.of(
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
                    Card.of(Rank.ACE, Suit.DIAMONDS)
            );
            List<Card> hand = allMinus(opponent, stack);

            PanState state = new PanState(hand, opponent, stack, true);
            int depth = 19;
            int score = panEvaluator.evaluate(state, depth, true);
            System.out.println(score);
//            PanEvaluator.MoveAndScore mas = panEvaluator.findBestMove(state, depth);
//            System.out.println("score: " + mas.score() + " (" + mas.move() + ')');
//
//            System.out.println(printScore(mas.score(), depth));
//            assertThat(mas.score()).isLessThan(0);
        }

        @Test
        void vs_Q_K_AAA_lose_in_11() {
            List<Card> opponent = List.of(
                    Card.of(Rank.QUEEN, Suit.DIAMONDS),
                    Card.of(Rank.KING, Suit.CLUBS),
                    Card.of(Rank.ACE, Suit.CLUBS),
                    Card.of(Rank.ACE, Suit.DIAMONDS),
                    Card.of(Rank.ACE, Suit.HEARTS)
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
                    Card.of(Rank.KING, Suit.SPADES)
            );
            List<Card> hand = allMinus(opponent, stack);

            boolean playersTurn = true;
            PanState state = new PanState(hand, opponent, stack, playersTurn);
            int depth = 17;
            int score = panEvaluator.evaluate(state, depth, playersTurn);
            System.out.println("score: " + score);

            System.out.println(printScore(score, depth));
            assertThat(score).isLessThan(0);
        }
    }

    @Disabled
    @Test
    void canYouWin2() {
        PanEvaluator panEvaluator = new PanEvaluator();
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

        int evaluatedNodes = panEvaluator.getEvaluatedNodes();
        long nodesPerSec = evaluatedNodes / time;
        System.out.println("Evaluated " + evaluatedNodes + " nodes in " + time + " ms (" + nodesPerSec + " knodes/s)");
        System.out.println("Cache hits " + panEvaluator.getCacheHits() + " (" + ((float) panEvaluator.getCacheHits() / evaluatedNodes * 100) + ')');
    }

    @Disabled
    @Test
    void queenAce() {
        PanEvaluator panEvaluator = new PanEvaluator();
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

        int evaluatedNodes = panEvaluator.getEvaluatedNodes();
        long nodesPerSec = evaluatedNodes / time;
        System.out.println("Evaluated " + evaluatedNodes + " nodes in " + time + " ms (" + nodesPerSec + " knodes/s)");
        System.out.println("Cache hits " + panEvaluator.getCacheHits() + " (" + ((float) panEvaluator.getCacheHits() / evaluatedNodes * 100) + ')');
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
