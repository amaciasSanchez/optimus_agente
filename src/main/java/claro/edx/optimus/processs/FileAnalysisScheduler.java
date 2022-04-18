package claro.edx.optimus.processs;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import claro.edx.optimus.bean.BrandCode;
import claro.edx.optimus.bean.DataByBank;
import claro.edx.optimus.bean.DataByBank.BankBreakdown;
import claro.edx.optimus.bean.DataByCreditCard;
import claro.edx.optimus.bean.FileView;
import claro.edx.optimus.bean.StatusDirResponse;
import claro.edx.optimus.bean.SumaryDigitalPayment;
import claro.edx.optimus.constants.FileviewerConstants;
import claro.edx.optimus.processs.util.Stopwatch;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
@EnableScheduling
public class FileAnalysisScheduler {

	@Value("${ruta.lectura.1}")
	String rutaRecibidosHW1;
	@Value("${ruta.lectura.2}")
	String rutaRecibidosHW2;
	@Value("${ruta.lectura.3}")
	String rutaRecibidosHW3;
	

	@Value("${interbank.id}")
	String strInterbankId;

	@Value("${interbank.name}")
	String strInterbankName;

	@Value("${RESTinvoke.fileViewerPutResumenEndpoint}")
	private String fileViewerPutResumenEndpoint;

	@Value("${filterFilesRecibidos}")
	String filterFilesRecibidos;
	
	@Value("${filterFilesWALKED}")
	String filterFilesWALKED;

	@Value("${ruta.escritura}")
	String rutaWalkedFiles;

	@Value("${dataPayment.dividir100.bank}")
	boolean divide100Bank;

	@Value("${dataPayment.dividir100.interbank}")
	boolean divide100InterBank;

	@Value("${dataPayment.activateFileAnalyzer}")
	boolean activateFileAnalyzer;

	@Autowired
	ObjectMapper objectMapper;
	
	
	/* datos de los catalogos de: codigos de tarjetas de credito y de los codigos de formato de archivo*/
	private File datosJsonBrandCodes= new File("/dockerappsvol/dockerappdata/edx/optimusScheduler/Metadata/MetadataBrandCodes.json");


