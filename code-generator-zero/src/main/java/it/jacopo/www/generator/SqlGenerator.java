package it.jacopo.www.generator;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import freemarker.template.TemplateException;
import it.jacopo.www.io.IO;
import it.jacopo.www.model.MetaClass;

public class SqlGenerator extends FileGenerator {

	private static final String SQL_TEMPLATE_NAME = "freemarker/sqlTemplate.ftl";

	public SqlGenerator(IO io) {
		super(io);
	}

	public File generate(Map<String, MetaClass> metaClasses, String outputRoot) {
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
			this.getIo().stampaMessaggio("Generato: " + destination.getAbsolutePath());
			this.getIo().stampaMessaggio("Generazione completata con successo!");
			return destination;
		} catch (IOException | TemplateException e) {
			throw new RuntimeException("Errore durante la generazione dello schema sql", e);
		}
	}
}
