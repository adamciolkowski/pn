import {
  cardId,
  parseCard,
  standardDeal,
  midgameExample,
  serializeState,
  deserializeState,
  legalSinglePlays,
  legalQuadPlays,
  legalThreeNines,
  validatePlay,
  applyPlay,
  applyPickup,
  mustPickup,
  canPickupFromStack,
  sortHand,
  isOpening,
  playerWithNineHearts,
  formatMove,
  cloneState,
} from './game.js';
import { renderCardElement } from './card-ui.js';

/** POST target when UI is served separately (e.g. python on 8765). Same-origin on :8080. */
function resolveEvaluateUrl() {
  const stored = localStorage.getItem('pan.engineUrl')?.trim();
  if (stored) {
    const base = stored.replace(/\/$/, '');
    return base.endsWith('/api/evaluate') ? base : `${base}/api/evaluate`;
  }

  if (location.port === '8080') return '/api/evaluate';

  const host = location.hostname === '127.0.0.1' ? 'localhost' : location.hostname;
  return `${location.protocol}//${host}:8080/api/evaluate`;
}

const EVALUATE_URL = resolveEvaluateUrl();

const $ = (sel, root) => root.querySelector(sel);

let rootEl;
let state = standardDeal();
let history = [cloneState(state)];
let historyIndex = 0;
let selected = new Set();
let evalDebounce = null;
let els = {};

function renderCard(c, opts = {}) {
  return renderCardElement(c, opts);
}

function pushHistory(newState) {
  history = history.slice(0, historyIndex + 1);
  history.push(cloneState(newState));
  historyIndex = history.length - 1;
}

function goToHistory(index) {
  historyIndex = index;
  state = cloneState(history[index]);
  selected.clear();
  render();
}

function legalPlayIds(playerIndex) {
  const ids = new Set();
  for (const c of legalSinglePlays(state, playerIndex)) ids.add(cardId(c));
  const three = legalThreeNines(state, playerIndex);
  if (three) three.forEach((c) => ids.add(cardId(c)));
  for (const quad of legalQuadPlays(state, playerIndex)) {
    quad.forEach((c) => ids.add(cardId(c)));
  }
  return ids;
}

function legalQuadIncludingCard(st, playerIndex, card) {
  const id = cardId(card);
  return legalQuadPlays(st, playerIndex).find((quad) =>
    quad.some((c) => cardId(c) === id)
  );
}

function cardUsesMultiSelect(st, playerIndex, card) {
  if (legalQuadIncludingCard(st, playerIndex, card)) return true;
  const three = legalThreeNines(st, playerIndex);
  return three?.some((c) => cardId(c) === cardId(card)) ?? false;
}

function multiSelectTargetIds(st, playerIndex, anchorCard) {
  const quad = legalQuadIncludingCard(st, playerIndex, anchorCard);
  if (quad) return new Set(quad.map(cardId));
  const three = legalThreeNines(st, playerIndex);
  if (three?.some((c) => cardId(c) === cardId(anchorCard))) {
    return new Set(three.map(cardId));
  }
  return new Set();
}

function isCompleteMultiSelection(st, playerIndex, selectedSet) {
  if (selectedSet.size === 0) return false;
  const anchor = parseCard([...selectedSet][0]);
  const target = multiSelectTargetIds(st, playerIndex, anchor);
  if (selectedSet.size !== target.size || target.size < 2) return false;
  for (const sid of selectedSet) {
    if (!target.has(sid)) return false;
  }
  return validatePlay(st, playerIndex, [...selectedSet].map(parseCard)).ok;
}

function renderHand(container, playerIndex) {
  container.innerHTML = '';
  const isActive = state.currentPlayer === playerIndex;
  const legal = isActive ? legalPlayIds(playerIndex) : new Set();

  const hand = sortHand(state.players[playerIndex].hand);
  for (const c of hand) {
    const id = cardId(c);
    const playable = isActive && legal.has(id);
    const el = renderCard(c, {
      selected: selected.has(id),
      legal: playable,
      illegal: isActive && !legal.has(id),
    });
    el.addEventListener('click', () => onCardClick(playerIndex, c));
    container.appendChild(el);
  }
}

const VISIBLE_STACK_CARDS = 5;
const STACK_UNDER_INDICATOR = 2;

