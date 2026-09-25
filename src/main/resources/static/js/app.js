/* ══════════════════════════════════════════════
   NOIR NAIL BAR — Frontend Logic
   API Base: /api  (Spring Boot static files)
   ══════════════════════════════════════════════ */

'use strict';

const API = '/api';

// ─── Türkçe Tarih Sabitleri ───
const TR_MONTHS = [
    'Ocak','Şubat','Mart','Nisan','Mayıs','Haziran',
    'Temmuz','Ağustos','Eylül','Ekim','Kasım','Aralık'
];
const TR_DAYS_SHORT = ['Pzt','Sal','Çar','Per','Cum','Cmt','Paz'];

// ─── Uygulama Durumu (State) ───
const state = {
    currentStep:       0,       // 0=hero, 1=hizmet, 2=tarih, 3=saat/form, 4=onay
    services:          [],
    selectedServices:  [],      // [{ id, name, price, durationInMinutes }, ...] — çoklu seçim
    selectedDate:      null,    // Date objesi
    staffList:         [],      // Aktif personel listesi
    selectedStaff:     null,    // { id, name } — null ise "Farketmez"
    availableSlots:    [],
    selectedSlot:      null,    // { startTime, endTime }
    calendarYear:      null,    // Takvim görünümü için
    calendarMonth:     null,
};

// Toplam süre hesaplama (birden fazla hizmet seçilince)
function getTotalDuration() {
    return state.selectedServices.reduce((sum, s) => sum + s.durationInMinutes, 0);
}

// Toplam fiyat hesaplama
function getTotalPrice() {
    return state.selectedServices.reduce((sum, s) => sum + s.price, 0);
}

// ─── Section ID Haritası ───
const SECTIONS = [
    'section-hero',
    'section-services',
    'section-date',
    'section-slots',
    'section-confirm',
];

/* ══════════════════════════════════════════════
   BAŞLANGIC
   ══════════════════════════════════════════════ */

document.addEventListener('DOMContentLoaded', async () => {
    // Takvim başlangıç tarihini yarına ayarla
    const tomorrow = new Date();
    tomorrow.setDate(tomorrow.getDate() + 1);
    state.calendarYear  = tomorrow.getFullYear();
    state.calendarMonth = tomorrow.getMonth();

    // Hizmetleri ve personelleri arka planda yükle
    await Promise.all([fetchServices(), fetchPublicStaff()]);

    // Form input'larına validation listener ekle
    document.getElementById('input-name').addEventListener('input', validateForm);
    document.getElementById('input-phone').addEventListener('input', validateForm);
});

/* ══════════════════════════════════════════════
   SECTION NAVİGASYON
   ══════════════════════════════════════════════ */

function startBooking() {
    goToStep(1);
}

async function goToStep(step) {
    // Adım 2'ye geçerken personeller henüz yüklenmediyse yükle
    if (step === 2) {
        if (!state.staffList || state.staffList.length === 0) {
            await fetchPublicStaff();
        }
        renderStaffSelection();
        renderCalendar();
        renderDateSidebar();
    }

    // Adım 3'e geçerken önce API'den slotları çek
    if (step === 3) {
        await loadAvailableSlots();
    }

    // Tüm sectionları gizle
    SECTIONS.forEach(id => {
        const el = document.getElementById(id);
        if (el) el.classList.remove('active');
    });

    // Hedef section'ı göster
    const target = document.getElementById(SECTIONS[step]);
    if (target) {
        target.classList.add('active');
    }

    // Navigation bar'ı güncelle
    updateNav(step);

    state.currentStep = step;
}

function updateNav(step) {
    const nav = document.getElementById('booking-nav');

    if (step === 0) {
        nav.classList.remove('visible');
        // Nav gizlenince yüksekliği sıfırla
        document.documentElement.style.setProperty('--nav-h', '0px');
        return;
    }

    nav.classList.add('visible');

    // Nav yüksekliğini CSS değişkenine yaz (bir sonraki frame'de ölçülür)
    requestAnimationFrame(() => {
        const navH = nav.getBoundingClientRect().height;
        document.documentElement.style.setProperty('--nav-h', `${navH}px`);
    });

    // Step indikatörlerini güncelle (adım 1-3 arası)
    for (let i = 1; i <= 3; i++) {
        const el = document.getElementById(`nav-step-${i}`);
        if (!el) continue;
        el.classList.remove('active', 'completed');

        if (i < step)       el.classList.add('completed');
        else if (i === step) el.classList.add('active');
    }
}

/* ══════════════════════════════════════════════
   API ÇAĞRILARI
   ══════════════════════════════════════════════ */

