package src.mua.core;

import src.mua.core.types.*;
import src.mua.parse.Interpreter;
import src.mua.parse.Lexer;

import java.io.*;
import java.text.ParseException;
import java.util.*;

import static java.lang.Thread.sleep;

@FunctionalInterface
interface TerFunction<One, Two, Three, Res> { //Ternary Function
    Res apply(One one, Two two, Three three) throws ParseException;
}

abstract public class MuaFunc {
    abstract public MuaValue apply(List<MuaValue> argList, SymbolTable symbolTable, Scanner scanner) throws ParseException;

    abstract public String argName(int idx);

    abstract public int argCount();
}

class MuaCustomFunc extends MuaFunc {
    private MuaList names;
    private MuaList code;

    public MuaCustomFunc(MuaValue def) {
        MuaList defList = (MuaList) def;
        names = (MuaList) defList.get(0);
        code = (MuaList) defList.get(1);
    }

    @Override
    public String argName(int idx) {
        return names.get(idx).toString();
    }

    @Override
    public int argCount() {
        return names.size();
    }

    @Override
    public MuaValue apply(List<MuaValue> argList, SymbolTable parentSymbolTable, Scanner scanner) throws ParseException {
        SymbolTable symbolTable = new SymbolTable(parentSymbolTable);
        for (int i = 0; i < names.size(); i++) {
            symbolTable.provideVariable(names.get(i).toString(), argList.get(i));
        }
        String rawCode = code.toString();
        rawCode = rawCode.substring(1, rawCode.length() - 1);
        return Interpreter.evalBlock(rawCode, symbolTable, scanner);
    }
}

class MuaCoreFunc extends MuaFunc {
    private TerFunction<List<MuaValue>, SymbolTable, Scanner, MuaValue> func;
    private List<String> names;

    public MuaCoreFunc(List<String> names, TerFunction<List<MuaValue>, SymbolTable, Scanner, MuaValue> func) {
        this.names = names;
        this.func = func;
    }

    @Override
    public String argName(int idx) {
        return names.get(idx);
    }

    @Override
    public int argCount() {
        return names.size();
    }

    @Override
    public MuaValue apply(List<MuaValue> argList, SymbolTable symbolTable, Scanner scanner) throws ParseException {
        return func.apply(argList, symbolTable, scanner);
    }
}

/*
Implementations of Mua core functions
 */
class MuaCoreFuncImpl {

    public static final MuaCoreFunc _make = new MuaCoreFunc(Arrays.asList("name", "value"),
            (List<MuaValue> args, SymbolTable symbolTable, Scanner scanner) -> {
                MuaValue name = args.get(0), value = args.get(1);
                if (!(name instanceof MuaWord)) {
                    throw new RuntimeException("Invalid type for parameter name:" + name.getClass().getName());
                }
                if (value instanceof MuaNull) {
                    throw new RuntimeException("Invalid type for parameter value:" + value.getClass().getName());
                }
                if (!Lexer.isValidId(name.toString())) {
                    throw new RuntimeException("Invalid name: " + name.toString());
                }
                symbolTable.provideVariable(name.toString(), value);
                return new MuaNull();
            });


    public static final MuaCoreFunc _erase = new MuaCoreFunc(Arrays.asList("name"),
            (List<MuaValue> args, SymbolTable symbolTable, Scanner scanner) -> {
                MuaValue name = args.get(0);
                if (!(name instanceof MuaWord)) {
                    throw new RuntimeException("Invalid type for parameter name:" + name.getClass().getName());
                }
                symbolTable.eraseSymbol(name.toString());
                return new MuaNull();
            });

    public static final MuaCoreFunc _isname = new MuaCoreFunc(Arrays.asList("name"),
            (List<MuaValue> args, SymbolTable symbolTable, Scanner scanner) -> {
                MuaValue name = args.get(0);
                if (!(name instanceof MuaWord)) {
                    return new MuaBool(false);
                }
                return new MuaBool(symbolTable.isSymbol(name.toString()));
            });

    public static final MuaCoreFunc _thing = new MuaCoreFunc(Arrays.asList("name"),
            (List<MuaValue> args, SymbolTable symbolTable, Scanner scanner) -> {
                MuaValue name = args.get(0);
                if (!(name instanceof MuaWord)) {
                    throw new RuntimeException("Invalid type for parameter name:" + name.getClass().getName());
                }
                return symbolTable.getVariable(name.toString());
            });

