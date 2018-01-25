/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.checker.dao.impl;

import org.dspace.checker.ChecksumHistory;
import org.dspace.checker.ChecksumHistory_;
import org.dspace.checker.ChecksumResultCode;
import org.dspace.checker.MostRecentChecksum;
import org.dspace.checker.MostRecentChecksum_;
import org.dspace.checker.dao.MostRecentChecksumDAO;
import org.dspace.content.Bitstream;
import org.dspace.content.BitstreamFormat;
import org.dspace.core.Context;
import org.dspace.core.AbstractHibernateDAO;
import org.dspace.eperson.Subscription;
import org.hibernate.Criteria;
import javax.persistence.Query;
import org.hibernate.criterion.*;

import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Order;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import java.sql.SQLException;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

/**
 * Hibernate implementation of the Database Access Object interface class for the MostRecentChecksum object.
 * This class is responsible for all database calls for the MostRecentChecksum object and is autowired by spring
 * This class should never be accessed directly.
 *
 * @author kevinvandevelde at atmire.com
 */
public class MostRecentChecksumDAOImpl extends AbstractHibernateDAO<MostRecentChecksum> implements MostRecentChecksumDAO
{
    protected MostRecentChecksumDAOImpl()
    {
        super();
    }


    @Override
    public List<MostRecentChecksum> findByNotProcessedInDateRange(Context context, Date startDate, Date endDate) throws SQLException {
//                    + "most_recent_checksum.last_process_start_date, most_recent_checksum.last_process_end_date, "
//                    + "most_recent_checksum.expected_checksum, most_recent_checksum.current_checksum, "
//                    + "result_description "
//                    + "from checksum_results, most_recent_checksum "
//                    + "where most_recent_checksum.to_be_processed = false "
//                    + "and most_recent_checksum.result = checksum_results.result_code "
//                    + "and most_recent_checksum.last_process_start_date >= ? "
//                    + "and most_recent_checksum.last_process_start_date < ? "
//                    + "order by most_recent_checksum.bitstream_id

        CriteriaBuilder criteriaBuilder = getCriteriaBuilder(context);
        CriteriaQuery<MostRecentChecksum> criteriaQuery = getCriteriaQuery(criteriaBuilder, MostRecentChecksum.class);
        Root<MostRecentChecksum> mostRecentChecksumRoot = criteriaQuery.from(MostRecentChecksum.class);
        criteriaQuery.select(mostRecentChecksumRoot);
        criteriaQuery.where(criteriaBuilder.and(criteriaBuilder.equal(mostRecentChecksumRoot.get(MostRecentChecksum_.toBeProcessed), false),
                                                criteriaBuilder.lessThanOrEqualTo(mostRecentChecksumRoot.<Date>get(MostRecentChecksum_.processStartDate), startDate),
                                                criteriaBuilder.greaterThan(mostRecentChecksumRoot.<Date>get(MostRecentChecksum_.processStartDate), endDate)
                                                )
                            );
        List<Order> orderList = new LinkedList<>();
        orderList.add(criteriaBuilder.asc(mostRecentChecksumRoot.get(MostRecentChecksum_.bitstream)));
        criteriaQuery.orderBy(orderList);
        return list(context, criteriaQuery, false, MostRecentChecksum.class, -1, -1);
    }


    @Override
    public MostRecentChecksum findByBitstream(Context context, Bitstream bitstream) throws SQLException {
        CriteriaBuilder criteriaBuilder = getCriteriaBuilder(context);
        CriteriaQuery<MostRecentChecksum> criteriaQuery = getCriteriaQuery(criteriaBuilder, MostRecentChecksum.class);
        Root<MostRecentChecksum> mostRecentChecksumRoot = criteriaQuery.from(MostRecentChecksum.class);
        criteriaQuery.select(mostRecentChecksumRoot);
        criteriaQuery.where(criteriaBuilder.equal(mostRecentChecksumRoot.get(MostRecentChecksum_.bitstream), bitstream));
        return singleResult(context, criteriaQuery);
    }


    @Override
    public List<MostRecentChecksum> findByResultTypeInDateRange(Context context, Date startDate, Date endDate, ChecksumResultCode resultCode) throws SQLException {
//        "select bitstream_id, last_process_start_date, last_process_end_date, "
//                    + "expected_checksum, current_checksum, result_description "
//                    + "from most_recent_checksum, checksum_results "
//                    + "where most_recent_checksum.result = checksum_results.result_code "
//                    + "and most_recent_checksum.result= ? "
//                    + "and most_recent_checksum.last_process_start_date >= ? "
//                    + "and most_recent_checksum.last_process_start_date < ? "
//                    + "order by bitstream_id";
        CriteriaBuilder criteriaBuilder = getCriteriaBuilder(context);
        CriteriaQuery<MostRecentChecksum> criteriaQuery = getCriteriaQuery(criteriaBuilder, MostRecentChecksum.class);
        Root<MostRecentChecksum> mostRecentChecksumRoot = criteriaQuery.from(MostRecentChecksum.class);
        criteriaQuery.select(mostRecentChecksumRoot);
        criteriaQuery.where(criteriaBuilder.and(criteriaBuilder.equal(mostRecentChecksumRoot.get("checksumResult.resultCode"), resultCode),
                                                criteriaBuilder.lessThanOrEqualTo(mostRecentChecksumRoot.<Date>get("processStartDate"), startDate),
                                                criteriaBuilder.greaterThan(mostRecentChecksumRoot.<Date>get("processStartDate"), endDate)
                                                )
                            );
        List<Order> orderList = new LinkedList<>();
        orderList.add(criteriaBuilder.asc(mostRecentChecksumRoot.get(MostRecentChecksum_.bitstream)));
        criteriaQuery.orderBy(orderList);
        return list(context, criteriaQuery, false, MostRecentChecksum.class, -1, -1);

    }

