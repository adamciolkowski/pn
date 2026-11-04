package pan;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class PanStateTest {

    @Test
    void playerWithNineOfHeartsHasToPlayThatCard() {
        PanState state = new PanState(
                List.of(
                        Card.of(Rank.NINE, Suit.HEARTS),
                        Card.of(Rank.NINE, Suit.SPADES),
                        Card.of(Rank.JACK, Suit.DIAMONDS)
                ),
                List.of(
                        Card.of(Rank.NINE, Suit.CLUBS),
                        Card.of(Rank.TEN, Suit.SPADES),
                        Card.of(Rank.JACK, Suit.HEARTS)
                ),
                stack(),
                true
        );

        List<PanMove> legalMoves = state.getLegalMoves();

        assertEquals(List.of(
                new PanMove.OneCard(Card.of(Rank.NINE, Suit.HEARTS))
        ), legalMoves);
    }

    @Test
    void playNineOfHearts() {
        PanState state = new PanState(
                List.of(
                        Card.of(Rank.NINE, Suit.HEARTS),
                        Card.of(Rank.NINE, Suit.SPADES),
                        Card.of(Rank.JACK, Suit.DIAMONDS)
                ),
                List.of(
                        Card.of(Rank.NINE, Suit.CLUBS),
                        Card.of(Rank.TEN, Suit.SPADES),
                        Card.of(Rank.JACK, Suit.HEARTS)
                ),
                stack(),
                true
        );

        PanState afterMove = state.move(new PanMove.OneCard(Card.of(Rank.NINE, Suit.HEARTS)));

        assertThat(afterMove).isEqualTo(new PanState(
                List.of(
                        Card.of(Rank.NINE, Suit.SPADES),
                        Card.of(Rank.JACK, Suit.DIAMONDS)
                ),
                List.of(
                        Card.of(Rank.NINE, Suit.CLUBS),
                        Card.of(Rank.TEN, Suit.SPADES),
                        Card.of(Rank.JACK, Suit.HEARTS)
                ),
                stack(Card.of(Rank.NINE, Suit.HEARTS)),
                false
        ));
    }

    @Test
    void startingPlayerCanPlayFourNines() {
        PanState state = new PanState(
                List.of(
                        Card.of(Rank.NINE, Suit.CLUBS),
                        Card.of(Rank.NINE, Suit.DIAMONDS),
                        Card.of(Rank.NINE, Suit.HEARTS),
                        Card.of(Rank.NINE, Suit.SPADES),
                        Card.of(Rank.TEN, Suit.CLUBS),
                        Card.of(Rank.TEN, Suit.DIAMONDS),
                        Card.of(Rank.TEN, Suit.HEARTS),
                        Card.of(Rank.TEN, Suit.SPADES)
                ),
                List.of(
                        Card.of(Rank.JACK, Suit.DIAMONDS)
                ),
                stack(),
                true
        );

        List<PanMove> legalMoves = state.getLegalMoves();

        assertThat(legalMoves).containsExactlyInAnyOrder(
                new PanMove.OneCard(Card.of(Rank.NINE, Suit.HEARTS)),
                new PanMove.FourCards(Rank.NINE)
        );
    }

    @Test
    void canOnlyPlaySameOrOlderCards() {
        PanState state = new PanState(
                List.of(
                        Card.of(Rank.NINE, Suit.CLUBS),
                        Card.of(Rank.TEN, Suit.DIAMONDS),
                        Card.of(Rank.JACK, Suit.HEARTS),
                        Card.of(Rank.QUEEN, Suit.SPADES),
                        Card.of(Rank.KING, Suit.CLUBS)
                ),
                List.of(
                        Card.of(Rank.NINE, Suit.DIAMONDS),
                        Card.of(Rank.JACK, Suit.DIAMONDS)
                ),
                stack(
                        Card.of(Rank.NINE, Suit.HEARTS),
                        Card.of(Rank.QUEEN, Suit.DIAMONDS)
                ),
                true
        );

        assertThat(state.getLegalMoves()).containsExactlyInAnyOrder(
                new PanMove.OneCard(Card.of(Rank.QUEEN, Suit.HEARTS)),
                new PanMove.OneCard(Card.of(Rank.KING, Suit.HEARTS)),
                PanMove.Take.INSTANCE
        );
    }

    @Test
    void canPlayFourCardsOfEqualRank() {
        PanState state = new PanState(
                List.of(
                        Card.of(Rank.JACK, Suit.CLUBS),
                        Card.of(Rank.JACK, Suit.DIAMONDS),
                        Card.of(Rank.JACK, Suit.HEARTS),
                        Card.of(Rank.JACK, Suit.SPADES),
                        Card.of(Rank.KING, Suit.CLUBS),
                        Card.of(Rank.KING, Suit.DIAMONDS),
                        Card.of(Rank.KING, Suit.HEARTS),
                        Card.of(Rank.KING, Suit.SPADES),
                        Card.of(Rank.ACE, Suit.CLUBS),
                        Card.of(Rank.ACE, Suit.DIAMONDS),
                        Card.of(Rank.ACE, Suit.HEARTS),
                        Card.of(Rank.ACE, Suit.SPADES)
                ),
                List.of(
                        Card.of(Rank.NINE, Suit.DIAMONDS),
                        Card.of(Rank.QUEEN, Suit.CLUBS)
                ),
                stack(
                        Card.of(Rank.NINE, Suit.HEARTS),
                        Card.of(Rank.QUEEN, Suit.DIAMONDS)
                ),
                true
        );

        List<PanMove> legalMoves = state.getLegalMoves();

        assertThat(legalMoves)
                .filteredOn(move -> move instanceof PanMove.FourCards)
                .containsExactlyInAnyOrder(
                        new PanMove.FourCards(Rank.KING),
                        new PanMove.FourCards(Rank.ACE)
                );
    }

    @Disabled("not implemented yet")
    @Test
    void canPlayThreeNinesIfNineOfHeartsIsOnStack() { }

    @Test
    void cannotTakeIfOnlyNineOfHeartsStack() {
        PanState state = new PanState(
                List.of(
                        Card.of(Rank.JACK, Suit.HEARTS)
                ),
                List.of(
                        Card.of(Rank.NINE, Suit.DIAMONDS),
//                        new Card(Rank.TEN, Suit.DIAMONDS),
                        Card.of(Rank.JACK, Suit.DIAMONDS)
                ),
                stack(Card.of(Rank.NINE, Suit.HEARTS)),
                true
        );

        List<PanMove> legalMoves = state.getLegalMoves();

        assertFalse(legalMoves.contains(PanMove.Take.INSTANCE));
    }

    @Test
    void take2Cards() {
        PanState state = new PanState(
                List.of(
                        Card.of(Rank.NINE, Suit.CLUBS)
                ),
                List.of(
                        Card.of(Rank.NINE, Suit.DIAMONDS),
                        Card.of(Rank.JACK, Suit.DIAMONDS)
                ),
                stack(
                        Card.of(Rank.NINE, Suit.HEARTS),
                        Card.of(Rank.NINE, Suit.SPADES),
                        Card.of(Rank.TEN, Suit.DIAMONDS)
                ),
                true
        );

        PanState afterTake = state.move(PanMove.Take.INSTANCE);

        assertThat(afterTake.getPlayerCards().stream().map(Card::rank).toList())
                .containsExactlyInAnyOrder(Rank.NINE, Rank.NINE, Rank.TEN);
    }

    @Test
    void take3Cards() {
        PanState state = new PanState(
                List.of(
                        Card.of(Rank.NINE, Suit.CLUBS)
                ),
                List.of(
                        Card.of(Rank.NINE, Suit.DIAMONDS),
                        Card.of(Rank.JACK, Suit.DIAMONDS)
                ),
                stack(
                        Card.of(Rank.NINE, Suit.HEARTS),
                        Card.of(Rank.NINE, Suit.SPADES),
                        Card.of(Rank.TEN, Suit.DIAMONDS),
                        Card.of(Rank.JACK, Suit.SPADES),
                        Card.of(Rank.JACK, Suit.HEARTS)
                ),
                true
        );

        PanState afterTake = state.move(PanMove.Take.INSTANCE);

        assertThat(afterTake.getPlayerCards().stream().map(Card::rank).toList())
                .containsExactlyInAnyOrder(Rank.NINE, Rank.TEN, Rank.JACK, Rank.JACK);
    }

    private static List<Card> stack(Card... cards) {
        return Arrays.asList(cards);
    }
}
