# Compilador BRL

Projeto da disciplina de Compiladores do curso de Ciência da Computação, Centro Universitário Dom Helder, 2026.

## Que problema resolve, e para quem

Um compilador transforma um programa escrito numa linguagem em algo que o computador executa. Para isso, ele precisa ler o código, entender a estrutura e encontrar os erros antes de rodar. Este projeto implementa essas etapas para **BRL**, uma linguagem com palavras-chave em português (`inicio`, `fim`, `se`, `senao`, `enquanto`, `faca`, `leitura`, `escrita`). Serve para estudantes que querem ver como um compilador funciona por dentro.

## O que o compilador faz

1. **Análise léxica:** lê o código-fonte e o separa em tokens (palavras reservadas, identificadores, números, textos e operadores). Também monta a tabela de símbolos.
2. **Análise sintática:** confere se os tokens seguem a gramática da linguagem.
3. **Análise semântica:** confere o significado do programa e aponta, com o número da linha:
   - variável usada sem ter sido declarada;
   - variável declarada duas vezes;
   - tipos incompatíveis em atribuições e operações;
   - condição de `se` ou `enquanto` que não é do tipo `logico`.

Tipos suportados: `inteiro`, `real`, `caractere` e `logico`.

A geração de código, que seria a etapa 3 de 3, ainda não foi implementada.

## Tecnologias

Java (sem bibliotecas externas).

## Como rodar

```bash
git clone https://github.com/juliaalvesg/compilador.git
cd compilador

javac -d out src/*.java
java -cp out src.Main Testes/programa.lc
```

Outros programas de teste ficam em `Testes/`. Por exemplo, o `erro.lc` mostra o compilador encontrando um erro:

```bash
java -cp out src.Main Testes/erro.lc
```

A saída mostra a lista de tokens, a tabela de símbolos e a tabela de tipos. Se houver erro, aparecem a mensagem e a linha onde ele está.

## Estrutura

| Arquivo | Função |
|---|---|
| `src/AnLexico.java` | Analisador léxico |
| `src/AnSintatico.java` | Analisador sintático |
| `src/AnSemantico.java` | Analisador semântico (tipos e declarações) |
| `src/TS.java` | Tabela de símbolos |
| `src/Token.java`, `src/TokenType.java` | Definição dos tokens |
| `src/Main.java` | Executa as etapas em ordem |

## Equipe e minha parte

Projeto em grupo: Ester Gonçalves, Júlia Garcia, Filipe Rabelo e Rafaela Gonçalves.

<!-- AJUSTE COM O QUE VOCÊ FEZ DE VERDADE -->
Minha parte: [descreva aqui, por exemplo: "implementei o analisador semântico e a tabela de símbolos e organizei a divisão de tarefas do grupo"].