    public static final MuaCoreFunc _print = new MuaCoreFunc(Arrays.asList("value"),
            (List<MuaValue> args, SymbolTable symbolTable, Scanner scanner) -> {
                MuaValue value = args.get(0);
                String prtString = value.toString();
                if (value instanceof MuaList)
                    System.out.println(prtString.substring(1,prtString.length()-1));
                else
                    System.out.println(prtString);
                return new MuaNull();
            });

    public static final MuaCoreFunc _read = new MuaCoreFunc(Arrays.asList(),
            (List<MuaValue> args, SymbolTable symbolTable, Scanner scanner) -> new MuaWord(scanner.nextLine()));

    public static final MuaCoreFunc _negative = new MuaCoreFunc(Arrays.asList("x"),
            (List<MuaValue> args, SymbolTable symbolTable, Scanner scanner) -> new MuaNum(-args.get(0).toNumber()));

    public static final MuaCoreFunc _add = new MuaCoreFunc(Arrays.asList("x", "y"),
            (List<MuaValue> args, SymbolTable symbolTable, Scanner scanner) -> {
                double x = args.get(0).toNumber(), y = args.get(1).toNumber();
                return new MuaNum(x + y);
            });

    public static final MuaCoreFunc _sub = new MuaCoreFunc(Arrays.asList("x", "y"),
            (List<MuaValue> args, SymbolTable symbolTable, Scanner scanner) -> {
                double x = args.get(0).toNumber(), y = args.get(1).toNumber();
                return new MuaNum(x - y);
            });

    public static final MuaCoreFunc _mul = new MuaCoreFunc(Arrays.asList("x", "y"),
            (List<MuaValue> args, SymbolTable symbolTable, Scanner scanner) -> {
                double x = args.get(0).toNumber(), y = args.get(1).toNumber();
                return new MuaNum(x * y);
            });

    public static final MuaCoreFunc _div = new MuaCoreFunc(Arrays.asList("x", "y"),
            (List<MuaValue> args, SymbolTable symbolTable, Scanner scanner) -> {
                double x = args.get(0).toNumber(), y = args.get(1).toNumber();
                return new MuaNum(x / y);
            });

    public static final MuaCoreFunc _mod = new MuaCoreFunc(Arrays.asList("x", "y"),
            (List<MuaValue> args, SymbolTable symbolTable, Scanner scanner) -> {
                double x = args.get(0).toNumber(), y = args.get(1).toNumber();
                return new MuaNum(Math.round(x) % Math.round(y));
            });

    public static final MuaCoreFunc _eq = new MuaCoreFunc(Arrays.asList("x", "y"),
            (List<MuaValue> args, SymbolTable symbolTable, Scanner scanner) -> {
                if (args.get(0) instanceof MuaList || args.get(1) instanceof MuaList){
                    return new MuaBool(false);
                }
                else if (args.get(0) instanceof MuaWord && args.get(1) instanceof MuaWord) {
                    String x = args.get(0).toString(), y = args.get(1).toString();
                    return new MuaBool(x.equals(y));
                } else {
                    double x,y;
                    try {
                        x = args.get(0).toNumber();
                        y = args.get(1).toNumber();
                    }
                    catch(RuntimeException re)  {
                        return new MuaBool(false);
                    }
                    return new MuaBool(x == y);
                }
            });

    public static final MuaCoreFunc _gt = new MuaCoreFunc(Arrays.asList("x", "y"),
            (List<MuaValue> args, SymbolTable symbolTable, Scanner scanner) -> {
                double x = args.get(0).toNumber(), y = args.get(1).toNumber();
                return new MuaBool(x > y);
            });

    public static final MuaCoreFunc _lt = new MuaCoreFunc(Arrays.asList("x", "y"),
            (List<MuaValue> args, SymbolTable symbolTable, Scanner scanner) -> {
                double x = args.get(0).toNumber(), y = args.get(1).toNumber();
                return new MuaBool(x < y);
            });

