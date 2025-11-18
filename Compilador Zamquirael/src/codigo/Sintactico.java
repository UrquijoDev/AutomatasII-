package codigo;

/**
 *
 * @author Rocapez
 */
//Checar parentesis abierto y cadena incompleta primera comilla
public class Sintactico {

    Token cabeza = null, p;
    boolean errorSintactico = false;
    StringBuilder resultadoBuilder = new StringBuilder("\n");
        int contadorCorchetes = 0;
        boolean esBoolean = false;

        
        private NodoVar cabezaVar = null;
        private NodoVar punteroVar = null;

       
        private NotacionPolish notacionPolish;
        private boolean enExpresion;
        private int tipoVariableActual; // Para asignaciones
        private boolean debugPolish = false; // Cambiar a false para no mostrar debug

        
        private GeneradorCodigo generadorCodigo;
        private boolean generarCodigoIntermedio;
    
    public void sintaxis() {
        p = cabeza;
    
    // INICIALIZAR NOTACIÓN POLISH
    notacionPolish = new NotacionPolish();
    enExpresion = false;
    tipoVariableActual = 0;
  
    generadorCodigo = new GeneradorCodigo();
    generarCodigoIntermedio = true; // Cambia a false si no quieres generar código
          // Verificar si la lista de tokens está vacía
    if (p == null) {
        resultadoBuilder = new StringBuilder("Error: No hay tokens para analizar (archivo vacío).\n");
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
                                            p.idToken == 204 || p.idToken == 205 || p.idToken == 218) {
                                        statements();
                                    } else if (p.idToken == 124) // }
                                    {
                                        p = p.sig;
                                        contadorCorchetes--; //Decrementar por cerrar corchete

                                    } else {

                                        resultadoBuilder.append("Inicio de sentencia invalido en ").append(p.linea).append("\n");

                                        errorSintactico = true;

                                    }
                                }

                            } else {
                                resultadoBuilder.append("Se espera { en ").append(p.linea).append("\n");

                                errorSintactico = true;
                            }
                        } else {
                            resultadoBuilder.append("Se espera un identificador en ").append(p.linea).append("\n");
                            errorSintactico = true;
                        }
                    } else {
                        resultadoBuilder.append("Se espera class en ").append(p.linea).append("\n");
                        errorSintactico = true;
                    }
                } else {
                    resultadoBuilder.append("Se espera ; en ").append(p.linea).append("\n");
                }
            } else {
                resultadoBuilder.append("Se espera un identificador en ").append(p.linea).append("\n");
                errorSintactico = true;
            }
        } else {
            resultadoBuilder.append("Se espera package en ").append(p.linea).append("\n");
            errorSintactico = true;
        }

        if (errorSintactico == false) {
            if (contadorCorchetes == 0) {
                errorSintactico = false;
                resultadoBuilder.append("\n/////////////Analisis Sintactico Terminado/////////////");

            }

            if (contadorCorchetes < 0) {
                resultadoBuilder.append("Sobran ").append(contadorCorchetes * -1).append(" corchetes\n");
                errorSintactico = true;

            }

            if (contadorCorchetes > 0) {

                resultadoBuilder.append("Falta cerrar ").append(contadorCorchetes).append(" corchetes\n");

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
                        resultadoBuilder.append("Error semántico: Variable '").append(p.lexema).append("' no declarada (línea ").append(p.linea).append(")\n");
                        errorSintactico = true;
                    }
                    String nombreVariable = p.lexema;
                    p = p.sig;
                    if (p.idToken == 113) // =
                    {
                        int renglon = p.linea;
                        p = p.sig;
                        enExpresion = true;
                        notacionPolish.limpiar();
                        if (checkExpreSimple()) {
                            enExpresion = false;
                            notacionPolish.finalizarExpresion();
                            int tipoVar = obtenerTipoVariable(nombreVariable);
                            if (!errorSintactico && !notacionPolish.validarAsignacion(tipoVar, (p != null ? p.linea : renglon))) {
                                resultadoBuilder.append(notacionPolish.getResultadoValidacion());
                                errorSintactico = true;
                            }
                            
                            // ASIGNACIÓN
                            if (generarCodigoIntermedio && !errorSintactico) {
                                // Pasa la lista de objetos RPN a GeneradorCodigo 
                                generadorCodigo.generarAsignacion(nombreVariable, notacionPolish.getExpresionPolish());
                            }
                            if (p.idToken == 121) // ; 
                            {
                                if (p.sig == null) {
                                    break;
                                } else {
                                    p = p.sig;
                                }
                            } else {
                                resultadoBuilder.append("Se espera ; en ").append(renglon).append("\n");
                                errorSintactico = true;
                            }
                        }
                    } else {
                        resultadoBuilder.append("Se espera = en ").append(p.linea).append("\n");
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
                                    resultadoBuilder.append("Error semántico: Variable '").append(p.lexema).append("' no declarada (línea ").append(p.linea).append(")\n");
                                    errorSintactico = true;
                                }
                            }
                            String valorPrint = p.lexema;
                            if (generarCodigoIntermedio && !errorSintactico) {
                                generadorCodigo.generarPrint(valorPrint);
                            }
                            p = p.sig;
                            if (p != null && p.idToken == 118) // ) )
                            {
                                p = p.sig;
                                if (p.idToken == 121) // ;
                                {
                                    p = p.sig;
                                } else {
                                    resultadoBuilder.append("Se espera ; en la linea ").append(p.linea).append("\n");
                                    errorSintactico = true;
                                }
                            } else {
                               resultadoBuilder.append("Se espera ) en ").append(p != null ? p.linea : "desconocida").append("\n");
                                errorSintactico = true;
                            }
                        } else {
                            resultadoBuilder.append("Se espera un identificador o cadena valida en linea ").append(_renglon).append("\n");
                            errorSintactico = true;
                        }

                    } else {
                        resultadoBuilder.append("Se espera ( en la linea ").append(p.linea).append("\n");
                        errorSintactico = true;
                    }
                } // fin de print
                else if (p.idToken == 218) // scanner
                {
                    procesarScannerConCodigo();
                } // fin de scanner
                else if (p.idToken == 200) //Inicio de if 
                {
                    procesarIfConCodigo();
                } else if (p.idToken == 203) //while 
                {
                    procesarWhileConCodigo();
                } else if (p.idToken == 205) { // break
                    p = p.sig;

                    if (p.idToken == 121) { // ;
                        p = p.sig;
                    } else {
                        resultadoBuilder.append("Se espera ; en la linea ").append(p.linea).append("\n");
                        errorSintactico = true;
                    }
                } else if (p.idToken == 204) { // return
                    p = p.sig;

                    if (p.idToken == 121) { // ;
                        p = p.sig;
                    } else {
                        resultadoBuilder.append("Se espera ; en la linea ").append(p.linea).append("\n");
                        errorSintactico = true;
                    }
                } else {

                    break;
                }
            }
        } catch (NullPointerException e) {

        }
    }

   
  
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
            resultadoBuilder.append("Error semántico: Variable '").append(nombre).append("' ya declarada (línea ").append(p.linea).append(")\n");
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
    tipoVariableActual = p.idToken; // Guardar tipo de variable (207, 208, etc.)
    p = p.sig;

    if (p.idToken == 100) { // Identificador
        String nombreVariable = p.lexema;
        insertarVariable(nombreVariable, tipoVariableActual);
        p = p.sig;
        
        if (p.idToken == 120) { // ,
            checkDeclaracionVariable(); // Llamada recursiva
        } else {
            if (p.idToken == 113) { // =
                p = p.sig;
                
                enExpresion = true;
                notacionPolish.limpiar();
                
                if (checkExpreSimple()) {
                    enExpresion = false;
                    notacionPolish.finalizarExpresion();
                    if (!errorSintactico && !notacionPolish.validarAsignacion(tipoVariableActual, p.linea)) {
                        resultadoBuilder.append(notacionPolish.getResultadoValidacion());
                        errorSintactico = true;
                    }
                    
                    // GENERAR CÓDIGO DE ASIGNACIÓN
                    if (generarCodigoIntermedio && !errorSintactico) {
                        generadorCodigo.generarAsignacion(nombreVariable, notacionPolish.getExpresionPolish());
                    }
                    if (p.idToken == 121) { // ;
                        p = p.sig;
                    } else {
                        resultadoBuilder.append("Se espera ; en ").append(p.linea).append("\n");
                        errorSintactico = true;
                    }
                }
            } else if (p.idToken == 121) { // ;
                p = p.sig;
            } else {
                resultadoBuilder.append("Se espera = o ; en ").append(p.linea).append("\n");
                errorSintactico = true;
            }
        }
    } else {
        resultadoBuilder.append("Se espera un identificador en línea ").append(p.linea).append("\n");
        errorSintactico = true;
    }
}

