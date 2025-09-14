package codigo;

/**
 *
 * @author Rocapez
 */
//Checar parentesis abierto y cadena incompleta primera comilla
public class Sintactico {

    Token cabeza = null, p;
    boolean errorSintactico = false;
    String resultado = "\n";
    int contadorCorchetes = 0;
    boolean esBoolean = false;

        // Nuevas variables para tabla de símbolos
    private NodoVar cabezaVar = null;
    private NodoVar punteroVar = null;
    
    
    public void sintaxis() {
        p = cabeza;

          // Verificar si la lista de tokens está vacía
    if (p == null) {
        resultado = "Error: No hay tokens para analizar (archivo vacío).\n";
        errorSintactico = true;
        return;
    }
        if (p.idToken == 216) //package 
        {
            if (p.sig.idToken == 100) {
                p = p.sig;
            }

            if (p.idToken == 100)//Identificador 
            {
                if (p.sig == null) {
                } else {
                    if (p.sig.idToken == 121) {
                        p = p.sig;
                    }
                }

                if (p.idToken == 121) // ;
                {
                    if (p.sig == null) {
                    } else {
                        if (p.sig.idToken == 217) {
                            p = p.sig;
                        }
                    }

                    if (p.idToken == 217) //class
                    {
                        if (p.sig == null) {

                        } else {
                            if (p.sig.idToken == 100) {
                                p = p.sig;
                            }
                        }

                        if (p.idToken == 100) //Identificador
                        {
                            if (p.sig == null) {

                            } else {
                                if (p.sig.idToken == 123) {
                                    p = p.sig;
                                }
                            }

                            if (p.idToken == 123) // {
                            {
                                contadorCorchetes++; //Incrementar por abrir corchete

                                if (p.sig == null) {

                                } else {
                                    p = p.sig;
                                }

                                while (p != null && errorSintactico == false) { //nodo de procesamiento no nulo
                                    if (p.idToken == 200 || p.idToken == 206 || p.idToken == 100 || 
                                            p.idToken == 203 || p.idToken == 207 || 
                                            p.idToken == 208 || p.idToken == 209 || p.idToken == 210 || 
                                            p.idToken == 204 || p.idToken == 205) {
                                        statements();
                                    } else if (p.idToken == 124) // }
                                    {
                                        p = p.sig;
                                        contadorCorchetes--; //Decrementar por cerrar corchete

                                    } else {

                                        resultado += "Inicio de sentencia invalido en " + p.linea + "\n";

                                        errorSintactico = true;

                                    }
                                }

                            } else {
                                resultado += "Se espera { en " + p.linea + "\n";

                                errorSintactico = true;
                            }
                        } else {
                            resultado += "Se espera un identificador en " + p.linea + "\n";
                            errorSintactico = true;
                        }
                    } else {
                        resultado += "Se espera class en " + p.linea + "\n";
                        errorSintactico = true;
                    }
                } else {
                    resultado += "Se espera ; en " + p.linea + "\n";
                }
            } else {
                resultado += "Se espera un identificador en " + p.linea + "\n";
                errorSintactico = true;
            }
        } else {
            resultado += "Se espera package en " + p.linea + "\n";
            errorSintactico = true;
        }

        if (errorSintactico == false) {
            if (contadorCorchetes == 0) {
                errorSintactico = false;
                resultado += "\n/////////////Analisis Sintactico Terminado/////////////";

            }

            if (contadorCorchetes < 0) {
                resultado += "Sobran " + contadorCorchetes * -1 + " corchetes\n";
                errorSintactico = true;

            }

            if (contadorCorchetes > 0) {

                resultado += "Falta cerrar " + contadorCorchetes + " corchetes\n";

                errorSintactico = true;
            }
        }

    }

