package com.mycompany.xmltracer;

import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
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
import org.camunda.bpm.model.bpmn.instance.ExtensionElements;
import org.camunda.bpm.model.bpmn.instance.Task;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaConnector;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaFormData;
import org.camunda.bpm.model.xml.ModelInstance;
import org.camunda.bpm.model.xml.instance.ModelElementInstance;

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
                    String userTaskLink = "";
                    if (getFormType(userTask).equals("Camunda Form")) {
                        userTaskLink = userTask.getCamundaFormRef();
                    } else if (getFormType(userTask).equals("Embedded or External Task Form")) {
                        userTaskLink = userTask.getCamundaFormKey();
                    }else{
                        userTaskLink = "N/A";
                    }
                    System.out.format("%-40s %-20s %-50s %-30s %-50s\n", userTask.getId(), userTask.getElementType().getTypeName(), userTask.getName(), getFormType(userTask), userTaskLink);
                } else if (element instanceof ServiceTask) {
                    ServiceTask serviceTask = (ServiceTask) element;
                    System.out.format("%-40s %-20s %-50s %-30s %-50s\n", serviceTask.getId(), serviceTask.getElementType().getTypeName(), serviceTask.getName(), getImplementationType(serviceTask), serviceTask.getCamundaClass());
                } else if (element instanceof SendTask) {
                    SendTask sendTask = (SendTask) element;
                    System.out.format("%-40s %-20s %-50s %-30s %-50s\n", sendTask.getId(), sendTask.getElementType().getTypeName(), sendTask.getName(), getImplementationSendTask(sendTask), sendTask.getAttributeValueNs("camunda", "connectorId"));
                } else if (element instanceof ReceiveTask) {
                    ReceiveTask receiveTask = (ReceiveTask) element;
                    System.out.println(receiveTask.toString());
                    //System.out.format("%-40s %-20s %-50s %-30s %-50s\n", receiveTask.getId(), receiveTask.getElementType().getTypeName(), receiveTask.getName(), getMessageImplementation(receiveTask));
                } else if (element instanceof BusinessRuleTask) {
                    BusinessRuleTask businessRuleTask = (BusinessRuleTask) element;
                    System.out.format("%-40s %-20s %-50s %-30s %-50s\n", businessRuleTask.getId(), businessRuleTask.getElementType().getTypeName(), businessRuleTask.getName(), getImplementationBusinessRuleTask(businessRuleTask), businessRuleTask.getCamundaDecisionRef());
                } else if (element instanceof ScriptTask) {
                    ScriptTask scriptTask = (ScriptTask) element;
                    System.out.format("%-40s %-20s %-50s %-30s %-50s\n", scriptTask.getId(), scriptTask.getElementType().getTypeName(), scriptTask.getName(), getImplementationScriptTask(scriptTask), scriptTask.getCamundaResource());
                } else if (element instanceof CallActivity) {
                    CallActivity callActivity = (CallActivity) element;
                    System.out.format("%-40s %-20s %-50s %-30s %-50s\n", callActivity.getId(), callActivity.getElementType().getTypeName(), callActivity.getName(), getCalledElementType(callActivity), callActivity.getCalledElement());
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

    private static String getImplementationType(ServiceTask serviceTask) {
        if (serviceTask.getCamundaClass() != null) {
            //System.out.println(serviceTask.getCamundaClass());
            return "class";
        } else if (serviceTask.getCamundaDelegateExpression() != null) {
            return "delegateExpression";
        } else if (serviceTask.getCamundaExpression() != null) {
            return "expression";
        } else {
            return "unknown";
        }
    }

    private static String getImplementationSendTask(SendTask sendTask) {
        if (sendTask.getCamundaType() != null && sendTask.getCamundaType().equals("external")) {
            return "external";
        } else if (sendTask.getCamundaClass() != null) {
            return "java class";
        } else if (sendTask.getCamundaExpression() != null) {
            return "expression";
        } else if (sendTask.getCamundaDelegateExpression() != null) {
            return "delegate expression";
        } else if (sendTask.getExtensionElements().getElementsQuery().filterByType(CamundaConnector.class).count() > 0) {
            return "connector";
        } else {
            return "unknown";
        }
    }

    private static String getImplementationBusinessRuleTask(BusinessRuleTask businessRuleTask) {
        String decisionRef = businessRuleTask.getCamundaDecisionRef();
        String taskType = businessRuleTask.getCamundaType();

        if (decisionRef != null) {
            return "DMN";
        } else if ("external".equals(taskType)) {
            return "external";
        } else if ("java class".equals(taskType)) {
            return "java class";
        } else if ("expression".equals(taskType)) {
            return "expression";
        } else if ("delegate expression".equals(taskType)) {
            return "delegate expression";
        } else if (businessRuleTask.getExtensionElements().getElementsQuery().filterByType(CamundaConnector.class).count() > 0) {
            return "connector";
        } else {
            return "none";
        }
    }

    private static String getImplementationScriptTask(ScriptTask scriptTask) {
        if (scriptTask.getCamundaResource() != null) {
            return "external resource";
        } else if (scriptTask.getCamundaResource() != null) {
            return "inline script";
        } else {
            return "none";
        }
    }

    private static String getCalledElementType(CallActivity callActivity) {
        String calledElement = callActivity.getCalledElement();
        if (calledElement == null) {
            return "none";
        } else {
            return "BPMN";
        }
    }

    public static String getFormType(UserTask userTask) {
        String formType = "Unknown";
        if (userTask.getCamundaFormRef() != null) {
            formType = "Camunda Form";
        } else if (userTask.getCamundaFormKey() != null) {
            formType = "Embedded or External Task Form";
        } else if (userTask.getExtensionElements().getElementsQuery().count() > 0) {
            formType = "Generated Task Form";
        }
        return formType;
    }

}
