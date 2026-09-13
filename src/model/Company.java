package model;

public class Company extends User {
    private static final long serialVersionUID = 1L;

    private String companyName;
    private String industry;
    private String location;
    private String website;
    private String description;

    public Company(String id, String email, String password, String companyName, String industry, String location, String website, String description) {
        super(id, email, password, UserRole.COMPANY);
        this.companyName = companyName;
        this.industry = industry;
        this.location = location;
        this.website = website;
        this.description = description;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getIndustry() {
        return industry;
    }

    public void setIndustry(String industry) {
        this.industry = industry;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
