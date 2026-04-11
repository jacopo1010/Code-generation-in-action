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
	private Configuration conf;
	private IO io;

	public FreeMarkerManager(IO io) {
		this.conf = new Configuration(Configuration.VERSION_2_3_34);
		this.conf.setClassLoaderForTemplateLoading(Thread.currentThread().getContextClassLoader(), "/");
		this.conf.setDefaultEncoding("UTF-8");
		this.io = io;
	}

	public void generateModel(String packageModel,Map<String, MetaClass> metaClasses, String outputRoot) {
		try {
			if (outputRoot == null || outputRoot.trim().isEmpty()) {
				throw new IllegalArgumentException("Definire nell'application.properties la cartella di output");
			}
			if (metaClasses == null || metaClasses.isEmpty()) {
				throw new IllegalArgumentException("Nessuna meta-classe disponibile per la generazione");
			}

			File outputDirectory = this.resolveOutputDirectory(outputRoot, packageModel);
			if (!outputDirectory.exists() && !outputDirectory.mkdirs()) {
				throw new IOException("Impossibile creare la cartella di output: " + outputDirectory.getAbsolutePath());
			}
			Template template = conf.getTemplate(MODEL_TEMPLATE_NAME);
			for (MetaClass metaClass : metaClasses.values()) {
				String nomeFile = metaClass.getName() + ".java";
				File fileDestinazione = new File(outputDirectory, nomeFile);
				Map<String, Object> dati = new HashMap<String, Object>();
				dati.put("metaClass", metaClass);
                dati.put("packageName", packageModel);
				
				try (Writer writer = new OutputStreamWriter(new FileOutputStream(fileDestinazione), StandardCharsets.UTF_8)) {
					template.process(dati, writer);
				}
				io.stampaMessaggio("Generato: " + fileDestinazione.getAbsolutePath());
			}
			io.stampaMessaggio("Generazione completata con successo!");
		} catch (IOException | TemplateException e) {
			throw new RuntimeException("Errore durante la generazione dei model", e);
		}
	}

	private File resolveOutputDirectory(String outputRoot, String packageModel) {
		File root = new File(outputRoot);
		String packagePath = packageModel.replace('.', File.separatorChar);
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
	
	public void generateSchema(Map<String, MetaClass> metaClasses, String outputRoot) {
		try {
			if (outputRoot == null || outputRoot.trim().isEmpty()) {
				throw new IllegalArgumentException("Definire nell'application.properties la cartella di output");
			}
			if (metaClasses == null || metaClasses.isEmpty()) {
				throw new IllegalArgumentException("Nessuna meta-classe disponibile per la generazione");
			}
			
			Path p = Paths.get(outputRoot);
			if (Files.isDirectory(p)) {
				throw new RuntimeException("Il path configurato punta a una directory: " + p);
			}
			if (p.getParent() != null) {
				Files.createDirectories(p.getParent());
			}
			String nomeFile = p.getFileName().toString();
			if(!nomeFile.toLowerCase().endsWith(".sql")) {
				throw new RuntimeException("Il file configurato non è un formato sql valido: " + p);
			}

			Template template = conf.getTemplate(SQL_TEMPLATE_NAME);
			Map<String, Object> dati = new HashMap<String, Object>();
			dati.put("metaClasses", metaClasses);

			File fileDestinazione = p.toFile();
			try (Writer writer = new OutputStreamWriter(new FileOutputStream(fileDestinazione), StandardCharsets.UTF_8)) {
				template.process(dati, writer);
			}
			io.stampaMessaggio("Generato: " + fileDestinazione.getAbsolutePath());
			
			io.stampaMessaggio("Generazione completata con successo!");
		}catch (IOException | TemplateException e) {
			throw new RuntimeException("Errore durante la generazione dello schema sql", e);
		}
	}
  
}
