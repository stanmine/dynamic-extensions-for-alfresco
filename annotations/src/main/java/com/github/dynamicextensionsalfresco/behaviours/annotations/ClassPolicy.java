package com.github.dynamicextensionsalfresco.behaviours.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates a method is a Class-level Policy.
 * <p>
 * This annotation should only be applied to {@link org.alfresco.repo.policy.ClassPolicy} interface methods.
 *
 * @author Laurens Fridael
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ClassPolicy {

    /**
     * The QNames of the types or aspects to apply the Policy to. Can be specified in prefix format (
     * <code>cm:content</code>) or fully-qualified format (
     * <code>{http://www.alfresco.org/model/content/1.0}content</code>).
     * <p>
     * If no value is specified, the Policy is applied to all types and aspects.
     */
    String[] value() default {};

    /**
     * Indicates when to trigger the Behaviour.
     */
    Event event() default Event.INHERITED_OR_ALL;

}
