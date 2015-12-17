package com.eslint.fixes;

import com.google.common.base.Strings;
import com.intellij.psi.PsiElement;

public final class Fixes {
    private Fixes() {
    }

    public static BaseActionFix getFixForRule(String rule, PsiElement element) {
//        Map<String, BaseActionFix> map = new HashMap<String, BaseActionFix>();
//        map.put("strict", )
        if (Strings.isNullOrEmpty(rule)) {
            return null;
        }
        if (rule.equals("strict")) {
            return new StrictActionFix(element);
        }
        if (rule.equals("no-new-object")) {
            return new NoNewObjectActionFix(element);
        }
        if (rule.equals("no-array-constructor")) {
            return new NoArrayConstructorActionFix(element);
        }
        if (rule.equals("eqeqeq")) {
            return new EqeqeqActionFix(element);
        }
        if (rule.equals("no-negated-in-lhs")) {
            return new NoNegatedInLhsActionFix(element);
        }
        if (rule.equals("no-lonely-if")) {
            return new NoLonelyIfActionFix(element);
        }
        if (rule.equals("dot-notation")) {
            return new DotNotationActionFix(element);
        }
        return null;
    }
}