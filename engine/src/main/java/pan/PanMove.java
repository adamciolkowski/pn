package pan;

public sealed interface PanMove {

    record OneCard(Card card) implements PanMove {

        @Override
        public String toString() {
            return "OneCard[" + card + ']';
        }
    }

    record FourCards(Rank rank) implements PanMove {

        @Override
        public String toString() {
            return "FourCards[" + rank + ']';
        }
    }

    final class Take implements PanMove {

        public static final Take INSTANCE = new Take();

        private Take() { }

        @Override
        public String toString() {
            return "Take";
        }
    }
}
