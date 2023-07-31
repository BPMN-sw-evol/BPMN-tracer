package com.mycompany.xmltracer;

import org.camunda.bpm.model.bpmn.instance.FlowElement;
import org.camunda.bpm.model.bpmn.instance.ReceiveTask;

public class MyReceiveTask extends Activity {

    private ReceiveTask receiveTask;

    @Override
    public String getTaskID() {
        return receiveTask.getId();
    }

    @Override
    public String getTaskType() {
        return receiveTask.getElementType().getTypeName();
    }

    @Override
    public String getTaskName() {
        return receiveTask.getName();
    }

    @Override
    public String getTaskImplementationType() {
        return "N/A";
    }

    @Override
    public String getReferenceOrImplementation() {
        return receiveTask.getMessage().getTextContent();
    }

    @Override
    public boolean checkTaskType(FlowElement element) {
        return element instanceof ReceiveTask;
    }

    @Override
    public void processElement(FlowElement element) {
        receiveTask = (ReceiveTask) element;
    }

}
