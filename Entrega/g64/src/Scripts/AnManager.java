package Scripts;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import javax.swing.JOptionPane;
import Scripts.Token.Accion;

/**
 * @version 2.0
 * @author Admin
 * @implNote Analizador princiapl
 *
 */
public class AnManager {
	public static String pathDir;
	//Contador de lineas para los errores (Lï¿½xico y sintacico, semantico)
	public static int contadorLineas=1;
	public static int lineasST = 1;
	//Lista de tokens para el analizador sintactico
	public static ArrayList <Token> listaTokens = new ArrayList<Token>();
	public static int posicionLista = 0;

	//Variable que servira para detectar si ha habido errores o no
	public static boolean sinErrores = true;
	
	public static void setPath(String path){
		pathDir = path.replace("\\", "/");
	}
	
	public static Token pedirTokenAlex(){
		if (posicionLista < listaTokens.size()){
			if (listaTokens.get(posicionLista).tipoToken.equals("EOL")){
				lineasST++;//Sumamos linea
			}
			return listaTokens.get(posicionLista++);
		}else{
			return null;
		}
	}
	public static Token comprobarSiguienteToken(){
		if (posicionLista < listaTokens.size()){
			return listaTokens.get(posicionLista);
		}else{
			return null;
		}
	}
	public static String getPath(){
		return pathDir;
	}

	
	
	/**
	 * @implNote Proceso Principal 
	 * @param file
	 * @throws IOException
	 */
	public static void pPrincipal(BufferedReader file) throws IOException{
		//Generacion de archivos
		genArchivoGramatica();
		rellenarGramatica();
		Token.genArchivoTokens();
		Errores.genArchivoErrores();
		AnalizadorSinSem.genArchivoParse();
		AnalizadorSinSem.genArchivoTS();
		//Proceso de lectura de tokens
		int state = 0;
		Accion accion;
		String concatenacion = "";
		int c;
		
		/**
		 * Contador para los identificadores
		 */
		int contador= 0;
		
		while((c = file.read()) != -1) {
			char character = (char) c; //Leemos un caracter
			accion = Token.estado(state, character);
			state = accion.estado;
			if(character == '\n') contadorLineas++; //Incrementamos contador lineas si encontramos un salto de linea
			//Accion: leer 0
			//        No leer 1, es decir, volvemos a ejecutar con otro estado
			//        Leer y Concatenar 2
			//        Comprobar palRes 3
			//        GenToken Numero 4
			//        GenToken Cadena 5
			while(accion.accion != 0){
				if (accion.accion == 1){
					accion = Token.estado(state, character); 
					state = accion.estado;
					continue;
				}
				if (accion.accion == 2){
					concatenacion = concatenacion + character;
					break;
				}
				if (accion.accion == 3){
					//Este año se han añadido alert y number, por tanto habrá que tratarlas en el semántico y sintáctico
					/**
					 * @see Pendiente: A.sintactico y semántico, revisar con los nuevos tokens (alert  ) y el operador Divigual '/='.
					 * @see Se ha añadido también el token number en vez de int(Se supone que esto ya está corregido)
					 */
					 //concatenacion.matches("\\b(?:true|var|int|if|false|while|function|bool|string|return|print|prompt)\\b")
					if(concatenacion.matches("\\b(?:true|let|number|if|false|while|function|boolean|string|return|alert|input)\\b")){
						Token.escribirToken(concatenacion, "",contadorLineas); //Palabra reservada
					}else{
						contador++;
						Token.escribirTokenID("ID", concatenacion,contadorLineas, contador); //Generamos ID
					}
					concatenacion=""; //Reset
					//Leemos caracter actual:
					accion = Token.estado(state, character); 
					state = accion.estado;
					continue;
				}
				if (accion.accion == 4){
					//Pasamos el string numero a int
					int n = Integer.parseInt(concatenacion);
					if (n<=32767){
						Token.escribirToken("Entero", concatenacion,contadorLineas); //Numero
					}else{
						Errores.escribirError("Analizador lexico","El numero "+n+" sobrepasa el valor permitido" , AnManager.contadorLineas );
					}
					concatenacion = "";
					//Leemos caracter actual:
					accion = Token.estado(state, character); 
					state = accion.estado;
					continue;
				}
				if (accion.accion == 5){
					Token.escribirToken("Cadena", concatenacion+"\"",contadorLineas); //Cadena
					concatenacion=""; //Reset
					break;
				}
			}//de while
		}// de while principal
		Token.escribirToken("EOF", " ",contadorLineas); //Fin de fichero
		
		//Una vez termina el analizador lï¿½xico y generamos los tokens, ejecutamos analizador Sintï¿½ctico/Semï¿½ntico
		AnalizadorSinSem.AnalizadorSt();
		//Si no hay errores escribimos: "Sin errores"
		if (sinErrores){
			Errores.escribirOK();
		}
		//Fin
	}
	
	
	
