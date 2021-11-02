package io.github.kaftejiman;

import com.intellij.codeInsight.completion.AllClassesGetter;
import com.intellij.codeInsight.completion.PlainPrefixMatcher;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileTypes.PlainTextLanguage;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.Processor;

public class ExportAction extends AnAction {

    int totalMethods = 0;
    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {

        final StringBuilder infoBuilder = new StringBuilder();
        Project project = anActionEvent.getData(CommonDataKeys.PROJECT);
        PsiFileFactory fileFactory = PsiFileFactory.getInstance(project);
        // process single class
        Processor<PsiClass> processor = new Processor<PsiClass>() {
            @Override
            public boolean process(PsiClass psiClass) {
                PsiMethod[] methods = psiClass.getMethods();
                String className = NameUtils.javaToSmaliType(psiClass.getQualifiedName());

                // loop through methods
                for (PsiMethod m: methods) {
                    infoBuilder.append(className+"->"+m.getName());
                    PsiParameter[] params = m.getParameterList().getParameters();
                    infoBuilder.append("(");
                    int i=0;
                    while (i<params.length){
                        PsiParameter p = params[i];
                        String param = NameUtils.javaToSmaliType(p.getType());
                        infoBuilder.append(param);
                        i++;
                    }
                    infoBuilder.append(")");
                    String ret;
                    PsiTypeElement retType = m.getReturnTypeElement();
                    if(retType == null){
                        ret = "null";
                    }else{
                        ret = NameUtils.javaToSmaliType(retType.getType().getCanonicalText());
                    }
                    infoBuilder.append(ret);
                    infoBuilder.append("\n");
                    totalMethods++;
                }
                return true;
            }
        };

        // Get all JavaClasses on project folder
        AllClassesGetter.processJavaClasses(
                new PlainPrefixMatcher(""),
                project,
                GlobalSearchScope.projectScope(project),
                processor
        );

        PsiFile outputFile = fileFactory.createFileFromText("methodList.txt", PlainTextLanguage.INSTANCE, (CharSequence) infoBuilder.toString());
        PsiDirectory directory = PsiManager.getInstance(project).findDirectory(project.getBaseDir());
        try{
            directory.add(outputFile);
            Messages.showMessageDialog(anActionEvent.getProject(), "Exported total methods of "+ totalMethods + "\nFile saved to: " + directory.toString().substring(13) + "\\methodList.txt", "Info", null);
        }catch(Exception e){
            Messages.showMessageDialog(anActionEvent.getProject(), "Method list already extracted, please delete methodList.txt and try again.", "Warning", null);
        }
    }

    @Override
    public void update(AnActionEvent e) {
        Editor editor = e.getData(CommonDataKeys.EDITOR);
        PsiFile psiFile = e.getData(CommonDataKeys.PSI_FILE);
        e.getPresentation().setEnabled(editor != null && psiFile != null);
    }

}