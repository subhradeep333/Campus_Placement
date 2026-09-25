// --- Self-Contained Local Storage Database & Web Portal Client ---

let currentUser = null;
let allJobs = [];

// Initialize Local Database on Load
document.addEventListener('DOMContentLoaded', () => {
    initLocalDb();
    checkSavedSession();
    loadJobs();
    loadCompanies();
});

// --- Local Storage Database Management ---

function initLocalDb() {
    if (localStorage.getItem('placement_portal_db')) {
        return; // DB already initialized
    }

    const defaultDb = {
        students: [
            {
                id: "STU101",
                email: "alex@university.edu",
                password: "pass123",
                fullName: "Alex Rivers",
                phone: "+1-555-0192",
                branch: "Computer Science",
                cgpa: 3.85,
                skills: "Java, Python, Data Structures, Algorithms, SQL, Git",
                cvSummary: "Senior CS Student with experience in backend Java development, microservices, and algorithm optimization. Built 3 open-source CLI tools.",
                cvFilePath: "/documents/Alex_Rivers_Resume.pdf",
                hasSubmittedCv: true
            },
            {
                id: "STU102",
                email: "sarah@university.edu",
                password: "pass123",
                fullName: "Sarah Chen",
                phone: "+1-555-0481",
                branch: "Software Engineering",
                cgpa: 3.92,
                skills: "Java, Spring Boot, React, Cloud Computing, Docker",
                cvSummary: "Full-stack enthusiast interested in scalable web architecture and cloud-native solutions. Winner of 2025 Hackathon.",
                cvFilePath: "/documents/Sarah_Chen_CV.pdf",
                hasSubmittedCv: true
            }
        ],
        companies: [
            {
                id: "CMP201",
                email: "recruitment@google.com",
                password: "google123",
                companyName: "Google",
                industry: "Technology / Cloud / AI",
                location: "Mountain View, CA",
                website: "https://careers.google.com",
                description: "Leading global tech company specializing in search, cloud, AI, and consumer tech."
            },
            {
                id: "CMP202",
                email: "hr@microsoft.com",
                password: "ms123",
                companyName: "Microsoft",
                industry: "Enterprise Software",
                location: "Redmond, WA",
                website: "https://careers.microsoft.com",
                description: "Building platforms and tools to empower every person and organization."
            },
            {
                id: "CMP203",
                email: "jobs@fintech.com",
                password: "fin123",
                companyName: "Apex Financial Technologies",
                industry: "FinTech / Banking",
                location: "New York, NY",
                website: "https://apexfintech.com",
                description: "High-frequency trading and modern banking software provider."
            }
        ],
        jobPostings: [
            {
                id: "JOB501",
                companyId: "CMP201",
                companyName: "Google",
                title: "Software Engineer I - Backend",
                description: "Design and implement scalable REST APIs, microservices, and distributed data pipelines.",
                requiredSkills: "Java, Distributed Systems, SQL",
                packageLpa: 28.5,
                minCgpa: 3.5,
                postedDate: "2026-09-01",
                active: true
            },
            {
                id: "JOB502",
                companyId: "CMP202",
                companyName: "Microsoft",
                title: "Cloud Solutions Developer",
                description: "Work with Azure platform engineering teams to build developer tooling.",
                requiredSkills: "Java, C#, Cloud Computing, Kubernetes",
                packageLpa: 26.0,
                minCgpa: 3.2,
                postedDate: "2026-09-02",
                active: true
            },
            {
                id: "JOB503",
                companyId: "CMP203",
                companyName: "Apex Financial Technologies",
                title: "Quantitative Developer Intern",
                description: "Implement high-speed algorithmic trading components in Core Java.",
                requiredSkills: "Java, Concurrency, Algorithms, Low-Latency",
                packageLpa: 32.0,
                minCgpa: 3.7,
                postedDate: "2026-09-03",
                active: true
            }
        ],
        applications: [
            {
                id: "APP1001",
                jobId: "JOB501",
                jobTitle: "Software Engineer I - Backend",
                studentId: "STU101",
                studentName: "Alex Rivers",
                studentBranch: "Computer Science",
                studentCgpa: 3.85,
                companyId: "CMP201",
                companyName: "Google",
                status: "SHORTLISTED",
                appliedDate: "2026-09-10 10:15",
                studentDetails: {
                    skills: "Java, Python, Data Structures, Algorithms, SQL, Git",
                    cvSummary: "Senior CS Student with experience in backend Java development, microservices, and algorithm optimization. Built 3 open-source CLI tools.",
                    cvFilePath: "/documents/Alex_Rivers_Resume.pdf",
                    phone: "+1-555-0192",
                    email: "alex@university.edu"
                }
            },
            {
                id: "APP1002",
                jobId: "JOB503",
                jobTitle: "Quantitative Developer Intern",
                studentId: "STU102",
                studentName: "Sarah Chen",
                studentBranch: "Software Engineering",
                studentCgpa: 3.92,
                companyId: "CMP203",
                companyName: "Apex Financial Technologies",
                status: "PENDING",
                appliedDate: "2026-09-12 14:30",
                studentDetails: {
                    skills: "Java, Spring Boot, React, Cloud Computing, Docker",
                    cvSummary: "Full-stack enthusiast interested in scalable web architecture and cloud-native solutions. Winner of 2025 Hackathon.",
                    cvFilePath: "/documents/Sarah_Chen_CV.pdf",
                    phone: "+1-555-0481",
                    email: "sarah@university.edu"
                }
            }
        ],
        interviews: [
            {
                id: "INT101",
                applicationId: "APP1001",
                jobId: "JOB501",
                jobTitle: "Software Engineer I - Backend",
                studentId: "STU101",
                studentName: "Alex Rivers",
                companyId: "CMP201",
                companyName: "Google",
                roundType: "Technical Coding Round",
                roundNumber: 1,
                scheduledDateTime: "2026-10-05 10:00 AM",
                locationOrLink: "https://meet.google.com/xyz-tech-round",
                interviewerName: "Dr. Alan Turing",
                status: "PASSED",
                feedback: "Excellent knowledge of Java concurrency, memory models, and data structure design. Strong problem-solving speed.",
                score: 9.2
            },
            {
                id: "INT102",
                applicationId: "APP1001",
                jobId: "JOB501",
                jobTitle: "Software Engineer I - Backend",
                studentId: "STU101",
                studentName: "Alex Rivers",
                companyId: "CMP201",
                companyName: "Google",
                roundType: "System Design & Architecture",
                roundNumber: 2,
                scheduledDateTime: "2026-10-12 02:30 PM",
                locationOrLink: "https://meet.google.com/xyz-system-design",
                interviewerName: "Grace Hopper",
                status: "SCHEDULED",
                feedback: "Upcoming Round. Candidate instructed to review distributed consensus algorithms.",
                score: 0.0
            }
        ],
        counters: { student: 102, company: 203, job: 503, application: 1002, interview: 102 }
    };

    localStorage.setItem('placement_portal_db', JSON.stringify(defaultDb));
}

