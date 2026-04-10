package it.jacopo.www.loader;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;

import junit.framework.TestCase;

public class CaricatoreDiFileImplTest extends TestCase {

	private File tempDirectory;

	@Override
	protected void setUp() throws Exception {
		this.tempDirectory = Files.createTempDirectory("code-generator-zero-test").toFile();
	}

	@Override
	protected void tearDown() throws Exception {
		this.deleteRecursively(this.tempDirectory);
	}

	public void testRisolvePathRelativoDelModello() throws Exception {
		File modelFile = new File(this.tempDirectory, "domain.xmi");
		this.scrivi(modelFile, "<xmi:XMI/>");

		File propertiesFile = new File(this.tempDirectory, "application.properties");
		this.scrivi(propertiesFile, PropertiesCostanti.MODEL_DOMAIN_PATH + "=domain.xmi");

		CaricatoreDiFileImpl loader = new CaricatoreDiFileImpl();

		File loaded = loader.carica(propertiesFile.getAbsolutePath());

		assertEquals(modelFile.getCanonicalFile(), loaded.getCanonicalFile());
	}

	public void testRifiutaEstensioneNonSupportata() throws Exception {
		File modelFile = new File(this.tempDirectory, "domain.txt");
		this.scrivi(modelFile, "not xml");

		File propertiesFile = new File(this.tempDirectory, "application.properties");
		this.scrivi(propertiesFile, PropertiesCostanti.MODEL_DOMAIN_PATH + "=domain.txt");

		CaricatoreDiFileImpl loader = new CaricatoreDiFileImpl();

		try {
			loader.carica(propertiesFile.getAbsolutePath());
			fail("Mi aspettavo un'eccezione per estensione non valida");
		} catch (RuntimeException e) {
			assertTrue(e.getCause().getMessage().contains("XML/XMI valido"));
		}
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
}