    public static final MuaCoreFunc _and = new MuaCoreFunc(Arrays.asList("x", "y"),
            (List<MuaValue> args, SymbolTable symbolTable, Scanner scanner) -> {
                if (!(args.get(0) instanceof MuaBool) || !(args.get(1) instanceof MuaBool)) {
                    throw new RuntimeException("Invalid type for boolean calculus:" + args.get(0).getClass().getName() + args.get(1).getClass().getName());
                }
                boolean x = ((MuaBool) args.get(0)).toBoolean(), y = ((MuaBool) args.get(1)).toBoolean();
                return new MuaBool(x & y);
            });

    public static final MuaCoreFunc _or = new MuaCoreFunc(Arrays.asList("x", "y"),
            (List<MuaValue> args, SymbolTable symbolTable, Scanner scanner) -> {
                if (!(args.get(0) instanceof MuaBool) || !(args.get(1) instanceof MuaBool)) {
                    throw new RuntimeException("Invalid type for boolean calculus:" + args.get(0).getClass().getName() + args.get(1).getClass().getName());
                }
                boolean x = ((MuaBool) args.get(0)).toBoolean(), y = ((MuaBool) args.get(1)).toBoolean();
                return new MuaBool(x | y);
            });

    public static final MuaCoreFunc _not = new MuaCoreFunc(Arrays.asList("x"),
            (List<MuaValue> args, SymbolTable symbolTable, Scanner scanner) -> {
                if (!(args.get(0) instanceof MuaBool)) {
                    throw new RuntimeException("Invalid type for boolean calculus:" + args.get(0).getClass().getName());
                }
                boolean x = ((MuaBool) args.get(0)).toBoolean();
                return new MuaBool(!x);
            });

    public static final MuaCoreFunc _if = new MuaCoreFunc(Arrays.asList("condition", "x", "y"),
            (List<MuaValue> args, SymbolTable symbolTable, Scanner scanner) -> {
                if (!(args.get(0) instanceof MuaBool)) {
                    throw new RuntimeException("Invalid type for boolean calculus:" + args.get(0).getClass().getName());
                }
                boolean condition = ((MuaBool) args.get(0)).toBoolean();
                String code1 = args.get(1).toString(), code2 = args.get(2).toString();
                if (condition)
                    return Interpreter.evalBlock(code1.substring(1, code1.length() - 1), symbolTable, scanner);
                else return Interpreter.evalBlock(code2.substring(1, code2.length() - 1), symbolTable, scanner);
            });

    public static final MuaCoreFunc _output = new MuaCoreFunc(Arrays.asList("x"),
            (List<MuaValue> args, SymbolTable symbolTable, Scanner scanner) -> {
                symbolTable.provideVariable("_output", args.get(0));
                return args.get(0);
            });

    public static final MuaCoreFunc _repeat = new MuaCoreFunc(Arrays.asList("x", "y"),
            (List<MuaValue> args, SymbolTable symbolTable, Scanner scanner) -> {
                if (!(args.get(0) instanceof MuaNum) || !(args.get(1) instanceof MuaList)) {
                    throw new RuntimeException("Invalid type for repeat:" + args.get(0).getClass().getName() + args.get(1).getClass().getName());
                }
                int x = (int) Math.round(args.get(0).toNumber());
                MuaValue res = new MuaNull();
                String code = args.get(1).toString();
                for (int i = 0; i < x; i++) {
                    res = Interpreter.evalBlock(code.substring(1, code.length() - 1), symbolTable, scanner);
                }
                return res;
            });

    public static final MuaCoreFunc _export = new MuaCoreFunc(Arrays.asList("name"),
            (List<MuaValue> args, SymbolTable symbolTable, Scanner scanner) -> {
                MuaValue name = args.get(0);
                symbolTable.exportVariable(name.toString());
                return new MuaNull();
            });

    public static final MuaCoreFunc _isnumber = new MuaCoreFunc(Arrays.asList("x"),
            (List<MuaValue> args, SymbolTable symbolTable, Scanner scanner) -> {
                return new MuaBool(args.get(0) instanceof MuaNum);
            });

    public static final MuaCoreFunc _isword = new MuaCoreFunc(Arrays.asList("x"),
            (List<MuaValue> args, SymbolTable symbolTable, Scanner scanner) -> {
                return new MuaBool(args.get(0) instanceof MuaWord);
            });

    public static final MuaCoreFunc _islist = new MuaCoreFunc(Arrays.asList("x"),
            (List<MuaValue> args, SymbolTable symbolTable, Scanner scanner) -> {
                return new MuaBool(args.get(0) instanceof MuaList);
            });

