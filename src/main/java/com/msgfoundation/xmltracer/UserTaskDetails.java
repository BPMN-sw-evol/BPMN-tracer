package com.msgfoundation.xmltracer;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.camunda.bpm.model.bpmn.instance.ExtensionElements;
import org.camunda.bpm.model.bpmn.instance.UserTask;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaFormData;

public class UserTaskDetails {

    public static JsonObject getUserTaskDetails(UserTask userTask) {
        JsonObject userTaskDetails = new JsonObject();

        userTaskDetails.addProperty("ID", userTask.getId());
        userTaskDetails.addProperty("Name", userTask.getName());
        userTaskDetails.addProperty("Type", "User Task");
        userTaskDetails.addProperty("Assignee", userTask.getCamundaAssignee());

        String userTaskLink = determineUserTaskImplementation(userTask);
        addTaskImplementationDetails(userTaskDetails, userTaskLink, userTask);

        if ("Generated Task Form".equals(userTaskLink)) {
            addFormFields(userTaskDetails, userTask);
        }
        
        return userTaskDetails;
    }

    private static String determineUserTaskImplementation(UserTask userTask) {
        return userTask.getCamundaFormKey() != null ? "Embedded or External Task Form"
                : userTask.getCamundaFormRef() != null ? "Camunda Form"
                : hasGeneratedTaskForm(userTask) ? "Generated Task Form" : "None";
    }

    private static void addTaskImplementationDetails(JsonObject jsonObject, String taskType, UserTask userTask) {
        jsonObject.addProperty("TypeImplementation", taskType);
        if (!"None".equals(taskType)) {
            String implementation = getUserTaskLinkValue(taskType, userTask);
            if (implementation != null) {
                jsonObject.addProperty("Implementation", implementation);
            }
        }
    }

    private static String getUserTaskLinkValue(String userTaskLink, UserTask userTask) {
        if ("Embedded or External Task Form".equals(userTaskLink)) {
            return userTask.getCamundaFormKey();
        } else if ("Camunda Form".equals(userTaskLink)) {
            return userTask.getCamundaFormRef();
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
        jsonObject.add("Form Fields", formFieldsArray);
    }
}