	// proceso principal del CRON 0 * * ? * *
	// TODO estudiar las expresiones CRON
	@Scheduled(cron = "${fileAnalysisScheduler.cron}")
	public void ejecutarJobsCalendarizados() throws JsonParseException, JsonMappingException, IOException {
		log.debug("\n\n\\n\\n FILE ANALYSIS SCHEDULER - Analiza archivos bancarios e interbancarios, y de tarjetas de credito ");

		List<BrandCode> brandCodesList  = objectMapper.readValue(datosJsonBrandCodes, new TypeReference<List<BrandCode>>() {});

		
		log.debug("RUTA 1"+rutaRecibidosHW1);
		log.debug("RUTA 2"+rutaRecibidosHW2);
		log.debug("RUTA 3"+rutaRecibidosHW3);
		
		if (activateFileAnalyzer) {
			log.debug("ON Analyzer files");

			Stopwatch timerGlobal = new Stopwatch();
			
			////////////////////////////////////////////
			// ----------------------------------------
			log.debug("Filtrando los registros de recibidos:" + filterFilesRecibidos);
			String[] tmp = filterFilesRecibidos.split("\\|");
			String[] tmp2 = filterFilesWALKED.split("\\|");
			int valor = tmp.length; //(tamano de array)}
			int valor2 = tmp2.length; //(tamano de array)}
			
			log.debug(">> En respuesta  recibidos tiene " + valor + " archivos");
			
			//FILTER array  de archivos 02recibidos
			String[] filterHW = new String[valor];
			
			// FILTER array para archivos resumen en carpeta 06WALKED
			String[] filterPro = new String[valor2];
			
			for (int i = 0; i < tmp.length; i++) {
				filterHW[i] = tmp[i];
			}
			for (int i = 0; i < tmp2.length; i++) {
				filterPro[i] = tmp2[i];
			}

			////////////////////////////////////////////
			// ----------------------------------------
			log.debug("\n\n  LISTA ARCHIVOS RECIBIDOS (Maximo 3)+++++++++++++++++++++++++++++++++++++++++++++++");
			List<FileView> listFilesRecibidos = new ArrayList<>();
			List<FileView> listFilesRecibidos1 = null;
			List<FileView> listFilesRecibidos2 = null;
			List<FileView> listFilesRecibidos3 = null;

			log.info("Se analiza el archivo de la ruta {}", rutaRecibidosHW1);
			if (rutaRecibidosHW1 != null && !rutaRecibidosHW1.isEmpty() && !"NULL".equalsIgnoreCase(rutaRecibidosHW1)) {
				listFilesRecibidos1 = Util.getFileList(rutaRecibidosHW1, filterHW, log);
				log.debug("Hay Lectura Archivos en Ruta: " + rutaRecibidosHW1 + " EXISTEN:"
						+ (listFilesRecibidos1 == null ? 0 : listFilesRecibidos1.size()) + "archivos");
			}
			log.info("Se analiza el archivo de la ruta {}", rutaRecibidosHW2);
			if (rutaRecibidosHW2 != null && !rutaRecibidosHW2.isEmpty() && !"NULL".equalsIgnoreCase(rutaRecibidosHW2)) {
				listFilesRecibidos2 = Util.getFileList(rutaRecibidosHW2, filterHW, log);
				log.debug("Hay Lectura Archivos en Ruta: " + rutaRecibidosHW2 + " EXISTEN:"
						+ (listFilesRecibidos2 == null ? 0 : listFilesRecibidos2.size()) + "archivos");
			}
			log.info("Se analiza el archivo de la ruta {}", rutaRecibidosHW3);
			if (rutaRecibidosHW3 != null && !rutaRecibidosHW3.isEmpty() && !"NULL".equalsIgnoreCase(rutaRecibidosHW3)) {
				listFilesRecibidos3 = Util.getFileList(rutaRecibidosHW3, filterHW, log);
				log.debug("Hay Lectura Archivos en Ruta: " + rutaRecibidosHW3 + " EXISTEN:"
						+ (listFilesRecibidos3 == null ? 0 : listFilesRecibidos3.size()) + "archivos");
			}
			
			log.info("POR CONSOLIDAR(1): listFilesRecibidos: "+listFilesRecibidos);
			
			if (listFilesRecibidos1 != null)
				listFilesRecibidos.addAll(listFilesRecibidos1);
			log.debug("Unificado 1: "+listFilesRecibidos.size());
			
			if (listFilesRecibidos2 != null)
				listFilesRecibidos.addAll(listFilesRecibidos2);
			log.debug("Unificado 2: "+listFilesRecibidos.size());
			
			if (listFilesRecibidos3 != null)
				listFilesRecibidos.addAll(listFilesRecibidos3);
			log.debug("Unificado 3: "+listFilesRecibidos.size());

			
			log.info("CONSOLIDADO(2): listFilesRecibidos: "+listFilesRecibidos);
			
			
			////////////////////////////////////////////
			// ----------------------------------------
			log.debug("\n\n  LISTA ARCHIVOS JSON  +++++++++++++++++++++++++++++++++++++++++++++++");
			//el metodo getFileList absorve la complejidad de retornar ARchivos de un directorio.
			List<FileView> ljsonFiles = Util.getFileList(rutaWalkedFiles, filterPro, log);

			
			
			if (listFilesRecibidos != null) {
				// DIRECTORIO NO ESTA VACIO
				
				/////JAVA 8 (stream es una tecnica de java 8)
//				Map<String, FileView> mapWalkedFile = ljsonFiles.stream()
//						.collect(Collectors.toMap(FileView::getNameFile, fileView -> fileView));
				
				/////JAVA 7
				// CONVERTE la lista de archivos JSON en un mapa JSON
				Map<String, FileView> mapWalkedFile = new HashMap<String, FileView>();
				for(FileView item: ljsonFiles) {
					mapWalkedFile.put(item.getNameFile(), item);
				}
				
				
				log.debug("los .json file se ponen en un MAPA para tener disponible para recorrer cada  "
						+ "archivo .TXT de recibidos y saber si fue procesado. " + mapWalkedFile);

				////////////////////////////////////////////
				// ----------------------------------------
				// RECORRER LOS ARCHIVOS .TXT de RECIBIDOS
				for (FileView itemFileHw : listFilesRecibidos) {
					log.debug("\n\n\n //////////////////////////////////////////////////////");
					boolean exist = mapWalkedFile.containsKey(itemFileHw.getNameFile() + ".json");
					log.info("El Archivo " + itemFileHw.getNameFile() + " tiene un archivo .json en WALKED-DIR: "
							+ "existe :" + exist);
					if (!exist) {
						// El archivo se busco entre json y NO-EXITO json!!
						// POR LO TANTO: voy analizarlo
						log.debug(
								"Como no existe se procesa, pero primero vemos si es INTERBANK(CONECEL)  o BANK(##_) o CREDITCARD");
						// AL NO EXISTIR el json DEBE procesarlo.
						boolean isInterBank = itemFileHw.getNameFile().startsWith(strInterbankId);		
						if (isInterBank) {
							log.debug("PROCESAR: Vamos a procesar el archivo: " + itemFileHw.getNameFile()
									+ " procesado en  INTERBANK");
							// Se van los archivos CONECEL
							processInterbank(new File(itemFileHw.getPath(), itemFileHw.getNameFile()));
						} else {
							
							/* valida si es un archivo de tarjeta de credito
							 * consulta metadata con informacion de los codigos de tarjeta de credito
							 */
							if (brandCodesList.size() >= 1) {
								for (int i = 0; i < brandCodesList.size(); i++) {
									boolean isCreditCard = itemFileHw.getNameFile().startsWith(brandCodesList.get(i).getBrandCode());
							        if (isCreditCard){
							        	
							        	log.debug("PROCESAR: Vamos a procesar el archivo: " + itemFileHw.getNameFile()
										+ " procesado en  BANK");
							        	/* 
							        	 * Se van a procesar los archivos 
							        	 * 3002, 3003, 3004 ... 3050, 435 => para visa
							        	 * 3010, 3011  					  => para american
							        	 * 3006, 3007, 3008 ... 3046, 3049 => para mastercard
							        	 * 3001                            => para diners
							        	 * 3012, 3013, 3014, 3015          => para discover
							        	 * 4684, 4685  					  => para alia
							        	 */
							        	processCreditCard(new File(itemFileHw.getPath(), itemFileHw.getNameFile()));

							        }else{ // is BANK
							        	log.debug("PROCESAR: Vamos a procesar el archivo: " + itemFileHw.getNameFile()
										+ " procesado en  BANK");
							        	// Se van los archivos 10,33, 37, 30....
										processBank(new File(itemFileHw.getPath(), itemFileHw.getNameFile()));
							        }
								}	
							}
							
						}

					}
				}
			}
			
			log.info("TIEMPO-FILE-ANALYSIS: " + timerGlobal.elapsedTimeMiliseconds());
			
		} else {
			log.debug(
					"OFF analyzer. Revisa las variables del archivo application.properties -dataPayment.activateFileAnalyzer-  TRUE|FALSE ");
		}
	}

