package com.msgfoundation.xmltracer;

import org.camunda.bpm.model.bpmn.instance.FlowElement;
import org.camunda.bpm.model.bpmn.instance.ServiceTask;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaConnector;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaIn;

import java.util.List;

public class MyServiceTask extends Activity {

    private ServiceTask serviceTask;

    @Override
    public String getTaskID() {
        return serviceTask.getId();
    }

    @Override
    public String getTaskType() {
        return serviceTask.getElementType().getTypeName();
    }

    @Override
    public String getTaskName() {
        return serviceTask.getName();
    }

    @Override
    public String getTaskImplementationType() {
        if (serviceTask.getCamundaTopic() != null) {
            return "External";
        } else if (serviceTask.getCamundaClass() != null) {
            return "Java class";
        } else if (serviceTask.getCamundaExpression() != null || serviceTask.getCamundaResultVariable() != null) {
            return "Expression";
        } else if (serviceTask.getCamundaDelegateExpression() != null) {
            return "Delegate expression";
        } else if (serviceTask.getExtensionElements() != null && serviceTask.getExtensionElements().getElementsQuery().filterByType(CamundaConnector.class).count() > 0) {
            return "Connector";
        } else {
            return "unknown";
        }
    }

    @Override
    public String getReferenceOrImplementation() {
        String serviceTaskLink = "NA";
        if (getTaskImplementationType().equals("External")) {
            serviceTaskLink = serviceTask.getCamundaTopic();
        } else if (getTaskImplementationType().equals("Java class")) {
            serviceTaskLink = serviceTask.getCamundaClass();
        } else if (getTaskImplementationType().equals("Expression")) {
            if (serviceTask.getCamundaExpression() != null) {
                serviceTaskLink = serviceTask.getCamundaExpression();
                if (serviceTask.getCamundaResultVariable() != null) {
                    serviceTaskLink = serviceTaskLink + " , " + serviceTask.getCamundaResultVariable();
                }
            }
            if (serviceTask.getCamundaResultVariable() != null) {
                serviceTaskLink = serviceTask.getCamundaResultVariable();
            }
        } else if (getTaskImplementationType().equals("Delegate expression")) {
            serviceTaskLink = serviceTask.getCamundaDelegateExpression();
        } else if (getTaskImplementationType().equals("Connector")) {
            CamundaConnector camundaConnector = serviceTask.getExtensionElements().getElementsQuery().filterByType(CamundaConnector.class).singleResult();
            if (camundaConnector != null) {
                serviceTaskLink = camundaConnector.getCamundaConnectorId().getTextContent();
            }
        }
        return serviceTaskLink;
    }

    @Override
    public boolean checkTaskType(FlowElement element) {
        return element instanceof ServiceTask;
    }

    @Override
    public void processElement(FlowElement element) {
        serviceTask = (ServiceTask) element;
    }

}
