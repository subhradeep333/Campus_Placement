const API_BASE = '';

let currentUser = null; // { id, role, email, ... }
let allJobs = [];

// Page Load Initializer
document.addEventListener('DOMContentLoaded', () => {
    checkSavedSession();
    loadJobs();
    loadCompanies();
});

// --- Session & Auth Management ---

function checkSavedSession() {
    const saved = localStorage.getItem('placement_user');
    if (saved) {
        try {
            currentUser = JSON.parse(saved);
            updateNavUI();
        } catch (e) {
            localStorage.removeItem('placement_user');
        }
    }
}

function updateNavUI() {
    const chip = document.getElementById('userInfoChip');
    const loginBtn = document.getElementById('btnLoginModal');
    const avatar = document.getElementById('userAvatar');
    const userName = document.getElementById('userName');

    const btnStudentDash = document.getElementById('btnNavStudentDash');
    const btnCompanyDash = document.getElementById('btnNavCompanyDash');

    if (currentUser) {
        loginBtn.classList.add('hidden');
        chip.classList.remove('hidden');
        avatar.textContent = (currentUser.fullName || currentUser.companyName || 'U').charAt(0).toUpperCase();
        userName.textContent = currentUser.fullName || currentUser.companyName || currentUser.email;

        if (currentUser.role === 'STUDENT') {
            btnStudentDash.classList.remove('hidden');
            btnCompanyDash.classList.add('hidden');
        } else if (currentUser.role === 'COMPANY') {
            btnCompanyDash.classList.remove('hidden');
            btnStudentDash.classList.add('hidden');
        }
    } else {
        loginBtn.classList.remove('hidden');
        chip.classList.add('hidden');
        btnStudentDash.classList.add('hidden');
        btnCompanyDash.classList.add('hidden');
    }
}

function openAuthModal() {
    document.getElementById('authModal').classList.add('active');
}

function closeAuthModal() {
    document.getElementById('authModal').classList.remove('active');
}

function switchAuthMode(mode) {
    document.querySelectorAll('.modal-tab').forEach(t => t.classList.remove('active'));
    document.querySelectorAll('.auth-form').forEach(f => f.classList.remove('active'));

    if (mode === 'login') {
        document.getElementById('tabLogin').classList.add('active');
        document.getElementById('formLogin').classList.add('active');
    } else if (mode === 'register') {
        document.getElementById('tabRegister').classList.add('active');
        document.getElementById('formRegisterStudent').classList.add('active');
    } else if (mode === 'companyReg') {
        document.getElementById('tabCompanyReg').classList.add('active');
        document.getElementById('formRegisterCompany').classList.add('active');
    }
}

function fillDemo(role) {
    if (role === 'student') {
        document.getElementById('loginEmail').value = 'alex@university.edu';
        document.getElementById('loginPassword').value = 'pass123';
    } else {
        document.getElementById('loginEmail').value = 'recruitment@google.com';
        document.getElementById('loginPassword').value = 'google123';
    }
}

async function handleLoginSubmit(event) {
    event.preventDefault();
    const email = document.getElementById('loginEmail').value;
    const password = document.getElementById('loginPassword').value;

    try {
        const res = await fetch(`${API_BASE}/api/login`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ email, password })
        });
        const data = await res.json();
        if (data.error) {
            showToast(data.error, 'error');
        } else {
            currentUser = { role: data.role, ...data.user };
            localStorage.setItem('placement_user', JSON.stringify(currentUser));
            updateNavUI();
            closeAuthModal();
            showToast(`Welcome back, ${currentUser.fullName || currentUser.companyName}!`, 'success');

            if (currentUser.role === 'STUDENT') {
                switchTab('studentDashTab');
            } else {
                switchTab('companyDashTab');
            }
        }
    } catch (err) {
        showToast('Login failed: ' + err.message, 'error');
    }
}

async function handleStudentRegisterSubmit(event) {
    event.preventDefault();
    const payload = {
        fullName: document.getElementById('regStudentName').value,
        email: document.getElementById('regStudentEmail').value,
        password: document.getElementById('regStudentPass').value,
        phone: document.getElementById('regStudentPhone').value,
        branch: document.getElementById('regStudentBranch').value,
        cgpa: document.getElementById('regStudentCgpa').value
    };

    try {
        const res = await fetch(`${API_BASE}/api/register/student`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });
        const data = await res.json();
        if (data.error) {
            showToast(data.error, 'error');
        } else {
            showToast('Registration successful! Please sign in.', 'success');
            switchAuthMode('login');
            document.getElementById('loginEmail').value = payload.email;
        }
    } catch (err) {
        showToast('Registration error', 'error');
    }
}

