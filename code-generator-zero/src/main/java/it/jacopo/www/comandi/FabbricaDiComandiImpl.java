package it.jacopo.www.comandi;

import java.util.Arrays;
import java.util.List;

import it.jacopo.www.io.IO;

public class FabbricaDiComandiImpl implements FabbricaDiComandi {

	@Override
	public Comando costruisciComando(String[] args, IO io) {
		List<String> input = Arrays.asList(args);
		String istruzione = input.get(0);
		Comando comando = null;
		if (istruzione != null) {
			switch (istruzione) {
			case ComandiCostanti.CONFIG:
				String parametro = input.get(1);
				if(parametro == null || parametro.isEmpty()) throw new IllegalArgumentException("Devi inserire il path "
						+ "del file application.properties fai copia e incolla! ");
				comando = new ComandoInizializzaConfigurazioni(io,parametro);
				break;
			default:
				comando = new ComandoNonValido(io);
				break;
			}
		}
		return comando;
	}

	
}
