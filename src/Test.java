import src.mua.Main;

import java.io.*;
import java.math.BigDecimal;
import java.util.Scanner;

public class Test {
    private static final String[] input =
    {
        "make \"a 6\n" +
                "print :a\n" +
                "make \"b \"a\n" +
                "print thing :b\n" +
                "make \"c mul add :a 13 :a\n" +
                "print sub :c \"6\n" +
                "make \"d read\n" +
                "1234\n" +
                "print isname \"d\n" +
                "print :d\n" +
                "make \"x eq :d 1234\n" +
                "print :x\n" +
                "erase \"d\n" +
                "print not isname \"d\n"
    };
    private static final Object[][] result = {
            {6.0,
            6.0,
            108.0,
            "true",
            "1234",
            "true",
            "true"}
    };
    public static void main(String[] args) throws Exception {
        PipedOutputStream pos = new PipedOutputStream();
        PipedInputStream pin = new PipedInputStream(pos);
        PrintStream sysout = System.out;
        int cnt = 0;
        for ( int i = 0; i<input.length; i++ ) {
            String case1 = input[i];
            ByteArrayInputStream cin = new ByteArrayInputStream(case1.getBytes());
            PrintStream cout = new PrintStream(pos, true);
            System.setIn(cin);
            System.setOut(cout);
            Main.main(null);
            cout.flush();
            cout.close();
            Scanner in = new Scanner(pin);
            int idx = 0;
            while (in.hasNext()) {
                String w = in.next();
                if (result[i][idx] instanceof String) {
                    cnt += result[i][idx].equals(w) ? 1 : 0;
                } else {
                    BigDecimal a = new BigDecimal((Double) result[i][idx]);
                    BigDecimal b = new BigDecimal(Double.parseDouble(w));
                    cnt += a.compareTo(b)==0?1:0;
                }
                idx++;
            }
        }
        System.setOut(sysout);
        System.out.println(cnt);
        /*
        PrintWriter out = new PrintWriter(
                new OutputStreamWriter(
                        new FileOutputStream(args[0])
                ));
        out.println(cnt);
        out.close();

         */
    }
}
