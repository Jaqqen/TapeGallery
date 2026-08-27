package com.jaqqen.tapeshub;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.jmolecules.archunit.JMoleculesArchitectureRules;
import org.jmolecules.archunit.JMoleculesDddRules;
import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.docs.Documenter;

public class ModularityTests {
    private static final String MAIN_PACKAGE = "com.jaqqen.tapeshub";
    private static final String TAPE_PACKAGE = MAIN_PACKAGE + ".tape";
    private static final ApplicationModules modules = ApplicationModules.of(TapesHubApplication.class);

    @Test
    void verifyModularity() {
        modules.verify();
    }

    @Test
    void createDocumentation() {
        new Documenter(modules).writeDocumentation();
    }

    @Test
    void checkAllClassesFollowDDDStructure() {
        JavaClasses classes = new ClassFileImporter().withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages(MAIN_PACKAGE);

        JMoleculesDddRules.all().check(classes);
    }

    @Test
    void tapeModuleShouldRespectOnionArchitecture() {
        JavaClasses classes = new ClassFileImporter().withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages(TAPE_PACKAGE);

        JMoleculesArchitectureRules.ensureOnionSimple().check(classes);
    }

}