function renderStack() {
  els.stack.innerHTML = '';
  const cards = state.stack;
  const n = cards.length;
  const hiddenBelow = Math.max(0, n - VISIBLE_STACK_CARDS);
  const docRoot = document.documentElement;
  const spread =
    Number.parseFloat(getComputedStyle(docRoot).getPropertyValue('--stack-spread')) || 36;
  const underSpread =
    Number.parseFloat(getComputedStyle(docRoot).getPropertyValue('--stack-under-spread')) || 8;

  const faceCards = cards.slice(-VISIBLE_STACK_CARDS);
  const narrowCount = hiddenBelow > 0 ? STACK_UNDER_INDICATOR : 0;
  const faceStartX = narrowCount * underSpread;

  for (let i = 0; i < narrowCount; i++) {
    const back = document.createElement('div');
    back.className = 'stack-card-back';
    back.style.left = `${i * underSpread}px`;
    back.style.zIndex = String(i);
    els.stack.appendChild(back);
  }

  faceCards.forEach((c, i) => {
    const el = renderCard(c, { stack: true });
    el.style.left = `${faceStartX + i * spread}px`;
    el.style.zIndex = String(narrowCount + i);
    els.stack.appendChild(el);
  });

  els.stack.dataset.hiddenCount = String(hiddenBelow);
  const cardW = Number.parseFloat(getComputedStyle(docRoot).getPropertyValue('--card-w')) || 52;
  let totalW = 0;
  if (n > 0) {
    totalW = faceStartX + cardW + (faceCards.length - 1) * spread;
  }
  els.stack.style.width = `${totalW}px`;
}

function renderMoveList() {
  els.moveList.innerHTML = '';
  const moves = [];
  for (let i = 1; i < history.length; i++) {
    const curr = history[i];
    if (curr._move) moves.push({ index: i, move: curr._move });
  }
  if (!moves.length) {
    els.moveList.innerHTML = '<div class="move-item">Start position</div>';
    return;
  }
  moves.forEach(({ index, move }) => {
    const div = document.createElement('div');
    div.className = `move-item${index === historyIndex ? ' move-item--current' : ''}`;
    div.textContent = `${index}. ${formatMove(move)}`;
    div.addEventListener('click', () => goToHistory(index));
    els.moveList.appendChild(div);
  });
}

function updateActionButtons() {
  const pi = state.currentPlayer;

  const pickupOk = canPickupFromStack(state);
  els.btnPickup.disabled = !!state.result || !pickupOk;

  let hint = '';
  if (state.result) {
    hint = `Deal over: ${state.result}${state.winner !== undefined ? ` (P${state.winner + 1} wins deal)` : ''}`;
  } else if (mustPickup(state, pi) && !pickupOk) {
    hint = `P${pi + 1} cannot play and the stack has nothing to pick up.`;
  } else if (mustPickup(state, pi)) {
    hint = `P${pi + 1} cannot play — must pick up from stack.`;
  } else if (isOpening(state)) {
    hint =
      pi === playerWithNineHearts(state)
        ? 'Opening — click 9♥ or select four 9s.'
        : `P${pi + 1} cannot move until 9♥ is played.`;
  } else if (selected.size > 0) {
    const anchor = parseCard([...selected][0]);
    const target = multiSelectTargetIds(state, pi, anchor);
    if (target.size > 1) {
      hint = `Select all ${target.size} cards of the set (${selected.size}/${target.size}), or click one again to play it alone.`;
    }
  } else if (selected.size === 0) {
    hint = `P${pi + 1} to move — click a card or pick up.`;
  } else {
    hint = `P${pi + 1} to move.`;
  }
  els.actionHint.textContent = hint;
}

function render() {
  rootEl.querySelectorAll('.player-area').forEach((area) => {
    const p = Number(area.dataset.player);
    area.classList.toggle('player-area--active', state.currentPlayer === p && !state.result);
  });

  renderHand(els.hand0, 0);
  renderHand(els.hand1, 1);
  renderStack();

  els.p0Count.textContent = `${state.players[0].hand.length} cards`;
  els.p1Count.textContent = `${state.players[1].hand.length} cards`;

  els.currentPlayer.value = String(state.currentPlayer);

  els.btnUndo.disabled = historyIndex <= 0;
  els.btnRedo.disabled = historyIndex >= history.length - 1;

  renderMoveList();
  updateActionButtons();
  scheduleEval();
}