function getDb() {
    initLocalDb();
    return JSON.parse(localStorage.getItem('placement_portal_db'));
}

function saveDb(db) {
    localStorage.setItem('placement_portal_db', JSON.stringify(db));
}

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

function togglePasswordVisibility(inputId, btn) {
    const input = document.getElementById(inputId);
    if (!input) return;
    if (input.type === 'password') {
        input.type = 'text';
        btn.textContent = '🙈';
    } else {
        input.type = 'password';
        btn.textContent = '👁️';
    }
}

function handleLoginSubmit(event) {
    event.preventDefault();
    const email = document.getElementById('loginEmail').value.trim();
    const password = document.getElementById('loginPassword').value.trim();

    const db = getDb();
    
    // Check Students
    const student = db.students.find(s => s.email.toLowerCase() === email.toLowerCase() && s.password === password);
    if (student) {
        currentUser = { role: 'STUDENT', ...student };
        localStorage.setItem('placement_user', JSON.stringify(currentUser));
        updateNavUI();
        closeAuthModal();
        showToast(`Welcome back, ${currentUser.fullName}!`, 'success');
        switchTab('studentDashTab');
        return;
    }

    // Check Companies
    const company = db.companies.find(c => c.email.toLowerCase() === email.toLowerCase() && c.password === password);
    if (company) {
        currentUser = { role: 'COMPANY', ...company };
        localStorage.setItem('placement_user', JSON.stringify(currentUser));
        updateNavUI();
        closeAuthModal();
        showToast(`Welcome back, ${currentUser.companyName}!`, 'success');
        switchTab('companyDashTab');
        return;
    }

    showToast('Invalid email or password!', 'error');
}

