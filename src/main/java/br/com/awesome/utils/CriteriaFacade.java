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
	private final Class<E> entityClass;
	private final EntityManager entityManager;
	private final CriteriaBuilder criteriaBuilder;
	private final CriteriaQuery<E> criteriaQuery;
	private final Root<E> root;
	private Predicate predicate;

	@SuppressWarnings("unchecked")
	public CriteriaFacade(EntityManager entityManager) {
		this.entityClass = (Class<E>) ((ParameterizedType) getClass().getGenericSuperclass())
				.getActualTypeArguments()[1];
		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		this.entityManager = entityManager;
		this.criteriaBuilder = criteriaBuilder;
		criteriaQuery = criteriaBuilder.createQuery(this.entityClass);
		root = criteriaQuery.from(this.entityClass);
		predicate = criteriaBuilder.and();
	}

	public void addAnd(String field, Object value) {
		predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(root.get(field), value));
	}

	public void addOr(String field, Object value) {
		predicate = criteriaBuilder.or(predicate, criteriaBuilder.equal(root.get(field), value));
	}

	public Predicate and(String field, Object value) {
		Predicate predicate = criteriaBuilder.and();
		return criteriaBuilder.and(predicate, criteriaBuilder.equal(root.get(field), value));
	}

	public Predicate or(String field, Object value) {
		Predicate predicate = criteriaBuilder.and();
		return criteriaBuilder.or(predicate, criteriaBuilder.equal(root.get(field), value));
	}

	public void and(Predicate... predicate) {
		Arrays.asList(predicate).stream().forEach(p -> this.predicate = criteriaBuilder.and(this.predicate, p));
	}

	public void or(Predicate... predicate) {
		Arrays.asList(predicate).stream().forEach(p -> this.predicate = criteriaBuilder.or(this.predicate, p));
	}

	public List<M> getResultList() {
		criteriaQuery.where(predicate);
		TypedQuery<E> typedQuery = entityManager.createQuery(criteriaQuery);

		List<E> result = typedQuery.getResultList();

		@SuppressWarnings("unchecked")
		List<M> listModel = (List<M>) result.stream().map(e -> {
			AbstractModel ret = null;
			try {
				ret = EntityUtils.convert2Model(e);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			return ret;
		}).collect(Collectors.toList());

		return listModel;
	}

	public M getSingleResult() {
		criteriaQuery.where(predicate);
		TypedQuery<E> typedQuery = entityManager.createQuery(criteriaQuery);

		E result = typedQuery.getSingleResult();

		M model = null;

		try {
			EntityUtils.copyEntity2Model(result, model);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return model;
	}
}
