import {
  cardId,
  parseCard,
  sortHand,
  standardDeal,
  midgameExample,
  cloneState,
  playerWithNineHearts,
  emptySetupState,
  RANKS,
  SUITS,
  SUIT_SYMBOL,
} from './game.js';
import {
  renderCardElement,
  setDragPayload,
  bindDropZone,
} from './card-ui.js';

const $ = (sel, root) => root.querySelector(sel);

let rootEl;
let editorState = emptySetupState();
let els = {};
let bound = false;

function placedIds() {
  const ids = new Set();
  for (const p of editorState.players) {
    for (const c of p) ids.add(cardId(c));
  }
  for (const c of editorState.stack) ids.add(cardId(c));
  return ids;
}

function moveCard(cardIdStr, from, to, stackIndex = null) {
  const card = parseCard(cardIdStr);
  if (from.zone === 'hand') {
    const hand = editorState.players[from.player];
    const idx = hand.findIndex((c) => cardId(c) === cardIdStr);
    if (idx === -1) return;
    hand.splice(idx, 1);
  } else if (from.zone === 'stack') {
    editorState.stack.splice(from.index, 1);
    if (to.zone === 'stack' && stackIndex != null && from.index < stackIndex) {
      stackIndex -= 1;
    }
  } else if (from.zone !== 'pool') {
    return;
  }

  if (to.zone === 'pool') {
    return;
  }
  if (to.zone === 'hand') {
    editorState.players[to.player].push(card);
    editorState.players[to.player] = sortHand(editorState.players[to.player]);
  } else if (to.zone === 'stack') {
    const at = stackIndex == null ? editorState.stack.length : stackIndex;
    editorState.stack.splice(at, 0, card);
  }
}

function stackDropIndex(container, clientX) {
  const cards = [...container.querySelectorAll('.card[data-id]')];
  if (!cards.length) return 0;
  for (let i = 0; i < cards.length; i++) {
    const rect = cards[i].getBoundingClientRect();
    const mid = rect.left + rect.width / 2;
    if (clientX < mid) return i;
  }
  return cards.length;
}

function attachCardDrag(el, card, from) {
  el.draggable = true;
  el.classList.add('card--draggable');
  el.addEventListener('dragstart', (e) => {
    setDragPayload(e, cardId(card), from);
    el.classList.add('card--dragging');
  });
  el.addEventListener('dragend', () => {
    el.classList.remove('card--dragging');
  });
}

function renderPool() {
  els.pool.innerHTML = '';
  const placed = placedIds();
  let inPoolCount = 0;

  const grid = document.createElement('div');
  grid.className = 'setup-pool-grid';

  const corner = document.createElement('div');
  corner.className = 'setup-pool-corner';
  grid.appendChild(corner);

  for (const rank of RANKS) {
    const head = document.createElement('div');
    head.className = 'setup-pool-rank-head';
    head.textContent = rank;
    grid.appendChild(head);
  }

  for (const suit of SUITS) {
    const suitLabel = document.createElement('div');
    suitLabel.className = `setup-pool-suit setup-pool-suit--${suit}`;
    suitLabel.textContent = SUIT_SYMBOL[suit];
    suitLabel.title = suit;
    grid.appendChild(suitLabel);

    for (const rank of RANKS) {
      const cell = document.createElement('div');
      cell.className = 'setup-pool-cell';
      const id = `${rank}${suit}`;
      if (!placed.has(id)) {
        inPoolCount += 1;
        const c = { rank, suit };
        const el = renderCardElement(c, { draggable: true });
        attachCardDrag(el, c, { zone: 'pool' });
        cell.appendChild(el);
      } else {
        cell.classList.add('setup-pool-cell--empty');
        cell.dataset.card = id;
      }
      grid.appendChild(cell);
    }
  }

  els.pool.appendChild(grid);
  els.poolCount.textContent = `${inPoolCount} in pool`;
}

function renderEditorHand(container, playerIndex) {
  container.innerHTML = '';
  const hand = sortHand(editorState.players[playerIndex]);
  for (const c of hand) {
    const el = renderCardElement(c, { draggable: true });
    attachCardDrag(el, c, { zone: 'hand', player: playerIndex });
    container.appendChild(el);
  }
}

