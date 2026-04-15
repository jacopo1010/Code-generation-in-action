package it.jacopo.www;

import java.nio.file.Path;
import java.nio.file.Paths;
import it.jacopo.www.loader.PropertiesCostanti;

public class FileUtil {
  
	public static String resolveConfiguredPath(String applicationPropertiesPath, String configuredPath) {
		if (configuredPath == null || configuredPath.trim().isEmpty()) {
			return configuredPath;
		}
        
		configuredPath = configuredPath.trim();   
		Path configured = Paths.get(configuredPath);
		if (configured.isAbsolute()) {
			return configured.normalize().toString();
		}

		Path propertiesFile = Paths.get(applicationPropertiesPath).toAbsolutePath().normalize();
		Path propertiesDirectory = propertiesFile.getParent();
		if (propertiesDirectory == null) {
			return configured.normalize().toString();
		}

		return propertiesDirectory.resolve(configured).normalize().toString();
	}

	public static String resolveJavaOutputPath(String applicationPropertiesPath, String configuredPath) {
		if (configuredPath == null || configuredPath.trim().isEmpty()) {
			throw new IllegalArgumentException(
					"Definire nell'application.properties la proprietà " + PropertiesCostanti.JAVA_OUTPUT_PATH);
		}
		return resolveConfiguredPath(applicationPropertiesPath, configuredPath);
	}
	
} 
