/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest.model.hateoas;

import org.dspace.app.rest.model.hateoas.annotations.RelNameDSpaceResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.core.EvoInflectorRelProvider;

/**
 * A DSpace Relation Provider that use the RelNameDSpaceResource to use the
 * right names for the embedded collection when a DSpaceResource is requested
 * 
 * @author Andrea Bollini (andrea.bollini at 4science.it)
 *
 */
public class DSpaceRelProvider extends EvoInflectorRelProvider {

	@Autowired
	private DSpaceCurieProvider dspaceCurieProvider;

	@Override
	public String getItemResourceRelFor(Class<?> type) {
        RelNameDSpaceResource nameAnnotation = type.getAnnotation(RelNameDSpaceResource.class);
        if (nameAnnotation != null) {
			DSpaceRestCategory categoryAnnotation = type.getAnnotation(DSpaceRestCategory.class);
			if(categoryAnnotation == null) {
				return dspaceCurieProvider.getNamespacedRelFor(nameAnnotation.value(), nameAnnotation.value());
			} else {
				return dspaceCurieProvider.getNamespacedRelFor(categoryAnnotation.value(), nameAnnotation.value());
			}
        }
		return super.getItemResourceRelFor(type);
	}

}
