package it.jacopo.www.generator;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import freemarker.template.TemplateException;
import it.jacopo.www.io.IO;
import it.jacopo.www.model.MetaClass;

public class ControllerGenerator extends FileGenerator {

	private static final String CONTROLLER_TEMPLATE_NAME = "freemarker/controllerTemplate.ftl";
	private static final String CONTROLLER_WRAPPER_TEMPLATE_NAME = "freemarker/controllerWrapperTemplate.ftl";

	public ControllerGenerator(IO io) {
		super(io);
	}

	public void generate(String packageModel, String packageService, String packageController,
			Map<String, MetaClass> metaClasses, String outputRoot) {
		try {
			this.validateOutputRoot(outputRoot);
			this.validateMetaClasses(metaClasses);
			this.validatePackage(packageModel);
			this.validatePackage(packageService);
			this.validatePackage(packageController);

			File outputDirectory = this.prepareOutputDirectory(outputRoot, packageController);
			for (MetaClass metaClass : metaClasses.values()) {
				Map<String, Object> data = new HashMap<String, Object>();
				data.put("metaClass", metaClass);
				data.put("packageModel", packageModel);
				data.put("packageService", packageService);
				data.put("packageController", packageController);
				data.put("metaClasses", metaClasses);

				File generatedDestination = new File(outputDirectory, metaClass.getName() + "ControllerBase.java");
				this.renderTemplateToFile(CONTROLLER_TEMPLATE_NAME, data, generatedDestination);
				this.getIo().stampaMessaggio("Generato: " + generatedDestination.getAbsolutePath());

				File wrapperDestination = new File(outputDirectory, metaClass.getName() + "Controller.java");
				this.renderTemplateToFileIfAbsent(CONTROLLER_WRAPPER_TEMPLATE_NAME, data, wrapperDestination);
			}
		} catch (IOException | TemplateException e) {
			throw new RuntimeException("Errore durante la generazione dei controller", e);
		}
	}
}