async function apiFetch(url, options = {}) {
    const res = await fetch(url, options);
    if (!res.ok) {
        let errMsg = 'Bir hata oluştu.';
        try {
            const body = await res.json();
            errMsg = body.message || errMsg;
        } catch (_) { /* JSON parse hatası → varsayılan mesaj */ }
        throw new Error(errMsg);
    }
    return res.json();
}

// GET /api/services
async function fetchServices() {
    try {
        state.services = await apiFetch(`${API}/services`);
        renderServices();
    } catch (err) {
        showToast('Hizmetler yüklenemedi. Lütfen sayfayı yenileyin.', 'error');
    }
}

// GET /api/staff/public
async function fetchPublicStaff() {
    try {
        const res = await apiFetch(`${API}/staff/public`);
        state.staffList = Array.isArray(res) ? res : [];
    } catch (err) {
        console.error('Personel listesi yüklenemedi:', err);
        state.staffList = [];
    }
}

// Staff Seçimi Rendering (Kompakt Pill Bar Düzeni)
function renderStaffSelection() {
    const container = document.getElementById('staff-selection-container');
    if (!container) return;

    const isNoStaffSelected = !state.selectedStaff;

    let html = `
        <div class="staff-section-title">
            <span>✦ Uzman Tercihi</span>
        </div>
        <div class="staff-bar-group">
            <button type="button" class="staff-pill ${isNoStaffSelected ? 'selected' : ''}" 
                    onclick="selectStaff(null)" aria-pressed="${isNoStaffSelected}">
                <span class="staff-pill-icon">✦</span>
                <span class="staff-pill-name">Farketmez (İlk Müsait)</span>
            </button>
    `;

    (state.staffList || []).forEach(staff => {
        const isSelected = state.selectedStaff && String(state.selectedStaff.id) === String(staff.id);
        const initials = staff.name ? staff.name.split(' ').map(n => n[0]).join('').toUpperCase().substring(0, 2) : 'U';
        html += `
            <button type="button" class="staff-pill ${isSelected ? 'selected' : ''}" 
                    onclick="selectStaff(${staff.id})" aria-pressed="${isSelected}">
                <span class="staff-pill-avatar">${initials}</span>
                <span class="staff-pill-name">${escapeHtml(staff.name)}</span>
            </button>
        `;
    });

    html += `</div>`;
    container.innerHTML = html;
}

function selectStaff(staffId) {
    if (!staffId) {
        state.selectedStaff = null;
    } else {
        const staff = (state.staffList || []).find(s => String(s.id) === String(staffId));
        state.selectedStaff = staff || null;
    }
    renderStaffSelection();
    renderDateSidebar();
    // Eğer 3. adımdaysak slotları yeniden yükle
    if (state.currentStep === 3) {
        loadAvailableSlots();
    }
}

// GET /api/appointments/available-slots?date=&serviceId=&totalDurationMinutes=&staffId=
async function loadAvailableSlots() {
    const container = document.getElementById('slots-container');
    container.innerHTML = `
        <div class="slots-loading">
            <div>
                <span class="slots-loading-dot"></span>
                <span class="slots-loading-dot"></span>
                <span class="slots-loading-dot"></span>
            </div>
            <p>Müsait saatler yükleniyor…</p>
        </div>
    `;

    state.selectedSlot = null;
    updateSubmitButton();

    const dateStr   = formatDateISO(state.selectedDate); // "YYYY-MM-DD"
    // İlk hizmetin ID'sini referans olarak kullan, toplam süreyi override olarak gönder
    const serviceId = state.selectedServices[0].id;
    const totalDur  = getTotalDuration();
    const staffParam = state.selectedStaff ? `&staffId=${state.selectedStaff.id}` : '';

    try {
        state.availableSlots = await apiFetch(
            `${API}/appointments/available-slots?date=${dateStr}&serviceId=${serviceId}&totalDurationMinutes=${totalDur}${staffParam}`
        );
        renderSlots();
    } catch (err) {
        container.innerHTML = `
            <div class="slots-empty">
                <p>😕 Saatler yüklenirken bir hata oluştu.</p>
                <p>${err.message}</p>
            </div>
        `;
    }
}

