package Scripts;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import javax.swing.JOptionPane;


/**
 * @version 2.0
 * @author Admin
 * @implNote Esta clase ejecuta el funcionamiento del analizador sintactico y semantico
 *
 */
public class AnalizadorSinSem {
	//Constantes para calcular el desplazamiento seg�n el tipo de variable
	private final static int desInt = 1; //Las variables enteras son 1 palabras 
	private final static int desChars = 64; //Las Cadenas son 1 palabra
	private final static int desBool = 1; //Los booleanos son 64 palabras

	//Variables necesarias para las tablas de s�mbolos
	public static itemTS T, Sa, X, H, L, R, Ra, U, Ua, V, Vb;// se ha a�adido un Vb tipo
	public static ArrayList <ArrayList <itemTS>> tablasSimbolos;
	private static ArrayList <itemTS> TSG; 	//TSG: TSimbolos Global
	private static ArrayList <itemTS> TSL; 	//TSL: TSimbolos Local
	private static ArrayList <String> tituloFunciones = new ArrayList<String>();
	private static boolean condicional = false;

	//Variables que controlan las funciones (Tablas locales)
	private static itemTS functionActual;
	private static int idFunction = 1;
	private static boolean llamadaFuncion = false;
	
	//Variables auxiliares para comprobar las expresiones
	private static boolean RhayError = false;
	private static boolean UaSuma = false;
	private static boolean RaRel = false;
	private static ArrayList <String> LArgumentos = new ArrayList<String>();
	private static ArrayList <String> VaArgumentos = new ArrayList<String>();
	//Token que estamos analizando
	public static Token sgtetoken;
	
	
	/**
	 * @implNote Programa principal
	 * @see Representara P', que es por donde se empieza siempre
	 * @see Iniciara las variables que se crean necesarias 
	 */
	public static void AnalizadorSt(){
		//El analizador sintactico es el Descendente Recursivo:
		escribirParse("Descendente");
		//Pedimos primer token
		sgtetoken = AnManager.pedirTokenAlex();
		//Tablas de s�mbolos
		TSG = new ArrayList<itemTS>();
		TSL = new ArrayList<itemTS>();
		tablasSimbolos = new ArrayList<ArrayList <itemTS>>();
		//Inicializamos variables necesarias para el analizador semantico
		T = Sa = X = H = L  = R = Ra = U = Ua = V = Vb =new itemTS();
		//La primera tabla de s�mbolos ser� la global
		tablasSimbolos.add(0,TSG);
		//Llamamos al axioma
		P();
		//Actualizamos tabla de s�mbolos global
		tablasSimbolos.set(0,TSG);
		//Imprimimos todas las tablas
		imprimirTablas();
	}

		
	/**
	 * @implNote Funcion por cada regla. 
	 * @see Todo esto hay que mirarlo en la Tabla LL1 que genera VAST(En nuestro caso usaremos este programa para generar la tabla LL1)
	 */
	public static void P(){// 9 casos
		if (sgtetoken == null){
			return;
		}else{
			switch(sgtetoken.tipoToken){
			case "let"://1
				escribirParse("1");
				B();
				P();
				return;
			case "if"://2
				escribirParse("1");
				B();
				P();
				return;
			case "ID"://3
				escribirParse("1");
				B();
				P();
				return;
			case "return"://4
				escribirParse("1");
				B();
				P();
				return;
			case "input"://5
				escribirParse("1");
				B();
				P();
				return;
			case "alert"://6
				escribirParse("1");
				B();
				P();
				return;
			case "while"://7
				escribirParse("1");
				B();
				P();
				return;
			case "function"://8
				escribirParse("2");
				F();
				P();
				return;
			case "EOL": //Ignoramos saltos de linea//excepcion
				sgtetoken = AnManager.pedirTokenAlex();
				P();
				return;
			case "EOF": //Ignoramos saltos de linea//9
				escribirParse("3");
				return;
			default:
				Errores.escribirError("Analizador sintactico", "No se permite la expresi�n"+sgtetoken.tipoToken, AnManager.lineasST);
				Errores.panicMode();
				P();
				break;
			}	

		}
	}

	
	public static void B(){// 7 casos 
		if (sgtetoken == null){
			Errores.escribirError("Analizador sintactico", "No hay mas palabras a analizar", AnManager.lineasST);
			return;
		}else{
			switch(sgtetoken.tipoToken){
			case "let"://1
				escribirParse("4");
				sgtetoken = AnManager.pedirTokenAlex();
				T();
				if(sgtetoken.tipoToken.equals("ID")){
					//Analizador semantico:
					if (functionActual == null){ //Si no hay local, buscamos en la global
						if (buscaTS(TSG, sgtetoken.attrToken) != null){
							Errores.escribirError("Analizador semantico", "La variable "+sgtetoken.attrToken+" ya se ha declarado anteriormente", AnManager.lineasST);
						}else{
							itemTS a = new itemTS();
							a.lexema = sgtetoken.attrToken;
							a.desplazamiento = calculoDesplazamiento(T.tipo);
							a.tipo = T.tipo;
							TSG.add(a);
						}
					}else{//Si hay local, buscamos en la tabla local
						if (buscaTS(TSL, sgtetoken.attrToken) != null){
							Errores.escribirError("Analizador semantico", "La variable "+sgtetoken.attrToken+" ya se ha declarado anteriormente", AnManager.lineasST);
						}else{
							itemTS a = new itemTS();
							a.lexema = sgtetoken.attrToken;
							a.desplazamiento = calculoDesplazamiento(T.tipo);
							a.tipo = T.tipo;
							TSL.add(a);
						}
					}
					sgtetoken = AnManager.pedirTokenAlex();

					if(sgtetoken.tipoToken.equals("PuntoComa")){
						sgtetoken = AnManager.pedirTokenAlex();
					}else{
						Errores.escribirError("Analizador sintactico", "Falta un ';' antes de recibir "+sgtetoken.tipoToken, AnManager.lineasST);
						//Tratamiento de error:
						if (AnManager.comprobarSiguienteToken().tipoToken.equals("PuntoComa")){ //Si el siguiente token es el que esperamos, continuamos normalmente
							sgtetoken = AnManager.pedirTokenAlex();
							sgtetoken = AnManager.pedirTokenAlex();
						}else{ //Si no es lo que esperamos, procedemos al PanicMode
							Errores.panicMode();
						}
					}
				}else{
					Errores.escribirError("Analizador sintactico", "Deberia haber un identificador", AnManager.lineasST);
					//Tratamiento de error:
					if (AnManager.comprobarSiguienteToken().tipoToken.equals("PuntoComa")){ //Si el siguiente token es el que esperamos, continuamos normalmente
						sgtetoken = AnManager.pedirTokenAlex();
						sgtetoken = AnManager.pedirTokenAlex();
					}else{ //Si no es lo que esperamos, procedemos al PanicMode
						Errores.panicMode();
					}
				}

				return;
				
			case "if"://2
				escribirParse("5");
				sgtetoken = AnManager.pedirTokenAlex();
				if(sgtetoken.tipoToken.equals("ap")){
					sgtetoken = AnManager.pedirTokenAlex();

					condicional = true; //Estamos en un condicional
					R();
					if (RhayError){	
						//Errores.escribirError("Analizador semantico", "No es correcta la expresion", AnManager.lineasST);
						RhayError = false;
					}
					//semantico
					if(!R.tipo.equals("Bool")){
						Errores.escribirError("Analizador semantico", "No es una expresion condicional lo que se encuentra en el if", AnManager.lineasST);
					}

					if(sgtetoken.tipoToken.equals("cp")){
						sgtetoken = AnManager.pedirTokenAlex();
						S();
						condicional = false;
					}else{
						Errores.escribirError("Analizador sintactico", "Falta un ')' antes de recibir la expresi�n "+"'"+sgtetoken.tipoToken+"'", AnManager.lineasST);
						//Tratamiento de error:
						Errores.panicMode();
					}	
				}else{
					Errores.escribirError("Analizador sintactico", "DD:\\Universidad\\Fpdl\\g64\\pruebasdefensaeber�a haber un par�ntesis tras el 'if' antes de recibir el token <"+sgtetoken.tipoToken, AnManager.lineasST);
					//Tratamiento de error:
					Errores.panicMode();
				}
				return;
				//FIRST 'S'
			case "ID"://3
				escribirParse("7");
				S();
				return;
			case "return"://4
				escribirParse("7");
				S();
				return;
			case "alert"://5
				escribirParse("7");
				S();
				return;
			case "input"://6
				escribirParse("7");
				S();
				return;
			case "while"://7
				escribirParse("6");
				sgtetoken = AnManager.pedirTokenAlex();
				if(sgtetoken.tipoToken.equals("ap")){
					sgtetoken = AnManager.pedirTokenAlex();	
				}else{
					Errores.escribirError("Analizador sintactico", "Deberia haber un par�ntesis tras el 'while' antes de recibir la expresi�n "+sgtetoken.tipoToken, AnManager.lineasST);
					//Tratamiento de error:
					if (AnManager.comprobarSiguienteToken().tipoToken.equals("ap")){ //Si el siguiente token es el que esperamos, continuamos normalmente
						sgtetoken = AnManager.pedirTokenAlex();
						sgtetoken = AnManager.pedirTokenAlex();
					}else{ //Si no es lo que esperamos, procedemos al PanicMode
						Errores.panicModeLlaves(0);
						return;
					}
				}
				R();
				if (!R.tipo.equals("Bool")){
					Errores.escribirError("Analizador semantico", "La expresion en el 'while' no es una condicion", AnManager.lineasST);
				}
				if(sgtetoken.tipoToken.equals("cp")){
					sgtetoken = AnManager.pedirTokenAlex();
				}else{
					Errores.escribirError("Analizador sintactico", "Falta un ')' en el while, no se entiende la expresi�n", AnManager.lineasST);
					//Tratamiento de error:
					if (AnManager.comprobarSiguienteToken().tipoToken.equals("cp")){ //Si el siguiente token es el que esperamos, continuamos normalmente
						sgtetoken = AnManager.pedirTokenAlex();
						sgtetoken = AnManager.pedirTokenAlex();
					}else{ //Si no es lo que esperamos, procedemos al PanicMode
						Errores.panicModeLlaves(0);
						return;
					}
				}
				if(sgtetoken.tipoToken.equals("al")){
					sgtetoken = AnManager.pedirTokenAlex();
				}else{
					Errores.escribirError("Analizador sintactico", "Falta un '{' antes de recibir la expresi�n "+sgtetoken.tipoToken+" en dicha l�nea", AnManager.lineasST);
					//Tratamiento de error:
					if (AnManager.comprobarSiguienteToken().tipoToken.equals("al")){ //Si el siguiente token es el que esperamos, continuamos normalmente
						sgtetoken = AnManager.pedirTokenAlex();
						sgtetoken = AnManager.pedirTokenAlex();
					}else{ //Si no es lo que esperamos, procedemos al PanicMode
						Errores.panicModeLlaves(0);
						return;
					}
				}
				C();
				if (RhayError){	
					//Errores.escribirError("Analizador semantico", "No es correcta la expresion", AnManager.lineasST);
					RhayError = false;
				}

				if(sgtetoken.tipoToken.equals("cl")){
					sgtetoken = AnManager.pedirTokenAlex();
				}else if (sgtetoken.tipoToken.equals("EOL")){
					Errores.escribirError("Analizador sintactico", "Falta un '}' antes de recibir la expresi�n "+sgtetoken.tipoToken, AnManager.lineasST);
					//Tratamiento de error:
					if (AnManager.comprobarSiguienteToken().tipoToken.equals("cl")){ //Si el siguiente token es el que esperamos, continuamos normalmente
						sgtetoken = AnManager.pedirTokenAlex();
						sgtetoken = AnManager.pedirTokenAlex();
					}else{ //Si no es lo que esperamos, procedemos al PanicMode
						Errores.panicModeLlaves(0);
						return;
					}
				}
				if (sgtetoken.tipoToken.equals("EOL")){ //Permitimos aqui un salto de linea ///Excepcion
					sgtetoken = AnManager.pedirTokenAlex();
				}			
			case "EOF":
				return;
			default:
				Errores.escribirError("Analizador sintactico", "No se permite la expresi�n "+sgtetoken.tipoToken, AnManager.lineasST);
				break;
			}	
		}
	}
	
	
	public static void T(){//3 casos 
		if (sgtetoken == null){
			Errores.escribirError("Analizador sintactico", "No hay mas tokens", AnManager.lineasST);
			return;
		}else{
			switch(sgtetoken.tipoToken){
			case "number"://1
				escribirParse("8");

				//Analizador semantico
				T.tipo = "Entero";

				sgtetoken = AnManager.pedirTokenAlex();
				return;
			case "string"://2
				escribirParse("10");

				//Analizador semantico
				T.tipo = "string";

				sgtetoken = AnManager.pedirTokenAlex();
				return;
			case "boolean"://3
				escribirParse("9");

				//Analizador semantico
				T.tipo = "Bool";

				sgtetoken = AnManager.pedirTokenAlex();
				return;
			default:
				Errores.escribirError("Analizador sintactico", "No se permite la expresi�n "+sgtetoken.tipoToken+ " aqui, deberia haber un 'Tipo'", AnManager.lineasST);
				break;
			}	
		}
	}
	
	
	public static void S(){// 4 casos 
		if (sgtetoken == null){
			Errores.escribirError("Analizador sintactico", "No hay mas palabras a analizar", AnManager.lineasST);
			return;
		}else{
			switch(sgtetoken.tipoToken){
			case "ID":/************************* 1 **************************************************************/
				escribirParse("11");

				//Analizador semantico:
				itemTS item = null;
				String lexema = sgtetoken.attrToken;

				//Vamos a comprobar si existe el id en alguna tabla, si no, lo consideramos entero y lo creamos
				if (functionActual == null){ //Tabla global
					if ((item = buscaTS(TSG, lexema)) == null){ //No estan en la tabla global
						//La consideramos variable entera y la metemos en TSG
						item = new itemTS();
						item.tipo = "Entero";
						item.desplazamiento = calculoDesplazamiento("Entero");
						item.lexema = lexema;
						TSG.add(item);
					}
				}else{
					if (!lexema.equals(functionActual.lexema)&&(item = buscaTS(TSL, lexema)) == null && (item = buscaTS(TSG, lexema)) == null){ //No est� en la tabla global
						//La consideramos variable entera y la metemos en TSG
						item = new itemTS();
						item.tipo = "Entero";
						item.desplazamiento = calculoDesplazamiento("Entero");
						item.lexema = lexema;
						TSG.add(item);
					}
				}

				sgtetoken = AnManager.pedirTokenAlex();
				Sa();
				if (Sa.tipo == null){ //Se ha llamado a una Funcion
					if ((item = buscaTS(TSG, lexema)) == null){ //La Funcion no existe
						if(functionActual != null && (item = functionActual).lexema.equals(lexema)){ //Puede ser una llamada recursiva
							int i=0;
							boolean aux = true;
							if (item.argumentos == LArgumentos.size()){
								while(i < item.argumentos){
									if (!TSL.get(i).tipo.equals(LArgumentos.get(i))){ //Vamos a la tabla de la Funcion y comprobamos las variables correspondientes a los par�metros
										aux = false;
									}
									i++;
								}
								if(!aux){
									Errores.escribirError("Analizador semantico", "No se ha llamado a la funcion "+item.lexema+" con los argumentos correctos", AnManager.lineasST-1);	
								}
							}else{
								Errores.escribirError("Analizador semantico", "No se ha llamado a la funcion "+item.lexema+" con los argumentos correctos", AnManager.lineasST-1);	
							}
					}else{
						//La Funcion no existe
						Errores.escribirError("Analizador semantico", "La funcion "+lexema+" no se ha declarado", AnManager.lineasST-1);
					}
						
					}else{//La Funcion existe y hay que comprobar si se han utilizado los argumentos correctos
						int i=0;
						boolean aux = true;
						if (item.argumentos == LArgumentos.size()){
							while(i < item.argumentos){
								if (!tablasSimbolos.get(item.entID).get(i).tipo.equals(LArgumentos.get(i))){ //Vamos a la tabla de la Funcion y comprobamos las variables correspondientes a los par�metros
									aux = false;
								}
								i++;
							}
							if(!aux){
								Errores.escribirError("Analizador semantico", "No se ha llamado a la funcion "+item.lexema+" con los argumentos correctos", AnManager.lineasST-1);	
							}
						}
					}
				}else{//Se ha asignado valor a un id
					if (!Sa.tipo.equals(item.tipo)){ //Si no es del mismo tipo, error
						Errores.escribirError("Analizador semantico", "No se puede asignar un valor de tipo "+Sa.tipo+" a la variable "+ lexema+" de tipo "+ item.tipo, AnManager.lineasST-1);
					}
				}
				LArgumentos.clear();
				return;

			case "return":/************************* 2 **************************************************************/
				escribirParse("12");
				sgtetoken = AnManager.pedirTokenAlex();
				X();
				//Analizador semantico
				if(functionActual != null){
					//Si ya Devuelto es false significa que ya se hab�a ejecutado un return
					if (X.tipo.equals(functionActual.tipoDevuelto)){
						if (!condicional){ //Si no estamos en un if, tenemos en cuenta que ya se ha devuelto el valor
							functionActual.yaDevuelto = true;
						}

					}else{
						Errores.escribirError("Analizador semantico","La funcion "+ functionActual.lexema + " no puede devolver un "+X.tipo+", tiene que devolver un "+functionActual.tipoDevuelto, AnManager.lineasST);
						functionActual.yaDevuelto = true; //Devuelve algo aunque sea erroneo
					}
				}else{
					Errores.escribirError("Analizador semantico","No se puede ejecutar un return aqui ", AnManager.lineasST);	
				}

				if (sgtetoken.tipoToken.equals("PuntoComa")){
					sgtetoken = AnManager.pedirTokenAlex();
				}else{
					Errores.escribirError("Analizador sintactico", "Se esperaba ';' y se ha recibido la expresion "+sgtetoken.tipoToken+","+sgtetoken.attrToken, AnManager.lineasST);
					Errores.panicMode();
				}
				return;

			case "input":/************************* 3 **************************************************************/
				escribirParse("14");
				sgtetoken = AnManager.pedirTokenAlex();
				if (sgtetoken.tipoToken.equals("ap")){
					sgtetoken = AnManager.pedirTokenAlex();
				}else{
					Errores.escribirError("Analizador sintactico", "Se esperaba '(' y se ha recibido el token <"+sgtetoken.tipoToken+","+sgtetoken.attrToken+"> aqui", AnManager.lineasST);
					//Tratamiento de error:
					if (AnManager.comprobarSiguienteToken().tipoToken.equals("ID")){ //Si el siguiente token es el que esperamos, continuamos normalmente
						sgtetoken = AnManager.pedirTokenAlex();
						sgtetoken = AnManager.pedirTokenAlex();
					}else{ //Si no es lo que esperamos, procedemos al PanicMode
						Errores.panicMode();
						return;
					}
				}


				if (sgtetoken.tipoToken.equals("ID")){
					itemTS a;
					//Analizador semantico:
					if (functionActual == null){
						if ((a = buscaTS(TSG, sgtetoken.attrToken)) != null){
							if(a.tipo.equals("Entero") || buscaTS(TSG, sgtetoken.attrToken).tipo.equals("string")){
								//Todo OK
							}else{
								Errores.escribirError("Analizador semantico", "La variable "+sgtetoken.attrToken+" no es de tipo 'Entero' o 'string'", AnManager.lineasST);
							}
						}else{ //Si no, la consideramos variable de tipo entero
							a = new itemTS();
							a.lexema = sgtetoken.attrToken;
							a.tipo = "Entero";
							a.desplazamiento = calculoDesplazamiento("Entero");
							TSG.add(a);
						}
					}else{
						if ((a = buscaTS(TSG, sgtetoken.attrToken)) == null && (a = buscaTS(TSL, sgtetoken.attrToken)) == null){
							//La consideramos entera y la insertamos en la tabla global
							a = new itemTS();
							a.lexema = sgtetoken.attrToken;
							a.tipo = "Entero";
							a.desplazamiento = calculoDesplazamiento("Entero");
							TSG.add(a);
						}else{  //Est� en la tabla y tenemos que comprobar si es de tipo entero y chars
							if(a.tipo.equals("Entero") || a.tipo.equals("string")){
								//Todo OK
							}else{
								Errores.escribirError("Analizador semantico", "La variable "+sgtetoken.attrToken+" no es de tipo 'Entero' o 'string'", AnManager.lineasST);
							}
						}
					}

					sgtetoken = AnManager.pedirTokenAlex();
				}else{
					Errores.escribirError("Analizador sintactico", "Se esperaba un identificador y se ha recibido la expresi�n "+sgtetoken.tipoToken, AnManager.lineasST);
					//Tratamiento de error:
					if (AnManager.comprobarSiguienteToken().tipoToken.equals("cp")){ //Si el siguiente token es el que esperamos, continuamos normalmente
						sgtetoken = AnManager.pedirTokenAlex();
						sgtetoken = AnManager.pedirTokenAlex();
					}else{ //Si no es lo que esperamos, procedemos al PanicMode
						Errores.panicMode();
						return;
					}
				}
				if (sgtetoken.tipoToken.equals("cp")){
					sgtetoken = AnManager.pedirTokenAlex();
				}else{
					Errores.escribirError("Analizador sintactico", "Se esperaba un ')' y se ha recibido la expresi�n "+sgtetoken.tipoToken, AnManager.lineasST);
					//Tratamiento de error:
					if (AnManager.comprobarSiguienteToken().tipoToken.equals("PuntoComa")){ //Si el siguiente token es el que esperamos, continuamos normalmente
						sgtetoken = AnManager.pedirTokenAlex();
						sgtetoken = AnManager.pedirTokenAlex();
					}else{ //Si no es lo que esperamos, procedemos al PanicMode
						Errores.panicMode();
						return;
					}
				}
				if (sgtetoken.tipoToken.equals("PuntoComa")){
					sgtetoken = AnManager.pedirTokenAlex();
				}else{
					Errores.escribirError("Analizador sintactico", "Se esperaba un ';' y se ha recibido la expresion "+sgtetoken.tipoToken, AnManager.lineasST);
					Errores.panicMode();
				}
				return;
				
			case "alert":/************************* 4 **************************************************************/
				escribirParse("13");
				sgtetoken = AnManager.pedirTokenAlex();
				if (sgtetoken.tipoToken.equals("ap")){
					sgtetoken = AnManager.pedirTokenAlex();
				}else{
					Errores.escribirError("Analizador sintactico", "Se esperaba '(' y se ha recibido la expresi�n "+sgtetoken.tipoToken, AnManager.lineasST);
					Errores.panicMode();
				}
				R();
				if (RhayError){	
					//Errores.escribirError("Analizador semantico", "No es correcta la expresion", AnManager.lineasST);
					RhayError = false;
				}
				if (sgtetoken.tipoToken.equals("cp")){
					sgtetoken = AnManager.pedirTokenAlex();
				}else{
					Errores.escribirError("Analizador sintactico", "Se esperaba un ')' y se ha recibido la expresi�n "+sgtetoken.tipoToken, AnManager.lineasST);
					//Tratamiento de error:
					if (AnManager.comprobarSiguienteToken().tipoToken.equals("PuntoComa")){ //Si el siguiente token es el que esperamos, continuamos normalmente
						sgtetoken = AnManager.pedirTokenAlex();
						sgtetoken = AnManager.pedirTokenAlex();
					}else{ //Si no es lo que esperamos, procedemos al PanicMode
						Errores.panicMode();
						return;
					}
				}
				if (sgtetoken.tipoToken.equals("PuntoComa")){
					sgtetoken = AnManager.pedirTokenAlex();
				}else{
					Errores.escribirError("Analizador sintactico", "Se esperaba un ';' y se ha recibido la expresion "+sgtetoken.tipoToken, AnManager.lineasST);
					Errores.panicMode();
				}
				return;
				
			default:
				Errores.escribirError("Analizador sintactico", "No se permite el token "+sgtetoken.tipoToken + " en dicha l�nea", AnManager.lineasST);
				break;
			}	
		}
	}
	
	
	public static void Sa(){ // 4
		if (sgtetoken == null){
			Errores.escribirError("Analizador sintactico", "No hay mas palabras a analizar", AnManager.lineasST);
			return;
		}else{
			switch(sgtetoken.tipoToken){
			case "igual":/*************************1**************************************************************/
				escribirParse("15");
				sgtetoken = AnManager.pedirTokenAlex();

				R();
				if (RhayError){	
				//	//Errores.escribirError("Analizador semantico", "No es correcta la expresion", AnManager.lineasST);
					RhayError = false;
				}
				//Analizador semantico
				Sa.tipo = R.tipo;		

				if (sgtetoken.tipoToken.equals("PuntoComa")){
					sgtetoken = AnManager.pedirTokenAlex();
				}
				else{
					Errores.escribirError("Analizador sintactico", "Se esperaba ';' y se ha recibido la expresion "+sgtetoken.tipoToken, AnManager.lineasST);
					Errores.panicMode();
				}
				return;
				
			case "ap":/*************************2**************************************************************/
				escribirParse("17");
				sgtetoken = AnManager.pedirTokenAlex();
				L();
				Sa.tipo = null;
				//Analizador semantico
				if (sgtetoken.tipoToken.equals("cp")){
					sgtetoken = AnManager.pedirTokenAlex();
				}else{
					Errores.escribirError("Analizador sintactico", "Se esperaba ')' y se ha recibido la expresion "+sgtetoken.tipoToken, AnManager.lineasST);
					//Tratamiento de error:
					if (AnManager.comprobarSiguienteToken().tipoToken.equals("PuntoComa")){ //Si el siguiente token es el que esperamos, continuamos normalmente
						sgtetoken = AnManager.pedirTokenAlex();
						sgtetoken = AnManager.pedirTokenAlex();
					}else{ //Si no es lo que esperamos, procedemos al PanicMode
						Errores.panicMode();
						return;
					}
				}
				if (sgtetoken.tipoToken.equals("PuntoComa")){
					sgtetoken = AnManager.pedirTokenAlex();
				}else{
					Errores.escribirError("Analizador sintactico", "Se esperaba ';'. Se ha recibido la expresi�n "+sgtetoken.tipoToken, AnManager.lineasST);
					Errores.panicMode();
				}
				return;
				
			case "distinto":/************************* 3 **************************************************************/
				escribirParse("16");
				sgtetoken = AnManager.pedirTokenAlex();

				R();
				if (RhayError){	
				//	//Errores.escribirError("Analizador semantico", "No es correcta la expresion", AnManager.lineasST);
					RhayError = false;
				}
				//Analizador semantico
				Sa.tipo = R.tipo;		

				if (sgtetoken.tipoToken.equals("PuntoComa")){
					sgtetoken = AnManager.pedirTokenAlex();
				}
				else{
					Errores.escribirError("Analizador sintactico", "Se esperaba ';'. Se ha recibido la expresion "+sgtetoken.tipoToken, AnManager.lineasST);
					Errores.panicMode();
				}
				return;
				
			case "Divigual": /************************* 4 **************************************************************/
				escribirParse("55");
				sgtetoken = AnManager.pedirTokenAlex();
				
				R();
				if (RhayError){	
					//	//Errores.escribirError("Analizador semantico", "No es correcta la expresion", AnManager.lineasST);
						RhayError = false;
					}
					//Analizador semantico
					Sa.tipo = R.tipo;		

					if (sgtetoken.tipoToken.equals("PuntoComa")){
						sgtetoken = AnManager.pedirTokenAlex();
					}
					else{
						Errores.escribirError("Analizador sintactico", "Se esperaba ';'. Se ha recibido la expresion "+sgtetoken.tipoToken, AnManager.lineasST);
						Errores.panicMode();
					}
					return;
			default:
				Errores.escribirError("Analizador sintactico", "No se permite el token "+sgtetoken.tipoToken, AnManager.lineasST);
				break;	
				
			}
		}
	}
	/////////////////////////////////////////////////////////////////////////TE QUEDASTE AQUI////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public static void X(){
		if (sgtetoken == null){
			Errores.escribirError("Analizador sintactico", "No hay mas tokens", AnManager.lineasST);
			return;
		}else{
			switch(sgtetoken.tipoToken){
			//First E
			case "ID"://1
				escribirParse("18");
				R();
				if (RhayError){	
				//	//Errores.escribirError("Analizador semantico", "No es correcta la expresion", AnManager.lineasST);
					RhayError = false;
				}
				//Analizador semantico
				X.tipo = R.tipo;
				return;
				
			case "Entero"://2
				escribirParse("18");
				R();
				if (RhayError){	
				//	//Errores.escribirError("Analizador semantico", "No es correcta la expresion", AnManager.lineasST);
					RhayError = false;
				}
				//Analizador semantico
				X.tipo = R.tipo;
				return;
				
			case "Cadena"://3
				escribirParse("18");
				R();
				if (RhayError){	
				//	//Errores.escribirError("Analizador semantico", "No es correcta la expresion", AnManager.lineasST);
					RhayError = false;
				}
				//Analizador semantico
				X.tipo = R.tipo;
				return;
				
//			case "Preincremento":
//				escribirParse("18");
//				R();
//				if (RhayError){	
//				//	//Errores.escribirError("Analizador semantico", "No es correcta la expresion", AnManager.lineasST); 
//					RhayError = false;
//				}
//				//Analizador semantico
//				X.tipo = R.tipo;
//				return;
				
			case "true"://4
				escribirParse("18");
				R();
				if (RhayError){	
				//	//Errores.escribirError("Analizador semantico", "No es correcta la expresion", AnManager.lineasST);
					RhayError = false;
				}
				//Analizador semantico
				X.tipo = R.tipo;
				return;
				
			case "false"://5
				escribirParse("18");
				R();
				if (RhayError){	
					//Errores.escribirError("Analizador semantico", "No es correcta la expresion", AnManager.lineasST);
					RhayError = false;
				}
				//Analizador semantico
				X.tipo = R.tipo;
				return;
				
			case "ap"://6
				escribirParse("18");
				R();
				if (RhayError){	
				//	//Errores.escribirError("Analizador semantico", "No es correcta la expresion", AnManager.lineasST);
					RhayError = false;
				}
				//Analizador semantico
				X.tipo = R.tipo;
				return;
				
			case "PuntoComa": //Follow//7
				escribirParse("19");
				//Analizador semantico
				X.tipo = "Void";
				return; 
				
			case "negacion":
				escribirParse("18");
				R();
				if (RhayError){	
				//	//Errores.escribirError("Analizador semantico", "No es correcta la expresion", AnManager.lineasST);
					RhayError = false;
				}
				//Analizador semantico
				X.tipo = R.tipo;
				return;
				
			default:
				Errores.escribirError("Analizador sintactico", "No se permite la expresi�n "+sgtetoken.tipoToken, AnManager.lineasST);
				break;
			}	
		}
	}

	
	public static void C(){
		if (sgtetoken == null){
			Errores.escribirError("Analizador sintactico", "No hay mas palabras a analizar", AnManager.lineasST);
			return;
		}else{
			switch(sgtetoken.tipoToken){
			//First B
			case "let"://1
				escribirParse("20");
				//Analizador semantico
				//Vamos a comprobar si ya se ha ejecutado return en la funcion (en el caso de que estemos)
				if (functionActual != null && functionActual.yaDevuelto){
					Errores.escribirError("Analizador semantico", "Lo que haya tras la expresi�n return no se ejecutar�", AnManager.lineasST);
				}
				B();
				C();
				return;
				
			case "if"://2
				escribirParse("20");
				//Analizador semantico
				//Vamos a comprobar si ya se ha ejecutado return en la funcion (en el caso de que estemos)
				if (functionActual != null && functionActual.yaDevuelto){
					Errores.escribirError("Analizador semantico", "Lo que haya tras la expresi�n return no se ejecutar�", AnManager.lineasST);
				}
				B();
				C();
				return;
				
			case "ID"://3
				escribirParse("20");
				//Analizador semantico
				//Vamos a comprobar si ya se ha ejecutado return en la funcion (en el caso de que estemos)
				if (functionActual != null && functionActual.yaDevuelto){
					Errores.escribirError("Analizador semantico", "Lo que haya tras la expresi�n return no se ejecutar�", AnManager.lineasST);
				}
				B();
				C();
				return;
				
			case "return"://4
				escribirParse("20");
				//Analizador semantico
				//Vamos a comprobar si ya se ha ejecutado return en la funcion (en el caso de que estemos)
				if (functionActual != null && functionActual.yaDevuelto){
					Errores.escribirError("Analizador semantico", "Lo que haya tras la expresi�n return no se ejecutar�", AnManager.lineasST);
				}
				B();
				C();
				return;
				
			case "alert"://5
				escribirParse("20");
				//Analizador semantico
				//Vamos a comprobar si ya se ha ejecutado return en la funcion (en el caso de que estemos)
				if (functionActual != null && functionActual.yaDevuelto){
					Errores.escribirError("Analizador semantico", "Lo que haya tras la expresi�n el return no se ejecutar�", AnManager.lineasST);
				}
				B();
				C();
				return;
				
			case "input"://6
				escribirParse("20");
				//Analizador semantico
				//Vamos a comprobar si ya se ha ejecutado return en la funcion (en el caso de que estemos)
				if (functionActual != null && functionActual.yaDevuelto){
					Errores.escribirError("Analizador semantico", "Lo que haya tras la expresi�n el return no se ejecutar�", AnManager.lineasST);
				}
				B();
				C();
				return;
				
			case "while"://7
				escribirParse("20");
				//Analizador semantico
				//Vamos a comprobar si ya se ha ejecutado return en la funcion (en el caso de que estemos)
				if (functionActual != null && functionActual.yaDevuelto){
					Errores.escribirError("Analizador semantico", "Lo que haya tras la expresi�n return no se ejecutar�", AnManager.lineasST);
				}
				B();
				C();
				return;
				
			case "EOL":	//Tenemos en cuenta aqui el salto de l�nea, pero no lo meteremos en el �rbol
				sgtetoken = AnManager.pedirTokenAlex();
				C();
				return;
				
				//follow C
			case "cl"://8
				escribirParse("21");
				return;
				
			default:
				Errores.escribirError("Analizador sintactico", "No se permite la expresi�n <"+sgtetoken.tipoToken, AnManager.lineasST);
				//En el caso de que haya funcion anidada
				if (sgtetoken.tipoToken.equals("function")){
					Errores.panicModeLlaves(0);
				}
				
				break;
			}	
		}
	}
	
	/**
	 * Esperar a que se explique
	 */
	public static void F(){//function(para el �ltimo) tiene un procedure complicado,
		if (sgtetoken == null){
			Errores.escribirError("Analizador sintactico", "No hay mas tokens", AnManager.lineasST);
			return;
		}else{
			if (sgtetoken.tipoToken.equals("function")){

				sgtetoken = AnManager.pedirTokenAlex();
				escribirParse("22");
				H();
				if (sgtetoken.tipoToken.equals("ID")){
					//Analizador semantico
					if ((functionActual = buscaTS(TSG, sgtetoken.attrToken)) != null){
						Errores.escribirError("Analizador semantico", "La Funcion "+sgtetoken.attrToken+" ya se ha declarado anteriormente", AnManager.lineasST);
						//Seguiremos analizando normalmente pero no la a�adiremos a la tabla final
						functionActual = new itemTS();
						functionActual.tipo = "Null";
						functionActual.tipoDevuelto = H.tipo;
						functionActual.lexema = sgtetoken.attrToken;
						functionActual.argumentos = 0;
						TSL.clear();
						functionActual.yaDevuelto = false;
					}else{
						functionActual = new itemTS();
						functionActual.tipo = "Funcion";
						functionActual.tipoDevuelto = H.tipo;
						functionActual.lexema = sgtetoken.attrToken;
						tituloFunciones.add(sgtetoken.attrToken);
						functionActual.argumentos = 0;
						TSL.clear();
						functionActual.yaDevuelto = false;
					}


					sgtetoken = AnManager.pedirTokenAlex();
				}else{
					Errores.escribirError("Analizador sintactico", "Se esperaba un identificador en 'function'. Se ha recibido el expresion "+sgtetoken.tipoToken, AnManager.lineasST);
					//Tratamiento de error:
					if (AnManager.comprobarSiguienteToken().tipoToken.equals("ap")){ //Si el siguiente token es el que esperamos, continuamos normalmente
						sgtetoken = AnManager.pedirTokenAlex();
						sgtetoken = AnManager.pedirTokenAlex();
					}else{ //Si no es lo que esperamos, procedemos al PanicMode
						Errores.panicModeLlaves(0);;
						return;
					}
				}
				if (sgtetoken.tipoToken.equals("ap")){
					sgtetoken = AnManager.pedirTokenAlex();
				}else{
					Errores.escribirError("Analizador sintactico", "Se esperaba '('. Se ha recibido la expresion "+sgtetoken.tipoToken, AnManager.lineasST);
					//Tratamiento de error:
						Errores.panicModeLlaves(0);;
						return;
					
				}
				A();
				//Ya se han a�adido los argumentos a la correspondiente tabla e itemTS
				if (sgtetoken.tipoToken.equals("cp")){
					sgtetoken = AnManager.pedirTokenAlex();
				}else{
					Errores.escribirError("Analizador sintactico", "Se esperaba ')'. Se ha recibido la expresion "+sgtetoken.tipoToken, AnManager.lineasST);
					//Tratamiento de error:
					if (AnManager.comprobarSiguienteToken().tipoToken.equals("EOL") || AnManager.comprobarSiguienteToken().tipoToken.equals("al")){ //Si el siguiente token es el que esperamos, continuamos normalmente
						sgtetoken = AnManager.pedirTokenAlex();
						sgtetoken = AnManager.pedirTokenAlex();
					}else{ //Si no es lo que esperamos, procedemos al PanicMode
						Errores.panicModeLlaves(0);
						return;
					}
				}
				if (sgtetoken.tipoToken.equals("EOL")){ //Permitimos aqui un salto de l�nea
					sgtetoken = AnManager.pedirTokenAlex();
				}
				if (sgtetoken.tipoToken.equals("al")){
					sgtetoken = AnManager.pedirTokenAlex();
				}else{
					Errores.escribirError("Analizador sintactico", "Se esperaba '{'. Se ha recibido la expresi�n "+sgtetoken.tipoToken, AnManager.lineasST);
						Errores.panicModeLlaves(1);
						return;
				}
				C();
				if (sgtetoken.tipoToken.equals("EOL")){ //Permitimos aqui un salto de l�nea (En el caso de que haya habido error)
					sgtetoken = AnManager.pedirTokenAlex();
				}
				if(!functionActual.yaDevuelto && !functionActual.tipoDevuelto.equals("Void")){
					Errores.escribirError("Analizador semantico " , "No devuelve nada la Funcion "+functionActual.lexema, AnManager.lineasST);
				}
				//El valor devuelto ya estar�a comprobado
				//Insertamos todos los datos de la funcion en la tabla de s�mbolos
				//Si es error no la guardamos
				if (!functionActual.tipo.equals("Null")){
				ArrayList<itemTS> copy = new ArrayList<itemTS>(TSL);
				tablasSimbolos.add(idFunction,copy);
				int i = 0;
				while (i<tablasSimbolos.size()){
					i++;
				}
				functionActual.entID = idFunction;
				idFunction++;
				TSG.add(functionActual); //A�adimos a la tabla global
				}
				//Reiniciamos variables
				TSL.clear();
				functionActual = null;


				if (sgtetoken.tipoToken.equals("cl")){
					sgtetoken = AnManager.pedirTokenAlex();
				}else{
					Errores.escribirError("Analizador sintactico", "Se esperaba '}'. Se ha recibido la expresi�n "+sgtetoken.tipoToken, AnManager.lineasST);
					return;
				}	
			}else{
				Errores.escribirError("Analizador sintactico", "Se esperaba ';'. Se ha recibido la expresi�n "+sgtetoken.tipoToken, AnManager.lineasST);
				Errores.panicMode();
				return;
			}
		}
	}
	
	
	public static void H(){//4
		if (sgtetoken == null){
			Errores.escribirError("Analizador sintactico", "No hay mas palabras a analizar", AnManager.lineasST);
			return;
		}else{
			//First T
			switch(sgtetoken.tipoToken){
			case "number"://1
				escribirParse("23");
				T();

				//Analizador semantico
				H.tipo = "Entero";
				return;
				
			case "string"://2
				escribirParse("23");
				T();

				//Analizador semantico
				H.tipo = "string";
				return;
				
			case "boolean"://3
				escribirParse("23");
				T();

				//Analizador semantico
				H.tipo = "Bool";

				return;
				//Follow H
				
			case "ID"://4
				escribirParse("24");
				//Analizador semantico
				H.tipo = "Void";
				return;
				
			default:
				Errores.escribirError("Analizador sintactico", "Se debe poner un 'Tipo' o un 'Identificador' tras 'function'", AnManager.lineasST);
				break;
			}	
		}
	}
	
	
	public static void A(){//4
		if (sgtetoken == null){
			Errores.escribirError("Analizador sintactico", "No hay mas palabras a analizar", AnManager.lineasST);
			return;
		}else{
			switch(sgtetoken.tipoToken){
			//First T
			case "number"://1
				escribirParse("25");
				T();
				//Analizador semantico
				if (sgtetoken.tipoToken.equals("ID")){
					//Tambi�n tenemos que a�adir los par�metros a la TS
					itemTS a= new itemTS();
					a.lexema = sgtetoken.attrToken;
					a.tipo = T.tipo;
					a.desplazamiento = calculoDesplazamiento(T.tipo);
					functionActual.argumentos = functionActual.argumentos + 1;
					TSL.add(a);
					sgtetoken = AnManager.pedirTokenAlex();
				}else{
					Errores.escribirError("Analizador sintactico", "Se esperaba un identificador. Se ha recibido la expresi�n "+sgtetoken.tipoToken, AnManager.lineasST);
					return;
				}	
				K();
				return;
				
			case "string"://2
				escribirParse("25");
				T();

				//Analizador semantico
				if (sgtetoken.tipoToken.equals("ID")){
					//Tambi�n tenemos que a�adir los par�metros a la TS
					itemTS a= new itemTS();
					a.lexema = sgtetoken.attrToken;
					a.tipo = T.tipo;
					a.desplazamiento = calculoDesplazamiento(T.tipo);
					functionActual.argumentos = functionActual.argumentos + 1;
					TSL.add(a);

					sgtetoken = AnManager.pedirTokenAlex();
				}else{
					Errores.escribirError("Analizador sintactico", "Se esperaba un identificador. Se ha recibido la expresi�n "+sgtetoken.tipoToken, AnManager.lineasST);
				return;
				}	
				K();
				return;
				
			case "boolean"://3
				escribirParse("25");
				T();
				//Analizador semantico
				if (sgtetoken.tipoToken.equals("ID")){
					//Tambi�n tenemos que a�adir los par�metros a la TS
					itemTS a= new itemTS();
					a.lexema = sgtetoken.attrToken;
					a.tipo = T.tipo;
					a.desplazamiento = calculoDesplazamiento(T.tipo);
					functionActual.argumentos = functionActual.argumentos + 1;
					TSL.add(a);
					sgtetoken = AnManager.pedirTokenAlex();
				}else{
					Errores.escribirError("Analizador sintactico", "Se esperaba un identificador. Se ha recibido la expresi�n "+sgtetoken.tipoToken, AnManager.lineasST);
				return;
				}	
				K();
				return;
				//Follow A
				
			case "cp"://4
				escribirParse("26");
				return;
				
			default:
				Errores.escribirError("Analizador sintactico", "Falta el 'Tipo' del parametro que recibe la Funcion", AnManager.lineasST);
				if (sgtetoken.tipoToken.equals("ID")){ //Se ha olvidado el tipo.
					sgtetoken = AnManager.pedirTokenAlex();
				}	
				break;
			}	
		}
	}

	
	public static void K(){//2
		if (sgtetoken == null){
			Errores.escribirError("Analizador sintactico", "No hay mas palabras a analizar", AnManager.lineasST);
			return;
		}else{
			switch(sgtetoken.tipoToken){
			
			case "Coma"://1
				escribirParse("27");
				sgtetoken = AnManager.pedirTokenAlex();
				T();

				//Analizador semantico
				if (sgtetoken.tipoToken.equals("ID")){
					//a�adir los par�metros a la TS
					itemTS a= new itemTS();
					a.lexema = sgtetoken.attrToken;
					a.tipo = T.tipo;
					a.desplazamiento = calculoDesplazamiento(T.tipo);
					functionActual.argumentos = functionActual.argumentos + 1;
					TSL.add(a);
					sgtetoken = AnManager.pedirTokenAlex();
					K();
				}else{
					Errores.escribirError("Analizador sintactico", "Se esperaba un identificador. Se ha recibido la expresi�n ", AnManager.lineasST);
				return;
				}	
				return;
				//Follow K
				
			case "cp"://2
				escribirParse("28");
				return;
				
			default:
				break;
			}	
		}
	}
	
	
	public static void L(){//8
		if (sgtetoken == null){
			Errores.escribirError("Analizador sintactico", "No hay mas palabras a analizar", AnManager.lineasST);
			return;
		}else{
			switch(sgtetoken.tipoToken){
			//First E
			case "ID"://1
				escribirParse("29");
				R();
				LArgumentos.clear();
				LArgumentos.add(R.tipo);
				Q();
				if (RhayError){	
				//	//Errores.escribirError("Analizador semantico", "No es correcta la expresion", AnManager.lineasST);
					RhayError = false;
				}
				return;
				
			case "Entero"://2
				escribirParse("29");
				R();
				LArgumentos.clear();
				LArgumentos.add(R.tipo);
				Q();
				if (RhayError){	
				//	//Errores.escribirError("Analizador semantico", "No es correcta la expresion", AnManager.lineasST);
					RhayError = false;
				}
				return;
				
			case "Cadena"://3
				escribirParse("29");
				R();
				LArgumentos.clear();
				LArgumentos.add(R.tipo);
				Q();
				if (RhayError){	
				//	//Errores.escribirError("Analizador semantico", "No es correcta la expresion", AnManager.lineasST);
					RhayError = false;
				}
				return;
				
//			case "Preincremento":
//				escribirParse("21");
//				R();
//				LArgumentos.clear();
//				LArgumentos.add(R.tipo);
//				Q();
//				if (RhayError){	
//				//	//Errores.escribirError("Analizador semantico", "No es correcta la expresion", AnManager.lineasST);
//					RhayError = false;
//				}
//				return;
			case "true"://4
				escribirParse("29");
				R();
				LArgumentos.clear();
				LArgumentos.add(R.tipo);
				Q();
				if (RhayError){	
				//	//Errores.escribirError("Analizador semantico", "No es correcta la expresion", AnManager.lineasST);
					RhayError = false;
				}
				return;
				
			case "false"://5
				escribirParse("29");
				R();
				LArgumentos.clear();
				LArgumentos.add(R.tipo);
				Q();
				if (RhayError){	
				//	//Errores.escribirError("Analizador semantico", "No es correcta la expresion", AnManager.lineasST);
					RhayError = false;
				}
				return;
				
			case "ap"://6
				escribirParse("29");
				R();
				LArgumentos.clear();
				LArgumentos.add(R.tipo);
				Q();
				if (RhayError){	
				//	//Errores.escribirError("Analizador semantico", "No es correcta la expresion", AnManager.lineasST);
					RhayError = false;
				}
				return;
				
			case "negacion"://7
				escribirParse("29");
				R();
				LArgumentos.clear();
				LArgumentos.add(R.tipo);
				Q();
				if (RhayError){	
				//	//Errores.escribirError("Analizador semantico", "No es correcta la expresion", AnManager.lineasST);
					RhayError = false;
				}
				return;
				
			case "div":// esta no entra one moment //**peligro
				escribirParse("29");
				R();
				LArgumentos.clear();
				LArgumentos.add(R.tipo);
				Q();
				if (RhayError){	
					//Errores.escribirError("Analizador semantico", "No es correcta la expresion", AnManager.lineasST);
					RhayError = false;
				}
				return;
				//Follow L
				
			case "cp"://8
				escribirParse("30");
				return;
				
			default:
				break;
			}	
		}
	}

	
	public static void Q(){//2
		if (sgtetoken == null){
			Errores.escribirError("Analizador sintactico", "No hay mas palabras a analizar ", AnManager.lineasST);
			return;
		}else{
			switch(sgtetoken.tipoToken){
			case "Coma"://1
				escribirParse("31");
				sgtetoken = AnManager.pedirTokenAlex();
				R();
				LArgumentos.add(R.tipo);
				Q();
				return;
				//Follow Q
				
			case "cp":
				escribirParse("32");
				return;
				
			default:
				break;
			}	
		}
	}
	
	
	public static void R(){//7
		if (sgtetoken == null){
			Errores.escribirError("Analizador sintactico", "No hay mas palabras a analizar", AnManager.lineasST);
			return;
		}else{
			switch(sgtetoken.tipoToken){
			//First U
			case "ID"://1
				escribirParse("33");
				U();
				String tipo = U.tipo;
				R.tipo = tipo;
				Ra();
				if (RaRel){
					if (!(U.tipo.equals("Entero") && tipo.equals("Entero")) ){
						RhayError = true;
					}
					R.tipo = "Bool";
				}
				RaRel = false;
				return;
				
			case "Entero"://2
				escribirParse("33");
				U();
				String tipo1 = U.tipo;
				R.tipo = tipo1;
				Ra();
				if (RaRel){
					if (!(U.tipo.equals("Entero") && tipo1.equals("Entero")) ){
						RhayError = true;
					}
					R.tipo = "Bool";
				}
				RaRel = false;
				return;
				
			case "Cadena"://3
				escribirParse("33");
				U();
				String tipo2 = U.tipo;
				R.tipo = tipo2;
				Ra();
				if (RaRel){
					if (!(U.tipo.equals("Entero") && tipo2.equals("Entero")) ){
						RhayError = true;
					}	
					R.tipo = "Bool";
				}
				RaRel = false;
				return;
				
//			case "Preincremento":
//				escribirParse("33");
//				U();
//				String tipo3 = U.tipo;
//				R.tipo = tipo3;
//				Ra();
//				if (RaRel){
//					if (!(U.tipo.equals("Entero") && tipo3.equals("Entero")) ){
//						RhayError = true;
//					}
//					R.tipo = "Bool";
//				}
//				RaRel = false;
//
//				return;
				
			case "true"://4
				escribirParse("33");
				U();
				String tipo4 = U.tipo;
				R.tipo = tipo4;
				Ra();
				if (RaRel){
					if (!(U.tipo.equals("Entero") && tipo4.equals("Entero")) ){
						RhayError = true;
					}
					R.tipo = "Bool";
				}
				RaRel = false;
				return;
				
			case "false"://5
				escribirParse("33");
				U();
				String tipo5 = U.tipo;
				R.tipo = tipo5;
				Ra();
				if (RaRel){
					if (!(U.tipo.equals("Entero") && tipo5.equals("Entero")) ){
						RhayError = true;
					}
					R.tipo = "Bool";
				}
				RaRel = false;
				return;
				
			case "ap"://6
				escribirParse("33");
				U();
				String tipo6 = U.tipo;
				R.tipo = tipo6;
				Ra();
				if (RaRel){
					if (!(U.tipo.equals("Entero") && tipo6.equals("Entero")) ){
						RhayError = true;
					}
					R.tipo = "Bool";
				}
				RaRel = false;

				return;
				
			case ("negacion")://7
				escribirParse("33");
				U();
				String tipo7 = U.tipo;
				R.tipo = tipo7;
				Ra();
				if (RaRel){
					if (!(U.tipo.equals("Entero") && tipo7.equals("Entero")) ){
						RhayError = true;
					}
					R.tipo = "Bool";
				}
				RaRel = false;
				return;
				
			default:
				break;
			}	
		}
	}

	
	public static void Ra(){//15
		if (sgtetoken == null){
			Errores.escribirError("Analizador sintactico", "No hay mas palabras a analizar", AnManager.lineasST);
			return;
		}else{
			switch(sgtetoken.tipoToken){
			case "igual"://1
				escribirParse("34");
				sgtetoken = AnManager.pedirTokenAlex();	
				R();
				RaRel = true;
				if (!U.tipo.equals("Entero")){
					RhayError = true;
				}
				return;
				
				//Follow Ra
			case "PuntoComa"://2
				escribirParse("36");
				return;
				
			case "cp"://3
				escribirParse("36");
				return;
				
			case "Coma"://4
				escribirParse("36");
				return;
				
			case "distinto"://5
				escribirParse("35");
				sgtetoken = AnManager.pedirTokenAlex();	
				R();
				RaRel = true;
				if (!U.tipo.equals("Entero")){
					RhayError = true;
				}
				return;
				
			case "alert"://6
				escribirParse("36");
				return;
				
			case "EOF"://7
				escribirParse("36");
				return;
			
			case "function"://8
				escribirParse("36");
				return;
			
			case "ID"://9
				escribirParse("36");
				return;
				
			case "if"://10
				escribirParse("36");
				return;
				
			case "input"://11
				escribirParse("36");
				return;
				
			case "let"://12	
				escribirParse("36");
				return;
				
			case "return"://13
				escribirParse("36");
				return;
				
			case "while"://14
				escribirParse("36");
				return;
				
			case "cl"://15
				escribirParse("36");
				return;
				
			default:
				break;
			}	

		}
	}
	
	
	public static void U(){
		if (sgtetoken == null){
			Errores.escribirError("Analizador sintactico", "No hay mas palabras a analizar", AnManager.lineasST);
			return;
		}else{
			switch(sgtetoken.tipoToken){
			//First V
			case "ID"://1
				escribirParse("37");
				V();
				String tipo = V.tipo;
				U.tipo = tipo;
				Ua();
				if (UaSuma){
					if (!(Ua.tipo.equals("Entero") && tipo.equals("Entero")) ){
						RhayError = true;
					}
					U.tipo = "Entero";
				}
				UaSuma = false;
				return;
				
			case "Entero"://2
				escribirParse("37");
				V();
				String tipo1 = V.tipo;
				U.tipo = tipo1;
				Ua();
				if (UaSuma){
					if (!(Ua.tipo.equals("Entero") && tipo1.equals("Entero")) ){
						RhayError = true;
					}
					U.tipo = "Entero";
				}
				UaSuma = false;
				return;
				
			case "Cadena"://3
				escribirParse("37");
				V();
				String tipo2 = V.tipo;
				U.tipo = tipo2;
				Ua();
				if (UaSuma){
					if (!(Ua.tipo.equals("Entero") && tipo2.equals("Entero")) ){
						RhayError = true;
					}
					U.tipo = "Entero";
				}
				UaSuma = false;
				return;
				
//			case "Preincremento":
//				escribirParse("37");
//				V();
//				String tipo3 = V.tipo;
//				U.tipo = tipo3;
//				Ua();
//				if (UaSuma){
//					if (!(Ua.tipo.equals("Entero") && tipo3.equals("Entero")) ){
//						RhayError = true;
//					}
//					U.tipo = "Entero";
//				}
//				UaSuma = false;
//				return;
			case "true"://4
				escribirParse("37");
				V();
				String tipo4 = V.tipo;
				U.tipo = tipo4;
				Ua();
				if (UaSuma){
					if (!(Ua.tipo.equals("Entero") && tipo4.equals("Entero")) ){
						RhayError = true;
					}
					U.tipo = "Bool";
				}
				UaSuma = false;
				return;
				
			case "false"://5
				escribirParse("37");
				V();
				String tipo5 = V.tipo;
				U.tipo = tipo5;
				Ua();
				if (UaSuma){
					if (!(Ua.tipo.equals("Entero") && tipo5.equals("Entero")) ){
						RhayError = true;
					}
					U.tipo = "Bool";
				}
				UaSuma = false;
				return;
				
			case "ap"://6
				escribirParse("37");
				V();
				String tipo6 = V.tipo;
				U.tipo = tipo6;
				Ua();
				if (UaSuma){
					if (!(Ua.tipo.equals("Entero") && tipo6.equals("Entero")) ){
						RhayError = true;
					}
					U.tipo = "Entero";
				}
				UaSuma = false;				
				return;
				
			case "negacion"://7
				escribirParse("37");
				V();
				String tipo7 = V.tipo;
				U.tipo = tipo7;
				Ua();
				if (UaSuma){
					if (!(Ua.tipo.equals("Entero") && tipo7.equals("Entero")) ){
						RhayError = true;
					}
					U.tipo = "Entero";
				}
				UaSuma = false;				
				return;
				
			case "div"://error este no entra
				escribirParse("37");
				V();
				String tipo8 = V.tipo;
				U.tipo = tipo8;
				Ua();
				if (UaSuma){
					if (!(Ua.tipo.equals("Entero") && tipo8.equals("Entero")) ){
						RhayError = true;
					}
					U.tipo = "Entero";
				}
				UaSuma = false;				
				return;
				
			default:
				break;
			}	
		}
	}
	
	
	public static void Ua(){//16
		if (sgtetoken == null){
			Errores.escribirError("Analizador sintactico", "No hay mas palabras a analizar", AnManager.lineasST);
			return;
		}else{
			//System.out.print(sgtetoken.tipoToken);
			switch(sgtetoken.tipoToken){
			case "suma"://1
				escribirParse("38");
				sgtetoken = AnManager.pedirTokenAlex();
				U();
				UaSuma = true;
				//Analizador semantico
				if (!V.tipo.equals("Entero")){
					RhayError = true;
				}
				Ua.tipo = "Entero";
				return;
				//Follow Ua
				
			case "igual"://2
				escribirParse("39");
				return;
				
			case "PuntoComa"://3
				escribirParse("39");
				return;
				
			case "cp"://4
				escribirParse("39");
				return;
				
			case "Coma"://5
				escribirParse("39");
				return;
				
			case "distinto"://6
				escribirParse("39");
				return;
				
			case "alert"://7
				escribirParse("39");
				return;
				
			case"EOF": //8
				escribirParse("39");
				return;
				
			case"function"://9
				escribirParse("39");
				return;
				
			case"ID"://10
				escribirParse("39");
				return;
				
			case "if"://11
				escribirParse("39");
				return;
				
			case "input"://12
				escribirParse("39");
				return;
				
			case "let"://13
				escribirParse("39");
				return;
				
			case "return"://14
				escribirParse("39");
				return;
				
			case "while": //15
				escribirParse("39");
				return;
				
			case "cl"://16
				escribirParse("39");
				return;
				
			default:
				break;
			}	
		}
	}
	
	
	
	
	public static void V(){//7
		if (sgtetoken == null){
			Errores.escribirError("Analizador sintactico", "No hay mas palabras a analizar", AnManager.lineasST);
			return;
		}else{
			switch(sgtetoken.tipoToken){
			case "ID"://1
				escribirParse("40");
				String lexema = sgtetoken.attrToken;
				sgtetoken = AnManager.pedirTokenAlex();
				Va();
				//Analizador semantico
				itemTS a;
				if (llamadaFuncion){ //Se ha llamado a una Funcion
					int i=0;
					boolean aux = true;
					ArrayList<itemTS> copia;
					if ((a = buscaTS(TSG, lexema)) == null){ //No existe la Funcion
						//Comprobamos si se est� haciendo una llamada recursiva:
						if(functionActual != null && (a = functionActual).lexema.equals(lexema)){
							V.tipo = a.tipoDevuelto;
							copia = new ArrayList<itemTS>(TSL);
							if (VaArgumentos.size() == a.argumentos){
								while(i < VaArgumentos.size()){						
									if (!VaArgumentos.get(i).equals(copia.get(i).tipo)){
										aux = false;
									}
									i++;
								}
								if(!aux){
									Errores.escribirError("Analizador semantico", "No se ha llamado a la Funcion "+lexema+" con los argumentos correctos", AnManager.lineasST);	
								}
							}else{
								Errores.escribirError("Analizador semantico", "No se ha llamado a la Funcion "+lexema+" con los argumentos correctos", AnManager.lineasST);	
							}
							copia.clear();
						}else{
							Errores.escribirError("Analizador semantico", "La Funcion "+lexema+" no se ha declarado", AnManager.lineasST);
						}
					}else{ //Existe la Funcion
						V.tipo = a.tipoDevuelto;
						copia = new ArrayList<itemTS>(tablasSimbolos.get(a.entID));
						if (VaArgumentos.size() == a.argumentos){
							while(i < VaArgumentos.size()){						
								if (!VaArgumentos.get(i).equals(copia.get(i).tipo)){
									aux = false;
								}
								i++;
							}
							if(!aux){
								Errores.escribirError("Analizador semantico", "No se ha llamado a la Funcion "+lexema+" con los argumentos correctos", AnManager.lineasST);	
							}
						}else{
							Errores.escribirError("Analizador semantico", "No se ha llamado a la Funcion "+lexema+" con los argumentos correctos", AnManager.lineasST);	
						}
						copia.clear();
					}
					llamadaFuncion = false;
					
				}else{ //No se ha llamado a una Funcion, se ha llamado una variable.
					if (functionActual == null){ //Si no hay local, buscamos en la global
						if ((a = buscaTS(TSG, lexema)) == null){
							////////////////Errores.escribirError("Analizador semantico", "La variable "+lexema+" no se ha declarado anteriormente", AnManager.lineasST);/////////////////////////////////////////////////CUIDADO!!!!!!!!!!!!!!!!!!!!!!!!!
							a = new itemTS();
							a.lexema = lexema;
							a.desplazamiento = calculoDesplazamiento("Entero");
							a.tipo = "Entero";
							TSG.add(a);
						}else{
							V.tipo = a.tipo;
						}
					}else{//Si hay local, buscamos en la tabla local
						if ((a = buscaTS(TSL, lexema)) != null || (a = buscaTS(TSG, lexema)) != null){
							V.tipo = a.tipo;
						}else{
							////////////////////Errores.escribirError("Analizador semantico", "La variable "+lexema+" no se ha declarado anteriormente", AnManager.lineasST);////////////////////////////////////////////CUIDADO!!!!!!!!!!
							a = new itemTS();
							a.lexema = lexema;
							a.desplazamiento = calculoDesplazamiento("Entero");
							a.tipo = "Entero";
							TSG.add(a);
						}
					}
				}
				return;
				
			case "Entero"://2
				escribirParse("41");
				V.tipo = "Entero";
				sgtetoken = AnManager.pedirTokenAlex();
				return;
				
			case "Cadena"://3
				escribirParse("42");
				V.tipo = "string";
				sgtetoken = AnManager.pedirTokenAlex();
				return;
				
//			case "Preincremento":
//				escribirParse("43");
//				V.tipo = "Entero";
//				sgtetoken = AnManager.pedirTokenAlex();
//				if (sgtetoken.tipoToken.equals("ID")){
//					//Analizador semantico:
//					itemTS a1 = new itemTS();
//					if (functionActual == null){ //Si no hay local, buscamos en la global
//						if ((a1=buscaTS(TSG, sgtetoken.attrToken)) == null){
//
//						}else{
//							if (!a1.tipo.equals("Entero")){
//								Errores.escribirError("Analizador semantico", "La variable "+sgtetoken.attrToken+" debe ser de tipo 'Entero'", AnManager.lineasST);
//							}
//						}
//					}else{//Si hay local, buscamos en la tabla local
//						if ((a1=buscaTS(TSL, sgtetoken.attrToken)) != null || (a1=buscaTS(TSG, sgtetoken.attrToken)) != null){
//							if (!a1.tipo.equals("Entero")){
//								Errores.escribirError("Analizador semantico", "La variable "+sgtetoken.attrToken+" debe ser de tipo 'Entero'", AnManager.lineasST);
//							}
//						}else{
//							Errores.escribirError("Analizador semantico", "La variable "+sgtetoken.attrToken+" no se ha declarado anteriormente", AnManager.lineasST);
//						}
//					}
//
//					sgtetoken = AnManager.pedirTokenAlex();
//				}else{
//					Errores.escribirError("Analizador sintactico", "Se esperaba un identificador. Se ha recibido el token <"+sgtetoken.tipoToken+","+sgtetoken.attrToken+"> aqui", AnManager.lineasST);
//					return;
//				}	
//				return;
				
			case "true"://4
				escribirParse("43");
				V.tipo = "Bool";
				sgtetoken = AnManager.pedirTokenAlex();
				return;
				
			case "false"://5
				escribirParse("44");
				V.tipo = "Bool";
				sgtetoken = AnManager.pedirTokenAlex();
				return;
				
			case "ap"://6
				escribirParse("45");
				sgtetoken = AnManager.pedirTokenAlex();
				R();
				V.tipo = R.tipo;
				if (sgtetoken.tipoToken.equals("cp")){
					sgtetoken = AnManager.pedirTokenAlex();
				}else{
					Errores.escribirError("Analizador sintactico", "Falta el cierre de un parentesis", AnManager.lineasST);
				}	
				return;
				
			case "negacion"://7
				escribirParse("46");
				V.tipo = "Bool";
				sgtetoken = AnManager.pedirTokenAlex();
				Vb();
				return;
				
			default:
				break;
			}	
		}
	}
	


	public static void EP() {
		
		if (sgtetoken == null){
			Errores.escribirError("Analizador sintactico", "No hay mas palabras a analizar", AnManager.lineasST);
		return;
		}
		
		else {
			switch(sgtetoken.tipoToken){
			case "Divigual":
				escribirParse("47");
				sgtetoken = AnManager.pedirTokenAlex();
				T();

				sgtetoken = AnManager.pedirTokenAlex();
				
				EP();
				
				if (sgtetoken.tipoToken.equals("PuntoComa")){
					sgtetoken = AnManager.pedirTokenAlex();
				}
				else{
					Errores.escribirError("Analizador sintactico", "Se esperaba ';'. Se ha recibido la expresi�n "+sgtetoken.tipoToken, AnManager.lineasST);
					Errores.panicMode();
				}
				return;		
				
			}
		}
		
		
	}
	
	
	public static void Vb() {//3
		if (sgtetoken == null){
			Errores.escribirError("Analizador sintactico", "No hay mas palabras a analizar", AnManager.lineasST);
			return;
		}
		else{
			switch(sgtetoken.tipoToken){
			
			case "true"://1
				escribirParse("51");
				V.tipo = "Bool";
				sgtetoken = AnManager.pedirTokenAlex();
				return;
				
			case "false"://2
				escribirParse("52");
				V.tipo = "Bool";
				sgtetoken = AnManager.pedirTokenAlex();
				return;
				
				
			case "ap"://4
				escribirParse("54");
				sgtetoken = AnManager.pedirTokenAlex();
				R();
				Vb.tipo = R.tipo;
				if (sgtetoken.tipoToken.equals("cp")){
					sgtetoken = AnManager.pedirTokenAlex();
				}else{
					Errores.escribirError("Analizador sintactico", "Falta el cierre de un parentesis", AnManager.lineasST);
				}	
				return;
				
				
			case "ID"://3
				escribirParse("53");
				itemTS a1 = new itemTS();
				sgtetoken = AnManager.pedirTokenAlex();

				if (functionActual == null){ //Si no hay local, buscamos en la global
					if ((a1=buscaTS(TSG, sgtetoken.attrToken)) == null){

					}else{
						if (!a1.tipo.equals("Bool")){
							Errores.escribirError("Analizador semantico", "La variable "+sgtetoken.attrToken+" debe ser de tipo 'Bool'", AnManager.lineasST);
						}
					}
				}else{//Si hay local, buscamos en la tabla local
					if ((a1=buscaTS(TSL, sgtetoken.attrToken)) != null || (a1=buscaTS(TSG, sgtetoken.attrToken)) != null){
						if (!a1.tipo.equals("Bool")){
							Errores.escribirError("Analizador semantico", "La variable "+sgtetoken.attrToken+" debe ser de tipo 'Bool'", AnManager.lineasST);
						}
					}else{//no hago nada
						//Errores.escribirError("Analizador semantico", "La variable "+sgtetoken.attrToken+" no se ha declarado anteriormente", AnManager.lineasST);
					}
					
				}
				

			default:
				break;
			}
		}
		
	}
	
	
	public static void Va(){//17
		if (sgtetoken == null){
			Errores.escribirError("Analizador sintactico", "No hay mas palabras a analizar", AnManager.lineasST);
			return;
		}else{
			switch(sgtetoken.tipoToken){
			
			case "ap"://1
				escribirParse("49");
				sgtetoken = AnManager.pedirTokenAlex();
				L();
				//Analizador semantico
				VaArgumentos = LArgumentos;
				llamadaFuncion=true;
				if (sgtetoken.tipoToken.equals("cp")){
					sgtetoken = AnManager.pedirTokenAlex();
				}else{
					Errores.escribirError("Analizador sintactico", "Falta el cierre de un parentesis", AnManager.lineasST);
				}	
				return;
				//Follow Va
				
			case "igual"://2
				escribirParse("50");
				llamadaFuncion=false;
				return;
				
			case "suma"://3
				escribirParse("50");
				llamadaFuncion=false;
				return;
				
			case "PuntoComa"://4
				escribirParse("50");
				llamadaFuncion=false;
				return;
				
			case "cp"://5
				escribirParse("50");
				llamadaFuncion=false;
				return;
				
			case "Coma"://6
				escribirParse("50");
				llamadaFuncion=false;
				return;
				
			case "distinto"://7
				escribirParse("50");
				llamadaFuncion=false;
				return;
				
			case "alert"://8
				escribirParse("50");
				llamadaFuncion=false;
				return;
				
			case "EOF"://9
				escribirParse("50");
				llamadaFuncion=false;
				return;
				
			case "function"://10
				escribirParse("50");
				llamadaFuncion=false;
				return;
				
			case "ID"://11
				escribirParse("50");
				llamadaFuncion=false;
				return;	
				
			case "if"://12
				escribirParse("50");
				llamadaFuncion=false;
				return;	
				
			case "input"://13
				escribirParse("50");
				llamadaFuncion=false;
				return;	
				
			case "let"://14
				escribirParse("50");
				llamadaFuncion=false;
				return;
				
			case "return"://15
				escribirParse("50");
				llamadaFuncion=false;
				return;
				
			case "while"://16
				escribirParse("50");
				llamadaFuncion=false;
				return;
				
			case "cl"://17
				escribirParse("50");
				llamadaFuncion=false;
				return;
			default:
				break;
			}	
		}
	}
	
	
	
