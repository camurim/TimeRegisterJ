package br.com.awesome.utils;

import java.lang.reflect.ParameterizedType;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import br.com.awesome.model.generic.AbstractModel;
import br.com.awesome.repository.entity.generic.AbstractEntity;

public class CriteriaFacade<M extends AbstractModel, E extends AbstractEntity> {
	private final Class<M> modelClass;
	private final Class<E> entityClass;
	private final EntityManager entityManager;
	private final CriteriaBuilder criteriaBuilder;
	private final CriteriaQuery<E> criteriaQuery;
	private final Root<E> root;

	@SuppressWarnings("unchecked")
	public CriteriaFacade(EntityManager entityManager) {
		this.modelClass = (Class<M>) ((ParameterizedType) getClass().getGenericSuperclass())
				.getActualTypeArguments()[0];
		this.entityClass = (Class<E>) ((ParameterizedType) getClass().getGenericSuperclass())
				.getActualTypeArguments()[1];
		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		this.entityManager = entityManager;
		this.criteriaBuilder = criteriaBuilder;
		criteriaQuery = criteriaBuilder.createQuery(entityClass);
		root = criteriaQuery.from(entityClass);
	}

	//------------------------------------------------------------------
	//-- Predicate Methods
	public Predicate equal(String field, Object value) {
		return criteriaBuilder.equal(root.get(field), value);
	}

	public Predicate notEqual(String field, Object value) {
		return criteriaBuilder.notEqual(root.get(field), value);
	}

	public Predicate like(String field, String value) {
		return criteriaBuilder.like(root.get(field), value);
	}

	public Predicate notLike(String field, String value) {
		return criteriaBuilder.notLike(root.get(field), value);
	}

	public <T extends Comparable<? super T>> Predicate greaterThan(String field, T value) {
		return criteriaBuilder.greaterThan(root.<T>get(field), value);
	}

	public <T extends Comparable<? super T>> Predicate lessThan(String field, T value) {
		return criteriaBuilder.lessThan(root.<T>get(field), value);
	}

	public <T extends Comparable<? super T>> Predicate greaterThanOrEqualTo(String field, T value) {
		return criteriaBuilder.greaterThanOrEqualTo(root.<T>get(field), value);
	}

	public <T extends Comparable<? super T>> Predicate lessThanOrEqualTo(String field, T value) {
		return criteriaBuilder.lessThanOrEqualTo(root.<T>get(field), value);
	}

	public <T extends Comparable<? super T>> Predicate between(String field, T value1, T value2) {
		return criteriaBuilder.between(root.<T>get(field), value1, value2);
	}

	public Predicate and(Predicate... predicate) {
		Predicate ret = criteriaBuilder.and();
		for (Predicate p : Arrays.asList(predicate)) {
			ret = criteriaBuilder.and(ret, p);
		}

		return ret;
	}

	public Predicate or(Predicate... predicate) {
		Predicate ret = criteriaBuilder.and();
		for (Predicate p : Arrays.asList(predicate)) {
			ret = criteriaBuilder.or(ret, p);
		}

		return ret;
	}

	public void where(Predicate predicate) {
		criteriaQuery.where(predicate);
	}

	//------------------------------------------------------------------
	//-- Result Methods
	public List<M> getResultList() {
		TypedQuery<E> typedQuery = entityManager.createQuery(criteriaQuery);

		List<E> result = typedQuery.getResultList();

		@SuppressWarnings("unchecked")
		List<M> listModel = (List<M>) result.stream().map(e -> {
			M ret = null;
			try {
				ret = (M) EntityUtils.convert2Model(e);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			return ret;
		}).collect(Collectors.<M>toList());

		return listModel;
	}

	public M getSingleResult() {
		TypedQuery<E> typedQuery = entityManager.createQuery(criteriaQuery);

		E result = typedQuery.getSingleResult();

		M model = null;
		try {
			model = this.modelClass.newInstance();
		} catch (InstantiationException | IllegalAccessException e1) {
			e1.printStackTrace();
		}

		try {
			EntityUtils.copyEntity2Model(result, model);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return model;
	}
}
