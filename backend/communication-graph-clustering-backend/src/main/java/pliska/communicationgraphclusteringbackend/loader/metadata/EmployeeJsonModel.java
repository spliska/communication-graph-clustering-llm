package pliska.communicationgraphclusteringbackend.loader.metadata;

public class EmployeeJsonModel {
    private String name;
    private String department;
    private String long_department;
    private String title;


    public String getFirstName() {
        return name.split(" ")[0];
    }

    public String getLastName() {
        String[] parts = name.split(" ");
        return parts.length > 1 ? parts[parts.length - 1] : "";
    }

    public String getDepartment() {
        return department;
    }

    public String getLongDepartment() {
        return long_department;
    }

    public String getTitle() {
        return title;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public void setLong_department(String long_department) {
        this.long_department = long_department;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}

