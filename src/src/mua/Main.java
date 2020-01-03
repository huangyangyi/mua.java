package src.mua;

import src.mua.core.SymbolTable;
import src.mua.parse.Interpreter;
import src.mua.parse.Lexer;

import java.text.ParseException;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws ParseException {
        Scanner scanner = new Scanner(System.in);
        SymbolTable symbolTable = null;
        try {
            symbolTable = new SymbolTable();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        Lexer lexer;
        lexer = new Lexer("", symbolTable);
        while (scanner.hasNext()) {
            String str = scanner.nextLine();
            lexer.append(str);
            while (!lexer.isComplete()) lexer.append(scanner.nextLine());
            Interpreter.accept(lexer, symbolTable, scanner);
        }
    }
}
