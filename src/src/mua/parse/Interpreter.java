package src.mua.parse;


import src.mua.core.*;
import src.mua.core.types.*;

import java.io.*;
import java.text.ParseException;
import java.util.*;

public class Interpreter {

    private static final MuaValue evalFunc(String name, Lexer tokenStream, SymbolTable symbolTable, Scanner scanner) throws ParseException {
        List<MuaValue> argList = new ArrayList<>();
        MuaFunc func = symbolTable.getFunction(name);
        for (int i = 0; i < func.argCount(); i++) {
            MuaValue arg = eval(tokenStream, symbolTable, scanner);
            /*
            if (arg instanceof MuaNull)
                throw new RuntimeException("Function " + name + " failed to get argument " + func.argName(i));
             */
            argList.add(arg);
        }
        return func.apply(argList, symbolTable, scanner);
    }

    private static final MuaList evalList(Lexer tokenStream) {
        ArrayList<MuaValue> list = new ArrayList<>();
        while (tokenStream.currentToken().type != Token.TokenType.BRACKETS || !tokenStream.currentToken().value.equals("]")) {
            if (tokenStream.currentToken().type == Token.TokenType.BRACKETS && tokenStream.currentToken().value.equals("[")) {
                tokenStream.nextToken();
                list.add(evalList(tokenStream));
            } else {
                list.add(new MuaWord(tokenStream.currentToken().value));
                tokenStream.nextToken();
            }
        }
        tokenStream.nextToken();
        return new MuaList(list);
    }

    private static final MuaValue evalExpr(Lexer tokenStream, SymbolTable symbolTable, Scanner scanner) throws RuntimeException, ParseException {
        Stack<MuaValue> nums = new Stack<>();
        Stack<String> ops = new Stack<>();
        while (ops.empty() || !ops.peek().equals(")")) {
            int neg_count=0;
            while (tokenStream.currentToken().type == Token.TokenType.OPERATOR && tokenStream.currentToken().equals("-")){
                neg_count++;
                tokenStream.nextToken();
            }
            MuaValue num = eval(tokenStream, symbolTable, scanner);
            for (;neg_count > 0;neg_count--)
                num = symbolTable.getFunction("-u").apply(Arrays.asList(num), symbolTable, scanner);
            if (!ops.empty() && (ops.peek().equals("*") || ops.peek().equals("/"))) {
                MuaValue res = symbolTable.getFunction(ops.pop()).apply(Arrays.asList(nums.pop(), num), symbolTable, scanner);
                nums.push(res);
            }
            else nums.push(num);
            if (tokenStream.currentToken().type != Token.TokenType.OPERATOR)
                throw new RuntimeException("Invalid expression!");
            ops.push(tokenStream.currentToken().value);
            tokenStream.nextToken();
        }
        ops.pop();
        while (!ops.empty()) {
            MuaValue y = nums.pop(), x = nums.pop();
            MuaValue res = symbolTable.getFunction(ops.pop()).apply(Arrays.asList(x, y), symbolTable, scanner);
            nums.push(res);
        }
        if (nums.size() != 1) throw new RuntimeException("Invalid expression!");
        return nums.peek();
    }

    private static final MuaValue eval(Lexer tokenStream, SymbolTable symbolTable, Scanner scanner) throws ParseException, RuntimeException {
        Token token = tokenStream.currentToken();
        tokenStream.nextToken();
        if (token.type == Token.TokenType.IDENTIFIER) {
            if (symbolTable.isFunction(token.value))
                return evalFunc(token.value, tokenStream, symbolTable, scanner);
            else return new MuaWord(token.value);
        } else if (token.type == Token.TokenType.INTEGER) return new MuaNum(Double.valueOf(token.value));
        else if (token.type == Token.TokenType.FLOAT) return new MuaNum(Double.valueOf(token.value));
        else if (token.type == Token.TokenType.BOOLEAN) return new MuaBool(Boolean.valueOf(token.value));
        else if (token.type == Token.TokenType.WORD_LITERAL) return new MuaWord(token.value);
        else if (token.type == Token.TokenType.BRACKETS) {
            if (token.value.equals("[")) return evalList(tokenStream);
            else throw new RuntimeException("Unexpected bracket \"" + token.value + "\"");
        } else if (token.type == Token.TokenType.OPERATOR) {
            if (token.value.equals("(")) return evalExpr(tokenStream, symbolTable, scanner);
            else if (token.value.equals("-")) return evalFunc("negative", tokenStream, symbolTable, scanner);
            else throw new RuntimeException("Unexpected operator \"" + token.value + "\"");
        } else if (token.type == Token.TokenType.MUA_KWD) {
            if (token.value.equals(":")) return evalFunc("thing", tokenStream, symbolTable, scanner);
            else throw new RuntimeException("Unknown MuaKwd \"" + token.value + "\"");
        } else if (token.type == Token.TokenType.EOF) {
            return new MuaNull();
        } else throw new RuntimeException("Unknown expected token <" + token.type.name() + ">" + token.value);
    }

    public static final MuaValue evalBlock(Lexer tokenStream, SymbolTable symbolTable, Scanner scanner) throws ParseException {
        MuaValue res = new MuaNull();
        while (!tokenStream.isTokenFinished()) {
            if (tokenStream.currentToken().value.equals("stop")) break;
            else res = eval(tokenStream, symbolTable, scanner);
        }
        if (symbolTable.hasReturnValue()) return symbolTable.getReturnValue();
        return res;
    }

    public static final MuaValue evalBlock(String sourceCode, SymbolTable symbolTable, Scanner scanner) throws ParseException {
        return evalBlock(new Lexer(sourceCode, symbolTable), symbolTable, scanner);
    }

    public static final void accept(Lexer tokenStream, SymbolTable symbolTable, Scanner scanner) throws ParseException {
        evalBlock(tokenStream, symbolTable, scanner);
    }

    public static final void accept(String sourceCode, SymbolTable symbolTable, Scanner scanner)
            throws ParseException {
        evalBlock(sourceCode, symbolTable, scanner);
    }

    public static final void acceptFile(String filePath, SymbolTable symbolTable, Scanner scanner)
        throws ParseException {
        StringBuilder sourceCodeBuilder = new StringBuilder();
        File file = new File(filePath);
        InputStreamReader reader;
        BufferedReader bf;
        try {
             reader = new InputStreamReader(new FileInputStream(file));
             bf = new BufferedReader(reader);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            throw new RuntimeException("File Read Error: " + filePath);
        }
        String line;
        while (true) {
            try {
                if (!((line = bf.readLine()) != null)) break;
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException("File Read Error: " + filePath);
            }
            sourceCodeBuilder.append(line);
            sourceCodeBuilder.append("\n");
        }
        try {
            reader.close();
            bf.close();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Error while closing file: " + filePath);
        }
        accept(sourceCodeBuilder.toString(), symbolTable, scanner);
    }
}