private void procesarIfConCodigo() {
    p = p.sig; // consumir 'if'
    if (p.idToken == 117) { // (
        p = p.sig;
        // Obtener la lista RPN para la condición
        generarRPNCondicional();
        if (generarCodigoIntermedio) {
            generadorCodigo.generarExpresion(notacionPolish.getExpresionPolish());
        }
        generadorCodigo.iniciarIf(); // Genera BRF L0
        notacionPolish.limpiar();
        
        if (p.idToken == 118) { // )
            p = p.sig;
            if (p.idToken == 123) { // {
                contadorCorchetes++;
                p = p.sig;
                
                statements(); // Esto genera el código DENTRO del if
                
                if (p.idToken == 124) { // }
                    contadorCorchetes--;
                    p = p.sig;
                }
            }
            
            generadorCodigo.generarThen(); // Genera BRI L1 y L0:
            
            // Procesar ELSE si existe
            if (p != null && p.idToken == 201) { // else
                p = p.sig;
                if (p.idToken == 123) { // {
                    contadorCorchetes++;
                    p = p.sig;
                    statements(); // Bloque ELSE
                    if (p.idToken == 124) { // }
                        contadorCorchetes--;
                        p = p.sig;
                    }
                }
            }
            generadorCodigo.finalizarIf(); // Genera L1:
        }
    }
}

