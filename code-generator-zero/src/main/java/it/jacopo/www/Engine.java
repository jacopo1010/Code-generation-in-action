package it.jacopo.www;

import java.io.File;
import java.util.Map;
import java.util.Properties;

import it.jacopo.www.generator.FreeMarkerManager;
import it.jacopo.www.generator.GeneratoreDiEntita;
import it.jacopo.www.generator.GeneratoreDiMetaClass;
import it.jacopo.www.generator.JooqGenerator;
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
		Map<String, MetaClass> metaClasses = this.metaCreator.generaMetaClass(xmlFile);
		File sqlFile = this.createSchemaSql(metaClasses, path);
		this.createModel(metaClasses, path);
		JooqGenerator jooq = new JooqGenerator(io);
		jooq.generateDtoAndDao(sqlFile, properties, path);
		this.createRepository(metaClasses, path);
		this.createService(metaClasses, path);
		this.createController(metaClasses, path);
		return metaClasses;
	}

	

	private void createModel(Map<String, MetaClass> metaClasses, String applicationPropertiesPath) {
		Properties properties = this.loader.getApplicationProperties();
		String output = properties.getProperty(PropertiesCostanti.JAVA_OUTPUT_PATH);
		String packageName = properties.getProperty(PropertiesCostanti.PACKAGE_MODEL);
		this.marker.generateModel(packageName, metaClasses,
				FileUtil.resolveJavaOutputPath(applicationPropertiesPath, output));
	}

	private File createSchemaSql(Map<String, MetaClass> metaClasses, String applicationPropertiesPath) {
		String output = this.loader.getApplicationProperties().getProperty(PropertiesCostanti.SQL_SCHEMA_PATH);
		File sqFile = this.marker.generateSchema(metaClasses, FileUtil.resolveConfiguredPath(applicationPropertiesPath, output));
		return sqFile;
	}
	
	private void createRepository(Map<String, MetaClass> metaClasses, String path) {
	    Properties properties = this.loader.getApplicationProperties();
	    String output = properties.getProperty(PropertiesCostanti.JAVA_OUTPUT_PATH);
	    String packageModel = properties.getProperty(PropertiesCostanti.PACKAGE_MODEL);
	    String jooqPackage = properties.getProperty(PropertiesCostanti.JOOQ_OUTPUT_PACKAGE);
	    String packageRepository = properties.getProperty(PropertiesCostanti.REPOSITORY_OUTPUT_PACKAGE);
	    if (packageRepository == null || packageRepository.trim().isEmpty()) {
	        packageRepository = jooqPackage.replace(".jooq", ".repository");
	    }
	    this.marker.generateRepository(packageModel, jooqPackage,
	            packageRepository, metaClasses,
	            FileUtil.resolveJavaOutputPath(path, output));
	}

	private void createService(Map<String, MetaClass> metaClasses, String applicationPropertiesPath) {
		Properties properties = this.loader.getApplicationProperties();
		String output = properties.getProperty(PropertiesCostanti.JAVA_OUTPUT_PATH);
		String packageModel = properties.getProperty(PropertiesCostanti.PACKAGE_MODEL);
		String packageRepository = properties.getProperty(PropertiesCostanti.REPOSITORY_OUTPUT_PACKAGE);
		String packageService = properties.getProperty(PropertiesCostanti.SERVICE_OUTPUT_PACKAGE);
		if (packageRepository == null || packageRepository.trim().isEmpty()) {
			String jooqPackage = properties.getProperty(PropertiesCostanti.JOOQ_OUTPUT_PACKAGE);
			packageRepository = jooqPackage.replace(".jooq", ".repository");
		}
		if (packageService == null || packageService.trim().isEmpty()) {
			packageService = packageRepository.replace(".repository", ".service");
		}
		this.marker.generateService(packageModel, packageRepository, packageService, metaClasses,
				FileUtil.resolveJavaOutputPath(applicationPropertiesPath, output));
	}

	private void createController(Map<String, MetaClass> metaClasses, String applicationPropertiesPath) {
		Properties properties = this.loader.getApplicationProperties();
		String output = properties.getProperty(PropertiesCostanti.JAVA_OUTPUT_PATH);
		String packageModel = properties.getProperty(PropertiesCostanti.PACKAGE_MODEL);
		String packageService = properties.getProperty(PropertiesCostanti.SERVICE_OUTPUT_PACKAGE);
		String packageController = properties.getProperty(PropertiesCostanti.CONTROLLER_OUTPUT_PACKAGE);
		if (packageController == null || packageController.trim().isEmpty()) {
			packageController = packageService.replace(".service", ".controller");
		}
		this.marker.generateController(packageModel, packageService, packageController, metaClasses,
				FileUtil.resolveJavaOutputPath(applicationPropertiesPath, output));
	}
}
