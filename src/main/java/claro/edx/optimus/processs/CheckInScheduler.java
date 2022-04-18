package claro.edx.optimus.processs;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

import claro.edx.optimus.bean.CheckinDirRequest;
import claro.edx.optimus.bean.FileCheckin;
import claro.edx.optimus.bean.StatusDirResponse;
import claro.edx.optimus.processs.util.FileWalker;
import claro.edx.optimus.processs.util.Stopwatch;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
@EnableScheduling
public class CheckInScheduler {

	// ZONA_CHECK_IN
	// ruta de donde vamos a tener la fase de checkin, el subdirectorio es complejo
	// por lo tanto solo invoco a los archivos y los filtro
	@Value("${ruta.lectura.checkin}")
	String rutaLecturaCheckin;
	@Value("${filterFiles.checkin}")
	String filterFilesCheckin;

	@Value("${dataPayment.activateFileAnalyzer}")
	boolean activateFileAnalyzer;

	@Value("${RESTinvoke.fileViewerPutcheckInDirEndpoint}")
	private String fileViewerPutCheckinDirEndpoint;

	@Autowired
	ObjectMapper objectMapper;

	// proceso principal del CRON 0 * * ? * *
	// TODO estudiar las expresiones CRON
	@Scheduled(cron = "${fileAnalysisScheduler.cron}")
	public void ejecutarJobsCalendarizados() {
		log.debug("\n\n\\n\\n Checkin SCHEDULER - Analiza archivos bancarios e interbancarios ");

		Stopwatch timerGlobal = new Stopwatch();
		
		log.debug("RUTA Checkin" + rutaLecturaCheckin);
		if (activateFileAnalyzer) {
			log.debug("ON Analyzer files for Checkin");

			/////////////////// -ARCHIVO PROCESO AWK- /////////////////////////
			// ----------------------------------------------------------------
			FileWalker fwalker =  new FileWalker();
			List<String> archivos = new ArrayList<>();
			fwalker.walk(rutaLecturaCheckin, archivos);

			log.info(" Checkin ARCHIVOS:");
//			if(log.isDebugEnabled()) {
				for(String a : archivos) {
		        	log.info( a );
		        }
//			}
			
			
			////////////////////////////////////////////
			// ----------------------------------------
			log.debug("Filtro  los registros Checkin:" + filterFilesCheckin);
			String[] tmp = filterFilesCheckin.split("\\|");
			int valor = tmp.length; // (tamano de array)
			log.debug(">> En respuesta  recibidos tiene " + valor + " tipos de extensiones de archivos");
			// FILTER array de archivos checkin
			String[] filter = new String[valor];
			for (int i = 0; i < tmp.length; i++) {
				filter[i] = tmp[i];
			}

////////////////////////////////////////////
// ----------------------------------------			

			log.info(" Checkin PROCESANDO LOS ARCHIVOS \n\n" + archivos);
			List<FileCheckin> filesChenkin = new ArrayList<FileCheckin>();
			DateFormat format = new SimpleDateFormat("MMMM dd, yyyy HH:mm");

			if (archivos != null) {
				for (String itemPath : archivos) {
					FileCheckin nf = null;
					if (itemPath == null || itemPath.isEmpty())
						continue;
					File file = new File(itemPath);
					log.info("FILE CHECKIN - Enviar"+ file.getAbsolutePath());

					if (file.exists()) {
						nf = new FileCheckin();
						nf.setName(file.getName());
						String pathRelative =file.getParent()+"/";
						pathRelative = pathRelative.replace(rutaLecturaCheckin, "");
						nf.setPath(pathRelative);
						nf.setDateModified(format.format(file.lastModified()));
						nf.setExtension(FilenameUtils.getExtension(file.getName()));
						nf.setSize(FileUtils.byteCountToDisplaySize(file.length()));
						nf.setDirBase(rutaLecturaCheckin);
						nf.setCarpeta("");
						nf.setSubgrupo("");
						try {
							if(pathRelative!=null) {
								log.debug("putCheckinDir Split PATH para obtener datos");
								String[] a = pathRelative.split("/");
								log.debug("putCheckinDir Split "+a);
								if(a!=null && a.length>=2) {
									nf.setCarpeta(a[1]);	
								}
								if(a!=null && a.length>=3) {
									nf.setCarpeta(a[1]);	
									nf.setSubgrupo(a[2]);
								}
							}
							}catch(Exception e) {
								e.printStackTrace();
							}
					}
					if (nf != null)
						filesChenkin.add(nf);
				} // FIN_FOR

			}
			
			CheckinDirRequest dirRequest = new CheckinDirRequest();
			dirRequest.setDirectoryPath(rutaLecturaCheckin);
			dirRequest.setArchivos(filesChenkin);
			dirRequest.setEstado("EXITO");
			dirRequest.setCodigo("0");
			log.info("REST-TEMPLATE "+ fileViewerPutCheckinDirEndpoint );
			Stopwatch restcallTime = new Stopwatch();
			RestTemplate restTemplate = new RestTemplate();
			StatusDirResponse response = restTemplate.postForObject(fileViewerPutCheckinDirEndpoint, dirRequest,
					StatusDirResponse.class);
			log.info("TIEMPO-REST-TEMPLATE: " + restcallTime.elapsedTimeMiliseconds()  );
			if (response != null) {
				log.info("Checkin SE ENVIO STATUS-DIRECTORIO: " + response.getCodigo() + " Mensaje:"
						+ response.getMensaje());
			}
			log.info("TIEMPO-CHECKIN-SCHEDULER: " + timerGlobal.elapsedTimeMiliseconds());

		} else {
			log.debug(
					"OFF analyzer. Revisa las variables del archivo application.properties -dataPayment.activateFileAnalyzer-  TRUE|FALSE ");
		}
	}

}
