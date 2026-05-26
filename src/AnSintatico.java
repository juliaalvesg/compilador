package src;
import java.util.EnumSet;
import java.util.Set;

public class AnSintatico {

    private static final Set<TokenType> TIPOS_VAR = EnumSet.of(
            TokenType.INTEIRO,
            TokenType.LOGICO,
            TokenType.CARACTERE,
            TokenType.REAL
    );
    private static final Set<TokenType> RELOPS = EnumSet.of(
            TokenType.IGUAL,
            TokenType.DIF,
            TokenType.MENOR,
            TokenType.MENOR_IGUAL,
            TokenType.MAIOR,
            TokenType.MAIOR_IGUAL
    );
    private static final Set<TokenType> ADDOPS = EnumSet.of(
            TokenType.SOMA,
            TokenType.SUB,
            TokenType.OU
    );
    private static final Set<TokenType> MULOPS = EnumSet.of(
            TokenType.MULT,
            TokenType.DIVISAO,
            TokenType.DIV,
            TokenType.MOD,
            TokenType.AND
    );
    private final AnLexico lexico;
    private final AnSemantico semantico;
    private Token tokenAtual;

    public AnSintatico(AnLexico lexico, AnSemantico semantico) {
        this.lexico    = lexico;
        this.semantico = semantico;
    }

    public void analisar() {
        proximoToken();
        parsePrograma();
        if (!verificar(TokenType.EOF)) {
            erroSintatico("tokens inesperados após o fim do programa");
        }
        System.out.println("Análise sintática concluída com sucesso.");
    }

    private void proximoToken() {
        tokenAtual = lexico.proximoToken();
    }

    private void consumir(TokenType tipo) {
        if (tokenAtual.type() == tipo) {
            proximoToken();
        } else {
            erroSintatico("esperado '" + tipo + "', encontrado '"
                    + tokenAtual.lexeme() + "'");
        }
    }

    private boolean verificar(TokenType tipo) {
        return tokenAtual.type() == tipo;
    }

    private boolean verificarQualquer(Set<TokenType> conjunto) {
        return conjunto.contains(tokenAtual.type());
    }

    private void erroSintatico(String msg) {
        throw new RuntimeException("[ERRO SINTATICO] linha "
                + tokenAtual.line() + ", coluna " + tokenAtual.column()
                + ": " + msg);
    }

    private void parsePrograma() {
        consumir(TokenType.INICIO);
        consumir(TokenType.ID);
        consumir(TokenType.PONTO_VIRGULA);

        while (verificarQualquer(TIPOS_VAR)) {
            parseDeclaracao();
        }

        while (!verificar(TokenType.FIM) && !verificar(TokenType.EOF)) {
            parseComando();
        }

        consumir(TokenType.FIM);
    }

    private void parseDeclaracao() {
        String nomeTipo = tokenAtual.lexeme();
        proximoToken(); 

        String nomeVar = tokenAtual.lexeme();
        int linhaVar   = tokenAtual.line();
        consumir(TokenType.ID);
        semantico.declararVariavel(nomeVar, nomeTipo, linhaVar);

        if (verificar(TokenType.ATRIB)) {
            parseInicializacao(nomeVar, nomeTipo);
        }

        while (verificar(TokenType.VIRGULA)) {
            consumir(TokenType.VIRGULA);
            nomeVar = tokenAtual.lexeme();
            linhaVar = tokenAtual.line();
            consumir(TokenType.ID);
            semantico.declararVariavel(nomeVar, nomeTipo, linhaVar);
            if (verificar(TokenType.ATRIB)) {
                parseInicializacao(nomeVar, nomeTipo);
            }
        }

        consumir(TokenType.PONTO_VIRGULA);
    }

    private void parseInicializacao(String nomeVar, String tipoVar) {
        consumir(TokenType.ATRIB);
        int linha = tokenAtual.line();

        if (verificar(TokenType.ID)) {
            String idDir = tokenAtual.lexeme();
            semantico.verificarUso(idDir, linha);
            String tipoDir = semantico.getTipo(idDir);
            semantico.verificarCompatibilidade(tipoVar, tipoDir, ":=", linha);
            consumir(TokenType.ID);

        } else if (isConstante()) {
            String tipoConst = tipoConstante();
            semantico.verificarCompatibilidade(tipoVar, tipoConst, ":=", linha);
            proximoToken();

        } else {
            erroSintatico("esperado ID ou constante após ':='");
        }
    }

    private void parseComando() {
        if      (verificar(TokenType.SE))       parseComandoSe();
        else if (verificar(TokenType.ENQUANTO)) parseComandoEnquanto();
        else if (verificar(TokenType.LEITURA))  parseComandoLeitura();
        else if (verificar(TokenType.ESCRITA))  parseComandoEscrita();
        else if (verificar(TokenType.ID))       parseAtribuicao();
        else if (verificarQualquer(TIPOS_VAR))  parseDeclaracao();
        else erroSintatico("comando inválido: '" + tokenAtual.lexeme() + "'");
    }

    private void parseComandoSe() {
        int linha = tokenAtual.line();
        consumir(TokenType.SE);

        String tipoExp = parseExpressao();
        semantico.verificarTipoLogico(tipoExp, linha);

        if (!verificar(TokenType.ID) || !tokenAtual.lexeme().equals("entao")) {
            erroSintatico("esperado 'entao' após condição do 'se'");
        }
        proximoToken();

        consumir(TokenType.INICIO);
        while (!verificar(TokenType.FIM)
                && !verificar(TokenType.SENAO)
                && !verificar(TokenType.EOF)) {
            parseComando();
        }
        consumir(TokenType.FIM);

        if (verificar(TokenType.SENAO)) {
            consumir(TokenType.SENAO);
            consumir(TokenType.INICIO);
            while (!verificar(TokenType.FIM) && !verificar(TokenType.EOF)) {
                parseComando();
            }
            consumir(TokenType.FIM);
        }
    }

