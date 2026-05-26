package src;
/**
 * Tipos de token e códigos numéricos (campo {@code token} na {@link TS}).
 */
public enum TokenType {
    // PALAVRAS RESERVADAS
    INTEIRO(1),
    LOGICO(2),
    CARACTERE(3),
    REAL(4),

    SE(5),
    ENQUANTO(6),
    LEITURA(7),
    ESCRITA(8),

    INICIO(9),
    FIM(10),

    VERDADEIRO(11),
    FALSO(12),

    SENAO(13),
    FACA(14),

    DIV(15),
    MOD(16),
    OU(17),

    // IDENTIFICADORES
    ID(20),

    // CONSTANTES
    CONST_INT(30),
    CONST_REAL(31),
    CONST_CARACTERE(32),
    CONST_STRING(33),

    // RELACIONAIS
    MENOR(40),
    MAIOR(41),
    DIF(42),
    MAIOR_IGUAL(43),
    MENOR_IGUAL(44),
    IGUAL(45),

    // ATRIBUIÇÃO
    ATRIB(50),

    // ARITMÉTICOS
    SOMA(60),
    SUB(61),
    MULT(62),
    DIVISAO(63),

    // LÓGICOS
    AND(70),

    // DELIMITADORES
    ABRE_PAR(80),
    FECHA_PAR(81),

    VIRGULA(82),
    PONTO_VIRGULA(83),
    DOIS_PONTOS(84),

    EOF(99);

	// Armazena o código numérico do token
    private final int codigo;

    // Construtor do enum.
    // Quando fazemos:
    // INTEIRO(1)
    // REAL(4)
    // o valor entre parênteses é recebido aqui.
    TokenType(int codigo) {
        this.codigo = codigo;
    }
    
    
     // Retorna o código numérico do token.
    // Exemplo:
    // TokenType.INTEIRO.codigo() -> 1
    public int codigo() {
        return codigo;
    }

    
     // Faz o caminho inverso:
    // recebe um código numérico e retorna o TokenType correspondente.
    // Exemplo:
    // deCodigo(1) -> INTEIRO
    // deCodigo(20) -> ID
    public static TokenType deCodigo(int codigo) {
    	// Percorre todos os valores do enum
        for (TokenType t : values()) {
        	// Verifica se o código do token atual é igual ao informado
            if (t.codigo == codigo) {
                return t;
            }
        }
        // Caso nenhum token possua o código informado,
        // lança erro.
        throw new IllegalArgumentException("código de token desconhecido: " + codigo);
    }
}