async function handleCompanyRegisterSubmit(event) {
    event.preventDefault();
    const payload = {
        companyName: document.getElementById('regCompName').value,
        email: document.getElementById('regCompEmail').value,
        password: document.getElementById('regCompPass').value,
        industry: document.getElementById('regCompIndustry').value,
        location: document.getElementById('regCompLocation').value,
        website: document.getElementById('regCompWebsite').value,
        description: document.getElementById('regCompDesc').value
    };

    try {
        const res = await fetch(`${API_BASE}/api/register/company`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });
        const data = await res.json();
        if (data.error) {
            showToast(data.error, 'error');
        } else {
            showToast('Company account registered! Please sign in.', 'success');
            switchAuthMode('login');
            document.getElementById('loginEmail').value = payload.email;
        }
    } catch (err) {
        showToast('Registration error', 'error');
    }
}

function logout() {
    currentUser = null;
    localStorage.removeItem('placement_user');
    updateNavUI();
    switchTab('jobsTab');
    showToast('Logged out successfully', 'success');
}

// --- Navigation Tabs ---

function switchTab(tabId) {
    document.querySelectorAll('.nav-btn').forEach(b => b.classList.remove('active'));
    document.querySelectorAll('.tab-content').forEach(t => t.classList.remove('active'));

    document.getElementById(tabId).classList.add('active');

    if (tabId === 'jobsTab') document.getElementById('btnNavJobs').classList.add('active');
    if (tabId === 'companiesTab') document.getElementById('btnNavCompanies').classList.add('active');
    if (tabId === 'studentDashTab') {
        document.getElementById('btnNavStudentDash').classList.add('active');
        loadStudentDashboard();
    }
    if (tabId === 'companyDashTab') {
        document.getElementById('btnNavCompanyDash').classList.add('active');
        loadCompanyDashboard();
    }
}

// --- Jobs & Companies Loaders ---

async function loadJobs() {
    try {
        const res = await fetch(`${API_BASE}/api/jobs`);
        allJobs = await res.json();
        document.getElementById('statJobsCount').textContent = allJobs.length;
        renderJobs(allJobs);
    } catch (err) {
        console.error('Error loading jobs:', err);
    }
}

function renderJobs(jobs) {
    const grid = document.getElementById('jobsGrid');
    if (jobs.length === 0) {
        grid.innerHTML = `<div class="empty-cell" style="grid-column:1/-1;">No job openings match your query.</div>`;
        return;
    }

    grid.innerHTML = jobs.map(j => `
        <div class="job-card">
            <div>
                <div class="job-header">
                    <span class="company-name-tag">🏢 ${escapeHtml(j.companyName)}</span>
                    <span class="job-pkg">💰 ${j.packageLpa} LPA</span>
                </div>
                <h3 class="job-title">${escapeHtml(j.title)}</h3>
                <p class="job-details">${escapeHtml(j.description)}</p>
                <div class="job-skills">
                    ${j.requiredSkills.split(',').map(s => `<span class="skill-tag">${escapeHtml(s.trim())}</span>`).join('')}
                    <span class="skill-tag" style="color:var(--primary-cyan);">Min CGPA: ${j.minCgpa}</span>
                </div>
            </div>
            <button class="apply-btn" onclick="applyJob('${j.id}')">📝 Apply Now</button>
        </div>
    `).join('');
}

function filterJobs() {
    const q = document.getElementById('jobSearchInput').value.toLowerCase();
    const filtered = allJobs.filter(j => 
        j.title.toLowerCase().includes(q) ||
        j.companyName.toLowerCase().includes(q) ||
        j.requiredSkills.toLowerCase().includes(q)
    );
    renderJobs(filtered);
}

async function loadCompanies() {
    try {
        const res = await fetch(`${API_BASE}/api/companies`);
        const companies = await res.json();
        document.getElementById('statCompaniesCount').textContent = companies.length;

        const grid = document.getElementById('companiesGrid');
        grid.innerHTML = companies.map(c => `
            <div class="company-card">
                <div>
                    <span class="company-name-tag">📍 ${escapeHtml(c.location)}</span>
                    <h3 class="job-title" style="margin-top:0.3rem;">🏢 ${escapeHtml(c.companyName)}</h3>
                    <p class="job-details" style="color:var(--primary-cyan); font-size:0.85rem;">Domain: ${escapeHtml(c.industry)}</p>
                    <p class="job-details">${escapeHtml(c.description)}</p>
                </div>
                <a href="${escapeHtml(c.website)}" target="_blank" class="action-btn" style="text-align:center; text-decoration:none;">🌐 Visit Career Website</a>
            </div>
        `).join('');
    } catch (err) {
        console.error('Error loading companies:', err);
    }
}

