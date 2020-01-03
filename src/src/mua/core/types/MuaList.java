package src.mua.core.types;

import java.util.ArrayList;
import java.util.List;

public class MuaList extends MuaValue {
    public final ArrayList<MuaValue> value;

    public MuaList(final List<MuaValue> value) {
        super();
        this.value = new ArrayList<>(value);
    }

    public int size() {
        return this.value.size();
    }

    public MuaValue get(int idx) {
        return this.value.get(idx);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("[");
        for (MuaValue item : value) {
            builder.append(item.toString());
            builder.append(" ");
        }
        if (!value.isEmpty()) builder.deleteCharAt(builder.length()-1);
        builder.append("]");
        String res = builder.toString();
        if (res.length() > 0)
            return res;
        else return res;
    }
}
