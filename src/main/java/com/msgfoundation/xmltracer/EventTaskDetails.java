package com.msgfoundation.xmltracer;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.camunda.bpm.model.bpmn.instance.ExtensionElements;
import org.camunda.bpm.model.bpmn.instance.StartEvent;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaFormData;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaFormField;

public class EventTaskDetails {

    public static JsonObject getStartEventDetails(StartEvent startEvent) {
        JsonObject eventDetails = new JsonObject();
        eventDetails.addProperty("ID", startEvent.getId());
        eventDetails.addProperty("Name", startEvent.getName());
        eventDetails.addProperty("Type", "Start Event");

        String startEventLink = determineStartEventImplementation(startEvent);
        addTaskImplementationDetails(eventDetails, startEventLink, startEvent);

        if ("Generated Task Form".equals(eventDetails.get("TypeImplementation").getAsString())) {
            eventDetails.add("Form Fields", getFormFields(startEvent));
        }

        return eventDetails;
    }

    private static String determineStartEventImplementation(StartEvent startEvent) {
        return startEvent.getCamundaFormKey() != null ? "Embedded or External Task Form"
                : startEvent.getCamundaFormRef() != null ? "Camunda Form"
                        : hasGeneratedTaskForm(startEvent) ? "Generated Task Form"
                                : "None";
    }

    private static void addTaskImplementationDetails(JsonObject jsonObject, String taskType, StartEvent startEvent) {
        jsonObject.addProperty("TypeImplementation", taskType);
        String implementation = getStartEventLinkValue(taskType, startEvent);
        if (implementation != null) {
            jsonObject.addProperty("Implementation", implementation);
        }
    }

    private static String getStartEventLinkValue(String startEventLink, StartEvent startEvent) {
        if ("Embedded or External Task Form".equals(startEventLink)) {
            return startEvent.getCamundaFormKey();
        } else if ("Camunda Form".equals(startEventLink)) {
            return startEvent.getCamundaFormRef();
        }
        return null;
    }

    private static boolean hasGeneratedTaskForm(StartEvent startEvent) {
        ExtensionElements extensionElements = startEvent.getExtensionElements();
        return extensionElements != null && extensionElements.getElementsQuery()
                .filterByType(CamundaFormData.class).count() > 0;
    }

    private static JsonArray getFormFields(StartEvent startEvent) {
        JsonArray formFields = new JsonArray();
        CamundaFormData formData = startEvent.getExtensionElements().getElementsQuery()
                .filterByType(CamundaFormData.class).singleResult();
        if (formData != null) {
            for (CamundaFormField field : formData.getCamundaFormFields()) {
                formFields.add(field.getCamundaId());
            }
        }
        return formFields;
    }
}
