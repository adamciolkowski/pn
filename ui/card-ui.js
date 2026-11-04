/** Shared DOM card rendering for analysis and setup views. */

export function renderCardElement(c, opts = {}) {
  const el = document.createElement('div');
  const red = c.suit === 'H' || c.suit === 'D';
  el.className = `card ${red ? 'card--red' : 'card--black'}`;
  el.dataset.id = `${c.rank}${c.suit}`;
  if (opts.selected) el.classList.add('card--selected');
  if (opts.legal) el.classList.add('card--legal');
  if (opts.illegal) el.classList.add('card--illegal');
  if (opts.stack) el.classList.add('card--stack');
  if (opts.draggable) {
    el.draggable = true;
    el.classList.add('card--draggable');
  }
  if (opts.dimmed) el.classList.add('card--dimmed');
  el.innerHTML = `<span class="rank">${c.rank}</span><span class="suit">${{ H: '♥', D: '♦', C: '♣', S: '♠' }[c.suit]}</span>`;
  return el;
}

export const DRAG_MIME = 'application/x-pan-card';
export const DRAG_FROM_MIME = 'application/x-pan-from';

export function setDragPayload(e, cardId, from) {
  e.dataTransfer.setData(DRAG_MIME, cardId);
  e.dataTransfer.setData(DRAG_FROM_MIME, JSON.stringify(from));
  e.dataTransfer.effectAllowed = 'move';
}

export function readDragPayload(e) {
  const cardId = e.dataTransfer.getData(DRAG_MIME);
  if (!cardId) return null;
  let from = { zone: 'pool' };
  try {
    from = JSON.parse(e.dataTransfer.getData(DRAG_FROM_MIME) || '{}');
  } catch {
    from = { zone: 'pool' };
  }
  return { cardId, from };
}

export function bindDropZone(el, onDrop) {
  el.addEventListener('dragover', (e) => {
    e.preventDefault();
    e.dataTransfer.dropEffect = 'move';
    el.classList.add('drop-zone--over');
  });
  el.addEventListener('dragleave', (e) => {
    if (!el.contains(e.relatedTarget)) el.classList.remove('drop-zone--over');
  });
  el.addEventListener('drop', (e) => {
    e.preventDefault();
    el.classList.remove('drop-zone--over');
    const payload = readDragPayload(e);
    if (payload) onDrop(payload, e);
  });
}
