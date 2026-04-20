package it.jacopo.www.generator;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.Properties;

import org.jooq.codegen.GenerationTool;
import org.jooq.meta.jaxb.Database;
import org.jooq.meta.jaxb.Generate;
import org.jooq.meta.jaxb.Generator;
import org.jooq.meta.jaxb.Property;
import org.jooq.meta.jaxb.Target;
import org.jooq.meta.jaxb.Configuration;
import it.jacopo.www.FileUtil;
import it.jacopo.www.io.IO;
import it.jacopo.www.loader.PropertiesCostanti;

public class JooqGenerator {

	private IO io;

	public JooqGenerator(IO io) {
		super();
		this.io = io;
	}
	public void generateJooqArtifacts(File sqlFile, Properties target, String applicationPropertiesPath) {
		String jooqPackage = target.getProperty(PropertiesCostanti.JOOQ_OUTPUT_PACKAGE);
		String projectPath = FileUtil.resolveJavaOutputPath(
				applicationPropertiesPath,
				target.getProperty(PropertiesCostanti.JAVA_OUTPUT_PATH));
		if (projectPath == null || projectPath.isEmpty())
			throw new IllegalArgumentException("Definire il path di output java nel properties");
		if (jooqPackage == null || jooqPackage.isEmpty())
			throw new IllegalArgumentException("Definire il package jooq nel properties");
		try {
			this.cleanupLegacyJooqOutput(projectPath, jooqPackage);
			Configuration configuration = new Configuration()
					.withGenerator(new Generator()
							.withDatabase(new Database()
									.withName("org.jooq.meta.extensions.ddl.DDLDatabase")
							.withProperties(
									new Property().withKey("scripts").withValue(sqlFile.getAbsolutePath()),
									new Property().withKey("sort").withValue("semantic")
									))
							.withGenerate(new Generate()
									.withPojos(false)
									.withJavaTimeTypes(true)
									.withDaos(false))
							.withTarget(new Target()
									.withPackageName(jooqPackage)
									.withDirectory(projectPath)));

			GenerationTool.generate(configuration);
			io.stampaMessaggio("Generazione jOOQ completata!");
		} catch (Exception e) {
			throw new RuntimeException("Errore jOOQ codegen", e);
		}
	}

	private void cleanupLegacyJooqOutput(String outputRoot, String jooqPackage) throws IOException {
		if (!jooqPackage.endsWith(".jooq")) {
			return;
		}
		String legacyPackage = jooqPackage.substring(0, jooqPackage.length() - ".jooq".length()) + ".dao";
		Path rootPath = Paths.get(outputRoot).toAbsolutePath().normalize();
		Path currentPackagePath = rootPath.resolve(jooqPackage.replace('.', File.separatorChar)).normalize();
		Path legacyPackagePath = rootPath.resolve(legacyPackage.replace('.', File.separatorChar)).normalize();
		if (legacyPackagePath.equals(currentPackagePath) || !legacyPackagePath.startsWith(rootPath)) {
			return;
		}
		if (!Files.exists(legacyPackagePath)) {
			return;
		}
		Files.walk(legacyPackagePath)
				.sorted(Comparator.reverseOrder())
				.forEach(path -> {
					try {
						Files.deleteIfExists(path);
					} catch (IOException exception) {
						throw new RuntimeException("Impossibile eliminare il package jOOQ legacy: " + legacyPackagePath,
								exception);
					}
				});
		this.io.stampaMessaggio("Rimosso output jOOQ legacy: " + legacyPackagePath);
	}



}
