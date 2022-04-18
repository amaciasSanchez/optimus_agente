package claro.edx.optimus.commons;

import java.io.IOException;
import java.net.InetAddress;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import org.apache.logging.log4j.CloseableThreadContext;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.apache.logging.log4j.util.Strings;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class UsefulFunctions {

	private UsefulFunctions() {}

	public static void debugMessage(String mensaje) {
		if (log.isDebugEnabled())
			log.debug(mensaje);
	}

	/**
	 * Genera Id de transacción por medio de la librería {@link UUID}
	 * @return
	 */
	public static String generateTransactionId() {
		return UUID.randomUUID().toString();
	}
	
	/**
	 * Realiza la obtención de la ip local de donde se está ejecutando la aplicación, 
	 * primero consulta la variable de entorno IP_HOST que puede ser agregada desde el docker compose. 
	 * Si no existe la variable de entorno la consulta de forma directa en el siguiente 
	 * metodo {@link InetAddress.getLocalHost().getHostAddress()}. Si no existe devuelve la IP 0.0.0.0
	 * @return
	 */
	public static String getLocalAddr() {
		String ipHost = "0.0.0.0";
		try {
			ipHost = System.getenv("IP_HOST");
		}catch(Exception e) {
			log.error("No se pudo obtener la variable de entorno: {}"+e.getMessage());
		} 
		if(ipHost==null) {
			try {
				return InetAddress.getLocalHost().getHostAddress();
			}catch(Exception e) {
				log.error("No local address: {}"+ e.getMessage());
				return ipHost;
			}
		}
		return ipHost;
	}

	/**
	 * Obtiene los datos datos básicos de una excepción, tales como número de línea y 
	 * @author robert.macias@gizlocorp.com
	 *
	 */
//	@Data
//	@AllArgsConstructor
//	@NoArgsConstructor
//	public static class ExceptionData {
//		public ExceptionData(Exception e) {
//			String packageNamea = this.getClass().getPackage().getName().replace(".common.util", "");
//			List<StackTraceElement> lstStackTrace = Arrays.asList(e.getStackTrace());
//			List<StackTraceElement> lstFiltered = lstStackTrace.stream()
//					.filter(s -> s.getClassName().contains(packageNamea)).collect(Collectors.toList());
//			this.className = lstFiltered.get(0).getClassName();
//			this.methodName = lstFiltered.get(0).getMethodName();
//			this.lineNumber = lstFiltered.get(0).getLineNumber();
//			this.msgLog = String.format("%1$s - %2$s() - Linea %3$s", className, methodName, lineNumber);
//		}
//		String className;
//		String methodName;
//		String msgLog;
//		Integer lineNumber;
//	}

	/**
	 * Genera valores de cabecera con los valores básicos de peticiones generales. 
	 * Los campos son operationId, transactionId y media-type {@link MediaType.APPLICATION_XML}
	 * @return
	 */
	public static HttpHeaders generateHttpHeadersXml() {
		HttpHeaders headers = getAuditValuesForHeader();
		headers.setContentType(MediaType.APPLICATION_XML);
		return headers;
	}
	
	/**
	 * Genera valores de cabecera con los valores básicos de peticiones generales. 
	 * Los campos son operationId, transactionId y media-type {@link MediaType.APPLICATION_JSON_UTF8}
	 * @return
	 */
	@SuppressWarnings("deprecation")
	public static HttpHeaders generateHttpHeadersJson() {
		HttpHeaders headers = getAuditValuesForHeader();
		headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
		return headers;
	}
	
	/**
	 * Genera valores genericos de cabecera, con valores de auditoria requeridos por claro
	 * @return
	 */
	public static HttpHeaders getAuditValuesForHeader() {
		HttpHeaders headers = new HttpHeaders();
		headers.set("operationId", ThreadContext.get("operationId"));
		headers.set("transactionId", ThreadContext.get("transactionId"));
		return headers;
	}
	
	/**
	 * Agrega al contexto el valor transactionTime generar log
	 * @param startTransactionTime
	 */
	public static void addTransactionTime(long startTransactionTime) {
		CloseableThreadContext.put("transactionTime", (System.currentTimeMillis()-startTransactionTime)+"Ms");
	}
	
	/**
	 * Remueve del contexto el valor transactionTime generar log
	 * @param startTransactionTime
	 */
	public static void removeTransactionTime() {
		CloseableThreadContext.put("transactionTime", null);
	}

	/**
	 * Realiza la obtención de los parámetros tipo Json
	 * @param stringJson
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static Map<String, Object> getJsonAttributes(String stringJson) {
		Map<String, Object> map = null;
        try {
        	ObjectMapper mapper = new ObjectMapper();
			map = mapper.readValue(stringJson, Map.class);
            return map;
        } catch (IOException e) {
           log.error("El Json enviado no es valido {}", stringJson);
           return null;
        }
	}
	
	/**
	 * Procesa el request en caso de que reciba errores de oracle, 
	 * esto es exclusivo del consumo de los apis de OFSC que tiene la siguiente estructura json.<br/>
	 * {
    <br/>"type": "URL",
    <br/>"title": "TITULO GENERAL",
    <br/>"status": "CODIGO QUE DEVUELVE ORACLE",
    <br/>"detail": "MENSAJE QUE DA LA CAUSA DEL ERROR" <br/>}
	 * @param bodyJson
	 * @return Mensaje procesado en caso que exista en el diccionario
	 */
	public static String processRequestOracleError(String bodyJson) {
		Map<String, Object> respuesta = getJsonAttributes(bodyJson);
		String errorEspecifico = "";
		if(respuesta!=null && respuesta.get("detail")!=null) {
			errorEspecifico = procesarCadenaErrorOracle(respuesta.get("detail").toString());
		} else if(respuesta!=null && respuesta.get("description")!=null) { 
			//Esto se invocará en caso de que no sea error de oracle
			errorEspecifico = respuesta.get("description").toString();
		}
		if(respuesta!=null && respuesta.containsKey("message")&& respuesta.get("message")!=null) { 
			//Esto se invocará en caso de que no sea error de oracle
			errorEspecifico = respuesta.get("message").toString();
		}
		return errorEspecifico;
	}
	
	/**
	 * Realoza traduccion del error de Oracle para el usuario, 
	 * si no está contemplado devuelve el mensaje de oracle en inglés tal y como es recibido del servicio
	 * @param mensaje
	 * @return
	 */
	public static String procesarCadenaErrorOracle(String mensaje){
		if(mensaje.contains("Activity with this status cannot execute this action")) {
			return "No se puede realizar la accion debido al estado de la actividad.";
		} else if(mensaje.contains("Activity not found")) {
			return "No existe la actividad consultada.";
		} else if(mensaje.contains("Activity cannot be rescheduled to or from the past date")) {
			return "Las actividades no pueden ser reagendadas desde o hacia fechas pasadas.";
		} else if(mensaje.contains("Can't delete activity with status:")) {
			return "No se pueden eliminar actividades con el estado"+mensaje.substring(mensaje.indexOf(':'));
		} else if(mensaje.contains("Resource not found:")) {
			return "El recurso indicado no existe"+mensaje.substring(mensaje.indexOf(':'));
		} else if(mensaje.contains("Moving between resources is not allowed for this activity type.")) {
			return "Mover entre recursos no está permitido para el tipo de la actividad, revise el recurso propietario";
		} else if(mensaje.contains("Invalid property value. Property: resourceId. Value:")) {
			return "El id de recurso enviado no es valido"+mensaje.substring(mensaje.lastIndexOf(':'));
		}
		return mensaje;
	}
	
	/**
	 * Agrega al contexto de la ejecución variables con los valores enviados por parámetros. 
	 * Los parámetros son transactionId, externalTransactionId, operationId y remoteAddress. 
	 * Se utiliza el método {@link CloseableThreadContext.put("llave", valor)}
	 * @param transactionId
	 * @param externalTransactionId
	 * @param operationId
	 * @param remoteAddress
	 */
	public static void addDataContext(
			String transactionId, String externalTransactionId,String operationId, String remoteAddress) {
		addDataContext("transactionId",transactionId);
		addDataContext("externalTransactionId",externalTransactionId);
		addDataContext("operationId",operationId);
		addDataContext("ipClient",remoteAddress);
		addDataContext("ipServer", getLocalAddr());
		addDataContext("transactionDate", new Date().getTime()+"");
	}
	
	/**
	 * Agrega al contexto de la ejecución variables con la llave y el valor enviados 
	 * Se utiliza el método {@link CloseableThreadContext.put("llave", valor)}
	 */
	public static void addDataContext(String llave, String valor) {
		CloseableThreadContext.put(llave, valor);
	}
	
	/**
	 * Retorna la fecha actual en formato yyyy-MM-dd HH:mm:ss
	 * @return
	 */
	public static String getStringDateFullFormat() {
		return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
	}
	
	public static String appendTimeToDate(String dateString, long quantityTime) {
		try {
			Date actualDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(dateString);
			actualDate.setTime(quantityTime*1000);
			String appendedDate =  new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(actualDate);
			return appendedDate;
		} catch (ParseException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static String getValueFromThreadContext(String key) {
		return ThreadContext.get(key);
	}

	public static void generateBasicDataActivity(String identificationId, String name, String activityId, String activityType) {
		if(!Strings.isBlank(identificationId))
			addDataContext("identificationId",identificationId);
		if(!Strings.isBlank(name))
			addDataContext("name",name);
		if(!Strings.isBlank(activityId))
			addDataContext("activityId",activityId);
		if(!Strings.isBlank(activityType))
			addDataContext("activityType",activityType);
	}
	public static void generateBasicDataSubscription(String subscriptionId, String serialNumber, String activityId, String somTransactionId) {
		if(!Strings.isBlank(subscriptionId))
			addDataContext("subscriptionId",subscriptionId);
		if(!Strings.isBlank(serialNumber))
			addDataContext("serialNumber",serialNumber);
		if(!Strings.isBlank(activityId))
			addDataContext("activityId",activityId);
		if(!Strings.isBlank(somTransactionId))
			addDataContext("somTransactionId",somTransactionId);
	}
	
	public static void generateLogError(Logger log, MessageSource messageSource, boolean isMessageProperty, String messageCode, Object... params) {
		String message = null;
		String correctStep = null;
		if(isMessageProperty)
			message = messageSource.getMessage(messageCode, params, Locale.getDefault());
		else 
			message = messageCode;
		String step = getDataFromContext("step");
		if(Strings.isNotBlank(step))  {
			correctStep = step;
			addDataContext("step","");	
		}
		addDataContext("correctStep",correctStep);	
		if(isMessageProperty)
			log.error(message);
		else 
			log.error(message, params);
	}
	
	public static void generateLogInfo(Logger log, MessageSource messageSource, boolean isMessageProperty, String messageCode, Object... params) {
		String message = null;
		if(isMessageProperty)
			message = messageSource.getMessage(messageCode, params, Locale.getDefault());
		else 
			message = messageCode;
		String correctStep = getDataFromContext("correctStep");
		if(Strings.isNotBlank(correctStep)) {
			addDataContext("step","");	
		} else {
			String step = getDataFromContext("step");
			if(Strings.isNotBlank(step)) {
				step = (Long.parseLong(step)+1)+"";
			} else {
				step = "1";
			}
			addDataContext("step",step);	
		}
		if(isMessageProperty)
			log.info(message);
		else 
			log.info(message, params);
	}
	
	public static String getDataFromContext(String key) {
		return ThreadContext.get(key);
	}

	/**
	 * Elimina todos los atributos
	 * @param startTransactionTime
	 */
	public static void clearLog() {
		ThreadContext.clearAll();
	}
	
}
