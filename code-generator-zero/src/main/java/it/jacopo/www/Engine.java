package it.jacopo.www;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
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
import it.jacopo.www.generator.GeneratoreDiEntita;
import it.jacopo.www.generator.GeneratoreDiMetaClass;
import it.jacopo.www.io.IO;
import it.jacopo.www.loader.CaricatoreDiFile;
import it.jacopo.www.loader.CaricatoreDiFileImpl;
import it.jacopo.www.loader.PropertiesCostanti;
import it.jacopo.www.model.MetaClass;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

public class Engine {

	private CaricatoreDiFile loader;
	private GeneratoreDiEntita metaCreator;
	private IO io;

	public Engine(IO io) {
		this.io = io;
		this.loader = new CaricatoreDiFileImpl();
		this.metaCreator = new GeneratoreDiMetaClass(io);
	}

	public CaricatoreDiFile getLoader() {
		return loader;
	}

	public void setLoader(CaricatoreDiFile loader) {
		this.loader = loader;
	}

	public Map<String, MetaClass> generate(String path){
		File xmlFile = this.loader.carica(path);
		String orm = this.loader.getApplicationProperties().getProperty(PropertiesCostanti.ORM);
		if (orm == null || orm.isEmpty()) {
			throw new RuntimeException(
					"Il file application.properties è configurato male. " +
							"Devi inserire la proprietà: " + PropertiesCostanti.ORM);
		}
		if(orm.equals("true")) {
			//invoca metodo per hibernate 
		}else if(orm.equals("false")){
			//invoca metodo per JDBC
			return this.metaCreator.generaMetaClass(xmlFile);
		}else {
			throw new IllegalArgumentException("Devi inserire " + PropertiesCostanti.ORM);
		}
		return new LinkedHashMap<String, MetaClass>();
	}
	
	
	// ... (dentro la tua classe) ...

	public void generateWithFreemarker(String path) {
	    Configuration conf = new Configuration(Configuration.VERSION_2_3_34);
	    
	    try {
	        // 1. CORREZIONE: Passiamo la CARTELLA, non il file!
	        conf.setDirectoryForTemplateLoading(new File("C:\\Users\\jaorr\\git\\Code-generation-in-action\\code-generator-zero\\src\\main\\resources\\freemarker"));
	        conf.setDefaultEncoding("UTF-8");
	        
	        // 2. Usiamo il tuo loader per prendere l'XMI e creare il NodeModel
	        File xmi = this.loader.carica(path);
	        NodeModel node = NodeModel.parse(xmi);
	        
	        // 3. Controlliamo l'output (CORREZIONE: uso && al posto di ||)
	        String output = this.loader.getApplicationProperties().getProperty(PropertiesCostanti.MODEL_OUTPUT_PATH);
	        if (output == null || output.trim().isEmpty()) {
	            throw new IllegalArgumentException("Definire nell'application.properties la cartella di output");
	        }
	        
	        // Creiamo la cartella di output se non esiste
	        new File(output).mkdirs();

	        // Carichiamo il template
	        Template template = conf.getTemplate("modelTemplate.ftl");

	        // 4. PARSER JAVA PER TROVARE LE CLASSI E CICLARE
	        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
	        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
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
