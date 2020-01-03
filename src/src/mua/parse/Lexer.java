package src.mua.parse;

import src.mua.core.SymbolTable;

import java.text.ParseException;
import java.util.ArrayList;

public class Lexer {
    private String sourceCode;
    private SymbolTable symbolTable;
    private int idx = 0, tokenIdx = 0, argCount = 0, bracketDepth = 0, parenthesisDepth = 0;
    private ArrayList<Token> tokens = new ArrayList<>();

    public Lexer(String sourceCode, SymbolTable symbolTable) throws ParseException {
        this.sourceCode = sourceCode;
        this.symbolTable = symbolTable;
        splitTokens();
        //printTokens();
    }

    public void append(String sourceCode) throws ParseException {
        this.sourceCode += sourceCode;
        splitTokens();
        //printTokens();
    }

    private static final String upperCaseLetter = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String lowerCaseLetter = "abcdefghijklmnopqrstuvwxyz";
    private static final String decDigits = "0123456789";
    private static final String digitSign = "+-";
    private static final String blanks = " \u000C\n\t\r ";
    private static final String firstIdChars = upperCaseLetter + lowerCaseLetter;
    private static final String idChars = firstIdChars + decDigits + "_";
    private static final String MUA_KWDChars = ":";
    private static final String brackets = "[]";
    private static final String opChars = "+-*/%()";
    private static final String eof = "\u0000";
    private static final String delimiters = opChars + brackets + MUA_KWDChars + blanks + eof;

    public static final boolean isValidId(String idName) {
        char[] name = idName.toCharArray();
        if (firstIdChars.indexOf(name[0]) == -1) return false;
        for (char ch : name) if (idChars.indexOf(ch) == -1) return false;
        return true;
    }

    private char currentChar() {
        if (sourceCode.length() > idx) return sourceCode.charAt(idx);
        else return '\u0000';
    }

    private char peekOneChar() {
        if (sourceCode.length() > idx + 1) return sourceCode.charAt(idx + 1);
        else return '\u0000';
    }

    private boolean isLexFinished() {
        return idx >= sourceCode.length();
    }

    public boolean isTokenFinished() {
        return tokenIdx >= tokens.size();
    }

    public boolean isComplete() {
        return argCount == 0 && bracketDepth == 0 && parenthesisDepth == 0;
    }

    private void nextChar() {
        idx++;
    }

    private void splitTokens() throws ParseException {
        while (this.currentChar() != '\u0000' && !isLexFinished()) {
            char ch = currentChar();
            if (brackets.indexOf(ch) != -1) lexBrackets();
            else if (blanks.indexOf(ch) != -1) ignoreBlanks();
            else if (ch == '/' && peekOneChar() == '/') ignoreComment();
            else if (bracketDepth > 0) lexWord();
            else if (ch == '\"') {
                nextChar();
                lexWord();
            } else if (MUA_KWDChars.indexOf(ch) != -1) lexMuaKwd();
            else if (opChars.indexOf(ch) != -1 &&(parenthesisDepth > 0 || ch=='(')) lexOp();
            else if (decDigits.indexOf(ch) != -1 || (parenthesisDepth == 0 && digitSign.indexOf(ch) != -1)) lexNumber();
            else if (firstIdChars.indexOf(ch) != -1) lexIdentifierOrConstant();
            else throw new ParseException("Unexpected character" + ch, idx);
        }
    }

    private void lexNumber() throws ParseException {
        if (this.argCount > 0) this.argCount--;
        StringBuilder builder = new StringBuilder();
        boolean isFloat;
        isFloat = false;
        if (digitSign.indexOf(this.currentChar())!=-1) {
            builder.append(this.currentChar());
            this.nextChar();
        }
        while (decDigits.indexOf(this.currentChar()) != -1 || (this.currentChar() == '.')) {
            if (this.currentChar() == '.') {
                isFloat = true;
            }
            builder.append(this.currentChar());
            this.nextChar();
        }
        if (delimiters.indexOf(currentChar()) != -1)
            tokens.add(new Token(isFloat ? Token.TokenType.FLOAT : Token.TokenType.INTEGER,
                    builder.toString()));
        else throw new ParseException("Invalid number literal " + builder.toString() + currentChar(), idx);
    }

    private void ignoreBlanks() {
        while (!isLexFinished() && blanks.indexOf(currentChar()) != -1) nextChar();
    }

    private void ignoreComment() {
        while (!isLexFinished() && currentChar() != '\n' && currentChar() != '\u0000') nextChar();
    }

    private void lexWord() {
        if (this.argCount > 0) this.argCount--;
        StringBuilder builder = new StringBuilder();
        builder.append(this.currentChar());
        this.nextChar();
        while (blanks.indexOf(currentChar()) == -1 && eof.indexOf(currentChar()) == -1 && brackets.indexOf(currentChar()) == -1) {
            builder.append(currentChar());
            nextChar();
        }
        tokens.add(new Token(Token.TokenType.WORD_LITERAL, builder.toString()));
    }

    private void lexNameWord() {
        if (this.argCount > 0) this.argCount--;
        StringBuilder builder = new StringBuilder();
        builder.append(this.currentChar());
        this.nextChar();
        while (idChars.indexOf(currentChar()) != -1) {
            builder.append(currentChar());
            nextChar();
        }
        tokens.add(new Token(Token.TokenType.WORD_LITERAL, builder.toString()));
    }

    private void lexBrackets() throws ParseException {
        Token currentToken = new Token(Token.TokenType.BRACKETS, String.valueOf(currentChar()));
        tokens.add(currentToken);
        nextChar();
        if (currentToken.value.equals("[")) bracketDepth++;
        else if (currentToken.value.equals("]")) {
            if (bracketDepth <= 0) throw new ParseException("Unexpected bracket ]", idx);
            bracketDepth--;
        }
    }

    private void lexMuaKwd() throws ParseException {
        Token currentToken = new Token(Token.TokenType.MUA_KWD, String.valueOf(currentChar()));
        tokens.add(currentToken);
        nextChar();
        if (currentToken.value.equals(":")) lexNameWord();
    }

    private void lexOp() {
        Token thisToken = new Token(Token.TokenType.OPERATOR, String.valueOf(currentChar()));
        tokens.add(thisToken);
        if (thisToken.value.equals("(")) this.parenthesisDepth++;
        else if (thisToken.value.equals(")")) this.parenthesisDepth--;
        nextChar();
    }

    private void lexIdentifierOrConstant() throws ParseException {
        if (this.argCount > 0) this.argCount--;
        StringBuilder builder = new StringBuilder();
        while (idChars.indexOf(currentChar()) != -1) {
            builder.append(currentChar());
            nextChar();
        }
        if (delimiters.indexOf(currentChar()) != -1) {
            String id = builder.toString();
            if (id.equals("true") || id.equals("false"))
                tokens.add(new Token(Token.TokenType.BOOLEAN, id));
            else {
                tokens.add(new Token(Token.TokenType.IDENTIFIER, id));
                if (symbolTable.isFunction(id)) this.argCount += symbolTable.getFunctionArgCount(id);
            }
        } else throw new ParseException("Invalid identifier" + builder.toString() + currentChar(), idx);
    }

    public void printTokens() {
        tokens.forEach((token) -> System.out.printf("<%s> %s ", token.type, token.value));
        System.out.println();
    }

    public Token currentToken() {
        return tokens.get(tokenIdx);
    }

    public void nextToken() {
        ++tokenIdx;
    }

    private void reset() {
        tokenIdx = 0;
    }


}
