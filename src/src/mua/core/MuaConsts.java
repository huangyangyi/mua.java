package src.mua.core;

import src.mua.core.types.*;

import java.util.Arrays;

public class MuaConsts {
    public static final MuaValue _pi = new MuaNum(3.14159);
    public static final MuaValue _run = new MuaList(Arrays.asList(new MuaList(Arrays.asList(new MuaWord("x"))),
            new MuaList(Arrays.asList(new MuaWord("repeat"), new MuaWord("1"), new MuaWord(":x")))));
}
