/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.api.token.dao.impl;

import java.sql.SQLException;
import java.util.List;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.dspace.api.token.ApiToken;
import org.dspace.api.token.ApiToken_;
import org.dspace.api.token.dao.ApiTokenDAO;
import org.dspace.core.AbstractHibernateDAO;
import org.dspace.core.Context;
import org.dspace.eperson.EPerson;

/**
 * Implementation class for the {@link ApiTokenDAO}
 */
public class ApiTokenDAOImpl extends AbstractHibernateDAO<ApiToken> implements ApiTokenDAO {

    @Override
    public List<ApiToken> findAllByEPerson(Context context, EPerson ePerson, int limit, int offset)
            throws SQLException {
        CriteriaBuilder criteriaBuilder = getCriteriaBuilder(context);
        CriteriaQuery criteriaQuery = getCriteriaQuery(criteriaBuilder, ApiToken.class);
        Root<ApiToken> tokenRoot = criteriaQuery.from(ApiToken.class);
        criteriaQuery.select(tokenRoot);
        criteriaQuery.where(criteriaBuilder.equal(tokenRoot.get(ApiToken_.ePerson), ePerson));

        return list(context, criteriaQuery, false, ApiToken.class, limit, offset);
    }
}
