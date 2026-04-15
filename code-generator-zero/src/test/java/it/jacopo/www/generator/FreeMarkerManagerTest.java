package it.jacopo.www.generator;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;

import it.jacopo.www.io.IO;
import it.jacopo.www.model.MetaClass;
import it.jacopo.www.model.MetaField;
import junit.framework.TestCase;

public class FreeMarkerManagerTest extends TestCase {

	public void testNonDuplicaIlPackageSeOutputGiaAllineato() throws Exception {
		FreeMarkerManager manager = new FreeMarkerManager(new IOFittizio());
		Method method = FreeMarkerManager.class.getDeclaredMethod(
				"resolveOutputDirectory", String.class, String.class);
		method.setAccessible(true);

		File resolved = (File) method.invoke(
				manager,
				"C:\\temp\\generated\\jacopo\\with\\develop\\model",
				"jacopo.with.develop.model");

		assertEquals(
				new File("C:\\temp\\generated\\jacopo\\with\\develop\\model").getPath(),
				resolved.getPath());
	}

	public void testAggiungeIlPackageSeOutputEPuntatoAllaRoot() throws Exception {
		FreeMarkerManager manager = new FreeMarkerManager(new IOFittizio());
		Method method = FreeMarkerManager.class.getDeclaredMethod(
				"resolveOutputDirectory", String.class, String.class);
		method.setAccessible(true);

		File resolved = (File) method.invoke(
				manager,
				"C:\\temp\\generated",
				"jacopo.with.develop.model");

		assertEquals(
				new File("C:\\temp\\generated\\jacopo\\with\\develop\\model").getPath(),
				resolved.getPath());
	}

	public void testGenerateDaoUsaPackageDaoEImportModelCorretti() throws Exception {
		File tempDirectory = Files.createTempDirectory("dao-template-test").toFile();
		try {
			FreeMarkerManager manager = new FreeMarkerManager(new IOFittizio());
			MetaClass metaClass = new MetaClass();
			metaClass.setName("Cliente");
			metaClass.setTable("cliente");

			MetaField idField = new MetaField();
			idField.setName("id");
			idField.setJavaType("Long");
			metaClass.addField(idField);

			Map<String, MetaClass> metaClasses = new LinkedHashMap<String, MetaClass>();
			metaClasses.put(metaClass.getName(), metaClass);

			manager.generateDao(
					"it.test.model",
					"it.test.dao",
					metaClasses,
					tempDirectory.getAbsolutePath());

			File genericDaoFile = new File(tempDirectory, "it\\test\\dao\\GenericDao.java");
			assertTrue("GenericDao non generato nel path atteso", genericDaoFile.isFile());

			File genericDaoImplFile = new File(tempDirectory, "it\\test\\dao\\GenericDaoImpl.java");
			assertTrue("GenericDaoImpl non generato nel path atteso", genericDaoImplFile.isFile());

			File generatedFile = new File(tempDirectory, "it\\test\\dao\\ClienteDao.java");
			assertTrue("DAO non generato nel path atteso", generatedFile.isFile());

			String genericDaoContent = new String(Files.readAllBytes(genericDaoFile.toPath()), StandardCharsets.UTF_8);
			assertTrue(genericDaoContent.contains("package it.test.dao;"));
			assertTrue(genericDaoContent.contains("public interface GenericDao<T, ID>"));

			String genericDaoImplContent = new String(Files.readAllBytes(genericDaoImplFile.toPath()), StandardCharsets.UTF_8);
			assertTrue(genericDaoImplContent.contains("package it.test.dao;"));
			assertTrue(genericDaoImplContent.contains("public abstract class GenericDaoImpl<T, ID> implements GenericDao<T, ID>"));

			String generatedContent = new String(Files.readAllBytes(generatedFile.toPath()), StandardCharsets.UTF_8);
			assertTrue(generatedContent.contains("package it.test.dao;"));
			assertTrue(generatedContent.contains("import it.test.model.Cliente;"));
			assertTrue(generatedContent.contains("extends GenericDaoImpl<Cliente, Long>"));
		} finally {
			this.deleteRecursively(tempDirectory);
		}
	}

	public void testGenerateDaoCreaFinderPerRelazioneManyToOne() throws Exception {
		File tempDirectory = Files.createTempDirectory("dao-many-to-one-test").toFile();
		try {
			FreeMarkerManager manager = new FreeMarkerManager(new IOFittizio());
			MetaClass studenteMetaClass = new MetaClass();
			studenteMetaClass.setName("Studente");

			MetaField studenteIdField = new MetaField();
			studenteIdField.setName("id");
			studenteIdField.setJavaType("Long");
			studenteMetaClass.addField(studenteIdField);

			MetaClass corsoMetaClass = new MetaClass();
			corsoMetaClass.setName("Corso");
			corsoMetaClass.setTable("corso");

			MetaField corsoIdField = new MetaField();
			corsoIdField.setName("id");
			corsoIdField.setJavaType("Long");
			corsoMetaClass.addField(corsoIdField);

			MetaField relazioneStudente = new MetaField();
			relazioneStudente.setName("studente");
			relazioneStudente.setJavaType("Studente");
			relazioneStudente.setRelation(true);
			relazioneStudente.setRelationType("MANY_TO_ONE");
			relazioneStudente.setForeignKeyColumn("studente_id");
			corsoMetaClass.addField(relazioneStudente);

			Map<String, MetaClass> metaClasses = new LinkedHashMap<String, MetaClass>();
			metaClasses.put(studenteMetaClass.getName(), studenteMetaClass);
			metaClasses.put(corsoMetaClass.getName(), corsoMetaClass);

			manager.generateDao(
					"it.test.model",
					"it.test.dao",
					metaClasses,
					tempDirectory.getAbsolutePath());

			File generatedFile = new File(tempDirectory, "it\\test\\dao\\CorsoDao.java");
			assertTrue("DAO non generato nel path atteso", generatedFile.isFile());

			String generatedContent = new String(Files.readAllBytes(generatedFile.toPath()), StandardCharsets.UTF_8);
			assertTrue(generatedContent.contains("public List<Corso> findByStudenteId(Long studenteId) throws SQLException"));
			assertTrue(generatedContent.contains("String sql = \"SELECT * FROM corso WHERE studente_id = ?\";"));
			assertTrue(generatedContent.contains("statement.setLong(1, studenteId);"));
		} finally {
			this.deleteRecursively(tempDirectory);
		}
	}

