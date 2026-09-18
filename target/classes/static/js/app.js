// Talks to the REST API under /api and renders plain HTML - no framework, on purpose

const CATEGORY_LABELS = {
    BAKERY: "Bread and bakery",
    FRUIT_VEG: "Fruit and vegetables",
    DAIRY: "Dairy and eggs",
    COOKED_MEAL: "Cooked meals",
    PANTRY: "Pantry / non-perishable",
    OTHER: "Other"
};

let currentFridgeId = null;

// -- small helpers --
async function api(path, options) {
    const response = await fetch(path, Object.assign({
        headers: { "Content-Type": "application/json" }
    }, options));

    if (response.status === 204) {
        return null;
    }

    let body = null;
    try {
        body = await response.json();
    } catch (ignored) {
        body = null;
    }

    if (!response.ok) {
        // backend always returns { message: "..." }, safe to show directly (see GlobalExceptionHandler)
        const message = (body && body.message) ? body.message : "Something went wrong. Please try again.";
        throw new Error(message);
    }
    return body;
}

function showBanner(message, type) {
    const banner = document.getElementById("banner");
    banner.textContent = message;
    banner.className = "banner " + type;
    window.scrollTo({ top: 0, behavior: "smooth" });
    clearTimeout(showBanner._timer);
    showBanner._timer = setTimeout(() => banner.classList.add("hidden"), 6000);
}

function formatDate(isoDate) {
    if (!isoDate) return "-";
    const [y, m, d] = isoDate.split("-");
    return `${d}.${m}.${y}`;
}

function today() {
    return new Date().toISOString().slice(0, 10);
}

// -- tabs --
function setupTabs() {
    document.querySelectorAll(".tab-btn").forEach(btn => {
        btn.addEventListener("click", () => {
            document.querySelectorAll(".tab-btn").forEach(b => b.classList.remove("active"));
            document.querySelectorAll(".tab-panel").forEach(p => p.classList.remove("active"));
            btn.classList.add("active");
            document.getElementById("tab-" + btn.dataset.tab).classList.add("active");
            if (btn.dataset.tab === "dashboard") loadDashboard();
            if (btn.dataset.tab === "fridges") loadFridges();
        });
    });
}

// -- dashboard --
async function loadDashboard() {
    try {
        const stats = await api("/api/stats");
        document.getElementById("statKgRescued").textContent = stats.kgRescued.toFixed(1) + " kg";
        document.getElementById("statKgLost").textContent = stats.kgLost.toFixed(1) + " kg";
        document.getElementById("statRate").textContent = stats.rescueRatePercent + "%";
        document.getElementById("statAvailable").textContent = stats.itemsAvailable;
        document.getElementById("statFridges").textContent = stats.activeFridges + " / " + stats.totalFridges;

        const catBody = document.querySelector("#categoryTable tbody");
        catBody.innerHTML = "";
        if (stats.byCategory.length === 0) {
            document.getElementById("categoryEmpty").classList.remove("hidden");
        } else {
            document.getElementById("categoryEmpty").classList.add("hidden");
            stats.byCategory.forEach(row => {
                const tr = document.createElement("tr");
                tr.innerHTML = `<td>${CATEGORY_LABELS[row.category] || row.category}</td>
                                 <td>${row.kgRescued.toFixed(1)} kg</td>
                                 <td>${row.sharePercent}%</td>`;
                catBody.appendChild(tr);
            });
        }

        const expiring = await api("/api/items/expiring?days=2");
        const expBody = document.querySelector("#expiringTable tbody");
        expBody.innerHTML = "";
        if (expiring.length === 0) {
            document.getElementById("expiringEmpty").classList.remove("hidden");
        } else {
            document.getElementById("expiringEmpty").classList.add("hidden");
            expiring.forEach(item => {
                const tr = document.createElement("tr");
                tr.innerHTML = `<td>${item.name}</td>
                                 <td>${item.fridge.name}</td>
                                 <td>${formatDate(item.expiryDate)}${item.expiryDate === today() ? " (today)" : ""}</td>
                                 <td>${item.quantityKg.toFixed(1)} kg</td>`;
                expBody.appendChild(tr);
            });
        }
    } catch (err) {
        showBanner(err.message, "error");
    }
}

// -- fridges --
async function loadFridges() {
    try {
        const fridges = await api("/api/fridges");
        const summaries = await Promise.all(
            fridges.map(f => api("/api/fridges/" + f.id + "/summary"))
        );

        const list = document.getElementById("fridgeList");
        list.innerHTML = "";
        summaries.forEach(f => {
            const percent = f.capacity > 0 ? Math.round((f.itemsInside / f.capacity) * 100) : 0;
            const card = document.createElement("div");
            card.className = "fridge-card" + (f.active ? "" : " inactive");
            card.innerHTML = `
                <span class="badge ${f.active ? "active" : "inactive"}">${f.active ? "In service" : "Out of service"}</span>
                <h3>${f.name}</h3>
                <div class="addr">${f.address}, ${f.district}</div>
                <div class="fill-bar"><div class="fill-bar-inner" style="width:${percent}%"></div></div>
                <div class="fridge-meta">${f.itemsInside} / ${f.capacity} slots used &middot; ${f.kgInside.toFixed(1)} kg inside</div>
                <div class="card-actions">
                    <button type="button" class="secondary" data-view="${f.id}" data-name="${f.name}">View items</button>
                    <button type="button" class="danger" data-delete="${f.id}">Delete</button>
                </div>`;
            list.appendChild(card);
        });

        list.querySelectorAll("[data-view]").forEach(btn =>
            btn.addEventListener("click", () => viewFridgeItems(btn.dataset.view, btn.dataset.name)));
        list.querySelectorAll("[data-delete]").forEach(btn =>
            btn.addEventListener("click", () => deleteFridge(btn.dataset.delete)));

        // Keep the "donate food" dropdown in sync with the current fridges.
        const select = document.getElementById("dFridge");
        select.innerHTML = "";
        fridges.filter(f => f.active).forEach(f => {
            const opt = document.createElement("option");
            opt.value = f.id;
            opt.textContent = f.name + " (" + f.district + ")";
            select.appendChild(opt);
        });
    } catch (err) {
        showBanner(err.message, "error");
    }
}

