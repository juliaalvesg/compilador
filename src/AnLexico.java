package src;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import src.TS.Entrada;

/**
 * Analisador léxico com autômato de estados e {@link TS}.
 * Estados: 0 (início), 1 (lexema alfanumérico), 14 (final — consulta/insere na TS).
 */
public class AnLexico {

    private static final int EOF = -1;

    private static final int ESTADO_INICIO = 0;
    private static final int ESTADO_ID = 1;
    private static final int ESTADO_NUM = 2;
    private static final int ESTADO_NUM_REAL = 3;
    private static final int ESTADO_CHAR = 4;
    private static final int ESTADO_STRING = 5;
    private static final int ESTADO_ID_FIM = 14;

    TS ts = new TS();

    private final String source;
    private final int length;

    private int pos;
    private int line = 1;
    private int column = 1;
    private int lineStart;

    int estadoAtual = ESTADO_INICIO;
    String lexema = "";
    private int b;

    private int tokenLine;
    private int tokenCol;

    public AnLexico(String source) {
        if (source == null) {
            throw new IllegalArgumentException("fonte não pode ser null");
        }
        this.source = source;
        this.length = source.length();
        this.lineStart = 0;
        this.b = lerProximo();
    }

    public AnLexico(Path arquivo) throws IOException {
        this(Files.readString(arquivo));
    }

    public TS tabelaSimbolos() {
        return ts;
    }

    public List<Token> analisar() {
        List<Token> saida = new ArrayList<>();
        Token t;
        do {
            t = proximoToken();
            saida.add(t);
        } while (t.type() != TokenType.EOF);
        return saida;
    }

    public Token proximoToken() {
        while (true) {
            switch (estadoAtual) {
            case ESTADO_INICIO -> {
                ignorarIrrelevantes();
                if (b == EOF) {
                    return new Token(TokenType.EOF, "", line, column);
                }

                tokenLine = line;
                tokenCol = column;
                char c = (char) b;

                if (isAlpha(c)) {
                    lexema = String.valueOf(c);
                    avancar();
                    estadoAtual = ESTADO_ID;
                    break;
                }

                if (Character.isDigit(c)) {
                    lexema = String.valueOf(c);
                    avancar();
                    estadoAtual = ESTADO_NUM;
                    break;
                }

                if ((c == '+' || c == '-') && pos < length && Character.isDigit(source.charAt(pos))) {
                    lexema = String.valueOf(c);
                    avancar();
                    estadoAtual = ESTADO_NUM;
                    break;
                }

                if (c == '\'') {
                    lexema = "'";
                    avancar();
                    estadoAtual = ESTADO_CHAR;
                    break;
                }

                if (c == '"') {
                    lexema = "\"";
                    avancar();
                    estadoAtual = ESTADO_STRING;
                    break;
                }

                String op = reconhecerOperador();
                if (op != null) {
                    TS.Entrada e = ts.buscar(op);
                    avancarOperador(op.length());
                    return new Token(TokenType.deCodigo(e.token), op, tokenLine, tokenCol);
                }

                erroLexico("caracter inválido '" + c + "'", line, column);
            }

                case ESTADO_ID -> {
                    if (b != EOF) {
                        char c = (char) b;
                        if (isAlpha(c) || Character.isDigit(c) || c == '_') {
                            if (lexema.length() >= 512) {
                                erroLexico("identificador excede 512 caracteres", tokenLine, tokenCol);
                            }

                            lexema += c;
                            avancar();
                            break;
                        }
                    }
                    estadoAtual = ESTADO_ID_FIM;
                    break;
                }

                case ESTADO_ID_FIM -> {
                    boolean achou = ts.pesquisaLexema(lexema);
                    if (!achou) {
                        ts.insereToken(lexema);
                    }
                    TS.Entrada e = ts.buscar(lexema);
                    String lx = lexema;
                    lexema = "";
                    estadoAtual = ESTADO_INICIO;
                    return new Token(TokenType.deCodigo(e.token), lx, tokenLine, tokenCol);
                }

                case ESTADO_NUM -> {
                    if (b != EOF && Character.isDigit((char) b)) {
                        lexema += (char) b;
                        avancar();
                        break;
                    }
                    if (b == (int) '.') {
                        lexema += '.';
                        avancar();
                        if (b == EOF || !Character.isDigit((char) b)) {
                            erroLexico("literal real requer dígitos após '.'", tokenLine, tokenCol);
                        }
                        estadoAtual = ESTADO_NUM_REAL;
                        break;
                    }
                    String num = lexema;
                    lexema = "";
                    estadoAtual = ESTADO_INICIO;
                    return new Token(TokenType.CONST_INT, num, tokenLine, tokenCol);
                }

                case ESTADO_NUM_REAL -> {
                    if (b != EOF && Character.isDigit((char) b)) {
                        lexema += (char) b;
                        avancar();
                        break;
                    }
                    String real = lexema;
                    lexema = "";
                    estadoAtual = ESTADO_INICIO;
                    return new Token(TokenType.CONST_REAL, real, tokenLine, tokenCol);
                }

                case ESTADO_CHAR -> {
                    if (b == EOF) {
                        erroLexico("literal de caractere não terminado", tokenLine, tokenCol);
                    }
                    if (b == (int) '\\') {
                        lexema += '\\';
                        avancar();
                        if (b == EOF) {
                            erroLexico("escape incompleto", line, column);
                        }
                        lexema += (char) b;
                        avancar();
                    } else {
                        char c = (char) b;
                        if (c == '\'' || c == '\n' || c == '\r') {
                            erroLexico("caractere inválido no literal", tokenLine, tokenCol);
                        }
                        lexema += c;
                        avancar();
                    }
                    if (b != (int) '\'') {
                        erroLexico("esperado ' para fechar literal", line, column);
                    }
                    lexema += '\'';
                    avancar();
                    String ch = lexema;
                    lexema = "";
                    estadoAtual = ESTADO_INICIO;
                    return new Token(TokenType.CONST_CARACTERE, ch, tokenLine, tokenCol);
                }

                case ESTADO_STRING -> {
                    if (b == EOF) {
                        erroLexico("literal string não terminado", tokenLine, tokenCol);
                    }
                    if (b == (int) '"') {
                        lexema += '"';
                        avancar();
                        String str = lexema;
                        lexema = "";
                        estadoAtual = ESTADO_INICIO;
                        return new Token(TokenType.CONST_STRING, str, tokenLine, tokenCol);
                    }
                    if (b == (int) '\\') {
                        lexema += '\\';
                        avancar();
                        if (b == EOF) {
                            erroLexico("escape incompleto em string", line, column);
                        }
                        lexema += (char) b;
                        avancar();
                        break;
                    }
                    char c = (char) b;
                    if (c == '\n' || c == '\r') {
                        erroLexico("quebra de linha dentro de string", tokenLine, tokenCol);
                    }
                    lexema += c;
                    avancar();
                    break;
                }

                default -> throw new IllegalStateException("estado inválido: " + estadoAtual);
            }
        }
    }

