/** Pan card game logic (Kurnik rules, 2-player analysis). */

export const RANKS = ['9', '10', 'J', 'Q', 'K', 'A'];
export const SUITS = ['H', 'D', 'C', 'S'];
export const SUIT_SYMBOL = { H: '♥', D: '♦', C: '♣', S: '♠' };
export const RANK_VALUE = { '9': 0, '10': 1, J: 2, Q: 3, K: 4, A: 5 };

/** @typedef {{ rank: string, suit: string }} Card */
/** @typedef {{ hand: Card[] }} Player */
/** @typedef {{ players: Player[], stack: Card[], currentPlayer: number }} GameState */

export function cardId(c) {
  return `${c.rank}${c.suit}`;
}

export function parseCard(id) {
  const suit = id.slice(-1);
  const rank = id.slice(0, -1);
  if (!SUITS.includes(suit) || !RANKS.includes(rank)) {
    throw new Error(`Invalid card: ${id}`);
  }
  return { rank, suit };
}

export function parseCards(ids) {
  return ids.map(parseCard);
}

export function fullDeck() {
  const deck = [];
  for (const rank of RANKS) {
    for (const suit of SUITS) {
      deck.push({ rank, suit });
    }
  }
  return deck;
}

export function shuffle(arr) {
  const a = [...arr];
  for (let i = a.length - 1; i > 0; i--) {
    const j = Math.floor(Math.random() * (i + 1));
    [a[i], a[j]] = [a[j], a[i]];
  }
  return a;
}

export function standardDeal(seedOrdered = false) {
  const deck = seedOrdered ? fullDeck() : shuffle(fullDeck());
  const p0 = deck.filter((_, i) => i % 2 === 0);
  const p1 = deck.filter((_, i) => i % 2 === 1);
  const opener = p0.some((c) => cardId(c) === '9H') ? 0 : 1;
  return {
    players: [{ hand: sortHand(p0) }, { hand: sortHand(p1) }],
    stack: [],
    currentPlayer: opener,
  };
}

export function midgameExample() {
  return {
    players: [
      { hand: sortHand(parseCards(['AH', 'KD', 'QC', '10S', '9D'])) },
      { hand: sortHand(parseCards(['AS', 'JH', '10H', '9C', '9S', '10C', 'JC'])) },
    ],
    stack: parseCards(['9H', '10D', 'JS', 'QS']),
    currentPlayer: 1,
  };
}

export function sortHand(hand) {
  return [...hand].sort((a, b) => {
    const dr = RANK_VALUE[a.rank] - RANK_VALUE[b.rank];
    if (dr !== 0) return dr;
    return SUITS.indexOf(a.suit) - SUITS.indexOf(b.suit);
  });
}

export function isOpening(state) {
  return state.stack.length === 0;
}

/** Player who holds 9♥ (opener at start of deal). */
export function playerWithNineHearts(state) {
  for (let i = 0; i < state.players.length; i++) {
    if (state.players[i].hand.some((c) => cardId(c) === '9H')) {
      return i;
    }
  }
  return -1;
}

export function stackTop(state) {
  return state.stack.length ? state.stack[state.stack.length - 1] : null;
}

export function isNineHeartsOnlyStack(state) {
  return state.stack.length === 1 && cardId(state.stack[0]) === '9H';
}

export function minPlayableRank(state) {
  const top = stackTop(state);
  return top ? RANK_VALUE[top.rank] : 0;
}

function groupByRank(hand) {
  const g = {};
  for (const c of hand) {
    (g[c.rank] ??= []).push(c);
  }
  return g;
}

/** Legal single-card plays from hand. */
export function legalSinglePlays(state, playerIndex) {
  if (isOpening(state)) {
    if (playerIndex !== playerWithNineHearts(state)) return [];
    return state.players[playerIndex].hand.filter((c) => cardId(c) === '9H');
  }
  const min = minPlayableRank(state);
  return state.players[playerIndex].hand.filter((c) => RANK_VALUE[c.rank] >= min);
}

