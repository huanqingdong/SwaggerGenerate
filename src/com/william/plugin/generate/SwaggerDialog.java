package com.william.plugin.generate;

import com.intellij.ide.util.DefaultPsiElementCellRenderer;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.LabeledComponent;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMember;
import com.intellij.psi.PsiMethod;
import com.intellij.ui.CollectionListModel;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.components.JBList;

import javax.swing.*;
import java.util.Arrays;
import java.util.List;

import static com.intellij.openapi.ui.LabeledComponent.create;
import static com.intellij.ui.ToolbarDecorator.createDecorator;

public class SwaggerDialog extends DialogWrapper {


    private final LabeledComponent<JPanel> component;
    private final JList memberList;
    private final PsiClass ownerClass;
    private final List<String> mappingAnnotations = Arrays.asList("org.springframework.web.bind.annotation.GetMapping",
            "org.springframework.web.bind.annotation.PutMapping","org.springframework.web.bind.annotation.DeleteMapping",
            "org.springframework.web.bind.annotation.PatchMapping","org.springframework.web.bind.annotation.RequestMapping",
            "org.springframework.web.bind.annotation.PostMapping");

    public SwaggerDialog(PsiClass ownerClass) {
        super(ownerClass.getProject());
        this.ownerClass = ownerClass;

        setTitle("Select Method or Field for Generate");

        String className = this.ownerClass.getName();
        CollectionListModel<PsiMember> list;
        if (className.contains("Controller")) {
            PsiMethod[] methods = ownerClass.getMethods();
            list = new CollectionListModel<>(methods);
            for (int i = 0; i < methods.length; i++) {
                if(!hasMappingAnnotation(methods[i])){
                    list.remove(methods[i]);
                }
            }
        } else {
            list = new CollectionListModel<>(ownerClass.getFields());
        }
        memberList = new JBList(list);
        memberList.setCellRenderer(new DefaultPsiElementCellRenderer());
        ToolbarDecorator decorator = createDecorator(memberList);
        decorator.disableAddAction();
        decorator.disableRemoveAction();
        decorator.disableUpDownActions();
        JPanel panel = decorator.createPanel();
        component = create(panel, "Select Fields or Method");
        init();
    }


    @Override
    protected JComponent createCenterPanel() {
        return component;
    }

    public java.util.List<PsiMember> getFields() {
        return memberList.getSelectedValuesList();
    }

    /**
     * 判断方法是否存在Mapping注解
     */
    private boolean hasMappingAnnotation(PsiMethod method){
        PsiAnnotation[] annotations = method.getAnnotations();
        for (int i = 0; i < annotations.length; i++) {
            if(mappingAnnotations.contains(annotations[i].getQualifiedName())){
                return true;
            }
        }
        return false;
    }
}
