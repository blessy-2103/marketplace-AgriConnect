/* ==========================================================
   AgriConnect - API wrapper
   Talks to the Spring Boot backend at /api/**.
   Since the frontend is served as static resources from the
   same Spring Boot app, requests use relative paths.
   ========================================================== */

const API_BASE = '/api';

const Auth = {
    getToken() { return localStorage.getItem('ac_token'); },
    setToken(t) { localStorage.setItem('ac_token', t); },
    clearToken() { localStorage.removeItem('ac_token'); },

    getUser() {
        const raw = localStorage.getItem('ac_user');
        return raw ? JSON.parse(raw) : null;
    },
    setUser(u) { localStorage.setItem('ac_user', JSON.stringify(u)); },
    clearUser() { localStorage.removeItem('ac_user'); },

    isLoggedIn() { return !!this.getToken(); },
    isFarmer() { const u = this.getUser(); return u && u.role === 'FARMER'; },
    isCustomer() { const u = this.getUser(); return u && u.role === 'CUSTOMER'; },

    logout() {
        api('/auth/logout', 'POST').catch(() => {});
        this.clearToken();
        this.clearUser();
        window.location.href = '/index.html';
    }
};

/**
 * Core request helper.
 * @param {string} path - path relative to /api, e.g. '/products'
 * @param {string} method
 * @param {object|null} body
 * @returns {Promise<any>} parsed JSON body (or null for 204)
 */
async function api(path, method = 'GET', body = null) {
    const headers = { 'Content-Type': 'application/json' };
    const token = Auth.getToken();
    if (token) headers['Authorization'] = `Bearer ${token}`;

    const res = await fetch(API_BASE + path, {
        method,
        headers,
        body: body ? JSON.stringify(body) : undefined
    });

    if (res.status === 204) return null;

    let data;
    try {
        data = await res.json();
    } catch (e) {
        data = null;
    }

    if (!res.ok) {
        const message = (data && data.message) ? data.message :
            (data && data.errors) ? Object.values(data.errors).join(', ') :
            `Request failed (${res.status})`;
        throw new Error(message);
    }

    return data;
}

/* ---------- Cart (client-side, persisted in localStorage until checkout) ---------- */
const Cart = {
    key: 'ac_cart',

    getItems() {
        const raw = localStorage.getItem(this.key);
        return raw ? JSON.parse(raw) : [];
    },

    save(items) {
        localStorage.setItem(this.key, JSON.stringify(items));
        updateCartBadge();
    },

    add(product, quantity) {
        const items = this.getItems();
        const existing = items.find(i => i.productId === product.id);
        if (existing) {
            existing.quantity += quantity;
        } else {
            items.push({
                productId: product.id,
                name: product.name,
                price: product.price,
                unit: product.unit,
                farmerName: product.farmer ? (product.farmer.farmName || product.farmer.name) : '',
                imageUrl: product.imageUrl,
                quantity: quantity
            });
        }
        this.save(items);
    },

    updateQuantity(productId, quantity) {
        let items = this.getItems();
        items = items.map(i => i.productId === productId ? { ...i, quantity } : i);
        this.save(items);
    },

    remove(productId) {
        const items = this.getItems().filter(i => i.productId !== productId);
        this.save(items);
    },

    clear() {
        localStorage.removeItem(this.key);
        updateCartBadge();
    },

    count() {
        return this.getItems().reduce((sum, i) => sum + i.quantity, 0);
    },

    total() {
        return this.getItems().reduce((sum, i) => sum + (i.price * i.quantity), 0);
    }
};

function updateCartBadge() {
    const badge = document.getElementById('cart-badge');
    if (badge) {
        const count = Cart.count();
        badge.textContent = count;
        badge.style.display = count > 0 ? 'inline-block' : 'none';
    }
}

function showToast(message, isError = false) {
    let toast = document.getElementById('ac-toast');
    if (!toast) {
        toast = document.createElement('div');
        toast.id = 'ac-toast';
        toast.className = 'toast';
        document.body.appendChild(toast);
    }
    toast.textContent = message;
    toast.style.background = isError ? '#B3452C' : '#1F3D2B';
    toast.classList.add('show');
    clearTimeout(toast._timer);
    toast._timer = setTimeout(() => toast.classList.remove('show'), 3200);
}

function formatMoney(n) {
    return '₹' + Number(n).toFixed(2);
}

function formatDate(iso) {
    const d = new Date(iso);
    return d.toLocaleDateString('en-IN', { day: 'numeric', month: 'short', year: 'numeric' }) +
           ' · ' + d.toLocaleTimeString('en-IN', { hour: '2-digit', minute: '2-digit' });
}
