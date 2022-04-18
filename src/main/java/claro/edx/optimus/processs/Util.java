package claro.edx.optimus.processs;

import java.io.File;
import java.io.FilenameFilter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import claro.edx.optimus.bean.FileView;

public class Util {

	public static List<FileView> getFileList(String path, final String[] extensionsfilter, final org.apache.logging.log4j.Logger log) {
		List<FileView> response = new ArrayList<>();
		DateFormat format = new SimpleDateFormat("MMMM dd, yyyy HH:mm");
		FilenameFilter filter = new FilenameFilter() {
			@Override
			public boolean accept(File f, String name) {
				for (String ext : extensionsfilter) {
					if (name.endsWith(ext)) {
						log.debug("Util:getFileList >>>    ACCEPT FILE: "+name);
						return true;
					}
				}
				return false;
			}
		};
		File f = new File(path);

//	 String[] pathnames = f.list(filter);
		File[] files = f.listFiles(filter);
		log.debug("Util:getFileList >>>       Archivos encontrados Directorio:: "+path+" = "+(files==null?"0":files.length) );
		if (files != null) {
			for (int i = 0; i < files.length; i++) {
				log.debug("Util:getFileList >>>      File in action:" + files[i].getName());
				FileView mf = new FileView();
				mf.setNameFile(files[i].getName());
				mf.setDateModified(format.format(files[i].lastModified()));
				mf.setExtension(FilenameUtils.getExtension(files[i].getName()));
//				if( mf.getExtension()!=null) {
//					mf.setExtension(mf.getExtension().toUpperCase());
//				}
				mf.setSize(FileUtils.byteCountToDisplaySize(files[i].length()));
				mf.setPath(path);
				mf.setPath( files[i].getParent() );
				if(files[i].getName()!=null && files[i].getName().contains("_")) {
					String[] aux = files[i].getName().split("_");
					if(aux.length>0) {
						mf.setCodigoFin(aux[0]);
					}
				}
				
				log.debug("Util:getFileList >>>       FileView creado con exito: " +files[i].getName() );
				response.add(mf);
			}
		}

		return response;
	}
}
