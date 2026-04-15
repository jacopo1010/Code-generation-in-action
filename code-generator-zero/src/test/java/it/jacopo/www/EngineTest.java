package it.jacopo.www;

import java.util.Properties;

import it.jacopo.www.loader.PropertiesCostanti;
import junit.framework.TestCase;

public class EngineTest extends TestCase {

	public void testJavaUsaIlPathComuneQuandoConfigurato() {
		Properties properties = new Properties();
		properties.setProperty(PropertiesCostanti.JAVA_OUTPUT_PATH, "src/main/java");

		String resolved = FileUtil.resolveJavaOutputPath(
				"C:\\workspace\\project\\application.properties",
				properties.getProperty(PropertiesCostanti.JAVA_OUTPUT_PATH));

		assertEquals("C:\\workspace\\project\\src\\main\\java", resolved);
	}

	public void testJavaAccettaAnchePathAssoluto() {
		String resolved = FileUtil.resolveJavaOutputPath(
				"C:\\workspace\\project\\application.properties",
				"D:\\generated\\src\\main\\java");

		assertEquals("D:\\generated\\src\\main\\java", resolved);
	}

	public void testJavaLanciaErroreSePathComuneManca() {
		try {
			FileUtil.resolveJavaOutputPath(
					"C:\\workspace\\project\\application.properties",
					null);
			fail("Attesa IllegalArgumentException quando manca il path java comune");
		} catch (IllegalArgumentException e) {
			assertEquals(
					"Definire nell'application.properties la proprietà " + PropertiesCostanti.JAVA_OUTPUT_PATH,
					e.getMessage());
		}
	}

	public void testJavaNormalizzaPathRelativo() {
		String resolved = FileUtil.resolveJavaOutputPath(
				"C:\\workspace\\project\\config\\application.properties",
				"..\\src\\main\\java");

		assertEquals("C:\\workspace\\project\\src\\main\\java", resolved);
	}
}