function renderEditorStack() {
  els.stackStrip.innerHTML = '';
  editorState.stack.forEach((c, index) => {
    const el = renderCardElement(c, { draggable: true, stack: true });
    el.style.position = 'relative';
    el.style.left = '0';
    attachCardDrag(el, c, { zone: 'stack', index });
    els.stackStrip.appendChild(el);
  });
}

function updatePlayerHighlight() {
  const pi = editorState.currentPlayer;
  els.areaP0.classList.toggle('setup-player--active', pi === 0);
  els.areaP1.classList.toggle('setup-player--active', pi === 1);
  els.sideToMove.value = String(pi);
}

function updateSummary() {
  const placed = placedIds().size;
  els.summary.textContent = `${placed} / 24 cards placed · P1: ${editorState.players[0].length} · P2: ${editorState.players[1].length} · Stack: ${editorState.stack.length}`;
  els.setupStatus.textContent = '';
  els.setupStatus.className = 'setup-status';
}

function render() {
  renderPool();
  renderEditorHand(els.handP0, 0);
  renderEditorHand(els.handP1, 1);
  renderEditorStack();
  updatePlayerHighlight();
  updateSummary();
}

function loadEditorState(next) {
  editorState = cloneState(next);
  delete editorState.result;
  delete editorState.winner;
  render();
}

function bindDropHandlers() {
  bindDropZone(els.pool, ({ cardId: id, from }, e) => {
    moveCard(id, from, { zone: 'pool' });
    render();
  });

  bindDropZone(els.handP0, ({ cardId: id, from }) => {
    moveCard(id, from, { zone: 'hand', player: 0 });
    render();
  });

  bindDropZone(els.handP1, ({ cardId: id, from }) => {
    moveCard(id, from, { zone: 'hand', player: 1 });
    render();
  });

  bindDropZone(els.stackDropPad, ({ cardId: id, from }, e) => {
    const idx = stackDropIndex(els.stackStrip, e.clientX);
    moveCard(id, from, { zone: 'stack' }, idx);
    render();
  });
}

function bindEvents() {
  if (bound) return;
  bound = true;
  bindDropHandlers();

  els.sideToMove.addEventListener('change', () => {
    editorState.currentPlayer = Number(els.sideToMove.value);
    updatePlayerHighlight();
  });

  els.btnClear.addEventListener('click', () => loadEditorState(emptySetupState()));

  els.btnPresetStandard.addEventListener('click', () => {
    const s = standardDeal(true);
    delete s.result;
    loadEditorState(s);
  });

  els.btnPresetMidgame.addEventListener('click', () => {
    const s = midgameExample();
    delete s.result;
    loadEditorState(s);
  });

  els.btnAnalyze.addEventListener('click', () => {
    const placed = placedIds().size;
    if (placed === 0) {
      els.setupStatus.textContent = 'Place at least one card before analyzing.';
      els.setupStatus.className = 'setup-status setup-status--error';
      return;
    }
    window.dispatchEvent(
      new CustomEvent('pan:analyze', {
        detail: { state: cloneState(editorState) },
      })
    );
  });
}

function cacheElements() {
  els = {
    pool: $('#setup-pool', rootEl),
    poolCount: $('#setup-pool-count', rootEl),
    handP0: $('#setup-hand-0', rootEl),
    handP1: $('#setup-hand-1', rootEl),
    stackStrip: $('#setup-stack', rootEl),
    stackDropPad: $('#setup-stack-pad', rootEl),
    areaP0: $('#setup-area-0', rootEl),
    areaP1: $('#setup-area-1', rootEl),
    sideToMove: $('#setup-side-to-move', rootEl),
    summary: $('#setup-summary', rootEl),
    setupStatus: $('#setup-status', rootEl),
    btnClear: $('#setup-clear', rootEl),
    btnPresetStandard: $('#setup-preset-standard', rootEl),
    btnPresetMidgame: $('#setup-preset-midgame', rootEl),
    btnAnalyze: $('#setup-analyze', rootEl),
  };
}

export function startSetup(initialState) {
  rootEl = document.getElementById('view-setup');
  cacheElements();
  bindEvents();
  if (initialState) {
    loadEditorState(initialState);
  } else {
    render();
  }
}

export function getSetupState() {
  return cloneState(editorState);
}

/** Import position from analysis JSON panel or external state. */
export function importSetupState(state) {
  if (!rootEl) return;
  loadEditorState(state);
}

export function exportSetupState() {
  return cloneState(editorState);
}
