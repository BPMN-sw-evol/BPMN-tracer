package com.msgfoundation.xmltracer;

import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.*;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class XMLTracer {

    // Fuera del método principal, puedes declarar un contador global
    private static int userTaskCount = 0;
    private static int GatewayCount = 0;
    private static int EndEventCount = 0;

    public static void main(String[] args) {

        // Cargar el modelo BPMN desde el archivo
        // BpmnModelInstance modelInstance = Bpmn.readModelFromFile(new
        // File("D:\\Universidad\\Software 1 - Profu\\SoftwareProject\\Process
        // Source\\Models\\EMSA-1.0.0.bpmn"));
        // BpmnModelInstance modelInstance = Bpmn.readModelFromFile(new
        // File("c:/Users/SOPORTES JPVM/Desktop/MSGF credit request.bpmn"));
        BpmnModelInstance modelInstance = Bpmn
                .readModelFromFile(new File("c:/Users/SOPORTES JPVM/Desktop/MSGF-Test.bpmn"));

        // JsonObject para almacenar detalles
        JsonObject bpmnDetails = new JsonObject();

        // Encontrar el evento de inicio
        Collection<StartEvent> startEvents = modelInstance.getModelElementsByType(StartEvent.class);

        for (StartEvent startEvent : startEvents) {
            Set<String> visitedNodes = new HashSet<>();
            listActivitiesFromStartEvent(modelInstance, startEvent, visitedNodes, bpmnDetails);
        }

        File file = new File("c:/Users/SOPORTES JPVM/Desktop/MSGF-Test.bpmn");
        String fileNameWithExtension = file.getName(); // Obtiene el nombre con la extensión
        String fileNameWithoutExtension = fileNameWithExtension.substring(0, fileNameWithExtension.lastIndexOf('.')); // Obtiene
                                                                                                                      // el
                                                                                                                      // nombre
                                                                                                                      // sin
                                                                                                                      // la
                                                                                                                      // extensión

        // Guardar el JSON en un archivo con el nombre del archivo original
        String formattedJson = formatJson(bpmnDetails.toString());
        saveJsonToFile(fileNameWithoutExtension, formattedJson);
    }

    public static void listActivitiesFromStartEvent(BpmnModelInstance modelInstance, FlowNode currentNode,
            Set<String> visitedNodes, JsonObject bpmnDetails) {
        if (visitedNodes.contains(currentNode.getId())) {
            return;
        }

        visitedNodes.add(currentNode.getId());
        printElementDetails(currentNode, bpmnDetails);
        Collection<SequenceFlow> outgoingFlows = currentNode.getOutgoing();

        for (SequenceFlow sequenceFlow : outgoingFlows) {
            FlowNode targetNode = sequenceFlow.getTarget();

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

    public static void printElementDetails(FlowNode flowNode, JsonObject bpmnDetails) {
        if (flowNode instanceof StartEvent) {
            StartEvent startEvent = (StartEvent) flowNode;
            bpmnDetails.add("StartEvent", EventTaskDetails.getStartEventDetails(startEvent));
        } else if (flowNode instanceof UserTask) {
            userTaskCount++;
            UserTask userTask = (UserTask) flowNode;
            bpmnDetails.add("UserTask" + userTaskCount, UserTaskDetails.getUserTaskDetails(userTask));
        } else if (flowNode instanceof Gateway && flowNode.getName() != null) {
            GatewayCount++;
            JsonObject gatewayDetails = new JsonObject();
            gatewayDetails.addProperty("ID", flowNode.getId());
            gatewayDetails.addProperty("Name", flowNode.getName());
            bpmnDetails.add("Gateway" + GatewayCount, gatewayDetails);
        } else if (flowNode instanceof ServiceTask) {
            ServiceTask serviceTask = (ServiceTask) flowNode;
            bpmnDetails.add("ServiceTask", ServiceTaskDetails.getServiceTaskDetails(serviceTask));
        } else if (flowNode instanceof SendTask) {
            SendTask sendTask = (SendTask) flowNode;
            bpmnDetails.add("SendTask", SendTaskDetails.getSendTaskDetails(sendTask));
        } else if (flowNode instanceof EndEvent) {
            EndEventCount++;
            JsonObject endEventDetails = new JsonObject();
            endEventDetails.addProperty("ID", flowNode.getId());
            endEventDetails.addProperty("Name", flowNode.getName());
            bpmnDetails.add("EndEvent" + EndEventCount, endEventDetails);
        }
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
            FileWriter fileWriter = new FileWriter(filePath);
            fileWriter.write(json);
            fileWriter.close();
            System.out.println("JSON guardado en el archivo: " + filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