    @Override
    public void deleteByBitstream(Context context, Bitstream bitstream) throws SQLException
    {
        String hql = "delete from MostRecentChecksum WHERE bitstream=:bitstream";
        Query query = createQuery(context, hql);
        query.setParameter("bitstream", bitstream);
        query.executeUpdate();
    }

    @Override
    public MostRecentChecksum getOldestRecord(Context context) throws SQLException {
        //        "select bitstream_id  "
        //        + "from most_recent_checksum " + "where to_be_processed = true "
        //        + "order by date_trunc('milliseconds', last_process_end_date), "
        //        + "bitstream_id " + "ASC LIMIT 1";



        CriteriaBuilder criteriaBuilder = getCriteriaBuilder(context);
        CriteriaQuery<MostRecentChecksum> criteriaQuery = getCriteriaQuery(criteriaBuilder, MostRecentChecksum.class);
        Root<MostRecentChecksum> mostRecentChecksumRoot = criteriaQuery.from(MostRecentChecksum.class);
        criteriaQuery.select(mostRecentChecksumRoot);
        criteriaQuery.where(criteriaBuilder.equal(mostRecentChecksumRoot.get(MostRecentChecksum_.toBeProcessed), true));
        List<Order> orderList = new LinkedList<>();
        orderList.add(criteriaBuilder.asc(mostRecentChecksumRoot.get(MostRecentChecksum_.bitstream)));
        orderList.add(criteriaBuilder.asc(mostRecentChecksumRoot.get(MostRecentChecksum_.processEndDate)));
        criteriaQuery.orderBy(orderList);
        return singleResult(context, criteriaQuery);
    }

    @Override
    public MostRecentChecksum getOldestRecord(Context context, Date lessThanDate) throws SQLException {
//                "select bitstream_id  "
//                + "from most_recent_checksum "
//                + "where to_be_processed = true "
//                + "and last_process_start_date < ? "
//                + "order by date_trunc('milliseconds', last_process_end_date), "
//                + "bitstream_id " + "ASC LIMIT 1";


        CriteriaBuilder criteriaBuilder = getCriteriaBuilder(context);
        CriteriaQuery<MostRecentChecksum> criteriaQuery = getCriteriaQuery(criteriaBuilder, MostRecentChecksum.class);
        Root<MostRecentChecksum> mostRecentChecksumRoot = criteriaQuery.from(MostRecentChecksum.class);
        criteriaQuery.select(mostRecentChecksumRoot);
        criteriaQuery.where(criteriaBuilder.and(criteriaBuilder.equal(mostRecentChecksumRoot.get(MostRecentChecksum_.toBeProcessed), true),
                                                criteriaBuilder.lessThan(mostRecentChecksumRoot.get(MostRecentChecksum_.processStartDate), lessThanDate)
                                                )
                            );

        List<Order> orderList = new LinkedList<>();
        orderList.add(criteriaBuilder.asc(mostRecentChecksumRoot.get(MostRecentChecksum_.processEndDate)));
        orderList.add(criteriaBuilder.asc(mostRecentChecksumRoot.get(MostRecentChecksum_.bitstream)));
        criteriaQuery.orderBy(orderList);

        return singleResult(context, criteriaQuery);
    }

    @Override
    public List<MostRecentChecksum> findNotInHistory(Context context) throws SQLException {

        CriteriaBuilder criteriaBuilder = getCriteriaBuilder(context);
        CriteriaQuery<MostRecentChecksum> criteriaQuery = getCriteriaQuery(criteriaBuilder, MostRecentChecksum.class);
        Root<MostRecentChecksum> checksumRoot = criteriaQuery.from(MostRecentChecksum.class);

        Subquery<Bitstream> subQuery = criteriaQuery.subquery(Bitstream.class);
        Root<ChecksumHistory> historyRoot = subQuery.from(ChecksumHistory.class);
        subQuery.select(historyRoot.get(ChecksumHistory_.bitstream));

        criteriaQuery.where(
                criteriaBuilder.not(checksumRoot.get(MostRecentChecksum_.bitstream).in(subQuery)));

        return list(context, criteriaQuery, false, MostRecentChecksum.class, -1, -1);
    }
}