// POST /api/appointments (birden fazla hizmet için sırayla randevu oluşturur)
async function submitBooking(event) {
    event.preventDefault();

    const customerName  = document.getElementById('input-name').value.trim();
    const customerPhone = document.getElementById('input-phone').value.trim();

    // Telefon formatı doğrula
    if (!/^05\d{9}$/.test(customerPhone)) {
        document.getElementById('input-phone').classList.add('error');
        showToast('Telefon numarası 05 ile başlayan 11 haneli olmalıdır.', 'error');
        return;
    }

    const btn = document.getElementById('btn-submit');
    btn.disabled = true;
    btn.textContent = 'Gönderiliyor…';

    const dateStr = formatDateISO(state.selectedDate);
    let currentTime = state.selectedSlot.startTime; // "HH:MM:SS"

    try {
        let lastBooking = null;

        for (const service of state.selectedServices) {
            const appointmentTime = `${dateStr}T${currentTime}`;
            const payload = {
                customerName,
                customerPhone,
                serviceId: service.id,
                appointmentTime,
            };
            if (state.selectedStaff && state.selectedStaff.id) {
                payload.staffId = state.selectedStaff.id;
            }

            const booking = await apiFetch(`${API}/appointments`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(payload),
            });
            lastBooking = booking;
            // Bir sonraki randevu bu hizmetin bitiş saatinden başlar
            currentTime = booking.appointmentEndTime.substring(11); // "HH:MM:SS"
        }

        renderConfirmation(lastBooking);
        goToStep(4);
        showToast('Randevunuz başarıyla oluşturuldu! 🖤', 'success');

    } catch (err) {
        showToast(err.message, 'error');
        btn.disabled = false;
        btn.textContent = 'Randevuyu Onayla';
    }
}

/* ══════════════════════════════════════════════
   RENDER — Hizmet Kartları
   ══════════════════════════════════════════════ */

function renderServices() {
    const grid = document.getElementById('services-grid');
    grid.innerHTML = '';

    if (state.services.length === 0) {
        grid.innerHTML = '<p style="color:var(--c-cream-muted); text-align:center; grid-column:1/-1; padding:40px 0;">Henüz hizmet tanımlanmamış.</p>';
        return;
    }

    state.services.forEach(service => {
        const card = document.createElement('div');
        card.className = 'service-card';
        card.dataset.id = service.id;
        card.setAttribute('role', 'button');
        card.setAttribute('tabindex', '0');
        card.setAttribute('aria-pressed', 'false');

        card.innerHTML = `
            <div class="service-deco">✦ &nbsp; ✦ &nbsp; ✦</div>
            <h3 class="service-name">${escapeHtml(service.name)}</h3>
            <div class="service-meta">
                <span class="service-duration">⏱ ${service.durationInMinutes} dk</span>
                <span class="service-price">${formatPrice(service.price)} ₺</span>
            </div>
        `;

        card.addEventListener('click', () => toggleService(service, card));
        card.addEventListener('keydown', e => {
            if (e.key === 'Enter' || e.key === ' ') {
                e.preventDefault();
                toggleService(service, card);
            }
        });

        grid.appendChild(card);
    });
}

function toggleService(service, card) {
    const idx = state.selectedServices.findIndex(s => s.id === service.id);

    if (idx === -1) {
        // Seçili değilse ekle
        state.selectedServices.push(service);
        card.classList.add('selected');
        card.setAttribute('aria-pressed', 'true');
    } else {
        // Seçiliyse kaldır
        state.selectedServices.splice(idx, 1);
        card.classList.remove('selected');
        card.setAttribute('aria-pressed', 'false');
    }

    // Seçim sayacını ve özet panelini güncelle
    updateServicesFooter();
}

function updateServicesFooter() {
    const btn = document.getElementById('btn-services-next');
    const summary = document.getElementById('services-selection-summary');
    const count   = state.selectedServices.length;

    btn.disabled = count === 0;

    if (summary) {
        if (count === 0) {
            summary.innerHTML = '<span class="selection-hint">Birden fazla hizmet seçebilirsiniz</span>';
        } else {
            const totalDur   = getTotalDuration();
            const totalPrice = getTotalPrice();
            summary.innerHTML = `
                <div class="selection-chips">
                    ${state.selectedServices.map(s => `<span class="selection-chip">${escapeHtml(s.name)}</span>`).join('')}
                </div>
                <span class="sel-total-badge">⏱ ${totalDur} dk  ◆  ${formatPrice(totalPrice)} ₺</span>
            `;
        }
    }
}

/* ══════════════════════════════════════════════
   RENDER — Tarih Adımı Kenar Paneli
   ══════════════════════════════════════════════ */

