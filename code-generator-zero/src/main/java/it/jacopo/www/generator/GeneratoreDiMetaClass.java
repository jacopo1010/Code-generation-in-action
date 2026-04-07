package it.jacopo.www.generator;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import it.jacopo.www.io.IO;
import it.jacopo.www.model.MetaClass;

public class GeneratoreDiMetaClass implements GeneratoreDiEntita{

	private DocumentBuilderFactory factory;
	private IO io;
	
	public GeneratoreDiMetaClass(IO io) {
		this.io = io;
		this.factory = DocumentBuilderFactory.newInstance();
	}
	
	@Override
	public MetaClass generaMetaClass(File xml) {
		 try {
			DocumentBuilder builder = this.factory.newDocumentBuilder();
			Document doc = builder.parse(xml);
			io.stampaMessaggio("Root element: " + doc.getDocumentElement().getNodeName());
		 } catch (ParserConfigurationException e) {
			e.printStackTrace();
		 } catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

}
