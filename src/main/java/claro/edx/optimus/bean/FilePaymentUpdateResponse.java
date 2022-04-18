package claro.edx.optimus.bean;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FilePaymentUpdateResponse {
	private String estado;
	private String obs;
	private String codigo;
	
	private String path;
	private String domain;  // pichincha, pacifico, bolivariano, todos
	private String user;
	private int accion; //   del(100)   env(200)
	
	private List<FilePaymentUpdate> files;

	public static FilePaymentUpdate newInstance(String absFileName_, String est, String obs) {
		FilePaymentUpdate obj = new FilePaymentUpdate();
		obj.setAbsFileName(absFileName_);
		obj.setEstado(est);
		obj.setObservacion(obs);
		return obj;
	}
	
	@Data
	@AllArgsConstructor
	@NoArgsConstructor
	public static class FilePaymentUpdate{
		private String absFileName;
		private String estado;
		private String observacion;
		private String type="";  //Checkin
		private String dirBase;
		private String relaPath;
		private String onlyName;
		
	}
}
