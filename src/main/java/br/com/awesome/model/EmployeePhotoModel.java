package br.com.awesome.model;

import java.util.Arrays;

import br.com.awesome.model.generic.AbstractModel;

public class EmployeePhotoModel extends AbstractModel {

	private static final long serialVersionUID = 1L;

	private EmployeeModel employee;

	private byte[] photo;

	public EmployeeModel getEmployee() {
		return employee;
	}

	public void setEmployee(EmployeeModel employee) {
		this.employee = employee;
	}

	public byte[] getPhoto() {
		return photo;
	}

	public void setPhoto(byte[] photo) {
		this.photo = photo;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((employee == null) ? 0 : employee.hashCode());
		result = prime * result + Arrays.hashCode(photo);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		EmployeePhotoModel other = (EmployeePhotoModel) obj;
		if (employee == null) {
			if (other.employee != null)
				return false;
		} else if (!employee.equals(other.employee))
			return false;
		if (!Arrays.equals(photo, other.photo))
			return false;
		return true;
	}
}
