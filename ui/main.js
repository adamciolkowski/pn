import { startAnalysis, getAnalysisState } from './analysis.js';
import { startSetup, importSetupState } from './setup.js';

const views = {
  setup: document.getElementById('view-setup'),
  analysis: document.getElementById('view-analysis'),
};

const nav = {
  setup: document.getElementById('nav-setup'),
  analysis: document.getElementById('nav-analysis'),
};

let analysisStarted = false;
let setupStarted = false;

function parseViewFromHash() {
  const hash = location.hash.replace(/^#\/?/, '');
  if (hash === 'analysis') return 'analysis';
  return 'setup';
}

function setActiveNav(view) {
  nav.setup.classList.toggle('nav-link--active', view === 'setup');
  nav.analysis.classList.toggle('nav-link--active', view === 'analysis');
}

export function showView(view, options = {}) {
  const name = view === 'analysis' ? 'analysis' : 'setup';
  views.setup.hidden = name !== 'setup';
  views.analysis.hidden = name !== 'analysis';
  setActiveNav(name);

  if (name === 'setup') {
    if (!setupStarted) {
      startSetup(options.state);
      setupStarted = true;
    } else if (options.state) {
      importSetupState(options.state);
    }
  } else if (name === 'analysis') {
    if (!analysisStarted) {
      startAnalysis(options.state);
      analysisStarted = true;
    } else if (options.reload) {
      startAnalysis(options.state);
    }
  }

  if (location.hash !== `#/${name}`) {
    history.replaceState(null, '', `#/${name}`);
  }
}

function onNavigate(e) {
  showView(e.detail.view, { state: e.detail.state });
}

function onAnalyze(e) {
  showView('analysis', { state: e.detail.state, reload: true });
}

nav.setup.addEventListener('click', (e) => {
  e.preventDefault();
  const state = analysisStarted ? getAnalysisState() : undefined;
  showView('setup', { state });
});

nav.analysis.addEventListener('click', (e) => {
  e.preventDefault();
  showView('analysis');
});

window.addEventListener('pan:navigate', onNavigate);
window.addEventListener('pan:analyze', onAnalyze);
window.addEventListener('hashchange', () => showView(parseViewFromHash()));

showView(parseViewFromHash());
