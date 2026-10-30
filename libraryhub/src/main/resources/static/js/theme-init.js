/* Runs in the head before first paint: applies the saved theme, or the system preference on a first visit. */
(function () {
  'use strict';
  var theme = null;
  try {
    var saved = localStorage.getItem('libraryhub-theme');
    if (saved === 'light' || saved === 'dark') {
      theme = saved;
    }
  } catch (e) {
    theme = null; /* storage is blocked: fall back to the system preference */
  }
  if (!theme) {
    theme = window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light';
  }
  document.documentElement.setAttribute('data-theme', theme);
})();
