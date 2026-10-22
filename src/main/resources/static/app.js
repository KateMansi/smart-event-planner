(() => {
  'use strict';

  const TEXT = {
    en: {
      title: 'Smart Event Planner',
      subtitle: 'Tell us about your event and get a budget, checklist and tips.',
      language: 'Language', type: 'Event type', wedding: 'Wedding', birthday: 'Birthday',
      corporate: 'Corporate', party: 'Party', date: 'Event date', budget: 'Budget (₹)',
      guests: 'Number of guests', city: 'City', outdoor: 'This is an outdoor event',
      diet: 'Food preference', veg: 'Vegetarian', nonveg: 'Non-vegetarian', jain: 'Jain', halal: 'Halal',
      notes: 'Special notes (optional)', submit: 'Create my plan',
      loading: 'Creating your plan…', done: 'Your plan is ready.',
      resultsTitle: 'Your event plan', mode: 'Planning mode', perGuest: 'Budget per guest',
      budgetTitle: 'Budget breakdown', category: 'Category', amount: 'Amount',
      checklistTitle: 'Checklist', warningsTitle: 'Things to watch',
      noWarnings: 'No warnings. Your plan looks good.', aiTitle: 'AI summary',
      urgent: 'Urgent', standard: 'Standard',
      errGeneric: 'Something went wrong. Please try again.',
      errNetwork: 'Could not reach the server. Check your connection.',
      errRate: 'Too many requests. Please wait a minute and try again.',
      errValidation: 'Please correct these fields:'
    },
    hi: {
      title: 'स्मार्ट इवेंट प्लानर',
      subtitle: 'अपने आयोजन की जानकारी दें और बजट, चेकलिस्ट व सुझाव पाएं।',
      language: 'भाषा', type: 'आयोजन का प्रकार', wedding: 'शादी', birthday: 'जन्मदिन',
      corporate: 'कॉर्पोरेट', party: 'पार्टी', date: 'आयोजन की तारीख', budget: 'बजट (₹)',
      guests: 'मेहमानों की संख्या', city: 'शहर', outdoor: 'यह खुले में होने वाला आयोजन है',
      diet: 'भोजन की पसंद', veg: 'शाकाहारी', nonveg: 'मांसाहारी', jain: 'जैन', halal: 'हलाल',
      notes: 'विशेष टिप्पणी (वैकल्पिक)', submit: 'मेरा प्लान बनाएं',
      loading: 'आपका प्लान बन रहा है…', done: 'आपका प्लान तैयार है।',
      resultsTitle: 'आपका इवेंट प्लान', mode: 'प्लानिंग मोड', perGuest: 'प्रति मेहमान बजट',
      budgetTitle: 'बजट का विवरण', category: 'श्रेणी', amount: 'राशि',
      checklistTitle: 'चेकलिस्ट', warningsTitle: 'ध्यान देने योग्य बातें',
      noWarnings: 'कोई चेतावनी नहीं। आपका प्लान ठीक है।', aiTitle: 'AI सारांश',
      urgent: 'तुरंत', standard: 'सामान्य',
      errGeneric: 'कुछ गड़बड़ हो गई। कृपया फिर प्रयास करें।',
      errNetwork: 'सर्वर से संपर्क नहीं हो सका। अपना कनेक्शन जांचें।',
      errRate: 'बहुत अधिक अनुरोध। कृपया एक मिनट रुककर फिर प्रयास करें।',
      errValidation: 'कृपया इन फ़ील्ड को सुधारें:'
    },
    mr: {
      title: 'स्मार्ट इव्हेंट प्लॅनर',
      subtitle: 'तुमच्या कार्यक्रमाची माहिती द्या आणि बजेट, चेकलिस्ट व सूचना मिळवा.',
      language: 'भाषा', type: 'कार्यक्रमाचा प्रकार', wedding: 'लग्न', birthday: 'वाढदिवस',
      corporate: 'कॉर्पोरेट', party: 'पार्टी', date: 'कार्यक्रमाची तारीख', budget: 'बजेट (₹)',
      guests: 'पाहुण्यांची संख्या', city: 'शहर', outdoor: 'हा खुल्या जागेतील कार्यक्रम आहे',
      diet: 'जेवणाची पसंती', veg: 'शाकाहारी', nonveg: 'मांसाहारी', jain: 'जैन', halal: 'हलाल',
      notes: 'विशेष सूचना (ऐच्छिक)', submit: 'माझा प्लॅन तयार करा',
      loading: 'तुमचा प्लॅन तयार होत आहे…', done: 'तुमचा प्लॅन तयार आहे.',
      resultsTitle: 'तुमचा इव्हेंट प्लॅन', mode: 'प्लॅनिंग मोड', perGuest: 'प्रति पाहुणा बजेट',
      budgetTitle: 'बजेटचे विभाजन', category: 'प्रकार', amount: 'रक्कम',
      checklistTitle: 'चेकलिस्ट', warningsTitle: 'लक्षात ठेवा',
      noWarnings: 'कोणतीही सूचना नाही. तुमचा प्लॅन छान आहे.', aiTitle: 'AI सारांश',
      urgent: 'तातडीचा', standard: 'सामान्य',
      errGeneric: 'काहीतरी चूक झाली. कृपया पुन्हा प्रयत्न करा.',
      errNetwork: 'सर्व्हरशी संपर्क होऊ शकला नाही. तुमचे कनेक्शन तपासा.',
      errRate: 'खूप जास्त विनंत्या. कृपया एक मिनिट थांबून पुन्हा प्रयत्न करा.',
      errValidation: 'कृपया ही फील्ड दुरुस्त करा:'
    }
  };

  const LANG_CODE = { ENGLISH: 'en', HINDI: 'hi', MARATHI: 'mr' };
  const money = new Intl.NumberFormat('en-IN', {
    style: 'currency', currency: 'INR', maximumFractionDigits: 0
  });

  const $ = (id) => document.getElementById(id);
  const form = $('plan-form');
  const languageSelect = $('language');
  let lastPlan = null;

  const t = () => TEXT[LANG_CODE[languageSelect.value]];

  function applyLanguage() {
    const code = LANG_CODE[languageSelect.value];
    document.documentElement.lang = code;
    document.querySelectorAll('[data-i18n]').forEach((el) => {
      el.textContent = TEXT[code][el.dataset.i18n];
    });
    document.title = TEXT[code].title;
    if (lastPlan) {
      renderPlan(lastPlan);
    }
  }

  function setToday() {
    const now = new Date();
    const local = new Date(now.getTime() - now.getTimezoneOffset() * 60000);
    $('date').min = local.toISOString().slice(0, 10);
  }

  function showError(message) {
    const box = $('error');
    box.textContent = message;
    box.hidden = false;
  }

  function clearError() {
    $('error').hidden = true;
    $('error').textContent = '';
  }

  function fillList(listEl, items) {
    listEl.replaceChildren();
    items.forEach((text) => {
      const li = document.createElement('li');
      li.textContent = text;
      listEl.appendChild(li);
    });
  }

  function renderPlan(plan) {
    const text = t();
    const modeLabel = plan.mode === 'URGENT' ? text.urgent : text.standard;
    $('summary-line').textContent =
      `${text.mode}: ${modeLabel}. ${text.perGuest}: ${money.format(plan.perGuestBudget)}.`;

    const body = $('budget-body');
    body.replaceChildren();
    Object.entries(plan.budget).forEach(([name, amount]) => {
      const row = document.createElement('tr');
      const th = document.createElement('th');
      th.scope = 'row';
      th.textContent = name;
      const td = document.createElement('td');
      td.className = 'num';
      td.textContent = money.format(amount);
      row.append(th, td);
      body.appendChild(row);
    });

    fillList($('checklist'), plan.checklist);
    fillList($('warnings'), plan.warnings);
    const none = $('no-warnings');
    none.hidden = plan.warnings.length > 0;
    none.textContent = text.noWarnings;

    $('ai-summary').textContent = plan.aiSummary || '';
    $('results').hidden = false;
  }

  function buildPayload() {
    return {
      type: $('type').value,
      date: $('date').value,
      budget: Number($('budget').value),
      guests: Number($('guests').value),
      city: $('city').value.trim(),
      outdoor: $('outdoor').checked,
      diet: $('diet').value,
      language: languageSelect.value,
      notes: $('notes').value.trim()
    };
  }

  async function onSubmit(event) {
    event.preventDefault();
    clearError();
    const button = $('submit');
    const status = $('status');

    button.disabled = true;
    status.textContent = t().loading;

    try {
      const response = await fetch('/api/plan', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(buildPayload())
      });
        if (response.status === 429) {
        showError(t().errRate);
        status.textContent = '';
        return;
      }

      if (response.status === 400) {
        const data = await response.json().catch(() => ({}));
        const fields = data.fields ? Object.keys(data.fields).join(', ') : '';
        showError(`${t().errValidation} ${fields}`.trim());
        status.textContent = '';
        return;
      }
      if (!response.ok) {
        throw new Error(`HTTP ${response.status}`);
      }

      lastPlan = await response.json();
      renderPlan(lastPlan);
      status.textContent = t().done;
      $('results-title').focus();
    } catch (err) {
      showError(err instanceof TypeError ? t().errNetwork : t().errGeneric);
      status.textContent = '';
    } finally {
      button.disabled = false;
    }
  }

  languageSelect.addEventListener('change', applyLanguage);
  form.addEventListener('submit', onSubmit);
  setToday();
  applyLanguage();
})();