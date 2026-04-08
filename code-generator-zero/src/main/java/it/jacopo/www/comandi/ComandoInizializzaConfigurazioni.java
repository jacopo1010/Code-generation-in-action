package it.jacopo.www.comandi;

import java.util.Map;

import it.jacopo.www.Engine;
import it.jacopo.www.io.IO;
import it.jacopo.www.model.MetaClass;
import it.jacopo.www.model.MetaField;

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
		io.stampaMessaggio("Generazione modelli in corso");
		this.stampaSchema(engine.generate(this.pathString));
	}
	
	private void stampaSchema(Map<String, MetaClass> data) {
		io.stampaMessaggio("Classi trovate: " + data.size());
		for(MetaClass m: data.values()) {
			io.stampaMessaggio("Classe: " + m.getName() + " | table: " + m.getTable() + " | pageTitle: " + m.getPageTitle());
			for (MetaField field : m.getFields().values()) {
				io.stampaMessaggio(
						"  Campo: " + field.getName() +
						" | tipo: " + field.getType() +
						" | label: " + field.getLabel() +
						" | widget: " + field.getWidget() +
						" | nullable: " + field.getTags().get("nullable") +
						" | gridVisible: " + field.getTags().get("gridVisible") +
						" | formVisible: " + field.getTags().get("formVisible") +
						" | relazione: " + field.isRelation() +
						" | cardinalita: " + field.getLowerBound() + ".." + field.getUpperBound());
			}
		}
	}

}
