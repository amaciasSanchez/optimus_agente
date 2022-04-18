package claro.edx.optimus.processs.util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FileWalker {

    public void  walk( String path, List<String> lista ) {
        File root = new File( path );
        File[] list = root.listFiles();

        if (list == null) return ;

        for ( File f : list ) {
            if ( f.isDirectory() ) {
                walk( f.getAbsolutePath(), lista );
            }
            else {
            	lista.add(f.getAbsolutePath());
            }
        }
        return ;
    }

    public static void main(String[] args) {
        FileWalker fw = new FileWalker();
        List<String> lista = new ArrayList<>();
        fw.walk("C:\\0SYS\\u01",lista);
        System.out.println(lista.size());
        for(String a : lista) {
        	System.out.println( a );
        }
        
    }

}