package it.jacopo.www.comandi;

import it.jacopo.www.Engine;
import it.jacopo.www.io.IO;

public class ComandoNonValido implements Comando {

	private IO io;
	
	public ComandoNonValido(IO io) {
		this.io = io;
	}
	
	@Override
	public void esegui(Engine engine) {
	    io.stampaMessaggio("COMANDO NON VALIDO!");
	}

}
