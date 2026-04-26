package it.jacopo.www.generator;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import freemarker.template.TemplateException;
import it.jacopo.www.io.IO;
import it.jacopo.www.model.MetaClass;

public class ModelGenerator extends FileGenerator {

	private static final String MODEL_TEMPLATE_NAME = "freemarker/modelTemplate.ftl";
	private static final String MODEL_WRAPPER_TEMPLATE_NAME = "freemarker/modelWrapperTemplate.ftl";

	public ModelGenerator(IO io) {
		super(io);
	}

	public void generate(String packageModel, Map<String, MetaClass> metaClasses, String outputRoot) {
		try {
			this.validateOutputRoot(outputRoot);
			this.validateMetaClasses(metaClasses);
			this.validatePackage(packageModel);

			File outputDirectory = this.prepareOutputDirectory(outputRoot, packageModel);
			for (MetaClass metaClass : metaClasses.values()) {
				Map<String, Object> data = new HashMap<String, Object>();
				data.put("metaClass", metaClass);
				data.put("metaClasses", metaClasses);
				data.put("packageName", packageModel);

				File generatedDestination = new File(outputDirectory, metaClass.getName() + "Base.java");
				this.renderTemplateToFile(MODEL_TEMPLATE_NAME, data, generatedDestination);
				this.getIo().stampaMessaggio("Generato: " + generatedDestination.getAbsolutePath());

				File wrapperDestination = new File(outputDirectory, metaClass.getName() + ".java");
				this.renderTemplateToFileIfAbsent(MODEL_WRAPPER_TEMPLATE_NAME, data, wrapperDestination);
			}
			this.getIo().stampaMessaggio("Generazione completata con successo!");
		} catch (IOException | TemplateException e) {
			throw new RuntimeException("Errore durante la generazione dei model", e);
		}
	}
}
