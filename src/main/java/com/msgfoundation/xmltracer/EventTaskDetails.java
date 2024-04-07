package com.msgfoundation.xmltracer;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.Collection;

import org.camunda.bpm.model.bpmn.instance.EndEvent;
import org.camunda.bpm.model.bpmn.instance.EventDefinition;
import org.camunda.bpm.model.bpmn.instance.ExtensionElements;
import org.camunda.bpm.model.bpmn.instance.IntermediateCatchEvent;
import org.camunda.bpm.model.bpmn.instance.MessageEventDefinition;
import org.camunda.bpm.model.bpmn.instance.StartEvent;
import org.camunda.bpm.model.bpmn.instance.TimerEventDefinition;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaFormData;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaFormField;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaInputOutput;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaInputParameter;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaOutputParameter;

public class EventTaskDetails {

    public static JsonObject getStartEventDetails(StartEvent startEvent) {
        JsonObject eventDetails = new JsonObject();
        eventDetails.addProperty("taskID", startEvent.getId());
        eventDetails.addProperty("taskName", startEvent.getName());
        eventDetails.addProperty("taskType", "Start Event");

        String userTaskLink = determineStartEventImplementation(startEvent);
        addTaskImplementationDetails(eventDetails, userTaskLink, startEvent);

        if ("Generated Task Form".equals(eventDetails.get("taskImplementationType").getAsString())) {
            eventDetails.add("Form Fields", getFormFields(startEvent));
        }

        return eventDetails;
    }

    public static String determineStartEventImplementation(StartEvent startEvent) {
        return startEvent.getCamundaFormKey() != null ? "Embedded or External Task Form"
                : startEvent.getCamundaFormRef() != null ? "Camunda Form"
                        : hasGeneratedTaskForm(startEvent) ? "Generated Task Form"
                                : "None";
    }

    private static void addTaskImplementationDetails(JsonObject jsonObject, String taskType, StartEvent startEvent) {
        jsonObject.addProperty("taskImplementationType", taskType);
        String implementation = getStartEventLinkValue(taskType, startEvent);
        if (implementation != null) {
            jsonObject.addProperty("taskReferenceOrImplementation", implementation);
        }
    }

    private static String getStartEventLinkValue(String startEventLink, StartEvent startEvent) {
        if ("Embedded or External Task Form".equals(startEventLink)) {
            return startEvent.getCamundaFormKey();
        } else if ("Camunda Form".equals(startEventLink)) {
            return startEvent.getCamundaFormRef();
        } else if ("Generated Task Form".equals(startEventLink)) {
            return "Have a Fields Form";
        } else if ("None".equals(startEventLink)) {
            return "None";
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

    public static JsonObject getEndEventDetails(EndEvent endEvent) {
        JsonObject eventDetails = new JsonObject();
        eventDetails.addProperty("taskID", endEvent.getId());
        eventDetails.addProperty("taskName", endEvent.getName());
        eventDetails.addProperty("taskType", "End Event");
        eventDetails.addProperty("taskImplementationType", "None");
        eventDetails.addProperty("taskReferenceOrImplementation", "None");
        return eventDetails;
    }

    public static JsonObject determineTypeIntermediateEvent(IntermediateCatchEvent intermediateEvent) {
        JsonObject eventDetails = new JsonObject();
        EventDefinition eventDefinition = intermediateEvent.getEventDefinitions().iterator().next();

        eventDetails.addProperty("taskID", intermediateEvent.getId());
        eventDetails.addProperty("taskName", intermediateEvent.getName());
        if (eventDefinition instanceof TimerEventDefinition) {
            getTimerIntermediateEventDetails((EventDefinition) eventDefinition, eventDetails);
            addEventInputsAndOutputsAsVariables(eventDetails, intermediateEvent);
        } else if (eventDefinition instanceof MessageEventDefinition) {
            getMessageIntermediateEventDetails((EventDefinition) eventDefinition, eventDetails);
            addEventInputsAndOutputsAsVariables(eventDetails, intermediateEvent);
        }
        return eventDetails;
    }

    public static JsonObject getTimerIntermediateEventDetails(EventDefinition eventDefinition,
            JsonObject eventDetails) {
        TimerEventDefinition timerEvent = (TimerEventDefinition) eventDefinition;
        eventDetails.addProperty("taskType", "Timer Intermediate Event");
        if ((TimerEventDefinition) timerEvent.getTimeCycle() != null) {
            eventDetails.addProperty("taskImplementationType", "Cycle");
            eventDetails.addProperty("taskReferenceOrImplementation", timerEvent.getTimeCycle().getTextContent());
        } else if (timerEvent.getTimeDuration() != null) {
            eventDetails.addProperty("taskImplementationType", "Duration");
            eventDetails.addProperty("taskReferenceOrImplementation", timerEvent.getTimeDuration().getTextContent());
        } else if (timerEvent.getTimeDate() != null) {
            eventDetails.addProperty("taskImplementationType", "Date");
            eventDetails.addProperty("taskReferenceOrImplementation", timerEvent.getTimeDate().getTextContent());
        }

        return eventDetails;
    }

    public static JsonObject getMessageIntermediateEventDetails(EventDefinition eventDefinition,
            JsonObject eventDetails) {
        MessageEventDefinition messageEvent = (MessageEventDefinition) eventDefinition;
        eventDetails.addProperty("taskType", "Message Intermediate Event");
        eventDetails.addProperty("taskImplementationType", messageEvent.getMessage().getName());
        eventDetails.addProperty("taskReferenceOrImplementation", messageEvent.getMessage().getName());
        return eventDetails;
    }

     private static void addEventInputsAndOutputsAsVariables(JsonObject jsonObject, IntermediateCatchEvent intermediateEvent) {
        ExtensionElements extensionElements = intermediateEvent.getExtensionElements();
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
