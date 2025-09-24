/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package codigo;

import java.util.ArrayList;
import java.util.Stack;

/**
 *
 * @author urqui
 */
public class NotacionPolish {
    private ArrayList<ElementoExpresion> expresionPolish;
    private Stack<ElementoExpresion> pilaOperadores;
    private Stack<Integer> pilaTipos;
    private StringBuilder resultadoValidacion;
    private boolean errorEncontrado;

    public NotacionPolish() {
        expresionPolish = new ArrayList<>();
        pilaOperadores = new Stack<>();
        pilaTipos = new Stack<>();
        resultadoValidacion = new StringBuilder();
        errorEncontrado = false;
    }
    
    // Método para obtener nombre del tipo (solo para mensajes)
    private String getTipoString(int tipo) {
        switch(tipo) {
            case 207: return "int";
            case 208: return "float";
            case 209: return "boolean";
            case 210: return "string";
            default: return "indefinido";
        }
    }
    
    // Agregar operando (número, string, boolean)
    public void agregarOperando(int tipoDato, String valor, int linea) {
        ElementoExpresion elem = new ElementoExpresion(ElementoExpresion.TIPO_OPERANDO, 
                                                       tipoDato, valor, linea);
        expresionPolish.add(elem);
        pilaTipos.push(tipoDato);
    }
    
    // Agregar variable
    public void agregarVariable(String nombre, int tipoDato, int linea) {
        ElementoExpresion elem = new ElementoExpresion(ElementoExpresion.TIPO_VARIABLE, 
                                                       tipoDato, nombre, linea);
        expresionPolish.add(elem);
        pilaTipos.push(tipoDato);
    }
    
    // Agregar operador
    public void agregarOperador(String operador, int linea) {
        // Manejar precedencia antes de agregar
        manejarPrecedencia(operador);
        
        ElementoExpresion elem = new ElementoExpresion(ElementoExpresion.TIPO_OPERADOR, 
                                                       0, operador, linea);
        pilaOperadores.push(elem);
    }
    
    // Manejar precedencia de operadores
    private void manejarPrecedencia(String nuevoOperador) {
        int precedenciaNuevo = precedencia(nuevoOperador);
        
        while (!pilaOperadores.isEmpty() && 
               precedencia(pilaOperadores.peek().valor) >= precedenciaNuevo) {
            ElementoExpresion operador = pilaOperadores.pop();
            expresionPolish.add(operador);
            validarOperacion(operador);
        }
    }
    
    // Obtener precedencia
    private int precedencia(String operador) {
        switch(operador) {
            case "*": case "/": case "%": return 3;
            case "+": case "-": return 2;
            case "<": case ">": case "<=": case ">=": case "==": case "!=": return 1;
            case "&&": case "||": return 0;
            default: return -1;
        }
    }
    
    // Finalizar expresión
    public void finalizarExpresion() {
        while (!pilaOperadores.isEmpty()) {
            ElementoExpresion operador = pilaOperadores.pop();
            expresionPolish.add(operador);
            validarOperacion(operador);
        }
    }
    
    // Validar operación
    private void validarOperacion(ElementoExpresion operador) {
        if (pilaTipos.size() < 2) {
            resultadoValidacion.append("Error: Faltan operandos para '")
                              .append(operador.valor).append("' en línea ")
                              .append(operador.linea).append("\n");
            errorEncontrado = true;
            return;
        }
        
        int tipoDer = pilaTipos.pop();
        int tipoIzq = pilaTipos.pop();
        int tipoResultado = validarTiposOperacion(operador.valor, tipoIzq, tipoDer, operador.linea);
        
        if (tipoResultado != -1) {
            pilaTipos.push(tipoResultado);
        } else {
            errorEncontrado = true;
        }
    }
    
