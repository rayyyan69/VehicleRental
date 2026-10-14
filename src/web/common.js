// Shared helpers used by all three pages.

function renderShell(active, title, subtitle) {
    const links = [['vehicles.html', 'Vehicles'], ['customers.html', 'Customers'], ['rentals.html', 'Rentals']];
    document.body.insertAdjacentHTML('afterbegin', `
        <div class="topbar"><div class="inner">
            <div class="logo">RentRide <span>Vehicle Rental System</span></div>
            <nav>${links.map(l => `<a href="${l[0]}" class="${l[1] === active ? 'active' : ''}">${l[1]}</a>`).join('')}</nav>
        </div></div>
        <div id="toasts"></div>`);
    document.getElementById('head').innerHTML = `<h1>${title}</h1><p>${subtitle}</p>`;
}

function toast(text, ok = true) {
    const t = document.createElement('div');
    t.className = 'toast ' + (ok ? 'ok' : 'err');
    t.textContent = text;
    document.getElementById('toasts').appendChild(t);
    setTimeout(() => { t.classList.add('out'); setTimeout(() => t.remove(), 300); }, 3200);
}

function esc(s) {
    return String(s).replace(/[&<>"']/g, c => ({'&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;'}[c]));
}

const money = n => '₹' + Number(n).toLocaleString('en-IN', {maximumFractionDigits: 2});

async function getJson(url) {
    const res = await fetch(url);
    return res.json();
}

// POSTs form-like data; shows a toast and returns the parsed response, or null on error.
async function postData(url, data, quiet = false) {
    try {
        const res = await fetch(url, {
            method: 'POST',
            headers: {'Content-Type': 'application/x-www-form-urlencoded'},
            body: new URLSearchParams(data)
        });
        const json = await res.json();
        if (!res.ok) { if (!quiet) toast(json.error, false); return null; }
        return json;
    } catch (e) {
        toast('Cannot reach the server. Is Main running?', false);
        return null;
    }
}

function submitForm(formId, url, message, onDone) {
    const form = document.getElementById(formId);
    form.addEventListener('submit', async e => {
        e.preventDefault();
        const json = await postData(url, new FormData(form));
        if (json) {
            toast(json.message || message);
            form.reset();
            if (form.dataset.reset) window[form.dataset.reset]();
            onDone();
        }
    });
}

function fillTable(id, headers, rows, emptyText) {
    const head = '<thead><tr>' + headers.map(h => `<th>${h}</th>`).join('') + '</tr></thead>';
    const body = rows.length
        ? rows.map(r => '<tr>' + r.map(c => `<td>${c}</td>`).join('') + '</tr>').join('')
        : `<tr><td class="empty" colspan="${headers.length}">${emptyText}</td></tr>`;
    document.getElementById(id).innerHTML = head + '<tbody>' + body + '</tbody>';
}

const typeBadge = t => `<span class="badge ${{Car: 'blue', Bike: 'amber', Truck: 'brand'}[t]}">${t}</span>`;
