package it.jacopo.www.generator;

import java.io.File;

import it.jacopo.www.model.MetaClass;

public interface GeneratoreDiEntita {

	public MetaClass generaMetaClass(File xml);
}
