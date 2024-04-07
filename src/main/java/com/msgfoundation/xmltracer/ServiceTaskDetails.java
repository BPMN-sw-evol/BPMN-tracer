package com.msgfoundation.xmltracer;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.Collection;

import org.camunda.bpm.model.bpmn.instance.ExtensionElements;
import org.camunda.bpm.model.bpmn.instance.ServiceTask;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaConnector;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaInputOutput;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaInputParameter;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaOutputParameter;

public class ServiceTaskDetails {

    public static JsonObject getServiceTaskDetails(ServiceTask serviceTask) {
        JsonObject serviceTaskDetails = new JsonObject();

        serviceTaskDetails.addProperty("taskID", serviceTask.getId());
        serviceTaskDetails.addProperty("taskName", serviceTask.getName());
        serviceTaskDetails.addProperty("taskType", "Service Task");

        String implementation = determineServiceTaskImplementation(serviceTask);
        addTaskImplementationDetails(serviceTaskDetails, implementation, serviceTask);

        // Agregar los inputs como variables si existen
        addTaskInputsAndOutputsAsVariables(serviceTaskDetails, serviceTask);

        return serviceTaskDetails;
    }

    private static void addTaskImplementationDetails(JsonObject jsonObject, String taskType, ServiceTask serviceTask) {
        jsonObject.addProperty("taskImplementationType", taskType);
        String implementation = getServiceTaskDetails(serviceTask, taskType);
        if (!"".equals(implementation)) {
            jsonObject.addProperty("taskReferenceOrImplementation", implementation);
        }
    }

    private static String determineServiceTaskImplementation(ServiceTask serviceTask) {
        return serviceTask.getCamundaTopic() != null ? "External"
                : serviceTask.getCamundaClass() != null ? "Java Class"
                        : (serviceTask.getCamundaExpression() != null || serviceTask.getCamundaResultVariable() != null)
                                ? "Expression"
                                : serviceTask.getCamundaDelegateExpression() != null ? "Delegate Expression"
                                        : (serviceTask.getExtensionElements() != null
                                                && serviceTask.getExtensionElements().getElementsQuery()
                                                        .filterByType(CamundaConnector.class).count() > 0) ? "Connector"
                                                                : "None";
    }

    private static String getServiceTaskDetails(ServiceTask serviceTask, String implementation) {
        if ("External".equals(implementation)) {
            return serviceTask.getCamundaTopic();
        } else if ("Java Class".equals(implementation)) {
            return serviceTask.getCamundaClass();
        } else if ("Expression".equals(implementation)) {
            return /* serviceTask.getCamundaExpression() + " y " + */ serviceTask.getCamundaResultVariable();
        } else if ("Delegate Expression".equals(implementation)) {
            return serviceTask.getCamundaDelegateExpression();
        } else if ("Connector".equals(implementation)) {
            return serviceTask.getExtensionElements().getElementsQuery()
                    .filterByType(CamundaConnector.class).singleResult().getCamundaConnectorId().getTextContent();
        } else if ("None".equals(implementation)) {
            return "None";
        }
        return "";
    }

    private static void addTaskInputsAndOutputsAsVariables(JsonObject jsonObject, ServiceTask serviceTask) {
        ExtensionElements extensionElements = serviceTask.getExtensionElements();
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