private void procesarWhileConCodigo() {
    generadorCodigo.iniciarWhile();
    p = p.sig; // consumir 'while'
    if (p.idToken == 117) { // (
        p = p.sig;
        // Obtener la lista RPN para la condición
        generarRPNCondicional();
        if (generarCodigoIntermedio) {
            generadorCodigo.generarExpresion(notacionPolish.getExpresionPolish());
        }
        notacionPolish.limpiar();
        generadorCodigo.generarCondicionWhile();
        if (p.idToken == 118) { // )
            p = p.sig;
            if (p.idToken == 123) { // {
                contadorCorchetes++;
                p = p.sig;
                statements();
                if (p.idToken == 124) { // }
                    contadorCorchetes--;
                    p = p.sig;
                }
            }
            generadorCodigo.finalizarWhile();
        }
    }
}

private void procesarScannerConCodigo() {
    p = p.sig; // consumir 'scanner'
    if (p.idToken == 100) { // Identificador
        String nombreVariable = p.lexema;
        if (generarCodigoIntermedio) {
            generadorCodigo.generarScanner(nombreVariable);
        }
        notacionPolish.limpiar(); // Limpiar después de scanner
        p = p.sig;
        if (p.idToken == 121) { // ;
            p = p.sig;
        }
    }
}

private String generarRPNCondicional() {
    notacionPolish.limpiar();
    enExpresion = true;
    checkExpreCond();
    enExpresion = false;
    notacionPolish.finalizarExpresion();
    return obtenerExpresionRPN();
}

private String obtenerExpresionRPN() {
    StringBuilder sb = new StringBuilder();
    for (ElementoExpresion elem : notacionPolish.getExpresionPolish()) {
        sb.append(elem.valor).append(" ");
    }
    return sb.toString().trim();
}