function renderDateSidebar() {
    const sidebar = document.getElementById('date-sidebar');
    if (!sidebar) return;

    if (state.selectedServices.length === 0) {
        sidebar.innerHTML = '';
        return;
    }

    const totalDur   = getTotalDuration();
    const totalPrice = getTotalPrice();
    const staffText  = state.selectedStaff ? state.selectedStaff.name : 'Farketmez (İlk Müsait)';

    sidebar.innerHTML = `
        <div class="date-sidebar-inner">
            <h4 class="sidebar-title">Seçilen Hizmetler</h4>
            <ul class="sidebar-services-list">
                ${state.selectedServices.map(s => `
                    <li class="sidebar-service-item">
                        <span class="sidebar-service-name">${escapeHtml(s.name)}</span>
                        <div class="sidebar-service-meta">
                            <span class="sidebar-service-dur">⏱ ${s.durationInMinutes} dk</span>
                            <span class="sidebar-service-price">${formatPrice(s.price)} ₺</span>
                        </div>
                    </li>
                `).join('')}
            </ul>
            <div class="sidebar-divider"></div>
            <div class="sidebar-total-row">
                <span class="sidebar-total-label">Uzman Tercihi</span>
                <span class="sidebar-total-val gold">${escapeHtml(staffText)}</span>
            </div>
            <div class="sidebar-total-row">
                <span class="sidebar-total-label">Toplam Süre</span>
                <span class="sidebar-total-val">${totalDur} dk</span>
            </div>
            <div class="sidebar-total-row">
                <span class="sidebar-total-label">Toplam Ücret</span>
                <span class="sidebar-total-val gold">${formatPrice(totalPrice)} ₺</span>
            </div>
        </div>
    `;
}

/* ══════════════════════════════════════════════
   RENDER — Takvim
   ══════════════════════════════════════════════ */

function renderCalendar() {
    const cal = document.getElementById('calendar');
    const year  = state.calendarYear;
    const month = state.calendarMonth;

    // Ayın ilk günü ve gün sayısı
    const firstDayRaw  = new Date(year, month, 1).getDay();       // 0=Pazar
    const firstDayMon  = (firstDayRaw + 6) % 7;                   // 0=Pazartesi
    const daysInMonth  = new Date(year, month + 1, 0).getDate();

    // "Bugün" ve "Yarın" (minimum seçilebilir gün)
    const today = new Date(); today.setHours(0, 0, 0, 0);
    const minDate = new Date(today); minDate.setDate(today.getDate() + 1);

    // Maksimum seçilebilir gün: 3 ay ilerisi
    const maxDate = new Date(today);
    maxDate.setMonth(today.getMonth() + 3);

    // Önceki/sonraki ay navigasyon limiti
    const navMinDate = new Date(today);
    navMinDate.setDate(1);
    const navMaxDate = new Date(maxDate);
    navMaxDate.setDate(1);

    const prevDisabled = new Date(year, month - 1, 1) < navMinDate;
    const nextDisabled = new Date(year, month + 1, 1) > navMaxDate;

    // Gün isimleri satırı
    const dayNamesHTML = TR_DAYS_SHORT
        .map(d => `<div class="cal-day-name">${d}</div>`)
        .join('');

    // Boş hücreler (ayın ilk gününden önce)
    let daysHTML = '';
    for (let i = 0; i < firstDayMon; i++) {
        daysHTML += '<div class="cal-day empty"></div>';
    }

    // Gün hücreleri
    for (let d = 1; d <= daysInMonth; d++) {
        const cellDate = new Date(year, month, d);
        cellDate.setHours(0, 0, 0, 0);

        const isDisabled = cellDate < minDate || cellDate > maxDate;
        const isToday    = cellDate.getTime() === today.getTime();
        const isSelected = state.selectedDate &&
                           cellDate.getTime() === state.selectedDate.getTime();

        let cls = 'cal-day';
        if (isDisabled) cls += ' disabled';
        if (isToday)    cls += ' today';
        if (isSelected) cls += ' selected';

        const clickAttr = isDisabled
            ? ''
            : `onclick="selectDate(new Date(${year}, ${month}, ${d}))"`;

        daysHTML += `<div class="${cls}" ${clickAttr}>${d}</div>`;
    }

    cal.innerHTML = `
        <div class="cal-header">
            <button class="cal-nav-btn" onclick="changeMonth(-1)" ${prevDisabled ? 'disabled' : ''} aria-label="Önceki ay">
                <svg viewBox="0 0 20 20" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M13 4l-6 6 6 6"/>
                </svg>
            </button>
            <span class="cal-month-label">${TR_MONTHS[month]} ${year}</span>
            <button class="cal-nav-btn" onclick="changeMonth(1)" ${nextDisabled ? 'disabled' : ''} aria-label="Sonraki ay">
                <svg viewBox="0 0 20 20" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M7 4l6 6-6 6"/>
                </svg>
            </button>
        </div>
        <div class="cal-day-names">${dayNamesHTML}</div>
        <div class="cal-grid">${daysHTML}</div>
    `;
}

function changeMonth(delta) {
    state.calendarMonth += delta;
    if (state.calendarMonth < 0) {
        state.calendarMonth = 11;
        state.calendarYear--;
    } else if (state.calendarMonth > 11) {
        state.calendarMonth = 0;
        state.calendarYear++;
    }
    renderCalendar();
}

