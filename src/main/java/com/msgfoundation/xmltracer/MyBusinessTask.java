package com.msgfoundation.xmltracer;

import org.camunda.bpm.model.bpmn.instance.BusinessRuleTask;
import org.camunda.bpm.model.bpmn.instance.FlowElement;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaConnector;

public class MyBusinessTask extends Activity {

    private BusinessRuleTask businessRuleTask;

    @Override
    public String getTaskID() {
        return businessRuleTask.getId();
    }

    @Override
    public String getTaskType() {
        return businessRuleTask.getElementType().getTypeName();
    }

    @Override
    public String getTaskName() {
        return businessRuleTask.getName();
    }

    @Override
    public String getTaskImplementationType() {
        if (businessRuleTask.getCamundaDecisionRef() != null) {
            return "DMN";
        } else if (businessRuleTask.getCamundaTopic() != null) {
            return "External";
        } else if (businessRuleTask.getCamundaClass() != null) {
            return "Java class";
        } else if (businessRuleTask.getCamundaExpression() != null || businessRuleTask.getCamundaResultVariable() != null) {
            return "Expression";
        } else if (businessRuleTask.getCamundaDelegateExpression() != null) {
            return "Delegate expression";
        } else if (businessRuleTask.getExtensionElements() != null && businessRuleTask.getExtensionElements().getElementsQuery().filterByType(CamundaConnector.class).count() > 0) {
            return "Connector";
        } else {
            return "unknown";
        }
    }

    @Override
    public String getReferenceOrImplementation() {
        String businessRuleTaskLink = "NA";
        if (getTaskImplementationType().equals("DMN")) {
            businessRuleTaskLink = businessRuleTask.getCamundaDecisionRef();
        } else if (getTaskImplementationType().equals("External")) {
            businessRuleTaskLink = businessRuleTask.getCamundaTopic();
        } else if (getTaskImplementationType().equals("Java class")) {
            businessRuleTaskLink = businessRuleTask.getCamundaClass();
        } else if (getTaskImplementationType().equals("Expression")) {
            if (businessRuleTask.getCamundaExpression() != null) {
                businessRuleTaskLink = businessRuleTask.getCamundaExpression();
                if (businessRuleTask.getCamundaResultVariable() != null) {
                    businessRuleTaskLink = businessRuleTaskLink + " , " + businessRuleTask.getCamundaResultVariable();
                }
            }
            if (businessRuleTask.getCamundaResultVariable() != null) {
                businessRuleTaskLink = businessRuleTask.getCamundaResultVariable();
            }
        } else if (getTaskImplementationType().equals("Delegate expression")) {
            businessRuleTaskLink = businessRuleTask.getCamundaDelegateExpression();
        } else if (getTaskImplementationType().equals("Connector")) {
            CamundaConnector camundaConnector = businessRuleTask.getExtensionElements().getElementsQuery().filterByType(CamundaConnector.class).singleResult();
            if (camundaConnector != null) {
                businessRuleTaskLink = camundaConnector.getCamundaConnectorId().getTextContent();
            }
        }
        return businessRuleTaskLink;
    }

    @Override
    public boolean checkTaskType(FlowElement element) {
        return element instanceof BusinessRuleTask;
    }

    @Override
    public void processElement(FlowElement element) {
        businessRuleTask = (BusinessRuleTask) element;
    }

}
