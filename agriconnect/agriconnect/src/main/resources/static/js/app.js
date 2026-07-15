/* ==========================================================
   AgriConnect - shared UI wiring (nav bar, guards)
   ========================================================== */

function renderNav(activePage) {
    const mount = document.getElementById('nav-mount');
    if (!mount) return;

    const user = Auth.getUser();
    const loggedIn = Auth.isLoggedIn();

    let rightLinks = '';
    if (loggedIn) {
        if (user.role === 'FARMER') {
            rightLinks = `
                <a href="/farmer-dashboard.html" class="${activePage === 'dashboard' ? 'active' : ''}">My Farm</a>
                <span class="muted">Hi, ${user.name.split(' ')[0]}</span>
                <button class="btn btn-outline btn-sm" onclick="Auth.logout()">Log out</button>
            `;
        } else {
            rightLinks = `
                <a href="/orders.html" class="${activePage === 'orders' ? 'active' : ''}">My Orders</a>
                <a href="/cart.html" class="nav-cart" title="Cart">🧺<span id="cart-badge" class="badge" style="display:none">0</span></a>
                <span class="muted">Hi, ${user.name.split(' ')[0]}</span>
                <button class="btn btn-outline btn-sm" onclick="Auth.logout()">Log out</button>
            `;
        }
    } else {
        rightLinks = `
            <a href="/cart.html" class="nav-cart" title="Cart">🧺<span id="cart-badge" class="badge" style="display:none">0</span></a>
            <a href="/login.html" class="btn btn-outline btn-sm">Log in</a>
            <a href="/register.html" class="btn btn-primary btn-sm">Sign up</a>
        `;
    }

    mount.innerHTML = `
        <nav class="navbar">
            <div class="navbar-inner">
                <a href="/index.html" class="brand"><span class="seed">🌱</span> AgriConnect</a>
                <div class="nav-links" id="nav-links">
                    <a href="/index.html" class="${activePage === 'home' ? 'active' : ''}">Home</a>
                    <a href="/marketplace.html" class="${activePage === 'marketplace' ? 'active' : ''}">Marketplace</a>
                    ${!loggedIn || user.role !== 'FARMER' ? '' : ''}
                </div>
                <div class="nav-actions">
                    ${rightLinks}
                    <button class="hamburger" onclick="document.getElementById('nav-links').classList.toggle('open')">☰</button>
                </div>
            </div>
        </nav>
    `;
    updateCartBadge();
}

/** Redirect to login if not authenticated. Returns the user or null. */
function requireAuth() {
    if (!Auth.isLoggedIn()) {
        window.location.href = '/login.html';
        return null;
    }
    return Auth.getUser();
}

/** Redirect away if the logged-in user isn't a farmer. */
function requireFarmer() {
    const user = requireAuth();
    if (user && user.role !== 'FARMER') {
        showToast('That page is only available to farmer accounts', true);
        window.location.href = '/marketplace.html';
        return null;
    }
    return user;
}

/** Redirect away if the logged-in user isn't a customer. */
function requireCustomer() {
    const user = requireAuth();
    if (user && user.role !== 'CUSTOMER') {
        showToast('That page is only available to customer accounts', true);
        window.location.href = '/farmer-dashboard.html';
        return null;
    }
    return user;
}
