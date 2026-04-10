package com.example.testsupport;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * Meta-annotation to mark a method as a component test scenario.
 * Combines {@link Test} with tags for filtering in build profiles.
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Test
@Tag("ComponentTest")
@Tag("slow")
public @interface ComponentTest {
}
