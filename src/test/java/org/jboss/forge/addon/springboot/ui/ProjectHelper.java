package org.jboss.forge.addon.springboot.ui;

import static org.jboss.forge.addon.javaee.JavaEEPackageConstants.DEFAULT_ENTITY_PACKAGE;

import java.io.IOException;

import javax.inject.Inject;
import javax.persistence.GenerationType;

import org.jboss.forge.addon.javaee.jpa.PersistenceOperations;
import org.jboss.forge.addon.parser.java.facets.JavaSourceFacet;
import org.jboss.forge.addon.parser.java.resources.JavaResource;
import org.jboss.forge.addon.projects.Project;
import org.jboss.forge.addon.projects.ProjectFactory;

public class ProjectHelper {
	@Inject  
	private ProjectFactory projectFactory; s

	@Inject
	private PersistenceOperations persistenceOperations;

	public JavaResource createJPAEntity(Project project, String entityName)
			throws IOException {
		String packageName = project.getFacet(JavaSourceFacet.class)
				.getBasePackage() + "." + DEFAULT_ENTITY_PACKAGE;
		return persistenceOperations.newEntity(project, entityName,
				packageName, GenerationType.AUTO);
	}
}
