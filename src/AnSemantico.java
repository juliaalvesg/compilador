package src;
import java.util.HashMap;
import java.util.Map;

public class AnSemantico {
    private final Map<String, String> tabelaTipos = new HashMap<>();
    public void declararVariavel(String nome, String tipo, int linha) {
        if (tabelaTipos.containsKey(nome)) {
            erroSemantico("variável '" + nome + "' já declarada (redeclaração)", linha);
        }
        tabelaTipos.put(nome, tipo.toLowerCase());
    }

    public void verificarUso(String nome, int linha) {
        if (!tabelaTipos.containsKey(nome)) {
            erroSemantico("variável '" + nome + "' não declarada", linha);
        }
    }
    public String getTipo(String nome) {
        return tabelaTipos.getOrDefault(nome, "desconhecido");
    }

    public void verificarCompatibilidade(String tipoEsq, String tipoDir,
                                         String op, int linha) {
        if (tipoEsq == null || tipoDir == null
                || tipoEsq.equals("desconhecido") || tipoDir.equals("desconhecido")) {
            return; // erro já foi reportado antes
        }
        if (tipoEsq.equals(tipoDir)) return;
        if (tipoEsq.equals("real") && tipoDir.equals("inteiro")) return; // promoção ok

        erroSemantico("tipos incompatíveis na operação '" + op
                + "': esperado '" + tipoEsq + "', encontrado '" + tipoDir + "'", linha);
    }

    public String verificarOperacaoAritmetica(String tipoEsq, String tipoDir,
                                               String op, int linha) {
        String opLower = op.toLowerCase();

        if (opLower.equals("ou") || opLower.equals("&&")) {
            if (!tipoEsq.equals("logico") || !tipoDir.equals("logico")) {
                erroSemantico("operação '" + op
                        + "' requer operandos do tipo 'logico', "
                        + "encontrado '" + tipoEsq + "' e '" + tipoDir + "'", linha);
            }
            return "logico";
        }

        if (opLower.equals("+")
                && tipoEsq.equals("caractere") && tipoDir.equals("caractere")) {
            return "caractere";
        }

        if (isNumerico(tipoEsq) && isNumerico(tipoDir)) {

            // div e mod exigem inteiro nos dois lados
            if (opLower.equals("div") || opLower.equals("mod")) {
                if (!tipoEsq.equals("inteiro") || !tipoDir.equals("inteiro")) {
                    erroSemantico("operação '" + op
                            + "' requer operandos do tipo 'inteiro'", linha);
                }
                return "inteiro";
            }

            if (tipoEsq.equals("real") || tipoDir.equals("real")) return "real";
            return "inteiro";
        }

        erroSemantico("operação '" + op
                + "' não suportada entre os tipos '"
                + tipoEsq + "' e '" + tipoDir + "'", linha);
        return "erro";
    }

    public void verificarOperacaoRelacional(String tipoEsq, String tipoDir,
                                             String op, int linha) {
        if (isNumerico(tipoEsq) && isNumerico(tipoDir)) return;

        // Mesmos tipos
        if (tipoEsq.equals(tipoDir)) {
            if (tipoEsq.equals("logico")) {
                if (!op.equals("==") && !op.equals("<>")) {
                    erroSemantico("tipo 'logico' só pode ser comparado com '==' ou '<>',"
                            + " operador '" + op + "' não permitido", linha);
                }
            }
            return;
        }

        erroSemantico("comparação '" + op
                + "' entre tipos incompatíveis: '"
                + tipoEsq + "' e '" + tipoDir + "'", linha);
    }

    public void verificarTipoLogico(String tipo, int linha) {
        if (!tipo.equals("logico")) {
            erroSemantico("condição deve ser do tipo 'logico',"
                    + " encontrado '" + tipo + "'", linha);
        }
    }

    public void imprimirTabelaTipos() {
        System.out.println("=== Tabela de Tipos (Semântico) ===");
        if (tabelaTipos.isEmpty()) {
            System.out.println("  (vazia)");
        } else {
            tabelaTipos.forEach((nome, tipo) ->
                    System.out.printf("  %-20s -> %s%n", nome, tipo));
        }
        System.out.println("===================================");
    }

    private boolean isNumerico(String tipo) {
        return "inteiro".equals(tipo) || "real".equals(tipo);
    }

    private void erroSemantico(String msg, int linha) {
        throw new RuntimeException(
                "[ERRO SEMANTICO] linha " + linha + ": " + msg);
    }
}