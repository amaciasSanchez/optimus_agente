package claro.edx.optimus.bean;

import java.io.Serializable;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReverseResponse implements Serializable{
	 private static final long serialVersionUID = 78080108551754406L;
	 private String respuesta;
	 private String descripcion;
	 private List<String> NombreArchivos;
	 
	 
	 
}
