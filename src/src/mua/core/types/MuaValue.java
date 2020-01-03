package src.mua.core.types;

public class MuaValue {
    public MuaValue(){}
    public String toString() {
        return "";
    }
    public double toNumber() {
        throw new RuntimeException("TypeError: Number expected");
    }
}

