package claro.edx.optimus.processs.util;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;

import lombok.extern.log4j.Log4j2;

/**
 * 
 * @author ronnb
 *
 */
@Log4j2
public class ChangeExtensionFiles {

	/*
	 * @param objFile
	 * @return
	 */
	public static boolean changeExtensionFiles(List<String> files, String path, String newExtension) {
		log.debug("changeExtensionFiles:: En Directorio: "+ path + "cambio archivos a extension "+ newExtension);
		for (String string : files) {
			int dotPos = string.lastIndexOf(".");

			String strFilename = string.substring(0, dotPos);

			try {
				FileUtils.moveFile(new File(path, string), new File(path, strFilename + "." + newExtension));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return true;
	}

//	public static void main(String[] args) {
//
//		List<String> prueba = new ArrayList<>();
//		prueba.add("10_20191204_20200214_1153141_000001.txt");
//		prueba.add("10_20191204_20200214_1153141_000002.txt");
//
//		String path = "C:\\test";
//		String newExtension = "env";
//
//		log.debug(ChangeExtensionFiles.changeExtensionFiles(prueba, path, newExtension));
//
//	}
}
