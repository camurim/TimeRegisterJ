package br.com.awesome.controller.generic;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.util.List;

import javax.enterprise.inject.spi.CDI;

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
	private final Class<T> persistenceClass;
	private GenericRepository<T, Long> repository;
	
	@SuppressWarnings("unchecked")
	public GenericController() {
		this.persistenceClass = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass())
				.getActualTypeArguments()[0];
		try {
			this.model = (T) (Class.forName(this.persistenceClass.getCanonicalName()).newInstance());
			this.filter = (T) (Class.forName(this.persistenceClass.getCanonicalName()).newInstance());
		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public void injectRepository() throws ClassNotFoundException {
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
}
