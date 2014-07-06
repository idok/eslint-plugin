package com.eslint;

import com.eslint.settings.Settings;
import com.eslint.utils.ESLintCommandLineUtilTest;
import com.intellij.codeInsight.daemon.impl.HighlightInfo;
import com.intellij.codeInsight.daemon.impl.HighlightInfoType;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.application.PluginPathManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ContentEntry;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.css.impl.VirtualFileUtil;
import com.intellij.testFramework.PsiTestUtil;
import com.intellij.testFramework.fixtures.*;
import com.intellij.testFramework.fixtures.impl.LightTempDirTestFixtureImpl;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;

import java.util.ArrayList;
import java.util.List;

public class ESLintTest extends LightPlatformCodeInsightFixtureTestCase {
    @Override
    protected String getTestDataPath() {
        return TestUtils.getTestDataPath();
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected boolean isWriteActionRequired() {
        return false;
    }

    protected void doTest(final String file) {
        Project project = myFixture.getProject();
        Settings settings = Settings.getInstance(project);
        settings.eslintExecutable = ESLintCommandLineUtilTest.ESLINT_BIN;
        settings.eslintRcFile = getTestDataPath() + "/.eslintrc";
        settings.nodeInterpreter = ESLintCommandLineUtilTest.NODE_INTERPRETER;
        settings.rulesPath = "";
        settings.pluginEnabled = true;
        myFixture.configureByFile(file);
        myFixture.enableInspections(new ESLintInspection());
        myFixture.checkHighlighting(true, false, true);
    }

    protected void doTest() {
        String name = getTestName(true).replaceAll("_", "-");
        doTest("/inspections/" + name + ".js");
    }

    public void testEqeqeq() {
        doTest();
    }

    public void testNo_negated_in_lhs() {
        doTest();
    }

    public void testValid_typeof() {
        doTest();
    }

    public void testNo_lonely_if() {
        doTest();
    }

    public void testNo_new_object() {
        doTest();
    }

    public void testNo_array_constructor() {
        doTest();
    }
}
