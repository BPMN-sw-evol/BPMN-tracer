package com.msgfoundation.xmltracer;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.Collection;

import org.camunda.bpm.model.bpmn.instance.ExtensionElements;
import org.camunda.bpm.model.bpmn.instance.UserTask;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaFormData;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaInputOutput;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaInputParameter;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaOutputParameter;

public class UserTaskDetails {

    public static JsonObject getUserTaskDetails(UserTask userTask) {
        JsonObject userTaskDetails = new JsonObject();

        userTaskDetails.addProperty("taskID", userTask.getId());
        userTaskDetails.addProperty("taskName", userTask.getName());
        userTaskDetails.addProperty("taskType", "User Task");

        String userTaskLink = determineUserTaskImplementation(userTask);
        addTaskImplementationDetails(userTaskDetails, userTaskLink, userTask);

        if ("Generated Task Form".equals(userTaskLink)) {
            addFormFields(userTaskDetails, userTask);
        } else if ("Camunda Form".equals(userTaskLink) || "Embedded or External Task Form".equals(userTaskLink)) {
            addTaskInputsAndOutputsAsVariables(userTaskDetails, userTask);
        }

        // userTaskDetails.addProperty("Assignee", userTask.getCamundaAssignee());

        return userTaskDetails;
    }

    private static String determineUserTaskImplementation(UserTask userTask) {
        return userTask.getCamundaFormKey() != null ? "Embedded or External Task Form"
                : userTask.getCamundaFormRef() != null ? "Camunda Form"
                        : hasGeneratedTaskForm(userTask) ? "Generated Task Form" : "None";
    }

    private static void addTaskImplementationDetails(JsonObject jsonObject, String taskType, UserTask userTask) {
        jsonObject.addProperty("taskImplementationType", taskType);
        if (!"None".equals(taskType)) {
            String implementation = getUserTaskLinkValue(taskType, userTask);
            if (implementation != null) {
                jsonObject.addProperty("taskReferenceOrImplementation", implementation);
            }
        }
    }

    private static String getUserTaskLinkValue(String userTaskLink, UserTask userTask) {
        if ("Embedded or External Task Form".equals(userTaskLink)) {
            return userTask.getCamundaFormKey();
        } else if ("Camunda Form".equals(userTaskLink)) {
            return userTask.getCamundaFormRef();
        } else if ("Generated Task Form".equals(userTaskLink)) {
            return "Have a Fields Form";
        } else if ("None".equals(userTaskLink)) {
            return "None";
        }
        return null;
    }

    private static boolean hasGeneratedTaskForm(UserTask userTask) {
        ExtensionElements extensionElements = userTask.getExtensionElements();
        return extensionElements != null && extensionElements.getElementsQuery()
                .filterByType(CamundaFormData.class).count() > 0;
    }

    private static void addFormFields(JsonObject jsonObject, UserTask userTask) {
        CamundaFormData formData = userTask.getExtensionElements().getElementsQuery()
                .filterByType(CamundaFormData.class).singleResult();
        JsonArray formFieldsArray = new JsonArray();
        if (formData != null) {
            formData.getCamundaFormFields().forEach(field -> formFieldsArray.add(field.getCamundaId()));
        }
        jsonObject.add("variables", formFieldsArray);
    }

    private static void addTaskInputsAndOutputsAsVariables(JsonObject jsonObject, UserTask userTask) {
        ExtensionElements extensionElements = userTask.getExtensionElements();
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
