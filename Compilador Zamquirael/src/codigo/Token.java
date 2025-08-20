
package codigo;

/**
 *
 * @author RocaPez
 */
public class Token {
      String lexema;
     int idToken;
     int linea;
    Token sig = null;
    
    public Token (String lexema1, int idToken1, int linea1){
        lexema = lexema1;
        idToken = idToken1;
        linea = linea1;
      
    }
    
       public String getLexema(){
        return lexema;
    }
    
    public int getidToken(){
        return idToken;
    }
    
    public int getLinea(){
        return linea;
    }
  
    
}
