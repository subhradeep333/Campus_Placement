package web;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import model.*;
import service.AuthService;
import service.RecruitmentService;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class WebServer {
    private int port;
    private AuthService authService;
    private RecruitmentService recruitmentService;
    private HttpServer server;

    public WebServer(int port, AuthService authService, RecruitmentService recruitmentService) {
        this.port = port;
        this.authService = authService;
        this.recruitmentService = recruitmentService;
    }

    public void start() {
        try {
            server = HttpServer.create(new InetSocketAddress(port), 0);

            // Static Files Handler
            server.createContext("/", new StaticFileHandler());

            // API Handlers
            server.createContext("/api/login", new LoginHandler());
            server.createContext("/api/register/student", new RegisterStudentHandler());
            server.createContext("/api/register/company", new RegisterCompanyHandler());
            server.createContext("/api/jobs", new JobsHandler());
            server.createContext("/api/apply", new ApplyHandler());
            server.createContext("/api/cv", new CvHandler());
            server.createContext("/api/applications", new ApplicationsHandler());
            server.createContext("/api/applications/status", new ApplicationStatusHandler());
            server.createContext("/api/companies", new CompaniesHandler());

            server.setExecutor(java.util.concurrent.Executors.newCachedThreadPool());
            server.start();
            System.out.println("🌐 Web Server started! Access UI at: \u001B[36mhttp://localhost:" + port + "\u001B[0m");
        } catch (IOException e) {
            System.err.println("[Web Server Error] Could not start server: " + e.getMessage());
        }
    }

    // Static Asset Handler (index.html, style.css, app.js)
    private class StaticFileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            if (path.equals("/")) {
                path = "/index.html";
            }

            File file = new File("web" + path);
            if (!file.exists() || file.isDirectory()) {
                sendResponse(exchange, 404, "text/plain", "404 Not Found");
                return;
            }

            String mimeType = "text/html";
            if (path.endsWith(".css")) mimeType = "text/css";
            else if (path.endsWith(".js")) mimeType = "application/javascript";
            else if (path.endsWith(".json")) mimeType = "application/json";
            else if (path.endsWith(".png")) mimeType = "image/png";

            byte[] bytes = Files.readAllBytes(file.toPath());
            sendResponseBytes(exchange, 200, mimeType, bytes);
        }
    }

    // --- API Handlers ---

    private class LoginHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            addCorsHeaders(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendResponse(exchange, 455, "application/json", "{\"error\":\"Method not allowed\"}");
                return;
            }

            String body = readRequestBody(exchange);
            Map<String, String> json = parseSimpleJson(body);
            String email = json.get("email");
            String password = json.get("password");

            User user = authService.login(email, password);
            if (user == null) {
                sendResponse(exchange, 401, "application/json", "{\"error\":\"Invalid email or password\"}");
                return;
            }

            StringBuilder resp = new StringBuilder("{");
            resp.append("\"success\":true,");
            resp.append("\"role\":\"").append(user.getRole()).append("\",");
            resp.append("\"user\":{");
            resp.append("\"id\":\"").append(user.getId()).append("\",");
            resp.append("\"email\":\"").append(user.getEmail()).append("\",");

            if (user instanceof Student) {
                Student s = (Student) user;
                resp.append("\"fullName\":\"").append(escapeJson(s.getFullName())).append("\",");
                resp.append("\"phone\":\"").append(escapeJson(s.getPhone())).append("\",");
                resp.append("\"branch\":\"").append(escapeJson(s.getBranch())).append("\",");
                resp.append("\"cgpa\":").append(s.getCgpa()).append(",");
                resp.append("\"skills\":\"").append(escapeJson(s.getSkills())).append("\",");
                resp.append("\"cvSummary\":\"").append(escapeJson(s.getCvSummary())).append("\",");
                resp.append("\"cvFilePath\":\"").append(escapeJson(s.getCvFilePath())).append("\",");
                resp.append("\"hasSubmittedCv\":").append(s.isHasSubmittedCv());
            } else if (user instanceof Company) {
                Company c = (Company) user;
                resp.append("\"companyName\":\"").append(escapeJson(c.getCompanyName())).append("\",");
                resp.append("\"industry\":\"").append(escapeJson(c.getIndustry())).append("\",");
                resp.append("\"location\":\"").append(escapeJson(c.getLocation())).append("\",");
                resp.append("\"website\":\"").append(escapeJson(c.getWebsite())).append("\",");
                resp.append("\"description\":\"").append(escapeJson(c.getDescription())).append("\"");
            }
            resp.append("}}");
            sendResponse(exchange, 200, "application/json", resp.toString());
        }
    }

    private class RegisterStudentHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            addCorsHeaders(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendResponse(exchange, 405, "application/json", "{\"error\":\"Method not allowed\"}");
                return;
            }

            try {
                Map<String, String> json = parseSimpleJson(readRequestBody(exchange));
                String email = json.get("email");
                String password = json.get("password");
                String fullName = json.get("fullName");
                String phone = json.get("phone");
                String branch = json.get("branch");
                double cgpa = Double.parseDouble(json.get("cgpa"));

                Student student = authService.registerStudent(email, password, fullName, phone, branch, cgpa);
                sendResponse(exchange, 200, "application/json", "{\"success\":true, \"message\":\"Registration successful\", \"id\":\"" + student.getId() + "\"}");
            } catch (Exception e) {
                sendResponse(exchange, 400, "application/json", "{\"error\":\"" + escapeJson(e.getMessage()) + "\"}");
            }
        }
    }

    private class RegisterCompanyHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            addCorsHeaders(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendResponse(exchange, 405, "application/json", "{\"error\":\"Method not allowed\"}");
                return;
            }

            try {
                Map<String, String> json = parseSimpleJson(readRequestBody(exchange));
                Company company = authService.registerCompany(
                        json.get("email"),
                        json.get("password"),
                        json.get("companyName"),
                        json.get("industry"),
                        json.get("location"),
                        json.get("website"),
                        json.get("description")
                );
                sendResponse(exchange, 200, "application/json", "{\"success\":true, \"message\":\"Company registered\", \"id\":\"" + company.getId() + "\"}");
            } catch (Exception e) {
                sendResponse(exchange, 400, "application/json", "{\"error\":\"" + escapeJson(e.getMessage()) + "\"}");
            }
        }
    }

    private class JobsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            addCorsHeaders(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                String query = exchange.getRequestURI().getQuery();
                List<JobPosting> jobs;
                if (query != null && query.contains("search=")) {
                    String kw = query.split("search=")[1].split("&")[0];
                    jobs = recruitmentService.filterJobsBySkillOrTitle(kw);
                } else if (query != null && query.contains("companyId=")) {
                    String cid = query.split("companyId=")[1].split("&")[0];
                    jobs = recruitmentService.getCompanyJobPostings(cid);
                } else {
                    jobs = recruitmentService.getAvailableJobsForStudent(null);
                }

                StringBuilder sb = new StringBuilder("[");
                for (int i = 0; i < jobs.size(); i++) {
                    JobPosting j = jobs.get(i);
                    int appCount = recruitmentService.getApplicationsForJob(j.getId()).size();
                    sb.append("{")
                            .append("\"id\":\"").append(j.getId()).append("\",")
                            .append("\"companyId\":\"").append(j.getCompanyId()).append("\",")
                            .append("\"companyName\":\"").append(escapeJson(j.getCompanyName())).append("\",")
                            .append("\"title\":\"").append(escapeJson(j.getTitle())).append("\",")
                            .append("\"description\":\"").append(escapeJson(j.getDescription())).append("\",")
                            .append("\"requiredSkills\":\"").append(escapeJson(j.getRequiredSkills())).append("\",")
                            .append("\"packageLpa\":").append(j.getPackageLpa()).append(",")
                            .append("\"minCgpa\":").append(j.getMinCgpa()).append(",")
                            .append("\"applicantCount\":").append(appCount).append(",")
                            .append("\"active\":").append(j.isActive())
                            .append("}").append(i < jobs.size() - 1 ? "," : "");
                }
                sb.append("]");
                sendResponse(exchange, 200, "application/json", sb.toString());
            } else if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                try {
                    Map<String, String> json = parseSimpleJson(readRequestBody(exchange));
                    String companyId = json.get("companyId");
                    Optional<Company> companyOpt = recruitmentService.getAllCompanies().stream().filter(c -> c.getId().equalsIgnoreCase(companyId)).findFirst();
                    if (companyOpt.isEmpty()) {
                        sendResponse(exchange, 400, "application/json", "{\"error\":\"Company not found\"}");
                        return;
                    }

                    JobPosting job = recruitmentService.createJobPosting(
                            companyOpt.get(),
                            json.get("title"),
                            json.get("description"),
                            json.get("requiredSkills"),
                            Double.parseDouble(json.get("packageLpa")),
                            Double.parseDouble(json.get("minCgpa"))
                    );
                    sendResponse(exchange, 200, "application/json", "{\"success\":true, \"jobId\":\"" + job.getId() + "\"}");
                } catch (Exception e) {
                    sendResponse(exchange, 400, "application/json", "{\"error\":\"" + escapeJson(e.getMessage()) + "\"}");
                }
            }
        }
    }

    private class ApplyHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            addCorsHeaders(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendResponse(exchange, 405, "application/json", "{\"error\":\"Method not allowed\"}");
                return;
            }

            try {
                Map<String, String> json = parseSimpleJson(readRequestBody(exchange));
                String studentId = json.get("studentId");
                String jobId = json.get("jobId");

                Optional<Student> studentOpt = recruitmentService.getStudentDetails(studentId);
                if (studentOpt.isEmpty()) {
                    sendResponse(exchange, 400, "application/json", "{\"error\":\"Student record not found\"}");
                    return;
                }

                String res = recruitmentService.applyForJob(studentOpt.get(), jobId);
                if (res.startsWith("SUCCESS")) {
                    sendResponse(exchange, 200, "application/json", "{\"success\":true, \"message\":\"" + escapeJson(res) + "\"}");
                } else {
                    sendResponse(exchange, 400, "application/json", "{\"error\":\"" + escapeJson(res) + "\"}");
                }
            } catch (Exception e) {
                sendResponse(exchange, 500, "application/json", "{\"error\":\"" + escapeJson(e.getMessage()) + "\"}");
            }
        }
    }

    private class CvHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            addCorsHeaders(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendResponse(exchange, 405, "application/json", "{\"error\":\"Method not allowed\"}");
                return;
            }

            try {
                Map<String, String> json = parseSimpleJson(readRequestBody(exchange));
                String studentId = json.get("studentId");
                Optional<Student> studentOpt = recruitmentService.getStudentDetails(studentId);
                if (studentOpt.isEmpty()) {
                    sendResponse(exchange, 400, "application/json", "{\"error\":\"Student not found\"}");
                    return;
                }

                Student s = studentOpt.get();
                recruitmentService.updateStudentCv(s, json.get("skills"), json.get("cvSummary"), json.get("cvFilePath"));
                sendResponse(exchange, 200, "application/json", "{\"success\":true, \"message\":\"CV profile updated successfully\"}");
            } catch (Exception e) {
                sendResponse(exchange, 500, "application/json", "{\"error\":\"" + escapeJson(e.getMessage()) + "\"}");
            }
        }
    }

    private class ApplicationsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            addCorsHeaders(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            String query = exchange.getRequestURI().getQuery();
            List<Application> apps;
            if (query != null && query.contains("studentId=")) {
                String sid = query.split("studentId=")[1].split("&")[0];
                apps = recruitmentService.getStudentApplications(sid);
            } else if (query != null && query.contains("companyId=")) {
                String cid = query.split("companyId=")[1].split("&")[0];
                apps = recruitmentService.getApplicationsForCompany(cid);
            } else {
                apps = List.of();
            }

            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < apps.size(); i++) {
                Application a = apps.get(i);
                Optional<Student> studentOpt = recruitmentService.getStudentDetails(a.getStudentId());
                Student s = studentOpt.orElse(null);

                sb.append("{")
                        .append("\"id\":\"").append(a.getId()).append("\",")
                        .append("\"jobId\":\"").append(a.getJobId()).append("\",")
                        .append("\"jobTitle\":\"").append(escapeJson(a.getJobTitle())).append("\",")
                        .append("\"studentId\":\"").append(a.getStudentId()).append("\",")
                        .append("\"studentName\":\"").append(escapeJson(a.getStudentName())).append("\",")
                        .append("\"studentBranch\":\"").append(escapeJson(a.getStudentBranch())).append("\",")
                        .append("\"studentCgpa\":").append(a.getStudentCgpa()).append(",")
                        .append("\"companyId\":\"").append(a.getCompanyId()).append("\",")
                        .append("\"companyName\":\"").append(escapeJson(a.getCompanyName())).append("\",")
                        .append("\"status\":\"").append(a.getStatus()).append("\",")
                        .append("\"appliedDate\":\"").append(a.getAppliedDate()).append("\",")
                        .append("\"studentDetails\":{")
                        .append("\"skills\":\"").append(s != null ? escapeJson(s.getSkills()) : "").append("\",")
                        .append("\"cvSummary\":\"").append(s != null ? escapeJson(s.getCvSummary()) : "").append("\",")
                        .append("\"cvFilePath\":\"").append(s != null ? escapeJson(s.getCvFilePath()) : "").append("\",")
                        .append("\"phone\":\"").append(s != null ? escapeJson(s.getPhone()) : "").append("\",")
                        .append("\"email\":\"").append(s != null ? escapeJson(s.getEmail()) : "").append("\"")
                        .append("}")
                        .append("}").append(i < apps.size() - 1 ? "," : "");
            }
            sb.append("]");
            sendResponse(exchange, 200, "application/json", sb.toString());
        }
    }

    private class ApplicationStatusHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            addCorsHeaders(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendResponse(exchange, 405, "application/json", "{\"error\":\"Method not allowed\"}");
                return;
            }

            try {
                Map<String, String> json = parseSimpleJson(readRequestBody(exchange));
                String appId = json.get("applicationId");
                ApplicationStatus status = ApplicationStatus.valueOf(json.get("status").toUpperCase());

                boolean ok = recruitmentService.updateApplicationStatus(appId, status);
                if (ok) {
                    sendResponse(exchange, 200, "application/json", "{\"success\":true, \"message\":\"Status updated to " + status + "\"}");
                } else {
                    sendResponse(exchange, 400, "application/json", "{\"error\":\"Failed to update application status\"}");
                }
            } catch (Exception e) {
                sendResponse(exchange, 500, "application/json", "{\"error\":\"" + escapeJson(e.getMessage()) + "\"}");
            }
        }
    }

    private class CompaniesHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            addCorsHeaders(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            List<Company> list = recruitmentService.getAllCompanies();
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < list.size(); i++) {
                Company c = list.get(i);
                sb.append("{")
                        .append("\"id\":\"").append(c.getId()).append("\",")
                        .append("\"companyName\":\"").append(escapeJson(c.getCompanyName())).append("\",")
                        .append("\"email\":\"").append(escapeJson(c.getEmail())).append("\",")
                        .append("\"industry\":\"").append(escapeJson(c.getIndustry())).append("\",")
                        .append("\"location\":\"").append(escapeJson(c.getLocation())).append("\",")
                        .append("\"website\":\"").append(escapeJson(c.getWebsite())).append("\",")
                        .append("\"description\":\"").append(escapeJson(c.getDescription())).append("\"")
                        .append("}").append(i < list.size() - 1 ? "," : "");
            }
            sb.append("]");
            sendResponse(exchange, 200, "application/json", sb.toString());
        }
    }

    // Utility methods
    private void addCorsHeaders(HttpExchange exchange) {
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Authorization");
    }

    private void sendResponse(HttpExchange exchange, int statusCode, String contentType, String response) throws IOException {
        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
        sendResponseBytes(exchange, statusCode, contentType, bytes);
    }

    private void sendResponseBytes(HttpExchange exchange, int statusCode, String contentType, byte[] bytes) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", contentType + "; charset=UTF-8");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private String readRequestBody(HttpExchange exchange) throws IOException {
        return new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
    }

    private Map<String, String> parseSimpleJson(String jsonStr) {
        Map<String, String> map = new HashMap<>();
        if (jsonStr == null || jsonStr.trim().isEmpty()) return map;

        String clean = jsonStr.trim();
        if (clean.startsWith("{") && clean.endsWith("}")) {
            clean = clean.substring(1, clean.length() - 1);
        }

        String[] pairs = clean.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
        for (String pair : pairs) {
            String[] kv = pair.split(":(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", 2);
            if (kv.length == 2) {
                String k = unescape(kv[0].trim().replaceAll("^\"|\"$", ""));
                String v = unescape(kv[1].trim().replaceAll("^\"|\"$", ""));
                map.put(k, v);
            }
        }
        return map;
    }

    private String unescape(String s) {
        return s.replace("\\\"", "\"").replace("\\\\", "\\").replace("\\n", "\n").replace("\\t", "\t");
    }

    private String escapeJson(String str) {
        if (str == null) return "";
        return str.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "");
    }
}
