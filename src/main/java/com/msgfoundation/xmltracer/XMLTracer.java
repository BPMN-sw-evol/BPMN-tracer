package com.msgfoundation.xmltracer;

import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.util.Collection;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.FlowElement;

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

            MyUserTask userTask = new MyUserTask();
            MyServiceTask serviceTask = new MyServiceTask();
            MySendTask sendTask = new MySendTask();
            MyReceiveTask receiveTask = new MyReceiveTask();
            MyBusinessTask businessRuleTask = new MyBusinessTask();
            MyScriptTask scriptTask = new MyScriptTask();
            MyCallActivity callActivity = new MyCallActivity();
            MyDefaultTask defaultTask = new MyDefaultTask();

            for (FlowElement element : flowElements) {
                if (userTask.checkTaskType(element)) {
                    userTask.processElement(element);
                    System.out.print(userTask.toString());
                } else if (serviceTask.checkTaskType(element)) {
                    serviceTask.processElement(element);
                    System.out.print(serviceTask.toString());
                } else if (sendTask.checkTaskType(element)) {
                    sendTask.processElement(element);
                    System.out.print(sendTask.toString());
                } else if (receiveTask.checkTaskType(element)) {
                    receiveTask.processElement(element);
                    System.out.print(receiveTask.toString());
                } else if (businessRuleTask.checkTaskType(element)) {
                    businessRuleTask.processElement(element);
                    System.out.print(businessRuleTask.toString());
                } else if (scriptTask.checkTaskType(element)) {
                    scriptTask.checkTaskType(element);
                    scriptTask.processElement(element);
                    System.out.print(scriptTask.toString());
                } else if (callActivity.checkTaskType(element)) {
                    callActivity.processElement(element);
                    System.out.print(callActivity.toString());
                } else if (defaultTask.checkTaskType(element)) {
                    defaultTask.processElement(element);
                    System.out.print(defaultTask.toString());
                } else if (defaultTask.checkTaskType(element)) {
                    defaultTask.processElement(element);
                    System.out.print(defaultTask.toString());
                }

            }
        } else {
            System.out.println("No file selected.");
        }
    }
}
