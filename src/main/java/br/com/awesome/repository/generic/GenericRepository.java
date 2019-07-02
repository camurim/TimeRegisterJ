package br.com.awesome.repository.generic;

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
import javax.transaction.Transactional;

import br.com.awesome.model.generic.AbstractModel;
import br.com.awesome.repository.entity.generic.AbstractEntity;
import br.com.awesome.utils.JPAUtils;

public class GenericRepository<T extends AbstractModel, K extends AbstractEntity, ID extends Number> {
	private final Class<T> modelClass;
	
	@SuppressWarnings("unchecked")
	public GenericRepository() {
		this.modelClass = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass())
				.getActualTypeArguments()[0];
	}
		
	public T findById(ID id) {
		EntityManager entityManager = getEntityManager();
		List<T> result = new ArrayList<>();
		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<T> criteriaQuery = criteriaBuilder.createQuery(this.modelClass);
		Root<T> root = criteriaQuery.from(this.modelClass);
		Predicate predicate = criteriaBuilder.and();

		predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(root.get("id"), id));

		criteriaQuery.where(predicate);
		TypedQuery<T> typedQuery = entityManager.createQuery(criteriaQuery);

		result = typedQuery.getResultList();

		return result.get(0);
	}

	@Transactional
	public T save(T object) throws EntityExistsException, IllegalArgumentException, TransactionRequiredException {
		getEntityManager().persist(object);
		return object;
	}

	@Transactional
	public T update(T object) throws IllegalArgumentException, TransactionRequiredException {
		return getEntityManager().merge(object);
	}
	
	@Transactional
	public T saveOrUpdate(T object) throws IllegalArgumentException, TransactionRequiredException {
		if (object.getId() != null)
			getEntityManager().merge(object);
		else
			getEntityManager().persist(object);

		return object;
	}

	@Transactional
	public void delete(T object) throws IllegalArgumentException, TransactionRequiredException {
		object = getEntityManager().merge(object);
		getEntityManager().remove(object);
	}

	public List<T> fetchAll(String orderBy) {
		StringBuffer sb = new StringBuffer("SELECT obj FROM " + this.modelClass.getSimpleName() + " obj ");
		if (orderBy != null) {
			sb.append("order by " + orderBy);
		}

		TypedQuery<T> query = getEntityManager().createQuery(sb.toString(), this.modelClass);

		return query.getResultList();
	}

	public Long count() {
		EntityManager entityManager = getEntityManager();
		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);
		criteriaQuery.select(criteriaBuilder.count(criteriaQuery.from(this.modelClass)));

		return entityManager.createQuery(criteriaQuery).getSingleResult();
	}

	public EntityManager getEntityManager() {		
		return JPAUtils.JpaEntityManager();
	}
}
