package codigo;

import java.util.ArrayList;
import java.util.Stack;

public class GeneradorCodigo {
    private ArrayList<String> codigoIntermedio;
    private int contadorEtiquetas;
    private Stack<String> pilaEtiquetas;
    private StringBuilder codigoActual;
    
    public GeneradorCodigo() {
        codigoIntermedio = new ArrayList<>();
        contadorEtiquetas = 0;
        pilaEtiquetas = new Stack<>();
        codigoActual = new StringBuilder();
    }
    
    public String nuevaEtiqueta() {
        return "L" + (contadorEtiquetas++);
    }
    
    public void generar(String instruccion) {
        codigoIntermedio.add(instruccion);
    }
    
    public void generarExpresion(String expresionRPN) {
        codigoIntermedio.add(expresionRPN);
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
    
    public void generarAsignacion(String variable, String expresionRPN) {
    generar(variable + " " + expresionRPN + " =");
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
    
    public void limpiar() {
        codigoIntermedio.clear();
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
}
