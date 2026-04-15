package it.jacopo.www;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import it.jacopo.www.loader.CaricatoreDiFile;
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
	public static String getConfiguredProperty(String primaryKey, String fallbackKey, CaricatoreDiFile loader) {
		Properties properties = loader.getApplicationProperties();
		String value = properties.getProperty(primaryKey);
		if (value == null || value.trim().isEmpty()) {
			return properties.getProperty(fallbackKey);
		}
		return value;
	}
	
	
	public static String resolveDaoOutputPath(String applicationPropertiesPath, CaricatoreDiFile loader) {
		String outputDao = getConfiguredProperty(
				PropertiesCostanti.DAO_OUTPUT_PATH,
				PropertiesCostanti.MODEL_OUTPUT_PATH,
				loader);
		return resolveConfiguredPath(applicationPropertiesPath, outputDao);
	}
	
	public static String resolveServiceOutputPath(String applicationPropertiesPath, CaricatoreDiFile loader) {
		String outputService = getConfiguredProperty(
				PropertiesCostanti.SERVICE_OUTPUT_PATH,
				PropertiesCostanti.DAO_OUTPUT_PATH,
				loader);
		if (outputService == null || outputService.trim().isEmpty()) {
			outputService = getConfiguredProperty(
					PropertiesCostanti.SERVICE_OUTPUT_PATH,
					PropertiesCostanti.MODEL_OUTPUT_PATH,
					loader);
		}
		return resolveConfiguredPath(applicationPropertiesPath, outputService);
	}
	
} 
