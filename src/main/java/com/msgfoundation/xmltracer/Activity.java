package com.msgfoundation.xmltracer;

import org.camunda.bpm.model.bpmn.instance.FlowElement;

public abstract class Activity {

    protected String taskID;
    protected String taskType;
    protected String taskName;
    protected String taskImplementationType;
    protected String referenceOrImplementation;

    public abstract String getTaskID();

    public abstract String getTaskType();

    public abstract String getTaskName();

    public abstract String getTaskImplementationType();

    public abstract String getReferenceOrImplementation();

    public abstract boolean checkTaskType(FlowElement element);

    public abstract void processElement(FlowElement element);

    public void setTaskID(String taskID) {
        this.taskID = taskID;
    }

    public void setTaskType(String taskType) {
        this.taskType = taskType;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public void setTaskImplementationType(String taskImplementationType) {
        this.taskImplementationType = taskImplementationType;
    }

    public void setReferenceOrImplementation(String referenceOrImplementation) {
        this.referenceOrImplementation = referenceOrImplementation;
    }

    @Override
    public String toString() {
        return String.format("%-40s %-20s %-50s %-30s %-50s\n",
                getTaskID(),
                getTaskType(),
                getTaskName(),
                getTaskImplementationType(),
                getReferenceOrImplementation());
    }

}
