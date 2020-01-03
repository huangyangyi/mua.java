package src.mua.parse;

public class Token {
    TokenType type;
    String value;

    public Token(TokenType type,
                 String value){
        this.type = type;
        this.value = value;
        //System.out.println("<"+type.name()+">"+ value);
    }

    public enum TokenType {
        INTEGER,   //number: int
        FLOAT,  //number: float
        WORD_LITERAL,//word literal value
        BOOLEAN,      //boolean value: true or false
        OPERATOR,
        IDENTIFIER,
        MUA_KWD,
        BRACKETS,
        EOF
    }
}

