package br.com.awesome.controller.generic;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.List;

import javax.enterprise.inject.spi.CDI;
import javax.persistence.TransactionRequiredException;

import br.com.awesome.model.generic.AbstractModel;
import br.com.awesome.repository.generic.GenericRepository;
import br.com.awesome.repository.generic.JpaRepository;

public class GenericController <T extends AbstractModel<T>, Y extends GenericRepository<T, Long>> implements Serializable{

	private static final long serialVersionUID = 1L;
	
	protected T model, filter;
	protected Y repo;
	protected List<T> listData;
	protected List<T> suggestions;
	protected boolean fetchAll;
	protected String orderBy = null;
	private final Class<T> modelClass;
	private GenericRepository<T, Long> repository;
	
	@SuppressWarnings("unchecked")
	public GenericController() throws ClassNotFoundException {
		injectRepository();
		this.modelClass = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass())
				.getActualTypeArguments()[0];
		try {
			this.model = (T) (Class.forName(this.modelClass.getCanonicalName()).newInstance());
			this.filter = (T) (Class.forName(this.modelClass.getCanonicalName()).newInstance());
		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public String save() {
		try {
			this.repository.saveOrUpdate(this.model);
		} catch (IllegalArgumentException | TransactionRequiredException e) {
		}
		return "";
	}
	
	public void delete() {
		try {
			this.repository.delete(model);
		} catch (IllegalArgumentException | TransactionRequiredException e) {
		}
	}
	
	public List<T> getListData() {
		if (this.listData == null) {
			this.listData = new ArrayList<T>();
		}

		if (this.listData.size() == 0 && this.fetchAll) {
			this.listData = repository.fetchAll(this.orderBy);
		}

		return this.listData;
	}
	
	public void clear() throws InstantiationException, IllegalAccessException {
		listData = new ArrayList<T>();
		model = this.modelClass.newInstance();
		filter = this.modelClass.newInstance();
		fetchAll = true;
	}
	
	private void injectRepository() throws ClassNotFoundException {
		Class<?> cl = this.getClass();
		if (cl.isAnnotationPresent(JpaRepository.class)) {
			JpaRepository jpaRepository = cl.getAnnotation(JpaRepository.class);
			this.repository = findRepo(jpaRepository.name());	
		}
	}
	
	@SuppressWarnings("unchecked")
	private Y findRepo(String repoName) throws ClassNotFoundException {
		String fullQualifiedName = "br.com.awesome.repository." + repoName;
		return (Y) CDI.current().select(Class.forName(fullQualifiedName)).get();
	}
	
	//---------------------------------------------------------------------------------
	// Getter's & Setter's
	
	public T getModel() {
		return this.model;
	}
	
	public void setModel(T model) {
		this.model = model;
	}
	
	public void setFilter(T filter) {
		this.filter = filter;
	}
	
	public T getFilter() {
		return this.filter;
	}
}
