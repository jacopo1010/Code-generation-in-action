package it.jacopo.www;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;

import it.jacopo.www.generator.FreeMarkerManager;
import it.jacopo.www.generator.GeneratoreDiEntita;
import it.jacopo.www.generator.GeneratoreDiMetaClass;
import it.jacopo.www.io.IO;
import it.jacopo.www.loader.CaricatoreDiFile;
import it.jacopo.www.loader.CaricatoreDiFileImpl;
import it.jacopo.www.loader.PropertiesCostanti;
import it.jacopo.www.model.MetaClass;

public class Engine {

	private CaricatoreDiFile loader;
	private GeneratoreDiEntita metaCreator;
	private FreeMarkerManager marker;
	private IO io;

	public Engine(IO io) {
		this.io = io;
		this.loader = new CaricatoreDiFileImpl();
		this.metaCreator = new GeneratoreDiMetaClass(io);
		this.marker = new FreeMarkerManager(io);
	}

	public CaricatoreDiFile getLoader() {
		return loader;
	}

	public void setLoader(CaricatoreDiFile loader) {
		this.loader = loader;
	}

	
	public GeneratoreDiEntita getMetaCreator() {
		return metaCreator;
	}

	public void setMetaCreator(GeneratoreDiEntita metaCreator) {
		this.metaCreator = metaCreator;
	}

	public FreeMarkerManager getMarker() {
		return marker;
	}

	public void setMarker(FreeMarkerManager marker) {
		this.marker = marker;
	}

	public Map<String, MetaClass> generate(String path){
		File xmlFile = this.loader.carica(path);
		String orm = this.loader.getApplicationProperties().getProperty(PropertiesCostanti.ORM);
		if (orm == null || orm.isEmpty()) {
			throw new RuntimeException(
					"Il file application.properties è configurato male. " +
							"Devi inserire la proprietà: " + PropertiesCostanti.ORM);
		}
		if ("true".equalsIgnoreCase(orm)) {
			throw new UnsupportedOperationException(
					"La generazione con Hibernate non è ancora implementata. Imposta "
							+ PropertiesCostanti.ORM + "=false per generare i model Java.");
		} else if ("false".equalsIgnoreCase(orm)) {
			Map<String, MetaClass> metaClasses = this.metaCreator.generaMetaClass(xmlFile);
			this.createModel(metaClasses, path);
			this.createSchemaSql(metaClasses, path);
			return metaClasses;
		} else {
			throw new IllegalArgumentException("Devi inserire " + PropertiesCostanti.ORM);
		}
	}
	
	
	private void createModel(Map<String, MetaClass> metaClasses, String applicationPropertiesPath) {
		String output = this.loader.getApplicationProperties().getProperty(PropertiesCostanti.MODEL_OUTPUT_PATH);
		String packageName = this.loader.getApplicationProperties().getProperty(PropertiesCostanti.PACKAGE_MODEL);
		this.marker.generateModel(packageName, metaClasses, this.resolveConfiguredPath(applicationPropertiesPath, output));
	}
  
	private void createSchemaSql(Map<String, MetaClass> metaClasses, String applicationPropertiesPath) {
		String output = this.loader.getApplicationProperties().getProperty(PropertiesCostanti.SQL_SCHEMA_PATH);
		this.marker.generateSchema(metaClasses, this.resolveConfiguredPath(applicationPropertiesPath, output));
	}

	private String resolveConfiguredPath(String applicationPropertiesPath, String configuredPath) {
		if (configuredPath == null || configuredPath.trim().isEmpty()) {
			return configuredPath;
		}

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
}
