package it.jacopo.www;

import java.io.File;
import java.util.Map;
import java.util.Properties;

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
		Properties properties = this.loader.getApplicationProperties();
		String orm = properties.getProperty(PropertiesCostanti.ORM);
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
			this.createSchemaSql(metaClasses, path);
			this.createModel(metaClasses, path);
			this.createDao(metaClasses, path);
			this.createService(metaClasses, path);
			return metaClasses;
		} else {
			throw new IllegalArgumentException("Devi inserire " + PropertiesCostanti.ORM);
		}
	}
	
	
	private void createModel(Map<String, MetaClass> metaClasses, String applicationPropertiesPath) {
		Properties properties = this.loader.getApplicationProperties();
		String output = properties.getProperty(PropertiesCostanti.MODEL_OUTPUT_PATH);
		String packageName = properties.getProperty(PropertiesCostanti.PACKAGE_MODEL);
		this.marker.generateModel(packageName, metaClasses,
				FileUtil.resolveConfiguredPath(applicationPropertiesPath, output));
	}
  
	private void createSchemaSql(Map<String, MetaClass> metaClasses, String applicationPropertiesPath) {
		String output = this.loader.getApplicationProperties().getProperty(PropertiesCostanti.SQL_SCHEMA_PATH);
		this.marker.generateSchema(metaClasses, FileUtil.resolveConfiguredPath(applicationPropertiesPath, output));
	}

	private void createDao(Map<String,MetaClass> metaClasses, String applicationPropertiesPath) {
		Properties properties = this.loader.getApplicationProperties();
		String packageDao = properties.getProperty(PropertiesCostanti.DAO_OUTPUT_PACKAGE);
		String packageModel = properties.getProperty(PropertiesCostanti.PACKAGE_MODEL);
		this.marker.generateDao(packageModel, packageDao, metaClasses,
				FileUtil.resolveDaoOutputPath(applicationPropertiesPath, this.loader));
	}

	private void createService(Map<String, MetaClass> metaClasses, String applicationPropertiesPath) {
		Properties properties = this.loader.getApplicationProperties();
		String packageDao = properties.getProperty(PropertiesCostanti.DAO_OUTPUT_PACKAGE);
		String packageModel = properties.getProperty(PropertiesCostanti.PACKAGE_MODEL);
		String packageService = properties.getProperty(PropertiesCostanti.SERVICE_OUTPUT_PACKAGE);
		if (packageService == null || packageService.trim().isEmpty()) {
			packageService = packageDao.replace(".dao", ".service");
		}
		this.marker.generateService(packageModel, packageDao, packageService, metaClasses,
				FileUtil.resolveServiceOutputPath(applicationPropertiesPath, this.loader));
	}
}
