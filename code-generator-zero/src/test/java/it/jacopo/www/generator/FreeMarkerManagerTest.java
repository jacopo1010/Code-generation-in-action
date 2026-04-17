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

	public void testGenerateDaoPersisteERicostruisceForeignKeyManyToOne() throws Exception {
		File tempDirectory = Files.createTempDirectory("dao-many-to-one-persistence-test").toFile();
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

		

			File generatedFile = new File(tempDirectory, "it\\test\\dao\\TaskDao.java");
			assertTrue("DAO non generato nel path atteso", generatedFile.isFile());

			String generatedContent = new String(Files.readAllBytes(generatedFile.toPath()), StandardCharsets.UTF_8);
			assertTrue(generatedContent.contains("\"project_id\""));
			assertTrue(generatedContent.contains("INSERT INTO tasks (name, project_id) VALUES (?, ?)"));
			assertTrue(generatedContent.contains("UPDATE tasks SET name = ?, project_id = ? WHERE id = ?"));
			assertTrue(generatedContent.contains("statement.setLong(2, entity.getContain().getId());"));
			assertTrue(generatedContent.contains("Project contain = new Project();"));
			assertTrue(generatedContent.contains("contain.setId(resultSet.getLong(\"project_id\"));"));
			assertTrue(generatedContent.contains("entity.setContain(contain);"));
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
			assertTrue(generatedContent.contains("private void validateEntityForWrite(Cliente entity) throws SQLException"));
			assertTrue(generatedContent.contains("if (entity == null)"));
			assertTrue(generatedContent.contains("throw new IllegalArgumentException(\"Cliente obbligatorio\");"));
			assertTrue(generatedContent.contains("this.prepareForCreate(entity);"));
			assertTrue(generatedContent.contains("this.prepareForUpdate(entity);"));
			assertTrue(generatedContent.contains("throw new IllegalArgumentException(\"Id obbligatorio\");"));
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
			assertTrue(generatedContent.contains("import it.test.model.Studente;"));
			assertTrue(generatedContent.contains("import it.test.dao.StudenteDao;"));
			assertTrue(generatedContent.contains("private final StudenteDao studenteDao;"));
			assertTrue(generatedContent.contains("this.studenteDao = new StudenteDao(this.dataSource);"));
			assertTrue(generatedContent.contains("Studente studente = entity.getStudente();"));
			assertTrue(generatedContent.contains("if (studente != null)"));
			assertTrue(generatedContent.contains("if (studente.getId() == null)"));
			assertTrue(generatedContent.contains("if (!this.studenteDao.existsById(studente.getId()))"));
			assertTrue(generatedContent.contains("throw new IllegalArgumentException(\"Id Studente associato obbligatorio\");"));
			assertTrue(generatedContent.contains("throw new IllegalArgumentException(\"Studente associato non esistente: \" + studente.getId());"));
			assertTrue(generatedContent.contains("public List<Corso> findByStudenteId(Long studenteId) throws SQLException"));
			assertTrue(generatedContent.contains("return this.getDao().findByStudenteId(studenteId);"));
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
					"it.test.dao",
					"it.test.service",
					metaClasses,
					tempDirectory.getAbsolutePath());

			File generatedFile = new File(tempDirectory, "it\\test\\service\\TaskService.java");
			assertTrue("Service non generato nel path atteso", generatedFile.isFile());

			String generatedContent = new String(Files.readAllBytes(generatedFile.toPath()), StandardCharsets.UTF_8);
			assertTrue(generatedContent.contains("import java.sql.Timestamp;"));
			assertTrue(generatedContent.contains("private void prepareForCreate(Task entity)"));
			assertTrue(generatedContent.contains("private void validateEntityForWrite(Task entity) throws SQLException"));
			assertTrue(generatedContent.contains("throw new IllegalArgumentException(\"Task obbligatorio\");"));
			assertTrue(generatedContent.contains("if (entity.getCreationTimeStamp() == null)"));
			assertTrue(generatedContent.contains("entity.setLastUpdateTimeStamp(now);"));
			assertTrue(generatedContent.contains("private void prepareForUpdate(Task entity)"));
			assertTrue(generatedContent.contains("if (entity.getId() == null)"));
			assertTrue(generatedContent.contains("throw new IllegalArgumentException(\"Id obbligatorio\");"));
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
					"it.test.dao",
					"it.test.service",
					metaClasses,
					tempDirectory.getAbsolutePath());

			File generatedFile = new File(tempDirectory, "it\\test\\service\\TaskService.java");
			assertTrue("Service non generato nel path atteso", generatedFile.isFile());

			String generatedContent = new String(Files.readAllBytes(generatedFile.toPath()), StandardCharsets.UTF_8);
			assertTrue(generatedContent.contains("import it.test.model.Project;"));
			assertTrue(generatedContent.contains("import it.test.dao.ProjectDao;"));
			assertTrue(generatedContent.contains("private final ProjectDao projectDao;"));
			assertTrue(generatedContent.contains("this.projectDao = new ProjectDao(this.dataSource);"));
			assertTrue(generatedContent.contains("Project contain = entity.getContain();"));
			assertTrue(generatedContent.contains("if (contain == null || contain.getId() == null)"));
			assertTrue(generatedContent.contains("throw new IllegalArgumentException(\"Project associato obbligatorio\");"));
			assertTrue(generatedContent.contains("if (!this.projectDao.existsById(contain.getId()))"));
			assertTrue(generatedContent.contains("throw new IllegalArgumentException(\"Project associato non esistente: \" + contain.getId());"));
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

			File generatedFile = new File(tempDirectory, "it\\test\\controller\\TaskController.java");
			assertTrue("Controller non generato nel path atteso", generatedFile.isFile());

			String generatedContent = new String(Files.readAllBytes(generatedFile.toPath()), StandardCharsets.UTF_8);
			assertTrue(generatedContent.contains("package it.test.controller;"));
			assertTrue(generatedContent.contains("import jakarta.ws.rs.Path;"));
			assertTrue(generatedContent.contains("import jakarta.ws.rs.core.Response;"));
			assertTrue(generatedContent.contains("import it.test.model.Task;"));
			assertTrue(generatedContent.contains("import it.test.service.TaskService;"));
			assertTrue(generatedContent.contains("@Path(\"api/tasks\")"));
			assertTrue(generatedContent.contains("private TaskService taskService;"));
			assertTrue(generatedContent.contains("this.taskService = new TaskService();"));
			assertTrue(generatedContent.contains("public Response getAllTasks() throws SQLException"));
			assertTrue(generatedContent.contains("return Response.ok(tasks).build();"));
			assertTrue(generatedContent.contains("public Response countTasks() throws SQLException"));
			assertTrue(generatedContent.contains("public Response existsTask(@PathParam(\"id\") Long id) throws SQLException"));
			assertTrue(generatedContent.contains("return Response.ok(this.taskService.existsById(id)).build();"));
			assertTrue(generatedContent.contains("public Response findByKeyword(@QueryParam(\"keyword\") String keyword) throws SQLException"));
			assertTrue(generatedContent.contains("return Response.ok(this.taskService.findByKeyword(keyword)).build();"));
			assertTrue(generatedContent.contains("public Response getTask(@PathParam(\"id\") Long id) throws SQLException"));
			assertTrue(generatedContent.contains("Task task = this.taskService.findById(id);"));
			assertTrue(generatedContent.contains("public Response createTask(Task task) throws SQLException"));
			assertTrue(generatedContent.contains("Task created = this.taskService.save(task);"));
			assertTrue(generatedContent.contains("return Response.created(URI.create(\"/api/tasks/\" + created.getId())).entity(created).build();"));
			assertTrue(generatedContent.contains("public Response updateTask(@PathParam(\"id\") Long id, Task task) throws SQLException"));
			assertTrue(generatedContent.contains("task.setId(id);"));
			assertTrue(generatedContent.contains("boolean updated = this.taskService.update(task);"));
			assertTrue(generatedContent.contains("public Response deleteAll() throws SQLException"));
			assertTrue(generatedContent.contains("public Response deleteById(@PathParam(\"id\") Long id) throws SQLException"));
			assertTrue(generatedContent.contains("boolean deleted = this.taskService.delete(id);"));
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

			File generatedFile = new File(tempDirectory, "it\\test\\controller\\TaskController.java");
			assertTrue("Controller non generato nel path atteso", generatedFile.isFile());

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
