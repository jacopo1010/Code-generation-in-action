package it.jacopo.www;

import java.io.File;
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
			this.createModel(metaClasses);
			return metaClasses;
		} else {
			throw new IllegalArgumentException("Devi inserire " + PropertiesCostanti.ORM);
		}
	}
	
	
	private void createModel(Map<String, MetaClass> metaClasses) {
		String output = this.loader.getApplicationProperties().getProperty(PropertiesCostanti.MODEL_OUTPUT_PATH);
		String packageName = this.loader.getApplicationProperties().getProperty(PropertiesCostanti.PACKAGE_MODEL);
		this.marker.generateModel(packageName, metaClasses, output);
	}
  
	private void createSchemaSql(Map<String, MetaClass> metaClasses) {
		String output = this.loader.getApplicationProperties().getProperty(PropertiesCostanti.MODEL_OUTPUT_PATH);
		this.marker.generateModel(metaClasses, output);
	}
}
