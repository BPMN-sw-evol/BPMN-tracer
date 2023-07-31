package com.mycompany.xmltracer;

import org.camunda.bpm.model.bpmn.instance.FlowElement;
import org.camunda.bpm.model.bpmn.instance.ManualTask;
import org.camunda.bpm.model.bpmn.instance.Task;

public class MyDefaultTask extends Activity {
    private Task defaultTask;

    @Override
    public String getTaskID() {
        return defaultTask.getId();
    }

    @Override
    public String getTaskType() {
        return defaultTask.getElementType().getTypeName();
    }

    @Override
    public String getTaskName() {
        return defaultTask.getName();
    }

    @Override
    public String getTaskImplementationType() {
        return "N/A";
    }

    @Override
    public String getReferenceOrImplementation() {
        return "N/A";
    }

    @Override
    public boolean checkTaskType(FlowElement element) {
        return element instanceof ManualTask || element instanceof Task;
    }

    @Override
    public void processElement(FlowElement element) {
        defaultTask = (Task) element;
    }

}
