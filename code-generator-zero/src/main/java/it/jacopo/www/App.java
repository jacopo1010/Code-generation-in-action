package it.jacopo.www;

import java.lang.reflect.Array;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.print.attribute.standard.MediaSize.Engineering;

import it.jacopo.www.comandi.Comando;
import it.jacopo.www.comandi.FabbricaDiComandi;
import it.jacopo.www.comandi.FabbricaDiComandiImpl;
import it.jacopo.www.generator.GeneratoreDiEntita;
import it.jacopo.www.io.IO;
import it.jacopo.www.io.IOConsole;

/**
 * Hello world!
 *
 */
public class App 
{
    private IO console;
	private Engine engine;
    
    public App(IO console) {
    	this.engine = new Engine(console);
    	this.console = console;
    }
    
	public IO getConsole() {
		return console;
	}

	public void setConsole(IO console) {
		this.console = console;
	}

	public Engine getEngine() {
		return engine;
	}

	public void setEngine(Engine engine) {
		this.engine = engine;
	}

    public void avvia(String... args) {
    	FabbricaDiComandi fabbricaDiComandi = new FabbricaDiComandiImpl();
		Comando comando = fabbricaDiComandi.costruisciComando(args, this.console);
		comando.esegui(this.engine);
    }
	
	
	public static void main( String[] args )
	{
		long startTime = System.currentTimeMillis();
		IO console = new IOConsole();
		App app = new App(console);
		app.avvia(args);
		long timeDiff = System.currentTimeMillis() - startTime;
	    System.out.println("Elapsed time in milliseconds: " + timeDiff);
	}
}
