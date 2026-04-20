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

	public void testGenerateModelUsaGenerationGapEPreservaCustom() throws Exception {
		File tempDirectory = Files.createTempDirectory("model-generation-gap-test").toFile();
		try {
			FreeMarkerManager manager = new FreeMarkerManager(new IOFittizio());
			MetaClass metaClass = new MetaClass();
			metaClass.setName("Cliente");
			metaClass.setJavaDoc("Model cliente");

			MetaField idField = new MetaField();
			idField.setName("id");
			idField.setJavaType("Long");
			idField.setJavaDoc("Identificativo");
			metaClass.addField(idField);

			Map<String, MetaClass> metaClasses = new LinkedHashMap<String, MetaClass>();
			metaClasses.put(metaClass.getName(), metaClass);

			manager.generateModel(
					"it.test.model",
					metaClasses,
					tempDirectory.getAbsolutePath());

			File generatedFile = new File(tempDirectory, "it\\test\\model\\ClienteBase.java");
			File wrapperFile = new File(tempDirectory, "it\\test\\model\\Cliente.java");
			assertTrue("Model base non generato nel path atteso", generatedFile.isFile());
			assertTrue("Model custom non generato nel path atteso", wrapperFile.isFile());

			String generatedContent = new String(Files.readAllBytes(generatedFile.toPath()), StandardCharsets.UTF_8);
			String wrapperContent = new String(Files.readAllBytes(wrapperFile.toPath()), StandardCharsets.UTF_8);
			assertTrue(generatedContent.contains("package it.test.model;"));
			assertTrue(generatedContent.contains("public class ClienteBase"));
			assertTrue(generatedContent.contains("public ClienteBase()"));
			assertTrue(generatedContent.contains("private Long id;"));
			assertTrue(generatedContent.contains("public Long getId()"));
			assertTrue(wrapperContent.contains("public class Cliente extends ClienteBase"));
			assertTrue(wrapperContent.contains("public Cliente()"));
			assertTrue(wrapperContent.contains("super();"));

			Files.write(wrapperFile.toPath(),
					("// mio codice custom\r\npublic class Cliente extends ClienteBase {}\r\n")
							.getBytes(StandardCharsets.UTF_8));

			manager.generateModel(
					"it.test.model",
					metaClasses,
					tempDirectory.getAbsolutePath());

			String preservedWrapperContent = new String(Files.readAllBytes(wrapperFile.toPath()), StandardCharsets.UTF_8);
			assertTrue(preservedWrapperContent.contains("// mio codice custom"));
		} finally {
			this.deleteRecursively(tempDirectory);
		}
	}

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

	public void testGenerateRepositoryUsaPackageRepositoryEImportModelCorretti() throws Exception {
		File tempDirectory = Files.createTempDirectory("repository-template-test").toFile();
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

			manager.generateRepository(
					"it.test.model",
					"it.test.jooq",
					"it.test.repository",
					metaClasses,
					tempDirectory.getAbsolutePath());

			File generatedFile = new File(tempDirectory, "it\\test\\repository\\ClienteRepository.java");
			File simpleRepositoryFile = new File(tempDirectory, "it\\test\\repository\\SimpleRepository.java");
			File simpleRepositoryImplFile = new File(tempDirectory, "it\\test\\repository\\SimpleRepositoryImpl.java");
			assertTrue("RepositoryGenerated non generato nel path atteso", generatedFile.isFile());
			assertTrue("SimpleRepository non generato nel path atteso", simpleRepositoryFile.isFile());
			assertTrue("SimpleRepositoryImpl non generato nel path atteso", simpleRepositoryImplFile.isFile());
			String generatedContent = new String(Files.readAllBytes(generatedFile.toPath()), StandardCharsets.UTF_8);
			String simpleRepositoryContent = new String(Files.readAllBytes(simpleRepositoryFile.toPath()),
					StandardCharsets.UTF_8);
			String simpleRepositoryImplContent = new String(Files.readAllBytes(simpleRepositoryImplFile.toPath()),
					StandardCharsets.UTF_8);
			assertTrue(generatedContent.contains("package it.test.repository;"));
			assertTrue(generatedContent.contains("import it.test.model.Cliente;"));
			assertTrue(generatedContent.contains("import static it.test.jooq.tables.Cliente.CLIENTE;"));
			assertTrue(generatedContent.contains("public class ClienteRepository extends SimpleRepositoryImpl<Cliente>"));
			assertTrue(generatedContent.contains("public ClienteRepository(HikariDataSource dataSource)"));
			assertTrue(generatedContent.contains("protected Table<?> getTable()"));
			assertTrue(generatedContent.contains("protected void bindRecord(UpdatableRecord<?> record, Cliente entity)"));
			assertTrue(generatedContent.contains("protected Field<Long> getIdField()"));
			assertFalse(generatedContent.contains("deleteById("));
			assertTrue(simpleRepositoryContent.contains("package it.test.repository;"));
			assertTrue(simpleRepositoryContent.contains("public interface SimpleRepository<T>"));
			assertTrue(simpleRepositoryImplContent.contains("package it.test.repository;"));
			assertTrue(simpleRepositoryImplContent.contains("public abstract class SimpleRepositoryImpl<T> implements SimpleRepository<T>"));
		} finally {
			this.deleteRecursively(tempDirectory);
		}
	}

	public void testGenerateRepositoryCreaFinderPerRelazioneManyToOne() throws Exception {
		File tempDirectory = Files.createTempDirectory("repository-many-to-one-test").toFile();
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

			manager.generateRepository(
					"it.test.model",
					"it.test.jooq",
					"it.test.repository",
					metaClasses,
					tempDirectory.getAbsolutePath());

			File generatedFile = new File(tempDirectory, "it\\test\\repository\\CorsoRepository.java");
			assertTrue("RepositoryGenerated non generato nel path atteso", generatedFile.isFile());
			String generatedContent = new String(Files.readAllBytes(generatedFile.toPath()), StandardCharsets.UTF_8);
			assertTrue(generatedContent.contains("public List<Corso> findByStudenteId(Long id)"));
			assertTrue(generatedContent.contains(".where(CORSO.STUDENTE_ID.eq(id))"));
			assertTrue(generatedContent.contains(".map(this::toModel);"));
		} finally {
			this.deleteRecursively(tempDirectory);
		}
	}

	public void testGenerateRepositoryNonSovrascriveIlCustomSeEsiste() throws Exception {
		File tempDirectory = Files.createTempDirectory("repository-custom-preserve-test").toFile();
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

			manager.generateRepository(
					"it.test.model",
					"it.test.jooq",
					"it.test.repository",
					metaClasses,
					tempDirectory.getAbsolutePath());

			File customFile = new File(tempDirectory, "it\\test\\repository\\ClienteRepository.java");
			Files.write(customFile.toPath(),
					("// mio codice custom\r\npublic class ClienteRepository {}\r\n")
							.getBytes(StandardCharsets.UTF_8));

			File simpleRepositoryFile = new File(tempDirectory, "it\\test\\repository\\SimpleRepository.java");
			Files.write(simpleRepositoryFile.toPath(),
					("// custom infra\r\npublic interface SimpleRepository<T> {}\r\n")
							.getBytes(StandardCharsets.UTF_8));

			manager.generateRepository(
					"it.test.model",
					"it.test.jooq",
					"it.test.repository",
					metaClasses,
					tempDirectory.getAbsolutePath());

			String customContent = new String(Files.readAllBytes(customFile.toPath()), StandardCharsets.UTF_8);
			assertTrue(customContent.contains("// mio codice custom"));
			String simpleRepositoryContent = new String(Files.readAllBytes(simpleRepositoryFile.toPath()),
					StandardCharsets.UTF_8);
			assertTrue(simpleRepositoryContent.contains("// custom infra"));
		} finally {
			this.deleteRecursively(tempDirectory);
		}
	}

	public void testGenerateRepositoryPersisteERicostruisceForeignKeyManyToOne() throws Exception {
		File tempDirectory = Files.createTempDirectory("repository-many-to-one-persistence-test").toFile();
		try {
			FreeMarkerManager manager = new FreeMarkerManager(new IOFittizio());

			MetaClass projectMetaClass = new MetaClass();
			projectMetaClass.setName("Project");

			MetaField projectIdField = new MetaField();
			projectIdField.setName("id");
			projectIdField.setJavaType("Long");
			projectIdField.setSqlType("BIGINT");
			projectMetaClass.addField(projectIdField);

			MetaClass taskMetaClass = new MetaClass();
			taskMetaClass.setName("Task");
			taskMetaClass.setTable("tasks");

			MetaField taskIdField = new MetaField();
			taskIdField.setName("id");
			taskIdField.setJavaType("Long");
			taskIdField.setSqlType("BIGINT");
			taskMetaClass.addField(taskIdField);

			MetaField taskNameField = new MetaField();
			taskNameField.setName("name");
			taskNameField.setJavaType("String");
			taskMetaClass.addField(taskNameField);

			MetaField creationField = new MetaField();
			creationField.setName("creationTimeStamp");
			creationField.setJavaType("Timestamp");
			taskMetaClass.addField(creationField);

			MetaField updateField = new MetaField();
			updateField.setName("lastUpdateTimeStamp");
			updateField.setJavaType("Timestamp");
			taskMetaClass.addField(updateField);

			MetaField projectRelationField = new MetaField();
			projectRelationField.setName("contain");
			projectRelationField.setJavaType("Project");
			projectRelationField.setRelation(true);
			projectRelationField.setRelationType("MANY_TO_ONE");
			projectRelationField.setForeignKeyColumn("project_id");
			projectRelationField.setRequired(true);
			taskMetaClass.addField(projectRelationField);

			Map<String, MetaClass> metaClasses = new LinkedHashMap<String, MetaClass>();
			metaClasses.put(projectMetaClass.getName(), projectMetaClass);
			metaClasses.put(taskMetaClass.getName(), taskMetaClass);

			manager.generateRepository(
					"it.test.model",
					"it.test.jooq",
					"it.test.repository",
					metaClasses,
					tempDirectory.getAbsolutePath());

			File generatedFile = new File(tempDirectory, "it\\test\\repository\\TaskRepository.java");
			assertTrue("RepositoryGenerated non generato nel path atteso", generatedFile.isFile());
			String generatedContent = new String(Files.readAllBytes(generatedFile.toPath()), StandardCharsets.UTF_8);
			assertTrue(generatedContent.contains("import java.time.LocalDateTime;"));
			assertTrue(generatedContent.contains("import java.sql.Timestamp;"));
			assertTrue(generatedContent.contains("private Timestamp toTimestamp(LocalDateTime value)"));
			assertTrue(generatedContent.contains("private LocalDateTime toLocalDateTime(Timestamp value)"));
			assertTrue(generatedContent.contains("entity.setCreationTimeStamp(this.toTimestamp(record.get(TASKS.CREATION_TIME_STAMP, LocalDateTime.class)));"));
			assertTrue(generatedContent.contains("entity.setLastUpdateTimeStamp(this.toTimestamp(record.get(TASKS.LAST_UPDATE_TIME_STAMP, LocalDateTime.class)));"));
			assertTrue(generatedContent.contains("import it.test.model.Project;"));
			assertTrue(generatedContent.contains("import static it.test.jooq.tables.Tasks.TASKS;"));
			assertTrue(generatedContent.contains("record.set(TASKS.CREATION_TIME_STAMP, this.toLocalDateTime(entity.getCreationTimeStamp()));"));
			assertTrue(generatedContent.contains("record.set(TASKS.LAST_UPDATE_TIME_STAMP, this.toLocalDateTime(entity.getLastUpdateTimeStamp()));"));
			assertTrue(generatedContent.contains("record.set(TASKS.PROJECT_ID, entity.getContain().getId());"));
			assertTrue(generatedContent.contains(".set(TASKS.CREATION_TIME_STAMP, this.toLocalDateTime(entity.getCreationTimeStamp()))"));
			assertTrue(generatedContent.contains(".set(TASKS.LAST_UPDATE_TIME_STAMP, this.toLocalDateTime(entity.getLastUpdateTimeStamp()))"));
			assertTrue(generatedContent.contains(".set(TASKS.PROJECT_ID,"));
			assertTrue(generatedContent.contains("entity.getContain() != null ? entity.getContain().getId() : null"));
			assertTrue(generatedContent.contains("Project contain = new Project();"));
			assertTrue(generatedContent.contains("contain.setId(record.get(TASKS.PROJECT_ID, Long.class));"));
			assertTrue(generatedContent.contains("entity.setContain(contain);"));
			assertTrue(generatedContent.contains("public class TaskRepository extends SimpleRepositoryImpl<Task>"));
		} finally {
			this.deleteRecursively(tempDirectory);
		}
	}

	public void testGenerateServiceConRepositoryInjection() throws Exception {
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
					"it.test.repository",
					"it.test.service",
					metaClasses,
					tempDirectory.getAbsolutePath());

			File generatedFile = new File(tempDirectory, "it\\test\\service\\ClienteServiceBase.java");
			File wrapperFile = new File(tempDirectory, "it\\test\\service\\ClienteService.java");
			assertTrue("ServiceGenerated non generato nel path atteso", generatedFile.isFile());
			assertTrue("Service custom non generato nel path atteso", wrapperFile.isFile());

			String generatedContent = new String(Files.readAllBytes(generatedFile.toPath()), StandardCharsets.UTF_8);
			String wrapperContent = new String(Files.readAllBytes(wrapperFile.toPath()), StandardCharsets.UTF_8);
			assertTrue(generatedContent.contains("package it.test.service;"));
			assertTrue(generatedContent.contains("import it.test.model.Cliente;"));
			assertTrue(generatedContent.contains("import it.test.repository.ClienteRepository;"));
			assertTrue(generatedContent.contains("public class ClienteServiceBase"));
			assertTrue(generatedContent.contains("protected final ClienteRepository repository;"));
			assertTrue(generatedContent.contains("protected ClienteServiceBase(ClienteRepository repository)"));
			assertTrue(generatedContent.contains("this.repository = repository;"));
			assertTrue(generatedContent.contains("private void validateEntityForWrite(Cliente entity)"));
			assertTrue(generatedContent.contains("if (entity == null)"));
			assertTrue(generatedContent.contains("throw new IllegalArgumentException(\"Cliente obbligatorio\");"));
			assertTrue(generatedContent.contains("this.prepareForCreate(entity);"));
			assertTrue(generatedContent.contains("this.prepareForUpdate(entity);"));
			assertTrue(generatedContent.contains("throw new IllegalArgumentException(\"Id obbligatorio per l'aggiornamento di Cliente\");"));
			assertTrue(generatedContent.contains("public List<Cliente> findByKeyword(String keyword)"));
			assertTrue(generatedContent.contains("return this.repository.findByKeyword(keyword);"));
			assertTrue(generatedContent.contains("return this.repository.delete(id);"));
			assertTrue(wrapperContent.contains("public class ClienteService extends ClienteServiceBase"));
			assertTrue(wrapperContent.contains("public ClienteService(ClienteRepository repository)"));
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
					"it.test.repository",
					"it.test.service",
					metaClasses,
					tempDirectory.getAbsolutePath());

			File generatedFile = new File(tempDirectory, "it\\test\\service\\CorsoServiceBase.java");
			assertTrue("ServiceGenerated non generato nel path atteso", generatedFile.isFile());

			String generatedContent = new String(Files.readAllBytes(generatedFile.toPath()), StandardCharsets.UTF_8);
			assertTrue(generatedContent.contains("import it.test.model.Studente;"));
			assertTrue(generatedContent.contains("import it.test.repository.StudenteRepository;"));
			assertTrue(generatedContent.contains("protected final StudenteRepository studenteRepository;"));
			assertTrue(generatedContent.contains("StudenteRepository studenteRepository"));
			assertTrue(generatedContent.contains("Studente studente = entity.getStudente();"));
			assertTrue(generatedContent.contains("if (studente != null)"));
			assertTrue(generatedContent.contains("if (studente.getId() == null)"));
			assertTrue(generatedContent.contains("if (!this.studenteRepository.existsById(studente.getId()))"));
			assertTrue(generatedContent.contains("throw new IllegalArgumentException(\"Id di Studente associato obbligatorio\");"));
			assertTrue(generatedContent.contains("throw new IllegalArgumentException(\"Studente associato non esistente: \""));
			assertTrue(generatedContent.contains("+ studente.getId());"));
			assertTrue(generatedContent.contains("public List<Corso> findByStudenteId(Long id)"));
			assertTrue(generatedContent.contains("return this.repository.findByStudenteId(id);"));
		} finally {
			this.deleteRecursively(tempDirectory);
		}
	}

	public void testGenerateServicePreparaTimestampTecniciPrimaDiSaveEUpdate() throws Exception {
		File tempDirectory = Files.createTempDirectory("service-timestamp-test").toFile();
		try {
			FreeMarkerManager manager = new FreeMarkerManager(new IOFittizio());
			MetaClass taskMetaClass = new MetaClass();
			taskMetaClass.setName("Task");

			MetaField idField = new MetaField();
			idField.setName("id");
			idField.setJavaType("Long");
			taskMetaClass.addField(idField);

			MetaField creationField = new MetaField();
			creationField.setName("creationTimeStamp");
			creationField.setJavaType("Timestamp");
			taskMetaClass.addField(creationField);

			MetaField updateField = new MetaField();
			updateField.setName("lastUpdateTimeStamp");
			updateField.setJavaType("Timestamp");
			taskMetaClass.addField(updateField);

			Map<String, MetaClass> metaClasses = new LinkedHashMap<String, MetaClass>();
			metaClasses.put(taskMetaClass.getName(), taskMetaClass);

			manager.generateService(
					"it.test.model",
					"it.test.repository",
					"it.test.service",
					metaClasses,
					tempDirectory.getAbsolutePath());

			File generatedFile = new File(tempDirectory, "it\\test\\service\\TaskServiceBase.java");
			assertTrue("ServiceGenerated non generato nel path atteso", generatedFile.isFile());

			String generatedContent = new String(Files.readAllBytes(generatedFile.toPath()), StandardCharsets.UTF_8);
			assertTrue(generatedContent.contains("import java.sql.Timestamp;"));
			assertTrue(generatedContent.contains("private void prepareForCreate(Task entity)"));
			assertTrue(generatedContent.contains("private void validateEntityForWrite(Task entity)"));
			assertTrue(generatedContent.contains("throw new IllegalArgumentException(\"Task obbligatorio\");"));
			assertTrue(generatedContent.contains("if (entity.getCreationTimeStamp() == null)"));
			assertTrue(generatedContent.contains("entity.setLastUpdateTimeStamp(now);"));
			assertTrue(generatedContent.contains("private void prepareForUpdate(Task entity)"));
			assertTrue(generatedContent.contains("if (entity.getId() == null)"));
			assertTrue(generatedContent.contains("throw new IllegalArgumentException(\"Id obbligatorio per l'aggiornamento di Task\");"));
			assertTrue(generatedContent.contains("entity.setCreationTimeStamp(new Timestamp(System.currentTimeMillis()));"));
			assertTrue(generatedContent.contains("entity.setLastUpdateTimeStamp(new Timestamp(System.currentTimeMillis()));"));
			assertTrue(generatedContent.contains("this.prepareForCreate(entity);"));
			assertTrue(generatedContent.contains("this.prepareForUpdate(entity);"));
		} finally {
			this.deleteRecursively(tempDirectory);
		}
	}

	public void testGenerateServiceValidaRelazioneManyToOneObbligatoriaPrimaDiSaveEUpdate() throws Exception {
		File tempDirectory = Files.createTempDirectory("service-required-relation-test").toFile();
		try {
			FreeMarkerManager manager = new FreeMarkerManager(new IOFittizio());
			MetaClass projectMetaClass = new MetaClass();
			projectMetaClass.setName("Project");

			MetaField projectIdField = new MetaField();
			projectIdField.setName("id");
			projectIdField.setJavaType("Long");
			projectMetaClass.addField(projectIdField);

			MetaClass taskMetaClass = new MetaClass();
			taskMetaClass.setName("Task");

			MetaField taskIdField = new MetaField();
			taskIdField.setName("id");
			taskIdField.setJavaType("Long");
			taskMetaClass.addField(taskIdField);

			MetaField projectRelationField = new MetaField();
			projectRelationField.setName("contain");
			projectRelationField.setJavaType("Project");
			projectRelationField.setRelation(true);
			projectRelationField.setRelationType("MANY_TO_ONE");
			projectRelationField.setForeignKeyColumn("project_id");
			projectRelationField.setRequired(true);
			taskMetaClass.addField(projectRelationField);

			Map<String, MetaClass> metaClasses = new LinkedHashMap<String, MetaClass>();
			metaClasses.put(projectMetaClass.getName(), projectMetaClass);
			metaClasses.put(taskMetaClass.getName(), taskMetaClass);

			manager.generateService(
					"it.test.model",
					"it.test.repository",
					"it.test.service",
					metaClasses,
					tempDirectory.getAbsolutePath());

			File generatedFile = new File(tempDirectory, "it\\test\\service\\TaskServiceBase.java");
			assertTrue("ServiceGenerated non generato nel path atteso", generatedFile.isFile());

			String generatedContent = new String(Files.readAllBytes(generatedFile.toPath()), StandardCharsets.UTF_8);
			assertTrue(generatedContent.contains("import it.test.model.Project;"));
			assertTrue(generatedContent.contains("import it.test.repository.ProjectRepository;"));
			assertTrue(generatedContent.contains("protected final ProjectRepository projectRepository;"));
			assertTrue(generatedContent.contains("ProjectRepository projectRepository"));
			assertTrue(generatedContent.contains("Project contain = entity.getContain();"));
			assertTrue(generatedContent.contains("if (contain == null || contain.getId() == null)"));
			assertTrue(generatedContent.contains("throw new IllegalArgumentException(\"Project associato obbligatorio per Task\");"));
			assertTrue(generatedContent.contains("if (!this.projectRepository.existsById(contain.getId()))"));
			assertTrue(generatedContent.contains("throw new IllegalArgumentException(\"Project associato non esistente: \""));
			assertTrue(generatedContent.contains("+ contain.getId());"));
			assertTrue(generatedContent.contains("this.validateEntityForWrite(entity);"));
		} finally {
			this.deleteRecursively(tempDirectory);
		}
	}

	public void testGenerateControllerCreaCrudCompletoBasatoSulService() throws Exception {
		File tempDirectory = Files.createTempDirectory("controller-template-test").toFile();
		try {
			FreeMarkerManager manager = new FreeMarkerManager(new IOFittizio());
			MetaClass taskMetaClass = new MetaClass();
			taskMetaClass.setName("Task");

			MetaField idField = new MetaField();
			idField.setName("id");
			idField.setJavaType("Long");
			taskMetaClass.addField(idField);

			MetaField nameField = new MetaField();
			nameField.setName("name");
			nameField.setJavaType("String");
			taskMetaClass.addField(nameField);

			Map<String, MetaClass> metaClasses = new LinkedHashMap<String, MetaClass>();
			metaClasses.put(taskMetaClass.getName(), taskMetaClass);

			manager.generateController(
					"it.test.model",
					"it.test.service",
					"it.test.controller",
					metaClasses,
					tempDirectory.getAbsolutePath());

			File generatedFile = new File(tempDirectory, "it\\test\\controller\\TaskControllerBase.java");
			File wrapperFile = new File(tempDirectory, "it\\test\\controller\\TaskController.java");
			assertTrue("ControllerGenerated non generato nel path atteso", generatedFile.isFile());
			assertTrue("Controller custom non generato nel path atteso", wrapperFile.isFile());

			String generatedContent = new String(Files.readAllBytes(generatedFile.toPath()), StandardCharsets.UTF_8);
			String wrapperContent = new String(Files.readAllBytes(wrapperFile.toPath()), StandardCharsets.UTF_8);
			assertTrue(generatedContent.contains("package it.test.controller;"));
			assertTrue(generatedContent.contains("import jakarta.ws.rs.Path;"));
			assertTrue(generatedContent.contains("import jakarta.ws.rs.core.Response;"));
			assertTrue(generatedContent.contains("import java.util.Optional;"));
			assertTrue(generatedContent.contains("import it.test.model.Task;"));
			assertTrue(generatedContent.contains("import it.test.service.TaskServiceBase;"));
			assertTrue(generatedContent.contains("@Path(\"api/tasks\")"));
			assertTrue(generatedContent.contains("public class TaskControllerBase"));
			assertTrue(generatedContent.contains("protected final TaskServiceBase taskService;"));
			assertTrue(generatedContent.contains("protected TaskControllerBase(TaskServiceBase taskService)"));
			assertTrue(generatedContent.contains("this.taskService = taskService;"));
			assertTrue(generatedContent.contains("public Response getAllTasks() throws SQLException"));
			assertTrue(generatedContent.contains("return Response.ok(tasks).build();"));
			assertTrue(generatedContent.contains("public Response countTasks() throws SQLException"));
			assertTrue(generatedContent.contains("public Response existsTask(@PathParam(\"id\") Long id) throws SQLException"));
			assertTrue(generatedContent.contains("return Response.ok(this.taskService.existsById(id)).build();"));
			assertTrue(generatedContent.contains("public Response findByKeyword(@QueryParam(\"keyword\") String keyword) throws SQLException"));
			assertTrue(generatedContent.contains("return Response.ok(this.taskService.findByKeyword(keyword)).build();"));
			assertTrue(generatedContent.contains("public Response getTask(@PathParam(\"id\") Long id) throws SQLException"));
			assertTrue(generatedContent.contains("Optional<Task> task = this.taskService.findById(id);"));
			assertTrue(generatedContent.contains("if (!task.isPresent())"));
			assertTrue(generatedContent.contains("return Response.ok(task.get()).build();"));
			assertTrue(generatedContent.contains("public Response createTask(Task task) throws SQLException"));
			assertTrue(generatedContent.contains("Task created = this.taskService.save(task);"));
			assertTrue(generatedContent.contains("return Response.created(URI.create(\"/api/tasks/\" + created.getId())).entity(created).build();"));
			assertTrue(generatedContent.contains("public Response updateTask(@PathParam(\"id\") Long id, Task task) throws SQLException"));
			assertTrue(generatedContent.contains("task.setId(id);"));
			assertTrue(generatedContent.contains("boolean updated = this.taskService.update(task);"));
			assertTrue(generatedContent.contains("public Response deleteAll() throws SQLException"));
			assertTrue(generatedContent.contains("public Response deleteById(@PathParam(\"id\") Long id) throws SQLException"));
			assertTrue(generatedContent.contains("boolean deleted = this.taskService.delete(id);"));
			assertTrue(wrapperContent.contains("public class TaskController extends TaskControllerBase"));
			assertTrue(wrapperContent.contains("public TaskController(TaskServiceBase taskService)"));
		} finally {
			this.deleteRecursively(tempDirectory);
		}
	}

	public void testGenerateControllerCreaEndpointPerRelazioneManyToOne() throws Exception {
		File tempDirectory = Files.createTempDirectory("controller-many-to-one-test").toFile();
		try {
			FreeMarkerManager manager = new FreeMarkerManager(new IOFittizio());
			MetaClass projectMetaClass = new MetaClass();
			projectMetaClass.setName("Project");

			MetaField projectIdField = new MetaField();
			projectIdField.setName("id");
			projectIdField.setJavaType("Long");
			projectMetaClass.addField(projectIdField);

			MetaClass taskMetaClass = new MetaClass();
			taskMetaClass.setName("Task");

			MetaField taskIdField = new MetaField();
			taskIdField.setName("id");
			taskIdField.setJavaType("Long");
			taskMetaClass.addField(taskIdField);

			MetaField projectRelationField = new MetaField();
			projectRelationField.setName("contain");
			projectRelationField.setJavaType("Project");
			projectRelationField.setRelation(true);
			projectRelationField.setRelationType("MANY_TO_ONE");
			projectRelationField.setForeignKeyColumn("project_id");
			taskMetaClass.addField(projectRelationField);

			Map<String, MetaClass> metaClasses = new LinkedHashMap<String, MetaClass>();
			metaClasses.put(projectMetaClass.getName(), projectMetaClass);
			metaClasses.put(taskMetaClass.getName(), taskMetaClass);

			manager.generateController(
					"it.test.model",
					"it.test.service",
					"it.test.controller",
					metaClasses,
					tempDirectory.getAbsolutePath());

			File generatedFile = new File(tempDirectory, "it\\test\\controller\\TaskControllerBase.java");
			assertTrue("ControllerGenerated non generato nel path atteso", generatedFile.isFile());

			String generatedContent = new String(Files.readAllBytes(generatedFile.toPath()), StandardCharsets.UTF_8);
			assertTrue(generatedContent.contains("@Path(\"/by-contain/{containId}\")"));
			assertTrue(generatedContent.contains("public Response findByContainId(@PathParam(\"containId\") Long containId) throws SQLException"));
			assertTrue(generatedContent.contains("return Response.ok(this.taskService.findByContainId(containId)).build();"));
		} finally {
			this.deleteRecursively(tempDirectory);
		}
	}

	public void testGenerateSchemaUsaIdentityPerIdBigInt() throws Exception {
		File tempFile = Files.createTempFile("schema-auto-increment-test", ".sql").toFile();
		try {
			FreeMarkerManager manager = new FreeMarkerManager(new IOFittizio());

			MetaClass userMetaClass = new MetaClass();
			userMetaClass.setName("User");
			userMetaClass.setTable("users");

			MetaField idField = new MetaField();
			idField.setName("id");
			idField.setJavaType("Long");
			idField.setSqlType("BIGINT");
			userMetaClass.addField(idField);

			MetaField nameField = new MetaField();
			nameField.setName("name");
			nameField.setJavaType("String");
			nameField.setSqlType("VARCHAR");
			userMetaClass.addField(nameField);

			Map<String, MetaClass> metaClasses = new LinkedHashMap<String, MetaClass>();
			metaClasses.put(userMetaClass.getName(), userMetaClass);

			manager.generateSchema(metaClasses, tempFile.getAbsolutePath());

			String generatedContent = new String(Files.readAllBytes(tempFile.toPath()), StandardCharsets.UTF_8);
			assertTrue(generatedContent.contains("id BIGINT GENERATED BY DEFAULT AS IDENTITY"));
		} finally {
			tempFile.delete();
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
