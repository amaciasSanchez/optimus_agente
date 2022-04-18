package claro.edx.optimus.service;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.google.common.base.Strings;

import claro.edx.optimus.bean.FilePaymentUpdateRequest;
import claro.edx.optimus.bean.FilePaymentUpdateResponse;
import claro.edx.optimus.bean.FilePaymentUpdateResponse.FilePaymentUpdate;
import claro.edx.optimus.bean.ReverseRequest;
import claro.edx.optimus.bean.ReverseResponse;
import claro.edx.optimus.constants.FileviewerConstants;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Service
public class TaskService {

	@Value("${files.send}")
	String extensionSEND;

	@Value("${ruta.lectura.1}")
	String rutaRecibidosBank;
	
	@Value("${ruta.lectura.2}")
	String rutaRecibidosInterbank;
	
	@Value("${ruta.lectura.3}")
	String rutaRecibidosTarjeta;

	@Value("${ruta.checkin.exito}")
	String rutaExitoPosCheckin;

	@Value("${ruta.lectura.checkin}")
	String rutaLecturaCheckin;
	

	@Value("${ruta.lectura.4}")
	String rutaBackup;
	
	@Value("${ruta.lectura.5}")
	String rutaBackupJson;

	/**
	 * 
	 * 
	 * @param req
	 * @return
	 */
	public FilePaymentUpdateResponse filePaymentUpdate(FilePaymentUpdateRequest req) {
		log.debug("Funcion de Servicio: filePaymentUpdate(...)");
		FilePaymentUpdateResponse res = new FilePaymentUpdateResponse();

		List<FilePaymentUpdate> lista = req.getFiles();
		log.debug("\n\n Lista de Archivo que vienen en la peticion " + req.getFiles());
		log.debug("\n\n Accion del(100)  env(200) " + req.getAccion());
		//
		for (FilePaymentUpdate fileOptimusItem : lista) {
			//
			String nameFile = fileOptimusItem.getAbsFileName();
			boolean nombreArchivoInvalido = nameFile == null || nameFile.isEmpty() || nameFile.trim().isEmpty();
			log.debug("nombreArchivoInvalido(" + nameFile + "): " + nombreArchivoInvalido);
			if (nombreArchivoInvalido) {
				fileOptimusItem.setEstado("ERROR");
				fileOptimusItem.setObservacion("Nombre de Archivo Nulo o Vacio, no se ejecuto tarea");
				continue;
			} else {

				// IDENTIFICAMOS ACCION A REALIZAR
				// 100 BORRAR
				boolean accionBorrar = req.getAccion() == FileviewerConstants.ACCION_BORRAR;
				boolean accionEnviar = req.getAccion() == FileviewerConstants.ACCION_ENVIAR;
				boolean accionBorrarCheckin = req.getAccion() == FileviewerConstants.ACCION_BORRAR_CHECKIN;
				boolean accionEnviarCheckin = req.getAccion() == FileviewerConstants.ACCION_ENVIAR_CHECKIN;

				if (accionBorrar) {
					log.debug("Borrando un archivos ");
					File fileInAction = new File(req.getPath(), nameFile);
					log.debug("archivo a borrar:  " + fileInAction);
					//
					if(!fileInAction.exists() || fileInAction.isDirectory()) { 
						fileOptimusItem.setEstado("ERROR");
						fileOptimusItem.setObservacion("El archivo no existe. directorio equivocado." + fileInAction.toString());
						continue;
					}
					//
					boolean actionbool = FileUtils.deleteQuietly(fileInAction);
					if (actionbool) {
						log.debug("El archivo fue borrado con EXITO");
						fileOptimusItem.setEstado("DELETED");
						fileOptimusItem.setObservacion("El archivo fue borrado con EXITO");
						continue;
					} else {
						actionbool = FileUtils.deleteQuietly(fileInAction);
						if (actionbool) {
							log.debug("El archivo fue borrado con EXITO");
							fileOptimusItem.setEstado("DELETED");
							fileOptimusItem.setObservacion("El archivo fue borrado con EXITO");
							continue;
						} else {
							File fileDest = null;
							try {
								fileDest = new File(fileInAction.getAbsolutePath() + ".DELETED");
								FileUtils.moveFile(fileInAction, fileDest);
								log.debug("Exito Archivo renombrado (inutilizado) exitosamente "
										+ fileDest.getAbsolutePath());
								fileOptimusItem.setEstado("DELETE-LABELED");
								fileOptimusItem.setObservacion("EXITO: Se inutilizo el archivo Inutilizar archivo: ");
								continue;
							} catch (IOException e) {
								log.debug("Error de accion ultima de Borrar archivo, cambiando Extension .DELETE "
										+ fileDest.getAbsolutePath());
								fileOptimusItem.setEstado("ERROR-DELETED");
								fileOptimusItem.setObservacion(
										"ERROR: No se pudo Borrar/ Inutilizar archivo: " + e.getMessage());
								continue;
							}
						}
					}
				} // fin accion:BORRAR

				if (accionBorrarCheckin && "checkin".equalsIgnoreCase(fileOptimusItem.getType())) {
					log.debug("Borrando un archivos checkin ");
					String fileCheckin = fileOptimusItem.getDirBase() + fileOptimusItem.getRelaPath()
							+ fileOptimusItem.getOnlyName();
					File fileInAction = new File(fileCheckin);
					log.debug("archivo a borrar (checkin):  " + fileCheckin);
					//
					if(!fileInAction.exists() || fileInAction.isDirectory()) { 
						fileOptimusItem.setEstado("ERROR");
						fileOptimusItem.setObservacion("El archivo no existe. directorio equivocado." + fileInAction.toString());
						continue;
					}
					//
					boolean actionbool = FileUtils.deleteQuietly(fileInAction);
					if (actionbool) {
						log.debug("El archivo fue borrado con EXITO");
						fileOptimusItem.setEstado("DELETED");
						fileOptimusItem.setObservacion("El archivo fue borrado con EXITO");
						continue;
					} else {
						actionbool = FileUtils.deleteQuietly(fileInAction);
						if (actionbool) {
							log.debug("El archivo fue borrado con EXITO");
							fileOptimusItem.setEstado("DELETED");
							fileOptimusItem.setObservacion("El archivo fue borrado con EXITO");
							continue;
						} else {
							File fileDest = null;
							try {
								fileDest = new File(fileInAction.getAbsolutePath() + ".DELETED");
								FileUtils.moveFile(fileInAction, fileDest);
								log.debug("Exito Archivo renombrado (inutilizado) exitosamente "
										+ fileDest.getAbsolutePath());
								fileOptimusItem.setEstado("DELETE-LABELED");
								fileOptimusItem.setObservacion(
										"EXITO: Se inutilizo el archivo - extension DELETED agregada  : ");
								continue;
							} catch (IOException e) {
								log.debug("Error de accion ultima de Borrar archivo, cambiando Extension .DELETE "
										+ fileDest.getAbsolutePath());
								fileOptimusItem.setEstado("ERROR-DELETED");
								fileOptimusItem.setObservacion(
										"ERROR: No se pudo Borrar/ Inutilizar archivo: " + e.getMessage());
								continue;
							}
						}
					}
				} // fin accion:BORRAR

				if (accionEnviar) {
					boolean containsPoint = nameFile.contains(".");
					if (!containsPoint) {
						log.debug("Error el archivo no tiene extension, no se encuentra el punto, ejemplo: .TXT");
						fileOptimusItem.setEstado("ERROR");
						fileOptimusItem.setObservacion(
								"Error el archivo no tiene extension, no se encuentra el punto, ejemplo: .TXT");
						continue;
					}
					int lugarUltimoPunto = nameFile.lastIndexOf(".");
					String strFilenameWithoutExtension = fileOptimusItem.getAbsFileName().substring(0,
							lugarUltimoPunto);

					File fileInAction = new File(req.getPath(), nameFile);
					boolean fileExiste = fileInAction.exists();
					if (!fileExiste) {
						log.debug("El archivo (" + nameFile + ") no existe en el directorio ");
						fileOptimusItem.setEstado("NOT-FOUND");
						fileOptimusItem.setObservacion("El archivo (" + nameFile + ") no fue encontrado " + new Date());
						continue;
					} else {
						log.debug("Archivo por Modificarse:" + fileInAction.getAbsolutePath());
						File fileDestino = new File(req.getPath(), strFilenameWithoutExtension + "." + extensionSEND);
						if (fileDestino.exists()) {
							log.debug("El archivo DESTINO  existe, y no deberia:  " + fileDestino);
							fileOptimusItem.setEstado("FILE-SEND-EXIST");
							fileOptimusItem.setObservacion("El archivo fue encontrado antes de " + new Date());
						} else {
							try {
								log.debug(" INTENTANDO MOVER EL ARCHIVO " + nameFile);
								LocalDate date = LocalDate.now();
								String rutaBackupFinal = rutaBackup + "/" + date.toString() + "/" + req.getDomain() 
														+ "/" +fileOptimusItem.getOnlyName();
								log.debug(" INTENTANDO MOVER AL DIRECTORIO " + rutaBackupFinal);
								FileUtils.copyFile(fileInAction, new File(rutaBackupFinal));
								FileUtils.moveFile(fileInAction, fileDestino);
								log.debug("req.getPath():"+req.getPath());
								log.debug("fileInAction"+fileInAction);
								log.debug("fileDestino:"+fileDestino);
								if (fileDestino.exists()) {
									log.debug("El archivo DESTINO  se creo exitosamente:  " + fileDestino);
									fileOptimusItem.setEstado("FILE-SEND-CREATED");
									fileOptimusItem.setObservacion(
											"El archivo (" + fileDestino + ") fue creado antes de " + new Date());
								}
							} catch (IOException e) {
								log.debug("Error al enviar archivo (Cambio Extension):  " + fileDestino);
								fileOptimusItem.setEstado("FILE-ERROR");
								fileOptimusItem.setObservacion("ERROR: " + e.getMessage());
								continue;
							}
						}

					}

				} // fin accion:ENVIAR

				if (accionEnviarCheckin && "checkin".equalsIgnoreCase(fileOptimusItem.getType())) {
					log.info("CHECKING MOVIENDO ARCHIVOS");
					String nombreArchivo = fileOptimusItem.getDirBase() + fileOptimusItem.getRelaPath()
							+ fileOptimusItem.getOnlyName();
					File fileInAction = new File(nombreArchivo);
					log.info("checkin-File-envio-Pago: " + nombreArchivo);

					fileInAction = new File(nombreArchivo);

					boolean fileExiste = fileInAction.exists();
					if (!fileExiste) {
						log.debug("El archivo (" + nombreArchivo + ") no existe en el directorio ");
						fileOptimusItem.setEstado("NOT-FOUND");
						fileOptimusItem
								.setObservacion("El archivo (" + nombreArchivo + ") no fue encontrado " + new Date());
						continue;
					} else {
						log.debug("Archivo por Modificarse:" + fileInAction.getAbsolutePath());
						String fileGoBank = rutaExitoPosCheckin + fileOptimusItem.getRelaPath()
								+ fileOptimusItem.getOnlyName();
						File fileDestino = new File(fileGoBank);

						if (fileDestino.exists()) {
							log.debug("El archivo DESTINO  existe, y no deberia:  " + fileDestino);
							fileOptimusItem.setEstado("FILE-SEND-EXIST");
							fileOptimusItem.setObservacion("El archivo fue encontrado antes de " + new Date());
						} else {
							try {
								log.debug(" INTENTANDO MOVER EL ARCHIVO " + nameFile);
								FileUtils.moveFile(fileInAction, fileDestino);
								if (fileDestino.exists()) {
									log.debug("El archivo DESTINO  se creo exitosamente:  " + fileDestino);
									fileOptimusItem.setEstado("FILE-SEND-CREATED");
									fileOptimusItem.setObservacion(
											"El archivo (" + fileDestino + ") fue creado antes de " + new Date());
								}
							} catch (IOException e) {
								log.debug("Error al enviar archivo (Cambio Extension):  " + fileDestino);
								fileOptimusItem.setEstado("FILE-ERROR");
								fileOptimusItem.setObservacion("ERROR: " + e.getMessage());
								continue;
							}
						}

					}

				} // fin accion:accionEnviarCheckin

			}

		}
//		res.setSolicitud(req);

		res.setCodigo("" + req.getAccion());
		res.setPath(req.getPath());
		res.setDomain(req.getDomain());
		res.setUser(req.getUser());
		res.setAccion(req.getAccion());
		if (req.getFiles() != null) {
			res.setFiles(req.getFiles());
		}
		res.setEstado("EXITO");
		res.setObs("EXITO");
		return res;
	}

	
	
