package it.jacopo.www.generator;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import freemarker.template.TemplateException;
import it.jacopo.www.io.IO;
import it.jacopo.www.model.MetaClass;

public class RepositoryGenerator extends FileGenerator {

	private static final String REPOSITORY_TEMPLATE_NAME = "freemarker/repositoryTemplate.ftl";
	private static final String SIMPLE_REPOSITORY_TEMPLATE_NAME = "freemarker/simpleRepositoryTemplate.ftl";
	private static final String SIMPLE_REPOSITORY_IMPL_TEMPLATE_NAME = "freemarker/simpleRepositoryImplTemplate.ftl";

	public RepositoryGenerator(IO io) {
		super(io);
	}

	public void generate(String packageModel, String packageRepository,
			Map<String, MetaClass> metaClasses, String outputRoot) {
		try {
			this.validateOutputRoot(outputRoot);
			this.validateMetaClasses(metaClasses);
			this.validatePackage(packageModel);
			this.validatePackage(packageRepository);

			this.generateSimpleRepositoryInfrastructure(packageRepository, outputRoot);

			File outputDirectory = this.prepareOutputDirectory(outputRoot, packageRepository);
			for (MetaClass metaClass : metaClasses.values()) {
				Map<String, Object> data = new HashMap<String, Object>();
				data.put("metaClass", metaClass);
				data.put("packageModel", packageModel);
				data.put("packageRepository", packageRepository);
				data.put("metaClasses", metaClasses);
				data.put("jakartaEe", JakartaEeTemplateContextFactory.create());

				File repositoryDestination = new File(outputDirectory, metaClass.getName() + "Repository.java");
				this.renderTemplateToFileIfAbsent(REPOSITORY_TEMPLATE_NAME, data, repositoryDestination);
			}
		} catch (IOException | TemplateException e) {
			throw new RuntimeException("Errore generazione Repository", e);
		}
	}

	private void generateSimpleRepositoryInfrastructure(String packageRepository, String outputRoot)
			throws IOException, TemplateException {
		File outputDirectory = this.prepareOutputDirectory(outputRoot, packageRepository);
		Map<String, Object> data = new HashMap<String, Object>();
		data.put("packageRepository", packageRepository);
		data.put("jakartaEe", JakartaEeTemplateContextFactory.create());

		File interfaceDestination = new File(outputDirectory, "SimpleRepository.java");
		this.renderTemplateToFileIfAbsent(SIMPLE_REPOSITORY_TEMPLATE_NAME, data, interfaceDestination);

		File implementationDestination = new File(outputDirectory, "SimpleRepositoryImpl.java");
		this.renderTemplateToFileIfAbsent(SIMPLE_REPOSITORY_IMPL_TEMPLATE_NAME, data, implementationDestination);
	}
}