    public static final MuaCoreFunc _isbool = new MuaCoreFunc(Arrays.asList("x"),
            (List<MuaValue> args, SymbolTable symbolTable, Scanner scanner) -> {
                return new MuaBool(args.get(0) instanceof MuaBool);
            });

    public static final MuaCoreFunc _isempty = new MuaCoreFunc(Arrays.asList("x"),
            (List<MuaValue> args, SymbolTable symbolTable, Scanner scanner) -> {
                MuaValue x = args.get(0);
                if (x instanceof MuaList) return new MuaBool(((MuaList) x).size() == 0);
                else if (x instanceof MuaWord) return new MuaBool(x.toString().length() == 0);
                else throw new RuntimeException("Invalid type for isempty:" + x.getClass().getName());
            });

    public static final MuaCoreFunc _readlist = new MuaCoreFunc(Arrays.asList(),
            (List<MuaValue> args, SymbolTable symbolTable, Scanner scanner) -> {
                List<MuaValue> list = new ArrayList<>();
                String input = scanner.nextLine();
                String[] values = input.split(" ");
                for (String val : values)
                    list.add(new MuaWord(val));
                return new MuaList(list);
            });

    public static final MuaCoreFunc _word = new MuaCoreFunc(Arrays.asList("x", "y"),
            (List<MuaValue> args, SymbolTable symbolTable, Scanner scanner) -> {
                if (!(args.get(0) instanceof MuaWord) || args.get(1) instanceof MuaList || args.get(1) instanceof MuaNull) {
                    throw new RuntimeException("Invalid type for function word:" + args.get(0).getClass().getName() + args.get(1).getClass().getName());
                }
                String x = args.get(0).toString(), y = args.get(1).toString();
                return new MuaWord(x + y);
            });

    public static final MuaCoreFunc _list = new MuaCoreFunc(Arrays.asList("x", "y"),
            (List<MuaValue> args, SymbolTable symbolTable, Scanner scanner) -> {
                if (args.get(0) instanceof MuaNull || args.get(1) instanceof MuaNull) {
                    throw new RuntimeException("Invalid type for function list:" + args.get(0).getClass().getName() + args.get(1).getClass().getName());
                }

                MuaValue x = args.get(0), y = args.get(1);
                ArrayList<MuaValue> list = new ArrayList<>();
                list.add(x);
                list.add(y);
                return new MuaList(list);
            });

    public static final MuaCoreFunc _sentence = new MuaCoreFunc(Arrays.asList("x", "y"),
            (List<MuaValue> args, SymbolTable symbolTable, Scanner scanner) -> {
                if (args.get(0) instanceof MuaNull || args.get(1) instanceof MuaNull) {
                    throw new RuntimeException("Invalid type for function list:" + args.get(0).getClass().getName() + args.get(1).getClass().getName());
                }

                MuaValue x = args.get(0), y = args.get(1);
                ArrayList<MuaValue> list = new ArrayList<>();
                if (x instanceof MuaList)
                    for (MuaValue item: ((MuaList) x).value) list.add(item);
                    else list.add(x);
                if (y instanceof MuaList)
                    for (MuaValue item: ((MuaList) y).value) list.add(item);
                else list.add(y);
                return new MuaList(list);
            });

    public static final MuaCoreFunc _join = new MuaCoreFunc(Arrays.asList("x", "y"),
            (List<MuaValue> args, SymbolTable symbolTable, Scanner scanner) -> {
                if (!(args.get(0) instanceof MuaList) || args.get(1) instanceof MuaNull) {
                    throw new RuntimeException("Invalid type for join:" + args.get(0).getClass().getName() + args.get(1).getClass().getName());
                }

                MuaList x = (MuaList) args.get(0);
                MuaValue y = args.get(1);
                ArrayList<MuaValue> list = new ArrayList<>(x.value);
                list.add(y);
                return new MuaList(list);
            });

    public static final MuaCoreFunc _first = new MuaCoreFunc(Arrays.asList("x"),
            (List<MuaValue> args, SymbolTable symbolTable, Scanner scanner) -> {
                MuaValue x = args.get(0);
                if (x instanceof MuaList) return ((MuaList) x).get(0);
                else return new MuaWord(x.toString().substring(0, 1));
            });

