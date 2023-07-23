package com.mycompany.tracerxml;

import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.util.Collection;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.Task;
import org.camunda.bpm.model.bpmn.instance.CallActivity;

public class TracerXML {

    public static void main(String[] args) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select a BPMN file");
        fileChooser.setFileFilter(new FileNameExtensionFilter("BPMN files (*.bpmn)", "bpmn"));

        int result = fileChooser.showOpenDialog(null);

        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();

            BpmnModelInstance modelInstance = Bpmn.readModelFromFile(file);

            Collection<Task> tasks = modelInstance.getModelElementsByType(Task.class);
            Collection<CallActivity> callActivities = modelInstance.getModelElementsByType(CallActivity.class);

            System.out.println("Task name \t| Task type \t| Implementation Type \t| Implementation Reference");
            System.out.println("-------------------------------------------------------------");
            for (Task task : tasks) {
                System.out.println(task.getName() + "\t\t| " + task.getElementType().getTypeName());
            }

            System.out.println("\nCall Activity name \t| Called Element");
            System.out.println("-------------------------------------");
            for (CallActivity callActivity : callActivities) {
                System.out.println(callActivity.getName() + "\t\t| " + callActivity.getCalledElement());
            }
        } else {
            System.out.println("No file selected.");
        }
    }
}
