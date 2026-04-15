package it.jacopo.www.loader;

public class PropertiesCostanti {

	public static final String MODEL_DOMAIN_PATH = "generator.domain.model.path";
	public static final String UI_CONFIG_PATH = "generator.ui.config.path";
	public static final String JAVA_OUTPUT_PATH = "generator.java.output.path";
	public static final String ORM = "generator.hibernate.enable";
    public static final String PACKAGE_MODEL ="generator.model.package"; 
    public static final String SQL_SCHEMA_PATH = "generator.sql.output";
    public static final String DAO_OUTPUT_PACKAGE = "generator.dao.package";
    public static final String SERVICE_OUTPUT_PACKAGE = "generator.service.package";
    public static final String CONTROLLER_OUTPUT_PACKAGE = "generator.controller.package";
	
	/***
 domain.model.path=C:/project/models/domain.xml
ui.config.path=C:/project/config/ui-config.json
output.path=C:/project/generated
generator.backend.enabled=true
generator.frontend.metadata.enabled=true
generator.sql.enabled=true
base.package=org.jacopo.generated
generator.java.output.path=src/main/java
	 */
}
