package codigo;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.IOException;
import java.util.ArrayList;

public class GeneradorASM {

    private int labelCounter = 0;

    public void generarArchivoASM(String nombreArchivo, ArrayList<String> codigoOptimizado, NodoVar cabezaVar) {
        if (!nombreArchivo.endsWith(".asm")) {
            nombreArchivo += ".asm";
        }

        try (FileWriter fw = new FileWriter(nombreArchivo);
                PrintWriter pw = new PrintWriter(fw)) {
            
            //Encabezados 
            pw.println("; Archivo generado por el Compilador");
            pw.println("INCLUDE MACROS.MAC");
            pw.println("DOSSEG");
            pw.println(".MODEL SMALL");
            pw.println(".STACK 100h");


            //Segmento de datos
            pw.println(".DATA");
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

            pw.println("\n\t; --- Variables del Usuario ---");
            NodoVar actual = cabezaVar;
            while (actual != null) {
                // Si es String arreglo de bytes grande.
                // Si es número necesitamos 2 bytes.
                if (actual.tipo == 210) { 
                    pw.println("\t" + actual.nombre + " DB 255 DUP('$')");
                } else {
                    pw.println("\t" + actual.nombre + " DW 0");
                }
                actual = actual.sig;
            }

            //Segmento de codigo
            pw.println("\n.CODE");
            pw.println(".386");
            pw.println("BEGIN:");
            pw.println("\tMOV AX, @DATA");
            pw.println("\tMOV DS, AX");

            // Recorremos el código intermedio 
            for (String linea : codigoOptimizado) {
                pw.println("\n\t; " + linea);
                String[] tokens = linea.split(" ");

                
                //Asignaciones
                if (linea.endsWith("=")) {
                    String variableDestino = tokens[0];
                    
                    boolean esAsignacionString = false;
                    String valorString = "";
                    
                    // Detectamos si asignamos texto 
                    for(String t : tokens) {
                        if(t.startsWith("\"") || t.startsWith("'")) {
                            esAsignacionString = true;
                            //espacios intermedios
                            int inicio = linea.indexOf(t);
                            int fin = linea.lastIndexOf("=");
                            valorString = linea.substring(inicio, fin).trim();
                            break;
                        }
                    }

                    if (esAsignacionString) {
                        //Asignación de String.
                        String textoLimpio = valorString.replace("\"", "").replace("'", "");
                        pw.println("\t; Asignando string letra por letra a " + variableDestino);
                        
                        for (int i = 0; i < textoLimpio.length(); i++) {
                            char c = textoLimpio.charAt(i);
                            pw.println("\tMOV " + variableDestino + "[" + i + "], '" + c + "'");
                        }
                        //Marcar el final del string con '$'
                        pw.println("\tMOV " + variableDestino + "[" + textoLimpio.length() + "], '$'");
                    } 
                    else {
                        //Asignación numérica normal
                        for (int i = 1; i < tokens.length - 1; i++) {
                            procesarToken(tokens[i], pw);
                        }
                        pw.println("\tPOP AX");
                        pw.println("\tMOV " + variableDestino + ", AX");
                    }
                }
                
                // print 
                else if (linea.endsWith("PRT")) {
                    String valor = tokens[0]; 

                    //Es texto 
                    if (valor.startsWith("\"") || valor.startsWith("'")) {
                         String textoLimpio = linea.substring(0, linea.lastIndexOf("PRT")).trim().replace("\"", "").replace("'", "");
                         // Imprimimos caracter por caracter 
                         for (int i = 0; i < textoLimpio.length(); i++) {
                            pw.println("\tMOV AH, 02h");
                            pw.println("\tMOV DL, '" + textoLimpio.charAt(i) + "'");
                            pw.println("\tINT 21h");
                         }
                         pw.println("\tWRITELN");
                    }
                    //saber si es String o Numero
                    else if (!esNumero(valor)) {
                        int tipo = obtenerTipoVar(valor, cabezaVar);
                        
                        if (tipo == 210) { //Es variable string
                            pw.println("\t; Imprimiendo variable string");
                            pw.println("\tLEA DX, " + valor); // Cargamos la dirección de memoria
                            pw.println("\tMOV AH, 09h");      // Usamos función 09h Imprimir cadena
                            pw.println("\tINT 21h");
                            pw.println("\tWRITELN");
                        } else {
                            //variable NUMÉRICA 
                            pw.println("\tMOV AX, " + valor);
                            pw.println("\tITOA BUFFER, AX");
                            pw.println("\tWRITE BUFFER");
                            pw.println("\tWRITELN");
                        }
                    }
                    // Es un número
                    else {
                        pw.println("\tMOV AX, " + valor);
                        pw.println("\tITOA BUFFER, AX");
                        pw.println("\tWRITE BUFFER");
                        pw.println("\tWRITELN");
                    }
                }
                // scanner
                else if (linea.endsWith("SCN")) {
                    String variable = tokens[0];
                    pw.println("\tREAD");
                    pw.println("\tATOI " + variable);
                }
                //Etiquetas y Saltos
                else if (linea.endsWith(":")) {
                    pw.println(linea);
                } else if (linea.startsWith("BRF")) {
                    pw.println("\tPOP AX");
                    pw.println("\tCMP AX, 0");
                    pw.println("\tJE " + tokens[1]);
                } else if (linea.startsWith("BRI")) {
                    pw.println("\tJMP " + tokens[1]);
                }
                //EXPRESIONES MATEMÁTICAS
                else {
                    for (String token : tokens) {
                        procesarToken(token, pw);
                    }
                }
            }

            // Fin
            pw.println("\n\tMOV AX, 4C00h");
            pw.println("\tINT 21h");
            pw.println("END BEGIN");

            System.out.println("Archivo ASM generado: " + nombreArchivo);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    // buscar el tipo de dato 
    private int obtenerTipoVar(String nombre, NodoVar cabeza) {
        NodoVar actual = cabeza;
        while(actual != null) {
            if(actual.nombre.equals(nombre)) {
                return actual.tipo;
            }
            actual = actual.sig;
        }
        return -1; 
    }

    // Método para procesar operaciones matemáticas
    private void procesarToken(String token, PrintWriter pw) {
        
        //traducimos a enteros.
        if (token.equals("true")) token = "1";
        else if (token.equals("false")) token = "0";

        if (token.matches("-?\\d+\\.\\d+")) {
            token = token.substring(0, token.indexOf(".")); // Truncar decimales
        }

        if (token.startsWith("\"") || token.startsWith("'")) return;

        // Traducción de operadores 
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
        // Operadores Relacionales
        else if (token.equals(">")) { comparar(pw, "JG"); }
        else if (token.equals("<")) { comparar(pw, "JL"); }
        else if (token.equals("==")) { comparar(pw, "JE"); }
        else if (token.equals("!=")) { comparar(pw, "JNE"); }
        // Operandos van a la pila
        else if (esNumero(token)) {
            pw.println("\tMOV AX, " + token);
            pw.println("\tPUSH AX");
        } else if (!token.equals("=") && !token.equals("PRT") && !token.equals("SCN") && !token.startsWith("L")) {
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