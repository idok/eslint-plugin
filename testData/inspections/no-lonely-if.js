var a;
if (a) <error descr="ESLint: Empty block statement (no-empty)">{</error>

} else {
    <warning descr="ESLint: Unexpected if as the only statement in an else block (no-lonely-if)">if</warning> (a) <error descr="ESLint: Empty block statement (no-empty)">{</error>

    }
    }