    private String reconhecerOperador() {
        if (b == EOF) {
            return null;
        }
        if (pos < length) {
            String dois = "" + (char) b + source.charAt(pos);
            if (ts.pesquisaLexema(dois) && ts.buscar(dois).classe == TS.CLASSE_OPERADOR) {
                return dois;
            }
        }
        String um = String.valueOf((char) b);
        if (ts.pesquisaLexema(um) && ts.buscar(um).classe == TS.CLASSE_OPERADOR) {
            return um;
        }
        return null;
    }

    private void avancarOperador(int tamanho) {
        for (int i = 0; i < tamanho; i++) {
            avancar();
        }
    }

    private void ignorarIrrelevantes() {
        while (b != EOF) {
            char c = (char) b;

            if (c == ' ' || c == '\t' || c == '\r' || c == '\n' || c == '\0') {
                avancar();
                continue;
            }

            if (c == '/' && pos < length && source.charAt(pos) == '*') {
                int sl = line;
                int sc = column;
                avancar();
                avancar();

                boolean fechou = false;
                while (b != EOF) {
                    if (b == '*' && pos < length && source.charAt(pos) == '/') {
                        avancar();
                        avancar();
                        fechou = true;
                        break;
                    }
                    avancar();
                }

                if (!fechou) {
                    erroLexico("comentário bloco não fechado", sl, sc);
                }

                continue;
            }

            break;
        }
    }

    /**
     * Lê o próximo caractere e mantém {@code line}/{@code column} consistentes (base 1).
     * Após {@code \n} ou {@code \r\n}, {@code column} fica 1 para o início da linha seguinte.
     */
    private int lerProximo() {
        if (pos >= length) {
            return EOF;
        }

        char c = source.charAt(pos++);

        if (c == '\r') {
            if (pos < length && source.charAt(pos) == '\n') {
                pos++;
            }
            line++;
            lineStart = pos;
            column = 1;
            return '\n';
        }

        if (c == '\n') {
            line++;
            lineStart = pos;
            column = 1;
            return c;
        }

        column = pos - lineStart;
        return c;
    }

    private void avancar() {
        b = lerProximo();
    }

    private static boolean isAlpha(char c) {
        return Character.isLetter(c) || c == '_';
    }

    private static void erroLexico(String msg, int ln, int col) {
        throw new RuntimeException(msg + " (linha " + ln + ", coluna " + col + ")");
    }
}
