/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package codigo;

/**
 *
 * @author urqui
 */
public class NodoVar {
    public String nombre;
    public int tipo;
    public NodoVar sig;

    public NodoVar(String nombre, int tipo) {
        this.nombre = nombre;
        this.tipo = tipo;
        this.sig = null;
    }
}
