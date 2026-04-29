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

	public void testGenerateModelGeneraUnaSolaEntityEPreservaCustom() throws Exception {
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

			File generatedFile = new File(tempDirectory, "it\\test\\model\\Cliente.java");
			assertTrue("Model non generato nel path atteso", generatedFile.isFile());

			String generatedContent = new String(Files.readAllBytes(generatedFile.toPath()), StandardCharsets.UTF_8);
			assertTrue(generatedContent.contains("package it.test.model;"));
			assertTrue(generatedContent.contains("import jakarta.persistence.Entity;"));
			assertTrue(generatedContent.contains("import jakarta.persistence.Table;"));
			assertTrue(generatedContent.contains("@Entity"));
			assertTrue(generatedContent.contains("@Table(name = \"cliente\")"));
			assertTrue(generatedContent.contains("public class Cliente"));
			assertTrue(generatedContent.contains("public Cliente()"));
			assertTrue(generatedContent.contains("@Id"));
			assertTrue(generatedContent.contains("@GeneratedValue(strategy = GenerationType.IDENTITY)"));
			assertTrue(generatedContent.contains("@Column(name = \"id\")"));
			assertTrue(generatedContent.contains("private Long id;"));
			assertTrue(generatedContent.contains("public Long getId()"));

			Files.write(generatedFile.toPath(),
					("// mio codice custom\r\npublic class Cliente {}\r\n")
							.getBytes(StandardCharsets.UTF_8));

			manager.generateModel(
					"it.test.model",
					metaClasses,
					tempDirectory.getAbsolutePath());

			String preservedContent = new String(Files.readAllBytes(generatedFile.toPath()), StandardCharsets.UTF_8);
			assertTrue(preservedContent.contains("// mio codice custom"));
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
			assertTrue(generatedContent.contains("import jakarta.ejb.Stateless;"));
			assertTrue(generatedContent.contains("@Stateless"));
			assertTrue(generatedContent.contains("public class ClienteRepository extends SimpleRepositoryImpl<Cliente>"));
			assertTrue(generatedContent.contains("public ClienteRepository()"));
			assertTrue(generatedContent.contains("super(Cliente.class);"));
			assertFalse(generatedContent.contains("public boolean update(Cliente entity)"));
			assertFalse(generatedContent.contains("deleteById("));
			assertTrue(simpleRepositoryContent.contains("package it.test.repository;"));
			assertTrue(simpleRepositoryContent.contains("import jakarta.persistence.EntityManager;"));
			assertTrue(simpleRepositoryContent.contains("public interface SimpleRepository<T>"));
			assertTrue(simpleRepositoryContent.contains("boolean update(T entity);"));
			assertTrue(simpleRepositoryContent.contains("T findById(Long id);"));
			assertTrue(simpleRepositoryImplContent.contains("package it.test.repository;"));
			assertTrue(simpleRepositoryImplContent.contains("public class SimpleRepositoryImpl<T> implements SimpleRepository<T>"));
			assertTrue(simpleRepositoryImplContent.contains("import jakarta.persistence.EntityManager;"));
			assertTrue(simpleRepositoryImplContent.contains("import jakarta.persistence.PersistenceContext;"));
			assertTrue(simpleRepositoryImplContent.contains("@PersistenceContext"));
			assertFalse(simpleRepositoryImplContent.contains("JpaUtil"));
			assertTrue(simpleRepositoryImplContent.contains("return this.em;"));
			assertTrue(simpleRepositoryImplContent.contains("public boolean update(T entity)"));
			assertTrue(simpleRepositoryImplContent.contains("this.em.merge(entity);"));
			assertTrue(simpleRepositoryImplContent.contains("return this.findById(id) != null;"));
		} finally {
			this.deleteRecursively(tempDirectory);
		}
	}

	public void testGenerateModelAggiungeAnnotazioniJpaPerRelazioni() throws Exception {
		File tempDirectory = Files.createTempDirectory("model-jpa-relations-test").toFile();
		try {
			FreeMarkerManager manager = new FreeMarkerManager(new IOFittizio());

			MetaClass userMetaClass = new MetaClass();
			userMetaClass.setName("User");
			userMetaClass.setTable("users");

			MetaField userIdField = new MetaField();
			userIdField.setName("id");
			userIdField.setJavaType("Long");
			userMetaClass.addField(userIdField);

			MetaField projectsField = new MetaField();
			projectsField.setName("owner");
			projectsField.setJavaType("Project");
			projectsField.setType("Project");
			projectsField.setRelation(true);
			projectsField.setCollection(true);
			projectsField.setRelationType("ONE_TO_MANY");
			userMetaClass.addField(projectsField);

			MetaClass projectMetaClass = new MetaClass();
			projectMetaClass.setName("Project");
			projectMetaClass.setTable("projects");

			MetaField projectIdField = new MetaField();
			projectIdField.setName("id");
			projectIdField.setJavaType("Long");
			projectMetaClass.addField(projectIdField);

			MetaField ownerField = new MetaField();
			ownerField.setName("owner");
			ownerField.setJavaType("User");
			ownerField.setType("User");
			ownerField.setRelation(true);
			ownerField.setRelationType("MANY_TO_ONE");
			ownerField.setForeignKeyColumn("user_id");
			ownerField.setRequired(true);
			ownerField.setCascadeOnDelete("CASCADE");
			ownerField.addTag("cascade", "MERGE,PERSIST");
			projectMetaClass.addField(ownerField);

			MetaField membersField = new MetaField();
			membersField.setName("members");
			membersField.setJavaType("Tag");
			membersField.setType("Tag");
			membersField.setRelation(true);
			membersField.setCollection(true);
			membersField.setRelationType("MANY_TO_MANY");
			membersField.setJoinTableRequired(true);
			membersField.addTag("cascade", "ALL");
			projectMetaClass.addField(membersField);

			MetaClass tagMetaClass = new MetaClass();
			tagMetaClass.setName("Tag");
			tagMetaClass.setTable("tags");

			MetaField tagIdField = new MetaField();
			tagIdField.setName("id");
			tagIdField.setJavaType("Long");
			tagMetaClass.addField(tagIdField);

			MetaField inverseMembersField = new MetaField();
			inverseMembersField.setName("members");
			inverseMembersField.setJavaType("Project");
			inverseMembersField.setType("Project");
			inverseMembersField.setRelation(true);
			inverseMembersField.setCollection(true);
			inverseMembersField.setRelationType("MANY_TO_MANY");
			inverseMembersField.setJoinTableRequired(true);
			inverseMembersField.addTag("mappedBy", "members");
			tagMetaClass.addField(inverseMembersField);

			Map<String, MetaClass> metaClasses = new LinkedHashMap<String, MetaClass>();
			metaClasses.put(userMetaClass.getName(), userMetaClass);
			metaClasses.put(projectMetaClass.getName(), projectMetaClass);
			metaClasses.put(tagMetaClass.getName(), tagMetaClass);

			manager.generateModel("it.test.model", metaClasses, tempDirectory.getAbsolutePath());

			File projectBaseFile = new File(tempDirectory, "it\\test\\model\\Project.java");
			File userBaseFile = new File(tempDirectory, "it\\test\\model\\User.java");
			File tagBaseFile = new File(tempDirectory, "it\\test\\model\\Tag.java");

			String projectContent = new String(Files.readAllBytes(projectBaseFile.toPath()), StandardCharsets.UTF_8);
			String userContent = new String(Files.readAllBytes(userBaseFile.toPath()), StandardCharsets.UTF_8);
			String tagContent = new String(Files.readAllBytes(tagBaseFile.toPath()), StandardCharsets.UTF_8);

			assertTrue(projectContent.contains("@ManyToOne(fetch = FetchType.LAZY, optional = false, cascade = { CascadeType.MERGE, CascadeType.PERSIST })"));
			assertTrue(projectContent.contains("@Fetch(FetchMode.SELECT)"));
			assertTrue(projectContent.contains("@JoinColumn(name = \"user_id\", nullable = false)"));
			assertTrue(projectContent.contains("@OnDelete(action = OnDeleteAction.CASCADE)"));
			assertTrue(projectContent.contains("@ManyToMany(fetch = FetchType.LAZY, cascade = { CascadeType.ALL })"));
			assertTrue(projectContent.contains("name = \"projects_tags\""));
			assertTrue(projectContent.contains("joinColumns = @JoinColumn(name = \"projects_id\")"));
			assertTrue(projectContent.contains("inverseJoinColumns = @JoinColumn(name = \"tags_id\")"));
			assertTrue(userContent.contains("@OneToMany(mappedBy = \"owner\", fetch = FetchType.LAZY)"));
			assertTrue(userContent.contains("@Fetch(FetchMode.SELECT)"));
			assertTrue(tagContent.contains("@ManyToMany(fetch = FetchType.LAZY, mappedBy = \"members\")"));
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
					"it.test.repository",
					metaClasses,
					tempDirectory.getAbsolutePath());

			File generatedFile = new File(tempDirectory, "it\\test\\repository\\CorsoRepository.java");
			assertTrue("RepositoryGenerated non generato nel path atteso", generatedFile.isFile());
			String generatedContent = new String(Files.readAllBytes(generatedFile.toPath()), StandardCharsets.UTF_8);
			assertTrue(generatedContent.contains("public List<Corso> findByStudenteId(Long id)"));
			assertTrue(generatedContent.contains("WHERE c.studente.id = :id"));
			assertTrue(generatedContent.contains("query.setParameter(\"id\", id);"));
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
					"it.test.repository",
					metaClasses,
					tempDirectory.getAbsolutePath());

			File generatedFile = new File(tempDirectory, "it\\test\\repository\\TaskRepository.java");
			assertTrue("RepositoryGenerated non generato nel path atteso", generatedFile.isFile());
			String generatedContent = new String(Files.readAllBytes(generatedFile.toPath()), StandardCharsets.UTF_8);
			assertTrue(generatedContent.contains("import jakarta.persistence.TypedQuery;"));
			assertTrue(generatedContent.contains("import java.util.Collections;"));
			assertTrue(generatedContent.contains("public List<Task> findByKeyword(String keyword)"));
			assertTrue(generatedContent.contains("LOWER(t.name) LIKE :keyword"));
			assertTrue(generatedContent.contains("public List<Task> findByContainId(Long id)"));
			assertTrue(generatedContent.contains("WHERE t.contain.id = :id"));
			assertFalse(generatedContent.contains("public boolean update(Task entity)"));
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
			assertTrue(generatedContent.contains("import jakarta.ejb.Stateless;"));
			assertTrue(generatedContent.contains("import jakarta.inject.Inject;"));
			assertTrue(generatedContent.contains("import it.test.model.Cliente;"));
			assertTrue(generatedContent.contains("import it.test.repository.ClienteRepository;"));
			assertTrue(generatedContent.contains("@Stateless"));
			assertTrue(generatedContent.contains("public class ClienteServiceBase"));
			assertTrue(generatedContent.contains("@Inject"));
			assertTrue(generatedContent.contains("protected ClienteRepository repository;"));
			assertTrue(generatedContent.contains("private void validateEntityForWrite(Cliente entity)"));
			assertTrue(generatedContent.contains("if (entity == null)"));
			assertTrue(generatedContent.contains("throw new IllegalArgumentException(\"Cliente obbligatorio\");"));
			assertTrue(generatedContent.contains("this.prepareForCreate(entity);"));
			assertTrue(generatedContent.contains("this.prepareForUpdate(entity);"));
			assertTrue(generatedContent.contains("throw new IllegalArgumentException(\"Id obbligatorio per l'aggiornamento di Cliente\");"));
			assertTrue(generatedContent.contains("public List<Cliente> findByKeyword(String keyword)"));
			assertTrue(generatedContent.contains("return this.repository.findByKeyword(keyword);"));
			assertTrue(generatedContent.contains("Cliente entity = this.repository.findById(id);"));
			assertTrue(generatedContent.contains("this.repository.delete(entity);"));
			assertTrue(wrapperContent.contains("import jakarta.ejb.Stateless;"));
			assertTrue(wrapperContent.contains("@Stateless"));
			assertTrue(wrapperContent.contains("public class ClienteService extends ClienteServiceBase"));
			assertFalse(wrapperContent.contains("public ClienteService("));
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
			assertTrue(generatedContent.contains("@Inject"));
			assertTrue(generatedContent.contains("protected StudenteRepository studenteRepository;"));
			assertTrue(generatedContent.contains("Studente studente = entity.getStudente();"));
			assertTrue(generatedContent.contains("if (studente != null)"));
			assertTrue(generatedContent.contains("if (studente.getId() == null)"));
			assertTrue(generatedContent.contains("if (!this.studenteRepository.existingById(studente.getId()))"));
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
			assertTrue(generatedContent.contains("@Inject"));
			assertTrue(generatedContent.contains("protected ProjectRepository projectRepository;"));
			assertTrue(generatedContent.contains("Project contain = entity.getContain();"));
			assertTrue(generatedContent.contains("if (contain == null || contain.getId() == null)"));
			assertTrue(generatedContent.contains("throw new IllegalArgumentException(\"Project associato obbligatorio per Task\");"));
			assertTrue(generatedContent.contains("if (!this.projectRepository.existingById(contain.getId()))"));
			assertTrue(generatedContent.contains("throw new IllegalArgumentException(\"Project associato non esistente: \""));
			assertTrue(generatedContent.contains("+ contain.getId());"));
			assertTrue(generatedContent.contains("this.validateEntityForWrite(entity);"));
		} finally {
			this.deleteRecursively(tempDirectory);
		}
	}

	public void testGenerateDtoCreaClasseAutonoma() throws Exception {
		File tempDirectory = Files.createTempDirectory("dto-template-test").toFile();
		try {
			FreeMarkerManager manager = new FreeMarkerManager(new IOFittizio());
			MetaClass taskMetaClass = new MetaClass();
			taskMetaClass.setName("Task");

			MetaField idField = new MetaField();
			idField.setName("id");
			idField.setJavaType("Long");
			taskMetaClass.addField(idField);

			Map<String, MetaClass> metaClasses = new LinkedHashMap<String, MetaClass>();
			metaClasses.put(taskMetaClass.getName(), taskMetaClass);

			manager.generateDto(
					"it.test.model",
					"it.test.dto",
					metaClasses,
					tempDirectory.getAbsolutePath());

			File generatedFile = new File(tempDirectory, "it\\test\\dto\\TaskDto.java");
			assertTrue("Dto non generato nel path atteso", generatedFile.isFile());

			String generatedContent = new String(Files.readAllBytes(generatedFile.toPath()), StandardCharsets.UTF_8);
			assertTrue(generatedContent.contains("package it.test.dto;"));
			assertTrue(generatedContent.contains("public class TaskDto"));
			assertFalse(generatedContent.contains("extends Task"));
			assertTrue(generatedContent.contains("private Long id;"));
			assertTrue(generatedContent.contains("public Long getId()"));
			assertTrue(generatedContent.contains("public TaskDto()"));
		} finally {
			this.deleteRecursively(tempDirectory);
		}
	}

	public void testGenerateDtoUsaLongPerLeRelazioni() throws Exception {
		File tempDirectory = Files.createTempDirectory("dto-relations-template-test").toFile();
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

			MetaField projectField = new MetaField();
			projectField.setName("project");
			projectField.setJavaType("Project");
			projectField.setRelation(true);
			projectField.setRelationType("MANY_TO_ONE");
			taskMetaClass.addField(projectField);

			Map<String, MetaClass> metaClasses = new LinkedHashMap<String, MetaClass>();
			metaClasses.put(projectMetaClass.getName(), projectMetaClass);
			metaClasses.put(taskMetaClass.getName(), taskMetaClass);

			manager.generateDto(
					"it.test.model",
					"it.test.dto",
					metaClasses,
					tempDirectory.getAbsolutePath());

			File generatedFile = new File(tempDirectory, "it\\test\\dto\\TaskDto.java");
			assertTrue("Dto non generato nel path atteso", generatedFile.isFile());

			String generatedContent = new String(Files.readAllBytes(generatedFile.toPath()), StandardCharsets.UTF_8);
			assertFalse(generatedContent.contains("import it.test.model.Project;"));
			assertTrue(generatedContent.contains("private Long project;"));
			assertTrue(generatedContent.contains("public Long getProject()"));
			assertTrue(generatedContent.contains("public void setProject(Long project)"));
		} finally {
			this.deleteRecursively(tempDirectory);
		}
	}

	public void testGenerateDtoUsaListaDiLongPerRelazioniCollezione() throws Exception {
		File tempDirectory = Files.createTempDirectory("dto-collection-relations-template-test").toFile();
		try {
			FreeMarkerManager manager = new FreeMarkerManager(new IOFittizio());

			MetaClass tagMetaClass = new MetaClass();
			tagMetaClass.setName("Tag");
			MetaField tagIdField = new MetaField();
			tagIdField.setName("id");
			tagIdField.setJavaType("Long");
			tagMetaClass.addField(tagIdField);

			MetaClass projectMetaClass = new MetaClass();
			projectMetaClass.setName("Project");
			MetaField projectIdField = new MetaField();
			projectIdField.setName("id");
			projectIdField.setJavaType("Long");
			projectMetaClass.addField(projectIdField);

			MetaField tagsField = new MetaField();
			tagsField.setName("tags");
			tagsField.setJavaType("Tag");
			tagsField.setRelation(true);
			tagsField.setCollection(true);
			tagsField.setRelationType("MANY_TO_MANY");
			projectMetaClass.addField(tagsField);

			Map<String, MetaClass> metaClasses = new LinkedHashMap<String, MetaClass>();
			metaClasses.put(tagMetaClass.getName(), tagMetaClass);
			metaClasses.put(projectMetaClass.getName(), projectMetaClass);

			manager.generateDto(
					"it.test.model",
					"it.test.dto",
					metaClasses,
					tempDirectory.getAbsolutePath());

			File generatedFile = new File(tempDirectory, "it\\test\\dto\\ProjectDto.java");
			assertTrue("Dto non generato nel path atteso", generatedFile.isFile());

			String generatedContent = new String(Files.readAllBytes(generatedFile.toPath()), StandardCharsets.UTF_8);
			assertTrue(generatedContent.contains("import java.util.List;"));
			assertTrue(generatedContent.contains("private List<Long> tags = new ArrayList<>();"));
			assertTrue(generatedContent.contains("public List<Long> getTags()"));
			assertTrue(generatedContent.contains("public void setTags(List<Long> tags)"));
		} finally {
			this.deleteRecursively(tempDirectory);
		}
	}

	public void testGenerateControllerConverteRelazioniDtoInId() throws Exception {
		File tempDirectory = Files.createTempDirectory("controller-dto-relations-template-test").toFile();
		try {
			FreeMarkerManager manager = new FreeMarkerManager(new IOFittizio());

			MetaClass projectMetaClass = new MetaClass();
			projectMetaClass.setName("Project");
			MetaField projectIdField = new MetaField();
			projectIdField.setName("id");
			projectIdField.setJavaType("Long");
			projectMetaClass.addField(projectIdField);

			MetaClass tagMetaClass = new MetaClass();
			tagMetaClass.setName("Tag");
			MetaField tagIdField = new MetaField();
			tagIdField.setName("id");
			tagIdField.setJavaType("Long");
			tagMetaClass.addField(tagIdField);

			MetaClass taskMetaClass = new MetaClass();
			taskMetaClass.setName("Task");
			MetaField taskIdField = new MetaField();
			taskIdField.setName("id");
			taskIdField.setJavaType("Long");
			taskMetaClass.addField(taskIdField);

			MetaField projectField = new MetaField();
			projectField.setName("project");
			projectField.setJavaType("Project");
			projectField.setRelation(true);
			projectField.setRelationType("MANY_TO_ONE");
			projectField.setForeignKeyColumn("project_id");
			taskMetaClass.addField(projectField);

			MetaField tagsField = new MetaField();
			tagsField.setName("tags");
			tagsField.setJavaType("Tag");
			tagsField.setRelation(true);
			tagsField.setCollection(true);
			tagsField.setRelationType("MANY_TO_MANY");
			taskMetaClass.addField(tagsField);

			Map<String, MetaClass> metaClasses = new LinkedHashMap<String, MetaClass>();
			metaClasses.put(projectMetaClass.getName(), projectMetaClass);
			metaClasses.put(tagMetaClass.getName(), tagMetaClass);
			metaClasses.put(taskMetaClass.getName(), taskMetaClass);

			manager.generateController(
					"it.test.model",
					"it.test.dto",
					"it.test.service",
					"it.test.controller",
					metaClasses,
					tempDirectory.getAbsolutePath());

			File generatedFile = new File(tempDirectory, "it\\test\\controller\\TaskControllerBase.java");
			assertTrue("ControllerGenerated non generato nel path atteso", generatedFile.isFile());

			String generatedContent = new String(Files.readAllBytes(generatedFile.toPath()), StandardCharsets.UTF_8);
			assertTrue(generatedContent.contains("import it.test.model.Project;"));
			assertTrue(generatedContent.contains("import it.test.model.Tag;"));
			assertTrue(generatedContent.contains("relationEntity.setId(dto.getProject());"));
			assertTrue(generatedContent.contains("for (Long relationId : dto.getTags())"));
			assertTrue(generatedContent.contains("relationEntity.setId(relationId);"));
			assertFalse(generatedContent.contains("dto.setProject(entity.getProject()"));
			assertFalse(generatedContent.contains("dto.setTags(tagsIds);"));
			assertFalse(generatedContent.contains("entity.getTags()"));
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
					"it.test.dto",
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
			assertTrue(generatedContent.contains("import jakarta.inject.Inject;"));
			assertTrue(generatedContent.contains("import jakarta.ws.rs.Path;"));
			assertTrue(generatedContent.contains("import jakarta.ws.rs.core.Response;"));
			assertTrue(generatedContent.contains("import java.util.Optional;"));
			assertTrue(generatedContent.contains("import it.test.model.Task;"));
			assertTrue(generatedContent.contains("import it.test.dto.TaskDto;"));
			assertTrue(generatedContent.contains("import it.test.service.TaskService;"));
			assertTrue(generatedContent.contains("public abstract class TaskControllerBase"));
			assertTrue(generatedContent.contains("@Inject"));
			assertTrue(generatedContent.contains("protected TaskService taskService;"));
			assertTrue(generatedContent.contains("public Response getAllTasks()"));
			assertTrue(generatedContent.contains("return Response.ok(this.toDtoList(tasks)).build();"));
			assertTrue(generatedContent.contains("public Response countTasks()"));
			assertTrue(generatedContent.contains("public Response existsTask(@PathParam(\"id\") Long id)"));
			assertTrue(generatedContent.contains("return Response.ok(this.taskService.existsById(id)).build();"));
			assertTrue(generatedContent.contains("public Response findByKeyword(@QueryParam(\"keyword\") String keyword)"));
			assertTrue(generatedContent.contains("return Response.ok(this.toDtoList(this.taskService.findByKeyword(keyword))).build();"));
			assertTrue(generatedContent.contains("public Response getTask(@PathParam(\"id\") Long id)"));
			assertTrue(generatedContent.contains("Optional<Task> task = this.taskService.findById(id);"));
			assertTrue(generatedContent.contains("if (!task.isPresent())"));
			assertTrue(generatedContent.contains("return Response.ok(this.toDto(task.get())).build();"));
			assertTrue(generatedContent.contains("public Response createTask(TaskDto task)"));
			assertTrue(generatedContent.contains("Task created = this.taskService.save(this.toEntity(task));"));
			assertTrue(generatedContent.contains("return Response.created(URI.create(\"/api/tasks/\" + created.getId())).entity(this.toDto(created)).build();"));
			assertTrue(generatedContent.contains("public Response updateTask(@PathParam(\"id\") Long id, TaskDto task)"));
			assertTrue(generatedContent.contains("task.setId(id);"));
			assertTrue(generatedContent.contains("Task entity = this.toEntity(task);"));
			assertTrue(generatedContent.contains("boolean updated = this.taskService.update(entity);"));
			assertTrue(generatedContent.contains("return Response.ok(this.toDto(entity)).build();"));
			assertTrue(generatedContent.contains("public Response deleteAll()"));
			assertTrue(generatedContent.contains("public Response deleteById(@PathParam(\"id\") Long id)"));
			assertTrue(generatedContent.contains("boolean deleted = this.taskService.delete(id);"));
			assertTrue(generatedContent.contains("protected TaskDto toDto(Task entity)"));
			assertTrue(generatedContent.contains("protected Task toEntity(TaskDto dto)"));
			assertTrue(wrapperContent.contains("import jakarta.ws.rs.Path;"));
			assertTrue(wrapperContent.contains("import jakarta.ws.rs.Produces;"));
			assertTrue(wrapperContent.contains("import jakarta.ws.rs.Consumes;"));
			assertTrue(wrapperContent.contains("import jakarta.ws.rs.core.MediaType;"));
			assertTrue(wrapperContent.contains("import jakarta.enterprise.context.RequestScoped;"));
			assertTrue(wrapperContent.contains("@Path(\"/tasks\")"));
			assertTrue(wrapperContent.contains("@Produces(MediaType.APPLICATION_JSON)"));
			assertTrue(wrapperContent.contains("@Consumes(MediaType.APPLICATION_JSON)"));
			assertTrue(wrapperContent.contains("@RequestScoped"));
			assertTrue(wrapperContent.contains("public class TaskController extends TaskControllerBase"));
			assertFalse(wrapperContent.contains("public TaskController("));
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
					"it.test.dto",
					"it.test.service",
					"it.test.controller",
					metaClasses,
					tempDirectory.getAbsolutePath());

			File generatedFile = new File(tempDirectory, "it\\test\\controller\\TaskControllerBase.java");
			assertTrue("ControllerGenerated non generato nel path atteso", generatedFile.isFile());

			String generatedContent = new String(Files.readAllBytes(generatedFile.toPath()), StandardCharsets.UTF_8);
			assertTrue(generatedContent.contains("@Path(\"/by-contain/{containId}\")"));
			assertTrue(generatedContent.contains("public Response findByContainId(@PathParam(\"containId\") Long containId)"));
			assertTrue(generatedContent.contains("return Response.ok(this.toDtoList(this.taskService.findByContainId(containId))).build();"));
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

	public void testGenerateSchemaScriveCascadeSulleForeignKey() throws Exception {
		File tempFile = Files.createTempFile("schema-cascade-test", ".sql").toFile();
		try {
			FreeMarkerManager manager = new FreeMarkerManager(new IOFittizio());

			MetaClass userMetaClass = new MetaClass();
			userMetaClass.setName("User");
			userMetaClass.setTable("users");

			MetaField userIdField = new MetaField();
			userIdField.setName("id");
			userIdField.setJavaType("Long");
			userIdField.setSqlType("BIGINT");
			userMetaClass.addField(userIdField);

			MetaClass projectMetaClass = new MetaClass();
			projectMetaClass.setName("Project");
			projectMetaClass.setTable("projects");

			MetaField projectIdField = new MetaField();
			projectIdField.setName("id");
			projectIdField.setJavaType("Long");
			projectIdField.setSqlType("BIGINT");
			projectMetaClass.addField(projectIdField);

			MetaField ownerField = new MetaField();
			ownerField.setName("owner");
			ownerField.setType("User");
			ownerField.setJavaType("User");
			ownerField.setRelation(true);
			ownerField.setRelationType("MANY_TO_ONE");
			ownerField.setForeignKeyColumn("user_id");
			ownerField.setCascadeOnDelete("CASCADE");
			ownerField.setCascadeOnUpdate("RESTRICT");
			projectMetaClass.addField(ownerField);

			Map<String, MetaClass> metaClasses = new LinkedHashMap<String, MetaClass>();
			metaClasses.put(userMetaClass.getName(), userMetaClass);
			metaClasses.put(projectMetaClass.getName(), projectMetaClass);

			manager.generateSchema(metaClasses, tempFile.getAbsolutePath());

			String generatedContent = new String(Files.readAllBytes(tempFile.toPath()), StandardCharsets.UTF_8);
			assertTrue(generatedContent.contains("FOREIGN KEY (user_id) REFERENCES users(id)"));
			assertTrue(generatedContent.contains("ON DELETE CASCADE"));
			assertTrue(generatedContent.contains("ON UPDATE RESTRICT"));
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
