package src;
import java.util.HashMap;
import java.util.Map;

/**
 * Tabela de Símbolos (TS) — núcleo do analisador léxico.
 * <p>
 * {@code classe}: 0 = palavra reservada, 1 = operador/delimitador, 2 = identificador<br>
 * {@code tipo}: 0 = nenhum, 1 = inteiro, 2 = logico, 3 = caractere, 4 = real
 */
public class TS {

    public static final int CLASSE_RESERVADA = 0;
    public static final int CLASSE_OPERADOR = 1;
    public static final int CLASSE_ID = 2;

    public static final int TIPO_NENHUM = 0;
    public static final int TIPO_INTEIRO = 1;
    public static final int TIPO_LOGICO = 2;
    public static final int TIPO_CARACTERE = 3;
    public static final int TIPO_REAL = 4;

    HashMap<String, Entrada> hm = new HashMap<>();
    private int proximoEndereco = 1;

    public static final class Entrada {
        public String nome;
        public int token;
        public String lexema;
        public int classe;
        public int tipo;
        public int endereco;

        public Entrada(String nome, int token, String lexema, int classe, int tipo, int endereco) {
            this.nome = nome;
            this.token = token;
            this.lexema = lexema;
            this.classe = classe;
            this.tipo = tipo;
            this.endereco = endereco;
        }
    }

    public TS() {
        preencheTS();
    }

    public boolean pesquisaLexema(String lexema) {
        return hm.containsKey(lexema);
    }

    public void insereToken(String lexema) {
        if (pesquisaLexema(lexema)) {
            return;
        }
        int end = proximoEndereco++;
        hm.put(lexema, new Entrada(
                lexema,
                TokenType.ID.codigo(),
                lexema,
                CLASSE_ID,
                TIPO_NENHUM,
                end
        ));
    }

    public Entrada buscar(String lexema) {
        return hm.get(lexema);
    }

    public Map<String, Entrada> entradas() {
        return hm;
    }

    public void preencheTS() {
        int end = 0;

        reservada("inteiro", TokenType.INTEIRO, TIPO_INTEIRO, end++);
        reservada("logico", TokenType.LOGICO, TIPO_LOGICO, end++);
        reservada("caractere", TokenType.CARACTERE, TIPO_CARACTERE, end++);
        reservada("real", TokenType.REAL, TIPO_REAL, end++);
        reservada("inicio", TokenType.INICIO, TIPO_NENHUM, end++);
        reservada("fim", TokenType.FIM, TIPO_NENHUM, end++);
        reservada("se", TokenType.SE, TIPO_NENHUM, end++);
        reservada("senao", TokenType.SENAO, TIPO_NENHUM, end++);
        reservada("enquanto", TokenType.ENQUANTO, TIPO_NENHUM, end++);
        reservada("faca", TokenType.FACA, TIPO_NENHUM, end++);
        reservada("leitura", TokenType.LEITURA, TIPO_NENHUM, end++);
        reservada("escrita", TokenType.ESCRITA, TIPO_NENHUM, end++);
        reservada("ou", TokenType.OU, TIPO_NENHUM, end++);
        reservada("div", TokenType.DIV, TIPO_NENHUM, end++);
        reservada("mod", TokenType.MOD, TIPO_NENHUM, end++);
        reservada("verdadeiro", TokenType.VERDADEIRO, TIPO_NENHUM, end++);
        reservada("falso", TokenType.FALSO, TIPO_NENHUM, end++);

        operador("(", TokenType.ABRE_PAR, end++);
        operador(")", TokenType.FECHA_PAR, end++);
        operador(";", TokenType.PONTO_VIRGULA, end++);
        operador(",", TokenType.VIRGULA, end++);
        operador(":", TokenType.DOIS_PONTOS, end++);
        operador(":=", TokenType.ATRIB, end++);
        operador("==", TokenType.IGUAL, end++);
        operador("<>", TokenType.DIF, end++);
        operador("<", TokenType.MENOR, end++);
        operador("<=", TokenType.MENOR_IGUAL, end++);
        operador(">", TokenType.MAIOR, end++);
        operador(">=", TokenType.MAIOR_IGUAL, end++);
        operador("+", TokenType.SOMA, end++);
        operador("-", TokenType.SUB, end++);
        operador("*", TokenType.MULT, end++);
        operador("/", TokenType.DIVISAO, end++);
        operador("&&", TokenType.AND, end++);
    }

    private void reservada(String lexema, TokenType tipo, int tipoDado, int endereco) {
        hm.put(lexema, new Entrada(lexema, tipo.codigo(), lexema, CLASSE_RESERVADA, tipoDado, endereco));
    }

    private void operador(String lexema, TokenType tipo, int endereco) {
        hm.put(lexema, new Entrada(lexema, tipo.codigo(), lexema, CLASSE_OPERADOR, TIPO_NENHUM, endereco));
    }
}
