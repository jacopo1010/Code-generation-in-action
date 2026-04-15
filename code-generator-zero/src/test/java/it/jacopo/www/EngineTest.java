package it.jacopo.www;

import java.util.Properties;

import it.jacopo.www.loader.CaricatoreDiFile;
import it.jacopo.www.loader.PropertiesCostanti;
import junit.framework.TestCase;

public class EngineTest extends TestCase {

	public void testDaoUsaIlPathDedicatoQuandoConfigurato() {
		Properties properties = new Properties();
		properties.setProperty(PropertiesCostanti.DAO_OUTPUT_PATH, "src/main/java");
		properties.setProperty(PropertiesCostanti.MODEL_OUTPUT_PATH, "generated/model");

		String resolved = FileUtil.resolveDaoOutputPath(
				"C:\\workspace\\project\\application.properties",
				new LoaderStub(properties));

		assertEquals("C:\\workspace\\project\\src\\main\\java", resolved);
	}

	public void testDaoFaFallbackSulPathDeiModel() {
		Properties properties = new Properties();
		properties.setProperty(PropertiesCostanti.MODEL_OUTPUT_PATH, "src/main/java");

		String resolved = FileUtil.resolveDaoOutputPath(
				"C:\\workspace\\project\\application.properties",
				new LoaderStub(properties));

		assertEquals("C:\\workspace\\project\\src\\main\\java", resolved);
	}

	public void testServiceUsaIlPathDedicatoQuandoConfigurato() {
		Properties properties = new Properties();
		properties.setProperty(PropertiesCostanti.SERVICE_OUTPUT_PATH, "src/main/service");
		properties.setProperty(PropertiesCostanti.DAO_OUTPUT_PATH, "src/main/dao");

		String resolved = FileUtil.resolveServiceOutputPath(
				"C:\\workspace\\project\\application.properties",
				new LoaderStub(properties));

		assertEquals("C:\\workspace\\project\\src\\main\\service", resolved);
	}

	public void testServiceFaFallbackSulPathDeiDao() {
		Properties properties = new Properties();
		properties.setProperty(PropertiesCostanti.DAO_OUTPUT_PATH, "src/main/java");

		String resolved = FileUtil.resolveServiceOutputPath(
				"C:\\workspace\\project\\application.properties",
				new LoaderStub(properties));

		assertEquals("C:\\workspace\\project\\src\\main\\java", resolved);
	}

	private static final class LoaderStub implements CaricatoreDiFile {

		private final Properties properties;

		private LoaderStub(Properties properties) {
			this.properties = properties;
		}

		@Override
		public java.io.File carica(String fileName) {
			return null;
		}

		@Override
		public Properties getApplicationProperties() {
			return this.properties;
		}
	}
}
