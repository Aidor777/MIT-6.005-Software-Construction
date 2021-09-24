/* Copyright (c) 2015-2017 MIT 6.005 course staff, all rights reserved.
 * Redistribution of original or derived work requires permission of course staff.
 */

// grammar Expression;

@skip whitespace{
    root ::= sum;
	sum ::= multiply ('+' multiply)*;
	multiply ::= primitive ('*' primitive)*;
	primitive ::= number | variable | '(' sum ')';
}
number ::= [0-9]+ | [0-9]+'.'[0-9]+;
variable ::= [a-zA-z]+;
whitespace ::= [ \t]+;
