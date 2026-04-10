package it.jacopo.www.comandi;

import it.jacopo.www.Engine;
import it.jacopo.www.io.IO;

public class ComandoNonValido implements Comando {

	private IO io;
	private String messaggio;
	
	public ComandoNonValido(IO io) {
		this(io, "COMANDO NON VALIDO!");
	}

	public ComandoNonValido(IO io, String messaggio) {
		this.io = io;
		this.messaggio = messaggio;
	}
	
	@Override
	public void esegui(Engine engine) {
	    io.stampaMessaggio(this.messaggio);
	}

}
