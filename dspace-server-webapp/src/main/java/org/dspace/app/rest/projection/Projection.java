/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest.projection;

import javax.persistence.Entity;

import org.dspace.app.rest.model.LinkRest;
import org.dspace.app.rest.model.RestModel;
import org.dspace.app.rest.model.hateoas.HALResource;
import org.dspace.app.rest.repository.DSpaceRestRepository;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RestController;

/**
 * A pluggable, uniquely-named {@link Component} that provides a way to change how a domain object is represented,
 * at one or more points in its lifecycle on the way to its being exposed via the REST API.
 *
 * <h2>The object lifecycle</h2>
 *
 * <p>
 * While fulfilling a typical REST request, a DSpace domain object takes three major forms, in order:
 * </p>
 *
 * <ul>
 *   <li> A model object provided by some service. This is typically a JPA {@link Entity}.</li>
 *   <li> A {@link RestModel} object provided by a {@link DSpaceRestRepository}.</li>
 *   <li> A {@link HALResource} object provided by the {@link RestController} to Spring, which then
 *        serializes it as JSON for the client to consume.</li>
 * </ul>
 *
 * <h2>What a projection can modify, and when</h2>
 *
 * A {@code Projection} implementation is capable of adding to or omitting information from the object
 * in any of these forms, at the following points in time:
 *
 * <ul>
 *   <li> Before it is converted to a {@link RestModel}, the projection may modify it
 *        via {@link #transformModel(Object)}.</li>
 *   <li> After it is converted to a {@link RestModel}, the projection may modify it
 *        via {@link #transformRest(RestModel)}.</li>
 *   <li> During conversion to a {@link HALResource}, the projection may opt in of certain annotation-discovered
 *        HAL embeds and links via {@link #allowEmbedding(HALResource, LinkRest)}
 *        and {@link #allowLinking(HALResource, LinkRest)}</li>
 *   <li> After conversion to a {@link HALResource}, the projection may modify it
 *        via {@link #transformResource(HALResource)}.</li>
 * </ul>
 *
 * <h2>How a projection is chosen</h2>
 *
 * When a REST request is made, the use of a projection may be explicit, as when it is provided as an argument
 * to the request, e.g. {@code /items/{uuid}?projection={projectionName}}. It may also be implicit, as when the
 * {@link ListProjection} is used automatically, in order to provide an abbreviated representation when serving
 * a collection of resources.
 */
public interface Projection {

    /**
     * The default projection.
     */
    Projection DEFAULT = new DefaultProjection();

    /**
     * Gets the projection name.
     *
     * @return the name, which is a unique alphanumeric string.
     */
    String getName();

    /**
     * Transforms the original model object (e.g. JPA entity) before conversion to a {@link RestModel}.
     *
     * This is a good place to omit data for certain properties that should not be included in the object's
     * representation as a {@link HALResource}. Omitting these properties early helps to prevent unnecessary
     * database calls for lazy-loaded properties that are unwanted for the projection.
     *
     * @param modelObject the original model object, which may be of any type.
     * @param <T> the return type, which must be the same type as the given model object.
     * @return the transformed model object, or the original, if the projection does not modify it.
     */
    <T> T transformModel(T modelObject);

    /**
     * Transforms the rest object after it was converted from a model object.
     *
     * This may add data to, or omit data from the rest representation of the object.
     *
     * @param restObject the rest object.
     * @param <T> the return type, which must be of the same type as the given rest object.
     * @return the transformed rest object, or the original, if the projection does not modify it.
     */
    <T extends RestModel> T transformRest(T restObject);

    /**
     * Transforms the resource object after it has been constructed and any constructor or annotation-based
     * links and embeds have been added.
     *
     * This may add data to, or omit data from the HAL resource representation of the object.
     *
     * @param halResource the resource object.
     * @param <T> the return type, which must be of the same type as the given resource object.
     * @return the transformed resource object, or the original, if the projection does not modify it.
     */
    <T extends HALResource> T transformResource(T halResource);

    /**
     * This method will indicate or define whether a certain linkRest annotated property can be embedded onto the
     * resource. This will define whether an optional property is allowed to be embedded; if the property isn't
     * optional then the value of this method will not be accounted for
     * @param halResource   The HALResource on which we want to embed
     * @param linkRest      The annotation on the property that we want to embed
     * @return              A boolean indicating whether we should embed or not
     */
    boolean allowEmbedding(HALResource halResource, LinkRest linkRest);

    /**
     * This method will indicate or define whether a certain linkRest annotated property can be linked to in the
     * resource. This will define whether an optional property is allowed to be linked to; if the property isn't
     * optional then the value of this method will not be accounted for
     * @param halResource   The HALResource on which we want to add the link
     * @param linkRest      The annotation on the property that we want to link to
     * @return              A boolean indicating whether we should create a link or not
     */
    boolean allowLinking(HALResource halResource, LinkRest linkRest);
}
