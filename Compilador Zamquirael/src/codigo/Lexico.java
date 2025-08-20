
package codigo;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.io.File;
import javax.swing.JTextArea;
/**
 *
 * @author Rocapez
 */
public class Lexico {
    Token token = null;
    Token cabeza = null;
    Token puntero;
   int estado = 0, columna, valorMatriz, numLinea = 1, caracter = 0;
   String Lexema = "";
   boolean errorFound = false, endOfFile = false;
   String archivo;
   ArrayList<Token> listaTokens = new ArrayList();
   RandomAccessFile file = null;

   public Lexico(String archivo){
   this.archivo = archivo;
   }
   

   
   //Matriz de Transiciones de Estado
   //PELIGRO: NO UTILIZAR FORMAT 
   int matriz [][] = {
     
        // 0     1       2       3       4       5       6       7       8       9       10      11      12      13      14      15      16      17      18      19      20      21      22      23      24      25      26     27
       // l     d       .       +       -       *       /       %       |       &        !       <       >       =       (       )       {       }       ,       ;       :       "       '     EB      TAB      EOL      EOF    OC   
  /*0*/ { 1,    2,    119,     103,  104,    105,      7,    107,     13,     14,      15,     10,     11,     12,    117,    118,    123,    124,    120,    121,    122,     16,     17,      0,      0,       0,      0,   505},
  /*1*/ { 1,    1,    100,     100,    100,    100,    100,    100,    100,    100,     100,    100,    100,    100,    100,    100,    100,    100,    100,    100,    100,    100,    100,    100,    100,     100,    100,   100},
  /*2*/ {101,   2,      3,     101,    101,    101,    101,    101,    101,    101,     101,    101,    101,    101,    101,    101,    101,    101,    101,    101,    101,    101,    101,    101,    101,     101,    100,   101},
  /*3*/ {500,   4,    500,     500,    500,    500,    500,    500,    500,    500,     500,    500,    500,    500,    500,    500,    500,    500,    500,    500,    500,    500,    500,    500,    500,     500,   500,   500},    
  /*4*/ {102,   4,    102,     102,    102,    102,    102,    102,    102,    102,     102,    102,    102,    102,    102,    102,    102,    102,    102,    102,    102,    102,    102,    102,    102,     102,    102,   102},
  /*5*/ {103,  103,   103,     103,    103,    103,    103,    103,    103,    103,     103,    103,    103,    103,    103,    103,   103,    103,    103,    103,    103,    103,     103,    103,    103,    103,    103,    103},
  /*6*/ {104,  104,   104,     104,    104,    104,    104,    104,    104,    104,     104,    104,    104,    104,    104,    104,    104,    104,    104,    104,    104,    104,    104,    104,    104,     104,    104,   104},
  /*7*/ {106,  106,   106,     106,    106,      8,    106,    106,    106,    106,     106,    106,    106,    106,    106,    106,    106,    106,    106,    106,    106,    106,    106,    106,    106,     106,    106,  106},
  /*8*/ { 8,    8,      8,       8,      8,      9,      8,      8,      8,      8,       8,      8,      8,      8,      8,      8,      8,      8,      8,      8,      8,      8,      8,      8,      8,       8,    503,    8},
  /*9*/ { 8,    8,      8,       8,      8,      9,      0,      8,      8,      8,       8,      8,      8,      8,      8,      8,      8,      8,      8,      8,      8,      8,      8,      8,      8,       8,    503,    8},
  /*10*/{108,  108,   108,     108,    108,    108,    108,    108,    108,    108,     108,    108,    108,    109,    108,    108,    108,    108,    108,    108,    108,    108,    108,    108,    108,     108,    108,   108},
  /*11*/{110,  110,   110,     110,    110,    110,    110,    110,    110,    110,     110,    110,    110,    111,    110,    110,    110,    110,    110,    110,    110,    110,    110,    110,    110,     110,    110,   110},
  /*12*/{113,  113,   113,     113,    113,    113,    113,    113,    113,    113,     113,    113,    113,    112,    113,    113,    113,    113,    113,    113,    113,    113,    113,    113,    113,     113,    113,   113},
  /*13*/{501,  501,   501,     501,    501,    501,    501,    501,    114,    501,     501,    501,    501,    501,    501,    501,    501,    501,    501,    501,    501,    501,    501,    501,    501,     501,    501,   501},
  /*14*/{502,  502,   502,     502,    502,    502,    502,    502,    502,    115,     502,    502,    502,    502,    502,    502,    502,    502,    502,    502,    502,    502,    502,    502,    502,     502,    502,   502},
  /*15*/{506,  506,   506,     506,    506,    506,    506,    506,    506,    506,     506,    506,    506,    116,    506,    506,    506,    506,    506,    506,    506,    506,    506,    506,    506,     506,     506,  506},
  /*16*/{16,    16,    16,      16,     16,     16,     16,     16,     16,     16,      16,     16,     16,     16,     16,     16,     16,     16,     16,     16,     16,    126,     16,     16,     16,     504,     504,   16},
  /*17*/{18,    18,    18,      18,     18,     18,     18,     18,     18,     18,      18,     18,     18,     18,     18,     18,     18,     18,     18,     18,     18,    18,      18,     18,     18,      18,      18,   18},
 /*18*/ {507,   507,   507,    507,    507,    507,    507,    507,    507,    507,     507,    507,    507,    507,    507,    507,     507,    507,    507,    507,    507,   507,    127,    507,    507,     507,     507,  507},

};

