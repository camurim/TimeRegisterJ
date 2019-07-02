package br.com.awesome.controller.generic;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.List;

import javax.enterprise.inject.spi.CDI;
import javax.persistence.TransactionRequiredException;

import br.com.awesome.exception.AnnotationNotFoundException;
import br.com.awesome.model.generic.AbstractModel;
import br.com.awesome.repository.entity.generic.AbstractEntity;
import br.com.awesome.repository.generic.GenericRepository;
import br.com.awesome.repository.generic.JpaRepository;

public class GenericController <M extends AbstractModel, E extends AbstractEntity, R extends GenericRepository<M, E, Long>> implements Serializable{

	private static final long serialVersionUID = 1L;
	
	protected M model, filter;
	protected R repo;
	protected List<M> listData;
	protected List<M> suggestions;
	protected boolean fetchAll;
	protected String orderBy = null;
	private final Class<M> modelClass;
	private GenericRepository<M, E, Long> repository;
	
	@SuppressWarnings("unchecked")
	public GenericController() throws ClassNotFoundException, AnnotationNotFoundException {
		injectRepository();
		this.modelClass = (Class<M>) ((ParameterizedType) getClass().getGenericSuperclass())
				.getActualTypeArguments()[0];
		try {
			this.model = (M) (Class.forName(this.modelClass.getCanonicalName()).newInstance());
			this.filter = (M) (Class.forName(this.modelClass.getCanonicalName()).newInstance());
		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public void save() throws IllegalArgumentException, TransactionRequiredException, NoSuchMethodException, SecurityException, IllegalAccessException, InvocationTargetException, NoSuchFieldException {
		this.repository.saveOrUpdate(this.model);
	}
	
	public void delete() throws IllegalArgumentException, TransactionRequiredException, NoSuchMethodException, SecurityException, IllegalAccessException, InvocationTargetException, NoSuchFieldException {
		this.repository.delete(model);
	}
	
	public List<M> getListData() {
		if (this.listData == null) {
			this.listData = new ArrayList<M>();
		}

		if (this.listData.size() == 0 && this.fetchAll) {
			this.listData = repository.fetchAll(this.orderBy);
		}

		return this.listData;
	}
	
	public void clear() throws InstantiationException, IllegalAccessException {
		listData = new ArrayList<M>();
		model = this.modelClass.newInstance();
		filter = this.modelClass.newInstance();
		fetchAll = true;
	}
	
	//---------------------------------------------------------------------------------
	// Private methods
	
	private void injectRepository() throws ClassNotFoundException, AnnotationNotFoundException {
		Class<?> cl = this.getClass();
		if (cl.isAnnotationPresent(JpaRepository.class)) {
			JpaRepository jpaRepository = cl.getAnnotation(JpaRepository.class);
			this.repository = findRepo(jpaRepository.name());	
		} else {
			throw new AnnotationNotFoundException("Annotation JpaRepository not found in class");
		}
	}
	
	@SuppressWarnings("unchecked")
	private R findRepo(String repoName) throws ClassNotFoundException {
		String fullQualifiedName = "br.com.awesome.repository." + repoName;
		return (R) CDI.current().select(Class.forName(fullQualifiedName)).get();
	}
	
	//---------------------------------------------------------------------------------
	// Getter's & Setter's
	
	public M getModel() {
		return this.model;
	}
	
	public void setModel(M model) {
		this.model = model;
	}

	public M getFilter() {
		return this.filter;
	}
	
	public void setFilter(M filter) {
		this.filter = filter;
	}
}