function selectDate(date) {
    state.selectedDate = date;
    document.getElementById('btn-date-next').disabled = false;
    renderCalendar(); // Seçili günü yeniden boyar
}

/* ══════════════════════════════════════════════
   RENDER — Saat Dilimleri
   ══════════════════════════════════════════════ */

function renderSlots() {
    const container = document.getElementById('slots-container');

    if (state.availableSlots.length === 0) {
        container.innerHTML = `
            <div class="slots-empty">
                <p>Bu gün için müsait saat bulunmuyor.</p>
                <p style="font-size:0.8rem; opacity:0.6; margin-top:4px;">Lütfen başka bir tarih seçin.</p>
            </div>
        `;
        return;
    }

    const grid = document.createElement('div');
    grid.className = 'slots-grid';

    state.availableSlots.forEach(slot => {
        const startLabel = slot.startTime.substring(0, 5);  // "10:00"
        const endLabel   = slot.endTime.substring(0, 5);    // "11:30"

        const chip = document.createElement('button');
        chip.type = 'button';
        chip.className = 'slot-chip';
        chip.innerHTML = `<strong>${startLabel}</strong><br><span style="opacity:0.6;font-size:0.7rem">${endLabel}</span>`;

        chip.addEventListener('click', () => selectSlot(slot, chip));
        grid.appendChild(chip);
    });

    container.innerHTML = '';
    container.appendChild(grid);
}

function selectSlot(slot, chip) {
    document.querySelectorAll('.slot-chip').forEach(c => c.classList.remove('selected'));
    chip.classList.add('selected');
    state.selectedSlot = slot;
    updateBookingSummary();
    validateForm();
}

/* ══════════════════════════════════════════════
   RENDER — Randevu Özeti
   ══════════════════════════════════════════════ */

function updateBookingSummary() {
    const summary = document.getElementById('booking-summary');
    if (!state.selectedSlot) {
        summary.classList.add('hidden');
        return;
    }

    const totalPrice = getTotalPrice();
    const totalDur   = getTotalDuration();

    const servicesHTML = state.selectedServices.length > 1
        ? state.selectedServices.map(s => `
            <div class="summary-row">
                <span class="summary-label">${escapeHtml(s.name)}</span>
                <span class="summary-value">${formatPrice(s.price)} ₺</span>
            </div>
          `).join('')
        : `
            <div class="summary-row">
                <span class="summary-label">Hizmet</span>
                <span class="summary-value highlight">${escapeHtml(state.selectedServices[0]?.name || '')}</span>
            </div>
          `;

    summary.classList.remove('hidden');
    summary.innerHTML = `
        ${servicesHTML}
        <div class="summary-row">
            <span class="summary-label">Uzman</span>
            <span class="summary-value">${state.selectedStaff ? escapeHtml(state.selectedStaff.name) : 'Farketmez (İlk Müsait)'}</span>
        </div>
        <div class="summary-row">
            <span class="summary-label">Tarih</span>
            <span class="summary-value">${formatDateTR(state.selectedDate)}</span>
        </div>
        <div class="summary-row">
            <span class="summary-label">Saat</span>
            <span class="summary-value highlight">${state.selectedSlot.startTime.substring(0,5)} – ${state.selectedSlot.endTime.substring(0,5)}</span>
        </div>
        <div class="summary-row">
            <span class="summary-label">Toplam Süre</span>
            <span class="summary-value">${totalDur} dk</span>
        </div>
        <div class="summary-row">
            <span class="summary-label">Toplam Ücret</span>
            <span class="summary-value highlight">${formatPrice(totalPrice)} ₺</span>
        </div>
    `;
}

/* ══════════════════════════════════════════════
   RENDER — Onay Sayfası
   ══════════════════════════════════════════════ */

