package it.jacopo.www.generator;

import java.io.File;
import java.lang.reflect.Method;

import it.jacopo.www.io.IO;
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

	private static final class IOFittizio implements IO {

		@Override
		public void stampaMessaggio(String msf) {
		}
	}
}
