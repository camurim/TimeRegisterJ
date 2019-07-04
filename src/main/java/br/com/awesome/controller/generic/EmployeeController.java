package br.com.awesome.controller.generic;

import br.com.awesome.exception.AnnotationNotFoundException;
import br.com.awesome.model.EmployeeModel;
import br.com.awesome.repository.EmployeeRepository;
import br.com.awesome.repository.entity.EmployeeEntity;

public class EmployeeController extends GenericController<EmployeeModel, EmployeeEntity, EmployeeRepository>{

	private static final long serialVersionUID = 1L;

	public EmployeeController() throws ClassNotFoundException, AnnotationNotFoundException {
		super();
	}

}
