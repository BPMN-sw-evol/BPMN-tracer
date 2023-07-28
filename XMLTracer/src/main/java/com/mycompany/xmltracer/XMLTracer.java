package com.mycompany.xmltracer;

import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.util.Collection;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.FlowElement;
import org.camunda.bpm.model.bpmn.instance.ServiceTask;
import org.camunda.bpm.model.bpmn.instance.UserTask;
import org.camunda.bpm.model.bpmn.instance.SendTask;
import org.camunda.bpm.model.bpmn.instance.ReceiveTask;
import org.camunda.bpm.model.bpmn.instance.ManualTask;
import org.camunda.bpm.model.bpmn.instance.BusinessRuleTask;
import org.camunda.bpm.model.bpmn.instance.ScriptTask;
import org.camunda.bpm.model.bpmn.instance.CallActivity;
import org.camunda.bpm.model.bpmn.instance.Task;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaConnector;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaFormData;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaFormField;

public class XMLTracer {

    public static void main(String[] args) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select a BPMN file");
        fileChooser.setFileFilter(new FileNameExtensionFilter("BPMN files (*.bpmn)", "bpmn"));

        int result = fileChooser.showOpenDialog(null);

        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            System.out.println("Selected File Path: " + file.getAbsolutePath());
            BpmnModelInstance modelInstance = Bpmn.readModelFromFile(file);
            Collection<FlowElement> flowElements = modelInstance.getModelElementsByType(FlowElement.class);
            System.out.println("Collection size: " + flowElements.size());
            System.out.format("%-40s %-20s %-50s %-30s %-50s\n", "Task ID", "Task Type", "Task Name", "Implementation Type", "Reference/Implementation");