    private void statements() {
        try {
            while (p != null && p.sig != null) {
                if (p.idToken == 207 || p.idToken == 208 || p.idToken == 209 || p.idToken == 210) {
                    checkDeclaracionVariable();

                } else if (p.idToken == 100) // identificador (Iniio de asignacion de variable)
                {
                    // Verificar si la variable está declarada en el lado izquierdo
                    if (!existeVariable(p.lexema)) {
                        resultado += "Error semántico: Variable '" + p.lexema + "' no declarada (línea " + p.linea + ")\n";
                        errorSintactico = true;
                    }
                    p = p.sig;
                    if (p.idToken == 113) // =
                    {
                        int renglon = p.linea;
                        p = p.sig;
                        if (checkExpreSimple()) {
                            if (checkOperacionRelac()) {
                                resultado += "Operador Invalido en  " + p.linea + "\n";
                                errorSintactico = true;
                                break;
                            }

                            if (p.idToken == 121) // ; 
                            {
                                if (p.sig == null) {
                                    break;
                                } else {
                                    p = p.sig;
                                }
                            } else {
                                resultado += "Se espera ; en " + renglon + "\n";
                                errorSintactico = true;
                            }
                        }
                    } else {
                        resultado += "Se espera = en " + p.linea + "\n";
                        errorSintactico = true;
                    }
                } // Fin de asignacion de variable 
                else if (p.idToken == 206) //Inicio de print 
                {
                    p = p.sig;
                    if (p.idToken == 117) // ( 
                    {
                        int _renglon = p.linea;
                        p = p.sig;

                        if (p.idToken == 100 || p.idToken == 126 || p.idToken == 127) //Id,Cadena o Char
                        {
                                // VERIFICAR SI ES UN IDENTIFICADOR Y SI ESTÁ DECLARADO
                            if (p.idToken == 100) { 
                                if (!existeVariable(p.lexema)) {
                                    resultado += "Error semántico: Variable '" + p.lexema + "' no declarada (línea " + p.linea + ")\n";
                                    errorSintactico = true;
                                }
                            }
                            p = p.sig;
                            if (p.idToken == 118) // )
                            {
                                p = p.sig;
                                if (p.idToken == 121) // ;
                                {
                                    p = p.sig;
                                } else {
                                    resultado += "Se espera ; en la linea " + p.linea + "\n";
                                    errorSintactico = true;
                                }
                            } else {
                                resultado += "Se espera ) en la linea " + p.linea + "\n";
                                errorSintactico = true;
                            }
                        } else {
                            resultado += "Se espera un identificador o cadena valida en linea " + _renglon + "\n";
                            errorSintactico = true;
                        }

                    } else {
                        resultado += "Se espera ( en la linea " + p.linea + "\n";
                        errorSintactico = true;
                    }
                } // fin de print
                else if (p.idToken == 200) //Inicio de if 
                {
                    p = p.sig;

                    if (p.idToken == 117) // (
                    {
                        p = p.sig;
                        if (checkExpreCond()) {

                            if (errorSintactico) {

                                break;
                            }

                            if (p.idToken == 118)// ) 
                            {

                                p = p.sig;

                                if (p.idToken == 123) // {
                                {
                                    contadorCorchetes++;
                                    p = p.sig;
                                    statements();

                                    if (!errorSintactico) {

                                        if (p.idToken == 124) // }
                                        {
                                            contadorCorchetes--;
                                            p = p.sig;

                                            if (p.idToken == 201) //else 
                                            {
                                                p = p.sig;
                                                if (p.idToken == 123) // {
                                                {
                                                    contadorCorchetes++;
                                                    p = p.sig;
                                                    statements();

                                                    if (!errorSintactico) {

                                                        if (p.idToken == 124) // }
                                                        {
                                                            contadorCorchetes--;
                                                            p = p.sig;
                                                        } else {
                                                            resultado += "Se espera } en " + p.linea + "\n";
                                                            errorSintactico = true;
                                                        }
                                                    }
                                                } else {
                                                    resultado += "Se espera { en " + p.linea + "\n";
                                                    errorSintactico = true;
                                                }
                                            }

                                        } else {
                                            resultado += "Se espera } en " + p.linea + "\n";
                                            errorSintactico = true;
                                        }
                                    }
                                } else {
                                    resultado += "Se espera { en " + p.linea + "\n";
                                    errorSintactico = true;
                                }
                            } else {
                                resultado += "Se espera ) en " + p.linea + "\n";
                                errorSintactico = true;
                            }
                        } else {
                            resultado += "Se espera expresion condicional en " + p.linea + "\n";
                            errorSintactico = true;
                        }
                    } else {
                        resultado += "Se espera ( en " + p.linea + "\n";
                        errorSintactico = true;
                    }

                } else if (p.idToken == 203) //while 
                {
                    p = p.sig;
                    if (p.idToken == 117) //(
                    {
                        p = p.sig;
                        if (checkExpreCond()) {
                            if (p.idToken == 118) // )
                            {
                                p = p.sig;
                                if (p.idToken == 123) //{
                                {
                                    contadorCorchetes++;
                                    p = p.sig;
                                    statements();
                                    if (p.idToken == 124) // }
                                    {
                                        contadorCorchetes--;
                                        p = p.sig;

                                    } else {
                                        resultado += "Se espera } en " + p.linea + "\n";
                                        errorSintactico = true;
                                    }
                                } else {
                                    resultado += "Se espera { en " + p.linea + "\n";
                                    errorSintactico = true;
                                }
                            } else {
                                resultado += "Se espera ) en " + p.linea + "\n";
                                errorSintactico = true;
                            }
                        } else {
                            resultado += "Se espera expresion en " + p.linea + "\n";
                            errorSintactico = true;
                        }
                    } else {
                        resultado += "Se espera ( en " + p.linea + "\n";
                        errorSintactico = true;
                    }
                } else if (p.idToken == 205) { // break
                    p = p.sig;

                    if (p.idToken == 121) { // ;
                        p = p.sig;
                    } else {
                        resultado += "Se espera ; en la linea " + p.linea + "\n";
                        errorSintactico = true;
                    }
                } else if (p.idToken == 204) { // return
                    p = p.sig;

                    if (p.idToken == 121) { // ;
                        p = p.sig;
                    } else {
                        resultado += "Se espera ; en la linea " + p.linea + "\n";
                        errorSintactico = true;
                    }
                } else {

                    break;
                }
            }
        } catch (NullPointerException e) {

        }
    }