public GeneradorCodigo getGeneradorCodigo() {
    return generadorCodigo;
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
    
    if (p.idToken == 100) { // id
        if (!variableDeclarada(p.lexema)) {
            resultadoBuilder.append("Error semántico: Variable '").append(p.lexema).append("' no declarada (línea ").append(p.linea).append(")\n");
            errorSintactico = true;
        } else {
            // AGREGAR A NOTACIÓN POLISH
            int tipoVar = obtenerTipoVariable(p.lexema);
            if (enExpresion) {
                notacionPolish.agregarVariable(p.lexema, tipoVar, p.linea);
            }
        }
        FactorEncontrado = true;
        
    } else if (p.idToken == 126) { // cadena
        // AGREGAR A NOTACIÓN POLISH
        if (enExpresion) {
            notacionPolish.agregarOperando(210, p.lexema, p.linea); // 210 = string
        }
        FactorEncontrado = true;
        
    } else if (p.idToken == 101) { // int
        // AGREGAR A NOTACIÓN POLISH
        if (enExpresion) {
            notacionPolish.agregarOperando(207, p.lexema, p.linea); // 207 = int
        }
        FactorEncontrado = true;
        
    } else if (p.idToken == 102) { // decimal
        // AGREGAR A NOTACIÓN POLISH
        if (enExpresion) {
            notacionPolish.agregarOperando(208, p.lexema, p.linea); // 208 = float
        }
        FactorEncontrado = true;
        
    } else if (p.idToken == 211 || p.idToken == 212) { // true o false
        // AGREGAR A NOTACIÓN POLISH
        if (enExpresion) {
            notacionPolish.agregarOperando(209, p.lexema, p.linea); // 209 = boolean
        }
        FactorEncontrado = true;
    }
    
    return FactorEncontrado;
}

