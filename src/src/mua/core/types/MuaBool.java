package src.mua.core.types;

public class MuaBool extends MuaValue {
    public final boolean  value;
    public MuaBool(boolean value){
        super();
        this.value = value;
    }
    @Override
    public String toString() {
        return value?"true":"false";
    }
    public boolean toBoolean() {
        return value;
    }
}
