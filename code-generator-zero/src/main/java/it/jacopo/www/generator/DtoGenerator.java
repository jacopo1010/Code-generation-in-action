package it.jacopo.www.generator;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import freemarker.template.TemplateException;
import it.jacopo.www.io.IO;
import it.jacopo.www.model.MetaClass;

public class DtoGenerator extends FileGenerator{

	private static final String DTO_TEMPLATE_NAME = "freemarker/dtoTemplate.ftl";

	public DtoGenerator(IO io) {
		super(io);
	}

	public void generate(String packageModel, String packageDto, Map<String, MetaClass> metaClasses, String outputRoot){
		try {
			this.validateOutputRoot(outputRoot);
			this.validateMetaClasses(metaClasses);
			this.validatePackage(packageModel);
			this.validatePackage(packageDto);

			File outputDirectory = this.prepareOutputDirectory(outputRoot, packageDto);
			for (MetaClass metaClass : metaClasses.values()) {
				Map<String, Object> data = new HashMap<String, Object>();
				data.put("metaClass", metaClass);
				data.put("metaClasses", metaClasses);
				data.put("packageModel", packageModel);
				data.put("packageDto", packageDto);

				File generatedDestination = new File(outputDirectory, metaClass.getName() + "Dto.java");
				this.renderTemplateToFileIfAbsent(DTO_TEMPLATE_NAME, data, generatedDestination);
			}
			this.getIo().stampaMessaggio("Generazione completata con successo!");
		} catch (IOException | TemplateException e) {
			throw new RuntimeException("Errore durante la generazione dei model", e);
		}
	}
}
