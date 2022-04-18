package claro.edx.optimus.bean;

import java.util.List;

import claro.edx.optimus.bean.FilePaymentUpdateResponse.FilePaymentUpdate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FilePaymentUpdateRequest {

	private List<FilePaymentUpdate> files;
	private String path;
	private String domain;  // pichincha, pacifico, bolivariano, todos
	private String user;
	private int accion; //   del(100)   env(200)
	
	
	
}
