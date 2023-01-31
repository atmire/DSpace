/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */

package org.dspace.app.rest.cache;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.cache.annotation.Cacheable;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
@Cacheable(
    value = "rest-repository",
    key = "T(org.dspace.app.rest.cache.RestCacheSupport).getKey(#root.targetClass, #root.methodName, #root.args)",
    condition = "T(org.dspace.app.rest.cache.RestCacheSupport).doCache()"
)
public @interface RestRepositoryCacheable {
}
