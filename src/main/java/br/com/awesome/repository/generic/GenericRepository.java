package br.com.awesome.repository.generic;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
import br.com.awesome.utils.EntityUtils;
import br.com.awesome.utils.JPAUtils;

public class GenericRepository<M extends AbstractModel, E extends AbstractEntity, ID extends Number> {
	private final Class<E> entityClass;

	@SuppressWarnings("unchecked")
	public GenericRepository() {
		this.entityClass = (Class<E>) ((ParameterizedType) getClass().getGenericSuperclass())
				.getActualTypeArguments()[1];
	}

	public M findById(ID id) throws NoSuchMethodException, SecurityException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException, NoSuchFieldException {
		EntityManager entityManager = getEntityManager();
		List<E> result = new ArrayList<>();
		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<E> criteriaQuery = criteriaBuilder.createQuery(this.entityClass);
		Root<E> root = criteriaQuery.from(this.entityClass);
		Predicate predicate = criteriaBuilder.and();

		predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(root.get("id"), id));

		criteriaQuery.where(predicate);
		TypedQuery<E> typedQuery = entityManager.createQuery(criteriaQuery);

		result = typedQuery.getResultList();

		M model = null;

		EntityUtils.copyEntity2Model(result.get(0), model);

		return model;
	}

	@Transactional
	public E save(M object)
			throws EntityExistsException, IllegalArgumentException, TransactionRequiredException, NoSuchMethodException,
			SecurityException, IllegalAccessException, InvocationTargetException, NoSuchFieldException {
		E entity = null;

		EntityUtils.copyModel2Entity(object, entity);

		getEntityManager().persist(entity);

		return entity;
	}

	@Transactional
	public E update(M object) throws IllegalArgumentException, TransactionRequiredException, NoSuchMethodException,
			SecurityException, IllegalAccessException, InvocationTargetException, NoSuchFieldException {
		E entity = null;

		EntityUtils.copyModel2Entity(object, entity);

		return getEntityManager().merge(entity);
	}

	@Transactional
	public E saveOrUpdate(M object)
			throws IllegalArgumentException, TransactionRequiredException, NoSuchMethodException, SecurityException,
			IllegalAccessException, InvocationTargetException, NoSuchFieldException {
		E entity = null;

		EntityUtils.copyModel2Entity(object, entity);

		if (object.getId() != null)
			getEntityManager().merge(entity);
		else
			getEntityManager().persist(entity);

		return entity;
	}

	@Transactional
	public void delete(M object) throws IllegalArgumentException, TransactionRequiredException, NoSuchMethodException,
			SecurityException, IllegalAccessException, InvocationTargetException, NoSuchFieldException {
		E entity = null;

		EntityUtils.copyModel2Entity(object, entity);

		entity = getEntityManager().merge(entity);
		getEntityManager().remove(entity);
	}

	public List<M> fetchAll(String orderBy) {
		StringBuffer sb = new StringBuffer("SELECT obj FROM " + this.entityClass.getSimpleName() + " obj ");
		if (orderBy != null) {
			sb.append("order by " + orderBy);
		}

		TypedQuery<E> query = getEntityManager().createQuery(sb.toString(), this.entityClass);
		
		List<E> listEntity = query.getResultList();
		
		@SuppressWarnings("unchecked")
		List<M> listModel = (List<M>) listEntity.stream().map(e -> {
			try {
				return EntityUtils.convertToModel(e);
			} catch (InstantiationException | IllegalAccessException | ClassNotFoundException | NoSuchMethodException
					| SecurityException | IllegalArgumentException | InvocationTargetException
					| NoSuchFieldException e1) {
				e1.printStackTrace();
				return null;
			}
		}).collect(Collectors.toList());

		return listModel;
	}

	public Long count() {
		EntityManager entityManager = getEntityManager();
		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);
		criteriaQuery.select(criteriaBuilder.count(criteriaQuery.from(this.entityClass)));

		return entityManager.createQuery(criteriaQuery).getSingleResult();
	}

	public EntityManager getEntityManager() {
		return JPAUtils.JpaEntityManager();
	}
}
