var a;
var b;

if (<error descr="ESLint: The `in` expression's left operand is negated (no-negated-in-lhs)">!</error>a in b) {
    a = 3;
}

var x = <error descr="ESLint: The `in` expression's left operand is negated (no-negated-in-lhs)">!</error>a in b;