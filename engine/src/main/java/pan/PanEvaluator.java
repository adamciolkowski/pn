package pan;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PanEvaluator {

    private long evaluatedNodes = 0;
    private final Map<StateKey, CacheEntry> stateCache = new HashMap<>();

    private record StateKey(PanState state, int depth, boolean isMaximizing) { }

    private enum Bound {
        EXACT, LOWER, UPPER
    }

    private record CacheEntry(int score, Bound bound) { }

    public MoveAndScore findBestMove(PanState board, int depth) {
        stateCache.clear();
        boolean maximizing = board.isPlayersTurn();
        int bestScore = maximizing ? Integer.MIN_VALUE : Integer.MAX_VALUE;
        PanMove bestMove = null;

        List<PanMove> legalMoves = board.getLegalMoves();
        for (PanMove move : legalMoves) {
            PanState childBoard = board.move(move);
            int score = alfabeta(childBoard, depth - 1, Integer.MIN_VALUE, Integer.MAX_VALUE, childBoard.isPlayersTurn());

            if (maximizing) {
                if (score > bestScore) {
                    bestScore = score;
                    bestMove = move;
                }
            } else if (score < bestScore) {
                bestScore = score;
                bestMove = move;
            }
        }
        System.out.println("evaluated nodes: " + evaluatedNodes);
        return new MoveAndScore(bestMove, bestScore);
    }

    public int evaluate(PanState state, int depth, boolean isMaximizing) {
        stateCache.clear();
        return alfabeta(state, depth, Integer.MIN_VALUE, Integer.MAX_VALUE, isMaximizing);
    }

    int minimax(PanState state, int depth, boolean isMaximizing) {
        StateKey key = new StateKey(state, depth, isMaximizing);
        CacheEntry cached = stateCache.get(key);
        if (cached != null) {
            return cached.score();
        }

        evaluatedNodes++;
        if (state.isGameOver()) {
            int score = 100 + depth;
            score = state.hasWon() ? score : -score;
            stateCache.put(key, new CacheEntry(score, Bound.EXACT));
            return score;
        }
        if (depth == 0) {
            stateCache.put(key, new CacheEntry(0, Bound.EXACT));
            return 0;
        }
        if (isMaximizing) {
            int max = Integer.MIN_VALUE;
            List<PanMove> moves = state.getLegalMoves();
            for (PanMove move : moves) {
                PanState newState = state.move(move);
                int score = minimax(newState, depth - 1, false);
                max = Math.max(max, score);
            }
            stateCache.put(key, new CacheEntry(max, Bound.EXACT));
            return max;
        } else {
            int min = Integer.MAX_VALUE;
            List<PanMove> moves = state.getLegalMoves();
            for (PanMove move : moves) {
                PanState newState = state.move(move);
                int score = minimax(newState, depth - 1, true);
                min = Math.min(min, score);
            }
            stateCache.put(key, new CacheEntry(min, Bound.EXACT));
            return min;
        }
    }

    public int alfabeta(PanState state, int depth, int alpha, int beta, boolean isMaximizing) {
        StateKey key = new StateKey(state, depth, isMaximizing);
        CacheEntry cached = stateCache.get(key);
        if (cached != null) {
            return switch (cached.bound()) {
                case EXACT -> cached.score();
                case LOWER -> cached.score() >= beta ? cached.score() : scoreAlfabeta(state, depth, alpha, beta, isMaximizing);
                case UPPER -> cached.score() <= alpha ? cached.score() : scoreAlfabeta(state, depth, alpha, beta, isMaximizing);
            };
        }
        return scoreAlfabeta(state, depth, alpha, beta, isMaximizing);
    }

    private int scoreAlfabeta(PanState state, int depth, int alpha, int beta, boolean isMaximizing) {
        StateKey key = new StateKey(state, depth, isMaximizing);
        int alphaOrig = alpha;
        int betaOrig = beta;

        evaluatedNodes++;
        if (state.isGameOver()) {
            int score = 100 + depth;
            score = state.hasWon() ? score : -score;
            stateCache.put(key, new CacheEntry(score, Bound.EXACT));
            return score;
        }
        if (depth == 0) {
            stateCache.put(key, new CacheEntry(0, Bound.EXACT));
            return 0;
        }
        int result;
        if (isMaximizing) {
            int max = Integer.MIN_VALUE;
            List<PanMove> moves = state.getLegalMoves();
            for (PanMove move : moves) {
                PanState newState = state.move(move);
                int score = alfabeta(newState, depth - 1, alpha, beta, false);
                max = Math.max(max, score);
                alpha = Math.max(alpha, score);
                if (beta <= alpha) {
                    break;//  # Beta cutoff (pruning)
                }
            }
            result = max;
        } else {
            int min = Integer.MAX_VALUE;
            List<PanMove> moves = state.getLegalMoves();
            for (PanMove move : moves) {
                PanState newState = state.move(move);
                int score = alfabeta(newState, depth - 1, alpha, beta, true);
                min = Math.min(min, score);
                beta = Math.min(beta, score);
                if (beta <= alpha) {
                    break;//  # Beta cutoff (pruning)
                }
            }
            result = min;
        }
        Bound bound = Bound.EXACT;
        if (result <= alphaOrig) {
            bound = Bound.UPPER;
        } else if (result >= betaOrig) {
            bound = Bound.LOWER;
        }
        stateCache.put(key, new CacheEntry(result, bound));
        return result;
    }

    public long getEvaluatedNodes() {
        return evaluatedNodes;
    }

    public record MoveAndScore(PanMove move, int score) { }
}
