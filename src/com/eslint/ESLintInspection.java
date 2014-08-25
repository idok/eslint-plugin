package com.eslint;

import com.eslint.settings.ESLintSettingsPage;
import com.google.common.base.Joiner;
import com.intellij.codeInspection.*;
import com.intellij.codeInspection.ex.UnfairLocalInspectionTool;
import com.intellij.ide.DataManager;
import com.intellij.ide.actions.ShowSettingsUtilImpl;
import com.intellij.lang.javascript.JSBundle;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ex.SingleConfigurableEditor;
import com.intellij.openapi.options.newEditor.OptionsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiFile;
import com.intellij.ui.HyperlinkAdapter;
import com.intellij.ui.HyperlinkLabel;
import com.intellij.ui.IdeBorderFactory;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import java.awt.*;
import java.util.List;

public class ESLintInspection extends LocalInspectionTool implements BatchSuppressableTool, UnfairLocalInspectionTool { //extends PropertySuppressableInspectionBase {

    public static final String INSPECTION_SHORT_NAME = "ESLintInspection";
    public static final Key<ESLintInspection> KEY = Key.create(INSPECTION_SHORT_NAME);

    private static final Logger LOG = Logger.getInstance(ESLintBundle.LOG_ID);

    @NotNull
    public String getDisplayName() {
        return ESLintBundle.message("eslint.property.inspection.display.name");
    }

    @NotNull
    public String getShortName() {
        return INSPECTION_SHORT_NAME;
    }


    public ProblemDescriptor[] checkFile(@NotNull PsiFile file, @NotNull final InspectionManager manager, final boolean isOnTheFly) {
        return ExternalAnnotatorInspectionVisitor.checkFileWithExternalAnnotator(file, manager, isOnTheFly, new ESLintExternalAnnotator());
    }

    @NotNull
    @Override
    public PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly, @NotNull LocalInspectionToolSession session) {
        return new ExternalAnnotatorInspectionVisitor(holder, new ESLintExternalAnnotator(), isOnTheFly);
    }

    public JComponent createOptionsPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        HyperlinkLabel settingsLink = createHyperLink();
        panel.setBorder(IdeBorderFactory.createTitledBorder(getDisplayName() + " options"));
        panel.add(settingsLink);
        return panel;
    }

    @NotNull
    public String getId() {
        return "Settings.JavaScript.Linters.ESLint";
    }

    @NotNull
    private HyperlinkLabel createHyperLink() {
        List path = ContainerUtil.newArrayList(JSBundle.message("settings.javascript.root.configurable.name"), JSBundle.message("settings.javascript.linters.configurable.name"), getDisplayName());

        String title = Joiner.on(" / ").join(path);
        final HyperlinkLabel settingsLink = new HyperlinkLabel(title);
        settingsLink.addHyperlinkListener(new HyperlinkAdapter() {
            public void hyperlinkActivated(HyperlinkEvent e) {
                DataContext dataContext = DataManager.getInstance().getDataContext(settingsLink);
                OptionsEditor optionsEditor = OptionsEditor.KEY.getData(dataContext);
                if (optionsEditor == null) {
                    Project project = CommonDataKeys.PROJECT.getData(dataContext);
                    if (project != null) {
                        showSettings(project);
                    }
                    return;
                }
                Configurable configurable = optionsEditor.findConfigurableById(ESLintInspection.this.getId());
                if (configurable != null) {
                    optionsEditor.clearSearchAndSelect(configurable);
                }
            }
        });
        return settingsLink;
    }

    public static void showSettings(Project project) {
        ESLintSettingsPage configurable = new ESLintSettingsPage(project);
        String dimensionKey = ShowSettingsUtilImpl.createDimensionKey(configurable);
        SingleConfigurableEditor singleConfigurableEditor = new SingleConfigurableEditor(project, configurable, dimensionKey, false);
        singleConfigurableEditor.show();
    }

    @Override
    public boolean isSuppressedFor(@NotNull PsiElement element) {
        return false;
    }

    @NotNull
    @Override
    public SuppressQuickFix[] getBatchSuppressActions(@Nullable PsiElement element) {
        return new SuppressQuickFix[0];
    }
}