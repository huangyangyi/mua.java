package src.mua.core.types;

public class MuaWord extends MuaValue {
    public final String value;
    public MuaWord(String value) {
        super();
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }

    @Override
    public double toNumber() {
        return Double.valueOf(value);
    }

}
