package claro.edx.optimus.processs;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import claro.edx.optimus.bean.BrandCode;
import claro.edx.optimus.bean.FileOptimus;
import claro.edx.optimus.bean.FileView;
import claro.edx.optimus.bean.StatusDirRequest;
import claro.edx.optimus.bean.StatusDirRequest.FilesByPath;
import claro.edx.optimus.bean.StatusDirResponse;
import claro.edx.optimus.constants.FileviewerConstants;
import claro.edx.optimus.processs.util.Stopwatch;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
@EnableScheduling
public class StatusDirectoryScheduler {

	@Value("${RESTinvoke.fileViewerPutStatusEndpoint}")
	private String fileViewerPutStatusEndpoint;

	@Value("${ruta.lectura.1}")
	String rutaRecibidosHW1;
	@Value("${ruta.lectura.2}")
	String rutaRecibidosHW2;
	@Value("${ruta.lectura.3}")
	String rutaRecibidosHW3;

	@Value("${filterFilesRecibidos}")
	String filterFilesRecibidos;
	
	@Autowired
	private ObjectMapper objectMapper;
	
	/* datos de los catalogos de: codigos de tarjetas de credito y de los codigos de formato de archivo*/
	private File datosJsonBrandCodes= new File("/dockerappsvol/dockerappdata/edx/optimusScheduler/Metadata/MetadataBrandCodes.json");


	
	/**
	 * 
	 * <b>TITKE</b><br />
	 * <br />
	 * <br />
	 * <br />
	 * <br />
	 */
	@Scheduled(cron = "${statusDirectoryScheduler.cron}")
	public void ejecutarJobsCalendarizados() {
		try {
			log.info("\n\n STATUS DIRECTORY SCHEDULER - Lista de archivos actuales RECIBIDOS y CHECK-IN ");

			Stopwatch timerGlobal = new Stopwatch();
			
			StatusDirRequest dirRequest = new StatusDirRequest();
			
			dirRequest.setFechaLectura(new Date());
			List<String> directoriesPath = new ArrayList<String>();
			dirRequest.setArchivosByPath(new ArrayList<FilesByPath>());
			
			
			List<BrandCode> brandCodesList  = objectMapper.readValue(datosJsonBrandCodes, new TypeReference<List<BrandCode>>() {});
			
			
			log.debug("RUTA 1"+rutaRecibidosHW1);
			log.debug("RUTA 2"+rutaRecibidosHW2);
			log.debug("RUTA 3"+rutaRecibidosHW3);
			
			///////////////// ARRAY DE LAS EXTENSIONES ///////////////
			// -------------------------------------------------------
			log.debug("Filtrando los registros de recibidos:" + filterFilesRecibidos);
			String[] tmp = filterFilesRecibidos.split("\\|");
			int valor = tmp.length;
			String[] filterHW = new String[valor];
			for (int i = 0; i < tmp.length; i++) {
				filterHW[i] = tmp[i];
			}
			
			
			
			////////////////////////////////////////////
			// ----------------------------------------
			List<FileView> listFilesExistentes = new ArrayList<>();
			List<FileView> listFilesRecibidos1 = null;
			List<FileView> listFilesRecibidos2 = null;
			List<FileView> listFilesRecibidos3 = null;

			
			
			if (rutaRecibidosHW1 != null && !rutaRecibidosHW1.isEmpty() && !"NULL".equalsIgnoreCase(rutaRecibidosHW1)) {
				listFilesRecibidos1 = Util.getFileList(rutaRecibidosHW1, filterHW, log);
				directoriesPath.add(rutaRecibidosHW1);
				log.debug("Hay Lectura Archivos en Ruta: " + rutaRecibidosHW1 + " EXISTEN:"
						+ (listFilesRecibidos1 == null ? 0 : listFilesRecibidos1.size()) + "archivos");
				dirRequest.getArchivosByPath().add(StatusDirRequest.newInstance(rutaRecibidosHW1, listFilesRecibidos1));
			}
			
			if (rutaRecibidosHW2 != null && !rutaRecibidosHW2.isEmpty() && !"NULL".equalsIgnoreCase(rutaRecibidosHW2)) {
				listFilesRecibidos2 = Util.getFileList(rutaRecibidosHW2, filterHW, log);
				log.debug("Hay Lectura Archivos en Ruta: " + rutaRecibidosHW2 + " EXISTEN:"
						+ (listFilesRecibidos2 == null ? 0 : listFilesRecibidos2.size()) + "archivos");
				directoriesPath.add(rutaRecibidosHW2);
				dirRequest.getArchivosByPath().add(StatusDirRequest.newInstance(rutaRecibidosHW2, listFilesRecibidos2));
			}
			
			if (rutaRecibidosHW3 != null && !rutaRecibidosHW3.isEmpty() && !"NULL".equalsIgnoreCase(rutaRecibidosHW3)) {
				listFilesRecibidos3 = Util.getFileList(rutaRecibidosHW3, filterHW, log);
				log.debug("Hay Lectura Archivos en Ruta: " + rutaRecibidosHW3 + " EXISTEN:"
						+ (listFilesRecibidos3 == null ? 0 : listFilesRecibidos3.size()) + "archivos");
				directoriesPath.add(rutaRecibidosHW3);
				dirRequest.getArchivosByPath().add(StatusDirRequest.newInstance(rutaRecibidosHW3, listFilesRecibidos3));
			}
			
			log.info("Consolidando los archivos en listFilesExistentes: "+listFilesExistentes);
			if (listFilesRecibidos1 != null) 	listFilesExistentes.addAll(listFilesRecibidos1);
			log.debug("Unificado 1: "+listFilesExistentes.size());
			if (listFilesRecibidos2 != null)    listFilesExistentes.addAll(listFilesRecibidos2);
			log.debug("Unificado 2: "+listFilesExistentes.size());
			if (listFilesRecibidos3 != null)    listFilesExistentes.addAll(listFilesRecibidos3);
			log.debug("Unificado 3: "+listFilesExistentes.size());
			
			log.debug("Finalmente unificados  los listas de archivos, hay: "
					+ ( (listFilesExistentes == null) ? 0 : listFilesExistentes.size()) + "archivos");

			////////////////////////////////////////////
			// ----------------------------------------			
			dirRequest.setDirectoryPaths(directoriesPath);
			
			
			log.info("PROCESANDO LOS ARCHIVOS \n\n"+listFilesExistentes);
			List<FileOptimus> filesInterBank = new ArrayList<FileOptimus>();
			List<FileOptimus> filesBank = new ArrayList<FileOptimus>();
			List<FileOptimus> filesCreditCards = new ArrayList<FileOptimus>();

			if (listFilesExistentes != null) {
				for (FileView itemFileHw : listFilesExistentes) {
					// AL NO EXISTIR el json DEBE procesarlo.
					boolean isInterBank = itemFileHw.getNameFile().startsWith(FileviewerConstants.DOMAIN_CONECEL.toUpperCase());
					if (isInterBank) {
						filesInterBank.add(new FileOptimus(itemFileHw));
					} else { 
						
						if (brandCodesList.size() >= 1) {
							for (int i = 0; i < brandCodesList.size(); i++) {
								boolean isCreditCard = itemFileHw.getNameFile().startsWith(brandCodesList.get(i).getBrandCode());
						        if (isCreditCard){
						        	filesCreditCards.add(new FileOptimus(itemFileHw));
						        }else{ // is BANK
						        	filesBank.add(new FileOptimus(itemFileHw));
						        }
							}	
						}	
					}

				} // FIN_FOR
			}
			dirRequest.setArchivosBank(filesBank);
			dirRequest.setArchivosInter(filesInterBank);
			dirRequest.setArchivosCreditCards(filesCreditCards);
			dirRequest.setEstado("EXITO");
			log.info("URL envio status de DIRECTORIO" + fileViewerPutStatusEndpoint);
			RestTemplate restTemplate = new RestTemplate();
			StatusDirResponse response = restTemplate.postForObject(fileViewerPutStatusEndpoint, dirRequest,
					StatusDirResponse.class);
			if (response != null) {
				log.info("SE ENVIO STATUS-DIRECTORIO: " + response.getCodigo() + " Mensaje:" + response.getMensaje());
			}
			
			log.info("TIEMPO-STATUS-DIRECTORY: " + timerGlobal.elapsedTimeMiliseconds());
			
		} catch (Exception e) {
			log.error("ERROR",e);
			e.printStackTrace();
		}
	}
}