function renderConfirmation(lastBooking) {
    const card = document.getElementById('confirm-card');

    const startTime = state.selectedSlot
        ? state.selectedSlot.startTime.substring(0, 5)
        : (lastBooking.appointmentTime ? lastBooking.appointmentTime.substring(11, 16) : '—');
    // Bitiş saati: kullanıcıya temizlik süresi dahil gösterilmez, slot'un bitiş saatini kullan
    const endTime = state.selectedSlot
        ? state.selectedSlot.endTime.substring(0, 5)
        : (lastBooking.appointmentEndTime ? lastBooking.appointmentEndTime.substring(11, 16) : '—');
    const dateObj = state.selectedDate || (lastBooking.appointmentTime ? new Date(lastBooking.appointmentTime) : null);

    const totalPrice = getTotalPrice();
    const totalDur   = getTotalDuration();

    const servicesListHTML = state.selectedServices.length > 1
        ? `<div class="confirm-row">
               <span class="confirm-label">Hizmetler</span>
               <span class="confirm-value gold">${state.selectedServices.map(s => escapeHtml(s.name)).join(' + ')}</span>
           </div>`
        : `<div class="confirm-row">
               <span class="confirm-label">Hizmet</span>
               <span class="confirm-value gold">${escapeHtml(state.selectedServices[0]?.name || lastBooking.serviceName || '')}</span>
           </div>`;

    const staffDisplayName = lastBooking.staffName || (state.selectedStaff ? state.selectedStaff.name : 'Farketmez (Atanacak)');

    card.innerHTML = `
        ${servicesListHTML}
        <div class="confirm-row">
            <span class="confirm-label">Uzman</span>
            <span class="confirm-value gold">${escapeHtml(staffDisplayName)}</span>
        </div>
        <div class="confirm-row">
            <span class="confirm-label">Tarih</span>
            <span class="confirm-value">${dateObj ? formatDateTR(dateObj) : '—'}</span>
        </div>
        <div class="confirm-row">
            <span class="confirm-label">Saat</span>
            <span class="confirm-value gold">${startTime} – ${endTime}</span>
        </div>
        <div class="confirm-row">
            <span class="confirm-label">Müşteri</span>
            <span class="confirm-value">${escapeHtml(lastBooking.customerName)}</span>
        </div>
        <div class="confirm-row">
            <span class="confirm-label">Telefon</span>
            <span class="confirm-value">${escapeHtml(lastBooking.customerPhone)}</span>
        </div>
        <div class="confirm-row">
            <span class="confirm-label">Toplam Süre</span>
            <span class="confirm-value">${totalDur} dk</span>
        </div>
        <div class="confirm-row">
            <span class="confirm-label">Toplam Ücret</span>
            <span class="confirm-value gold">${formatPrice(totalPrice)} ₺</span>
        </div>
    `;

    // Ekstra bileşenleri render et (harita, WhatsApp, çalışma saatleri)
    renderConfirmExtras();
}

/* ══════════════════════════════════════════════
   RENDER — Onay Sonrası Ekstra Bileşenler
   (Google Maps, WhatsApp, Çalışma Saatleri)
   ══════════════════════════════════════════════ */

