/**
 * 
 */
package edu.ucdavis.cs.dblp.data.stereotype;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.stereotype.Service;

/**
 * Extension to Spring @Service annotation, indicating functionality
 * that extracts information from data.  Example: keyphrase recognizer.
 * @author pfishero
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Service
public @interface Extractor {
	String value() default "";
}
