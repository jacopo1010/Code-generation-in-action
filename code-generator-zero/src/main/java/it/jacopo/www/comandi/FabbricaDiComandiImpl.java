package it.jacopo.www.comandi;

import java.util.Arrays;
import java.util.List;

import it.jacopo.www.io.IO;

public class FabbricaDiComandiImpl implements FabbricaDiComandi {

	@Override
	public Comando costruisciComando(String[] args, IO io) {
		if (args == null || args.length == 0) {
			return new ComandoNonValido(io,"Nessun comando specificato. Uso: generate <path-application-properties>");
		}

		List<String> input = Arrays.asList(args);
		String istruzione = input.get(0);
		if (istruzione == null || istruzione.trim().isEmpty()) {
			return new ComandoNonValido(io,"Comando vuoto. Uso: generate <path-application-properties>");
		}

		switch (istruzione) {
		case ComandiCostanti.CONFIG:
			if (input.size() < 2 || input.get(1) == null || input.get(1).trim().isEmpty()) {
				return new ComandoNonValido(io,"Devi inserire il path del file application.properties. Uso: generate <path-application-properties>");
			}
			return new ComandoInizializzaConfigurazioni(io, input.get(1).trim());
		default:
			return new ComandoNonValido(io,"Comando non valido: " + istruzione + ". Uso: generate <path-application-properties>");
		}
	}

	
}
