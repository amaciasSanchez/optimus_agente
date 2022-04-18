package claro.edx.optimus.bean;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StatusDirRequest {

	private Date fechaLectura;
	private List<String> directoryPaths;
	private String codigo;
	private List<FileOptimus> archivosInter;
	private List<FileOptimus> archivosBank;
	private List<FileOptimus> archivosCreditCards;
	private List<FilesByPath> archivosByPath;
	private String estado; //EXITO  ERROR
	
	public static FilesByPath newInstance(String dirpath, List<FileView> absNamesfiles ) {
		FilesByPath f = new FilesByPath();
		f.setDirectoryPath(dirpath);
		List<String> absNamesFilesList = new ArrayList<String>();
		if(absNamesfiles!=null) {
			for(FileView itm : absNamesfiles) {
				absNamesFilesList.add(itm.getNameFile());  //NOMBRE_FILE
			}
		}
		f.setAbsoluteNameFiles(absNamesFilesList);
		return f;
	}
	
	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class FilesByPath{
		private String directoryPath;
		private List<String> absoluteNameFiles;
	}

}