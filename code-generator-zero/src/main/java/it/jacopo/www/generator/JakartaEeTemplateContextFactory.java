package it.jacopo.www.generator;

import java.util.List;
import java.util.Map;

public final class JakartaEeTemplateContextFactory {

	private JakartaEeTemplateContextFactory() {
	}

	public static Map<String, Object> create() {
		return Map.of(
				"repository", createRepositoryContext(),
				"service", createServiceContext(),
				"controller", createControllerContext());
	}

	private static Map<String, Object> createRepositoryContext() {
		return Map.of(
				"imports", List.of("jakarta.ejb.Stateless"),
				"classAnnotations", List.of("@Stateless"),
				"entityManagerFieldImports", List.of("jakarta.persistence.PersistenceContext"),
				"entityManagerFieldAnnotations", List.of("@PersistenceContext"));
	}

	private static Map<String, Object> createServiceContext() {
		return Map.of(
				"baseImports", List.of("jakarta.ejb.Stateless", "jakarta.inject.Inject"),
				"baseClassAnnotations", List.of("@Stateless"),
				"fieldAnnotations", List.of("@Inject"),
				"wrapperImports", List.of("jakarta.ejb.Stateless"),
				"wrapperAnnotations", List.of("@Stateless"));
	}

	private static Map<String, Object> createControllerContext() {
		return Map.of(
				"baseImports", List.of("jakarta.inject.Inject"),
				"baseClassAnnotations", List.of(),
				"fieldAnnotations", List.of("@Inject"),
				"wrapperImports", List.of(),
				"wrapperAnnotations", List.of());
	}
}