	public void testGenerateServiceConCostruttoreVuotoEInizializzazioneDao() throws Exception {
		File tempDirectory = Files.createTempDirectory("service-template-test").toFile();
		try {
			FreeMarkerManager manager = new FreeMarkerManager(new IOFittizio());
			MetaClass metaClass = new MetaClass();
			metaClass.setName("Cliente");

			MetaField idField = new MetaField();
			idField.setName("id");
			idField.setJavaType("Long");
			metaClass.addField(idField);

			MetaField nomeField = new MetaField();
			nomeField.setName("nome");
			nomeField.setJavaType("String");
			metaClass.addField(nomeField);

			Map<String, MetaClass> metaClasses = new LinkedHashMap<String, MetaClass>();
			metaClasses.put(metaClass.getName(), metaClass);

			manager.generateService(
					"it.test.model",
					"it.test.dao",
					"it.test.service",
					metaClasses,
					tempDirectory.getAbsolutePath());

			File generatedFile = new File(tempDirectory, "it\\test\\service\\ClienteService.java");
			assertTrue("Service non generato nel path atteso", generatedFile.isFile());

			String generatedContent = new String(Files.readAllBytes(generatedFile.toPath()), StandardCharsets.UTF_8);
			assertTrue(generatedContent.contains("package it.test.service;"));
			assertTrue(generatedContent.contains("import it.test.model.Cliente;"));
			assertTrue(generatedContent.contains("public ClienteService()"));
			assertTrue(generatedContent.contains("this.dataSource = new HikariDataSource(hikariConfig);"));
			assertTrue(generatedContent.contains("this.dao = new ClienteDao(this.dataSource);"));
			assertTrue(generatedContent.contains("public List<Cliente> findByKeyword(String keyword) throws SQLException"));
			assertTrue(generatedContent.contains("return this.getDao().searchByKeyword(keyword);"));
		} finally {
			this.deleteRecursively(tempDirectory);
		}
	}

	public void testGenerateServiceCreaPassThroughPerRelazioneManyToOne() throws Exception {
		File tempDirectory = Files.createTempDirectory("service-many-to-one-test").toFile();
		try {
			FreeMarkerManager manager = new FreeMarkerManager(new IOFittizio());
			MetaClass studenteMetaClass = new MetaClass();
			studenteMetaClass.setName("Studente");

			MetaField studenteIdField = new MetaField();
			studenteIdField.setName("id");
			studenteIdField.setJavaType("Long");
			studenteMetaClass.addField(studenteIdField);

			MetaClass corsoMetaClass = new MetaClass();
			corsoMetaClass.setName("Corso");

			MetaField corsoIdField = new MetaField();
			corsoIdField.setName("id");
			corsoIdField.setJavaType("Long");
			corsoMetaClass.addField(corsoIdField);

			MetaField relazioneStudente = new MetaField();
			relazioneStudente.setName("studente");
			relazioneStudente.setJavaType("Studente");
			relazioneStudente.setRelation(true);
			relazioneStudente.setRelationType("MANY_TO_ONE");
			relazioneStudente.setForeignKeyColumn("studente_id");
			corsoMetaClass.addField(relazioneStudente);

			Map<String, MetaClass> metaClasses = new LinkedHashMap<String, MetaClass>();
			metaClasses.put(studenteMetaClass.getName(), studenteMetaClass);
			metaClasses.put(corsoMetaClass.getName(), corsoMetaClass);

			manager.generateService(
					"it.test.model",
					"it.test.dao",
					"it.test.service",
					metaClasses,
					tempDirectory.getAbsolutePath());

			File generatedFile = new File(tempDirectory, "it\\test\\service\\CorsoService.java");
			assertTrue("Service non generato nel path atteso", generatedFile.isFile());

			String generatedContent = new String(Files.readAllBytes(generatedFile.toPath()), StandardCharsets.UTF_8);
			assertTrue(generatedContent.contains("public List<Corso> findByStudenteId(Long studenteId) throws SQLException"));
			assertTrue(generatedContent.contains("return this.getDao().findByStudenteId(studenteId);"));
		} finally {
			this.deleteRecursively(tempDirectory);
		}
	}

	private void deleteRecursively(File file) {
		if (file == null || !file.exists()) {
			return;
		}
		if (file.isDirectory()) {
			File[] children = file.listFiles();
			if (children != null) {
				for (File child : children) {
					this.deleteRecursively(child);
				}
			}
		}
		file.delete();
	}

	private static final class IOFittizio implements IO {

		@Override
		public void stampaMessaggio(String msf) {
		}
	}
}
