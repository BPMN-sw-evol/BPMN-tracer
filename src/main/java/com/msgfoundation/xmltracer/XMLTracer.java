package com.msgfoundation.xmltracer;

import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.FlowElement;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.camunda.bpm.model.bpmn.instance.Lane;

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

            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
            ArrayNode jsonArray = JsonNodeFactory.instance.arrayNode();

            MyUserTask userTask = new MyUserTask();
            MyServiceTask serviceTask = new MyServiceTask();
            MySendTask sendTask = new MySendTask();
            MyReceiveTask receiveTask = new MyReceiveTask();
            MyBusinessTask businessRuleTask = new MyBusinessTask();
            MyScriptTask scriptTask = new MyScriptTask();
            MyCallActivity callActivity = new MyCallActivity();
            MyDefaultTask defaultTask = new MyDefaultTask();

            for (FlowElement element : flowElements) {
                ObjectNode jsonNode = objectMapper.createObjectNode();
                if (userTask.checkTaskType(element)) {
                    userTask.processElement(element);
                    jsonNode.put("taskID", userTask.getTaskID());
                    jsonNode.put("taskName",userTask.getTaskName());
                    jsonNode.put("taskType", userTask.getTaskType());
                    jsonNode.put("taskImplementationType", userTask.getTaskImplementationType());
                    jsonNode.put("taskReferenceOrImplementation", userTask.getReferenceOrImplementation());
                    jsonNode.put("assignee",userTask.getAssignee());
                    ArrayNode variablesArray = objectMapper.createArrayNode();
                    List<String> definedVariables = userTask.getDefinedVariables();
                    for (String variable : definedVariables) {
                        variablesArray.add(variable);
                    }
                    jsonNode.set("variables", variablesArray);
                    jsonArray.add(jsonNode);
                } else if (serviceTask.checkTaskType(element)) {
                    serviceTask.processElement(element);
                    jsonNode.put("taskID", serviceTask.getTaskID());
                    jsonNode.put("taskName",serviceTask.getTaskName());
                    jsonNode.put("taskType", serviceTask.getTaskType());
                    jsonNode.put("taskImplementationType", serviceTask.getTaskImplementationType());
                    jsonNode.put("taskReferenceOrImplementation", serviceTask.getReferenceOrImplementation());
                    jsonArray.add(jsonNode);
                } else if (sendTask.checkTaskType(element)) {
                    sendTask.processElement(element);
                    jsonNode.put("taskID", sendTask.getTaskID());
                    jsonNode.put("taskName", sendTask.getTaskName());
                    jsonNode.put("taskType", sendTask.getTaskType());
                    jsonNode.put("taskImplementationType", sendTask.getTaskImplementationType());
                    jsonNode.put("taskReferenceOrImplementation", sendTask.getReferenceOrImplementation());
                    jsonArray.add(jsonNode);
                } else if (receiveTask.checkTaskType(element)) {
                    receiveTask.processElement(element);
                    jsonNode.put("taskID", receiveTask.getTaskID());
                    jsonNode.put("taskName", receiveTask.getTaskName());
                    jsonNode.put("taskType", receiveTask.getTaskType());
                    jsonNode.put("taskImplementationType", receiveTask.getTaskImplementationType());
                    jsonNode.put("taskReferenceOrImplementation", receiveTask.getReferenceOrImplementation());
                    jsonArray.add(jsonNode);
                } else if (businessRuleTask.checkTaskType(element)) {
                    businessRuleTask.processElement(element);
                    jsonNode.put("taskID", businessRuleTask.getTaskID());
                    jsonNode.put("taskName", businessRuleTask.getTaskName());
                    jsonNode.put("taskType", businessRuleTask.getTaskType());
                    jsonNode.put("taskImplementationType", businessRuleTask.getTaskImplementationType());
                    jsonNode.put("taskReferenceOrImplementation", businessRuleTask.getReferenceOrImplementation());
                    jsonArray.add(jsonNode);
                } else if (scriptTask.checkTaskType(element)) {
                    scriptTask.processElement(element);
                    jsonNode.put("taskID", scriptTask.getTaskID());
                    jsonNode.put("taskName", scriptTask.getTaskName());
                    jsonNode.put("taskType", scriptTask.getTaskType());
                    jsonNode.put("taskImplementationType", scriptTask.getTaskImplementationType());
                    jsonNode.put("taskReferenceOrImplementation", scriptTask.getReferenceOrImplementation());
                    jsonArray.add(jsonNode);
                } else if (callActivity.checkTaskType(element)) {
                    callActivity.processElement(element);
                    jsonNode.put("taskID", callActivity.getTaskID());
                    jsonNode.put("taskName", callActivity.getTaskName());
                    jsonNode.put("taskType", callActivity.getTaskType());
                    jsonNode.put("taskImplementationType", callActivity.getTaskImplementationType());
                    jsonNode.put("taskReferenceOrImplementation", callActivity.getReferenceOrImplementation());
                    jsonArray.add(jsonNode);
                } else if (defaultTask.checkTaskType(element)) {
                    defaultTask.processElement(element);
                    jsonNode.put("taskID", defaultTask.getTaskID());
                    jsonNode.put("taskName", defaultTask.getTaskName());
                    jsonNode.put("taskType", defaultTask.getTaskType());
                    jsonNode.put("taskImplementationType", defaultTask.getTaskImplementationType());
                    jsonNode.put("taskReferenceOrImplementation", defaultTask.getReferenceOrImplementation());
                    jsonArray.add(jsonNode);
                }
            }

            try {
                // Convertir el ArrayNode a formato JSON y imprimirlo
                String jsonString = objectMapper.writeValueAsString(jsonArray);
                System.out.println(jsonString);

                // Obtener el nombre del archivo sin extensión
                String fileNameWithoutExtension = file.getName().replaceFirst("[.][^.]+$", "");

                // Crear una carpeta 'output' si no existe
                File outputFolder = new File("output");
                if (!outputFolder.exists()) {
                    outputFolder.mkdir();
                }

                // Construir la ruta del archivo JSON con marca de tiempo
                String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                String outputPath = "output/" + fileNameWithoutExtension + "_" + timestamp + ".json";

                // Guardar el JSON en un archivo con el nombre del modelo BPMN y marca de tiempo
                objectMapper.writeValue(new File(outputPath), jsonArray);
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else {
            System.out.println("No file selected.");
        }
    }
}