	public ReverseResponse executeReverse(ReverseRequest request) {
		String[] partsDate = request.getFecha().split(",");
		ReverseResponse response = new ReverseResponse();
		List<String> NombreArchivos = new ArrayList<String>();
		Arrays.asList(partsDate).forEach( date -> {
				String rutaBackupBuscar = rutaBackup + "/" + date + "/" + request.getDomain();
				File carpeta = new File(rutaBackupBuscar);
				File[] archivos = carpeta.listFiles();
				if (archivos == null || archivos.length == 0) {
				    System.out.println("No hay elementos dentro de la carpeta actual");
				    log.info("No hay elementos dentro de la carpeta actual");
				    return;
				}else {
					String fileInicio = rutaBackupBuscar;
					String fileDestino = "";
					SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
					
					if(request.getIdentificador().equals("B")) fileDestino = rutaRecibidosBank;
					else if(request.getIdentificador().equals("C")) fileDestino = rutaRecibidosInterbank;
					else fileDestino = rutaRecibidosTarjeta;
			        
				    for (int i=0; i< archivos.length; i++) {
				        File archivo = archivos[i];
				        log.info("Mover archivo desde: "+ fileInicio + "/" + archivo.getName());
			        	log.info("Mover archivo hacia: "+ fileDestino + "/" + archivo.getName());
				        try {
				        	 
							FileUtils.moveFile(new File(fileInicio + "/" + archivo.getName()), 
											   new File(fileDestino + "/" + archivo.getName()));
							File archivoEliminar = new File(rutaBackupJson + "/" + archivo.getName() + ".json");
							NombreArchivos.add(archivo.getName());
							if (archivoEliminar.delete())
								log.info("El fichero ha sido borrado satisfactoriamente: " + archivoEliminar.getName());
						    else {
						    	log.error("Error inesperado");
								response.setRespuesta("ERROR");
								response.setDescripcion("error al eliminar archivos .json verifique en los logs y rutas de archivos");
						    }					
						  } catch (IOException e) {
							log.error("Error inesperado: "+ e.getMessage());
							response.setRespuesta("ERROR");
							response.setDescripcion("no se obtuvo informacion del servicio o sucedio algo inesperado");
						}
				        
				        System.out.println(String.format("%s (%s) - %d - %s",
				                archivo.getName(),
				                archivo.isDirectory() ? "Carpeta" : "Archivo",
				                archivo.length(),
				                sdf.format(archivo.lastModified())
				                ));
				    }
				
				}
				
		});
		
		if(response == null || Strings.isNullOrEmpty(response.getRespuesta())) {
			response.setRespuesta("EXITO");
			response.setDescripcion("Se realizo el respaldo correctamente");
			response.setNombreArchivos(NombreArchivos);
		}
		log.info("Respuesta: "+ response);
		return response;
	}
}
