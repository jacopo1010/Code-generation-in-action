package it.jacopo.www.generator;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.DriverManager;
import java.util.Properties;

import org.flywaydb.core.Flyway;
import org.jooq.codegen.GenerationTool;
import org.jooq.meta.jaxb.Database;
import org.jooq.meta.jaxb.Generate;
import org.jooq.meta.jaxb.Generator;
import org.jooq.meta.jaxb.Jdbc;
import org.jooq.meta.jaxb.Target;
import org.jooq.meta.jaxb.Configuration;
import it.jacopo.www.io.IO;
import it.jacopo.www.loader.CaricatoreDiFile;
import it.jacopo.www.loader.CaricatoreDiFileImpl;
import it.jacopo.www.loader.PropertiesCostanti;

public class JooqGenerator {

	private IO io;
	private Properties properties;

	public JooqGenerator(IO io) {
		super();
		this.io = io;
		this.properties = new Properties();
	}


	/* metodo che serve per fare generare queste componenti a jooq 
	 * WORKFLOW: MI SERVONO DRIVE USERNAME PASSWORD DEL DB DA PRENDERE DAL FILE PROPERTIES
	 * PRIMA DI INTERROGARE LO SCHEMA ESSO DEVE ESSERE PRESENTE NEL DB QUINDI USERO' FLYWAY
	 * PER LA MIGRATION e poi GENERO IL TUTTO CON JOOQ*/

	public void generateDtoAndDao(File file,Properties target) {
		if (file == null || !file.exists() || !file.isFile()) {
			throw new IllegalArgumentException("Il file sorgente non esiste o non è valido");
		}
		try(InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("application.properties")){
			this.properties.load(inputStream);
			io.stampaMessaggio("caricato il properties del generatore!");
			String url = this.properties.getProperty("generator.jooq.db.url").trim();
			if(url == null || url.isEmpty()) throw new IllegalArgumentException("Definire nell'application.properties la url di connessione con il database");
			String username = this.properties.getProperty("generator.jooq.db.username").trim();
			if(username == null || username.isEmpty()) throw new IllegalArgumentException("Definire nell'application.properties lo username del database");
			String password = this.properties.getProperty("generator.jooq.db.password").trim();
			if(password == null || password.isEmpty()) throw new IllegalArgumentException("Definire nell'application.properties la password del database");
			String schema = this.properties.getProperty("generator.jooq.db.schema").trim();
			if(schema == null || schema.isEmpty()) throw new IllegalArgumentException("Definire nell'application.properties lo schema del database");

			/* configuro i file e le directory */
			File directory = new File("target/flyway-migration");
			if(!directory.isDirectory()) {
				directory.mkdirs();
			}
			File migrazioneFile = new File(directory,"V1__init.sql");
			Files.copy(file.toPath(), migrazioneFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
			Flyway flyway = Flyway.configure()
					.dataSource(url, username, password)
					.locations("filesystem:" + directory.getAbsolutePath())
					.cleanDisabled(false)
					.load();
			io.stampaMessaggio("FlyWay configurato correttemente!");
			flyway.clean();
			flyway.migrate();
			io.stampaMessaggio("Flyway migrate eseguita correttamente!");
			this.initJooq(target, url, username, password, schema);
		} catch (IOException e) {
			throw new RuntimeException("Errore durante migrate + code generation", e);
		}
	}

	private void initJooq(Properties target, String url, String username, String password, String schema) {
		String projectPath = target.getProperty(PropertiesCostanti.JAVA_OUTPUT_PATH);
		String daoPackage = target.getProperty(PropertiesCostanti.DAO_OUTPUT_PACKAGE);
		String driver = this.properties.getProperty("generator.jooq.db.driver").trim();
		
		if(projectPath == null || projectPath.isEmpty()) {
			throw new IllegalArgumentException("Definire nel properties target il path di output java");
		}
		
		if(daoPackage == null || daoPackage.isEmpty()) {
			throw new IllegalArgumentException("Definire nel properties target il package dao");
		}
		
		if(driver == null || driver.isEmpty()) {
			throw new IllegalArgumentException("Definire nell'application.properties del generatore il driver del database");
		}
		
		try {
			Configuration configuration = new Configuration()
					.withJdbc(new Jdbc()
							.withDriver(driver)
							.withUrl(url)
							.withUser(username)
							.withPassword(password))
					.withGenerator(new Generator()
							.withDatabase(new Database()
									.withName("org.jooq.meta.postgres.PostgresDatabase")
									.withInputSchema(schema)
									.withIncludes(".*")
									.withExcludes("flyway_schema_history"))
							.withGenerate(new Generate()
									.withPojos(true)
									.withDaos(true))
							.withTarget(new Target()
									.withPackageName(daoPackage)
									.withDirectory(projectPath)));

			GenerationTool.generate(configuration);
			io.stampaMessaggio("Generazione jOOQ completata!");
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}




}
