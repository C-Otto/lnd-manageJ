package de.cotto.lndmanagej;

import com.codahale.metrics.annotation.Timed;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.core.importer.Location;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition;
import com.tngtech.archunit.library.dependencies.SliceRule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.regex.Pattern;

import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

class ArchUnitIT {

    private static JavaClasses importedClasses;

    @BeforeAll
    static void setUp() {
        importedClasses = new ClassFileImporter().withImportOption(new DoNotIncludeTests()).importPackages("de.cotto");
    }

    @Test
    void services_must_not_access_dto_classes_directly() {
        ArchRule rule = ArchRuleDefinition.noClasses().that()
                .haveSimpleNameEndingWith("Dto").or().haveSimpleNameEndingWith("DTO")
                .should()
                .dependOnClassesThat().haveSimpleNameEndingWith("Service");
        rule.check(importedClasses);
    }

    @Test
    void services_must_not_access_spring_data_repositories_directly() {
        ArchRule rule = ArchRuleDefinition.noClasses().that()
                .areAssignableTo("org.springframework.data.repository.Repository")
                .should()
                .dependOnClassesThat().haveSimpleNameEndingWith("Service");
        rule.check(importedClasses);
    }

    @Test
    void no_package_cycle() {
        SliceRule rule = slices().matching("de.cotto.(**)").should().beFreeOfCycles();
        rule.check(importedClasses);
    }

    @Test
    void daos_are_transactional() {
        ArchRule rule = ArchRuleDefinition.classes().that()
                .haveSimpleNameEndingWith("DaoImpl")
                .should()
                .beAnnotatedWith("javax.transaction.Transactional")
                .orShould()
                .beAnnotatedWith("org.springframework.transaction.annotation.Transactional");
        // https://stackoverflow.com/q/26387399/947526
        rule.check(importedClasses);
    }

    @Test
    void timed_methods_are_public() {
        ArchRule rule = ArchRuleDefinition.methods().that()
                .areAnnotatedWith(Timed.class)
                .should()
                .bePublic();
        rule.check(importedClasses);
    }

    private static class DoNotIncludeTests implements ImportOption {
        private static final Pattern GRADLE_PATTERN = Pattern.compile(".*/build/classes/([^/]+/)?[a-zA-Z-]*[tT]est/.*");

        @Override
        public boolean includes(Location location) {
            return !location.matches(GRADLE_PATTERN);
        }
    }
}
