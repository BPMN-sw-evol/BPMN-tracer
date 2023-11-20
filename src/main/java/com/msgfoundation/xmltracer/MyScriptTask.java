package com.msgfoundation.xmltracer;

import org.camunda.bpm.model.bpmn.instance.FlowElement;
import org.camunda.bpm.model.bpmn.instance.ScriptTask;

public class MyScriptTask extends Activity {

    private ScriptTask scriptTask;

    @Override
    public String getTaskID() {
        return scriptTask.getId();
    }

    @Override
    public String getTaskType() {
        return scriptTask.getElementType().getTypeName();
    }

    @Override
    public String getTaskName() {
        return scriptTask.getName();
    }

    @Override
    public String getTaskImplementationType() {
        if (scriptTask.getCamundaResource() != null) {
            return "External resource";
        } else if (scriptTask.getScript() != null) {
            return "Inline script";
        } else {
            return "unknown";
        }
    }

    @Override
    public String getReferenceOrImplementation() {
        String scriptTaskLink = "NA";
        if (getTaskImplementationType().equals("External resource")) {
            scriptTaskLink = scriptTask.getCamundaResource();
        } else if (getTaskImplementationType().equals("Inline script")) {
            scriptTaskLink = scriptTask.getScript().getTextContent();
        }
        return scriptTaskLink;    }

    @Override
    public boolean checkTaskType(FlowElement element) {
        return element instanceof ScriptTask;
    }

    @Override
    public void processElement(FlowElement element) {
        scriptTask = (ScriptTask) element;
    }

}
