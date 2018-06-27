package com.william.plugin.generate;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiMember;

import java.util.List;

import static com.intellij.openapi.actionSystem.CommonDataKeys.EDITOR;
import static com.intellij.openapi.actionSystem.CommonDataKeys.PSI_FILE;
import static com.intellij.psi.util.PsiTreeUtil.getParentOfType;

/**
 * Created by william.lee on 2018/6/27 0027.
 */
public class SwaggerGenerationAction extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        PsiClass psiClass = getPsiClass(e);
        SwaggerDialog dialog = new SwaggerDialog(psiClass);
        dialog.show();
        if (dialog.isOK()) {
            List<PsiMember> members = dialog.getFields();
            if (members.size() > 0) {
                new SwaggerGenerator(psiClass, members).generate();
            }
        }
    }

    @Override
    public void update(AnActionEvent e) {
        PsiClass psiClass = getPsiClass(e);
        e.getPresentation().setEnabled(psiClass != null);
    }

    private PsiClass getPsiClass(AnActionEvent e) {
        PsiFile psiFile = e.getData(PSI_FILE);
        Editor editor = e.getData(EDITOR);
        if (psiFile == null || editor == null) {
            return null;
        }
        int offset = editor.getCaretModel().getOffset();
        PsiElement elementAt = psiFile.findElementAt(offset);
        return getParentOfType(elementAt, PsiClass.class);
    }
}