function onCardClick(playerIndex, card) {
  if (state.result) return;
  if (playerIndex !== state.currentPlayer) {
    els.actionHint.textContent = `Switch "On move" to P${playerIndex + 1} to play their cards (analysis mode).`;
    els.currentPlayer.value = String(playerIndex);
    state.currentPlayer = playerIndex;
    selected.clear();
    render();
    return;
  }

  const legal = legalPlayIds(playerIndex);
  const id = cardId(card);

  if (selected.has(id)) {
    if (selected.size === 1) {
      const one = [...selected].map(parseCard);
      if (validatePlay(state, playerIndex, one).ok) {
        tryPlayCards(one);
        return;
      }
    }
    selected.delete(id);
    render();
    return;
  }

  if (!legal.has(id)) return;

  if (selected.size === 0) {
    if (!cardUsesMultiSelect(state, playerIndex, card)) {
      tryPlayCards([card]);
      return;
    }
    selected.add(id);
  } else {
    const anchor = parseCard([...selected][0]);
    const target = multiSelectTargetIds(state, playerIndex, anchor);
    if (target.has(id)) {
      selected.add(id);
    } else if (cardUsesMultiSelect(state, playerIndex, card)) {
      selected.clear();
      selected.add(id);
    } else {
      selected.clear();
      tryPlayCards([card]);
      return;
    }
  }

  if (isCompleteMultiSelection(state, playerIndex, selected)) {
    tryPlayCards([...selected].map(parseCard));
    return;
  }

  render();
}

function commitMove(newState, move) {
  newState._move = move;
  state = newState;
  selected.clear();
  pushHistory(state);
  render();
}

function tryPlayCards(cards) {
  if (state.result) return;
  try {
    const { state: next, move } = applyPlay(state, cards);
    commitMove(next, move);
  } catch (e) {
    els.actionHint.textContent = e.message;
    render();
  }
}

function doPickup() {
  if (state.result) return;
  try {
    const { state: next, move } = applyPickup(state);
    commitMove(next, move);
  } catch (e) {
    els.actionHint.textContent = e.message;
  }
}

function applyStateFromJson() {
  try {
    const obj = JSON.parse(els.stateJson.value);
    state = deserializeState(obj);
    selected.clear();
    history = [cloneState(state)];
    historyIndex = 0;
    render();
  } catch (e) {
    alert(`Invalid state JSON: ${e.message}`);
  }
}

function loadPreset(name) {
  if (name === 'standard') state = standardDeal(true);
  else if (name === 'midgame') state = midgameExample();
  else {
    els.stateJson.value = JSON.stringify(serializeState(state), null, 2);
    return;
  }
  els.stateJson.value = JSON.stringify(serializeState(state), null, 2);
  selected.clear();
  history = [cloneState(state)];
  historyIndex = 0;
  render();
}

function buildEvaluateRequest(gameState) {
  const s = serializeState(gameState);
  return {
    players: s.players,
    stack: s.stack,
    currentPlayer: s.currentPlayer,
  };
}

const ENGINE_SUIT = { HEARTS: 'H', CLUBS: 'C', DIAMONDS: 'D', SPADES: 'S' };
const ENGINE_RANK = {
  NINE: '9',
  TEN: '10',
  JACK: 'J',
  QUEEN: 'Q',
  KING: 'K',
  ACE: 'A',
};

function formatEngineBestMove(move) {
  if (move == null) return '';
  if (typeof move === 'string') return move;
  if (move.type === 'one' && move.card) {
    try {
      return formatCard(parseCard(move.card));
    } catch {
      return move.card;
    }
  }
  if (move.type === 'four' && move.rank) {
    return `Four ${move.rank}s`;
  }
  if (move.type === 'pickup') {
    return 'Pick up from stack';
  }
  return '';
}

async function fetchEval(gameState) {
  if (gameState.result) {
    return {
      score: 0,
      winIn: null,
      loseIn: null,
      bestMove: null,
      note: 'Deal finished — no engine evaluation.',
      source: 'engine',
    };
  }

  const payload = buildEvaluateRequest(gameState);

  try {
    const res = await fetch(EVALUATE_URL, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload),
    });
    if (!res.ok) throw new Error(`HTTP ${res.status}`);
    const data = await res.json();
    if (data.error) throw new Error(data.error);
    const bestMove = data.bestMove ?? data.best_move ?? null;
    const noteParts = [];
    if (bestMove != null) noteParts.push(`Best: ${formatEngineBestMove(bestMove)}`);
    return {
      score: data.score ?? 0,
      winIn: data.winIn ?? data.win_in ?? null,
      loseIn: data.loseIn ?? data.lose_in ?? null,
      bestMove,
      note: noteParts.join(' · '),
      source: 'engine',
    };
  } catch {
    return { source: 'unavailable' };
  }
}