/** Legal four-of-a-kind plays (exactly 4 cards same rank). */
export function legalQuadPlays(state, playerIndex) {
  const groups = groupByRank(state.players[playerIndex].hand);
  const plays = [];
  if (isOpening(state)) {
    if (playerIndex !== playerWithNineHearts(state)) return [];
    const nines = groups['9'];
    if (nines?.length >= 4) plays.push(nines.slice(0, 4));
    return plays;
  }
  const min = minPlayableRank(state);
  for (const [rank, cards] of Object.entries(groups)) {
    if (cards.length >= 4 && RANK_VALUE[rank] >= min) {
      plays.push(cards.slice(0, 4));
    }
  }
  return plays;
}

/** Special: three 9s when stack is only 9♥. */
export function legalThreeNines(state, playerIndex) {
  if (!isNineHeartsOnlyStack(state)) return null;
  const nines = state.players[playerIndex].hand.filter((c) => c.rank === '9' && c.suit !== 'H');
  if (nines.length === 3) return nines;
  return null;
}

export function canPlay(state, playerIndex) {
  if (legalSinglePlays(state, playerIndex).length) return true;
  if (legalQuadPlays(state, playerIndex).length) return true;
  if (legalThreeNines(state, playerIndex)) return true;
  return false;
}

export function mustPickup(state, playerIndex) {
  return !canPlay(state, playerIndex);
}

/** Cards taken when picking up from stack. */
export function pickupCards(state) {
  const stack = state.stack;
  if (stack.length <= 1) return [];
  if (stack.length <= 3) {
    return stack.slice(1);
  }
  return stack.slice(-3);
}

export function canPickupFromStack(state) {
  return pickupCards(state).length > 0;
}

export function cardsMatchSelection(hand, selectedIds) {
  return selectedIds.every((id) => hand.some((c) => cardId(c) === id));
}

export function validatePlay(state, playerIndex, selectedCards) {
  const hand = state.players[playerIndex].hand;
  const ids = selectedCards.map(cardId);
  if (!cardsMatchSelection(hand, ids)) {
    return { ok: false, reason: 'Selected cards not in hand' };
  }

  if (isOpening(state)) {
    const opener = playerWithNineHearts(state);
    if (playerIndex !== opener) {
      return { ok: false, reason: 'Only the player with 9♥ can open' };
    }
    if (selectedCards.length === 1 && cardId(selectedCards[0]) === '9H') {
      return { ok: true, type: 'single' };
    }
    if (
      selectedCards.length === 4 &&
      selectedCards.every((c) => c.rank === '9')
    ) {
      return { ok: true, type: 'quad' };
    }
    return { ok: false, reason: 'Open with 9♥ or four 9s' };
  }

  const three = legalThreeNines(state, playerIndex);
  if (three && selectedCards.length === 3 && selectedCards.every((c) => c.rank === '9')) {
    return { ok: true, type: 'three-nines' };
  }

  if (selectedCards.length === 1) {
    const min = minPlayableRank(state);
    if (RANK_VALUE[selectedCards[0].rank] < min) {
      return { ok: false, reason: 'Card too low for stack top' };
    }
    return { ok: true, type: 'single' };
  }

  if (selectedCards.length === 4) {
    const rank = selectedCards[0].rank;
    if (!selectedCards.every((c) => c.rank === rank)) {
      return { ok: false, reason: 'Four-of-a-kind must be same rank' };
    }
    const min = minPlayableRank(state);
    if (RANK_VALUE[rank] < min) {
      return { ok: false, reason: 'Rank too low for stack top' };
    }
    return { ok: true, type: 'quad' };
  }

  return { ok: false, reason: 'Play 1 card, 3 nines (9♥ only on stack), or 4 of a kind' };
}

function removeCardsFromHand(hand, toRemove) {
  const ids = new Set(toRemove.map(cardId));
  return hand.filter((c) => !ids.has(cardId(c)));
}

function orderPlayOnStack(cards, type) {
  if (type === 'three-nines') {
    const others = cards.filter((c) => cardId(c) !== '9H');
    return [...others];
  }
  if (type === 'quad' && cards.some((c) => c.rank === '9' && c.suit === 'H')) {
    const nineH = cards.find((c) => cardId(c) === '9H');
    const rest = cards.filter((c) => cardId(c) !== '9H');
    return nineH ? [nineH, ...rest] : cards;
  }
  return cards;
}

