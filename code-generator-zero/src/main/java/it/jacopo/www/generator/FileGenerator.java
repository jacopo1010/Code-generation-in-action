package it.jacopo.www.generator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import it.jacopo.www.io.IO;
import it.jacopo.www.model.MetaClass;

public abstract class FileGenerator  {
  
	private final Configuration conf;
	private final IO io;
	
	public FileGenerator(IO io) {
		this.conf = new Configuration(Configuration.VERSION_2_3_34);
		this.conf.setClassLoaderForTemplateLoading(Thread.currentThread().getContextClassLoader(), "/");
		this.conf.setDefaultEncoding("UTF-8");
		this.io = io;
	}

	protected Configuration getConfiguration() {
		return this.conf;
	}

	protected IO getIo() {
		return this.io;
	}

	protected void validateOutputRoot(String outputRoot) {
		if (outputRoot == null || outputRoot.trim().isEmpty()) {
			throw new IllegalArgumentException("Definire nell'application.properties la cartella di output");
		}
	}

	protected void validateMetaClasses(Map<String, MetaClass> metaClasses) {
		if (metaClasses == null || metaClasses.isEmpty()) {
			throw new IllegalArgumentException("Nessuna meta-classe disponibile per la generazione");
		}
	}

	protected void validatePackage(String packageName) {
		if (packageName == null || packageName.trim().isEmpty()) {
			throw new IllegalArgumentException("Definire nell'application.properties il package di output");
		}
	}

	protected File prepareOutputDirectory(String outputRoot, String packageName) throws IOException {
		File outputDirectory = this.resolveOutputDirectoryInternal(outputRoot, packageName);
		if (!outputDirectory.exists() && !outputDirectory.mkdirs()) {
			throw new IOException("Impossibile creare la cartella di output: " + outputDirectory.getAbsolutePath());
		}
		return outputDirectory;
	}

	protected File resolveOutputDirectoryInternal(String outputRoot, String packageName) {
		File root = new File(outputRoot);
		String packagePath = packageName.replace('.', File.separatorChar);
		String normalizedRootPath = this.normalizePathInternal(root.getPath());
		String normalizedPackagePath = this.normalizePathInternal(packagePath);
		if (normalizedRootPath.endsWith(normalizedPackagePath)) {
			return root;
		}
		return new File(root, packagePath);
	}

	protected String normalizePathInternal(String path) {
		return path.replace('\\', '/').replaceAll("/+", "/").replaceAll("/$", "");
	}

	protected void renderTemplateToFile(String templateName, Map<String, Object> data, File destination)
			throws IOException, TemplateException {
		Template template = this.conf.getTemplate(templateName);
		this.renderTemplateToFile(template, data, destination);
	}

	protected void renderTemplateToFileIfAbsent(String templateName, Map<String, Object> data, File destination)
			throws IOException, TemplateException {
		if (destination.exists()) {
			this.io.stampaMessaggio("Preservato file custom: " + destination.getAbsolutePath());
			return;
		}
		this.renderTemplateToFile(templateName, data, destination);
		this.io.stampaMessaggio("Creato scaffold custom: " + destination.getAbsolutePath());
	}

	protected void renderTemplateToFile(Template template, Map<String, Object> data, File destination)
			throws IOException, TemplateException {
		try (Writer writer = new OutputStreamWriter(new FileOutputStream(destination), StandardCharsets.UTF_8)) {
			template.process(data, writer);
		}
	}
}
