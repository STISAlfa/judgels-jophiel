package org.iatoki.judgels.jophiel.models.daos.hibernate;

import org.iatoki.judgels.commons.models.daos.hibernate.AbstractHibernateDao;
import org.iatoki.judgels.jophiel.models.daos.interfaces.EmailDao;
import org.iatoki.judgels.jophiel.models.domains.EmailModel;
import play.db.jpa.JPA;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Selection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class EmailHibernateDao extends AbstractHibernateDao<Long, EmailModel> implements EmailDao {

    @Override
    public EmailModel findByUserJid(String userJid) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<EmailModel> query = cb.createQuery(EmailModel.class);

        Root<EmailModel> root = query.from(EmailModel.class);

        query.where(cb.equal(root.get("userJid"), userJid));

        return JPA.em().createQuery(query).getSingleResult();
    }

    @Override
    public List<String> findUserJidByFilter(String filterString) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<String> query = cb.createQuery(String.class);
        Root<EmailModel> root = query.from(EmailModel.class);

        List<Predicate> predicates = new ArrayList<>();
        predicates.add(cb.like(root.get("email"), "%" + filterString + "%"));

        Predicate condition = cb.or(predicates.toArray(new Predicate[predicates.size()]));

        query.select(root.get("userJid")).where(condition);
        return JPA.em().createQuery(query).getResultList();
    }

    @Override
    public List<String> sortUserJid(Collection<String> userJids, String sortBy, String order) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<String> query = cb.createQuery(String.class);
        Root<EmailModel> root = query.from(EmailModel.class);

        Predicate condition = root.get("userJid").in(userJids);

        Order orderBy = null;
        if ("asc".equals(order)) {
            orderBy = cb.asc(root.get(sortBy));
        } else {
            orderBy = cb.desc(root.get(sortBy));
        }

        query.select(root.get("userJid")).where(condition).orderBy(orderBy);
        return JPA.em().createQuery(query).getResultList();
    }

    @Override
    public List<EmailModel> findBySetOfUserJid(Collection<String> userJids, long first, long max) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<EmailModel> query = cb.createQuery(EmailModel.class);
        Root<EmailModel> root = query.from(EmailModel.class);

        List<Selection<?>> selection = new ArrayList<>();
        selection.add(root.get("userJid"));
        selection.add(root.get("email"));

        Predicate condition = root.get("userJid").in(userJids);

        CriteriaBuilder.Case orderCase = cb.selectCase();
        long i = 0;
        for (String userJid : userJids) {
            orderCase = orderCase.when(cb.equal(root.get("userJid"), userJid), i);
            ++i;
        }
        Order order = cb.asc(orderCase.otherwise(i));

        query
            .multiselect(selection)
            .where(condition)
            .orderBy(order);

        List<EmailModel> list = JPA.em().createQuery(query).setFirstResult((int) first).setMaxResults((int) max).getResultList();

        return list;
    }

    @Override
    public EmailModel findByEmail(String email) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<EmailModel> query = cb.createQuery(EmailModel.class);
        Root<EmailModel> root = query.from(EmailModel.class);

        query.where(cb.equal(root.get("email"), email));

        return JPA.em().createQuery(query).getSingleResult();
    }
}
