package com.william.plugin.generate;

import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementFactory;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiImportList;
import com.intellij.psi.PsiImportStatement;
import com.intellij.psi.PsiMember;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiModifierListOwner;
import com.intellij.psi.PsiParameter;
import com.intellij.psi.PsiParameterList;
import com.intellij.psi.PsiParserFacade;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.javadoc.PsiDocComment;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static com.intellij.psi.JavaPsiFacade.getElementFactory;

public class SwaggerGenerator {

    private final PsiClass ownerClass;
    private final List<PsiMember> memberToGenerate;
    private final PsiElementFactory elementFactory;
    private final PsiParserFacade parserFacade;
    private static final String SWAGGER_PACKAGE = "io.swagger.annotations";
    /**
     * Bean类下相关注解
     */
    private static final String API_MODEL = "io.swagger.annotations.ApiModel";
    private static final String API_MODEL_PROPERTY = "io.swagger.annotations.ApiModelProperty";
    private static final String API_MODEL_TEMPLATE = "@ApiModel(value = \"\", description = \"\")";
    private static final String API_MODEL_PROPERTY_TEMPLATE = "@ApiModelProperty(value = \"\")\n";
    /**
     * Controller类相关注解
     */
    private static final String API = "io.swagger.annotations.Api";
    private static final String API_OPERATION = "io.swagger.annotations.ApiOperation";
    private static final String API_PARAM = "io.swagger.annotations.ApiParam";
    private static final String API_TEMPLATE = "@Api(value = \"\", description = \"\")\n";
    private static final String API_OPERATION_TEMPLATE = "@ApiOperation(value = \"\", notes =  \"\")";
    private static final String API_PARAM_TEMPLATE = "@ApiParam(value = \"\") ";


    public SwaggerGenerator(PsiClass ownerClass, List<PsiMember> memberToGenerate) {
        this.ownerClass = ownerClass;
        this.memberToGenerate = memberToGenerate;
        elementFactory = getElementFactory(ownerClass.getProject());
        parserFacade = PsiParserFacade.SERVICE.getInstance(ownerClass.getProject());
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
            // 添加ApiModel
            if (notHasAnnotation(ownerClass, API_MODEL)) {
                psiElementOpt.ifPresent(element -> {
                    PsiAnnotation classAnno = elementFactory.createAnnotationFromText(API_MODEL_TEMPLATE, ownerClass);
                    ownerClass.addBefore(classAnno, getFirstChild(element));
                });
            }
        } else if (psiMember instanceof PsiMethod) {
            isField = false;
            // 添加@Api
            if (notHasAnnotation(ownerClass, API)) {
                psiElementOpt.ifPresent(element -> {
                    PsiAnnotation classAnno = elementFactory.createAnnotationFromText(API_TEMPLATE, ownerClass);
                    ownerClass.addBefore(classAnno, getFirstChild(element));
                });
            }
        }
        // 导入依赖包
        if (notHasImport(SWAGGER_PACKAGE)) {
            PsiImportStatement importStatement = elementFactory.createImportStatementOnDemand(SWAGGER_PACKAGE);
            ownerClass.getParent().addBefore(importStatement, ownerClass);
        }
        for (PsiMember member : memberToGenerate) {
            if (isField) {
                PsiField field = (PsiField) member;
                if (notHasAnnotation(field, API_MODEL_PROPERTY)) {
                    PsiAnnotation fieldAnno = elementFactory.createAnnotationFromText(API_MODEL_PROPERTY_TEMPLATE, field);
                    field.addBefore(fieldAnno, getFirstChild(field));
                }
            } else {
                PsiMethod method = (PsiMethod) member;
                PsiParameterList parameterList = method.getParameterList();
                PsiParameter[] parameters = parameterList.getParameters();
                // 参数前添加@ApiParam
                for (PsiParameter parameter : parameters) {
                    if (notHasAnnotation(parameter, API_PARAM)) {
                        PsiAnnotation apiParam = elementFactory.createAnnotationFromText(API_PARAM_TEMPLATE, parameter);
                        PsiElement whiteSpace = parserFacade.createWhiteSpaceFromText(" ");
                        parameter.addBefore(apiParam, getFirstChild(parameter));
                        parameter.addAfter(whiteSpace, getFirstChild(parameter));
                    }
                }
                // 方法上添加@ApiOperation
                if (notHasAnnotation(method, API_OPERATION)) {
                    PsiAnnotation apiOperation = elementFactory.createAnnotationFromText(API_OPERATION_TEMPLATE, method);
                    method.addBefore(apiOperation, getFirstChild(method));
                }
            }
        }

        CodeStyleManager codeStyleManager = CodeStyleManager.getInstance(ownerClass.getProject());
        PsiFile containingFile = ownerClass.getContainingFile();
        codeStyleManager.reformatText(containingFile, 0, containingFile.getTextLength());
    }

    /**
     * 判断对象是否不存在某一注解(@xxx)
     */
    private boolean notHasAnnotation(PsiModifierListOwner owner, String annotation) {
        return owner.getAnnotation(annotation) == null;
    }

    /**
     * 判断类对象是否不存在某一导入项(import xxx)
     */
    private boolean notHasImport(String name) {
        boolean notHas = true;
        PsiElement[] elements = ownerClass.getParent().getChildren();
        for (int i = 0; i < elements.length; i++) {
            if (elements[i] instanceof PsiImportList) {
                notHas = ((PsiImportList) elements[i]).findOnDemandImportStatement(name) == null;
            }
        }
        return notHas;
    }

    /**
     * 获取第一个非文档注释类型的子对象
     */
    public PsiElement getFirstChild(PsiElement element) {
        PsiElement[] elements = element.getChildren();
        for (PsiElement psiElement : elements) {
            if (!(psiElement instanceof PsiDocComment)) {
                return psiElement;
            }
        }
        return element;
    }

}
