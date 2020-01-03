package src.mua.core;

import src.mua.core.types.MuaList;
import src.mua.core.types.MuaNull;
import src.mua.core.types.MuaValue;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Set;

public class SymbolTable {
    private HashMap<String, MuaValue> localVarTable = new HashMap<>();
    private HashMap<String, MuaValue> globalVarTable;
    private HashMap<String, MuaFunc> coreFuncTable;
    private HashMap<String, MuaFunc> operatorTable;
    private MuaCoreFuncImpl muaCoreFuncImpl;

    public SymbolTable() throws IllegalAccessException {
        globalVarTable = localVarTable;
        coreFuncTable = new HashMap<>();
        muaCoreFuncImpl = new MuaCoreFuncImpl();
        MuaConsts muaConsts = new MuaConsts();
        for (Field field : MuaCoreFuncImpl.class.getFields())
            coreFuncTable.put(field.getName().substring(1), (MuaFunc) field.get(muaCoreFuncImpl));
        for (Field field : MuaConsts.class.getFields())
            localVarTable.put(field.getName().substring(1), (MuaValue) field.get(muaConsts));
        operatorTable =new HashMap<>();
        operatorTable.put("+", coreFuncTable.get("add"));
        operatorTable.put("-", coreFuncTable.get("sub"));
        operatorTable.put("*", coreFuncTable.get("mul"));
        operatorTable.put("/", coreFuncTable.get("div"));
        operatorTable.put("%", coreFuncTable.get("mod"));
        operatorTable.put("-u", coreFuncTable.get("negative"));
    }

    public SymbolTable(SymbolTable table) {
        this.globalVarTable = table.globalVarTable;
        this.coreFuncTable = table.coreFuncTable;
        this.muaCoreFuncImpl = table.muaCoreFuncImpl;
        this.operatorTable = table.operatorTable;
    }

    public void provideVariable(String name, MuaValue value) {
        if (coreFuncTable.containsKey(name))
            throw new RuntimeException("Invalid variable name: reserved word");
        localVarTable.put(name, value);
    }

    public void exportVariable(String name) {
        MuaValue value = getVariable(name);
        globalVarTable.put(name, value);
    }

    public MuaValue getVariable(String name) {
        if (localVarTable.containsKey(name)) return localVarTable.get(name);
        else if (globalVarTable.containsKey(name)) return globalVarTable.get(name);
        else return new MuaNull();
    }

    public void eraseSymbol(String name) {
        if (localVarTable.containsKey(name)) localVarTable.remove(name);
        else throw new RuntimeException("Word " + name + " is not a variable name");
    }

    public MuaFunc getFunction(String name) {
        if (!isFunction(name)) throw new RuntimeException("Word " + name + " is not a function name");
        if (coreFuncTable.containsKey(name)) return coreFuncTable.get(name);
        else if (operatorTable.containsKey(name)) return operatorTable.get(name);
        else return new MuaCustomFunc(getVariable(name));
    }

    public boolean isFunction(String name) {
        return coreFuncTable.containsKey(name)||operatorTable.containsKey(name) || (isCustomFunction(getVariable(name)));
    }

    public boolean isCustomFunction(MuaValue value) {
        if (value instanceof MuaList) {
            MuaList func = (MuaList) value;
            if (func.size() != 2) return false;
            if (!(func.get(0) instanceof MuaList && func.get(1) instanceof MuaList)) return false;
            return true;
        } else return false;
    }

    public int getFunctionArgCount(String name) {
        if (!isFunction(name)) return -1;
        else return getFunction(name).argCount();
    }

    public boolean isSymbol(String name) {
        return globalVarTable.containsKey(name) || localVarTable.containsKey(name) || coreFuncTable.containsKey(name);
    }

    public boolean hasReturnValue() {
        return localVarTable.containsKey("_output");
    }

    public MuaValue getReturnValue() {
        return localVarTable.get("_output");
    }

    public Set<String> getLocalNames(){
        return localVarTable.keySet();
    }

    public void eraseLocalVariables() {
        localVarTable.clear();
    }
}