private boolean checkExpreSimple() {
    boolean expresionSimpleEncontrada = false;
    
    if (checkTermino()) {
        expresionSimpleEncontrada = true;
        
        // Procesar operaciones aditivas
        while (p != null && checkOperacionAditiva()) {
            String operador = obtenerOperador(p.idToken);
            if (enExpresion) {
                notacionPolish.agregarOperador(operador, p.linea);
            }
            p = p.sig;
            
            if (!checkTermino()) {
                resultadoBuilder.append("Se espera término en ").append(p.linea).append("\n");
                errorSintactico = true;
                return false;
            }
        }
    } else if (checkBoolean()) {
        expresionSimpleEncontrada = true;
        p = p.sig;
        esBoolean = true;
    } else {
        // Si no es término ni booleano, podría ser un error
            if (p != null && p.idToken != 118 && p.idToken != 121) { // No es ) o ;
            resultadoBuilder.append("Se espera expresión simple en ").append(p.linea).append("\n");
            errorSintactico = true;
        }
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
    
    // Procesar primer factor
    if (checkFactor()) {
        TerminoEncontrado = true;
        p = p.sig;
    }
    
    // Procesar operaciones multiplicativas
    if (p != null && checkOperacionMult()) {
        String operador = obtenerOperador(p.idToken);
        // AGREGAR OPERADOR A NOTACIÓN POLISH
        if (enExpresion) {
            notacionPolish.agregarOperador(operador, p.linea);
        }
        p = p.sig;
        
        if (checkFactor()) {
            TerminoEncontrado = true;
            p = p.sig;
        } else {
            resultadoBuilder.append("Se espera factor en ").append(p.linea).append("\n");
            errorSintactico = true;
        }
    }
    
    return TerminoEncontrado;
}

private boolean checkExpreCond() {
    boolean expresionCondicional = false;
    
    // INICIAR NOTACIÓN POLISH PARA CONDICIÓN
    enExpresion = true;
    notacionPolish.limpiar();

    if (checkExpreSimple()) {
        expresionCondicional = true;
        
        // PROCESAR OPERADORES RELACIONALES Y LÓGICOS
        while (p != null && (checkOperacionRelac() || checkOperacionLogica())) {
            if (checkOperacionRelac()) {
                String operador = obtenerOperador(p.idToken);
                if (enExpresion) {
                    notacionPolish.agregarOperador(operador, p.linea);
                }
                p = p.sig;
                
                if (checkExpreSimple()) {
                    // Continuar procesando
                } else {
                    errorSintactico = true;
                    resultadoBuilder.append("Se espera expresión simple después del operador relacional en línea ").append(p.linea).append("\n");
                    break;
                }
            } else if (checkOperacionLogica()) {
                String operador = obtenerOperador(p.idToken);
                if (enExpresion) {
                    notacionPolish.agregarOperador(operador, p.linea);
                }
                p = p.sig;
                
                if (checkExpreSimple()) {
                    // Continuar procesando
                } else {
                    errorSintactico = true;
                    resultadoBuilder.append("Se espera expresión simple después del operador lógico en línea ").append(p.linea).append("\n");
                    break;
                }
            }
        }
        
        // FINALIZAR Y VALIDAR CONDICIÓN
        enExpresion = false;
        notacionPolish.finalizarExpresion();
        
        // VALIDAR QUE SEA BOOLEAN (solo si no hay errores previos)
        if (!errorSintactico && !notacionPolish.validarCondicional(p.linea)) {
            resultadoBuilder.append(notacionPolish.getResultadoValidacion());
            errorSintactico = true;
        }
        
        // MOSTRAR NOTACIÓN POLISH SOLO SI HAY ERROR O EN DEBUG
        if (errorSintactico) {
            mostrarNotacionPolish();
        }
    } else {
        errorSintactico = true;
        resultadoBuilder.append("Se espera expresión condicional en línea ").append(p.linea).append("\n");
    }
    
    return expresionCondicional;
}

// AGREGAR ESTE MÉTODO NUEVO PARA DETECTAR OPERADORES LÓGICOS
private boolean checkOperacionLogica() {
    return p.idToken == 114 || p.idToken == 115; // || o &&
}
    
    // MÉTODOS AUXILIARES PARA NOTACIÓN POLISH
private String obtenerOperador(int token) {
    switch(token) {
        case 103: return "+";
        case 104: return "-";
        case 105: return "*";
        case 106: return "/";
        case 107: return "%";
        case 108: return "<";
        case 109: return "<=";
        case 110: return ">";
        case 111: return ">=";
        case 112: return "==";
        case 116: return "!=";
        case 114: return "||";
        case 115: return "&&";
        default: return "?";
    }
}

    // Variables para cache manual de tipo de variable
    private String ultimaVariableBuscada = null;
    private int ultimoTipoEncontrado = 0;

    private int obtenerTipoVariable(String nombre) {
        // Verificar cache primero
        if (nombre.equals(ultimaVariableBuscada)) {
            return ultimoTipoEncontrado;
        }
        // Búsqueda normal si no está en cache
        NodoVar actual = cabezaVar;
        while (actual != null) {
            if (actual.nombre.equals(nombre)) {
                // Actualizar cache
                ultimaVariableBuscada = nombre;
                ultimoTipoEncontrado = actual.tipo;
                return actual.tipo;
            }
            actual = actual.sig;
        }
        // Variable no encontrada - actualizar cache también
        ultimaVariableBuscada = nombre;
        ultimoTipoEncontrado = 0;
        return 0;
    }

private void mostrarNotacionPolish() {
    // SOLO MOSTRAR SI HAY ERRORES O SI EL DEBUG ESTÁ ACTIVADO
    if (errorSintactico || debugPolish) {
        if (!notacionPolish.getExpresionPolish().isEmpty()) {
            resultadoBuilder.append("Notación Polish: ");
            for (ElementoExpresion elem : notacionPolish.getExpresionPolish()) {
                resultadoBuilder.append(elem.valor).append(" ");
            }
            resultadoBuilder.append("\n");

            // Si hay errores de validación, mostrarlos también
            if (!notacionPolish.getResultadoValidacion().isEmpty()) {
                resultadoBuilder.append(notacionPolish.getResultadoValidacion());
            }
        }
    }
}

// Devuelve el resultado del análisis como String
public String getResultado() {
    return resultadoBuilder.toString();
}

}