function handleStudentRegisterSubmit(event) {
    event.preventDefault();
    const db = getDb();

    const email = document.getElementById('regStudentEmail').value.trim();
    if (db.students.some(s => s.email.toLowerCase() === email.toLowerCase()) || db.companies.some(c => c.email.toLowerCase() === email.toLowerCase())) {
        showToast('Email address is already registered!', 'error');
        return;
    }

    db.counters.student++;
    const newStudent = {
        id: `STU${db.counters.student}`,
        email: email,
        password: document.getElementById('regStudentPass').value,
        fullName: document.getElementById('regStudentName').value.trim(),
        phone: document.getElementById('regStudentPhone').value.trim(),
        branch: document.getElementById('regStudentBranch').value.trim(),
        cgpa: parseFloat(document.getElementById('regStudentCgpa').value),
        skills: "",
        cvSummary: "",
        cvFilePath: "",
        hasSubmittedCv: false
    };

    db.students.push(newStudent);
    saveDb(db);

    showToast('Student registration successful! Please sign in.', 'success');
    switchAuthMode('login');
    document.getElementById('loginEmail').value = email;
}

function handleCompanyRegisterSubmit(event) {
    event.preventDefault();
    const db = getDb();

    const email = document.getElementById('regCompEmail').value.trim();
    if (db.companies.some(c => c.email.toLowerCase() === email.toLowerCase()) || db.students.some(s => s.email.toLowerCase() === email.toLowerCase())) {
        showToast('Email address is already registered!', 'error');
        return;
    }

    db.counters.company++;
    const newCompany = {
        id: `CMP${db.counters.company}`,
        email: email,
        password: document.getElementById('regCompPass').value,
        companyName: document.getElementById('regCompName').value.trim(),
        industry: document.getElementById('regCompIndustry').value.trim(),
        location: document.getElementById('regCompLocation').value.trim(),
        website: document.getElementById('regCompWebsite').value.trim(),
        description: document.getElementById('regCompDesc').value.trim()
    };

    db.companies.push(newCompany);
    saveDb(db);

    showToast('Company registered successfully! Please sign in.', 'success');
    switchAuthMode('login');
    document.getElementById('loginEmail').value = email;
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

    if (tabId === 'jobsTab') {
        document.getElementById('btnNavJobs').classList.add('active');
        loadJobs();
    }
    if (tabId === 'companiesTab') {
        document.getElementById('btnNavCompanies').classList.add('active');
        loadCompanies();
    }
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

function loadJobs() {
    const db = getDb();
    allJobs = db.jobPostings || [];
    const statElement = document.getElementById('statJobsCount');
    if (statElement) statElement.textContent = allJobs.length;
    renderJobs(allJobs);
}

function calculateSkillMatch(studentSkillsStr, requiredSkillsStr) {
    if (!studentSkillsStr || !requiredSkillsStr) return 0;
    const studentSkills = studentSkillsStr.toLowerCase().split(',').map(s => s.trim()).filter(Boolean);
    const reqSkills = requiredSkillsStr.toLowerCase().split(',').map(s => s.trim()).filter(Boolean);
    if (reqSkills.length === 0) return 100;

    let matches = 0;
    reqSkills.forEach(req => {
        if (studentSkills.some(st => st.includes(req) || req.includes(st))) {
            matches++;
        }
    });

    return Math.min(100, Math.round((matches / reqSkills.length) * 100));
}

function renderJobs(jobs) {
    const grid = document.getElementById('jobsGrid');
    if (!grid) return;
    if (jobs.length === 0) {
        grid.innerHTML = `<div class="empty-cell" style="grid-column:1/-1;">No job openings match your query.</div>`;
        return;
    }

    grid.innerHTML = jobs.map(j => {
        let aiBadge = '';
        if (currentUser && currentUser.role === 'STUDENT') {
            const matchPct = calculateSkillMatch(currentUser.skills, j.requiredSkills);
            const cgpaMet = (currentUser.cgpa || 0) >= j.minCgpa;
            aiBadge = `
                <div class="ai-match-pill ${matchPct >= 50 && cgpaMet ? 'match-high' : 'match-med'}">
                    <span>⚡ <strong>${matchPct}% Skill Match</strong></span>
                    <span>•</span>
                    <span>${cgpaMet ? 'CGPA Cutoff Met ✔' : 'Below Cutoff ⚠️'}</span>
                </div>
            `;
        } else {
            aiBadge = `
                <div class="ai-match-pill match-info">
                    <span>✨ AI Matcher Available (Log in as Student to evaluate match)</span>
                </div>
            `;
        }

        return `
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
                    ${aiBadge}
                </div>
                <button class="apply-btn" onclick="applyJob('${j.id}')">📝 Apply Now</button>
            </div>
        `;
    }).join('');
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

function loadCompanies() {
    const db = getDb();
    const companies = db.companies || [];
    const statElement = document.getElementById('statCompaniesCount');
    if (statElement) statElement.textContent = companies.length;

    const grid = document.getElementById('companiesGrid');
    if (!grid) return;

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
}

function applyJob(jobId) {
    if (!currentUser || currentUser.role !== 'STUDENT') {
        showToast('Please login as a Student to apply for jobs!', 'error');
        openAuthModal();
        return;
    }

    if (!currentUser.hasSubmittedCv) {
        showToast('You must submit your CV & Skills profile before applying for jobs!', 'error');
        switchTab('studentDashTab');
        return;
    }

    const db = getDb();
    const job = db.jobPostings.find(j => j.id === jobId);
    if (!job) {
        showToast('Invalid Job ID!', 'error');
        return;
    }

    if (currentUser.cgpa < job.minCgpa) {
        showToast(`Your CGPA (${currentUser.cgpa}) is below the required cutoff (${job.minCgpa}) for this role.`, 'error');
        return;
    }

    const existingApp = db.applications.find(a => a.studentId === currentUser.id && a.jobId === jobId);
    if (existingApp) {
        showToast('You have already submitted an application for this role!', 'error');
        return;
    }

    db.counters.application++;
    const nowStr = new Date().toISOString().replace('T', ' ').substring(0, 16);
    const newApp = {
        id: `APP${db.counters.application}`,
        jobId: job.id,
        jobTitle: job.title,
        studentId: currentUser.id,
        studentName: currentUser.fullName,
        studentBranch: currentUser.branch,
        studentCgpa: currentUser.cgpa,
        companyId: job.companyId,
        companyName: job.companyName,
        status: "PENDING",
        appliedDate: nowStr,
        studentDetails: {
            skills: currentUser.skills,
            cvSummary: currentUser.cvSummary,
            cvFilePath: currentUser.cvFilePath,
            phone: currentUser.phone,
            email: currentUser.email
        }
    };

    db.applications.push(newApp);
    saveDb(db);

    showToast(`Application submitted successfully! Ref ID: ${newApp.id}`, 'success');
}

// --- Drag & Drop CV File Handlers ---

function handleDragOver(e) {
    e.preventDefault();
    e.stopPropagation();
    const zone = document.getElementById('cvDropZone');
    if (zone) zone.classList.add('dragover');
}

function handleDragLeave(e) {
    e.preventDefault();
    e.stopPropagation();
    const zone = document.getElementById('cvDropZone');
    if (zone) zone.classList.remove('dragover');
}

function handleFileDrop(e) {
    e.preventDefault();
    e.stopPropagation();
    const zone = document.getElementById('cvDropZone');
    if (zone) zone.classList.remove('dragover');

    const files = e.dataTransfer.files;
    if (files && files.length > 0) {
        processCvFile(files[0]);
    }
}

function handleCvFileSelect(e) {
    const files = e.target.files;
    if (files && files.length > 0) {
        processCvFile(files[0]);
    }
}

function processCvFile(file) {
    if (!file) return;

    const maxSize = 10 * 1024 * 1024;
    if (file.size > maxSize) {
        showToast('File is too large! Maximum size allowed is 10MB.', 'error');
        return;
    }

    const formattedSize = file.size > 1024 * 1024 
        ? (file.size / (1024 * 1024)).toFixed(2) + ' MB'
        : (file.size / 1024).toFixed(1) + ' KB';

    document.getElementById('fileNameText').textContent = file.name;
    document.getElementById('fileSizeText').textContent = formattedSize;
    document.getElementById('cvFilePath').value = `/uploads/${file.name}`;

    document.getElementById('dropZoneContent').classList.add('hidden');
    document.getElementById('filePreviewBox').classList.remove('hidden');

    showToast(`Attached file: ${file.name}`, 'success');
}

function removeCvFile(e) {
    if (e) e.stopPropagation();
    document.getElementById('cvFileInput').value = '';
    document.getElementById('cvFilePath').value = '';

    document.getElementById('filePreviewBox').classList.add('hidden');
    document.getElementById('dropZoneContent').classList.remove('hidden');
}

// --- Student Dashboard Logic ---

function loadStudentDashboard() {
    if (!currentUser || currentUser.role !== 'STUDENT') return;

    document.getElementById('studentDashWelcome').textContent = `Welcome, ${currentUser.fullName} (${currentUser.branch}, CGPA: ${currentUser.cgpa})`;
    
    // Fill CV Form fields
    document.getElementById('cvSkills').value = currentUser.skills || '';
    document.getElementById('cvSummary').value = currentUser.cvSummary || '';
    document.getElementById('cvFilePath').value = currentUser.cvFilePath || '';

    if (currentUser.cvFilePath) {
        const parts = currentUser.cvFilePath.split('/');
        const name = parts[parts.length - 1] || 'Attached_CV.pdf';
        document.getElementById('fileNameText').textContent = name;
        document.getElementById('fileSizeText').textContent = 'Uploaded Document';
        document.getElementById('dropZoneContent').classList.add('hidden');
        document.getElementById('filePreviewBox').classList.remove('hidden');
    } else {
        removeCvFile();
    }

    const badge = document.getElementById('cvStatusBadge');
    if (currentUser.hasSubmittedCv) {
        badge.textContent = '✔ Submitted';
        badge.className = 'status-pill status-accepted';
    } else {
        badge.textContent = '✖ Not Submitted';
        badge.className = 'status-pill status-rejected';
    }

    // Load Student Applications from Local Storage
    const db = getDb();
    const apps = (db.applications || []).filter(a => a.studentId === currentUser.id);
    const tbody = document.getElementById('studentAppsTableBody');

    if (apps.length === 0) {
        tbody.innerHTML = `<tr><td colspan="6" class="empty-cell">You have not applied for any job openings yet.</td></tr>`;
    } else {
        window.cachedStudentApps = apps;
        tbody.innerHTML = apps.map(a => `
            <tr>
                <td><strong>${a.id}</strong></td>
                <td>${escapeHtml(a.jobTitle)}</td>
                <td>${escapeHtml(a.companyName)}</td>
                <td>${a.appliedDate}</td>
                <td><span class="status-pill status-${a.status.toLowerCase()}">${a.status}</span></td>
                <td>
                    ${a.status === 'ACCEPTED' || a.status === 'SHORTLISTED' 
                        ? `<button class="action-btn" style="background:rgba(0,230,118,0.15); border-color:#00e676; color:#00e676;" onclick="openOfferModal('${a.id}')">📜 View Offer Letter</button>` 
                        : '<span style="color:var(--text-muted); font-size:0.8rem;">Pending Review</span>'}
                </td>
            </tr>
        `).join('');
    }

    loadStudentInterviews();
}

function saveStudentCv(event) {
    event.preventDefault();
    if (!currentUser) return;

    const db = getDb();
    const studentIdx = db.students.findIndex(s => s.id === currentUser.id);

    const skills = document.getElementById('cvSkills').value;
    const cvSummary = document.getElementById('cvSummary').value;
    const cvFilePath = document.getElementById('cvFilePath').value;

    if (studentIdx !== -1) {
        db.students[studentIdx].skills = skills;
        db.students[studentIdx].cvSummary = cvSummary;
        db.students[studentIdx].cvFilePath = cvFilePath;
        db.students[studentIdx].hasSubmittedCv = true;
        saveDb(db);
    }

    currentUser.skills = skills;
    currentUser.cvSummary = cvSummary;
    currentUser.cvFilePath = cvFilePath;
    currentUser.hasSubmittedCv = true;
    localStorage.setItem('placement_user', JSON.stringify(currentUser));

    showToast('Your CV profile has been saved!', 'success');
    loadStudentDashboard();
}

// --- Company Dashboard Logic ---

function loadCompanyDashboard() {
    if (!currentUser || currentUser.role !== 'COMPANY') return;

    document.getElementById('companyDashWelcome').textContent = `Manage jobs & candidate selection for ${currentUser.companyName}`;

    // Load Received Candidate Applications from Local Storage
    const db = getDb();
    const apps = (db.applications || []).filter(a => a.companyId === currentUser.id);
    const tbody = document.getElementById('companyAppsTableBody');

    if (apps.length === 0) {
        tbody.innerHTML = `<tr><td colspan="7" class="empty-cell">No candidate applications received yet.</td></tr>`;
    } else {
        window.cachedApps = apps;
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
                    <button class="action-btn" style="background:rgba(124,77,255,0.15); border-color:#7c4dff; color:#7c4dff;" onclick="openScheduleInterviewModal('${a.id}', '${escapeHtml(a.studentName)}', '${escapeHtml(a.jobTitle)}')">📅 Schedule Round</button>
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
    }

    loadCompanyInterviews();
}

function createJobPosting(event) {
    event.preventDefault();
    if (!currentUser) return;

    const db = getDb();
    db.counters.job++;

    const nowStr = new Date().toISOString().substring(0, 10);
    const newJob = {
        id: `JOB${db.counters.job}`,
        companyId: currentUser.id,
        companyName: currentUser.companyName,
        title: document.getElementById('jobTitle').value.trim(),
        packageLpa: parseFloat(document.getElementById('jobPackage').value),
        minCgpa: parseFloat(document.getElementById('jobMinCgpa').value),
        requiredSkills: document.getElementById('jobSkills').value.trim(),
        description: document.getElementById('jobDesc').value.trim(),
        postedDate: nowStr,
        active: true
    };

    db.jobPostings.push(newJob);
    saveDb(db);

    showToast('Job posting published successfully!', 'success');
    document.getElementById('postJobForm').reset();
    loadJobs();
}

function updateAppStatus(applicationId, newStatus) {
    const db = getDb();
    const app = db.applications.find(a => a.id === applicationId);
    if (app) {
        app.status = newStatus;
        saveDb(db);
        showToast(`Candidate status updated to ${newStatus}`, 'success');
        loadCompanyDashboard();
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

// Modal Digital Placement Offer Letter Generator
function openOfferModal(appId) {
    let app = (window.cachedApps || []).find(a => a.id === appId);
    if (!app && window.cachedStudentApps) {
        app = window.cachedStudentApps.find(a => a.id === appId);
    }
    if (!app) return;

    const modalBody = document.getElementById('offerModalBody');
    modalBody.innerHTML = `
        <div class="offer-letter-box">
            <div class="offer-watermark">PLACED</div>
            <div class="offer-header">
                <h1>🎓 OFFICIAL PLACEMENT OFFER LETTER</h1>
                <p style="color:var(--text-muted); font-size:0.85rem;">Campus Recruitment & Placement Authority • Session 2026</p>
            </div>
            
            <p style="margin-bottom:1.25rem; line-height:1.7; font-size:1.02rem;">
                Dear <strong>${escapeHtml(app.studentName)}</strong>,<br>
                We are delighted to confirm your official placement selection for the role of <strong>${escapeHtml(app.jobTitle)}</strong> at <strong>${escapeHtml(app.companyName)}</strong>. On behalf of the hiring board, congratulations on passing all selection rounds!
            </p>

            <div class="offer-details-grid">
                <div class="offer-item">
                    <span>Candidate Name</span>
                    <strong>${escapeHtml(app.studentName)}</strong>
                </div>
                <div class="offer-item">
                    <span>Department / Branch</span>
                    <strong>${escapeHtml(app.studentBranch)} (CGPA: ${app.studentCgpa})</strong>
                </div>
                <div class="offer-item">
                    <span>Hiring Company</span>
                    <strong>${escapeHtml(app.companyName)}</strong>
                </div>
                <div class="offer-item">
                    <span>Selected Role</span>
                    <strong>${escapeHtml(app.jobTitle)}</strong>
                </div>
                <div class="offer-item">
                    <span>Verification Reference ID</span>
                    <strong>${app.id}</strong>
                </div>
                <div class="offer-item">
                    <span>Issued Date</span>
                    <strong>${app.appliedDate || 'Current Session 2026'}</strong>
                </div>
            </div>

            <div style="display:flex; justify-content:space-between; align-items:center; margin-top:1.5rem; padding-top:1rem; border-top:1px solid rgba(255,255,255,0.1);">
                <div class="offer-stamp">VERIFIED OFFER ✔</div>
                <div style="text-align:right;">
                    <p style="font-size:0.8rem; color:var(--text-muted);">Placement Officer Signature</p>
                    <p style="font-family:'Outfit',sans-serif; color:var(--primary-cyan); font-weight:700;">Campus Placement Cell 📜</p>
                </div>
            </div>
        </div>
    `;

    document.getElementById('offerModal').classList.add('active');
}

function closeOfferModal() {
    document.getElementById('offerModal').classList.remove('active');
}

// --- Interview Management Functions ---

function loadStudentInterviews() {
    if (!currentUser || currentUser.role !== 'STUDENT') return;
    const db = getDb();
    const interviews = (db.interviews || []).filter(i => i.studentId === currentUser.id);
    const tbody = document.getElementById('studentInterviewsTableBody');
    if (!tbody) return;

    if (interviews.length === 0) {
        tbody.innerHTML = `<tr><td colspan="7" class="empty-cell">No interview rounds scheduled yet.</td></tr>`;
    } else {
        tbody.innerHTML = interviews.map(i => {
            let scoreBadge = i.score > 0 ? `<br><span style="color:#00e676; font-weight:700;">Score: ${i.score}/10</span>` : '';
            let linkHtml = i.locationOrLink 
                ? (i.locationOrLink.startsWith('http') 
                    ? `<a href="${escapeHtml(i.locationOrLink)}" target="_blank" style="color:var(--primary-cyan);">🔗 Join Meeting</a>` 
                    : escapeHtml(i.locationOrLink))
                : 'N/A';
            return `
                <tr>
                    <td><strong>${i.id}</strong></td>
                    <td>Round #${i.roundNumber}<br><span style="font-size:0.8rem; color:var(--primary-cyan);">${escapeHtml(i.roundType)}</span></td>
                    <td>${escapeHtml(i.companyName)}</td>
                    <td>📅 ${escapeHtml(i.scheduledDateTime)}</td>
                    <td>${escapeHtml(i.interviewerName || 'Assigned Lead')}<br><small style="opacity:0.8;">${linkHtml}</small></td>
                    <td><span class="status-pill status-${i.status.toLowerCase()}">${i.status}</span></td>
                    <td>${escapeHtml(i.feedback || 'No feedback logged yet')} ${scoreBadge}</td>
                </tr>
            `;
        }).join('');
    }
}

function loadCompanyInterviews() {
    if (!currentUser || currentUser.role !== 'COMPANY') return;
    const db = getDb();
    const interviews = (db.interviews || []).filter(i => i.companyId === currentUser.id);
    const tbody = document.getElementById('companyInterviewsTableBody');
    if (!tbody) return;

    if (interviews.length === 0) {
        tbody.innerHTML = `<tr><td colspan="8" class="empty-cell">No interview rounds scheduled yet.</td></tr>`;
    } else {
        window.cachedCompanyInterviews = interviews;
        tbody.innerHTML = interviews.map(i => {
            let scoreBadge = i.score > 0 ? `<br><span style="color:#00e676; font-weight:700;">Score: ${i.score}/10</span>` : '';
            let linkHtml = i.locationOrLink 
                ? (i.locationOrLink.startsWith('http') 
                    ? `<a href="${escapeHtml(i.locationOrLink)}" target="_blank" style="color:var(--primary-cyan);">🔗 Link</a>` 
                    : escapeHtml(i.locationOrLink))
                : 'N/A';
            return `
                <tr>
                    <td><strong>${i.id}</strong></td>
                    <td>${escapeHtml(i.studentName)}</td>
                    <td>${escapeHtml(i.jobTitle)}</td>
                    <td>Round #${i.roundNumber}<br><span style="font-size:0.8rem; color:var(--primary-cyan);">${escapeHtml(i.roundType)}</span></td>
                    <td>📅 ${escapeHtml(i.scheduledDateTime)}</td>
                    <td>${escapeHtml(i.interviewerName || 'Recruiter')}<br><small style="opacity:0.8;">${linkHtml}</small></td>
                    <td><span class="status-pill status-${i.status.toLowerCase()}">${i.status}</span></td>
                    <td>
                        <button class="action-btn" onclick="openInterviewFeedbackModal('${i.id}')">📝 Result & Feedback</button>
                    </td>
                </tr>
            `;
        }).join('');
    }
}

function openScheduleInterviewModal(appId, candidateName, jobTitle) {
    document.getElementById('schedAppId').value = appId;
    document.getElementById('schedCandidateInfo').value = `${candidateName} — Role: ${jobTitle}`;
    document.getElementById('schedRoundNumber').value = 1;
    document.getElementById('schedDateTime').value = '';
    document.getElementById('schedLocationOrLink').value = '';
    document.getElementById('schedInterviewerName').value = '';
    document.getElementById('scheduleInterviewModal').classList.add('active');
}

function closeScheduleInterviewModal() {
    document.getElementById('scheduleInterviewModal').classList.remove('active');
}

function submitScheduleInterview(event) {
    event.preventDefault();
    const db = getDb();
    const appId = document.getElementById('schedAppId').value;
    const app = db.applications.find(a => a.id === appId);

    if (!app) {
        showToast('Application not found!', 'error');
        return;
    }

    db.counters.interview++;
    const newInterview = {
        id: `INT${db.counters.interview}`,
        applicationId: app.id,
        jobId: app.jobId,
        jobTitle: app.jobTitle,
        studentId: app.studentId,
        studentName: app.studentName,
        companyId: app.companyId,
        companyName: app.companyName,
        roundType: document.getElementById('schedRoundType').value,
        roundNumber: parseInt(document.getElementById('schedRoundNumber').value) || 1,
        scheduledDateTime: document.getElementById('schedDateTime').value.trim(),
        locationOrLink: document.getElementById('schedLocationOrLink').value.trim(),
        interviewerName: document.getElementById('schedInterviewerName').value.trim(),
        status: "SCHEDULED",
        feedback: "",
        score: 0.0
    };

    if (app.status === 'PENDING') {
        app.status = 'SHORTLISTED';
    }

    db.interviews.push(newInterview);
    saveDb(db);

    showToast(`Interview round #${newInterview.roundNumber} scheduled successfully!`, 'success');
    closeScheduleInterviewModal();
    loadCompanyDashboard();
}

function openInterviewFeedbackModal(interviewId) {
    const db = getDb();
    const interview = (db.interviews || []).find(i => i.id === interviewId);
    if (!interview) return;

    document.getElementById('fbInterviewId').value = interview.id;
    document.getElementById('fbInterviewInfo').value = `[${interview.id}] ${interview.studentName} — Round #${interview.roundNumber} (${interview.roundType})`;
    document.getElementById('fbStatus').value = interview.status;
    document.getElementById('fbScore').value = interview.score > 0 ? interview.score : '';
    document.getElementById('fbFeedback').value = interview.feedback || '';
    document.getElementById('interviewFeedbackModal').classList.add('active');
}

function closeInterviewFeedbackModal() {
    document.getElementById('interviewFeedbackModal').classList.remove('active');
}

function submitInterviewFeedback(event) {
    event.preventDefault();
    const db = getDb();
    const intId = document.getElementById('fbInterviewId').value;
    const interview = db.interviews.find(i => i.id === intId);

    if (!interview) {
        showToast('Interview not found!', 'error');
        return;
    }

    interview.status = document.getElementById('fbStatus').value;
    interview.score = parseFloat(document.getElementById('fbScore').value) || 0.0;
    interview.feedback = document.getElementById('fbFeedback').value.trim();

    saveDb(db);

    showToast('Interview result & feedback updated successfully!', 'success');
    closeInterviewFeedbackModal();
    loadCompanyDashboard();
}

// Toast Notifications
function showToast(msg, type = 'success') {
    const container = document.getElementById('toastContainer');
    if (!container) return;
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
