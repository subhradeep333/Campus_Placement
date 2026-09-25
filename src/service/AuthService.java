package service;

import model.*;
import java.util.Optional;

public class AuthService {
    private DataStore dataStore;
    private User currentUser;

    public AuthService(DataStore dataStore) {
        this.dataStore = dataStore;
        this.currentUser = null;
    }

    public Student registerStudent(String email, String password, String fullName, String phone, String branch, double cgpa) throws IllegalArgumentException {
        if (dataStore.findStudentByEmail(email).isPresent() || dataStore.findCompanyByEmail(email).isPresent()) {
            throw new IllegalArgumentException("An account with this email already exists!");
        }

        String id = dataStore.generateStudentId();
        Student student = new Student(id, email, password, fullName, phone, branch, cgpa);
        dataStore.addStudent(student);
        PersistenceService.saveDataStore(dataStore);
        return student;
    }

    public Company registerCompany(String email, String password, String companyName, String industry, String location, String website, String description) throws IllegalArgumentException {
        if (dataStore.findStudentByEmail(email).isPresent() || dataStore.findCompanyByEmail(email).isPresent()) {
            throw new IllegalArgumentException("An account with this email already exists!");
        }

        String id = dataStore.generateCompanyId();
        Company company = new Company(id, email, password, companyName, industry, location, website, description);
        dataStore.addCompany(company);
        PersistenceService.saveDataStore(dataStore);
        return company;
    }

    public User login(String email, String password) {
        if (email == null || password == null) return null;
        String cleanEmail = email.trim();
        String cleanPass = password.trim();

        Optional<Student> studentOpt = dataStore.findStudentByEmail(cleanEmail);
        if (studentOpt.isPresent()) {
            Student s = studentOpt.get();
            if (s.getPassword() != null && s.getPassword().trim().equals(cleanPass)) {
                this.currentUser = s;
                return s;
            }
        }

        Optional<Company> companyOpt = dataStore.findCompanyByEmail(cleanEmail);
        if (companyOpt.isPresent()) {
            Company c = companyOpt.get();
            if (c.getPassword() != null && c.getPassword().trim().equals(cleanPass)) {
                this.currentUser = c;
                return c;
            }
        }

        return null;
    }

    public void logout() {
        this.currentUser = null;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }
}
