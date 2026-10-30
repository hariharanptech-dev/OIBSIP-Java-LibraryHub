/* LibraryHub front-end behaviour. Plain JavaScript, no dependencies. */
(function () {
  'use strict';

  var THEME_KEY = 'libraryhub-theme';

  /* ---------- Theme ---------- */
  function currentTheme() {
    return document.documentElement.getAttribute('data-theme') === 'dark' ? 'dark' : 'light';
  }

  function refreshThemeButtons() {
    var dark = currentTheme() === 'dark';
    document.querySelectorAll('[data-theme-toggle]').forEach(function (button) {
      button.setAttribute('aria-label', dark ? 'Switch to light theme' : 'Switch to dark theme');
    });
  }

  function setTheme(theme) {
    document.documentElement.setAttribute('data-theme', theme);
    try {
      localStorage.setItem(THEME_KEY, theme);
    } catch (e) {
      /* storage is blocked: the choice still applies to this page */
    }
    refreshThemeButtons();
  }

  document.querySelectorAll('[data-theme-toggle]').forEach(function (button) {
    button.addEventListener('click', function () {
      setTheme(currentTheme() === 'dark' ? 'light' : 'dark');
    });
  });
  refreshThemeButtons();

  /* ---------- Password show and hide ---------- */
  document.querySelectorAll('[data-toggle-password]').forEach(function (button) {
    var input = document.getElementById(button.getAttribute('data-toggle-password'));
    if (!input) { return; }
    button.addEventListener('click', function () {
      var reveal = input.type === 'password';
      input.type = reveal ? 'text' : 'password';
      button.classList.toggle('is-on', reveal);
      button.setAttribute('aria-label', reveal ? 'Hide password' : 'Show password');
    });
  });

  /* ---------- Password strength meter ---------- */
  var STRENGTH_LABELS = ['Too weak', 'Weak', 'Fair', 'Good', 'Strong'];

  function strengthScore(value) {
    var score = 0;
    if (value.length >= 8) { score++; }
    if (value.length >= 12) { score++; }
    if (/[a-z]/.test(value) && /[A-Z]/.test(value)) { score++; }
    if (/\d/.test(value)) { score++; }
    if (/[^A-Za-z0-9]/.test(value)) { score++; }
    return Math.min(score, 4);
  }

  document.querySelectorAll('[data-strength-for]').forEach(function (meter) {
    var input = document.getElementById(meter.getAttribute('data-strength-for'));
    var text = meter.querySelector('.strength-text');
    if (!input || !text) { return; }
    var hint = text.textContent;
    input.addEventListener('input', function () {
      if (!input.value) {
        meter.setAttribute('data-level', '0');
        text.textContent = hint;
        return;
      }
      var score = strengthScore(input.value);
      meter.setAttribute('data-level', String(score));
      text.textContent = 'Password strength: ' + STRENGTH_LABELS[score];
    });
  });

  /* ---------- Instant form checks (the server repeats every check) ---------- */
  var CHECKS = {
    name: function (value) {
      var length = value.trim().length;
      return length >= 2 && length <= 80 ? '' : 'Enter your full name (2 to 80 characters).';
    },
    email: function (value) {
      return /^[^@\s]+@[^@\s]+\.[^@\s]{2,}$/.test(value.trim()) ? '' : 'Enter a valid email address, like name@example.com.';
    },
    password: function (value, form) {
      if (!form.hasAttribute('data-password-rules')) {
        return value ? '' : 'Enter your password.';
      }
      return /^(?=.*[A-Za-z])(?=.*\d).{8,72}$/.test(value) ? '' : 'Use 8 to 72 characters with at least one letter and one number.';
    },
    confirmPassword: function (value, form) {
      return value && value === form.elements.password.value ? '' : "The passwords don't match.";
    }
  };

  function showFieldState(input, message) {
    var field = input.closest('.field');
    var error = document.getElementById(input.id + '-error');
    if (field) { field.classList.toggle('has-error', Boolean(message)); }
    input.setAttribute('aria-invalid', message ? 'true' : 'false');
    if (error) {
      error.textContent = message;
      error.hidden = !message;
    }
  }

  function validateInput(input, form) {
    var check = CHECKS[input.name];
    if (!check) { return true; }
    var message = check(input.value, form);
    showFieldState(input, message);
    return !message;
  }

  document.querySelectorAll('form[data-validate]').forEach(function (form) {
    var inputs = Array.prototype.slice.call(form.querySelectorAll('input')).filter(function (input) {
      return Boolean(CHECKS[input.name]);
    });

    inputs.forEach(function (input) {
      input.addEventListener('blur', function () {
        if (input.value || input.getAttribute('aria-invalid') === 'true') { validateInput(input, form); }
      });
      input.addEventListener('input', function () {
        if (input.getAttribute('aria-invalid') === 'true') { validateInput(input, form); }
      });
    });

    form.addEventListener('submit', function (event) {
      var firstInvalid = null;
      inputs.forEach(function (input) {
        if (!validateInput(input, form) && !firstInvalid) { firstInvalid = input; }
      });
      if (firstInvalid) {
        event.preventDefault();
        firstInvalid.focus();
        return;
      }
      /* Prevent double submits: show the spinner and disable the button once the submit has started. */
      var button = form.querySelector('button[type="submit"]');
      if (button) {
        button.classList.add('is-loading');
        button.setAttribute('aria-busy', 'true');
        window.setTimeout(function () { button.disabled = true; }, 0);
      }
    });
  });

  /* Restore buttons when the browser brings the page back from its back/forward cache. */
  window.addEventListener('pageshow', function (event) {
    if (!event.persisted) { return; }
    document.querySelectorAll('button.is-loading').forEach(function (button) {
      button.classList.remove('is-loading');
      button.removeAttribute('aria-busy');
      button.disabled = false;
    });
  });

  /* ---------- Demo account buttons on the sign-in page ---------- */
  document.querySelectorAll('.demo-fill').forEach(function (button) {
    button.addEventListener('click', function () {
      var email = document.getElementById('email');
      var password = document.getElementById('password');
      if (!email || !password) { return; }
      email.value = button.getAttribute('data-email');
      password.value = button.getAttribute('data-password');
      password.focus();
    });
  });

  /* ---------- Flash messages: close button and auto-hide for success ---------- */
  document.querySelectorAll('[data-flash]').forEach(function (flash) {
    var close = document.createElement('button');
    close.type = 'button';
    close.className = 'flash-close';
    close.setAttribute('aria-label', 'Dismiss message');
    close.innerHTML = '<svg class="icon" aria-hidden="true"><use href="#i-x"/></svg>';
    close.addEventListener('click', function () { flash.hidden = true; });
    flash.appendChild(close);
    if (flash.classList.contains('flash-success')) {
      window.setTimeout(function () { flash.hidden = true; }, 6000);
    }
  });

  /* ---------- Admin sidebar drawer on small screens ---------- */
  var toggle = document.querySelector('[data-sidebar-toggle]');
  var backdrop = document.querySelector('[data-sidebar-close]');

  function setSidebar(open) {
    document.body.classList.toggle('sidebar-open', open);
    if (toggle) {
      toggle.setAttribute('aria-expanded', open ? 'true' : 'false');
      toggle.setAttribute('aria-label', open ? 'Close navigation' : 'Open navigation');
    }
    if (backdrop) { backdrop.hidden = !open; }
  }

  if (toggle) {
    toggle.addEventListener('click', function () {
      setSidebar(!document.body.classList.contains('sidebar-open'));
    });
  }
  if (backdrop) {
    backdrop.addEventListener('click', function () { setSidebar(false); });
  }
  document.addEventListener('keydown', function (event) {
    if (event.key === 'Escape' && document.body.classList.contains('sidebar-open')) {
      setSidebar(false);
      if (toggle) { toggle.focus(); }
    }
  });
})();
