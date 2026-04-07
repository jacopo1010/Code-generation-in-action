package it.jacopo.www.comandi;

import javax.print.DocFlavor.STRING;

import it.jacopo.www.Engine;
import it.jacopo.www.io.IO;

public class ComandoInizializzaConfigurazioni implements Comando {

	private IO io;
	private String pathString;
	
	public ComandoInizializzaConfigurazioni(IO io, String pathString) {
		this.io = io;
		this.pathString = pathString;
	}
	
	@Override
	public void esegui(Engine engine) {
		/* qui posso domandare se usare hibernate oppure no e in base alla risposta eseguire qualcosa*/
		io.stampaMessaggio("Generazione modelli");
		engine.generate(this.pathString);  
	}

}
