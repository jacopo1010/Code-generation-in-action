package it.jacopo.www.generator;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import it.jacopo.www.io.IO;
import it.jacopo.www.model.MetaClass;

public class FreeMarkerManager extends FileGenerator {

	private final ModelGenerator modelGenerator;
	private final SqlGenerator sqlGenerator;
	private final RepositoryGenerator repositoryGenerator;
	private final ServiceGenerator serviceGenerator;
	private final ControllerGenerator controllerGenerator;

	public FreeMarkerManager(IO io) {
		super(io);
		this.modelGenerator = new ModelGenerator(io);
		this.sqlGenerator = new SqlGenerator(io);
		this.repositoryGenerator = new RepositoryGenerator(io);
		this.serviceGenerator = new ServiceGenerator(io);
		this.controllerGenerator = new ControllerGenerator(io);
	}

	public void generateModel(String packageModel, Map<String, MetaClass> metaClasses, String outputRoot) {
		this.modelGenerator.generate(packageModel, metaClasses, outputRoot);
	}

	public File generateSchema(Map<String, MetaClass> metaClasses, String outputRoot) {
		return this.sqlGenerator.generate(metaClasses, outputRoot);
	}

	public void generateRepository(String packageModel, String packageRepository,
	        Map<String, MetaClass> metaClasses, String outputRoot) {
		this.repositoryGenerator.generate(packageModel, packageRepository, metaClasses, outputRoot);
	}
	
	public void generateService(String packageModel, String packageRepository, String packageService, Map<String, MetaClass> metaClasses, String outputRoot) {
		this.serviceGenerator.generate(packageModel, packageRepository, packageService, metaClasses, outputRoot);
	}

	public void generateController(String packageModel, String packageService, String packageController,
			Map<String, MetaClass> metaClasses, String outputRoot) {
		this.controllerGenerator.generate(packageModel, packageService, packageController, metaClasses, outputRoot);
	}

	protected void validateOutputRoot(String outputRoot) {
		super.validateOutputRoot(outputRoot);
	}

	protected void validateMetaClasses(Map<String, MetaClass> metaClasses) {
		super.validateMetaClasses(metaClasses);
	}

	protected void validatePackage(String packageName) {
		super.validatePackage(packageName);
	}

	protected File prepareOutputDirectory(String outputRoot, String packageName) throws IOException {
		return super.prepareOutputDirectory(outputRoot, packageName);
	}

	protected File resolveOutputDirectory(String outputRoot, String packageName) {
		return super.resolveOutputDirectoryInternal(outputRoot, packageName);
	}

	protected String normalizePath(String path) {
		return super.normalizePathInternal(path);
	}
}
