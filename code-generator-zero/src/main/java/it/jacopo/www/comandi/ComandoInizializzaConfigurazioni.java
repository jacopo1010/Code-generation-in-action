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
		engine.generateWithFreemarker(pathString);
	}

	private void stampaSchema(Map<String, MetaClass> data) {
		io.stampaMessaggio("Classi trovate: " + data.size());

		for (MetaClass m : data.values()) {
			io.stampaMessaggio("Classe: " + m.getName() 
			+ " | table: " + clean(m.getTable()) 
			+ " | pageTitle: " + clean(m.getPageTitle()));

			for (MetaField field : m.getFields().values()) {

				String messaggio = "  Campo: " + field.getName()
				+ " | tipo: " + field.getType()
				+ " | javaType: " + clean(field.getJavaType())
				+ " | sqlType: " + clean(field.getSqlType())
				+ " | label: " + clean(field.getLabel())
				+ " | widget: " + clean(field.getWidget())
				+ " | nullable: " + clean(field.getTags().get("nullable"))
				+ " | gridVisible: " + clean(field.getTags().get("gridVisible"))
				+ " | formVisible: " + clean(field.getTags().get("formVisible"))
				+ " | required: " + field.isRequired()
				+ " | relazione: " + field.isRelation()
				+ " | collection: " + field.isCollection();

				if (field.isRelation()) {
					messaggio += " | cardinalita owner: "
							+ clean(field.getOwnerLowerBound()) + ".." + clean(field.getOwnerUpperBound())
							+ " | cardinalita target: "
							+ clean(field.getTargetLowerBound()) + ".." + clean(field.getTargetUpperBound())
							+ " | relationType: " + clean(field.getRelationType())
							+ " | joinTableRequired: " + field.isJoinTableRequired()
							+ " | foreignKeyColumn: " + clean(field.getForeignKeyColumn());
				} else {
					messaggio += " | cardinalita: null..null";
				}

				io.stampaMessaggio(messaggio);
			}
		}
	}

	private String clean(String value) {
		return (value == null || value.isEmpty()) ? "null" : value;
	}

}