function renderConfirmExtras() {
    const mapCol   = document.getElementById('confirm-map-col');
    const rightCol = document.getElementById('confirm-right-col');
    if (!mapCol || !rightCol) return;

    // ─── Stüdyo Bilgileri ───────────────────────────────────────────────────
    // ⚠️  Aşağıdaki değerleri kendi stüdyo bilgilerinize göre güncelleyin.
    const STUDIO = {
        phone:       '905441005398',
        mapsQuery:   "Noir+Nail+Bar+Atatürk+Mahallesi+Fazılpaşa+Sokak+Ataşehir+İstanbul",
        mapsEmbedQ:  "Noir%20Nail%20Bar%20Atat%C3%BCrk%20Mahallesi%20Faz%C4%B1lpa%C5%9Fa%20Sokak%20Ata%C5%9Fehir%20%C4%B0stanbul",
        whatsappMsg: encodeURIComponent('Merhaba, az önce randevu oluşturdum. Bir sorum olacaktı.'),
    };
    // ────────────────────────────────────────────────────────────────────────

    // ─── Çalışma Saatleri ───
    // Gün adları ve saatler: ihtiyaca göre düzenleyin
    const workingHours = [
        { day: 'Pazartesi', hours: '09:00 – 19:00', closed: false },
        { day: 'Salı',      hours: '09:00 – 19:00', closed: false },
        { day: 'Çarşamba',  hours: '09:00 – 19:00', closed: false },
        { day: 'Perşembe',  hours: '09:00 – 19:00', closed: false },
        { day: 'Cuma',      hours: '09:00 – 19:00', closed: false },
        { day: 'Cumartesi', hours: '09:00 – 19:00', closed: false },
        { day: 'Pazar',     hours: 'Kapalı',         closed: true  },
    ];

    // Bugünün sıra indeksini bul (0=Paz → 6, 1=Pzt → 0)
    const todayIdx = (new Date().getDay() + 6) % 7;

    const hoursRows = workingHours.map((row, i) => `
        <div class="ce-hours-row${i === todayIdx ? ' ce-hours-today' : ''}">
            <span class="ce-hours-day">${row.day}</span>
            <span class="ce-hours-val${row.closed ? ' ce-hours-closed' : ''}">${row.hours}</span>
        </div>
    `).join('');

    // ─── Sol Sütun: Harita ───────────────────────────────────────────────────
    mapCol.innerHTML = `
        <div class="ce-card ce-map-card">
            <div class="ce-card-header">
                <svg class="ce-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.6">
                    <path d="M12 2C8.13 2 5 5.13 5 9c0 5.25 7 13 7 13s7-7.75 7-13c0-3.87-3.13-7-7-7z"/>
                    <circle cx="12" cy="9" r="2.5"/>
                </svg>
                <span class="ce-card-title">Konumumuz</span>
            </div>
            <div class="ce-map-frame-wrapper">
                <iframe
                    class="ce-map-iframe"
                    loading="lazy"
                    allowfullscreen
                    referrerpolicy="no-referrer-when-downgrade"
                    src="https://maps.google.com/maps?q=${STUDIO.mapsEmbedQ}&output=embed&z=15"
                    title="Noir Nail Bar Konumu">
                </iframe>
            </div>
            <a  class="ce-map-btn btn-primary"
                href="https://www.google.com/maps/search/?api=1&query=${STUDIO.mapsQuery}"
                target="_blank"
                rel="noopener noreferrer"
                id="btn-open-maps">
                <svg class="btn-icon" viewBox="0 0 20 20" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M4 10h12M10 4l6 6-6 6"/>
                </svg>
                Google Maps'te Aç / Yol Tarifi Al
            </a>
        </div>
    `;

    // ─── Sağ Sütun: WhatsApp + Çalışma Saatleri ──────────────────────────────
    rightCol.innerHTML = `
        <!-- WhatsApp -->
        <div class="ce-card ce-wa-card">
            <div class="ce-card-header">
                <svg class="ce-icon ce-wa-icon" viewBox="0 0 24 24" fill="currentColor">
                    <path d="M17.472 14.382c-.297-.149-1.758-.867-2.03-.967-.273-.099-.471-.148-.67.15-.197.297-.767.966-.94 1.164-.173.199-.347.223-.644.075-.297-.15-1.255-.463-2.39-1.475-.883-.788-1.48-1.761-1.653-2.059-.173-.297-.018-.458.13-.606.134-.133.298-.347.446-.52.149-.174.198-.298.298-.497.099-.198.05-.371-.025-.52-.075-.149-.669-1.612-.916-2.207-.242-.579-.487-.5-.669-.51-.173-.008-.371-.01-.57-.01-.198 0-.52.074-.792.372-.272.297-1.04 1.016-1.04 2.479 0 1.462 1.065 2.875 1.213 3.074.149.198 2.096 3.2 5.077 4.487.709.306 1.262.489 1.694.625.712.227 1.36.195 1.871.118.571-.085 1.758-.719 2.006-1.413.248-.694.248-1.289.173-1.413-.074-.124-.272-.198-.57-.347z"/>
                    <path d="M12.003 2C6.477 2 2 6.477 2 12.003c0 1.853.507 3.594 1.392 5.089L2 22l5.042-1.364A9.965 9.965 0 0012.003 22C17.527 22 22 17.527 22 12.003 22 6.477 17.527 2 12.003 2zm0 18.162a8.146 8.146 0 01-4.16-1.144l-.298-.177-3.093.838.84-3.036-.196-.312A8.124 8.124 0 013.84 12.003C3.84 7.49 7.49 3.84 12.003 3.84c4.512 0 8.162 3.65 8.162 8.163 0 4.512-3.65 8.159-8.162 8.159z"/>
                </svg>
                <span class="ce-card-title">Hızlı İletişim</span>
            </div>
            <p class="ce-wa-desc">Aklınıza takılan soruları tek tıkla sorabilirsiniz.</p>
            <a  class="ce-wa-btn"
                href="https://wa.me/${STUDIO.phone}?text=${STUDIO.whatsappMsg}"
                target="_blank"
                rel="noopener noreferrer"
                id="btn-whatsapp">
                <svg viewBox="0 0 24 24" fill="currentColor" width="20" height="20" style="flex-shrink:0">
                    <path d="M17.472 14.382c-.297-.149-1.758-.867-2.03-.967-.273-.099-.471-.148-.67.15-.197.297-.767.966-.94 1.164-.173.199-.347.223-.644.075-.297-.15-1.255-.463-2.39-1.475-.883-.788-1.48-1.761-1.653-2.059-.173-.297-.018-.458.13-.606.134-.133.298-.347.446-.52.149-.174.198-.298.298-.497.099-.198.05-.371-.025-.52-.075-.149-.669-1.612-.916-2.207-.242-.579-.487-.5-.669-.51-.173-.008-.371-.01-.57-.01-.198 0-.52.074-.792.372-.272.297-1.04 1.016-1.04 2.479 0 1.462 1.065 2.875 1.213 3.074.149.198 2.096 3.2 5.077 4.487.709.306 1.262.489 1.694.625.712.227 1.36.195 1.871.118.571-.085 1.758-.719 2.006-1.413.248-.694.248-1.289.173-1.413-.074-.124-.272-.198-.57-.347z"/>
                    <path d="M12.003 2C6.477 2 2 6.477 2 12.003c0 1.853.507 3.594 1.392 5.089L2 22l5.042-1.364A9.965 9.965 0 0012.003 22C17.527 22 22 17.527 22 12.003 22 6.477 17.527 2 12.003 2zm0 18.162a8.146 8.146 0 01-4.16-1.144l-.298-.177-3.093.838.84-3.036-.196-.312A8.124 8.124 0 013.84 12.003C3.84 7.49 7.49 3.84 12.003 3.84c4.512 0 8.162 3.65 8.162 8.163 0 4.512-3.65 8.159-8.162 8.159z"/>
                </svg>
                WhatsApp'tan yazın
            </a>
        </div>

        <!-- Çalışma Saatleri -->
        <div class="ce-card ce-hours-card">
            <div class="ce-card-header">
                <svg class="ce-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.6">
                    <circle cx="12" cy="12" r="10"/>
                    <path d="M12 6v6l4 2"/>
                </svg>
                <span class="ce-card-title">Çalışma Saatleri</span>
            </div>
            <div class="ce-hours-list">
                ${hoursRows}
            </div>
        </div>
    `;
}

