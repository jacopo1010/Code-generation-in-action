package it.jacopo.www.comandi;

import it.jacopo.www.io.IO;

public interface FabbricaDiComandi {

	public Comando costruisciComando(String[] args,IO Io);
	
}
