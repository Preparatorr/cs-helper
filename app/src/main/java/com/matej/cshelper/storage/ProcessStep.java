package com.matej.cshelper.storage;

public class ProcessStep {

    public enum Status
    {
        NOT_STARTED,
        DONE,
        IGNORE
    }

    public String name = "";
    public Status status = Status.NOT_STARTED;
    public int type = 0;
    public boolean mandatory = true;

    public ProcessStep(ProcessStep step)
    {
        this.name = step.name;
        this.status = step.status;
        this.type = step.type;
        this.mandatory = step.mandatory;
    }
}
