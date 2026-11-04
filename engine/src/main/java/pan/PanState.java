package pan;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class PanState {

    private final Hand player, opponent;
    private final Stack stack;
    private final boolean playersTurn;

    public PanState(List<Card> player, List<Card> opponent, List<Card> stack, boolean playersTurn) {
        this(Hand.from(player), Hand.from(opponent), Stack.encode(stack), playersTurn);
    }

    private PanState(Hand player, Hand opponent, Stack stack, boolean playersTurn) {
        verify(player, opponent, stack);
        this.player = player;
        this.opponent = opponent;
        this.stack = stack;
        this.playersTurn = playersTurn;
    }

    private void verify(Hand player, Hand opponent, Stack stack) {
        extracted(player.cardSet(), opponent.cardSet());
        extracted(player.cardSet(), stack.cardSet());
        extracted(opponent.cardSet(), stack.cardSet());
    }

    private static void extracted(CardSet a, CardSet b) {
        CardSet overlap = a.intersect(b);
        if (!overlap.isEmpty()) {
            Iterator<Card> it = overlap.cards();
            StringBuilder sb = new StringBuilder();
            while (it.hasNext()) {
                sb.append(it.next());
                if (it.hasNext()) {
                    sb.append(", ");
                }
            }
            throw new IllegalArgumentException("Duplicate cards: " + sb);
        }
    }

    static void addSingleCardMoves(Hand hand, Card onTopOfDeck, List<PanMove> moves) {
//        int sameOrHigher = onTopOfDeck.rank().ordinal() * 4;
        Card prev = null;
        for (Iterator<Card> it = hand.cards(); it.hasNext(); ) {
            Card card = it.next();
            if (card.rank().compareTo(onTopOfDeck.rank()) < 0) {
                continue;
            }
            if (prev != null) {
                if (prev.rank() == card.rank()) {
                    continue;
                }
            }
            moves.add(new PanMove.OneCard(card));
            prev = card;
        }
    }

    static void addFourCardMoves(Hand cards, Card onTopOfDeck, List<PanMove> moves) {
//        int idxOfFirstHigher = (onTopOfDeck.rank().ordinal() + 1) * 4;
        for (Rank rank : cards.fourCards()) {
            if (rank.compareTo(onTopOfDeck.rank()) < 0) {
                continue;
            }
            moves.add(new PanMove.FourCards(rank));
        }

//        for (int i = idxOfFirstHigher; i < MAX_CARDS; i += 4) {
//            int fourCardsMask = FOUR_CARDS_MASK << i;
//            if ((cards & fourCardsMask) != fourCardsMask) {
//                continue;
//            }
//            Rank rank = RANKS[i / 4];
//            moves.add(new PanMove.FourCards(rank));
//        }
    }

    public List<PanMove> getLegalMoves() {
        Hand player = currentPlayer();
        if (stack.isEmpty()) {
            PanMove.OneCard playNineOfHearts = new PanMove.OneCard(Card.of(Rank.NINE, Suit.HEARTS));
            if (player.hasFourNines()) {
                return List.of(new PanMove.FourCards(Rank.NINE), playNineOfHearts);
            }
            return List.of(playNineOfHearts);
        }
        Card top = stack.peek();
        List<PanMove> moves = new ArrayList<>();
        addFourCardMoves(player, top, moves);
        addSingleCardMoves(player, top, moves);
        if (stack.size() > 1) {
            moves.add(PanMove.Take.INSTANCE);
        }
        return moves;
    }

    private Hand currentPlayer() {
        return playersTurn ? player : opponent;
    }

    /** True when {@code player} (UI player 0) is on move. */
    public boolean isPlayersTurn() {
        return playersTurn;
    }

    public boolean isGameOver() {
        return opponent.isEmpty() || player.isEmpty();
    }

    public boolean hasWon() {
        return player.isEmpty();
    }

    public PanState move(PanMove move) {
        if (move instanceof PanMove.OneCard scm) {
            Stack newStack = stack.addCard(scm.card());

            if (currentPlayer() == player) {
                return new PanState(
                        player.removeCard(scm.card()),
                        opponent,
                        newStack,
                        !playersTurn
                );
            } else {
                return new PanState(
                        player,
                        opponent.removeCard(scm.card()),
                        newStack,
                        true
                );
            }
        } else if (move instanceof PanMove.FourCards fcm) {
            Stack newStack = stack.addFourCards(fcm.rank());
            if (currentPlayer() == player) {
                return new PanState(
                        player.removeFourCards(fcm.rank()),
                        opponent,
                        newStack,
                        !playersTurn
                );
            } else {
                return new PanState(
                        player,
                        opponent.removeFourCards(fcm.rank()),
                        newStack,
                        true
                );
            }
        } else if (move instanceof PanMove.Take) {
            CardSet takenCards = stack.copy().MUTABLE_popThree();

            if (currentPlayer() == player) {
                return new PanState(
                        player.add(takenCards),
                        opponent,
                        stack.remove(takenCards),
                        !playersTurn
                );
            } else {
                return new PanState(
                        player,
                        opponent.add(takenCards),
                        stack.remove(takenCards),
                        true
                );
            }
        }
        throw new IllegalStateException("Illegal move");
    }

    public List<Card> getPlayerCards() {
        return player.getCards();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PanState state = (PanState) o;
        if (playersTurn != state.playersTurn) return false;
        if (!stack.equals(state.stack)) return false;
        if (!player.equals(state.player)) return false;
        return opponent.equals(state.opponent);
    }

    @Override
    public int hashCode() {
        int result = player.hashCode();
        result = 31 * result + opponent.hashCode();
        result = 31 * result + stack.hashCode();
        result = 31 * result + (playersTurn ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return "PanState{" +
                "player=" + player +
                ", opponent=" + opponent +
                ", stack=" + stack +
                ", playersTurn=" + playersTurn +
                '}';
    }
}
