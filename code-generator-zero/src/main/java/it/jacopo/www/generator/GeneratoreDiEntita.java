package it.jacopo.www.generator;

import java.io.File;
import java.util.Map;

import it.jacopo.www.model.MetaClass;

public interface GeneratoreDiEntita {

	public Map<String, MetaClass> generaMetaClass(File xml);
}
