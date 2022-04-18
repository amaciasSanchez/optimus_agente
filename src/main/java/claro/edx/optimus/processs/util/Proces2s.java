package claro.edx.optimus.processs.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class Proces2s {
	@SuppressWarnings("unused")
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		String commando = "awk 'BEGIN { FS=OFS=SUBSEP=\" \"}{arr[$1]+=$2}END {for (i in arr) print i,arr[i]}' C://test//loadTest.txt";
	
		
		
		String[] command = { "bash", "-c", commando };
		
		
		try {
			
			String[] commandow = new String[] { "cmd", "/c", ("type pwd" ) };
					// System.out.println("Linea a ejecutarse: ", command::toString);
			Process process = Runtime.getRuntime().exec("cmd /c whoami");
			System.out.println("Linea de comando ejecutada!");
			System.out.println("Leyendo salida...");
			InputStreamReader entrada = new InputStreamReader(process.getInputStream());
			BufferedReader stdInput = new BufferedReader(entrada);
			String out = null; 
			while ((out = stdInput.readLine()) != null) {
				System.out.println(out);
			}

		} catch (Exception e) {

		}
	}

}
