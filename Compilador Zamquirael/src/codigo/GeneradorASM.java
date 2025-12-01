package codigo;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.IOException;
import java.util.ArrayList;

public class GeneradorASM {

    // Variables globales para etiquetas
    private int labelCounter = 0;

    public void generarArchivoASM(String nombreArchivo, ArrayList<String> codigoOptimizado, NodoVar cabezaVar) {
        // Agregamos .asm al nombre si no lo tiene
        if (!nombreArchivo.endsWith(".asm")) {
            nombreArchivo += ".asm";
        }

        try (FileWriter fw = new FileWriter(nombreArchivo);
                PrintWriter pw = new PrintWriter(fw)) {
            pw.println("; Archivo generado por el Compilador");
            pw.println("INCLUDE MACROS.MAC"); // OJO: Extension correcta .MAC
            pw.println("DOSSEG");
            pw.println(".MODEL SMALL");
            pw.println(".STACK 100h");

            // 2. SEGMENTO DE DATOS

            pw.println(".DATA");

            // A) VARIABLES DE SISTEMA 
            pw.println("\t; --- Variables internas para Macros ---");
            pw.println("\tBUFFER      DB 8 DUP('$')");
            pw.println("\tBUFFERTEMP  DB 8 DUP('$')");
            pw.println("\tBLANCO      DB '#'");
            pw.println("\tBLANCOS     DB '$'");
            pw.println("\tMENOS       DB '-$'");
            pw.println("\tCOUNT       DW 0");
            pw.println("\tNEGATIVO    DB 0");
            pw.println("\tLISTAPAR    LABEL BYTE");
            pw.println("\tLONGMAX     DB 254");
            pw.println("\tTOTCAR      DB ?");
            pw.println("\tINTRODUCIDOS DB 254 DUP ('$')");
            pw.println("\tMULT10      DW 1");

            // B) VARIABLES DE USUARIO
            pw.println("\n\t; --- Variables del Usuario ---");
            NodoVar actual = cabezaVar;
            while (actual != null) {
                // Declaramos todas como DW en 0
                pw.println("\t" + actual.nombre + " DW 0");
                actual = actual.sig;
            }

            // 3. SEGMENTO DE CODIGO
            pw.println("\n.CODE");
            pw.println(".386");
            pw.println("BEGIN:");
            pw.println("\tMOV AX, @DATA");
            pw.println("\tMOV DS, AX");

            // 4. PROCESAMIENTO DEL CODIGO (RPN)
            for (String linea : codigoOptimizado) {
                pw.println("\n\t; " + linea); // Comentario para debug

                String[] tokens = linea.split(" ");

                // Analisis por tipo de instruccion
                // CASO 1: ASIGNACION
                if (linea.endsWith("=")) {
                    String variableDestino = tokens[0]; 

                    // Procesamos lo de en medio si hay expresion
                    for (int i = 1; i < tokens.length - 1; i++) {
                        procesarToken(tokens[i], pw);
                    }

                    pw.println("\tPOP AX"); 
                    pw.println("\tMOV " + variableDestino + ", AX"); 
                }

                // CASO 2: PRINT
                else if (linea.endsWith("PRT")) {
                    String valor = tokens[0];

                    // Si es variable o numero, lo movemos a AX
                    if (esNumero(valor)) {
                        pw.println("\tMOV AX, " + valor);
                    } else {
                        pw.println("\tMOV AX, " + valor);
                    }

                    
                    pw.println("\tITOA BUFFER, AX"); // Convierte AX a texto en BUFFER
                    pw.println("\tWRITE BUFFER");
                    pw.println("\tWRITELN"); // 
                }

                // CASO 3: SCANNER ( ... SCN )
                else if (linea.endsWith("SCN")) {
                    String variable = tokens[0];
                    // Usamos Macros de blue.asm
                    pw.println("\tREAD"); // Lee teclado
                    pw.println("\tATOI " + variable); // Convierte a numero y guarda en variable
                }

                // CASO 4: ETIQUETAS Y SALTOS
                else if (linea.endsWith(":")) {
                    pw.println(linea); // L1:
                } else if (linea.startsWith("BRF")) { // Branch if False
                    pw.println("\tPOP AX");
                    pw.println("\tCMP AX, 0");
                    pw.println("\tJE " + tokens[1]); // Salta si es 0 (Falso)
                } else if (linea.startsWith("BRI")) { // Salto incondicional
                    pw.println("\tJMP " + tokens[1]);
                }

                // CASO 5: EXPRESIONES NORMALES (5 3 +)
                else {
                    for (String token : tokens) {
                        procesarToken(token, pw);
                    }
                }
            }

            // 5. CIERRE
            pw.println("\n\tMOV AX, 4C00h");
            pw.println("\tINT 21h");
            pw.println("END BEGIN");

            System.out.println("Archivo ASM generado: " + nombreArchivo);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Metodo auxiliar para procesar operadores y operandos
    private void procesarToken(String token, PrintWriter pw) {
        if (token.equals("+")) {
            pw.println("\tPOP BX");
            pw.println("\tPOP AX");
            pw.println("\tADD AX, BX");
            pw.println("\tPUSH AX");
        } else if (token.equals("-")) {
            pw.println("\tPOP BX");
            pw.println("\tPOP AX");
            pw.println("\tSUB AX, BX");
            pw.println("\tPUSH AX");
        } else if (token.equals("*")) {
            pw.println("\tPOP BX");
            pw.println("\tPOP AX");
            pw.println("\tIMUL BX");
            pw.println("\tPUSH AX");
        } else if (token.equals("/")) {
            pw.println("\tPOP BX");
            pw.println("\tPOP AX");
            pw.println("\tCWD"); 
            pw.println("\tIDIV BX");
            pw.println("\tPUSH AX");
        }
        // Operadores Relacionales (Producen 1 o 0)
        else if (token.equals(">")) {
            comparar(pw, "JG");
        } else if (token.equals("<")) {
            comparar(pw, "JL");
        } else if (token.equals("==")) {
            comparar(pw, "JE");
        } else if (token.equals("!=")) {
            comparar(pw, "JNE");
        }
        // Numeros y Variables
        else if (esNumero(token)) {
            pw.println("\tMOV AX, " + token);
            pw.println("\tPUSH AX");
        } else if (!token.equals("=") && !token.equals("PRT") && !token.equals("SCN") && !token.startsWith("L")) {
            // Asumimos que es variable si no es palabra reservada
            pw.println("\tMOV AX, " + token);
            pw.println("\tPUSH AX");
        }
    }

    private void comparar(PrintWriter pw, String salto) {
        int id = labelCounter++;
        pw.println("\tPOP BX");
        pw.println("\tPOP AX");
        pw.println("\tCMP AX, BX");
        pw.println("\t" + salto + " TRUE_" + id);
        pw.println("\tMOV AX, 0");
        pw.println("\tJMP END_" + id);
        pw.println("TRUE_" + id + ":");
        pw.println("\tMOV AX, 1");
        pw.println("END_" + id + ":");
        pw.println("\tPUSH AX");
    }

    private boolean esNumero(String str) {
        return str.matches("-?\\d+(\\.\\d+)?");
    }
}