package pan;

import java.util.*;

public class PanEvaluator {

    private static final int WIN_SCORE = 100;

    private int evaluatedNodes = 0;
    private int skippedDueToRepetition = 0;
    private final Map<PanState, Integer> evaluatedStates = new HashMap<>();

    private int cacheHits = 0;
    private int cacheMisses = 0;

    public MoveAndScore findBestMove(PanState board, int depth) {
        boolean maximizing = board.isPlayersTurn();
        int bestScore = maximizing ? Integer.MIN_VALUE : Integer.MAX_VALUE;
        PanMove bestMove = null;

        List<PanMove> legalMoves = board.getLegalMoves();
        for (PanMove move : legalMoves) {
            PanState childBoard = board.move(move);
            int score = evaluate(childBoard, depth - 1, childBoard.isPlayersTurn());

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

        return new MoveAndScore(bestMove, bestScore);
    }
    
//    public int evaluate(PanState state, int depth, boolean isMaximizing) {
//    }

    public int evaluate(PanState state, int depth, boolean isMaximizing) {
        return alfabeta(state, depth, Integer.MIN_VALUE, Integer.MAX_VALUE, isMaximizing);
    }

    int minimax(PanState state, int depth, boolean isMaximizing) {
        evaluatedNodes++;
        if (state.isGameOver()) {
            int score = 100 + depth;
            return state.hasWon() ? score : -score;
        }
        if (depth == 0) {
            return 0;
        }
        if (isMaximizing) {
            int max = Integer.MIN_VALUE;
            List<PanMove> moves = state.getLegalMoves();
            for (PanMove move : moves) {
                PanState newState = state.move(move);
                int score = evaluate(newState, depth - 1, false);
                max = Math.max(max, score);
            }
            return max;
        } else {
            int min = Integer.MAX_VALUE;
            List<PanMove> moves = state.getLegalMoves();
            for (PanMove move : moves) {
                PanState newState = state.move(move);
                int score = evaluate(newState, depth - 1, true);
                min = Math.min(min, score);
            }
            return min;
        }
    }

    public int alfabeta(PanState state, int depth, int alpha, int beta, boolean isMaximizing) {
        evaluatedStates.put(state, depth);
        evaluatedNodes++;
        if (state.isGameOver()) {
            int score = 100 + depth;
            // player won, isMaximizing=true   --> +
            // player won, isMaximizing=false   --> -
            // player lose, isMaximizing=true   -->
            // player lose, isMaximizing=false   -->
            return state.hasWon() ? score : -score;
        }
        if (depth == 0) {
            return 0;
        }
        if (isMaximizing) {
            int max = Integer.MIN_VALUE;
            List<PanMove> moves = state.getLegalMoves();
            for (PanMove move : moves) {
                PanState newState = state.move(move);
                Integer contains = evaluatedStates.get(newState);
                if (contains != null && contains == depth) {
                    skippedDueToRepetition++;
                    continue;
                }
                int score = alfabeta(newState, depth - 1, alpha, beta, false);
                max = Math.max(max, score);
                alpha = Math.max(alpha, score);
                if (beta <= alpha) {
                    break;//  # Beta cutoff (pruning)
                }
            }
            return max;
        } else {
            int min = Integer.MAX_VALUE;
            List<PanMove> moves = state.getLegalMoves();
            for (PanMove move : moves) {
                PanState newState = state.move(move);
                Integer contains = evaluatedStates.get(newState);
                if (contains != null && contains == depth) {
                    skippedDueToRepetition++;
                    continue;
                }
                int score = alfabeta(newState, depth - 1, alpha, beta, true);
                min = Math.min(min, score);
                beta = Math.min(beta, score);
                if (beta <= alpha) {
                    break;//  # Beta cutoff (pruning)
                }
            }
            return min;
        }
    }

    public int getEvaluatedNodes() {
        return evaluatedNodes;
    }

    public int getCacheHits() {
        return cacheHits;
    }

    public int getCacheMisses() {
        return cacheMisses;
    }

    public int getSkippedDueToRepetition() {
        return skippedDueToRepetition;
    }

    public record MoveAndScore(PanMove move, int score) {
    }
}
