package edu.yale.sml.model;

import lombok.ToString;

import java.util.Date;

@ToString(callSuper=true, includeFieldNames=true)
public class Log implements java.io.Serializable {

    private static final long serialVersionUID = 1777878878L;
    private Integer id;
    private String net_id;
    private Date timestamp;
    private String operation;
    private String input_file;
    private String application_env;
    private String user_env;
    private String stacktrace;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNet_id() {
        return net_id;
    }

    public void setNet_id(String net_id) {
        this.net_id = net_id;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public String getInput_file() {
        return input_file;
    }

    public void setInput_file(String input_file) {
        this.input_file = input_file;
    }

    public String getApplication_env() {
        return application_env;
    }

    public void setApplication_env(String application_env) {
        this.application_env = application_env;
    }

    public String getUser_env() {
        return user_env;
    }

    public Log() {
        super();
    }

    public void setUser_env(String user_env) {
        this.user_env = user_env;
    }

    public String getStacktrace() {
        return stacktrace;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public void setStacktrace(String stacktrace) {
        this.stacktrace = stacktrace;
    }


}