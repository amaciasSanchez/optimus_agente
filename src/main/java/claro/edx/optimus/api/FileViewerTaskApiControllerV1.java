package claro.edx.optimus.api;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import org.apache.logging.log4j.CloseableThreadContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

import claro.edx.optimus.bean.FilePaymentUpdateRequest;
import claro.edx.optimus.bean.FilePaymentUpdateResponse;
import claro.edx.optimus.bean.ReverseRequest;
import claro.edx.optimus.bean.ReverseResponse;
import claro.edx.optimus.commons.UsefulFunctions;
import claro.edx.optimus.service.TaskService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.log4j.Log4j2;

@Log4j2
@RestController
@CrossOrigin("*")
@Validated
@RequestMapping(value = "/api/v1")
@Api(value = "File Task - Data Payment Files",
description = "API para manipulacion de archivos de pago. Cambio de Extensiones, Eliminacion de Archivos de Pago."
)
public class FileViewerTaskApiControllerV1 {

	
	@Autowired
	TaskService taskService;
	
	
	@Value("${dataPayment.enableFileManagement}")
	boolean enableFileManagement;
	
	/**
	 * BORRAR 100     ENVIAR 200
	 * 
	 *@param req
	 *@param request
	 *@return
	 *@throws HttpServerErrorException
	 *@throws HttpClientErrorException
	 *@throws IOException
	 *@throws Exception
	 */
	@ApiOperation(value = "Actualizacion Eliminacion de Archivos de Pago ", nickname = "filePaymentUpdate",
			notes = "Enviar codigo de Accion 100Bo 200Env", responseContainer = "Object")
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "successful task",  responseContainer = "Object") })
	@RequestMapping(value = "/filePaymentUpdate", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody FilePaymentUpdateResponse filePaymentUpdate(
				@RequestBody FilePaymentUpdateRequest req,
				HttpServletRequest request) throws  HttpServerErrorException, HttpClientErrorException, IOException, Exception  {
		
		
		
		log.debug("\n\n\n TASK filePaymentUpdate: INVOKE ("+req.getAccion()+") *****************************************************");
		log.debug("Configuracion Definida dataPayment.enableFileManagement="+enableFileManagement);
		FilePaymentUpdateResponse response = null;
		long startTransactionTime = System.currentTimeMillis();
		String transactionId = UsefulFunctions.generateTransactionId();
		UsefulFunctions.addDataContext(transactionId, "extID", "operID" ,	 request.getRemoteAddr());
	
		try(final CloseableThreadContext.Instance ctc = CloseableThreadContext.put("id",transactionId)) {
			
			if(enableFileManagement) {
				UsefulFunctions.generateLogInfo(log, null, false, "Se inicia proceso de tareas sobre archivo modificar_extension o Eliminar" , 
						request.getQueryString());
				
				log.debug(" Se reciben ("+ (req.getFiles()!=null?req.getFiles().size():0) +") archivos en Array String");
				
				response = taskService.filePaymentUpdate(req);
			}else {
				response = new FilePaymentUpdateResponse();
				response.setCodigo(""+req.getAccion());
				response.setPath(req.getPath());
				response.setDomain(req.getDomain());
				response.setUser(req.getUser());
				response.setAccion(req.getAccion());
				if(req.getFiles()!=null) {
					response.setFiles(req.getFiles());
				}
				response.setEstado("Bloqueado");
				response.setObs("Propiedad dataPayment.enableFileManagement debe habilitarse a true y reiniciar el componente Task Scheduler");
			}
			
			
			
		}catch(Exception e) {
			log.error("ERROR",e);
			response.setCodigo("ERROR");
			response.setObs(e.getMessage());
			response.setEstado("ERROR");
			
		}finally {
			UsefulFunctions.addTransactionTime(startTransactionTime);
			UsefulFunctions.generateLogInfo(log, null, false, "Finalizado proceso de Actualizacion de archivos de pago");
			UsefulFunctions.clearLog();
		}
		
//		String correlationId = UUID.randomUUID().toString();
//		String attentionDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date());
//		StopWatch watch = new StopWatch();
//		watch.start();
		return response;
	}
	

	
	@RequestMapping(value = "/executeReverse", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody ReverseResponse executeReverse(
				@RequestBody ReverseRequest req) throws  HttpServerErrorException, HttpClientErrorException, IOException, Exception  {
		
		log.info("\n\n\n REVERSO INVOKE *****************************************************");
		log.info("request: "+ req);
		ReverseResponse response = null;
		
		response = taskService.executeReverse(req);
		
		return response;
	}
	
	
	
	
	
}
