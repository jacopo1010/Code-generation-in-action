package it.jacopo.www.generator;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;

import it.jacopo.www.io.IO;
import it.jacopo.www.model.MetaClass;
import it.jacopo.www.model.MetaField;
import junit.framework.TestCase;

public class GeneratoreDiMetaClassTest extends TestCase {

	private File tempDirectory;

	@Override
	protected void setUp() throws Exception {
		this.tempDirectory = Files.createTempDirectory("meta-class-generator-test").toFile();
	}

	@Override
	protected void tearDown() throws Exception {
		this.deleteRecursively(this.tempDirectory);
	}

	public void testRiconosceManyToOneAncheSeAssociazioneEDefinitaNellAltraClasse() throws Exception {
		File xmiFile = new File(this.tempDirectory, "domain.xmi");
		this.scrivi(xmiFile,
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
				+ "<xmi:XMI xmi:version=\"2.1\" xmlns:uml=\"http://schema.omg.org/spec/UML/2.0\" xmlns:xmi=\"http://schema.omg.org/spec/XMI/2.1\">\n"
				+ "  <uml:Model xmi:id=\"model1\" xmi:type=\"uml:Model\" name=\"RootModel\">\n"
				+ "    <packagedElement xmi:id=\"pkg1\" name=\"Model\" xmi:type=\"uml:Model\">\n"
				+ "      <packagedElement xmi:id=\"userClass\" name=\"User\" xmi:type=\"uml:Class\">\n"
				+ "        <ownedMember xmi:id=\"assoc1\" name=\"Ownership\" xmi:type=\"uml:Association\">\n"
				+ "          <ownedEnd xmi:id=\"userEnd\" xmi:type=\"uml:Property\" type=\"userClass\">\n"
				+ "            <lowerValue xmi:id=\"l1\" xmi:type=\"uml:LiteralInteger\" value=\"1\"/>\n"
				+ "            <upperValue xmi:id=\"u1\" xmi:type=\"uml:LiteralInteger\" value=\"1\"/>\n"
				+ "          </ownedEnd>\n"
				+ "          <ownedEnd xmi:id=\"projectEnd\" xmi:type=\"uml:Property\" type=\"projectClass\">\n"
				+ "            <lowerValue xmi:id=\"l2\" xmi:type=\"uml:LiteralUnlimitedNatural\" value=\"*\"/>\n"
				+ "            <upperValue xmi:id=\"u2\" xmi:type=\"uml:LiteralUnlimitedNatural\" value=\"*\"/>\n"
				+ "          </ownedEnd>\n"
				+ "        </ownedMember>\n"
				+ "        <ownedAttribute xmi:id=\"userId\" name=\"id\" type=\"long_id\" xmi:type=\"uml:Property\"/>\n"
				+ "      </packagedElement>\n"
				+ "      <packagedElement xmi:id=\"projectClass\" name=\"Project\" xmi:type=\"uml:Class\">\n"
				+ "        <ownedAttribute xmi:id=\"projectId\" name=\"id\" type=\"long_id\" xmi:type=\"uml:Property\"/>\n"
				+ "      </packagedElement>\n"
				+ "    </packagedElement>\n"
				+ "    <packagedElement xmi:id=\"long_id\" xmi:type=\"uml:DataType\" name=\"long\"/>\n"
				+ "  </uml:Model>\n"
				+ "</xmi:XMI>");

		GeneratoreDiMetaClass generator = new GeneratoreDiMetaClass(new IOFittizio());

		Map<String, MetaClass> result = generator.generaMetaClass(xmiFile);

		MetaClass projectMetaClass = result.get("Project");
		assertNotNull(projectMetaClass);

		MetaField ownershipField = projectMetaClass.getFields().get("Ownership");
		assertNotNull("La relazione MANY_TO_ONE su Project deve essere presente", ownershipField);
		assertTrue(ownershipField.isRelation());
		assertEquals("MANY_TO_ONE", ownershipField.getRelationType());
		assertEquals("user_id", ownershipField.getForeignKeyColumn());
	}

	public void testNormalizzaDataTypeStarUmlMaiuscoliEListaComeCollezione() throws Exception {
		File xmiFile = new File(this.tempDirectory, "datatype-domain.xmi");
		this.scrivi(xmiFile,
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
				+ "<xmi:XMI xmi:version=\"2.1\" xmlns:uml=\"http://schema.omg.org/spec/UML/2.0\" xmlns:xmi=\"http://schema.omg.org/spec/XMI/2.1\">\n"
				+ "  <uml:Model xmi:id=\"model1\" xmi:type=\"uml:Model\" name=\"RootModel\">\n"
				+ "    <packagedElement xmi:id=\"pkg1\" name=\"Model\" xmi:type=\"uml:Model\">\n"
				+ "      <packagedElement xmi:id=\"studentClass\" name=\"Studente\" xmi:type=\"uml:Class\">\n"
				+ "        <ownedAttribute xmi:id=\"studentId\" name=\"id\" type=\"long_id\" xmi:type=\"uml:Property\"/>\n"
				+ "        <ownedAttribute xmi:id=\"etaId\" name=\"eta\" type=\"int_id\" xmi:type=\"uml:Property\"/>\n"
				+ "        <ownedAttribute xmi:id=\"coursesId\" name=\"corsiIscritto\" type=\"list_id\" xmi:type=\"uml:Property\"/>\n"
				+ "      </packagedElement>\n"
				+ "    </packagedElement>\n"
				+ "    <packagedElement xmi:id=\"long_id\" xmi:type=\"uml:DataType\" name=\"Long\"/>\n"
				+ "    <packagedElement xmi:id=\"int_id\" xmi:type=\"uml:DataType\" name=\"int\"/>\n"
				+ "    <packagedElement xmi:id=\"list_id\" xmi:type=\"uml:DataType\" name=\"List\"/>\n"
				+ "  </uml:Model>\n"
				+ "</xmi:XMI>");

		GeneratoreDiMetaClass generator = new GeneratoreDiMetaClass(new IOFittizio());

		Map<String, MetaClass> result = generator.generaMetaClass(xmiFile);

		MetaClass studenteMetaClass = result.get("Studente");
		assertNotNull(studenteMetaClass);

		MetaField idField = studenteMetaClass.getFields().get("id");
		assertNotNull(idField);
		assertEquals("Long", idField.getJavaType());
		assertEquals("BIGINT", idField.getSqlType());

		MetaField etaField = studenteMetaClass.getFields().get("eta");
		assertNotNull(etaField);
		assertEquals("Integer", etaField.getJavaType());
		assertEquals("INTEGER", etaField.getSqlType());

		MetaField coursesField = studenteMetaClass.getFields().get("corsiIscritto");
		assertNotNull(coursesField);
		assertTrue(coursesField.isCollection());
		assertEquals("Object", coursesField.getJavaType());
		assertNull(coursesField.getSqlType());
	}

	private void scrivi(File file, String contenuto) throws IOException {
		try (FileWriter writer = new FileWriter(file)) {
			writer.write(contenuto);
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
