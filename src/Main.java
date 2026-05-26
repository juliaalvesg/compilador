package src;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class Main {

    public static void main(String[] args) throws IOException {

        Path pathEntrada = args.length >= 1
                ? Path.of(args[0])
                : Path.of("exemplos", "programa.lc");

        Path pathSaida = args.length >= 2
                ? Path.of(args[1])
                : Path.of("exemplos", "programa.asm");

        String source = Files.readString(pathEntrada);

        System.out.println("=== Compilador BRL ===");
        System.out.println("Arquivo fonte : " + pathEntrada);
        System.out.println("Arquivo saída : " + pathSaida);
        System.out.println();

        try {
            System.out.println("[1/3] Análise léxica...");
            AnLexico lexico = new AnLexico(source);
            AnLexico lexicoDebug = new AnLexico(source);
            List<Token> tokens = lexicoDebug.analisar();
            for (Token t : tokens) {
                System.out.println("  " + t);
            }

            System.out.println("\n  --- Tabela de Símbolos (identificadores) ---");
            lexicoDebug.tabelaSimbolos().entradas().values().stream()
                    .filter(e -> e.classe == TS.CLASSE_ID)
                    .forEach(e -> System.out.printf("  ID = '%s'%n", e.lexema));

            System.out.println("\n  Análise léxica: OK");
            System.out.println("\n[2/3] Análise sintática e semântica...");

            AnSemantico semantico = new AnSemantico();
            AnSintatico sintatico = new AnSintatico(lexico, semantico);
            sintatico.analisar();

            System.out.println();
            semantico.imprimirTabelaTipos();

            System.out.println("\n  Análise sintática e semântica: OK");

            System.out.println("\n[3/3] Geração de código: (a implementar)");

        } catch (RuntimeException e) {
            System.err.println("\n" + e.getMessage());
            System.exit(1);
        }
    }
}