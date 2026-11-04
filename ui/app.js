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
  canPlay,
  canPickupFromStack,
  sortHand,
  isOpening,
  playerWithNineHearts,
  formatCard,
  formatMove,
  cloneState,
} from './game.js';

const $ = (sel) => document.querySelector(sel);

let state = standardDeal();
let history = [cloneState(state)];
let historyIndex = 0;
let selected = new Set();
let evalDebounce = null;

const els = {
  hand0: $('#hand-0'),
  hand1: $('#hand-1'),
  stack: $('#stack'),
  p0Count: $('#p0-count'),
  p1Count: $('#p1-count'),
  currentPlayer: $('#current-player'),
  variantSpades: $('#variant-spades'),
  btnPickup: $('#btn-pickup'),
  actionHint: $('#action-hint'),
  moveList: $('#move-list'),
  btnUndo: $('#btn-undo'),
  btnRedo: $('#btn-redo'),
  stateJson: $('#state-json'),
  preset: $('#preset'),
  btnApply: $('#btn-apply-state'),
  btnExport: $('#btn-export-state'),
  evalUrl: $('#eval-url'),
  evalStatus: $('#eval-status'),
  evalContent: $('#eval-content'),
  evalScore: $('#eval-score'),
  evalBar: $('#eval-bar'),
  evalWin: $('#eval-win'),
  evalLose: $('#eval-lose'),
  evalNote: $('#eval-note'),
};

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

function renderCard(c, opts = {}) {
  const el = document.createElement('div');
  const red = c.suit === 'H' || c.suit === 'D';
  el.className = `card ${red ? 'card--red' : 'card--black'}`;
  el.dataset.id = cardId(c);
  if (opts.selected) el.classList.add('card--selected');
  if (opts.legal) el.classList.add('card--legal');
  if (opts.illegal) el.classList.add('card--illegal');
  if (opts.stack) el.classList.add('card--stack');
  el.innerHTML = `<span class="rank">${c.rank}</span><span class="suit">${{ H: '♥', D: '♦', C: '♣', S: '♠' }[c.suit]}</span>`;
  return el;
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

function legalQuadIncludingCard(state, playerIndex, card) {
  const id = cardId(card);
  return legalQuadPlays(state, playerIndex).find((quad) =>
    quad.some((c) => cardId(c) === id)
  );
}

function cardUsesMultiSelect(state, playerIndex, card) {
  if (legalQuadIncludingCard(state, playerIndex, card)) return true;
  const three = legalThreeNines(state, playerIndex);
  return three?.some((c) => cardId(c) === cardId(card)) ?? false;
}

/** Card ids that must be selected together (4 of a rank, or 3 nines with only 9♥ on stack). */
function multiSelectTargetIds(state, playerIndex, anchorCard) {
  const quad = legalQuadIncludingCard(state, playerIndex, anchorCard);
  if (quad) return new Set(quad.map(cardId));
  const three = legalThreeNines(state, playerIndex);
  if (three?.some((c) => cardId(c) === cardId(anchorCard))) {
    return new Set(three.map(cardId));
  }
  return new Set();
}

function isCompleteMultiSelection(state, playerIndex, selectedSet) {
  if (selectedSet.size === 0) return false;
  const anchor = parseCard([...selectedSet][0]);
  const target = multiSelectTargetIds(state, playerIndex, anchor);
  if (selectedSet.size !== target.size || target.size < 2) return false;
  for (const sid of selectedSet) {
    if (!target.has(sid)) return false;
  }
  return validatePlay(state, playerIndex, [...selectedSet].map(parseCard)).ok;
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
  const root = document.documentElement;
  const spread =
    Number.parseFloat(getComputedStyle(root).getPropertyValue('--stack-spread')) || 36;
  const underSpread =
    Number.parseFloat(getComputedStyle(root).getPropertyValue('--stack-under-spread')) || 8;

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
  const cardW = Number.parseFloat(getComputedStyle(root).getPropertyValue('--card-w')) || 52;
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
    const prev = history[i - 1];
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
  document.querySelectorAll('.player-area').forEach((area) => {
    const p = Number(area.dataset.player);
    area.classList.toggle('player-area--active', state.currentPlayer === p && !state.result);
  });

  renderHand(els.hand0, 0);
  renderHand(els.hand1, 1);
  renderStack();

  els.p0Count.textContent = `${state.players[0].hand.length} cards`;
  els.p1Count.textContent = `${state.players[1].hand.length} cards`;

  els.currentPlayer.value = String(state.currentPlayer);
  els.variantSpades.checked = state.variantSpades;

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

function localFallbackEval(gameState) {
  const p0 = gameState.players[0].hand.length;
  const p1 = gameState.players[1].hand.length;
  const total = p0 + p1 || 1;
  const score = ((p1 - p0) / total) * 2;
  const pi = gameState.currentPlayer;
  const can = canPlay(gameState, pi);
  return {
    score: Math.round(score * 100) / 100,
    winIn: can && pi === 0 && p0 <= 2 ? p0 : null,
    loseIn: can && pi === 0 && p1 <= 2 ? p1 : null,
    bestMove: null,
    note: 'Heuristic fallback (engine offline). Card count only.',
    source: 'fallback',
  };
}

async function fetchEval(gameState) {
  const url = els.evalUrl.value.trim() || '/api/evaluate';
  const payload = { state: serializeState(gameState) };

  try {
    const res = await fetch(url, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload),
    });
    if (!res.ok) throw new Error(`HTTP ${res.status}`);
    const data = await res.json();
    return {
      score: data.score ?? 0,
      winIn: data.winIn ?? data.win_in ?? null,
      loseIn: data.loseIn ?? data.lose_in ?? null,
      bestMove: data.bestMove ?? data.best_move ?? null,
      note: data.note ?? '',
      source: 'engine',
    };
  } catch {
    return localFallbackEval(gameState);
  }
}

function displayEval(data) {
  els.evalContent.classList.remove('hidden');
  els.evalStatus.className = `eval-status eval-status--${data.source === 'engine' ? 'engine' : 'fallback'}`;
  els.evalStatus.textContent =
    data.source === 'engine' ? 'Engine' : 'Fallback — engine unreachable';

  const score = Number(data.score);
  const scoreText = score > 0 ? `+${score.toFixed(2)}` : score.toFixed(2);
  els.evalScore.textContent = scoreText;
  els.evalScore.style.color = score > 0 ? 'var(--p1)' : score < 0 ? 'var(--p2)' : 'var(--text)';

  const pct = Math.max(0, Math.min(100, 50 + score * 25));
  els.evalBar.style.marginLeft = `${pct - 50}%`;
  els.evalBar.style.width = '50%';

  els.evalWin.textContent = data.winIn != null ? `${data.winIn} moves` : '—';
  els.evalLose.textContent = data.loseIn != null ? `${data.loseIn} moves` : '—';
  els.evalNote.textContent = data.note || '';
}

function scheduleEval() {
  clearTimeout(evalDebounce);
  els.evalStatus.className = 'eval-status eval-status--loading';
  els.evalStatus.textContent = 'Analyzing…';
  evalDebounce = setTimeout(async () => {
    const data = await fetchEval(state);
    displayEval(data);
  }, 280);
}

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
els.variantSpades.addEventListener('change', () => {
  state.variantSpades = els.variantSpades.checked;
  render();
});

els.stateJson.value = JSON.stringify(serializeState(state), null, 2);
render();
