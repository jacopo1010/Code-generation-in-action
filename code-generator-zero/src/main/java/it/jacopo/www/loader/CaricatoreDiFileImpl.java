package it.jacopo.www.loader;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;


public class CaricatoreDiFileImpl implements CaricatoreDiFile{

	private Properties properties;

	public CaricatoreDiFileImpl() {
		this.properties = new Properties();
	}

	@Override
	public File carica(String path) {

		try (InputStream input = new FileInputStream(path)) {
			this.properties.load(input);

			String posizioneFile = properties.getProperty(PropertiesCostanti.MODEL_DOMAIN_PATH);

			if (posizioneFile == null || posizioneFile.isEmpty()) {
				throw new RuntimeException(
						"Il file application.properties è configurato male. " +
								"Devi inserire la proprietà: " + PropertiesCostanti.MODEL_DOMAIN_PATH
						);
			}

			Path propertiesPath = Paths.get(path).toAbsolutePath();
			Path modelPath = Paths.get(posizioneFile);

			if (!modelPath.isAbsolute()) {
				modelPath = propertiesPath.getParent().resolve(modelPath).normalize();
			}

			if (!Files.exists(modelPath)) {
				throw new RuntimeException("Il file del modello non esiste: " + modelPath);
			}

			if (Files.isDirectory(modelPath)) {
				throw new RuntimeException("Il path configurato punta a una directory: " + modelPath);
			}

			if (!Files.isRegularFile(modelPath)) {
				throw new RuntimeException("Il path configurato non è un file valido: " + modelPath);
			}

			String fileName = modelPath.getFileName().toString().toLowerCase();
			if (!fileName.endsWith(".xml") && !fileName.endsWith(".xmi")) {
				throw new RuntimeException("Il file configurato non è un XML/XMI valido: " + modelPath);
			}

			return modelPath.toFile();

		} catch (Exception e) {
			throw new RuntimeException("Errore nella lettura del file properties", e);
		}
	}


	@Override
	public Properties getApplicationProperties() {
		return this.properties;
	}


}