    // Validar compatibilidad de tipos
    private int validarTiposOperacion(String operador, int tipoIzq, int tipoDer, int linea) {
        // Operadores aritméticos
        if (operador.equals("+") || operador.equals("-") || operador.equals("*") || 
            operador.equals("/") || operador.equals("%")) {
            
            if (tipoIzq == 207 && tipoDer == 207) return 207; // int op int = int
            if ((tipoIzq == 207 || tipoIzq == 208) && (tipoDer == 207 || tipoDer == 208)) 
                return 208; // float op float = float
            
            resultadoValidacion.append("Error: Tipos incompatibles '")
                              .append(getTipoString(tipoIzq)).append(" ").append(operador)
                              .append(" ").append(getTipoString(tipoDer))
                              .append("' en línea ").append(linea).append("\n");
            return -1;
        }
        
        // Operadores relacionales
        if (operador.equals("<") || operador.equals(">") || operador.equals("<=") || 
            operador.equals(">=") || operador.equals("==") || operador.equals("!=")) {
            
            if ((tipoIzq == 207 || tipoIzq == 208) && (tipoDer == 207 || tipoDer == 208)) 
                return 209; // número op número = boolean
            
            resultadoValidacion.append("Error: Tipos incompatibles '")
                              .append(getTipoString(tipoIzq)).append(" ").append(operador)
                              .append(" ").append(getTipoString(tipoDer))
                              .append("' en línea ").append(linea).append("\n");
            return -1;
        }
        
        // Operadores lógicos
        if (operador.equals("&&") || operador.equals("||")) {
            if (tipoIzq == 209 && tipoDer == 209) return 209; // boolean op boolean = boolean
            
            resultadoValidacion.append("Error: Tipos incompatibles '")
                              .append(getTipoString(tipoIzq)).append(" ").append(operador)
                              .append(" ").append(getTipoString(tipoDer))
                              .append("' en línea ").append(linea).append("\n");
            return -1;
        }
        
        return -1;
    }
    
    // Validar asignación
    public boolean validarAsignacion(int tipoVariable, int linea) {
        if (pilaTipos.size() != 1) {
            resultadoValidacion.append("Error: Expresión inválida en línea ").append(linea).append("\n");
            return false;
        }
        
        int tipoExpresion = pilaTipos.pop();
        boolean valido = esAsignacionValida(tipoVariable, tipoExpresion);
        
        if (!valido) {
            resultadoValidacion.append("Error: No se puede asignar '")
                              .append(getTipoString(tipoExpresion))
                              .append("' a variable '")
                              .append(getTipoString(tipoVariable))
                              .append("' en línea ").append(linea).append("\n");
        }
        
        return valido;
    }
    
    private boolean esAsignacionValida(int tipoVariable, int tipoExpresion) {
        if (tipoVariable == tipoExpresion) return true; // mismo tipo
        if (tipoVariable == 208 && tipoExpresion == 207) return true; // float = int
        return false;
    }
    
    // Validar condición (debe ser boolean)
    public boolean validarCondicional(int linea) {
        if (pilaTipos.size() != 1) {
            resultadoValidacion.append("Error: Condición inválida en línea ").append(linea).append("\n");
            return false;
        }
        
        int tipoExpresion = pilaTipos.pop();
        boolean valido = (tipoExpresion == 209);
        
        if (!valido) {
            resultadoValidacion.append("Error: La condición debe ser boolean, se encontró '")
                              .append(getTipoString(tipoExpresion))
                              .append("' en línea ").append(linea).append("\n");
        }
        
        return valido;
    }
    
    // Getters
    public ArrayList<ElementoExpresion> getExpresionPolish() { return expresionPolish; }
    public String getResultadoValidacion() { return resultadoValidacion.toString(); }
    public boolean hayError() { return errorEncontrado; }
    
    public void limpiar() {
        expresionPolish.clear();
        pilaOperadores.clear();
        pilaTipos.clear();
        resultadoValidacion.setLength(0);
        errorEncontrado = false;
    }
}