/*****************************************************Funciones de analizador sintactico(para el parse)****************************************************/
	/**
	 * @implNoteGenera Funcion que genera el archivo "parse"
	 */
	public static void genArchivoParse(){
		String path = AnManager.getPath() + File.separator +"Resultados Grupo64" + File.separator+ "Parse.txt";
		File f = new File(path);
		f.getParentFile().mkdirs(); 
		try {
			f.delete(); //Eliminamos si existe algo antes
			f.createNewFile();
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, "Fallo al crear archivo parse");
		}
	}

	
	
	/**
	 * @implNote Funcion para escribir en el archivo "Parse";
	 * @param dato
	 */
	public static void escribirParse(String dato){
		PrintWriter pw = null;
		try {
			File file = new File(AnManager.getPath() + File.separator +"Resultados Grupo64" + File.separator+ "Parse.txt");
			FileWriter fw = new FileWriter(file, true);
			pw = new PrintWriter(fw);
			pw.print(dato + " ");
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (pw != null) {
				pw.close();
			}
		}
	}


	
/**********************************Funciones de analizador semantico(para completar la tabla de simbolos definicitva :)))))*****************************/
	/**
	 * @implNote Funcion que comprueba si existe ya en la tabla de simbolos
	 * @param TS
	 * @param lex
	 * @return
	 */
	public static itemTS buscaTS(ArrayList <itemTS> TS, String lex){
		int i = 0;
		if (TS != null){
			while(i < TS.size()){
				if (TS.get(i).lexema.equals(lex)){
					return TS.get(i);
				}
				i++;
			}
		}
		return null;
	}

	

	/**
	 * @implNote Calcula el desplazamiento dependiendo del tipo (boolean, entero...)
	 * @param tipo
	 * @return
	 */
	private static int calculoDesplazamiento(String tipo){
		if (tipo.equals("Entero")){
			return desInt;
		}
		if (tipo.equals("Bool")){
			return desBool;
		}
		if (tipo.equals("string")){
			return desChars;
		}
		return -1;
	}
	
	
	
	/**
	 * @implNote Generamos el archivo que llamaremos "Tabla de Simbolos.txt"
	 */
	public static void genArchivoTS(){
		String path = AnManager.getPath() + File.separator +"Resultados Grupo64" + File.separator+ "Tabla de Simbolos.txt";
		File f = new File(path);
		f.getParentFile().mkdirs(); 
		try {
			f.delete(); //Eliminamos si existe algo antes
			f.createNewFile();
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, "Fallo al crear archivo Tabla de Simbolos");
		}
	}
	
	
	
	/**
	 * @implNote Funcion que imprime/escribe en "Tabal de Simbolos.txt"
	 */
	//Funcion que imprime todas las tablas de s�mbolos generadas
	public static void imprimirTablas(){
		PrintWriter pw = null;
		try {
			File file = new File(AnManager.getPath() + File.separator +"Resultados Grupo64" + File.separator+ "Tabla de Simbolos.txt");
			FileWriter fw = new FileWriter(file, true);
			pw = new PrintWriter(fw);
			int iter = 0;
			int i = 0;
			int contador=1;
			int desplazamiento = 0;
			pw.println("TABLA GLOBAL #1 :"); //Tabla global
			pw.println(""); //Espacio
			while(i<tablasSimbolos.get(0).size()){
				if (tablasSimbolos.get(0).get(i).tipo.equals("Funcion")){ //Si es una Funcion:
//					pw.println("  * LEXEMA: '"+tablasSimbolos.get(0).get(i).lexema+"' (Funcion)");
					pw.println("  * LEXEMA: '"+tablasSimbolos.get(0).get(i).lexema+ "'");
					pw.println("	+ Tipo:	'Funcion'");
//					pw.println("	+ TipoDevuelto: '"+tablasSimbolos.get(0).get(i).tipoDevuelto+"'");
					pw.println("	+ TipoRetorno: '"+tablasSimbolos.get(0).get(i).tipoDevuelto+"'");					
					pw.println("	+ NumParam: '"+tablasSimbolos.get(0).get(i).argumentos+"'");
					
					/**
					 * Para poner los tipos de par�metros de la funci�n
					 */
					int parametro= tablasSimbolos.get(0).get(i).argumentos;
					int m=1;

					int iter2;
//					while( iter<tituloFunciones.size() ) {

						iter2 = 0;
						while(m<= parametro && iter2 < tablasSimbolos.get(iter+1).size()) {
								pw.println("	+ TipoParam0"+ m + ":'"+tablasSimbolos.get(iter+1).get(iter2).tipo+"'");
								m++;
								iter2++;
						}
						iter++;
//					}
					/****************************************************************************************/
					
					pw.println("	+ EtiqFuncion: '"+ "Et"+tablasSimbolos.get(0).get(i).lexema+"0"+contador+"'");
					pw.println("");
					contador++;
				}else{
					pw.println("  * LEXEMA: '"+tablasSimbolos.get(0).get(i).lexema+"'");
					pw.println("	+ Tipo: '"+tablasSimbolos.get(0).get(i).tipo+"'");
					pw.println("	+ Despl: '"+desplazamiento+"'");
					desplazamiento = desplazamiento + tablasSimbolos.get(0).get(i).desplazamiento;
					pw.println("");
				}
				i++;
			}
			pw.println("-----------------------------------------------------------------------"); 
			//Ahora introducimos las demas tablas

			int j = 0;
			int x;
			int argumentos;
			while(j<tituloFunciones.size()){
				x = 0;
				pw.println("TABLA DE LA Funcion "+tituloFunciones.get(j)+" #"+(j+2)+" :"); //Tabla local
				pw.println(""); //Espacio
				desplazamiento = 0;
				argumentos = 0;
				while(x < tablasSimbolos.get(j+1).size()){
					if (argumentos != buscaTS(TSG, tituloFunciones.get(j)).argumentos){
						pw.println("  * LEXEMA: '"+tablasSimbolos.get(j+1).get(x).lexema+"' (parametro de funcion)");
						argumentos++;
					}else{
						pw.println("  * LEXEMA: '"+tablasSimbolos.get(j+1).get(x).lexema+"'");
					}
					pw.println("	+ Tipo: '"+tablasSimbolos.get(j+1).get(x).tipo+"'");
					pw.println("	+ Despl: '"+desplazamiento+"'");
					desplazamiento = desplazamiento + tablasSimbolos.get(j+1).get(x).desplazamiento;
					pw.println("");
					x++;
				}
				j++;
				pw.println("-----------------------------------------------------------------------"); 
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (pw != null) {
				pw.close();
			}
		}
	}
}
