package com.msgfoundation.xmltracer;

import org.camunda.bpm.model.bpmn.instance.CallActivity;
import org.camunda.bpm.model.bpmn.instance.FlowElement;

public class MyCallActivity extends Activity {

    private CallActivity callActivity;

    @Override
    public String getTaskID() {
        return callActivity.getId();
    }

    @Override
    public String getTaskType() {
        return callActivity.getElementType().getTypeName();
    }

    @Override
    public String getTaskName() {
        return callActivity.getName();
    }

    @Override
    public String getTaskImplementationType() {
        if (callActivity.getCalledElement() != null) {
            return "BPMN";
        } else if (callActivity.getCamundaCaseRef() != null) {
            return "CMMN";
        } else {
            return "unknown";
        }
    }

    @Override
    public String getReferenceOrImplementation() {
        String callActivityLink = "N/A";
        if (getTaskImplementationType().equals("BPMN")) {
            callActivityLink = callActivity.getCalledElement();
        } else if (getTaskImplementationType().equals("CMMN")) {
            callActivityLink = callActivity.getCamundaCaseRef();
        }
        return callActivityLink;
    }

    @Override
    public boolean checkTaskType(FlowElement element) {
        return element instanceof CallActivity;
    }

    @Override
    public void processElement(FlowElement element) {
        callActivity = (CallActivity) element;
    }

}
