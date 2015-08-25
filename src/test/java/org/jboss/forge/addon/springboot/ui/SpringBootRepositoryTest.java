package org.jboss.forge.addon.springboot.ui;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.jboss.forge.addon.javaee.JavaEEPackageConstants.DEFAULT_ENTITY_PACKAGE;
import static org.jboss.forge.addon.springboot.ui.SpringBootRepository.DEFAULT_REPOSITORY_PACKAGE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.addon.javaee.jpa.JPAFacet;
import org.jboss.forge.addon.parser.java.facets.JavaSourceFacet;
import org.jboss.forge.addon.parser.java.resources.JavaResource;
import org.jboss.forge.addon.projects.Project;
import org.jboss.forge.addon.shell.test.ShellTest;
import org.jboss.forge.addon.ui.controller.CommandController;
import org.jboss.forge.addon.ui.metadata.UICommandMetadata;
import org.jboss.forge.addon.ui.result.Failed;
import org.jboss.forge.addon.ui.result.Result;
import org.jboss.forge.addon.ui.test.UITestHarness;
import org.jboss.forge.arquillian.archive.AddonArchive;
import org.jboss.forge.roaster.model.JavaInterface;
import org.jboss.forge.roaster.model.source.JavaInterfaceSource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class SpringBootRepositoryTest {

	@Deployment
	public static AddonArchive getDeployment() {
		AddonArchive result = ShrinkWrap.create(AddonArchive.class)
				.addBeansXML().addClass(ProjectHelper.class)
				.addClass(SpringBootRepository.class)
				.addClass(SpringBootRepositoryType.class);
		return result;
	}

	private Project project;

	@Inject
	private UITestHarness uiTestHarness;

	@Inject
	private ShellTest shellTest;

	@Inject
	private ProjectHelper projectHelper;

	@Before
	public void setUp() throws IOException {
		project = projectHelper.createSpringBootProject();

		projectHelper.createJPAEntity(project, "Customer");
		// JavaResource entity = projectHelper.createJPAEntity(project,
		// "CustomerWithIdClass");
		// JavaClassSource source = entity.getJavaType();
		// AnnotationSource<JavaClassSource> annotation = source
		// .addAnnotation(IdClass.class);
		// annotation.setAnnotationValue("");
	}

	@Test
	public void checkCommandMetadata() throws Exception {
		try (CommandController controller = uiTestHarness
				.createCommandController(SpringBootRepository.class,
						project.getRoot())) {
			controller.initialize();
			// Checks the command metadata
			assertTrue(controller.getCommand() instanceof SpringBootRepository);
			UICommandMetadata metadata = controller.getMetadata();
			assertEquals("Spring Boot: Repository", metadata.getName());
			assertEquals("Spring", metadata.getCategory().getName());
			assertEquals("Boot", metadata.getCategory().getSubCategory()
					.getName());
			assertEquals(5, controller.getInputs().size());
			assertTrue(controller.hasInput("repositoryEntity"));
			assertTrue(controller.hasInput("repositoryType"));
			assertTrue(controller.hasInput("targetPackage"));
			assertTrue(controller.hasInput("named"));
			assertTrue(controller.hasInput("overwrite"));
		}
	}

	@Test
	public void testCrud() throws Exception {
		String baseBackageName = project.getFacet(JavaSourceFacet.class)
				.getBasePackage();
		String entityPackageName = baseBackageName + "."
				+ DEFAULT_ENTITY_PACKAGE;
		String repositoryPackageName = baseBackageName + "."
				+ DEFAULT_REPOSITORY_PACKAGE;
		shellTest.getShell().setCurrentResource(project.getRoot());
		Result result = shellTest
				.execute(
						("spring-boot-repository --repositoryEntity "
								+ entityPackageName
								+ ".Customer --named CustomerRepository --targetPackage "
								+ repositoryPackageName + " --repositoryType CRUD"),
						10, TimeUnit.SECONDS);
		Assert.assertThat(result, not(instanceOf(Failed.class)));
		Assert.assertTrue(project.hasFacet(JPAFacet.class));

		JavaSourceFacet facet = project.getFacet(JavaSourceFacet.class);
		JavaResource repositoryResuource = facet
				.getJavaResource(repositoryPackageName + ".CustomerRepository");
		Assert.assertNotNull(repositoryResuource);
		Assert.assertThat(repositoryResuource.getJavaType(),
				is(instanceOf(JavaInterface.class)));

		JavaInterfaceSource intf = (JavaInterfaceSource) repositoryResuource
				.getJavaType();
		Assert.assertEquals(intf.getInterfaces().size(), 1);
		String interfaceName = intf.getInterfaces().get(0);
		Assert.assertEquals(interfaceName, "CrudRepository<Customer, int>");
	}

	@Test
	public void testPaginAndSorting() throws Exception {
		JavaResource entity = projectHelper
				.createJPAEntity(project, "Customer");

		String baseBackageName = project.getFacet(JavaSourceFacet.class)
				.getBasePackage();
		String entityPackageName = baseBackageName + "."
				+ DEFAULT_ENTITY_PACKAGE;
		String repositoryPackageName = baseBackageName + "."
				+ DEFAULT_REPOSITORY_PACKAGE;
		shellTest.getShell().setCurrentResource(project.getRoot());
		Result result = shellTest
				.execute(
						("spring-boot-repository --repositoryEntity "
								+ entityPackageName
								+ ".Customer --named CustomerRepository --targetPackage "
								+ repositoryPackageName + " --repositoryType CRUD"),
						10, TimeUnit.SECONDS);
		Assert.assertThat(result, not(instanceOf(Failed.class)));
		Assert.assertTrue(project.hasFacet(JPAFacet.class));

		JavaSourceFacet facet = project.getFacet(JavaSourceFacet.class);
		JavaResource repositoryResuource = facet
				.getJavaResource(repositoryPackageName + ".CustomerRepository");
		Assert.assertNotNull(repositoryResuource);
		Assert.assertThat(repositoryResuource.getJavaType(),
				is(instanceOf(JavaInterface.class)));

		JavaInterfaceSource intf = (JavaInterfaceSource) repositoryResuource
				.getJavaType();
		Assert.assertEquals(intf.getInterfaces().size(), 1);
		String interfaceName = intf.getInterfaces().get(0);
		Assert.assertEquals(interfaceName,
				"PagingAndSortingRepository<Customer, int>");
	}

	// @Test
	// public void testIdClassPrimaryKey() throws Exception {
	// JavaResource entity = projectHelper
	// .createJPAEntity(project, "Customer");
	// }
}
