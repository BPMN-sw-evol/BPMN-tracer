package com.msgfoundation.xmltracer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.EndEvent;
import org.camunda.bpm.model.bpmn.instance.FlowNode;
import org.camunda.bpm.model.bpmn.instance.Gateway;
import org.camunda.bpm.model.bpmn.instance.IntermediateCatchEvent;
import org.camunda.bpm.model.bpmn.instance.Participant;
import org.camunda.bpm.model.bpmn.instance.SendTask;
import org.camunda.bpm.model.bpmn.instance.SequenceFlow;
import org.camunda.bpm.model.bpmn.instance.ServiceTask;
import org.camunda.bpm.model.bpmn.instance.StartEvent;
import org.camunda.bpm.model.bpmn.instance.UserTask;

public class XMLTracer {

    // Fuera del método principal, puedes declarar un contador global
    private static BpmnModelInstance modelInstance = null;

    public static void main(String[] args) {
        JsonObject bpmnDetails = new JsonObject();
        String bpmnFilePath = "";

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("Archivos BPMN", "bpmn"));

        int result = fileChooser.showOpenDialog(null);

        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            bpmnFilePath = selectedFile.getAbsolutePath();
        } else {
            JOptionPane.showMessageDialog(null, "Operación cancelada por el usuario, modelo BPMN no analizado.",
                    "Error", JOptionPane.INFORMATION_MESSAGE);
        }

        modelInstance = Bpmn.readModelFromFile(new File(bpmnFilePath));
        File file = new File(bpmnFilePath);
        String fileNameWithoutExtension = file.getName().substring(0, file.getName().lastIndexOf('.'));

        Collection<StartEvent> startEvents = modelInstance.getModelElementsByType(StartEvent.class);
        for (StartEvent startEvent : startEvents) {
            Set<String> visitedNodes = new HashSet<>();
            listActivitiesFromStartEvent(modelInstance, startEvent, visitedNodes, bpmnDetails);
        }

        JsonObject bpmnWithBpmName = new JsonObject();
        bpmnWithBpmName.addProperty("bpmPath", bpmnFilePath);
        bpmnWithBpmName.addProperty("bpmNameFile", file.getName());
        bpmnWithBpmName.addProperty("bpmNameProcess", getParticipant(modelInstance));
        bpmnWithBpmName.add("trace", bpmnDetails.getAsJsonArray("trace"));

        // Guardar el JSON con el campo "bpmName"
        saveJsonToFile(fileNameWithoutExtension, formatJson(bpmnWithBpmName.toString()));
    }

    public static void listActivitiesFromStartEvent(BpmnModelInstance modelInstance, FlowNode currentNode,
            Set<String> visitedNodes, JsonObject bpmnDetails) {

        // Obtener el array JSON existente o crear uno nuevo si no existe
        JsonArray traceArray = bpmnDetails.has("trace") ? bpmnDetails.getAsJsonArray("trace") : new JsonArray();

        if (visitedNodes.contains(currentNode.getId())) {
            return;
        }

        visitedNodes.add(currentNode.getId());
        if (currentNode.getName() != null && !currentNode.getName().isEmpty()) {
            printElementDetails(currentNode, bpmnDetails, traceArray);
        }

        Collection<SequenceFlow> outgoingFlows = currentNode.getOutgoing();
        // Antes de comenzar a procesar los SequenceFlows, crea un conjunto para
        // realizar un seguimiento de los IDs ya procesados
        Set<String> sequenceFlowIdsProcesados = new HashSet<>();

        for (SequenceFlow sequenceFlow : outgoingFlows) {
            FlowNode targetNode = sequenceFlow.getTarget();
            if (sequenceFlow.getConditionExpression() != null
                    && !sequenceFlowIdsProcesados.contains(sequenceFlow.getId())) {
                sequenceFlowIdsProcesados.add(sequenceFlow.getId());
                JsonObject sequenceFlowDetails = FlowSequenceDetails.processSequenceFlow(sequenceFlow);
                if (!traceArray.contains(sequenceFlowDetails)) {
                    traceArray.add(sequenceFlowDetails);
                }
            }

            // Imprimir la información de la compuerta
            // if (currentNode instanceof Gateway) {
            // System.out.println("Flujo desde la compuerta: " + currentNode.getName());
            // }
            // Si la actividad actual no ha sido visitada, seguir recursivamente
            if (!visitedNodes.contains(targetNode.getId())) {
                listActivitiesFromStartEvent(modelInstance, targetNode, visitedNodes, bpmnDetails);
            }
        }
    }

    public static <ExtensionElement> void printElementDetails(FlowNode flowNode, JsonObject bpmnDetails, JsonArray traceArray) {
        JsonObject taskDetails = new JsonObject();

        if (flowNode instanceof StartEvent) {
            taskDetails = EventTaskDetails.getStartEventDetails((StartEvent) flowNode);
        } else if (flowNode instanceof UserTask) {
            taskDetails = UserTaskDetails.getUserTaskDetails((UserTask) flowNode);
        } else if (flowNode instanceof Gateway && flowNode.getName() != null && !flowNode.getName().isEmpty()) {
            taskDetails.addProperty("taskID", flowNode.getId());
            taskDetails.addProperty("taskName", flowNode.getName());
            taskDetails.addProperty("taskType", "Gateway");
            taskDetails.addProperty("taskImplementationType", "None");
            taskDetails.addProperty("taskReferenceOrImplementation", "None");
        } else if (flowNode instanceof ServiceTask) {
            taskDetails = ServiceTaskDetails.getServiceTaskDetails((ServiceTask) flowNode);
        } else if (flowNode instanceof SendTask) {
            taskDetails = SendTaskDetails.getSendTaskDetails((SendTask) flowNode);
        } else if (flowNode instanceof IntermediateCatchEvent) {
            IntermediateCatchEvent intermediateEvent = (IntermediateCatchEvent) flowNode;
            if (intermediateEvent.getEventDefinitions() != null
                    && !intermediateEvent.getEventDefinitions().isEmpty()) {
                taskDetails = EventTaskDetails.determineTypeIntermediateEvent(intermediateEvent);
            }
        } else if (flowNode instanceof EndEvent) {
            taskDetails = EventTaskDetails.getEndEventDetails((EndEvent) flowNode);
        }

        // Agregar los detalles de la tarea al array
        traceArray.add(taskDetails);

        // Colocar el array de traza actualizado en bpmnDetails
        bpmnDetails.add("trace", traceArray);
    }

    public static String getParticipant(BpmnModelInstance modelInstance) {
        String processName = "";
        Collection<Participant> participants = modelInstance.getModelElementsByType(Participant.class);
        for (Participant participant : participants) {
            processName = participant.getName();
        }
        return processName;
    }

    public static String formatJson(String jsonString) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(gson.fromJson(jsonString, Object.class));
    }

    public static void saveJsonToFile(String fileName, String json) {
        try {
            // Obtener la ruta del directorio actual del proyecto
            String currentDirectory = System.getProperty("user.dir");

            // Crear el directorio "output" si no existe
            String outputDirectory = currentDirectory + File.separator + "output";
            Path outputPath = Paths.get(outputDirectory);
            if (!Files.exists(outputPath)) {
                Files.createDirectories(outputPath);
                System.out.println("Directorio 'output' creado en: " + outputPath);
            }

            // Crear un FileWriter en el directorio "output" con el nombre del archivo
            // original y extensión .json
            String filePath = outputDirectory + File.separator + fileName + ".json";
            try (FileWriter fileWriter = new FileWriter(filePath)) {
                fileWriter.write(json);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
