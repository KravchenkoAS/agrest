package com.nhl.link.rest.annotation.listener;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * A chain listener annotation for methods to be invoked after
 * {@link com.nhl.link.rest.runtime.processor.select.SelectStage#PARSE_REQUEST} stage.
 *
 * @deprecated since 2.7 as annotated listeners were deprecated in favor of the functional interceptor API.
 * @since 1.19
 */
@Target({ METHOD })
@Retention(RUNTIME)
@Inherited
public @interface SelectRequestParsed {

}
