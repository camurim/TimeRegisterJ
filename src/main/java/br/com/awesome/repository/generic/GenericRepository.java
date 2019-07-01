package br.com.awesome.repository.generic;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityExistsException;
import javax.persistence.EntityManager;
import javax.persistence.TransactionRequiredException;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import br.com.awesome.utils.JPAUtils;

public class GenericRepository<T, ID extends Serializable> {
	private final Class<T> persistenceClass;
	
	@SuppressWarnings("unchecked")
	public GenericRepository() {
		this.persistenceClass = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass())
				.getActualTypeArguments()[0];
	}
	
	public Class<T> getPersistenceClass() {
		return this.persistenceClass;
	}
	
	public T findById(ID id) {
		EntityManager entityManager = getEntityManager();
		List<T> result = new ArrayList<>();
		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<T> criteriaQuery = criteriaBuilder.createQuery(this.persistenceClass);
		Root<T> root = criteriaQuery.from(this.persistenceClass);
		Predicate predicate = criteriaBuilder.and();

		predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(root.get("id"), id));

		criteriaQuery.where(predicate);
		TypedQuery<T> typedQuery = entityManager.createQuery(criteriaQuery);

		result = typedQuery.getResultList();

		return result.get(0);
	}

	public T save(T object) throws EntityExistsException, IllegalArgumentException, TransactionRequiredException {
		getEntityManager().persist(object);
		return object;
	}

	public T update(T object) throws IllegalArgumentException, TransactionRequiredException {
		return getEntityManager().merge(object);
	}

	public T delete(T object) throws IllegalArgumentException, TransactionRequiredException {
		object = getEntityManager().merge(object);
		getEntityManager().remove(object);
		return null;
	}

	public List<T> fetchAll(String orderBy) {
		StringBuffer sb = new StringBuffer("SELECT obj FROM " + this.persistenceClass.getSimpleName() + " obj ");
		if (orderBy != null) {
			sb.append("order by " + orderBy);
		}

		TypedQuery<T> query = getEntityManager().createQuery(sb.toString(), this.persistenceClass);

		return query.getResultList();
	}

	public T saveOrUpdate(T object) throws IllegalArgumentException, TransactionRequiredException {
		getEntityManager().merge(object);
		return object;
	}

	public Long count() {
		EntityManager entityManager = getEntityManager();
		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);
		criteriaQuery.select(criteriaBuilder.count(criteriaQuery.from(this.persistenceClass)));

		return entityManager.createQuery(criteriaQuery).getSingleResult();
	}

	public EntityManager getEntityManager() {		
		return JPAUtils.JpaEntityManager();
	}
}
