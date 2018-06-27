package com.william.plugin.generate;

import com.intellij.ide.util.DefaultPsiElementCellRenderer;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.LabeledComponent;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiMember;
import com.intellij.ui.CollectionListModel;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.components.JBList;

import javax.swing.*;

import static com.intellij.openapi.ui.LabeledComponent.create;
import static com.intellij.ui.ToolbarDecorator.createDecorator;

public class SwaggerDialog extends DialogWrapper {


    private final LabeledComponent<JPanel> component;
    private final JList memberList;
    private final PsiClass ownerClass;

    public SwaggerDialog(PsiClass ownerClass) {
        super(ownerClass.getProject());
        this.ownerClass = ownerClass;

        setTitle("Select Method or Field for Generate");

        String className = this.ownerClass.getName();
        CollectionListModel<PsiMember> list;
        if (className.contains("Controller")) {
            list = new CollectionListModel<>(ownerClass.getMethods());
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
}
