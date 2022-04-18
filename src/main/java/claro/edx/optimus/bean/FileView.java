package claro.edx.optimus.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FileView {
	 private String id;
     private String nameFile;
     private String dateModified;
     private String type;
     private String size;
     private String codigoFin;
     private String nombreFin;
     private String fileFormat;
     private String path;
     private String extension;
}
