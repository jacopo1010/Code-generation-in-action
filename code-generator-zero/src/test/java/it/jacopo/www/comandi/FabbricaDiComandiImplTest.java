package it.jacopo.www.comandi;

import it.jacopo.www.io.IO;
import junit.framework.TestCase;

public class FabbricaDiComandiImplTest extends TestCase {

	public void testRitornaComandoNonValidoSeArgsAssenti() {
		FabbricaDiComandiImpl factory = new FabbricaDiComandiImpl();

		Comando comando = factory.costruisciComando(new String[0], new IOFittizio());

		assertTrue(comando instanceof ComandoNonValido);
	}

	public void testRitornaComandoConfigurazioneSePathPresente() {
		FabbricaDiComandiImpl factory = new FabbricaDiComandiImpl();

		Comando comando = factory.costruisciComando(
				new String[] { ComandiCostanti.CONFIG, "config/application.properties" },
				new IOFittizio());

		assertTrue(comando instanceof ComandoInizializzaConfigurazioni);
	}

	private static final class IOFittizio implements IO {

		@Override
		public void stampaMessaggio(String msf) {
		}
	}
}
