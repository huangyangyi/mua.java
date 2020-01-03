package src.mua.core.types;

public class MuaNum extends MuaValue {
    public final double value;
    public MuaNum(double value) {
        super();
        this.value = value;
    }

    @Override
    public double toNumber() {
        return this.value;
    }

    @Override
    public String toString() {
        return Double.toString(value);
    }

}