	/**
	 * 
	 * <b>CONECEL File type </b><br />
	 * <br />
	 * <br />
	 * <br />
	 * <br />
	 * 
	 * @param file
	 */
	public void processInterbank(File file) {
		try {
			////////////////////// VARIABLES //////////////////////
			// ----------------------------------------
			String out = null;
			SumaryDigitalPayment sumaryDigitalPayment = new SumaryDigitalPayment();
			DataByBank interbankData= new DataByBank();
			interbankData.setBankId(strInterbankId);
			interbankData.setBankName(strInterbankName);
			interbankData.setTotal(0.0);
			interbankData.setAmount(0);
			List<BankBreakdown> lBankBreakdowns = new ArrayList<BankBreakdown>();
			interbankData.setBreakdown(lBankBreakdowns);
			List<DataByBank> dataByBanks = new ArrayList<DataByBank>();
			dataByBanks.add(interbankData);
			sumaryDigitalPayment.setDataByBanks(dataByBanks);
			
			///////////////////  -ARCHIVO PROCESO AWK- /////////////////////////
			// ----------------------------------------------------------------
//			String commando = "awk 'BEGIN { FS=OFS=SUBSEP=\" \"}{count[$8]+=1;sum[$8]+=$6}END {for (i in count) print i,count[i],sum[i]}' "
//					+ file.getAbsolutePath();
//			String[] command = { "bash", "-c", commando };
			String commando = "./fileMonitor.sh " + '"'+file.getAbsolutePath()+'"';
			String[] command = { "sh", "-c", commando };
			log.debug("\n\nINTERBANK:: COMANDO:" + commando);
			Process process = Runtime.getRuntime().exec(command);
			//Process process = Runtime.getRuntime().exec(new String[]{"sh", "-c", "./fileMonitor.sh"});
//			process.waitFor();  
//			process.exitValue();
			InputStreamReader entrada = new InputStreamReader(process.getInputStream());
			BufferedReader stdInput = new BufferedReader(entrada);
			///////////////////  -LECTURA RESULTADO- /////////////////////////
			// ----------------------------------------------------------------
			log.debug("\n\nINTERBANK::  Se proceso el archivo");
			
			int i =0;
			while ((out = stdInput.readLine()) != null) {
				String[] data = out.split(" ");
				Double total = Double.parseDouble(data[2]);
				int amount = Integer.parseInt(data[1]);
				interbankData.setTotal(interbankData.getTotal() + total);
				interbankData.setAmount(interbankData.getAmount()+amount);
//				totalAc = totalAc + total;
//				contador = contador + amount;
//				DataByBank dataByBank = DataByBank.newInstance(data[0],  total, amount);
				BankBreakdown bank = DataByBank.newInstanceBankBreakdown(data[0],  total, amount);
//				dataByBanks.add(dataByBank);
				lBankBreakdowns.add(bank);
				log.debug("\t\t >> INTERBANK:: dataByBank("+i+"): " + bank);
				i++;
			}
			
			if(i != 0){
				///////////////////////////////////////////////
				// AJUSTE DE LOS VALORES DIVIDIR para 100
				if (divide100InterBank && interbankData.getTotal() != null && interbankData.getTotal() > 0.0) {
					interbankData.setTotal( interbankData.getTotal() / 100.0 );
				}
				// ============================================
	
				sumaryDigitalPayment.setAbsoluteName(file.getAbsolutePath());
				sumaryDigitalPayment.setFileName(file.getName());
				sumaryDigitalPayment.setAmount(interbankData.getAmount());
				sumaryDigitalPayment.setDataByBanks(dataByBanks);
				sumaryDigitalPayment.setTotal(interbankData.getTotal());
				sumaryDigitalPayment.setType(FileviewerConstants.PAYMENT_TYPE_INTERBANK);
				
				
				//PARA OBETENER LOTE
				//------------------------------------------------------
				String nombre = "10_20191204_20200311_1153141_000003.TXT";
				nombre = file.getName();
				if(nombre!=null && nombre.contains("_")) {
					String[] a = nombre.split("_");
					if(a!=null && a.length==2 ) {
						sumaryDigitalPayment.setNameSp0(a[0]);
						sumaryDigitalPayment.setNameSp1(a[1]);
					}
					if(a!=null && a.length==3 ) {
						sumaryDigitalPayment.setNameSp0(a[0]);
						sumaryDigitalPayment.setNameSp1(a[1]);
						sumaryDigitalPayment.setNameSp2(a[2]);
					}
					if(a!=null &&  a.length>=4  ) {
	
						sumaryDigitalPayment.setNameSp0(a[0]);
						sumaryDigitalPayment.setNameSp1(a[1]);
						sumaryDigitalPayment.setNameSp2(a[2]);
						sumaryDigitalPayment.setNameSp3(a[3]);
					}
				}
				
				
				
				String pattern = "yyyy-MM-dd";
				SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
				String date = simpleDateFormat.format(new Date());
				sumaryDigitalPayment.setResumeDate(date);
				log.debug("INTERBANK::  " + sumaryDigitalPayment);
	
				
				///////////////////  -JSON- /////////////////////////
				// --------------------------------------------------
				// LINEA CONVIERTE java a JSON
				String json = objectMapper.writeValueAsString(sumaryDigitalPayment);
	
				log.debug("FILE" + sumaryDigitalPayment.getFileName() + " --JSON--:\n" + json);
				
				///////////////////  -REST-TEMPLATE- /////////////////////////
				// --------------------------------------------------
				// (llama al fileviewer (calback API) para que guarde el json en baseDatos MONGO)
				StatusDirResponse resp = sendFileViewer(sumaryDigitalPayment);
				boolean exitoGuardarFile = false;
				log.debug("RESPUESTA call01: " + resp);
				if (resp == null || !"EXITO".equals(resp.getCodigo())) {
					StatusDirResponse resp2 = sendFileViewer(sumaryDigitalPayment);
					log.debug("RESPUESTA call02: " + resp2);
					if (resp2 != null && "EXITO".equals(resp2.getCodigo())) {
						exitoGuardarFile = true;
					} else {
						log.error("Error: no se logro enviar el ");
					}
				} else {
					exitoGuardarFile = true;
				}
				if (exitoGuardarFile) {
					log.debug("EXITO, se procede a guardar el archivo " + file.getName() + "Walked-DIR");
					FileUtils.writeStringToFile(
							new File(rutaWalkedFiles + System.getProperty("file.separator") + file.getName() + ".json"),
							json, StandardCharsets.UTF_8, false);
				}
				
			
			}else {
				log.error("ERROR AL PROCESAR ARCHIVO "+ i);
			}
		} catch (Exception e) {
			log.error("ERROR ",e);
			e.printStackTrace();
		}

	}

	
	
	
	public void processCreditCard(File file) {
		try {
			String out = null;
			SumaryDigitalPayment sumaryDigitalPayment = new SumaryDigitalPayment();
			List<DataByCreditCard> dataByCreditCards = new ArrayList<DataByCreditCard>();
			Double totalAc = 0.0;
			Integer amountAc = 0;
			/* -ARCHIVO PROCESO AWK-  */
			String commando = "awk -F \"|\"  {'if($1==\"3\") print'} " + file.getAbsolutePath();
			String[] command = { "bash", "-c", commando };
			Process process = Runtime.getRuntime().exec(command);
			log.debug("\n\n  CREDIT CARD :: COMANDO: " + commando);
			InputStreamReader entrada = new InputStreamReader(process.getInputStream());
			BufferedReader stdInput = new BufferedReader(entrada);
			
///////////////////  -LECTURA RESULTADO- /////////////////////////
		// ----------------------------------------------------------------
		
		int i =0;
		log.debug("\n\n CREDITCARD::  Se proceso el archivo");
		while ((out = stdInput.readLine()) != null) {
			try {
				String[] data = out.split("\\|");
				totalAc = Double.parseDouble(data[2]);
				if(totalAc!=null && totalAc >0.0) {
					totalAc = totalAc/100.00;
				}
				amountAc = Integer.parseInt(data[1]);
				DataByCreditCard dataByCreditCard = DataByCreditCard.newInstance(file.getName().split("_")[0], totalAc, amountAc);
				dataByCreditCards.add(dataByCreditCard);
				log.debug("\t\t >> BANK:: dataByCreditCards("+i+"): " + dataByCreditCards);
				i++;
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		stdInput.close();
		if(i != 0) {	
			sumaryDigitalPayment.setAbsoluteName(file.getAbsolutePath());
			sumaryDigitalPayment.setFileName(file.getName());
			sumaryDigitalPayment.setAmount(amountAc);
			sumaryDigitalPayment.setDataByCreditCards(dataByCreditCards);
			sumaryDigitalPayment.setTotal(Double.valueOf(totalAc));
			sumaryDigitalPayment.setType(FileviewerConstants.PAYMENT_TYPE_CREDIT_CARD);
			
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat(FileviewerConstants.DAY_PATTERN);
			String date = simpleDateFormat.format(new Date());
			sumaryDigitalPayment.setResumeDate(date);
			log.debug("CREDITCARD::  " + sumaryDigitalPayment);
			
			
			//PARA OBETENER LOTE
			//------------------------------------------------------
			///String nombre = "10_20191204_20200311_1153141_000003.TXT";
			String nombre = file.getName();
			if(nombre!=null && nombre.contains("_")) {
				String[] a = nombre.split("_");
				if(a!=null && a.length==2 ) {
					sumaryDigitalPayment.setNameSp0(a[0]);
					sumaryDigitalPayment.setNameSp1(a[1]);
				}
				if(a!=null && a.length==3 ) {
					sumaryDigitalPayment.setNameSp0(a[0]);
					sumaryDigitalPayment.setNameSp1(a[1]);
					sumaryDigitalPayment.setNameSp2(a[2]);
				}
				if(a!=null &&  a.length>=4  ) {

					sumaryDigitalPayment.setNameSp0(a[0]);
					sumaryDigitalPayment.setNameSp1(a[1]);
					sumaryDigitalPayment.setNameSp2(a[2]);
					sumaryDigitalPayment.setNameSp3(a[3]);
				}
			}
			
			
			

			///////////////////  -JSON- /////////////////////////
			// --------------------------------------------------
			String json = objectMapper.writeValueAsString(sumaryDigitalPayment);
			log.debug("//////////////////////////////////////////////////////");
			log.debug("CREDITCARD:: JSON: " + json);

			///////////////////  -REST-TEMPLATE- /////////////////////////
			// --------------------------------------------------
			StatusDirResponse resp = sendFileViewer(sumaryDigitalPayment);
			boolean exitoGuardarFile = false;
			log.debug("RESPUESTA call01: " + resp);
			if (resp == null || !"EXITO".equals(resp.getCodigo())) {
				StatusDirResponse resp2 = sendFileViewer(sumaryDigitalPayment);
				log.debug("RESPUESTA call02: " + resp);
				if (resp2 != null && "EXITO".equals(resp2.getCodigo())) {
					exitoGuardarFile = true;
				} else {
					log.error("Error: no se logro enviar el ");
				}
			} else {
				exitoGuardarFile = true;
			}
			if (exitoGuardarFile) {
				log.debug("EXITO, se procede a guardar el archivo " + file.getName() + "Walked-DIR");
				FileUtils.writeStringToFile(
						new File(rutaWalkedFiles + System.getProperty("file.separator") + file.getName() + ".json"),
						json, StandardCharsets.UTF_8, false);
			}
		}else {
			log.error("ERROR AL PROCESAR ARCHIVO ");						
		}
			
		}catch (Exception e) {
			log.error("ERROR ",e);
			e.printStackTrace();
		}
		
		
	}
	
	
	public void processBank(File file) {

		try {
			//////////////////////VARIABLES //////////////////////
			// ---------------------------------------------------
			String out = null;
			SumaryDigitalPayment sumaryDigitalPayment = new SumaryDigitalPayment();
			List<DataByBank> dataByBanks = new ArrayList<DataByBank>();
			Double totalAc = 0.0;
			Integer amountAc = 0;
			
			

			///////////////////  -ARCHIVO PROCESO AWK- /////////////////////////
			// ----------------------------------------------------------------
			String commando = "awk -F \"|\"  {'if($1==\"3\") print'} " + file.getAbsolutePath();
			String[] command = { "bash", "-c", commando };
			Process process = Runtime.getRuntime().exec(command);
			log.debug("\n\nBANK:: COMANDO: " + commando);
			InputStreamReader entrada = new InputStreamReader(process.getInputStream());
			BufferedReader stdInput = new BufferedReader(entrada);


			
			///////////////////  -LECTURA RESULTADO- /////////////////////////
			// ----------------------------------------------------------------
			
			int i =0;
			log.debug("\n\nBANK::  Se proceso el archivo");
			while ((out = stdInput.readLine()) != null) {
				try {
					String[] data = out.split("\\|");
					totalAc = Double.parseDouble(data[2]);
					if(totalAc!=null && totalAc >0.0) {
						totalAc = totalAc/100.00;
					}
					amountAc = Integer.parseInt(data[1]);
					DataByBank dataByBank = DataByBank.newInstance(file.getName().split("_")[0], totalAc, amountAc);
					dataByBanks.add(dataByBank);
					log.debug("\t\t >> BANK:: dataByBank("+i+"): " + dataByBank);
					i++;
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			stdInput.close();
			if(i != 0) {	
				sumaryDigitalPayment.setAbsoluteName(file.getAbsolutePath());
				sumaryDigitalPayment.setFileName(file.getName());
				sumaryDigitalPayment.setAmount(amountAc);
				sumaryDigitalPayment.setDataByBanks(dataByBanks);
				sumaryDigitalPayment.setTotal(Double.valueOf(totalAc));
				sumaryDigitalPayment.setType(FileviewerConstants.PAYMENT_TYPE_BANK);

				SimpleDateFormat simpleDateFormat = new SimpleDateFormat(FileviewerConstants.DAY_PATTERN);
				String date = simpleDateFormat.format(new Date());
				sumaryDigitalPayment.setResumeDate(date);
				log.debug("BANK::  " + sumaryDigitalPayment);
				
				
				//PARA OBETENER LOTE
				//------------------------------------------------------
				String nombre = "10_20191204_20200311_1153141_000003.TXT";
				nombre = file.getName();
				if(nombre!=null && nombre.contains("_")) {
					String[] a = nombre.split("_");
					if(a!=null && a.length==2 ) {
						sumaryDigitalPayment.setNameSp0(a[0]);
						sumaryDigitalPayment.setNameSp1(a[1]);
					}
					if(a!=null && a.length==3 ) {
						sumaryDigitalPayment.setNameSp0(a[0]);
						sumaryDigitalPayment.setNameSp1(a[1]);
						sumaryDigitalPayment.setNameSp2(a[2]);
					}
					if(a!=null &&  a.length>=4  ) {
	
						sumaryDigitalPayment.setNameSp0(a[0]);
						sumaryDigitalPayment.setNameSp1(a[1]);
						sumaryDigitalPayment.setNameSp2(a[2]);
						sumaryDigitalPayment.setNameSp3(a[3]);
					}
				}
				
				
				
	
				///////////////////  -JSON- /////////////////////////
				// --------------------------------------------------
				String json = objectMapper.writeValueAsString(sumaryDigitalPayment);
				log.debug("//////////////////////////////////////////////////////");
				log.debug("BANK:: JSON: " + json);
	
				///////////////////  -REST-TEMPLATE- /////////////////////////
				// --------------------------------------------------
				StatusDirResponse resp = sendFileViewer(sumaryDigitalPayment);
				boolean exitoGuardarFile = false;
				log.debug("RESPUESTA call01: " + resp);
				if (resp == null || !"EXITO".equals(resp.getCodigo())) {
					StatusDirResponse resp2 = sendFileViewer(sumaryDigitalPayment);
					log.debug("RESPUESTA call02: " + resp);
					if (resp2 != null && "EXITO".equals(resp2.getCodigo())) {
						exitoGuardarFile = true;
					} else {
						log.error("Error: no se logro enviar el ");
					}
				} else {
					exitoGuardarFile = true;
				}
				if (exitoGuardarFile) {
					log.debug("EXITO, se procede a guardar el archivo " + file.getName() + "Walked-DIR");
					FileUtils.writeStringToFile(
							new File(rutaWalkedFiles + System.getProperty("file.separator") + file.getName() + ".json"),
							json, StandardCharsets.UTF_8, false);
				}
			}else {
				log.error("ERROR AL PROCESAR ARCHIVO ");						
			}


		} catch (Exception e) {
			log.error("ERROR ",e);
			e.printStackTrace();
		}
	}

	
	/**
	 * 
	 * <b>TITKE</b><br />
	 *		<br />
	 *		<br />
	 *		<br />
	 *		<br />
	 *@param sumaryDigitalPayment
	 *@return
	 */
	public StatusDirResponse sendFileViewer(SumaryDigitalPayment sumaryDigitalPayment) {
		log.debug("SCHEDULER - SEND-FileResumen " + sumaryDigitalPayment.getAbsoluteName());
		log.debug("URL envio status de DIRECTORIO: " + fileViewerPutResumenEndpoint);
		RestTemplate restTemplate = new RestTemplate();
		StatusDirResponse response = restTemplate.postForObject(fileViewerPutResumenEndpoint, sumaryDigitalPayment,
				StatusDirResponse.class);
		return response;
	}



}