function displayEval(data) {
  if (data.source === 'unavailable') {
    els.evalScore.textContent = '—';
    els.evalScore.style.color = 'var(--text-muted)';
    els.evalBar.style.marginLeft = '-25%';
    els.evalWin.textContent = '—';
    els.evalLose.textContent = '—';
    els.evalNote.textContent = 'Engine unreachable';
    return;
  }

  const score = Number(data.score);
  const scoreText = score > 0 ? `+${score.toFixed(2)}` : score.toFixed(2);
  els.evalScore.textContent = scoreText;
  els.evalScore.style.color = score > 0 ? 'var(--p1)' : score < 0 ? 'var(--p2)' : 'var(--text)';

  const barUnit = score / 100;
  const pct = Math.max(0, Math.min(100, 50 + barUnit * 25));
  els.evalBar.style.marginLeft = `${pct - 50}%`;
  els.evalBar.style.width = '50%';

  els.evalWin.textContent = data.winIn != null ? `${data.winIn} moves` : '—';
  els.evalLose.textContent = data.loseIn != null ? `${data.loseIn} moves` : '—';
  els.evalNote.textContent = data.note || '';
}

function scheduleEval() {
  clearTimeout(evalDebounce);
  els.evalNote.textContent = 'Analyzing…';
  evalDebounce = setTimeout(async () => {
    const data = await fetchEval(state);
    displayEval(data);
  }, 280);
}

let bound = false;

function bindEvents() {
  if (bound) return;
  bound = true;

  els.btnPickup.addEventListener('click', doPickup);
  els.btnUndo.addEventListener('click', () => goToHistory(historyIndex - 1));
  els.btnRedo.addEventListener('click', () => goToHistory(historyIndex + 1));
  els.btnApply.addEventListener('click', applyStateFromJson);
  els.btnExport.addEventListener('click', () => {
    els.stateJson.value = JSON.stringify(serializeState(state), null, 2);
  });
  els.preset.addEventListener('change', () => loadPreset(els.preset.value));
  els.currentPlayer.addEventListener('change', () => {
    state.currentPlayer = Number(els.currentPlayer.value);
    selected.clear();
    render();
  });
  els.btnEditPosition?.addEventListener('click', () => {
    window.dispatchEvent(
      new CustomEvent('pan:navigate', {
        detail: { view: 'setup', state: cloneState(state) },
      })
    );
  });
}

function cacheElements() {
  els = {
    hand0: $('#hand-0', rootEl),
    hand1: $('#hand-1', rootEl),
    stack: $('#stack', rootEl),
    p0Count: $('#p0-count', rootEl),
    p1Count: $('#p1-count', rootEl),
    currentPlayer: $('#current-player', rootEl),
    btnPickup: $('#btn-pickup', rootEl),
    actionHint: $('#action-hint', rootEl),
    moveList: $('#move-list', rootEl),
    btnUndo: $('#btn-undo', rootEl),
    btnRedo: $('#btn-redo', rootEl),
    stateJson: $('#state-json', rootEl),
    preset: $('#preset', rootEl),
    btnApply: $('#btn-apply-state', rootEl),
    btnExport: $('#btn-export-state', rootEl),
    btnEditPosition: $('#btn-edit-position', rootEl),
    evalContent: $('#eval-content', rootEl),
    evalScore: $('#eval-score', rootEl),
    evalBar: $('#eval-bar', rootEl),
    evalWin: $('#eval-win', rootEl),
    evalLose: $('#eval-lose', rootEl),
    evalNote: $('#eval-note', rootEl),
  };
}

/** Load a position and show the analysis board. */
export function startAnalysis(initialState) {
  rootEl = document.getElementById('view-analysis');
  cacheElements();
  bindEvents();

  const next = initialState ? cloneState(initialState) : standardDeal(true);
  delete next.result;
  delete next.winner;
  state = next;
  selected.clear();
  history = [cloneState(state)];
  historyIndex = 0;
  if (els.stateJson) {
    els.stateJson.value = JSON.stringify(serializeState(state), null, 2);
  }
  render();
}

export function getAnalysisState() {
  return cloneState(state);
}