function nextPlayer(state, playedCards, wasPickup) {
  if (wasPickup) {
    return (state.currentPlayer + 1) % 2;
  }
  return (state.currentPlayer + 1) % 2;
}

/** @returns {{ state: GameState, move: object }} */
export function applyPlay(state, selectedCards) {
  const pi = state.currentPlayer;
  const v = validatePlay(state, pi, selectedCards);
  if (!v.ok) throw new Error(v.reason);

  const ordered = orderPlayOnStack(selectedCards, v.type);
  const newHand = sortHand(removeCardsFromHand(state.players[pi].hand, selectedCards));
  const players = state.players.map((p, i) =>
    i === pi ? { ...p, hand: newHand } : { ...p }
  );
  const newStack = [...state.stack, ...ordered];

  let newState = {
    ...state,
    players,
    stack: newStack,
    currentPlayer: nextPlayer(state, ordered, false),
  };

  newState = resolveDealEnd(newState, pi);
  return {
    state: newState,
    move: { type: 'play', player: pi, cards: selectedCards.map(cardId), playType: v.type },
  };
}

export function applyPickup(state) {
  const pi = state.currentPlayer;
  const taken = pickupCards(state);
  if (!taken.length) {
    throw new Error('Nothing to pick up from the stack');
  }
  const newHand = sortHand([...state.players[pi].hand, ...taken]);
  let newStack = state.stack;
  if (taken.length) {
    if (state.stack.length <= 3) {
      newStack = [state.stack[0]];
    } else {
      newStack = state.stack.slice(0, -3);
    }
  }
  const players = state.players.map((p, i) =>
    i === pi ? { ...p, hand: newHand } : { ...p }
  );
  return {
    state: {
      ...state,
      players,
      stack: newStack,
      currentPlayer: (pi + 1) % 2,
    },
    move: { type: 'pickup', player: pi, cards: taken.map(cardId) },
  };
}

function countAces(hand) {
  return hand.filter((c) => c.rank === 'A').length;
}

/** Check deal end: one player emptied hand. */
export function resolveDealEnd(state, lastPlayerWhoPlayed) {
  const empty = state.players.findIndex((p) => p.hand.length === 0);
  if (empty === -1) return state;

  const loser = state.players.findIndex((p) => p.hand.length > 0);
  if (loser === -1) return state;

  const winnerHand = state.players[empty].hand;
  const loserHand = state.players[loser].hand;

  // Draw: last player could play all remaining (1 or 4 aces) on their turn
  if (
    loser === state.currentPlayer &&
    (loserHand.length === 1 || loserHand.length === 4) &&
    countAces(loserHand) === loserHand.length &&
    canPlay(state, loser)
  ) {
    return { ...state, result: 'draw' };
  }

  return {
    ...state,
    result: 'deal-won',
    winner: empty,
    loser,
  };
}

export function cloneState(state) {
  return JSON.parse(JSON.stringify(state));
}

export function serializeState(state) {
  return {
    players: state.players.map((p) => ({
      hand: sortHand(p.hand).map(cardId),
    })),
    stack: state.stack.map(cardId),
    currentPlayer: state.currentPlayer,
    ...(state.result ? { result: state.result } : {}),
    ...(state.winner !== undefined ? { winner: state.winner } : {}),
  };
}

export function deserializeState(obj) {
  const state = {
    players: obj.players.map((p) => ({
      hand: sortHand(parseCards(p.hand)),
    })),
    stack: parseCards(obj.stack ?? []),
    currentPlayer: obj.currentPlayer ?? 0,
  };
  if (obj.result) state.result = obj.result;
  if (obj.winner !== undefined) state.winner = obj.winner;
  return state;
}

export function formatCard(c) {
  return `${c.rank}${SUIT_SYMBOL[c.suit]}`;
}

function formatCardId(id) {
  return formatCard(parseCard(id));
}

export function formatMove(move) {
  if (move.type === 'pickup') {
    const cards = move.cards?.length
      ? ` [${move.cards.map(formatCardId).join(', ')}]`
      : '';
    return `P${move.player + 1} pickup${cards}`;
  }
  return `P${move.player + 1} ${move.cards.map(formatCardId).join(' ')}`;
}
