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
    

    public void generar(String instruccion) {
        codigoIntermedio.add(instruccion);
        codigoOptimizado.add(instruccion);
    }
    
  
    public void generarExpresion(ArrayList<ElementoExpresion> listaRPN) {
        
        String rpnOriginalStr = rpnToString(listaRPN);
        codigoIntermedio.add(rpnOriginalStr);

        optimizarRPN(listaRPN);

       
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
        generar("BRI " + etiquetaInicio);
        generar(etiquetaFin + ":");
    }
    
   
    public void generarAsignacion(String variable, String expresionRPN) {
        generar(variable + " " + expresionRPN + " =");
    }
    
    
    public void generarAsignacion(String variable, ArrayList<ElementoExpresion> listaRPN) {
        
        String rpnOriginalStr = rpnToString(listaRPN);
        codigoIntermedio.add(variable + " " + rpnOriginalStr + " =");

        optimizarRPN(listaRPN);

      
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

   
  
   
    private void optimizarRPN(ArrayList<ElementoExpresion> lista) {
        boolean huboCambios = true;
        while (huboCambios) {
            huboCambios = false;
            for (int i = 0; i + 2 < lista.size(); i++) {
                ElementoExpresion e1 = lista.get(i);
                ElementoExpresion e2 = lista.get(i + 1);
                ElementoExpresion e3 = lista.get(i + 2);
                

                // X puede ser variable o operando
                if ((e1.tipoElemento == ElementoExpresion.TIPO_VARIABLE
                        || e1.tipoElemento == ElementoExpresion.TIPO_OPERANDO)
                        && e2.esOperando() && e3.esOperador()) {
                    String op = e3.valor;
                    // Verificar constantes 0 o 1
                    boolean e2IsZero = (e2.tipoDato == 207 && ("0".equals(e2.valor) || "0.0".equals(e2.valor)));
                    boolean e2IsOne = (e2.tipoDato == 207 && ("1".equals(e2.valor) || "1.0".equals(e2.valor)));

                    // X 0 + -> X
                    if (e2IsZero && "+".equals(op)) {
                        
                        lista.remove(i + 2); // e3
                        lista.remove(i + 1); // e2
                        huboCambios = true;
                        break; 
                    }

                    // X 0 - -> X
                    if (e2IsZero && "-".equals(op)) {
                        lista.remove(i + 2);
                        lista.remove(i + 1);
                        huboCambios = true;
                        break;
                    }

                    //  X 1 * -> X
                    if (e2IsOne && "*".equals(op)) {
                        lista.remove(i + 2);
                        lista.remove(i + 1);
                        huboCambios = true;
                        break;
                    }

                    // X 0 * -> 0
                    if (e2IsZero && "*".equals(op)) {

                        ElementoExpresion cero = new ElementoExpresion(ElementoExpresion.TIPO_OPERANDO, 207, "0",
                                e2.linea);
                        lista.remove(i);
                        lista.remove(i);
                        lista.remove(i);
                        lista.add(i, cero);
                        huboCambios = true;
                        break;
                    }
                }

                // Folding
                // OPERANDO, OPERANDO, OPERADOR
                if (e1.esOperando() && e2.esOperando() && e3.esOperador()) {
                    int tipo1 = e1.tipoDato;
                    int tipo2 = e2.tipoDato;

                   
                    if ((tipo1 == 207 || tipo1 == 208) && (tipo2 == 207 || tipo2 == 208)) {
                        try {
                            double v1 = Double.parseDouble(e1.valor);
                            double v2 = Double.parseDouble(e2.valor);
                            String resultadoStr = "0";
                            int tipoResultado = 207; 

                            // Si es float
                            if (tipo1 == 208 || tipo2 == 208) {
                                tipoResultado = 208;
                            }

                            switch (e3.valor) {
                                case "+":
                                    if (tipoResultado == 208)
                                        resultadoStr = String.valueOf(v1 + v2);
                                    else
                                        resultadoStr = String.valueOf((long) (v1 + v2));
                                    break;
                                case "-":
                                    if (tipoResultado == 208)
                                        resultadoStr = String.valueOf(v1 - v2);
                                    else
                                        resultadoStr = String.valueOf((long) (v1 - v2));
                                    break;
                                case "*":
                                    if (tipoResultado == 208)
                                        resultadoStr = String.valueOf(v1 * v2);
                                    else
                                        resultadoStr = String.valueOf((long) (v1 * v2));
                                    break;
                                case "/":
                                    double div = v1 / v2;
                                    if (tipoResultado == 207 && v1 % v2 == 0) {
                                        resultadoStr = String.valueOf((long) div);
                                    } else {
                                        resultadoStr = String.valueOf(div);
                                        tipoResultado = 208;
                                    }
                                    break;
                                default:
                                    // No soportado
                                    continue;
                            }

                            // Crear nuevo Elemento con resultado
                            ElementoExpresion nuevo = new ElementoExpresion(ElementoExpresion.TIPO_OPERANDO,
                                    tipoResultado, resultadoStr, e1.linea);

                            // Reemplazar
                            lista.remove(i);
                            lista.remove(i);
                            lista.remove(i);
                            lista.add(i, nuevo);
                            huboCambios = true;
                            break;
                        } catch (NumberFormatException ex) {

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

    public ArrayList<String> getCodigoOptimizado() {
        return codigoOptimizado;
    }
}