	/**
	 * Genera gramatica en la capeta destino
	 */
	public static void genArchivoGramatica(){
		String path = AnManager.getPath() + File.separator +"Resultados Grupo64" + File.separator+ "Gramatica.txt";
		File f = new File(path);
		f.getParentFile().mkdirs(); 
		try {
			f.delete(); //Eliminamos si existe algo antes
			f.createNewFile();
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, "Fallo al crear archivo Gramatica");
		}
	}

	

	/**
	 * @implNote Gramatica que se usara en Vast
	 */
	public static void rellenarGramatica(){
		PrintWriter pw = null;
		try {
			File file = new File(AnManager.getPath() + File.separator +"Resultados Grupo64" + File.separator+ "Gramatica.txt");
			FileWriter fw = new FileWriter(file, true);
			pw = new PrintWriter(fw);
			pw.println("");
		  //pw.println("Terminales = { var id ; if =  {  } (  ) ! != +  ++ , function int string bool true false return print prompt entero cadena while eof }");recuerda añadir alert() y el operador '/=' Divigual
			pw.println("Terminales = { let id ; if = {  } (  ) ! != + , function number string boolean true false return alert input entero cadena while /= eof }");//cambio
			pw.println("");
//			pw.println("NoTerminales = { P B T S Sa X C F H A K L Q R Ra U Ua V Va Vb }");
			pw.println("NoTerminales = { P B T S Sa X C F H A K L Q R Ra U Ua V Ep Va Vb }");//cambio
			pw.println("");
			pw.println("Axioma = P");
			pw.println("");
			pw.println("Producciones = {");
			pw.println("P -> B P");//1
			pw.println("P -> F P");//2
			pw.println("P -> eof");//3
			pw.println("B -> let T id ;");//4
			pw.println("B -> if ( R ) S");//5
			pw.println("B -> while ( R ) { C }");//6
			pw.println("B -> S");//7
			pw.println("T -> number");//8
			pw.println("T -> boolean");//9
			pw.println("T -> string");//10
			pw.println("S -> id Sa");//11   Cuidado con esto, ya que puede declarar y a la vez, inicializar con "/="
			pw.println("S -> return X ;");//12
			pw.println("S -> alert ( R ) ; ");//13
			pw.println("S -> input ( id ) ;");//14
			pw.println("Sa -> = R ; ");//15
			pw.println("Sa -> != R ; ");//16
			pw.println("Sa -> ( L ) ;");//17
			pw.println("X -> R");//18
			pw.println("X -> lambda");//19
			pw.println("C -> B C");//20
			pw.println("C -> lambda");//21
			pw.println("F -> function H id ( A ) { C }");//22
			pw.println("H -> T");//23
			pw.println("H -> lambda");//24
			pw.println("A -> T id K");//25
			pw.println("A -> lambda");//26
			pw.println("K -> , T id K");//27
			pw.println("K -> lambda");//28
			pw.println("L -> R Q");//29
			pw.println("L -> lambda");//30
			pw.println("Q -> , R Q");//31
			pw.println("Q -> lambda");//32
			pw.println("R -> U Ra");//33
			pw.println("Ra -> = R");//34
			pw.println("Ra -> != R");//35
			pw.println("Ra -> lambda");//36
			pw.println("U -> V Ua");//37
			pw.println("Ua -> + U");//38
			pw.println("Ua -> lambda");//39
			pw.println("V -> id Va");//40
			pw.println("V -> entero");//41
			pw.println("V -> cadena");//42
//			pw.println("V -> ++ id");//43
			pw.println("V -> true");//43
			pw.println("V -> false");//44
			pw.println("V -> ( R )");//45
			pw.println("V -> ! Vb");//46
			pw.println("Ep -> /= T Ep");//47 cambio
			pw.println("Ep -> lambda");//48 cambio
			pw.println("Va -> ( L )");//49
			pw.println("Va -> lambda");//50
			pw.println("Vb -> true");//51
			pw.println("Vb -> false");//52
			pw.println("Vb -> id");//53
			pw.println("Vb -> ( R )");//54
			pw.println("Sa -> /= R ;");//55cambio
			pw.println("}");
			pw.println("");
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (pw != null) {
				pw.close();
			}
		}
	}//gramatica
}
