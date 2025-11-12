package codigo;

import java.util.ArrayList;
import java.util.Stack;

public class GeneradorCodigo {
    private ArrayList<String> codigoIntermedio;
    private ArrayList<String> codigoOptimizado;
    private int contadorEtiquetas;
    private Stack<String> pilaEtiquetas;
    private StringBuilder codigoActual;
    
    public GeneradorCodigo() {
        codigoIntermedio = new ArrayList<>();
        codigoOptimizado = new ArrayList<>();
        contadorEtiquetas = 0;
        pilaEtiquetas = new Stack<>();
        codigoActual = new StringBuilder();
    }
    
    public String nuevaEtiqueta() {
        return "L" + (contadorEtiquetas++);
    }
    
    // Genera una instrucción en AMBAS listas (original y optimizada)
    public void generar(String instruccion) {
        codigoIntermedio.add(instruccion);
        codigoOptimizado.add(instruccion);
    }
    
    // Ahora acepta la lista de objetos RPN y realiza el flujo Usar-Modificar-Reusar
    public void generarExpresion(ArrayList<ElementoExpresion> listaRPN) {
        // 1. Usar: convertir original a String
        String rpnOriginalStr = rpnToString(listaRPN);
        codigoIntermedio.add(rpnOriginalStr);

        // 2. Modificar in-place
        optimizarFolding(listaRPN);

        // 3. Reusar: convertir optimizada y añadir
        String rpnOptimizadaStr = rpnToString(listaRPN);
        codigoOptimizado.add(rpnOptimizadaStr);
    }
    
    public void iniciarIf() {
        String etiquetaElse = nuevaEtiqueta();
        String etiquetaFin = nuevaEtiqueta();
        pilaEtiquetas.push(etiquetaElse);
        pilaEtiquetas.push(etiquetaFin);
        
        // BRF: Branch if False (salta a else si condición es falsa)
        generar("BRF " + etiquetaElse);
    }
    
    public void generarThen() {
        String etiquetaFin = pilaEtiquetas.peek();
        generar("BRI " + etiquetaFin); // Salta al final después del then
        
        String etiquetaElse = pilaEtiquetas.get(pilaEtiquetas.size() - 2);
        generar(etiquetaElse + ":"); // Etiqueta para else
    }
    
    public void finalizarIf() {
        String etiquetaFin = pilaEtiquetas.pop();
        String etiquetaElse = pilaEtiquetas.pop();
        generar(etiquetaFin + ":"); // Etiqueta fin
    }
    
    public void iniciarWhile() {
        String etiquetaInicio = nuevaEtiqueta();
        String etiquetaFin = nuevaEtiqueta();
        
        generar(etiquetaInicio + ":"); // Etiqueta inicio del while
        pilaEtiquetas.push(etiquetaInicio);
        pilaEtiquetas.push(etiquetaFin);
    }
    
    public void generarCondicionWhile() {
        String etiquetaFin = pilaEtiquetas.peek();
        generar("BRF " + etiquetaFin); // Si condición falsa, salir
    }
    
    public void finalizarWhile() {
        String etiquetaFin = pilaEtiquetas.pop();
        String etiquetaInicio = pilaEtiquetas.pop();
        generar("BRI " + etiquetaInicio); // Volver al inicio
        generar(etiquetaFin + ":"); // Etiqueta fin del while
    }
    
    // Compatibilidad: mantener versión que acepta String
    public void generarAsignacion(String variable, String expresionRPN) {
        generar(variable + " " + expresionRPN + " =");
    }
    
    // Nueva versión: acepta la lista de objetos RPN
    public void generarAsignacion(String variable, ArrayList<ElementoExpresion> listaRPN) {
        // 1. Usar: convertir original a String
        String rpnOriginalStr = rpnToString(listaRPN);
        codigoIntermedio.add(variable + " " + rpnOriginalStr + " =");

        // 2. Modificar in-place
        optimizarFolding(listaRPN);

        // 3. Reusar: convertir optimizada y añadir
        String rpnOptimizadaStr = rpnToString(listaRPN);
        codigoOptimizado.add(variable + " " + rpnOptimizadaStr + " =");
    }
    
