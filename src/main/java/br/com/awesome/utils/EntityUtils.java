package br.com.awesome.utils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import javax.persistence.Transient;

import br.com.awesome.model.generic.AbstractModel;
import br.com.awesome.repository.entity.generic.AbstractEntity;

public class EntityUtils {
	public static boolean compareEntities(final AbstractEntity eSource, final AbstractEntity eTarget)
			throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException,
			InvocationTargetException {
		boolean ret = true;

		Class<?> clSource = eSource.getClass();
		Class<?> clTarget = eTarget.getClass();

		if (clSource.equals(clTarget)) {

			List<String> fieldAnnotated = getClassTransientFields(clSource);

			for (Method mSource : clSource.getDeclaredMethods()) {
				if (!mSource.getName().equalsIgnoreCase("getId") && mSource.getName().substring(0, 3).equals("get")
						&& !fieldAnnotated.contains(methodToField(mSource.getName()))) {

					Method mTarget = clTarget.getMethod(mSource.getName());

					if (mSource.invoke(eSource) != null
							&& AbstractEntity.class.isAssignableFrom(mSource.invoke(eSource).getClass())) {
						if (!mSource.invoke(eSource).equals(mTarget.invoke(eTarget))) {
							ret = false;
							break;
						}
					} else {
						if (mSource.invoke(eSource) != mTarget.invoke(eTarget)) {
							ret = false;
							break;
						}
					}
				}
			}
		} else {
			ret = false;
		}

		return ret;
	}

	public static void copyEntity(final AbstractEntity eSource, final AbstractEntity eTarget)
			throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException,
			InvocationTargetException, NoSuchFieldException {

		Class<?> clSource = eSource.getClass();
		Class<?> clTarget = eTarget.getClass();

		if (clSource.equals(clTarget)) {

			List<String> fieldAnnotated = getClassTransientFields(clSource);

			for (Method mSource : clSource.getDeclaredMethods()) {
				if (!fieldAnnotated.contains(methodToField(mSource.getName()))) {
					if (mSource.getName().substring(0, 3).equals("get")
							|| mSource.getName().substring(0, 2).equals("is")) {
						if (mSource.invoke(eSource) != null) {

							Method mTarget = null;
							if (mSource.getName().substring(0, 3).equals("get")) {
								Class<?> parClass = null;
								if (Timestamp.class.isAssignableFrom(mSource.invoke(eSource).getClass())) {
									parClass = Date.class;
								} else {
									parClass = mSource.invoke(eSource).getClass();
								}
								mTarget = clTarget.getMethod("set".concat(mSource.getName().substring(3)),
										new Class[] { parClass });
							} else if (mSource.getName().substring(0, 2).equals("is")) {
								mTarget = clTarget.getMethod("set".concat(mSource.getName().substring(2)),
										new Class[] { boolean.class });
							}

							if (mTarget != null) {
								mTarget.invoke(eTarget, mSource.invoke(eSource));
							}
						}
					}
				}
			}
		}
	}

	public static void copyEntityInherited(final AbstractEntity eSource, final AbstractEntity eTarget)
			throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException,
			InvocationTargetException, NoSuchFieldException {

		Class<?> clSource = eSource.getClass();
		Class<?> clTarget = eTarget.getClass();

		if (clSource.equals(clTarget)) {

			List<String> fieldAnnotated = getClassTransientFields(clSource);

			for (Method mSource : clSource.getMethods()) {
				if (!fieldAnnotated.contains(methodToField(mSource.getName()))
						&& !mSource.getName().equals("getClass")) {
					if (mSource.getName().substring(0, 3).equals("get")
							|| mSource.getName().substring(0, 2).equals("is")) {
						if (mSource.invoke(eSource) != null) {

							Method mTarget = null;
							if (mSource.getName().substring(0, 3).equals("get")) {
								Class<?> parClass = null;
								if (Timestamp.class.isAssignableFrom(mSource.invoke(eSource).getClass())) {
									parClass = Date.class;
								} else {
									parClass = mSource.invoke(eSource).getClass();
								}
								mTarget = clTarget.getMethod("set".concat(mSource.getName().substring(3)),
										new Class[] { parClass });
							} else if (mSource.getName().substring(0, 2).equals("is")) {
								mTarget = clTarget.getMethod("set".concat(mSource.getName().substring(2)),
										new Class[] { boolean.class });
							}

							if (mTarget != null) {
								mTarget.invoke(eTarget, mSource.invoke(eSource));
							}
						}
					}
				}
			}
		}
	}

