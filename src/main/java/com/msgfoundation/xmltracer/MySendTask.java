package com.msgfoundation.xmltracer;

import org.camunda.bpm.model.bpmn.instance.FlowElement;
import org.camunda.bpm.model.bpmn.instance.SendTask;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaConnector;

public class MySendTask extends Activity {

    private SendTask sendTask;

    @Override
    public String getTaskID() {
        return sendTask.getId();
    }

    @Override
    public String getTaskType() {
        return sendTask.getElementType().getTypeName();
    }

    @Override
    public String getTaskName() {
        return sendTask.getName();
    }

    @Override
    public String getTaskImplementationType() {
        if (sendTask.getCamundaTopic() != null) {
            return "External";
        } else if (sendTask.getCamundaClass() != null) {
            return "Java class";
        } else if (sendTask.getCamundaExpression() != null || sendTask.getCamundaResultVariable() != null) {
            return "Expression";
        } else if (sendTask.getCamundaDelegateExpression() != null) {
            return "Delegate expression";
        } else if (sendTask.getExtensionElements() != null && sendTask.getExtensionElements().getElementsQuery().filterByType(CamundaConnector.class).count() > 0) {
            return "Connector";
        } else {
            return "unknown";
        }
    }

    @Override
    public String getReferenceOrImplementation() {
        String sendTaskLink = "NA";
        if (getTaskImplementationType().equals("External")) {
            sendTaskLink = sendTask.getCamundaTopic();
        } else if (getTaskImplementationType().equals("Java class")) {
            sendTaskLink = sendTask.getCamundaClass();
        } else if (getTaskImplementationType().equals("Expression")) {
            if (sendTask.getCamundaExpression() != null) {
                sendTaskLink = sendTask.getCamundaExpression();
                if (sendTask.getCamundaResultVariable() != null) {
                    sendTaskLink = sendTaskLink + " , " + sendTask.getCamundaResultVariable();
                }
            }
            if (sendTask.getCamundaResultVariable() != null) {
                sendTaskLink = sendTask.getCamundaResultVariable();
            }
        } else if (getTaskImplementationType().equals("Delegate expression")) {
            sendTaskLink = sendTask.getCamundaDelegateExpression();
        } else if (getTaskImplementationType().equals("Connector")) {
            CamundaConnector camundaConnector = sendTask.getExtensionElements().getElementsQuery().filterByType(CamundaConnector.class).singleResult();
            if (camundaConnector != null) {
                sendTaskLink = camundaConnector.getCamundaConnectorId().getTextContent();
            }
        }
        return sendTaskLink;
    }

    @Override
    public boolean checkTaskType(FlowElement element) {
        return element instanceof SendTask;
    }

    @Override
    public void processElement(FlowElement element) {
        sendTask = (SendTask) element;
    }

}