async function applyJob(jobId) {
    if (!currentUser || currentUser.role !== 'STUDENT') {
        showToast('Please login as a Student to apply for jobs!', 'error');
        openAuthModal();
        return;
    }

    try {
        const res = await fetch(`${API_BASE}/api/apply`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ studentId: currentUser.id, jobId })
        });
        const data = await res.json();
        if (data.error) {
            showToast(data.error, 'error');
        } else {
            showToast(data.message || 'Application submitted successfully!', 'success');
        }
    } catch (err) {
        showToast('Application error: ' + err.message, 'error');
    }
}

// --- Student Dashboard Logic ---

async function loadStudentDashboard() {
    if (!currentUser || currentUser.role !== 'STUDENT') return;

    document.getElementById('studentDashWelcome').textContent = `Welcome, ${currentUser.fullName} (${currentUser.branch}, CGPA: ${currentUser.cgpa})`;
    
    // Fill CV Form fields
    document.getElementById('cvSkills').value = currentUser.skills || '';
    document.getElementById('cvSummary').value = currentUser.cvSummary || '';
    document.getElementById('cvFilePath').value = currentUser.cvFilePath || '';

    const badge = document.getElementById('cvStatusBadge');
    if (currentUser.hasSubmittedCv) {
        badge.textContent = '✔ Submitted';
        badge.className = 'status-pill status-accepted';
    } else {
        badge.textContent = '✖ Not Submitted';
        badge.className = 'status-pill status-rejected';
    }

    // Load Student Applications
    try {
        const res = await fetch(`${API_BASE}/api/applications?studentId=${currentUser.id}`);
        const apps = await res.json();
        const tbody = document.getElementById('studentAppsTableBody');

        if (apps.length === 0) {
            tbody.innerHTML = `<tr><td colspan="5" class="empty-cell">You have not applied for any job openings yet.</td></tr>`;
        } else {
            tbody.innerHTML = apps.map(a => `
                <tr>
                    <td><strong>${a.id}</strong></td>
                    <td>${escapeHtml(a.jobTitle)}</td>
                    <td>${escapeHtml(a.companyName)}</td>
                    <td>${a.appliedDate}</td>
                    <td><span class="status-pill status-${a.status.toLowerCase()}">${a.status}</span></td>
                </tr>
            `).join('');
        }
    } catch (err) {
        console.error('Error loading student applications:', err);
    }
}

async function saveStudentCv(event) {
    event.preventDefault();
    if (!currentUser) return;

    const payload = {
        studentId: currentUser.id,
        skills: document.getElementById('cvSkills').value,
        cvSummary: document.getElementById('cvSummary').value,
        cvFilePath: document.getElementById('cvFilePath').value
    };

    try {
        const res = await fetch(`${API_BASE}/api/cv`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });
        const data = await res.json();
        if (data.error) {
            showToast(data.error, 'error');
        } else {
            currentUser.skills = payload.skills;
            currentUser.cvSummary = payload.cvSummary;
            currentUser.cvFilePath = payload.cvFilePath;
            currentUser.hasSubmittedCv = true;
            localStorage.setItem('placement_user', JSON.stringify(currentUser));

            showToast('Your CV profile has been saved!', 'success');
            loadStudentDashboard();
        }
    } catch (err) {
        showToast('Failed to save CV', 'error');
    }
}

// --- Company Dashboard Logic ---

