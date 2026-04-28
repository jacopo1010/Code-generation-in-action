package it.jacopo.www.generator;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import freemarker.template.TemplateException;
import it.jacopo.www.io.IO;
import it.jacopo.www.model.MetaClass;

public class ServiceGenerator extends FileGenerator {

	private static final String SERVICE_TEMPLATE_NAME = "freemarker/serviceTemplate.ftl";
	private static final String SERVICE_WRAPPER_TEMPLATE_NAME = "freemarker/serviceWrapperTemplate.ftl";

	public ServiceGenerator(IO io) {
		super(io);
	}

	public void generate(String packageModel, String packageRepository, String packageService,
			Map<String, MetaClass> metaClasses, String outputRoot) {
		try {
			this.validateOutputRoot(outputRoot);
			this.validateMetaClasses(metaClasses);
			this.validatePackage(packageModel);
			this.validatePackage(packageRepository);
			this.validatePackage(packageService);

			File outputDirectory = this.prepareOutputDirectory(outputRoot, packageService);
			for (MetaClass metaClass : metaClasses.values()) {
				Map<String, Object> data = new HashMap<String, Object>();
				data.put("metaClass", metaClass);
				data.put("packageModel", packageModel);
				data.put("packageRepository", packageRepository);
				data.put("packageService", packageService);
				data.put("metaClasses", metaClasses);
				data.put("jakartaEe", JakartaEeTemplateContextFactory.create());

				File generatedDestination = new File(outputDirectory, metaClass.getName() + "ServiceBase.java");
				this.renderTemplateToFile(SERVICE_TEMPLATE_NAME, data, generatedDestination);
				this.getIo().stampaMessaggio("Generato: " + generatedDestination.getAbsolutePath());

				File wrapperDestination = new File(outputDirectory, metaClass.getName() + "Service.java");
				this.renderTemplateToFileIfAbsent(SERVICE_WRAPPER_TEMPLATE_NAME, data, wrapperDestination);
			}
		} catch (IOException | TemplateException e) {
			throw new RuntimeException("Errore durante la generazione dei service", e);
		}
	}
}
