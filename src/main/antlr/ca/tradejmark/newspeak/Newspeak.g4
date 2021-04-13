grammar Newspeak;

@header {
  package ca.tradejmark.newspeak;
}

ASSIGN : ([Ii]'s');
PRINT : [Pp]'rint';
WHILE : [Ww]'hile';
IF : [Ii]'f';
INC : [Ii]'ncrement';
DEC : [Dd]'ecrement';
EXIT : [Ee]'xit';
SET : [Ss]'et';
TO : [Tt]'o';
FOREACH : [Ff]'or each';
IN : 'in';
POS : 'at position';
A : 'a' | 'an';
FUNC : 'function';
GIVEN : 'given';
RUN : [Rr]'un';
WITH : 'with';
THE : 'the';
RESULT : [Rr]'esult';
OF : 'of';
READ : [Rr]'ead';
INTO : 'into';
REALNUM : 'real number';
WHAT : [Ww]'hat';
ELSE : [Oo]'therwise';
ADD : [Aa]'dd';
REMOVE : [Rr]'emove';
EMPTY : 'empty';
ITEM : 'item';
FROM : 'from';

DOT : '.';
OPAREN : '(';
CPAREN : ')';
COLON : ':';
COMMA : ',';
INDENT : '`';
DEDENT : '~';
SLIST : '[';
ELIST : ']';
QM : '?';

POWER : 'to the power of' | '^';
PLUS : 'plus' | '+';
MINUS : 'minus' | '-';
TIMES : 'times' | '*';
DIV : 'divided by' | '/';
LT : 'is less than' | '<';
GT : 'is greater than' | '>';
fragment OEQ : ' or equal to';
LTE : 'is less than'OEQ | '<=';
GTE : 'is greater than'OEQ | '>=';
NEQUALS : 'is not equal to' | '!=';
EQUALS : 'is equal to';

EQS : '=';

STRING : '"'.*?'"';
fragment DIGIT : [0-9];
INT : DIGIT+;
REAL : DIGIT+ '.' DIGIT+;
TRUE : [Tt]'rue';
FALSE : [Ff]'alse';

ID : [a-zA-Z][a-zA-Z0-9]*;

WHITESPACE : [ \n\r\t]+ -> channel(HIDDEN);


line : (statement | question | command | control | special) DOT?;

control : whl | iff | foreach;
whl : WHILE expression (sent | block);
iff : IF expression (sent | block) elsee?;
elsee : ELSE (sent | block);
foreach : FOREACH ID IN expression (sent | block);
block : COLON INDENT line ( line)* DEDENT;
sent : COMMA line;

statement : assignment;
assignment : idOrPos (ASSIGN | EQS) expression;

question : (ident | lmem | lemp) QM;
ident : WHAT ASSIGN expression;
lmem : ASSIGN expression IN expression;
lemp : ASSIGN expression EMPTY;

special : result;
result : RESULT COLON expression;

command : print | incdec | exit | set | run | read | add | remove;
print : PRINT expression;
incdec : (INC | DEC) ID;
exit : EXIT;
set : SET idOrPos TO expression;
run : RUN expression (WITH ID expression (COMMA ID expression)*)?;
type : REALNUM | ID;
read : READ (A? type)? INTO ID;
add : ADD expression TO expression;
remove : REMOVE ITEM expression FROM expression;


idOrPos : ID | expression;

bool : TRUE | FALSE;
list : SLIST (expression)? (COMMA expression)* ELIST;
func : A? FUNC (GIVEN ID (COMMA ID)*)? block;
fres : THE? RESULT OF expression (WITH ID expression (COMMA ID expression)*)?;
expression : INT | REAL | STRING | bool | ID | list | func | fres
                | OPAREN subex=expression CPAREN
                | <assoc=right> expression bop=POWER expression
                | expression bop=(TIMES | DIV) expression
                | expression bop=(PLUS | MINUS) expression
                | expression bop=POS expression
                | expression bop=(GT | LT | GTE | LTE | EQS | EQUALS | NEQUALS) expression;