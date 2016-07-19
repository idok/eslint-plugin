package com.eslint.actions;

import com.eslint.ESLintProjectComponent;
import com.eslint.utils.ESLintRunner;
import com.intellij.execution.ExecutionException;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataConstants;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;

public class ESLintFixAction extends AnAction implements DumbAware {

    public void actionPerformed(AnActionEvent e) {
        final Project project = e.getProject();
        if (project == null) return;
        final VirtualFile file = (VirtualFile) e.getDataContext().getData(DataConstants.VIRTUAL_FILE);

        // TODO handle multiple selection
        if (file == null) {
//            File[] rtFiles = RTFile.DATA_KEY.getData(e.getDataContext());
//            if (rtFiles == null || rtFiles.length == 0) {
//                System.out.println("No file for rt compile");
//                return;
//            }
//            // handle all files
//            for (RTFile rtFile : rtFiles) {
//                RTFileListener.compile(rtFile.getRtFile().getVirtualFile(), project);
//            }
        } else {
            ESLintProjectComponent component = project.getComponent(ESLintProjectComponent.class);
            if (!component.isSettingsValid() || !component.isEnabled()) {
                return;
            }
//            Result result = ESLintRunner.lint(project.getBasePath(), relativeFile, component.nodeInterpreter, component.eslintExecutable, component.eslintRcFile, component.customRulesPath);

            if (project.getBasePath() != null) {
                ESLintRunner.ESLintSettings settings = ESLintRunner.buildSettings(project.getBasePath(), file.getPath(), component);
                try {
                    ESLintRunner.fix(settings);
                    file.refresh(false, false);
                } catch (ExecutionException e1) {
                    e1.printStackTrace();
                }
            }
        }
    }

    //TODO implement update, disable when not relevant?
    // add project view popup
    //fix menu location
}
