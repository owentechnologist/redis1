package ex1;

import java.beans.JavaBean;

@JavaBean
public class User {
    private Integer id;
    private String fName;
    private String lName;
    private String jobTitle;

    public String getfName() {
        return fName;
    }

    public Integer getId() {
        return id;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public String getlName() {
        return lName;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public void setfName(String fName) {
        this.fName = fName;
    }

    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }

    public void setlName(String lName) {
        this.lName = lName;
    }
}
