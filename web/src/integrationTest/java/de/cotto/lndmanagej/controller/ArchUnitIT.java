package de.cotto.lndmanagej.controller;

import com.codahale.metrics.annotation.Timed;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.core.importer.Location;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

class ArchUnitIT {

    private static JavaClasses importedClasses;

    @BeforeAll
    static void setUp() {
        importedClasses = new ClassFileImporter()
                .withImportOption(new DoNotIncludeTests())
                .importPackages("de.cotto.lndmanagej.controller");
    }

    @Test
    void public_controller_methods_are_timed() {
        ArchRule rule = ArchRuleDefinition.methods().that()
                .areDeclaredInClassesThat().areAnnotatedWith(RequestMapping.class)
                .and()
                .arePublic()
                .should()
                .beAnnotatedWith(Timed.class);
        assertThat(importedClasses).isNotEmpty();
        rule.check(importedClasses);
    }

    @Test
    void public_controller_methods_do_not_return_collection_type() {
        ArchRule rule = ArchRuleDefinition.methods().that()
                .areDeclaredInClassesThat().areAnnotatedWith(RequestMapping.class)
                .and()
                .arePublic()
                .should()
                .notHaveRawReturnType(Collection.class)
                .andShould()
                .notHaveRawReturnType(List.class)
                .andShould()
                .notHaveRawReturnType(Set.class);
        assertThat(importedClasses).isNotEmpty();
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
