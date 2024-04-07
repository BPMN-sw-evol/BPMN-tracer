package com.msgfoundation.xmltracer;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.Collection;

import org.camunda.bpm.model.bpmn.instance.ExtensionElements;
import org.camunda.bpm.model.bpmn.instance.SendTask;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaConnector;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaInputOutput;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaInputParameter;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaOutputParameter;

class SendTaskDetails {
    public static JsonObject getSendTaskDetails(SendTask sendTask) {
        JsonObject sendTaskDetails = new JsonObject();

        sendTaskDetails.addProperty("taskID", sendTask.getId());
        sendTaskDetails.addProperty("taskName", sendTask.getName());
        sendTaskDetails.addProperty("taskType", "Send Task");

        String implementation = determineSendTaskImplementation(sendTask);
        addTaskImplementationDetails(sendTaskDetails, implementation, sendTask);

        // Agregar los inputs como variables si existen
        addTaskInputsAndOutputsAsVariables(sendTaskDetails, sendTask);

        return sendTaskDetails;
    }

    private static void addTaskImplementationDetails(JsonObject jsonObject, String taskType, SendTask sendTask) {
        jsonObject.addProperty("taskImplementationType", taskType);
        String implementation = getSendTaskDetails(sendTask, taskType);
        if (!"".equals(implementation)) {
            jsonObject.addProperty("taskReferenceOrImplementation", implementation);
        }
    }

    private static String determineSendTaskImplementation(SendTask sendTask) {
        return sendTask.getCamundaTopic() != null ? "External"
                : sendTask.getCamundaClass() != null ? "Java Class"
                        : (sendTask.getCamundaExpression() != null || sendTask.getCamundaResultVariable() != null)
                                ? "Expression"
                                : sendTask.getCamundaDelegateExpression() != null ? "Delegate Expression"
                                        : (sendTask.getExtensionElements() != null
                                                && sendTask.getExtensionElements().getElementsQuery()
                                                        .filterByType(CamundaConnector.class).count() > 0) ? "Connector"
                                                                : "None";
    }

    private static String getSendTaskDetails(SendTask sendTask, String implementation) {
        if ("External".equals(implementation)) {
            return sendTask.getCamundaTopic();
        } else if ("Java Class".equals(implementation)) {
            return sendTask.getCamundaClass();
        } else if ("Expression".equals(implementation)) {
            return sendTask.getCamundaExpression() + " y " + sendTask.getCamundaResultVariable();
        } else if ("Delegate Expression".equals(implementation)) {
            return sendTask.getCamundaDelegateExpression();
        } else if ("Connector".equals(implementation)) {
            return sendTask.getExtensionElements().getElementsQuery()
                    .filterByType(CamundaConnector.class).singleResult().getCamundaConnectorId().getTextContent();
        } else if ("None".equals(implementation)) {
            return "None";
        }
        return "";
    }

    private static void addTaskInputsAndOutputsAsVariables(JsonObject jsonObject, SendTask sendTask) {
        ExtensionElements extensionElements = sendTask.getExtensionElements();
        if (extensionElements != null) {
            Collection<CamundaInputOutput> inputOutputs = extensionElements
                    .getChildElementsByType(CamundaInputOutput.class);

            if (!inputOutputs.isEmpty()) {
                JsonArray variablesArray = new JsonArray();
                for (CamundaInputOutput inputOutput : inputOutputs) {
                    for (CamundaInputParameter inputParameter : inputOutput.getCamundaInputParameters()) {
                        variablesArray.add(inputParameter.getCamundaName());
                    }
                    for (CamundaOutputParameter outputParameter : inputOutput.getCamundaOutputParameters()) {
                        variablesArray.add(outputParameter.getCamundaName());
                    }
                }
                jsonObject.add("variables", variablesArray);

            }
        }
    }
}