    public static final MuaCoreFunc _last = new MuaCoreFunc(Arrays.asList("x"),
            (List<MuaValue> args, SymbolTable symbolTable, Scanner scanner) -> {
                MuaValue x = args.get(0);
                if (x instanceof MuaList) return ((MuaList) x).get(((MuaList) x).size() - 1);
                else if (x instanceof MuaWord) return new MuaWord(x.toString().substring(x.toString().length() - 1));
                else throw new RuntimeException("Invalid type for last:" + x.getClass().getName());
            });

    public static final MuaCoreFunc _butfirst = new MuaCoreFunc(Arrays.asList("x"),
            (List<MuaValue> args, SymbolTable symbolTable, Scanner scanner) -> {
                MuaValue x = args.get(0);
                if (x instanceof MuaList) return new MuaList(((MuaList) x).value.subList(1, ((MuaList) x).size()));
                else if (x instanceof MuaWord) return new MuaWord(x.toString().substring(1));
                else throw new RuntimeException("Invalid type for butfirst:" + x.getClass().getName());
            });

    public static final MuaCoreFunc _butlast = new MuaCoreFunc(Arrays.asList("x"),
            (List<MuaValue> args, SymbolTable symbolTable, Scanner scanner) -> {
                MuaValue x = args.get(0);
                if (x instanceof MuaList) return new MuaList(((MuaList) x).value.subList(0, ((MuaList) x).size() - 1));
                else if (x instanceof MuaWord) return new MuaWord(x.toString().substring(x.toString().length() - 1));
                else throw new RuntimeException("Invalid type for butlast:" + x.getClass().getName());
            });

    public static final MuaCoreFunc _random = new MuaCoreFunc(Arrays.asList("x"),
            (List<MuaValue> args, SymbolTable symbolTable, Scanner scanner) -> new MuaNum(Math.random() * args.get(0).toNumber()));

    public static final MuaCoreFunc _int = new MuaCoreFunc(Arrays.asList("x"),
            (List<MuaValue> args, SymbolTable symbolTable, Scanner scanner) -> new MuaNum(Math.floor(args.get(0).toNumber())));

    public static final MuaCoreFunc _sqrt = new MuaCoreFunc(Arrays.asList("x"),
            (List<MuaValue> args, SymbolTable symbolTable, Scanner scanner) -> new MuaNum(Math.sqrt(args.get(0).toNumber())));

    public static final MuaCoreFunc _wait = new MuaCoreFunc(Arrays.asList("x"),
            (List<MuaValue> args, SymbolTable symbolTable, Scanner scanner) -> {
                try {
                    Thread.sleep((long) args.get(0).toNumber());
                } catch (InterruptedException e) {
                    throw new RuntimeException("Failed to sleep!");
                }
                return new MuaNull();
            });

    public static final MuaCoreFunc _poall = new MuaCoreFunc(Arrays.asList(),
            (List<MuaValue> args, SymbolTable symbolTable, Scanner scanner) -> {
                Set<String> names = symbolTable.getLocalNames();
                for (String name: names) System.out.println(name);
                return new MuaNull();
            });

    public static final MuaCoreFunc _erall = new MuaCoreFunc(Arrays.asList(),
            (List<MuaValue> args, SymbolTable symbolTable, Scanner scanner) -> {
                symbolTable.eraseLocalVariables();
                return new MuaNull();
            });

    public static final MuaCoreFunc _save = new MuaCoreFunc(Arrays.asList("filename"),
            (List<MuaValue> args, SymbolTable symbolTable, Scanner scanner) -> {
                Set<String> names = symbolTable.getLocalNames();
                OutputStream fs = null;
                try {
                    fs = new FileOutputStream(args.get(0).toString());
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    throw new RuntimeException("File Not Found: " + args.get(0).toString());
                }
                PrintWriter writer = new PrintWriter(fs);
                for (String name: names) writer.printf("make \"%s %s\n", name, symbolTable.getVariable(name).toString());
                writer.close();
                try {
                    fs.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    throw new RuntimeException("IOException while closing FileStream: " + args.get(0).toString());
                }
                return new MuaNull();
            });

    public static final MuaCoreFunc _load = new MuaCoreFunc(Arrays.asList("filename"),
            (List<MuaValue> args, SymbolTable symbolTable, Scanner scanner) -> {
                Interpreter.acceptFile(args.get(0).toString(), symbolTable, scanner);
                return new MuaNull();
            });

}
