package ex1;

import java.beans.JavaBean;

@JavaBean
public class Task {
    private String taskName;
    private String taskExtraInfo;
    private Integer id;

    public Task(){}

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public String getTaskExtraInfo() {
        return taskExtraInfo;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskExtraInfo(String taskExtraInfo) {
        this.taskExtraInfo = taskExtraInfo;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }
}