            for (FlowElement element : flowElements) {
                if (element instanceof UserTask) {
                    UserTask userTask = (UserTask) element;
                    String userTaskType = getUserTaskType(userTask);
                    String userTaskLink = getUserTaskLink(userTask);
                    System.out.format("%-40s %-20s %-50s %-30s %-50s\n", userTask.getId(), userTask.getElementType().getTypeName(), userTask.getName(), userTaskType, userTaskLink);
                } else if (element instanceof ServiceTask) {
                    ServiceTask serviceTask = (ServiceTask) element;
                    String serviceTaskType = getServiceTaskType(serviceTask);
                    String serviceTaskLink = getServiceTaskLink(serviceTask, serviceTaskType);
                    System.out.format("%-40s %-20s %-50s %-30s %-50s\n", serviceTask.getId(), serviceTask.getElementType().getTypeName(), serviceTask.getName(), serviceTaskType, serviceTaskLink);
                } else if (element instanceof SendTask) {
                    SendTask sendTask = (SendTask) element;
                    String sendTaskType = getSendTaskType(sendTask);
                    String sendTaskLink = getSendTaskLink(sendTask, sendTaskType);
                    System.out.format("%-40s %-20s %-50s %-30s %-50s\n", sendTask.getId(), sendTask.getElementType().getTypeName(), sendTask.getName(), sendTaskType, sendTaskLink);
                } else if (element instanceof ReceiveTask) {
                    ReceiveTask receiveTask = (ReceiveTask) element;
                    System.out.format("%-40s %-20s %-50s %-30s %-50s\n", receiveTask.getId(), receiveTask.getElementType().getTypeName(), receiveTask.getName(), "N/A", receiveTask.getMessage());
                } else if (element instanceof BusinessRuleTask) {
                    BusinessRuleTask businessRuleTask = (BusinessRuleTask) element;
                    String businessRuleTaskType = getBusinessRuleTaskType(businessRuleTask);
                    String businessRuleTaskLink = getBusinessRuleTaskLink(businessRuleTask, businessRuleTaskType);
                    System.out.format("%-40s %-20s %-50s %-30s %-50s\n", businessRuleTask.getId(), businessRuleTask.getElementType().getTypeName(), businessRuleTask.getName(), businessRuleTaskType, businessRuleTaskLink);
                } else if (element instanceof ScriptTask) {
                    ScriptTask scriptTask = (ScriptTask) element;
                    String scriptTaskType = getScriptTaskType(scriptTask);
                    String scriptTaskLink = getScriptTaskLink(scriptTask, scriptTaskType);
                    System.out.format("%-40s %-20s %-50s %-30s %-50s\n", scriptTask.getId(), scriptTask.getElementType().getTypeName(), scriptTask.getName(), scriptTaskType, scriptTaskLink);
                } else if (element instanceof CallActivity) {
                    CallActivity callActivity = (CallActivity) element;
                    String callActivityType = getCallActivityType(callActivity);
                    String callActivityLink = getCallActivityLink(callActivity, callActivityType);
                    System.out.format("%-40s %-20s %-50s %-30s %-50s\n", callActivity.getId(), callActivity.getElementType().getTypeName(), callActivity.getName(), callActivityType, callActivityLink);
                } else if (element instanceof ManualTask) {
                    ManualTask manualTask = (ManualTask) element;
                    System.out.format("%-40s %-20s %-50s %-30s %-50s\n", manualTask.getId(), manualTask.getElementType().getTypeName(), manualTask.getName(), "N/A", "N/A");
                } else if (element instanceof Task) {
                    Task task = (Task) element;
                    System.out.format("%-40s %-20s %-50s %-30s %-50s\n", task.getId(), task.getElementType().getTypeName(), task.getName(), "N/A", "N/A");
                }

            }
        } else {
            System.out.println("No file selected.");
        }
    }

    private static String getServiceTaskType(ServiceTask serviceTask) {
        if (serviceTask.getCamundaTopic() != null) {
            return "External";
        } else if (serviceTask.getCamundaClass() != null) {
            return "Java class";
        } else if (serviceTask.getCamundaExpression() != null || serviceTask.getCamundaResultVariable() != null) {
            return "Expression";
        } else if (serviceTask.getCamundaDelegateExpression() != null) {
            return "Delegate expression";
        } else if (serviceTask.getExtensionElements() != null && serviceTask.getExtensionElements().getElementsQuery().filterByType(CamundaConnector.class).count() > 0) {
//            CamundaConnector camundaConnector = serviceTask.getExtensionElements().getElementsQuery().filterByType(CamundaConnector.class).singleResult();
//            if (camundaConnector != null) {
//                String connectorID = camundaConnector.getTextContent();
//                System.out.println(connectorID);
//            }
            return "Connector";
        } else {
            return "unknown";
        }
    }

    private static String getServiceTaskLink(ServiceTask serviceTask, String serviceTaskType) {
        String serviceTaskLink = "NA";
        if (serviceTaskType.equals("External")) {
            serviceTaskLink = serviceTask.getCamundaTopic();
        } else if (serviceTaskType.equals("Java class")) {
            serviceTaskLink = serviceTask.getCamundaClass();
        } else if (serviceTaskType.equals("Expression")) {
            if (serviceTask.getCamundaExpression() != null) {
                serviceTaskLink = serviceTask.getCamundaExpression();
                if (serviceTask.getCamundaResultVariable() != null) {
                    serviceTaskLink = serviceTaskLink + " , " + serviceTask.getCamundaResultVariable();
                }
            }
            if (serviceTask.getCamundaResultVariable() != null) {
                serviceTaskLink = serviceTask.getCamundaResultVariable();
            }
        } else if (serviceTaskType.equals("Delegate expression")) {
            serviceTaskLink = serviceTask.getCamundaDelegateExpression();
        } else if (serviceTaskType.equals("Connector")) {
            CamundaConnector camundaConnector = serviceTask.getExtensionElements().getElementsQuery().filterByType(CamundaConnector.class).singleResult();
            if (camundaConnector != null) {
                serviceTaskLink = camundaConnector.getCamundaConnectorId().getTextContent();
            }
        }
        return serviceTaskLink;
    }

    private static String getSendTaskType(SendTask sendTask) {
        if (sendTask.getCamundaTopic() != null) {
            return "External";
        } else if (sendTask.getCamundaClass() != null) {
            return "Java class";
        } else if (sendTask.getCamundaExpression() != null || sendTask.getCamundaResultVariable() != null) {
            return "Expression";
        } else if (sendTask.getCamundaDelegateExpression() != null) {
            return "Delegate expression";

        } else if (sendTask.getExtensionElements() != null && sendTask.getExtensionElements().getElementsQuery().filterByType(CamundaConnector.class).count() > 0) {
//            CamundaConnector camundaConnector = sendTask.getExtensionElements().getElementsQuery().filterByType(CamundaConnector.class).singleResult();
//            if (camundaConnector != null) {
//                String connectorID = camundaConnector.getTextContent();
//                System.out.println(connectorID);
//            }

            return "Connector";
        } else {
            return "unknown";
        }
    }

    private static String getSendTaskLink(SendTask sendTask, String sendTaskType) {
        String sendTaskLink = "NA";
        if (sendTaskType.equals("External")) {
            sendTaskLink = sendTask.getCamundaTopic();
        } else if (sendTaskType.equals("Java class")) {
            sendTaskLink = sendTask.getCamundaClass();
        } else if (sendTaskType.equals("Expression")) {
            if (sendTask.getCamundaExpression() != null) {
                sendTaskLink = sendTask.getCamundaExpression();
                if (sendTask.getCamundaResultVariable() != null) {
                    sendTaskLink = sendTaskLink + " , " + sendTask.getCamundaResultVariable();
                }
            }
            if (sendTask.getCamundaResultVariable() != null) {
                sendTaskLink = sendTask.getCamundaResultVariable();
            }
        } else if (sendTaskType.equals("Delegate expression")) {
            sendTaskLink = sendTask.getCamundaDelegateExpression();
        } else if (sendTaskType.equals("Connector")) {
            CamundaConnector camundaConnector = sendTask.getExtensionElements().getElementsQuery().filterByType(CamundaConnector.class).singleResult();
            if (camundaConnector != null) {
                sendTaskLink = camundaConnector.getCamundaConnectorId().getTextContent();
            }
        }
        return sendTaskLink;
    }

    private static String getBusinessRuleTaskType(BusinessRuleTask businessRuleTask) {
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
//            CamundaConnector camundaConnector = businessRuleTask.getExtensionElements().getElementsQuery().filterByType(CamundaConnector.class).singleResult();
//            if (camundaConnector != null) {
//                String connectorID = camundaConnector.getTextContent();
//                System.out.println(connectorID);
//            }

            return "Connector";
        } else {
            return "unknown";
        }
    }

    private static String getBusinessRuleTaskLink(BusinessRuleTask businessRuleTask, String businessRuleTaskType) {
        String businessRuleTaskLink = "NA";
        if (businessRuleTaskType.equals("DMN")) {
            businessRuleTaskLink = businessRuleTask.getCamundaDecisionRef();
        } else if (businessRuleTaskType.equals("External")) {
            businessRuleTaskLink = businessRuleTask.getCamundaTopic();
        } else if (businessRuleTaskType.equals("Java class")) {
            businessRuleTaskLink = businessRuleTask.getCamundaClass();  
        }else if (businessRuleTaskType.equals("Expression")) {
            if (businessRuleTask.getCamundaExpression() != null) {
                businessRuleTaskLink = businessRuleTask.getCamundaExpression();
                if (businessRuleTask.getCamundaResultVariable() != null) {
                    businessRuleTaskLink = businessRuleTaskLink + " , " + businessRuleTask.getCamundaResultVariable();
                }
            }
            if (businessRuleTask.getCamundaResultVariable() != null) {
                businessRuleTaskLink = businessRuleTask.getCamundaResultVariable();
            }
        } else if (businessRuleTaskType.equals("Delegate expression")) {
            businessRuleTaskLink = businessRuleTask.getCamundaDelegateExpression();
        } else if (businessRuleTaskType.equals("Connector")) {
            CamundaConnector camundaConnector = businessRuleTask.getExtensionElements().getElementsQuery().filterByType(CamundaConnector.class).singleResult();
            if (camundaConnector != null) {
                businessRuleTaskLink = camundaConnector.getCamundaConnectorId().getTextContent();
            }
        }
        return businessRuleTaskLink;
    }

    private static String getScriptTaskType(ScriptTask scriptTask) {
        if (scriptTask.getCamundaResource() != null) {
            return "External resource";
        } else if (scriptTask.getScript() != null) {
            return "Inline script";
        } else {
            return "unknown";
        }
    }
    
    private static String getScriptTaskLink(ScriptTask scriptTask, String scriptTaskType) {
        String scriptTaskLink = "NA";
        if (scriptTaskType.equals("External resource")) {
            scriptTaskLink = scriptTask.getCamundaResource();
        } else if (scriptTaskLink.equals("Inline script")) {
            scriptTaskLink = scriptTask.getScript().getTextContent();
        } 
        return scriptTaskLink;
    }

    private static String getCallActivityType(CallActivity callActivity) {
        if (callActivity.getCalledElement() != null) {
            return "BPMN";
        } else if (callActivity.getCamundaCaseRef() != null) {
            return "CMMN";
        } else {
            return "unknown";
        }
    }
    private static String getCallActivityLink(CallActivity callActivity, String callActivityType) {
        String callActivityLink = "N/A";
        if (callActivityType.equals("BPMN")) {
            callActivityLink = callActivity.getCalledElement();
        } else if (callActivityType.equals("CMMN")) {
            callActivityLink = callActivity.getCamundaCaseRef();
        } 
        return callActivityLink;
    }
    
    public static String getUserTaskType(UserTask userTask) {
        String formType = "Unknown";
        if (userTask.getCamundaFormRef() != null) {
            formType = "Camunda Form";
        } else if (userTask.getCamundaFormKey() != null) {
            formType = "Embedded or External Task Form";

        } else if (userTask.getExtensionElements().getElementsQuery().filterByType(CamundaFormData.class
        ).count() > 0) {
            formType = "Generated Task Form";
        }
        return formType;
    }

    public static Collection<CamundaFormField> getFieldsForm(UserTask userTask) {
        CamundaFormData camundaFormData = userTask.getExtensionElements().getElementsQuery().filterByType(CamundaFormData.class).singleResult();
        if (camundaFormData != null) {
            return camundaFormData.getCamundaFormFields();
        }
        return null;
    }

    public static String hasFormFields(UserTask userTask) {
        CamundaFormData camundaFormData = userTask.getExtensionElements().getElementsQuery().filterByType(CamundaFormData.class).singleResult();
        String hasFormField = "Doesn't have fields form";
        if (camundaFormData != null) {
            if (!camundaFormData.getCamundaFormFields().isEmpty()) {
                hasFormField = "Have fields form";
            }
        }
        return hasFormField;
    }

    public static String getUserTaskLink(UserTask userTask) {
        String userTaskLink = "NA";
        if (getUserTaskType(userTask).equals("Camunda Form")) {
            userTaskLink = userTask.getCamundaFormRef();
        } else if (getUserTaskType(userTask).equals("Embedded or External Task Form")) {
            userTaskLink = userTask.getCamundaFormKey();
        } else if (getUserTaskType(userTask).equals("Generated Task Form")) {
            userTaskLink = hasFormFields(userTask);
        }
        return userTaskLink;
    }
}
