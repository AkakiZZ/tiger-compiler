grammar Tiger;

//lexer rules

ARRAY: 'array';
BEGIN: 'begin';
BREAK: 'break';
DO: 'do';
END: 'end';
ENDDO: 'enddo';
ENDIF: 'endif';
FLOAT: 'float';
FOR: 'for';
FUNCTION: 'function';
IF: 'if';
ELSE: 'else';
INT: 'int';
LET: 'let';
OF: 'of';
PROGRAM: 'program';
RETURN: 'return';
STATIC: 'static';
THEN: 'then';
TO: 'to';
TYPE: 'type';
VAR: 'var';
WHILE: 'while';
COMMA: ',';
DOT: '.';
COLON: ':';
SEMICOLON: ';';
OPENPAREN: '(';
CLOSEPAREN: ')';
OPENBRACK: '[';
CLOSEBRACK: ']';
OPENCURLY: '{';
CLOSECURLY: '}';
PLUS: '+';
MINUS: '-';
MULT: '*';
DIV: '/';
POW: '**';
EQUAL: '==';
NEQUAL: '!=';
LESS: '<';
GREAT: '>';
LESSEQ: '<=';
GREATEQ: '>=';
AND: '&';
OR: '|';
ASSIGN: ':=';
TASSIGN: '=';
ID: ([a-z] | [A-Z])[a-zA-Z0-9_]*;
INTLIT: '0' | ([1-9]+)[0-9]*;
FLOATLIT: '0.'[0-9]+ | [1-9][0-9]*'.'[0-9]+;
WS: [ \r\n\t] + -> skip;
COMMENT: '/*' .*? '*/' -> skip;


//parser rules

tiger_program: PROGRAM ID LET declaration_segment BEGIN funct_list END;

declaration_segment: type_declaration_list var_declaration_list;

type_declaration_list: type_declaration type_declaration_list | ;

var_declaration_list: var_declaration var_declaration_list | ;

funct_list: funct funct_list | ;

type_declaration: TYPE ID TASSIGN type SEMICOLON;

type: base_type
    | array_type = ARRAY OPENBRACK INTLIT CLOSEBRACK OF base_type
    | custom_type = ID;

base_type: INT | FLOAT;

var_declaration: storage_class id_list COLON type optional_init SEMICOLON;

storage_class: VAR | STATIC;

id_list: ID | ID COMMA id_list;

optional_init: ASSIGN constant | ;

funct: FUNCTION ID OPENPAREN param_list CLOSEPAREN ret_type BEGIN stat_seq END;

param_list: param param_list_tail | ;

param_list_tail: COMMA param param_list_tail | ;

ret_type: COLON type |;

param: ID COLON type;

stat_seq returns [boolean hasReturn, String returnType, ParserRuleContext retContext]:
      stat
    | stat stat_seq
    ;

stat returns [boolean hasReturn, String returnType, ParserRuleContext retContext]:
      assign_stat = value ASSIGN expr SEMICOLON {$hasReturn = false;}
    | ifOp = IF expr THEN stat_seq ENDIF SEMICOLON {$hasReturn = false;}
    | ifElseOp = IF expr THEN stat_seq ELSE stat_seq ENDIF SEMICOLON {$hasReturn = false;}
    | whileLoop = WHILE expr DO stat_seq ENDDO SEMICOLON {$hasReturn = false;}
    | forLoop = FOR ID ASSIGN expr TO expr DO stat_seq ENDDO SEMICOLON {$hasReturn = false;}
    | function_call = optprefix ID OPENPAREN expr_list CLOSEPAREN SEMICOLON {$hasReturn = false;}
    | BREAK SEMICOLON {$hasReturn = false;}
    | RETURN optreturn SEMICOLON
    | new_scope = LET declaration_segment BEGIN stat_seq END {$hasReturn = false;}
    ;

optreturn returns [String returnType] :
     expr | ;

optprefix: value ASSIGN | ;

expr returns [String typeName, boolean containsEqualityOp]:
      cons = constant {$typeName = $constant.typeName;}
    | val = value {$containsEqualityOp = false;}
    | par = OPENPAREN expr CLOSEPAREN {$containsEqualityOp = false;}
    | <assoc=right> expr POW expr
    | mult_div = expr mult_div_op expr
    | plus_minus = expr plus_minus_op expr
    | equality = expr equality_op expr
    | and = expr AND expr
    | or = expr OR expr;

constant returns [String typeName]:
    INTLIT {$typeName = "int";}
    | FLOATLIT {$typeName = "float";};

equality_op: LESS | GREAT | LESSEQ | GREATEQ | EQUAL | NEQUAL;

mult_div_op: MULT | DIV;

plus_minus_op: PLUS | MINUS;

expr_list: expr expr_list_tail | ;

expr_list_tail: COMMA expr expr_list_tail | ;

value: ID value_tail;

value_tail: OPENBRACK expr CLOSEBRACK | ;