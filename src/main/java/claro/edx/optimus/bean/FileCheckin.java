package claro.edx.optimus.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DEscripcion de los archivos
 * @author ronnb
 *
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FileCheckin {

	public FileCheckin(FileView obj) {
		name = obj.getNameFile();
		size = obj.getSize();
		type = obj.getType();
		dateModified = obj.getDateModified();
		extension= obj.getExtension();
		codigoFin = obj.getCodigoFin();
		nombreFin = obj.getNombreFin();
		path  = obj.getPath();
		
	}
	private String name;	//Nombre del archivo    
	private String size;	// tamaño del archivo
	private String status;  // estado PEN , DELETED, ERROR, 
	private String obs;		// Observaciones importantes
	private String dateModified;
    private String type;
    private String codigoFin; // Codigo Financiera
    private String nombreFin; // Nombre Financiera
    private String fileFormat;
    private String path;
    private String extension;
    
    private String carpeta;
    private String subgrupo;
    private String dirBase;
    

}