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
	private static final String SQL_TEMPLATE_NAME = "freemarker/sqlTemplate.ftl";
	private static final String GENERIC_DAO_TEMPLATE_NAME = "freemarker/genericDaoTemplate.ftl";
	private static final String GENERIC_DAO_IMPL_TEMPLATE_NAME = "freemarker/genericDaoImplTemplate.ftl";
	private static final String DAO_TEMPLATE_NAME = "freemarker/daoTemplate.ftl";
	private static final String SERVICE_TEMPLATE_NAME = "freemarker/serviceTemplate.ftl";
	private static final String CONTROLLER_TEMPLATE_NAME = "freemarker/controllerTemplate.ftl";
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

				File destination = new File(outputDirectory, metaClass.getName() + ".java");
				this.renderTemplateToFile(MODEL_TEMPLATE_NAME, data, destination);
				this.io.stampaMessaggio("Generato: " + destination.getAbsolutePath());
			}
			this.io.stampaMessaggio("Generazione completata con successo!");
		} catch (IOException | TemplateException e) {
			throw new RuntimeException("Errore durante la generazione dei model", e);
		}
	}

	public void generateSchema(Map<String, MetaClass> metaClasses, String outputRoot) {
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
		} catch (IOException | TemplateException e) {
			throw new RuntimeException("Errore durante la generazione dello schema sql", e);
		}
	}

	public void generateDao(String packageModel, String packageDao, Map<String, MetaClass> metaClasses, String outputRoot) {
		try {
			this.validateOutputRoot(outputRoot);
			this.validateMetaClasses(metaClasses);
			this.validatePackage(packageDao);

			File outputDirectory = this.prepareOutputDirectory(outputRoot, packageDao);
			Map<String, Object> baseData = new HashMap<String, Object>();
			baseData.put("packageDao", packageDao);

			File genericDaoDestination = new File(outputDirectory, "GenericDao.java");
			this.renderTemplateToFile(GENERIC_DAO_TEMPLATE_NAME, baseData, genericDaoDestination);
			this.io.stampaMessaggio("Generato: " + genericDaoDestination.getAbsolutePath());

			File genericDaoImplDestination = new File(outputDirectory, "GenericDaoImpl.java");
			this.renderTemplateToFile(GENERIC_DAO_IMPL_TEMPLATE_NAME, baseData, genericDaoImplDestination);
			this.io.stampaMessaggio("Generato: " + genericDaoImplDestination.getAbsolutePath());

			for (MetaClass metaClass : metaClasses.values()) {
				Map<String, Object> data = new HashMap<String, Object>();
				data.put("metaClass", metaClass);
				data.put("packageDao", packageDao);
				data.put("modelPackage", packageModel);
				data.put("metaClasses", metaClasses);

				File destination = new File(outputDirectory, metaClass.getName() + "Dao.java");
				this.renderTemplateToFile(DAO_TEMPLATE_NAME, data, destination);
				this.io.stampaMessaggio("Generato: " + destination.getAbsolutePath());
			}
		} catch (IOException | TemplateException e) {
			throw new RuntimeException("Errore durante la generazione dei dao", e);
		}
	}

	public void generateService(String packageModel, String packageDao, String packageService, Map<String, MetaClass> metaClasses, String outputRoot) {
		try {
			this.validateOutputRoot(outputRoot);
			this.validateMetaClasses(metaClasses);
			this.validatePackage(packageModel);
			this.validatePackage(packageDao);
			this.validatePackage(packageService);

			File outputDirectory = this.prepareOutputDirectory(outputRoot, packageService);
			for (MetaClass metaClass : metaClasses.values()) {
				Map<String, Object> data = new HashMap<String, Object>();
				data.put("metaClass", metaClass);
				data.put("packageModel", packageModel);
				data.put("packageDao", packageDao);
				data.put("packageService", packageService);
				data.put("metaClasses", metaClasses);

				File destination = new File(outputDirectory, metaClass.getName() + "Service.java");
				this.renderTemplateToFile(SERVICE_TEMPLATE_NAME, data, destination);
				this.io.stampaMessaggio("Generato: " + destination.getAbsolutePath());
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

				File destination = new File(outputDirectory, metaClass.getName() + "Controller.java");
				this.renderTemplateToFile(CONTROLLER_TEMPLATE_NAME, data, destination);
				this.io.stampaMessaggio("Generato: " + destination.getAbsolutePath());
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

	private void renderTemplateToFile(Template template, Map<String, Object> data, File destination)
			throws IOException, TemplateException {
		try (Writer writer = new OutputStreamWriter(new FileOutputStream(destination), StandardCharsets.UTF_8)) {
			template.process(data, writer);
		}
	}
}
