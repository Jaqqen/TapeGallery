package com.jaqqen.tapeshub;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.jmolecules.archunit.JMoleculesArchitectureRules;
import org.jmolecules.archunit.JMoleculesDddRules;
import com.tngtech.archunit.core.domain.JavaPackage;
import org.jspecify.annotations.NullMarked;
import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.docs.Documenter;

import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * The structure is only real if breaking it fails the build. These tests are what make that true:
 * {@code tape} may not reach into {@code genre}'s internals, no ring may depend outwards, and
 * aggregates must refer to each other by identity.
 */
public class ModularityTests {

    private static final String MAIN_PACKAGE = "com.jaqqen.tapeshub";
    private static final ApplicationModules modules = ApplicationModules.of(TapesHubApplication.class);

    private static JavaClasses productionClasses() {
        return new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages(MAIN_PACKAGE);
    }

    /** Fails if a module reaches past another module's base package into its internals. */
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
        JMoleculesDddRules.all().check(productionClasses());
    }

    /**
     * The simplified onion - domain, application, infrastructure - matching the
     * {@code org.jmolecules.architecture.onion.simplified} annotations the package-info files carry.
     * Runs over the whole application, not just one module, so both onions are checked.
     */
    @Test
    void modulesShouldRespectOnionArchitecture() {
        JMoleculesArchitectureRules.ensureOnionSimple().check(productionClasses());
    }

    /**
     * NullAway runs in {@code OnlyNullMarked} mode, which checks marked code and silently ignores
     * everything else - so a package that forgets {@code @NullMarked} is not an error, it is an
     * unchecked hole that nothing would report. This is what makes the annotation mandatory: NullAway
     * checks that marked code is null-safe, and this checks that code is marked.
     */
    @Test
    void everyProductionPackageIsNullMarked() {
        JavaPackage root = productionClasses().getPackage(MAIN_PACKAGE);

        Set<String> unmarked = Stream.concat(Stream.of(root), root.getSubpackagesInTree().stream())
            // Intermediate packages that only group subpackages hold no code to check.
            .filter(pkg -> !pkg.getClasses().isEmpty())
            .filter(pkg -> !pkg.isAnnotatedWith(NullMarked.class))
            .map(JavaPackage::getName)
            .collect(Collectors.toCollection(TreeSet::new));

        if (!unmarked.isEmpty()) {
            throw new AssertionError(
                "These packages are missing @NullMarked, so NullAway silently skips them." +
                    " Add it to each package-info.java (JSpecify does not inherit it from a parent package): "
                    + String.join(", ", unmarked));
        }
    }
}
