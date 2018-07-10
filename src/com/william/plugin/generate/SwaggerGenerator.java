package com.william.plugin.generate;

import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.psi.*;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.javadoc.PsiDocComment;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.intellij.psi.JavaPsiFacade.getElementFactory;

public class SwaggerGenerator {

    private final PsiClass ownerClass;
    private final List<PsiMember> memberToGenerate;
    private final PsiElementFactory elementFactory;

    public SwaggerGenerator(PsiClass ownerClass, List<PsiMember> memberToGenerate) {
        this.ownerClass = ownerClass;
        this.memberToGenerate = memberToGenerate;
        elementFactory = getElementFactory(ownerClass.getProject());
    }

    public void generate() {
        new WriteCommandAction.Simple(ownerClass.getProject(), ownerClass.getContainingFile()) {
            @Override
            protected void run() throws Throwable {
                generateCode();
            }
        }.execute();
    }

    private void generateCode() {
        PsiMember psiMember = memberToGenerate.get(0);
        boolean isField = false;
        Optional<PsiElement> psiElementOpt = Arrays.stream(ownerClass.getChildren()).filter(element -> !(element instanceof PsiDocComment)).findFirst();
        if (psiMember instanceof PsiField) {
            isField = true;
            psiElementOpt.ifPresent(element -> {
                PsiAnnotation classAnno = elementFactory.createAnnotationFromText("@ApiModel(value = \"\", description = \"\")", ownerClass);
                ownerClass.addBefore(classAnno, getFirstChild(element));
            });

            PsiImportStatement importStatement = elementFactory.createImportStatementOnDemand("io.swagger.annotations");
            ownerClass.addBefore(importStatement, getFirstChild(ownerClass));
        } else if (psiMember instanceof PsiMethod) {
            isField = false;
            psiElementOpt.ifPresent(element -> {
                PsiAnnotation classAnno = elementFactory.createAnnotationFromText("@Api(value = \"\", description = \"\")\n", ownerClass);
                ownerClass.addBefore(classAnno, getFirstChild(element));
            });
            PsiImportStatement importStatement = elementFactory.createImportStatementOnDemand("io.swagger.annotations");
            ownerClass.addBefore(importStatement, getFirstChild(ownerClass));
        }

        for (PsiMember member : memberToGenerate) {
            if (isField) {
                PsiField field = (PsiField) member;
                PsiAnnotation fieldAnno = elementFactory.createAnnotationFromText("@ApiModelProperty(value = \"\")", field);
                ownerClass.addBefore(fieldAnno, getFirstChild(field));
            } else {
                PsiMethod method = (PsiMethod) member;
                PsiParameterList parameterList = method.getParameterList();
                PsiParameter[] parameters = parameterList.getParameters();

                for (PsiParameter parameter : parameters) {
                    String str = "@ApiParam(name = \"\")";
                    PsiAnnotation apiParam = elementFactory.createAnnotationFromText(str, parameter);
                    ownerClass.addBefore(apiParam, getFirstChild(parameter));
                }
            }
        }

        CodeStyleManager codeStyleManager = CodeStyleManager.getInstance(ownerClass.getProject());
        PsiFile containingFile = ownerClass.getContainingFile();
        codeStyleManager.reformatText(containingFile, 0, containingFile.getTextLength());
    }

    public PsiElement getFirstChild(PsiElement element) {
        PsiElement firstChild = element.getFirstChild();
        if (Objects.isNull(firstChild))
            return element;
        else
            return firstChild;
    }

}