    // NUEVOS MÉTODOS PARA MANEJO DE TABLA DE SÍMBOLOS
   // Método para verificar si una variable está declarada
    private boolean variableDeclarada(String nombre) {
        NodoVar actual = cabezaVar;
        while (actual != null) {
            if (actual.nombre.equals(nombre)) {
                return true;
            }
            actual = actual.sig;
        }
        return false;
    } 
    
    private boolean existeVariable(String nombre) {
        NodoVar actual = cabezaVar;
        while (actual != null) {
            if (actual.nombre.equals(nombre)) {
                return true;
            }
            actual = actual.sig;
        }
        return false;
    }

    private void insertarVariable(String nombre, int tipo) {
        if (existeVariable(nombre)) {
            resultado += "Error semántico: Variable '" + nombre + "' ya declarada (línea " + p.linea + ")\n";
            errorSintactico = true;
            return;
        }
        
        NodoVar nuevaVar = new NodoVar(nombre, tipo);
        if (cabezaVar == null) {
            cabezaVar = nuevaVar;
            punteroVar = cabezaVar;
        } else {
            punteroVar.sig = nuevaVar;
            punteroVar = nuevaVar;
        }
    }
    
    private void checkDeclaracionVariable() {
        int tipoVariable = p.idToken; // Guardar el tipo de variable (207, 208, etc.)
        p = p.sig;

        if (p.idToken == 100) //Identificador
        {
            // Insertar en tabla de símbolos
            insertarVariable(p.lexema, tipoVariable);
            
            p = p.sig;
            if (p.idToken == 120) // ,
            {
                checkDeclaracionVariable(); // Llamada Recursiva 
            } else {

                if (p.idToken == 113) // =
                {
                    p = p.sig;
                    if (checkExpreSimple()) {
                        if (checkOperacionRelac()) {
                            resultado += "Operador Invalido en  " + p.linea + "\n";
                            errorSintactico = true;
                        }
                        if (p.idToken == 121) // ; 
                        {
                            if (p.sig == null) {

                            } else {
                                p = p.sig;
                            }
                        } else {
                            resultado += "Se espera ; en " + p.linea + "\n";
                            errorSintactico = true;
                        }
                    } else {
                        resultado += "Se espera expresión simple en " + p.linea + "\n";
                        errorSintactico = true;
                    }
                } else {
                    if (p.idToken == 121) // ;
                    {
                        p = p.sig;
                    } else {
                        resultado += "Se espera ; en renglon " + p.linea + "\n";
                        errorSintactico = true;
                    }
                }

            }
        } else {
            resultado += "Se espera un identificador en linea " + p.linea + "\n";
            errorSintactico = true;
        }

        if (esBoolean) {
            esBoolean = false;
        }

    }

    private boolean checkOperacionAditiva() {

        if (p.idToken == 103 || p.idToken == 104) {
            return true;
        } else {
            return false;
        }
    }

    private boolean checkOperacionMult() {
        if (p.idToken == 105 || p.idToken == 106 || p.idToken == 107) {
            return true;
        } else {
            return false;
        }
    }

