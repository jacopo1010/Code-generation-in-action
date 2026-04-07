package it.jacopo.www;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

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
	private IO io;

	public Engine(IO io) {
		this.io = io;
		this.loader = new CaricatoreDiFileImpl();
		this.metaCreator = new GeneratoreDiMetaClass(io);
	}

	public CaricatoreDiFile getLoader() {
		return loader;
	}

	public void setLoader(CaricatoreDiFile loader) {
		this.loader = loader;
	}

	public Map<String, MetaClass> generate(String path){
		Map<String, MetaClass> classiDiDominio = new LinkedHashMap<String, MetaClass>();
		File xmlFile = this.loader.carica(path);
		String orm = this.loader.getApplicationProperties().getProperty(PropertiesCostanti.ORM);
		if (orm == null || orm.isEmpty()) {
			throw new RuntimeException(
					"Il file application.properties è configurato male. " +
							"Devi inserire la proprietà: " + PropertiesCostanti.ORM);
		}
		if(orm.equals("true")) {
			//invoca metodo per hibernate 
		}else if(orm.equals("false")){
			//invoca metodo per JDBC
			MetaClass meta = this.metaCreator.generaMetaClass(xmlFile);
		}else {
			throw new IllegalArgumentException("Devi inserire " + PropertiesCostanti.ORM);
		}
		return classiDiDominio;
	}

}
