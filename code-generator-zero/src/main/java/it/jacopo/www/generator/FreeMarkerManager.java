package it.jacopo.www.generator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import it.jacopo.www.io.IO;
import it.jacopo.www.model.MetaClass;

public class FreeMarkerManager {

	private static final String MODEL_TEMPLATE_NAME = "freemarker/modelTemplate.ftl";
	private static final String MODEL_WRAPPER_TEMPLATE_NAME = "freemarker/modelWrapperTemplate.ftl";
	private static final String SQL_TEMPLATE_NAME = "freemarker/sqlTemplate.ftl";
	private static final String SERVICE_TEMPLATE_NAME = "freemarker/serviceTemplate.ftl";
	private static final String SERVICE_WRAPPER_TEMPLATE_NAME = "freemarker/serviceWrapperTemplate.ftl";
	private static final String CONTROLLER_TEMPLATE_NAME = "freemarker/controllerTemplate.ftl";
	private static final String CONTROLLER_WRAPPER_TEMPLATE_NAME = "freemarker/controllerWrapperTemplate.ftl";
	private static final String REPOSITORY_TEMPLATE_NAME = "freemarker/repositoryTemplate.ftl";
	private static final String SIMPLE_REPOSITORY_TEMPLATE_NAME = "freemarker/simpleRepositoryTemplate.ftl";
	private static final String SIMPLE_REPOSITORY_IMPL_TEMPLATE_NAME = "freemarker/simpleRepositoryImplTemplate.ftl";

	private Configuration conf;
	private IO io;

	public FreeMarkerManager(IO io) {
		this.conf = new Configuration(Configuration.VERSION_2_3_34);
		this.conf.setClassLoaderForTemplateLoading(Thread.currentThread().getContextClassLoader(), "/");
		this.conf.setDefaultEncoding("UTF-8");
		this.io = io;
	}

	public void generateModel(String packageModel, Map<String, MetaClass> metaClasses, String outputRoot) {
		try {
			this.validateOutputRoot(outputRoot);
			this.validateMetaClasses(metaClasses);

			File outputDirectory = this.prepareOutputDirectory(outputRoot, packageModel);
			for (MetaClass metaClass : metaClasses.values()) {
				Map<String, Object> data = new HashMap<String, Object>();
				data.put("metaClass", metaClass);
				data.put("packageName", packageModel);

				File generatedDestination = new File(outputDirectory, metaClass.getName() + "Base.java");
				this.renderTemplateToFile(MODEL_TEMPLATE_NAME, data, generatedDestination);
				this.io.stampaMessaggio("Generato: " + generatedDestination.getAbsolutePath());

				File wrapperDestination = new File(outputDirectory, metaClass.getName() + ".java");
				this.renderTemplateToFileIfAbsent(MODEL_WRAPPER_TEMPLATE_NAME, data, wrapperDestination);
			}
			this.io.stampaMessaggio("Generazione completata con successo!");
		} catch (IOException | TemplateException e) {
			throw new RuntimeException("Errore durante la generazione dei model", e);
		}
	}

	public File generateSchema(Map<String, MetaClass> metaClasses, String outputRoot) {
		try {
			this.validateOutputRoot(outputRoot);
			this.validateMetaClasses(metaClasses);

			Path outputPath = Paths.get(outputRoot);
			if (Files.isDirectory(outputPath)) {
				throw new RuntimeException("Il path configurato punta a una directory: " + outputPath);
			}
			if (outputPath.getParent() != null) {
				Files.createDirectories(outputPath.getParent());
			}
			String fileName = outputPath.getFileName().toString();
			if (!fileName.toLowerCase().endsWith(".sql")) {
				throw new RuntimeException("Il file configurato non è un formato sql valido: " + outputPath);
			}

			Map<String, Object> data = new HashMap<String, Object>();
			data.put("metaClasses", metaClasses);

			File destination = outputPath.toFile();
			this.renderTemplateToFile(SQL_TEMPLATE_NAME, data, destination);
			this.io.stampaMessaggio("Generato: " + destination.getAbsolutePath());
			this.io.stampaMessaggio("Generazione completata con successo!");
			return destination;
		} catch (IOException | TemplateException e) {
			throw new RuntimeException("Errore durante la generazione dello schema sql", e);
		}
	}