    private boolean checkOperacionRelac() {
        if (p.idToken == 108 || p.idToken == 109 || p.idToken == 110 || p.idToken == 111 || p.idToken == 112 || p.idToken == 116 || p.idToken == 114 || p.idToken == 115) {
            return true;
        } else {
            return false;
        }
    }

    private boolean checkFactor() {
        boolean FactorEncontrado = false;
        if (p.idToken == 100) {// id
            // VERIFICAR SI LA VARIABLE ESTÁ DECLARADA
            if (!variableDeclarada(p.lexema)) {
                resultado += "Error semántico: Variable '" + p.lexema + "' no declarada (línea " + p.linea + ")\n";
                errorSintactico = true;
            }
            FactorEncontrado = true;
        } else if (p.idToken == 126) {// cadena
            FactorEncontrado = true;
        } else if (p.idToken == 101) {// int
            FactorEncontrado = true;
        } else if (p.idToken == 102) {// decimal
            FactorEncontrado = true;
        }

        /*else if (p.idToken == 117) {// (
            p = p.sig;
            if (checkExpreSimple()) {

                if (p.idToken == 118) {// )
                    FactorEncontrado = true;

                } else {

                    resultado += "Se espera ) en " + p.linea + "\n";

                    errorSintactico = true;
                }
            } else {

                resultado += "Se espera expresion simple en " + p.linea + "\n";

                errorSintactico = true;

            }

        }*/
        return FactorEncontrado;
    }

    private boolean checkExpreSimple() {
        boolean expresionSimpleEncontrada = false;
        if (checkSignos()) {// + - 

            if (checkTermino()) {
                expresionSimpleEncontrada = true;
            } else {

                resultado += "Se espera termino en " + p.linea + "\n";

                errorSintactico = true;
            }

        } else if (checkTermino()) {
            expresionSimpleEncontrada = true;
            if (checkOperacionAditiva() || checkOperacionMult()) {
                p = p.sig;
                expresionSimpleEncontrada = false;

                if (checkExpreSimple()) {
                    expresionSimpleEncontrada = true;
                } else {

                    if (!errorSintactico) {
                        resultado += "se espera una expresion simple en " + p.linea + "\n";

                        errorSintactico = true;
                    }

                }
            } else {

            }
        } else if (checkBoolean()) {
            expresionSimpleEncontrada = true;
            p = p.sig;
            esBoolean = true;
        }
        return expresionSimpleEncontrada;
    }

    private boolean checkSignos() {
        if (p.idToken == 103 || p.idToken == 104) {
            return true;
        } else {
            return false;
        }
    }

    private boolean checkBoolean() {
        if (p.idToken == 211 || p.idToken == 212) {
            return true;
        } else {
            esBoolean = false;
            return false;
        }
    }

    private boolean checkTermino() {

        boolean TerminoEncontrado = false;
        if (checkFactor()) {
            TerminoEncontrado = true;
            p = p.sig;
        }
        if (p.idToken != 121 && TerminoEncontrado) {
            if (checkOperacionMult()) {
                p = p.sig;
                if (checkFactor()) {
                    TerminoEncontrado = true;
                    p = p.sig;
                } else {

                    resultado += "Se espera  factor en " + p.linea + "\n";

                    errorSintactico = true;
                }
            } else {

                TerminoEncontrado = true;
            }
        }

        return TerminoEncontrado;
    }

    private boolean checkExpreCond() {
        boolean expresionCondicional = false;

        if (checkExpreSimple()) {
            if (esBoolean) {
                expresionCondicional = true;
                esBoolean = false;
            } else {

                if (checkOperacionRelac()) {
                    p = p.sig;
                    if (checkExpreSimple()) {
                        expresionCondicional = true;

                        if (esBoolean) {
                            esBoolean = false;
                        }

                        if (p.idToken != 118 && expresionCondicional) {

                            if (checkOperacionRelac()) {
                                p = p.sig;
                                checkExpreCond();
                            } else {

                                if (checkExpreSimple()) {
                                    errorSintactico = true;
                                    resultado += "se espera operador relacional en " + p.linea + "\n";
                                }

                            }

                        }

                    } else {
                        errorSintactico = true;
                        resultado += "se espera expresion Simple en " + p.linea + "\n";

                    }
                } else {
                    errorSintactico = true;
                    resultado += "se espera operador relacional en " + p.linea + "\n";
                }

            }

        } else {
            errorSintactico = true;
            resultado += "se espera expresion Simple en " + p.linea + "\n";

        }

        return expresionCondicional;
    }
}