    private void parseComandoEnquanto() {
        int linha = tokenAtual.line();
        consumir(TokenType.ENQUANTO);

        String tipoExp = parseExpressao();
        semantico.verificarTipoLogico(tipoExp, linha);

        consumir(TokenType.FACA);
        consumir(TokenType.INICIO);
        while (!verificar(TokenType.FIM) && !verificar(TokenType.EOF)) {
            parseComando();
        }
        consumir(TokenType.FIM);
    }

    private void parseComandoLeitura() {
        consumir(TokenType.LEITURA);
        consumir(TokenType.ABRE_PAR);

        int linha = tokenAtual.line();
        String id = tokenAtual.lexeme();
        consumir(TokenType.ID);
        semantico.verificarUso(id, linha);

        while (verificar(TokenType.VIRGULA)) {
            consumir(TokenType.VIRGULA);
            linha = tokenAtual.line();
            id    = tokenAtual.lexeme();
            consumir(TokenType.ID);
            semantico.verificarUso(id, linha);
        }

        consumir(TokenType.FECHA_PAR);
        consumir(TokenType.PONTO_VIRGULA);
    }

    private void parseComandoEscrita() {
        consumir(TokenType.ESCRITA);
        consumir(TokenType.ABRE_PAR);
        parseExpressao();
        consumir(TokenType.FECHA_PAR);
        consumir(TokenType.PONTO_VIRGULA);
    }

    private void parseAtribuicao() {
        int linha    = tokenAtual.line();
        String idEsq = tokenAtual.lexeme();
        consumir(TokenType.ID);
        semantico.verificarUso(idEsq, linha);

        consumir(TokenType.ATRIB);

        String tipoExp = parseExpressao();
        String tipoVar = semantico.getTipo(idEsq);
        semantico.verificarCompatibilidade(tipoVar, tipoExp, ":=", linha);

        consumir(TokenType.PONTO_VIRGULA);
    }

    private String parseExpressao() {
        String tipoEsq = parseExps();

        if (verificarQualquer(RELOPS)) {
            String op   = tokenAtual.lexeme();
            int linha   = tokenAtual.line();
            proximoToken();
            String tipoDir = parseExps();
            semantico.verificarOperacaoRelacional(tipoEsq, tipoDir, op, linha);
            return "logico";
        }
        return tipoEsq;
    }

    private String parseExps() {
        if (verificar(TokenType.SOMA) || verificar(TokenType.SUB)) {
            proximoToken();
        }

        String tipoRes = parseTermo();

        while (verificarQualquer(ADDOPS)) {
            String op  = tokenAtual.lexeme();
            int linha  = tokenAtual.line();
            proximoToken();
            String tipoDir = parseTermo();
            tipoRes = semantico.verificarOperacaoAritmetica(tipoRes, tipoDir, op, linha);
        }
        return tipoRes;
    }

    private String parseTermo() {
        String tipoRes = parseFator();

        while (verificarQualquer(MULOPS)) {
            String op  = tokenAtual.lexeme();
            int linha  = tokenAtual.line();
            proximoToken();
            String tipoDir = parseFator();
            tipoRes = semantico.verificarOperacaoAritmetica(tipoRes, tipoDir, op, linha);
        }
        return tipoRes;
    }

    private String parseFator() {
        int linha = tokenAtual.line();

        if (verificar(TokenType.ID)) {
            String id = tokenAtual.lexeme();
            semantico.verificarUso(id, linha);
            String tipo = semantico.getTipo(id);
            consumir(TokenType.ID);
            return tipo;

        } else if (verificar(TokenType.CONST_INT)) {
            consumir(TokenType.CONST_INT);
            return "inteiro";

        } else if (verificar(TokenType.CONST_REAL)) {
            consumir(TokenType.CONST_REAL);
            return "real";

        } else if (verificar(TokenType.CONST_CARACTERE)
                || verificar(TokenType.CONST_STRING)) {
            proximoToken();
            return "caractere";

        } else if (verificar(TokenType.VERDADEIRO) || verificar(TokenType.FALSO)) {
            proximoToken();
            return "logico";

        } else if (verificar(TokenType.ABRE_PAR)) {
            consumir(TokenType.ABRE_PAR);
            String tipo = parseExpressao();
            consumir(TokenType.FECHA_PAR);
            return tipo;

        } else {
            erroSintatico("fator inválido: '" + tokenAtual.lexeme() + "'");
            return "erro";
        }
    }

    private boolean isConstante() {
        return verificar(TokenType.CONST_INT)
                || verificar(TokenType.CONST_REAL)
                || verificar(TokenType.CONST_CARACTERE)
                || verificar(TokenType.CONST_STRING)
                || verificar(TokenType.VERDADEIRO)
                || verificar(TokenType.FALSO);
    }

    private String tipoConstante() {
        if (verificar(TokenType.CONST_INT))                              return "inteiro";
        if (verificar(TokenType.CONST_REAL))                             return "real";
        if (verificar(TokenType.CONST_CARACTERE)
                || verificar(TokenType.CONST_STRING))                    return "caractere";
        if (verificar(TokenType.VERDADEIRO) || verificar(TokenType.FALSO)) return "logico";
        return "desconhecido";
    }
}