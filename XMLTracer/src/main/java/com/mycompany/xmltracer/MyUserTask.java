package com.mycompany.xmltracer;

import java.util.Collection;
import org.camunda.bpm.model.bpmn.instance.FlowElement;
import org.camunda.bpm.model.bpmn.instance.UserTask;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaFormData;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaFormField;

public class MyUserTask extends Activity {

    private UserTask userTask;

    @Override
    public String getTaskID() {
        return userTask.getId();
    }

    @Override
    public String getTaskType() {
        return userTask.getElementType().getTypeName();
    }

    @Override
    public String getTaskName() {
        return userTask.getName();
    }

    @Override
    public String getTaskImplementationType() {
        String formType = "Unknown";
        if (userTask.getCamundaFormRef() != null) {
            formType = "Camunda Form";
        } else if (userTask.getCamundaFormKey() != null) {
            formType = "Embedded or External Task Form";
        } else if (userTask.getExtensionElements().getElementsQuery().filterByType(CamundaFormData.class).count() > 0) {
            formType = "Generated Task Form";
        }
        return formType;
    }

    @Override
    public String getReferenceOrImplementation() {
        String userTaskLink = "NA";
        if (getTaskImplementationType().equals("Camunda Form")) {
            userTaskLink = userTask.getCamundaFormRef();
        } else if (getTaskImplementationType().equals("Embedded or External Task Form")) {
            userTaskLink = userTask.getCamundaFormKey();
        } else if (getTaskImplementationType().equals("Generated Task Form")) {
            userTaskLink = hasFormFields();
        }
        return userTaskLink;
    }

    @Override
    public boolean checkTaskType(FlowElement element) {
        return element instanceof UserTask;
    }

    @Override
    public void processElement(FlowElement element) {
        userTask = (UserTask) element;
    }

    public String hasFormFields() {
        CamundaFormData camundaFormData = userTask.getExtensionElements().getElementsQuery().filterByType(CamundaFormData.class).singleResult();
        String hasFormField = "Doesn't have fields form";
        if (camundaFormData != null) {
            if (!camundaFormData.getCamundaFormFields().isEmpty()) {
                hasFormField = "Have fields form";
            }
        }
        return hasFormField;
    }

    public Collection<CamundaFormField> getFieldsForm(UserTask userTask) {
        CamundaFormData camundaFormData = userTask.getExtensionElements().getElementsQuery().filterByType(CamundaFormData.class).singleResult();
        if (camundaFormData != null) {
            return camundaFormData.getCamundaFormFields();
        }
        return null;
    }
}
