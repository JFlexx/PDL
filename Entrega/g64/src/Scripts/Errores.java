package Scripts;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import javax.swing.JOptionPane;

/**
 * @version 2.0
 * @author Admin
 * @see Se implementaran funciones, que nos ayuden con el control de Errores
 */
public class Errores {
	/**
	 * @implNote Fallo cuando se crea el archivo, porque no existe el path o el propio archivo
	 */
	public static void genArchivoErrores(){
		String path = AnManager.getPath() + File.separator +"Resultados Grupo64" + File.separator+ "Errores.txt";
		File f = new File(path);
		f.getParentFile().mkdirs(); 
		try {
			f.delete(); //Eliminamos si existe algo antes
			f.createNewFile();
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, "Fallo al crear archivo errores");
		}
	}

	
	
	/**
	 * @implNote En caso de Error: escribe el error y además nos indica en que analizador se encuentra el error
	 * @param analizador
	 * @param def
	 * @param line
	 */
	public static void escribirError(String analizador, String def, int line){
		AnManager.sinErrores = false;
		PrintWriter pw = null;
		try {
			File file = new File(AnManager.getPath() + File.separator +"Resultados Grupo64" + File.separator+ "Errores.txt");
			FileWriter fw = new FileWriter(file, true);
			pw = new PrintWriter(fw);
			//pw.println("->Error en "+analizador+": " + def);
			pw.println("->Error(Linea " + line+ ")"+analizador+": " + def);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (pw != null) {
				pw.close();
			}
		}
	}

	
	
	/**
	 * @implNote Cuando no existe ningun en ningun analizador(lexico, sintactico ni semantico)
	 */
	public static void escribirOK(){
		PrintWriter pw = null;
		try {
			File file = new File(AnManager.getPath() + File.separator +"Resultados Grupo64" + File.separator+ "Errores.txt");
			FileWriter fw = new FileWriter(file, true);
			pw = new PrintWriter(fw);
			pw.println("*No se han detectado errores en el codigo*");
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (pw != null) {
				pw.close();
			}
		}
	}

	
	
	/**
	 * @implNote Realiza la recuperacion de Errores, en caso de que falten llaves por cerrar
	 * @param llavesabiertas
	 */
	//Esta funciónn se encarga de realizar la recuperacion de errores "Panicmode", buscamos una salida segura, como un ; o un EOL para recuperarse del error y seguir analizando
	public static void panicModeLlaves(int llavesabiertas){ //Saltamos todo hasta encontrar el final de }
		int aux= llavesabiertas; //Contamos posibles llaves
		while(true){
			Token token = AnManager.pedirTokenAlex();
			if (token.tipoToken.equals("{")){
				aux++;
			}
			if (token.tipoToken.equals("}")){
				aux--;
				if (aux <= 0){ //No hay llaves que cerrar pendientes
					AnalizadorSinSem.sgtetoken =  AnManager.pedirTokenAlex();
				}
				return;
			}
			if (token.tipoToken.equals("EOF")){
				AnalizadorSinSem.sgtetoken = token;
				return;
			}
			
			if (token.tipoToken.equals("function")){ //estamos en function
				AnalizadorSinSem.sgtetoken = token;
				return;
			}
		}
	}
	
	
	
	/**
	 * @implNote Realiza la funcion de panicmode
	 */
	public static void panicMode(){ // Buscamos 'EOL', ';' o 'EOF'
		if (AnalizadorSinSem.sgtetoken.tipoToken.equals(";") || AnalizadorSinSem.sgtetoken.tipoToken.equals("EOL") || AnalizadorSinSem.sgtetoken.tipoToken.equals("EOF")){
			return;
		}
		while(true){
			Token token = AnManager.pedirTokenAlex();
			if (token.tipoToken.equals(";")){
				AnalizadorSinSem.sgtetoken =  AnManager.pedirTokenAlex();
				return;
			}
			if (token.tipoToken.equals("EOL")){
				AnalizadorSinSem.sgtetoken = token;
				return;
			}
			if (token.tipoToken.equals("EOF")){
				AnalizadorSinSem.sgtetoken = token;
				return;
			}
		}
	}
}