	public static void copyModel2Entity(final AbstractModel model, final AbstractEntity entity)
			throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException,
			InvocationTargetException, NoSuchFieldException {

		Class<?> clSource = model.getClass();
		Class<?> clTarget = entity.getClass();

		for (Method mSource : clSource.getDeclaredMethods()) {
			if (methodExists(mSource.getName(), clTarget)) {
				if (mSource.getName().substring(0, 3).equals("get") || mSource.getName().substring(0, 2).equals("is")) {
					if (mSource.invoke(model) != null) {

						Method mTarget = null;
						if (mSource.getName().substring(0, 3).equals("get")) {
							Class<?> parClass = null;
							if (Timestamp.class.isAssignableFrom(mSource.invoke(model).getClass())) {
								parClass = Date.class;
							} else {
								parClass = mSource.invoke(model).getClass();
							}
							mTarget = clTarget.getMethod("set".concat(mSource.getName().substring(3)),
									new Class[] { parClass });
						} else if (mSource.getName().substring(0, 2).equals("is")) {
							mTarget = clTarget.getMethod("set".concat(mSource.getName().substring(2)),
									new Class[] { boolean.class });
						}

						if (mTarget != null) {
							mTarget.invoke(entity, mSource.invoke(model));
						}
					}
				}
			}
		}
	}

	public static void copyEntity2Model(final AbstractEntity entity, final AbstractModel model)
			throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException,
			InvocationTargetException, NoSuchFieldException {

		Class<?> clSource = entity.getClass();
		Class<?> clTarget = model.getClass();

		List<String> fieldAnnotated = getClassTransientFields(clSource);

		for (Method mSource : clSource.getDeclaredMethods()) {
			if (!fieldAnnotated.contains(methodToField(mSource.getName()))
					&& methodExists(mSource.getName(), clTarget)) {
				if (mSource.getName().substring(0, 3).equals("get") || mSource.getName().substring(0, 2).equals("is")) {
					if (mSource.invoke(entity) != null) {

						Method mTarget = null;
						if (mSource.getName().substring(0, 3).equals("get")) {
							Class<?> parClass = null;
							if (Timestamp.class.isAssignableFrom(mSource.invoke(entity).getClass())) {
								parClass = Date.class;
							} else {
								parClass = mSource.invoke(entity).getClass();
							}
							mTarget = clTarget.getMethod("set".concat(mSource.getName().substring(3)),
									new Class[] { parClass });
						} else if (mSource.getName().substring(0, 2).equals("is")) {
							mTarget = clTarget.getMethod("set".concat(mSource.getName().substring(2)),
									new Class[] { boolean.class });
						}

						if (mTarget != null) {
							mTarget.invoke(model, mSource.invoke(entity));
						}
					}
				}
			}
		}
	}

	public static AbstractEntity convert2Entity(final AbstractModel model)
			throws InstantiationException, IllegalAccessException, ClassNotFoundException, NoSuchMethodException,
			SecurityException, IllegalArgumentException, InvocationTargetException, NoSuchFieldException {
		final Class<?> cl = model.getClass();
		AbstractEntity entity = model2Entity(cl);

		copyModel2Entity(model, entity);

		return entity;
	}

	public static AbstractModel convert2Model(final AbstractEntity entity)
			throws InstantiationException, IllegalAccessException, ClassNotFoundException, NoSuchMethodException,
			SecurityException, IllegalArgumentException, InvocationTargetException, NoSuchFieldException {
		final Class<?> cl = entity.getClass();
		AbstractModel model = entity2Model(cl);

		copyEntity2Model(entity, model);

		return model;
	}

	private static AbstractEntity model2Entity(final Class<?> cl)
			throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		final String fqn = "br.com.awesome.repository.entity.";
		AbstractEntity instance = null;
		if (AbstractModel.class.isAssignableFrom(cl)) {
			String modelName = cl.getName();
			String entityName = modelName.substring(0, modelName.length() - 5).concat("Entity");

			instance = (AbstractEntity) Class.forName(fqn.concat(entityName)).newInstance();
		}

		return instance;
	}

	private static AbstractModel entity2Model(final Class<?> cl)
			throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		final String fqn = "br.com.awesome.repository.model.";
		AbstractModel instance = null;
		if (AbstractEntity.class.isAssignableFrom(cl)) {
			String entityName = cl.getName();
			String modelName = entityName.substring(0, entityName.length() - 5).concat("Model");

			instance = (AbstractModel) Class.forName(fqn.concat(modelName)).newInstance();
		}

		return instance;
	}

	private static List<String> getClassTransientFields(final Class<?> cl) {
		List<String> fieldAnnotated = Arrays.stream(cl.getDeclaredFields())
				.filter(f -> f.isAnnotationPresent(Transient.class)).map(f -> f.getName()).collect(Collectors.toList());
		return fieldAnnotated;
	}

	private static String methodToField(final String methodName) {
		String fieldName = null;
		if (methodName.substring(0, 3).equals("get")) {
			fieldName = methodName.substring(3, 4).toLowerCase().concat(methodName.substring(4));
		} else if (methodName.substring(0, 2).equals("is")) {
			fieldName = methodName.substring(2, 3).toLowerCase().concat(methodName.substring(3));
		}

		return fieldName;
	}

	private static boolean methodExists(String methodName, Class<?> cl) {
		boolean ret = true;

		try {
			Arrays.stream(cl.getDeclaredFields()).filter(i -> i.getName().equals(methodName)).findFirst().get();
		} catch (NoSuchElementException e) {
			ret = false;
		}

		return ret;
	}
}