async function loadCompanyDashboard() {
    if (!currentUser || currentUser.role !== 'COMPANY') return;

    document.getElementById('companyDashWelcome').textContent = `Manage jobs & candidate selection for ${currentUser.companyName}`;

    // Load Received Candidate Applications
    try {
        const res = await fetch(`${API_BASE}/api/applications?companyId=${currentUser.id}`);
        const apps = await res.json();
        const tbody = document.getElementById('companyAppsTableBody');

        if (apps.length === 0) {
            tbody.innerHTML = `<tr><td colspan="6" class="empty-cell">No candidate applications received yet.</td></tr>`;
        } else {
            tbody.innerHTML = apps.map(a => `
                <tr>
                    <td><strong>${a.id}</strong></td>
                    <td>${escapeHtml(a.studentName)}</td>
                    <td>${escapeHtml(a.studentBranch)} (CGPA: ${a.studentCgpa})</td>
                    <td>${escapeHtml(a.jobTitle)}</td>
                    <td>
                        <button class="action-btn" onclick="openCvModal('${a.id}')">📄 Inspect CV</button>
                    </td>
                    <td>
                        <select class="status-select" onchange="updateAppStatus('${a.id}', this.value)">
                            <option value="PENDING" ${a.status === 'PENDING' ? 'selected' : ''}>PENDING</option>
                            <option value="SHORTLISTED" ${a.status === 'SHORTLISTED' ? 'selected' : ''}>SHORTLISTED ⭐</option>
                            <option value="ACCEPTED" ${a.status === 'ACCEPTED' ? 'selected' : ''}>ACCEPTED 🎉</option>
                            <option value="REJECTED" ${a.status === 'REJECTED' ? 'selected' : ''}>REJECTED ✖</option>
                        </select>
                    </td>
                </tr>
            `).join('');
            
            // Cache apps for modal inspection
            window.cachedApps = apps;
        }
    } catch (err) {
        console.error('Error loading company applications:', err);
    }
}

async function createJobPosting(event) {
    event.preventDefault();
    if (!currentUser) return;

    const payload = {
        companyId: currentUser.id,
        title: document.getElementById('jobTitle').value,
        packageLpa: document.getElementById('jobPackage').value,
        minCgpa: document.getElementById('jobMinCgpa').value,
        requiredSkills: document.getElementById('jobSkills').value,
        description: document.getElementById('jobDesc').value
    };

    try {
        const res = await fetch(`${API_BASE}/api/jobs`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });
        const data = await res.json();
        if (data.error) {
            showToast(data.error, 'error');
        } else {
            showToast('Job posting published!', 'success');
            document.getElementById('postJobForm').reset();
            loadJobs();
        }
    } catch (err) {
        showToast('Error publishing job', 'error');
    }
}

async function updateAppStatus(applicationId, newStatus) {
    try {
        const res = await fetch(`${API_BASE}/api/applications/status`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ applicationId, status: newStatus })
        });
        const data = await res.json();
        if (data.error) {
            showToast(data.error, 'error');
        } else {
            showToast(`Candidate status updated to ${newStatus}`, 'success');
            loadCompanyDashboard();
        }
    } catch (err) {
        showToast('Error updating status', 'error');
    }
}

// Modal Candidate CV Inspector
function openCvModal(appId) {
    const app = (window.cachedApps || []).find(a => a.id === appId);
    if (!app) return;

    const sd = app.studentDetails || {};
    const body = document.getElementById('cvModalBody');

    body.innerHTML = `
        <div style="line-height:1.7;">
            <p><strong>Full Name:</strong> ${escapeHtml(app.studentName)}</p>
            <p><strong>Email:</strong> ${escapeHtml(sd.email || 'N/A')}</p>
            <p><strong>Phone:</strong> ${escapeHtml(sd.phone || 'N/A')}</p>
            <p><strong>Branch / Department:</strong> ${escapeHtml(app.studentBranch)}</p>
            <p><strong>CGPA:</strong> <span style="color:var(--primary-cyan); font-weight:700;">${app.studentCgpa}</span></p>
            <p><strong>Key Skills:</strong> ${escapeHtml(sd.skills || 'Not listed')}</p>
            <p><strong>Attached Resume File Path:</strong> ${escapeHtml(sd.cvFilePath || 'None attached')}</p>
            <div style="margin-top:1.2rem; padding:1rem; background:rgba(0,0,0,0.3); border-radius:10px; border:1px solid var(--border-card);">
                <h4 style="margin-bottom:0.5rem; color:var(--primary-cyan);">📄 CV Dossier Summary:</h4>
                <p style="white-space:pre-wrap; color:var(--text-main); font-size:0.95rem;">${escapeHtml(sd.cvSummary || 'No CV summary provided.')}</p>
            </div>
        </div>
    `;

    document.getElementById('cvModal').classList.add('active');
}

function closeCvModal() {
    document.getElementById('cvModal').classList.remove('active');
}

// Toast Notifications
function showToast(msg, type = 'success') {
    const container = document.getElementById('toastContainer');
    const toast = document.createElement('div');
    toast.className = `toast ${type}`;
    toast.textContent = msg;
    container.appendChild(toast);
    setTimeout(() => {
        toast.remove();
    }, 4000);
}

function escapeHtml(str) {
    if (!str) return '';
    return str.replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;").replace(/"/g, "&quot;").replace(/'/g, "&#039;");
}