    public void generarPrint(String valor) {
        generar(valor + " PRT");
    }
    
    public void generarScanner(String variable) {
        generar(variable + " SCN");
    }
    
    public ArrayList<String> getCodigoIntermedio() {
        return codigoIntermedio;
    }

    public String getCodigoOptimizadoComoString() {
        StringBuilder sb = new StringBuilder();
        for (String linea : codigoOptimizado) {
            sb.append(linea).append("\n");
        }
        return sb.toString();
    }
    
    public void limpiar() {
        codigoIntermedio.clear();
        codigoOptimizado.clear();
        contadorEtiquetas = 0;
        pilaEtiquetas.clear();
        codigoActual.setLength(0);
    }
    
    public String getCodigoComoString() {
        StringBuilder sb = new StringBuilder();
        for (String linea : codigoIntermedio) {
            sb.append(linea).append("\n");
        }
        return sb.toString();
    }

    // -------------------- Helpers para optimización --------------------
    private void optimizarFolding(ArrayList<ElementoExpresion> lista) {
        boolean huboCambios = true;
        while (huboCambios) {
            huboCambios = false;
            for (int i = 0; i + 2 < lista.size(); i++) {
                ElementoExpresion e1 = lista.get(i);
                ElementoExpresion e2 = lista.get(i + 1);
                ElementoExpresion e3 = lista.get(i + 2);

                // Buscamos patrón OPERANDO, OPERANDO, OPERADOR
                if (e1.esOperando() && e2.esOperando() && e3.esOperador()) {
                    String op = e3.valor;
                    int tipo1 = e1.tipoDato;
                    int tipo2 = e2.tipoDato;

                    // Solo operar números por ahora (207=int, 208=float)
                    if ((tipo1 == 207 || tipo1 == 208) && (tipo2 == 207 || tipo2 == 208)) {
                        try {
                            double v1 = Double.parseDouble(e1.valor);
                            double v2 = Double.parseDouble(e2.valor);
                            String resultadoStr = "0";
                            int tipoResultado = 207; // por defecto int

                            // Si alguno es float, mantener float
                            if (tipo1 == 208 || tipo2 == 208) {
                                tipoResultado = 208;
                            }

                            switch (op) {
                                case "+":
                                    if (tipoResultado == 208) resultadoStr = String.valueOf(v1 + v2);
                                    else resultadoStr = String.valueOf((long)(v1 + v2));
                                    break;
                                case "-":
                                    if (tipoResultado == 208) resultadoStr = String.valueOf(v1 - v2);
                                    else resultadoStr = String.valueOf((long)(v1 - v2));
                                    break;
                                case "*":
                                    if (tipoResultado == 208) resultadoStr = String.valueOf(v1 * v2);
                                    else resultadoStr = String.valueOf((long)(v1 * v2));
                                    break;
                                case "/":
                                    double div = v1 / v2;
                                    if (tipoResultado == 207 && v1 % v2 == 0) {
                                        resultadoStr = String.valueOf((long)div);
                                    } else {
                                        resultadoStr = String.valueOf(div);
                                        tipoResultado = 208;
                                    }
                                    break;
                                default:
                                    // operador no soportado para folding
                                    continue;
                            }

                            // Crear nuevo ElementoExpresion con resultado
                            ElementoExpresion nuevo = new ElementoExpresion(ElementoExpresion.TIPO_OPERANDO, tipoResultado, resultadoStr, e1.linea);

                            // Reemplazar 3 elementos por 1
                            lista.remove(i); // e1
                            lista.remove(i); // e2 (ahora en i)
                            lista.remove(i); // e3 (ahora en i)
                            lista.add(i, nuevo);
                            huboCambios = true;
                            break; // reiniciar el barrido
                        } catch (NumberFormatException ex) {
                            // No se puede parsear, omitir
                        }
                    }
                }
            }
        }
    }

    private String rpnToString(ArrayList<ElementoExpresion> lista) {
        StringBuilder sb = new StringBuilder();
        for (ElementoExpresion e : lista) {
            sb.append(e.valor).append(" ");
        }
        return sb.toString().trim();
    }

}
