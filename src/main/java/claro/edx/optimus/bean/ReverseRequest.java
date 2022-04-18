package claro.edx.optimus.bean;

import java.io.Serializable;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReverseRequest implements Serializable {

	private static final long serialVersionUID = 78080108331754406L;
	private String identificador;
	@ApiModelProperty(value = "Dominio de Archivos. ",example = "pichincha, pacifico, bolivariano, todos")
	private String domain;
	private String fecha;
}