async function viewFridgeItems(fridgeId, fridgeName) {
    currentFridgeId = fridgeId;
    document.getElementById("itemsHeading").textContent = "Items inside " + fridgeName;
    try {
        const items = await api("/api/fridges/" + fridgeId + "/items");
        const table = document.getElementById("itemsTable");
        const hint = document.getElementById("itemsHint");
        const body = table.querySelector("tbody");
        body.innerHTML = "";

        if (items.length === 0) {
            hint.textContent = "This fridge is currently empty.";
            hint.classList.remove("hidden");
            table.classList.add("hidden");
            return;
        }
        hint.classList.add("hidden");
        table.classList.remove("hidden");

        items.forEach(item => {
            const tr = document.createElement("tr");
            let action = "-";
            if (item.status === "AVAILABLE") {
                action = `<button type="button" class="secondary" data-claim="${item.id}">Claim</button>
                          <button type="button" class="danger" data-remove="${item.id}">Remove</button>`;
            }
            tr.innerHTML = `<td>${item.name}</td>
                             <td>${CATEGORY_LABELS[item.category] || item.category}</td>
                             <td>${item.quantityKg.toFixed(1)} kg</td>
                             <td>${formatDate(item.expiryDate)}</td>
                             <td><span class="badge status-${item.status}">${item.status}</span></td>
                             <td>${action}</td>`;
            body.appendChild(tr);
        });

        body.querySelectorAll("[data-claim]").forEach(btn =>
            btn.addEventListener("click", () => claimItem(btn.dataset.claim)));
        body.querySelectorAll("[data-remove]").forEach(btn =>
            btn.addEventListener("click", () => removeItem(btn.dataset.remove)));
    } catch (err) {
        showBanner(err.message, "error");
    }
}

async function claimItem(itemId) {
    const name = window.prompt("Your name, so we know who rescued this food:");
    if (!name) return;
    try {
        await api("/api/items/" + itemId + "/claim", {
            method: "POST",
            body: JSON.stringify({ claimedBy: name })
        });
        showBanner("Thank you, " + name + "! Enjoy the food.", "success");
        refreshCurrentFridge();
        loadDashboard();
    } catch (err) {
        showBanner(err.message, "error");
    }
}

async function removeItem(itemId) {
    if (!window.confirm("Remove this item from the fridge?")) return;
    try {
        await api("/api/items/" + itemId + "/remove", { method: "POST" });
        showBanner("Item removed from the fridge.", "success");
        refreshCurrentFridge();
    } catch (err) {
        showBanner(err.message, "error");
    }
}

async function deleteFridge(fridgeId) {
    if (!window.confirm("Delete this fridge? This only works while it holds no available food.")) return;
    try {
        await api("/api/fridges/" + fridgeId, { method: "DELETE" });
        showBanner("Fridge deleted.", "success");
        loadFridges();
    } catch (err) {
        showBanner(err.message, "error");
    }
}

function refreshCurrentFridge() {
    if (currentFridgeId) {
        const card = document.querySelector(`[data-view="${currentFridgeId}"]`);
        viewFridgeItems(currentFridgeId, card ? card.dataset.name : "");
    }
    loadFridges();
}

// -- forms --
function setupForms() {
    document.getElementById("fridgeForm").addEventListener("submit", async (event) => {
        event.preventDefault();
        try {
            await api("/api/fridges", {
                method: "POST",
                body: JSON.stringify({
                    name: document.getElementById("fName").value,
                    address: document.getElementById("fAddress").value,
                    district: document.getElementById("fDistrict").value,
                    capacity: Number(document.getElementById("fCapacity").value),
                    active: true
                })
            });
            showBanner("Fridge added.", "success");
            event.target.reset();
            document.getElementById("fCapacity").value = 10;
            loadFridges();
        } catch (err) {
            showBanner(err.message, "error");
        }
    });

    document.getElementById("dExpiry").value = today();

    document.getElementById("donateForm").addEventListener("submit", async (event) => {
        event.preventDefault();
        const fridgeId = document.getElementById("dFridge").value;
        if (!fridgeId) {
            showBanner("Please register a fridge first.", "error");
            return;
        }
        try {
            await api("/api/fridges/" + fridgeId + "/items", {
                method: "POST",
                body: JSON.stringify({
                    name: document.getElementById("dName").value,
                    category: document.getElementById("dCategory").value,
                    quantityKg: Number(document.getElementById("dQuantity").value),
                    expiryDate: document.getElementById("dExpiry").value,
                    donorName: document.getElementById("dDonor").value
                })
            });
            showBanner("Food donated - thank you!", "success");
            event.target.reset();
            document.getElementById("dQuantity").value = 1.0;
            document.getElementById("dExpiry").value = today();
            loadDashboard();
        } catch (err) {
            showBanner(err.message, "error");
        }
    });
}

// -- start-up --
document.addEventListener("DOMContentLoaded", () => {
    setupTabs();
    setupForms();
    loadDashboard();
    loadFridges();
});
