package com.eslint;

import com.eslint.settings.Settings;
import com.eslint.utils.ESLintRunnerTest;
import com.intellij.openapi.project.Project;
import com.intellij.testFramework.fixtures.*;

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
        settings.eslintExecutable = ESLintRunnerTest.ESLINT_BIN;
        settings.eslintRcFile = getTestDataPath() + "/.eslintrc";
        settings.nodeInterpreter = ESLintRunnerTest.NODE_INTERPRETER;
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