/* ══════════════════════════════════════════════
   FORM DOĞRULAMA
   ══════════════════════════════════════════════ */

function validateForm() {
    const name  = document.getElementById('input-name').value.trim();
    const phone = document.getElementById('input-phone').value.trim();
    const phoneInput = document.getElementById('input-phone');

    // Hata sınıfını sadece değer varken göster
    if (phone.length > 0 && !/^05\d{9}$/.test(phone)) {
        phoneInput.classList.add('error');
    } else {
        phoneInput.classList.remove('error');
    }

    const isValid = name.length > 0 &&
                    /^05\d{9}$/.test(phone) &&
                    state.selectedSlot !== null;

    document.getElementById('btn-submit').disabled = !isValid;
}

function updateSubmitButton() {
    document.getElementById('btn-submit').disabled = true;
}

/* ══════════════════════════════════════════════
   SIFIRLAMA
   ══════════════════════════════════════════════ */

function resetBooking() {
    state.selectedServices = [];
    state.selectedDate     = null;
    state.selectedStaff    = null;
    state.availableSlots   = [];
    state.selectedSlot     = null;

    const tomorrow = new Date();
    tomorrow.setDate(tomorrow.getDate() + 1);
    state.calendarYear  = tomorrow.getFullYear();
    state.calendarMonth = tomorrow.getMonth();

    // Hizmet kartlarının seçim görünümünü temizle
    document.querySelectorAll('.service-card').forEach(c => {
        c.classList.remove('selected');
        c.setAttribute('aria-pressed', 'false');
    });

    // Formları sıfırla
    document.getElementById('input-name').value  = '';
    document.getElementById('input-phone').value = '';
    document.getElementById('input-name').classList.remove('error');
    document.getElementById('input-phone').classList.remove('error');
    document.getElementById('booking-summary').classList.add('hidden');

    // Butonları sıfırla
    document.getElementById('btn-services-next').disabled = true;
    document.getElementById('btn-date-next').disabled = true;
    document.getElementById('btn-submit').disabled = true;
    document.getElementById('btn-submit').textContent = 'Randevuyu Onayla';

    // Seçim özetini sıfırla
    updateServicesFooter();

    goToStep(1);
}

/* ══════════════════════════════════════════════
   YARDIMCI FONKSIYONLAR
   ══════════════════════════════════════════════ */

/** Date → "YYYY-MM-DD" */
function formatDateISO(date) {
    const y  = date.getFullYear();
    const m  = String(date.getMonth() + 1).padStart(2, '0');
    const d  = String(date.getDate()).padStart(2, '0');
    return `${y}-${m}-${d}`;
}

/** Date → "15 Ağustos 2026, Cmt" */
function formatDateTR(date) {
    const dayOfWeek = TR_DAYS_SHORT[(date.getDay() + 6) % 7];
    return `${date.getDate()} ${TR_MONTHS[date.getMonth()]} ${date.getFullYear()}, ${dayOfWeek}`;
}

/** 1900.0 → "1.900" */
function formatPrice(price) {
    return Number(price).toLocaleString('tr-TR');
}

/** XSS koruması */
function escapeHtml(str) {
    if (!str) return '';
    return String(str)
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;');
}

/** Toast bildirimi göster */
let toastTimer = null;
function showToast(message, type = 'info') {
    const toast = document.getElementById('toast');
    if (toastTimer) clearTimeout(toastTimer);

    toast.textContent = message;
    toast.className   = `toast ${type} visible`;

    toastTimer = setTimeout(() => {
        toast.classList.remove('visible');
    }, 4000);
}
