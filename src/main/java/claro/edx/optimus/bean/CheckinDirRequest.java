package claro.edx.optimus.bean;

import java.util.Date;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CheckinDirRequest {
	private Date fechaLectura;
	private String directoryPath;
	private String codigo;
	private List<FileCheckin> archivos;
	private String estado; // EXITO ERROR

}