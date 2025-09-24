/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package codigo;

/**
 *
 * @author urqui
 */
public class ElementoExpresion {
    public static final int TIPO_OPERADOR = 1;
    public static final int TIPO_OPERANDO = 2;
    public static final int TIPO_VARIABLE = 3;

    int tipoElemento;
    int tipoDato; // Para operandos: 207 (int), 208 (float), etc.
    String valor; // Lexema o valor del operador
    int linea; // Línea donde aparece

    public ElementoExpresion(int tipoElemento, int tipoDato, String valor, int linea) {
        this.tipoElemento = tipoElemento;
        this.tipoDato = tipoDato;
        this.valor = valor;
        this.linea = linea;
    }
    
    public boolean esOperador() {
        return tipoElemento == TIPO_OPERADOR;
    }
    
    public boolean esOperando() {
        return tipoElemento == TIPO_OPERANDO;
    }
    
    public boolean esVariable() {
        return tipoElemento == TIPO_VARIABLE;
    }
    
    @Override
    public String toString() {
        String tipoStr = "";
        switch(tipoElemento) {
            case TIPO_OPERADOR: tipoStr = "OPERADOR"; break;
            case TIPO_OPERANDO: tipoStr = "OPERANDO"; break;
            case TIPO_VARIABLE: tipoStr = "VARIABLE"; break;
        }
        return tipoStr + "[" + valor + ", tipo:" + tipoDato + ", linea:" + linea + "]";
    }
}