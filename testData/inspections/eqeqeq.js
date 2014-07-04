function f() {
    'use strict';
    var e;
    if (<warning descr="ESLint: Expected '===' and instead saw '==' (eqeqeq)">e</warning> == 3) {
        return;
    }
    if (<warning descr="ESLint: Expected '!==' and instead saw '!=' (eqeqeq)">e</warning> != 3) {
        return;
    }
}