	public void generateRepository(String packageModel, String jooqPackage, String packageRepository,
	        Map<String, MetaClass> metaClasses, String outputRoot) {
	    try {
	        this.validateOutputRoot(outputRoot);
	        this.validateMetaClasses(metaClasses);
	        this.validatePackage(packageModel);
	        this.validatePackage(jooqPackage);
	        this.validatePackage(packageRepository);

	        this.generateSimpleRepositoryInfrastructure(packageRepository, outputRoot);

	        File outputDirectory = this.prepareOutputDirectory(outputRoot, packageRepository);
	        for (MetaClass metaClass : metaClasses.values()) {
	            Map<String, Object> data = new HashMap<>();
	            data.put("metaClass", metaClass);
	            data.put("packageModel", packageModel);
	            data.put("jooqPackage", jooqPackage);
	            data.put("packageRepository", packageRepository);
	            data.put("packageRepository", packageRepository);
	            data.put("metaClasses", metaClasses);

	            File repositoryDestination = new File(outputDirectory,
	                    metaClass.getName() + "Repository.java");
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

		File interfaceDestination = new File(outputDirectory, "SimpleRepository.java");
		this.renderTemplateToFileIfAbsent(SIMPLE_REPOSITORY_TEMPLATE_NAME, data, interfaceDestination);

		File implementationDestination = new File(outputDirectory, "SimpleRepositoryImpl.java");
		this.renderTemplateToFileIfAbsent(SIMPLE_REPOSITORY_IMPL_TEMPLATE_NAME, data, implementationDestination);
	}
	
	public void generateService(String packageModel, String packageRepository, String packageService, Map<String, MetaClass> metaClasses, String outputRoot) {
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

				File generatedDestination = new File(outputDirectory, metaClass.getName() + "ServiceBase.java");
				this.renderTemplateToFile(SERVICE_TEMPLATE_NAME, data, generatedDestination);
				this.io.stampaMessaggio("Generato: " + generatedDestination.getAbsolutePath());

				File wrapperDestination = new File(outputDirectory, metaClass.getName() + "Service.java");
				this.renderTemplateToFileIfAbsent(SERVICE_WRAPPER_TEMPLATE_NAME, data, wrapperDestination);

			}
		} catch (IOException | TemplateException e) {
			throw new RuntimeException("Errore durante la generazione dei service", e);
		}
	}

	public void generateController(String packageModel, String packageService, String packageController,
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
				this.io.stampaMessaggio("Generato: " + generatedDestination.getAbsolutePath());

				File wrapperDestination = new File(outputDirectory, metaClass.getName() + "Controller.java");
				this.renderTemplateToFileIfAbsent(CONTROLLER_WRAPPER_TEMPLATE_NAME, data, wrapperDestination);

			}
		} catch (IOException | TemplateException e) {
			throw new RuntimeException("Errore durante la generazione dei controller", e);
		}
	}

	private void validateOutputRoot(String outputRoot) {
		if (outputRoot == null || outputRoot.trim().isEmpty()) {
			throw new IllegalArgumentException("Definire nell'application.properties la cartella di output");
		}
	}

	private void validateMetaClasses(Map<String, MetaClass> metaClasses) {
		if (metaClasses == null || metaClasses.isEmpty()) {
			throw new IllegalArgumentException("Nessuna meta-classe disponibile per la generazione");
		}
	}

	private void validatePackage(String packageName) {
		if (packageName == null || packageName.trim().isEmpty()) {
			throw new IllegalArgumentException("Definire nell'application.properties il package di output");
		}
	}

	private File prepareOutputDirectory(String outputRoot, String packageName) throws IOException {
		File outputDirectory = this.resolveOutputDirectory(outputRoot, packageName);
		if (!outputDirectory.exists() && !outputDirectory.mkdirs()) {
			throw new IOException("Impossibile creare la cartella di output: " + outputDirectory.getAbsolutePath());
		}
		return outputDirectory;
	}

	private File resolveOutputDirectory(String outputRoot, String packageName) {
		File root = new File(outputRoot);
		String packagePath = packageName.replace('.', File.separatorChar);
		String normalizedRootPath = this.normalizePath(root.getPath());
		String normalizedPackagePath = this.normalizePath(packagePath);
		if (normalizedRootPath.endsWith(normalizedPackagePath)) {
			return root;
		}
		return new File(root, packagePath);
	}

	private String normalizePath(String path) {
		return path.replace('\\', '/').replaceAll("/+", "/").replaceAll("/$", "");
	}

	private void renderTemplateToFile(String templateName, Map<String, Object> data, File destination)
			throws IOException, TemplateException {
		Template template = this.conf.getTemplate(templateName);
		this.renderTemplateToFile(template, data, destination);
	}

	private void renderTemplateToFileIfAbsent(String templateName, Map<String, Object> data, File destination)
			throws IOException, TemplateException {
		if (destination.exists()) {
			this.io.stampaMessaggio("Preservato file custom: " + destination.getAbsolutePath());
			return;
		}
		this.renderTemplateToFile(templateName, data, destination);
		this.io.stampaMessaggio("Creato scaffold custom: " + destination.getAbsolutePath());
	}

	private void renderTemplateToFile(Template template, Map<String, Object> data, File destination)
			throws IOException, TemplateException {
		try (Writer writer = new OutputStreamWriter(new FileOutputStream(destination), StandardCharsets.UTF_8)) {
			template.process(data, writer);
		}
	}
}