 //Palabras Reservadas
    String PalabrasReservadas[][] = {
        // 0       1
        {"200","if"},
        {"201","else"},
        {"203","while"},
        {"204","return"},
        {"205","break"},
        {"206","print"},
        {"207","int"},
        {"208","float"},
        {"209","boolean"},
        {"210","string"},
        {"211","true"},
        {"212","false"},
        {"216", "package"},
        {"217", "class"}
    };

    //Tabla de Errores
    String ErroresLexicos[][] = {
        {"500","Se espera un digito "},
        {"501","Se espera otro | "},
        {"502","Se espera otro & "},
        {"503","Se espera cerrar el comentario "},
        {"504","Se espera cerrar la cadena "},
        {"505","Simbolo Invalido "},
        {"506", "Se espera = "},
        {"507", "Se espera cerrar el caracter "}
    };

    public ArrayList<Token> lexico (javax.swing.JTextArea salida ){
        
        
        try{
            file = new RandomAccessFile(archivo,"r");
            
            //Lee el archivo hasta el final
            while (!endOfFile){
              
                caracter = file.read();
                
                //Final del archivo
                if(caracter == -1){
                    columna = 26;
                    endOfFile = true;
                }else if(Character.isLetter((char)caracter)){ //Caracter
                    
                    columna = 0;
                    
                } else if(Character.isDigit((char)caracter)){ //Digito
                    columna = 1;
                }else{      
                    switch ((char) caracter){
                        case '.': columna = 2;
                        break;
                        
                        case '+': columna = 3;
                        break;
                        
                        case '-': columna = 4;
                        break;
                        
                        case '*': columna = 5;
                        break;
                        
                        case '/': columna = 6;
                        break;
                        
                        case '%': columna = 7;
                        break;
                        
                        case '|': columna = 8;
                        break;
                        
                        case '&': columna = 9;
                        break;
                        
                        case '!': columna = 10;
                        break;
                        
                        case '<': columna = 11;
                        break;
                        
                        case '>': columna = 12;
                        break;
                        
                        case '=': columna = 13;
                        break;
                        
                        case '(': columna = 14;
                        break;
                        
                        case ')': columna = 15;
                        break;
                        
                        case '{': columna = 16;
                        break;
                        
                        case '}': columna = 17;
                        break;
                        
                        case ',': columna = 18;
                        break;
                        
                        case ';': columna = 19;
                        break;
                        
                        case ':': columna = 20;
                        break;
                        
                        case '"': columna = 21;
                        break;
                        
                        case '\'': columna = 22;
                        break;

                        //EB
                        case ' ': columna = 23; 
                        break;
                        
                        //Tab
                        case 9: columna = 24;
                        break;

                        //EOL
                        case 10: 
                        columna = 25;
                        numLinea++;
                        break;

                        //EOL
                        case 13: columna = 25; 
                        break;

                        //Oc
                        default: 
                        columna = 27;
                        break;
                    }
                }

                //Valor dado de la matriz 
                valorMatriz = matriz[estado][columna];

                //Si valorMatriz < 100, el valor de estado se convierte en valorMatriz
                if (valorMatriz < 100) { //Valida si es un estado
                    estado = valorMatriz;

                    if (estado == 0) {
                        Lexema = "";
                    }else{
                        Lexema = Lexema + (char) caracter;
                    }
                }
                else if (valorMatriz >= 100 && valorMatriz < 500){ //Validar si es palabra reservada
                    if (valorMatriz == 100) {
                        PalabraReservada();
                    }

                    if (valorMatriz == 100 || valorMatriz == 101 || valorMatriz == 102 ||
                        valorMatriz == 106 || valorMatriz == 108 ||valorMatriz >= 200  ||
                        valorMatriz == 110 ||  valorMatriz == 113) { //
                        file.seek(file.getFilePointer()-1);
                    }else{
                        Lexema = Lexema + (char) caracter;
                    }

                    //Se termina todo, se reinicia el estado y el lexema y se vuelve a leer el archivo
                    //Esto pasa hasta que llegue al final del archivo o salga algun error
                    
                    token = new Token(Lexema,valorMatriz, numLinea);
                    listaTokens.add(token);
                    
                      InsertarNodo();
                      
                    estado = 0;
                    Lexema = "";
                }else{
                    ImprimirErrorLexico(salida); //Imprime un error de la tabla y reinicia todo
                    estado=0; 
                    Lexema = ""; 
                }
            }
           
        }
        catch(Exception e){
            System.out.println(e.getMessage());
        }
        
        if(!errorFound){
            salida.append("/////////////Analisis lexico Terminado///////////// \n");
        }
        return this.listaTokens;
        
    }

    //*Metodos del Analizador Lexico================================================================================================================
    //*Metodo para validar si es palabra reservada
    private void PalabraReservada(){
        for(String [] PalRes : PalabrasReservadas){
            if(Lexema.equals(PalRes[1])){
                valorMatriz = Integer.valueOf(PalRes[0]);
            }
        }
    }

    //Metodo para imprimir los errores lexicos
    private void ImprimirErrorLexico(javax.swing.JTextArea salida){
        if ((caracter != -1 && valorMatriz >= 500) || (caracter == -1 && valorMatriz >= 500))  {
            for(String[] Errores : ErroresLexicos){
                if (valorMatriz == Integer.valueOf(Errores[0])){
                    salida.append("El error encontrado es: " + "Error " + valorMatriz + ", " + Errores[1]  + "en la linea: " + (numLinea) + "\n");

                }
            }
        }
        errorFound = true;
    }
      
    private void InsertarNodo(){
        Token nodo = new Token(Lexema, valorMatriz, numLinea);
        
        if(cabeza == null){
            cabeza = nodo;
            puntero = cabeza;
        }
        else{
            puntero.sig = nodo;
            puntero = nodo;
        }
    } 
    
    
}
