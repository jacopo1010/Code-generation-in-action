package it.jacopo.www.loader;

import java.io.File;
import java.util.Properties;

public interface CaricatoreDiFile {

    public File carica(String path);
	
    public Properties getApplicationProperties();
}
