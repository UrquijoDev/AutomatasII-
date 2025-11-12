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
    
  
    public void generarExpresion(ArrayList<ElementoExpresion> listaRPN) {
        // 1. Usar: convertir original a String
        String rpnOriginalStr = rpnToString(listaRPN);
        codigoIntermedio.add(rpnOriginalStr);

    // 2. Modificar in-place (plegado de constantes + simplificaciones)
    optimizarRPN(listaRPN);

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

    // 2. Modificar in-place (plegado de constantes + simplificaciones)
    optimizarRPN(listaRPN);

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
    // Ahora: optimizarRPN realiza dos fases: simplificación de identidades algebraicas
    // y luego plegado de constantes. Modifica la lista in-place.
    private void optimizarRPN(ArrayList<ElementoExpresion> lista) {
        boolean huboCambios = true;
        while (huboCambios) {
            huboCambios = false;
            for (int i = 0; i + 2 < lista.size(); i++) {
                ElementoExpresion e1 = lista.get(i);
                ElementoExpresion e2 = lista.get(i + 1);
                ElementoExpresion e3 = lista.get(i + 2);
                // --- Primera fase: simplificación de identidades algebraicas ---
                // Patrón: [X, 0, +] => [X]
                //          [X, 0, -] => [X]
                //          [X, 1, *] => [X]
                //          [X, 0, *] => [0]  (Elemento absorbente)
                // Donde X puede ser variable (TIPO_VARIABLE) o operando (TIPO_OPERANDO)
                if ((e1.tipoElemento == ElementoExpresion.TIPO_VARIABLE || e1.tipoElemento == ElementoExpresion.TIPO_OPERANDO)
                        && e2.esOperando() && e3.esOperador()) {
                    String op = e3.valor;
                    // Verificar constantes 0 o 1 en e2 (esperamos que el token entero sea "0" o "1" y tipoDato 207)
                    boolean e2IsZero = (e2.tipoDato == 207 && ("0".equals(e2.valor) || "0.0".equals(e2.valor)));
                    boolean e2IsOne = (e2.tipoDato == 207 && ("1".equals(e2.valor) || "1.0".equals(e2.valor)));

                    // Identidad aditiva: X 0 + -> X
                    if (e2IsZero && "+".equals(op)) {
                        // eliminar los elementos e2 y e3, conservar e1
                        lista.remove(i + 2); // e3
                        lista.remove(i + 1); // e2
                        huboCambios = true;
                        break; // reiniciar
                    }

                    // Identidad sustractiva: X 0 - -> X
                    if (e2IsZero && "-".equals(op)) {
                        lista.remove(i + 2);
                        lista.remove(i + 1);
                        huboCambios = true;
                        break;
                    }

                    // Elemento neutro multiplicativo: X 1 * -> X
                    if (e2IsOne && "*".equals(op)) {
                        lista.remove(i + 2);
                        lista.remove(i + 1);
                        huboCambios = true;
                        break;
                    }

                    // Elemento absorbente multiplicativo: X 0 * -> 0
                    if (e2IsZero && "*".equals(op)) {
                        // Reemplazar los tres elementos por un operando 0 (tipoDato 207)
                        ElementoExpresion cero = new ElementoExpresion(ElementoExpresion.TIPO_OPERANDO, 207, "0", e2.linea);
                        lista.remove(i); // e1
                        lista.remove(i); // e2 (ahora en i)
                        lista.remove(i); // e3 (ahora en i)
                        lista.add(i, cero);
                        huboCambios = true;
                        break;
                    }
                }

                // --- Segunda fase: plegado de constantes (si aplica) ---
                // Buscamos patrón OPERANDO, OPERANDO, OPERADOR
                if (e1.esOperando() && e2.esOperando() && e3.esOperador()) {
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

                            switch (e3.valor) {
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
