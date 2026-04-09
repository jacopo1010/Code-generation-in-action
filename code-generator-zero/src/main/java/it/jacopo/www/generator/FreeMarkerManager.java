package it.jacopo.www.generator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import freemarker.ext.dom.NodeModel;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import it.jacopo.www.io.IO;
import it.jacopo.www.loader.PropertiesCostanti;

public class FreeMarkerManager {

	private DocumentBuilderFactory factory;
	private Configuration conf;
	private IO io;

	public FreeMarkerManager(IO io) {
		this.factory = DocumentBuilderFactory.newInstance();
		this.conf = new Configuration(Configuration.VERSION_2_3_34);
		this.io = io;
	}

	public void generateModel(String Path, File xmi,String output) {
		try {
			// 1. CORREZIONE: Passiamo la CARTELLA, non il file!
			conf.setDirectoryForTemplateLoading(new File("C:\\Users\\jaorr\\git\\Code-generation-in-action\\code-generator-zero\\src\\main\\resources\\freemarker"));
			conf.setDefaultEncoding("UTF-8");
			NodeModel node = NodeModel.parse(xmi);
			if (output == null || output.trim().isEmpty()) {
				throw new IllegalArgumentException("Definire nell'application.properties la cartella di output");
			}

			// Creiamo la cartella di output se non esiste
			new File(output).mkdir();
			Template template = conf.getTemplate("modelTemplate.ftl");
			DocumentBuilder dBuilder = this.factory.newDocumentBuilder();
			Document docJava = dBuilder.parse(xmi);
			NodeList listaElementi = docJava.getElementsByTagName("packagedElement");

			for (int i = 0; i < listaElementi.getLength(); i++) {
				Element elemento = (Element) listaElementi.item(i);

				if ("uml:Class".equals(elemento.getAttribute("xmi:type"))) {

					String nomeClasse = elemento.getAttribute("name");
					String nomeFile = nomeClasse + ".java";

					io.stampaMessaggio(output);

					// 5. CORREZIONE: Ora passiamo un FileOutputStream al Writer!
					File fileDestinazione = new File(output, nomeFile);
					Writer writer = new OutputStreamWriter(new FileOutputStream(fileDestinazione), StandardCharsets.UTF_8);

					// Prepariamo i dati
					Map<String, Object> dati = new HashMap<>();
					dati.put("doc", node); // Tutto l'XML
					dati.put("classeCorrente", nomeClasse); // Il nome della classe in questo giro di ciclo

					// Eseguiamo il template
					template.process(dati, writer);

					// Salviamo e chiudiamo il file
					writer.close();
				}
			}
			io.stampaMessaggio("Generazione completata con successo!");
		} catch (IOException | SAXException | ParserConfigurationException | TemplateException e) {
			System.err.println("Errore durante la generazione: " + e.getMessage());
			e.printStackTrace();
		}
	}
	
	
}

