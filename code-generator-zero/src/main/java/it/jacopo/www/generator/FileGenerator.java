package it.jacopo.www.generator;

import freemarker.template.Configuration;
import it.jacopo.www.io.IO;

public abstract class FileGenerator  {
  
	private Configuration conf;
	private IO io;
	
	public FileGenerator(IO io) {
		this.conf = new Configuration(Configuration.VERSION_2_3_34);
		this.conf.setClassLoaderForTemplateLoading(Thread.currentThread().getContextClassLoader(), "/");
		this.conf.setDefaultEncoding("UTF-8");
		this.io = io;
	}
	
	
	
}
