/*
 */
package org.eclipse.fennec.data.atlas.mapping.model.jpamapping.impl;

import org.eclipse.emf.common.notify.Notification;

import org.eclipse.emf.ecore.EClass;

import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.MinimalEObjectImpl;

import org.eclipse.fennec.data.atlas.mapping.model.jpamapping.DataSourceConfig;
import org.eclipse.fennec.data.atlas.mapping.model.jpamapping.JPAMappingPackage;
import org.eclipse.fennec.data.atlas.mapping.model.jpamapping.SqlDialect;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Data Source Config</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.data.atlas.mapping.model.jpamapping.impl.DataSourceConfigImpl#getDriverClass <em>Driver Class</em>}</li>
 *   <li>{@link org.eclipse.fennec.data.atlas.mapping.model.jpamapping.impl.DataSourceConfigImpl#getJdbcUrl <em>Jdbc Url</em>}</li>
 *   <li>{@link org.eclipse.fennec.data.atlas.mapping.model.jpamapping.impl.DataSourceConfigImpl#getUsername <em>Username</em>}</li>
 *   <li>{@link org.eclipse.fennec.data.atlas.mapping.model.jpamapping.impl.DataSourceConfigImpl#getPasswordRef <em>Password Ref</em>}</li>
 *   <li>{@link org.eclipse.fennec.data.atlas.mapping.model.jpamapping.impl.DataSourceConfigImpl#getPoolSize <em>Pool Size</em>}</li>
 *   <li>{@link org.eclipse.fennec.data.atlas.mapping.model.jpamapping.impl.DataSourceConfigImpl#getDialect <em>Dialect</em>}</li>
 * </ul>
 *
 * @generated
 */
public class DataSourceConfigImpl extends MinimalEObjectImpl.Container implements DataSourceConfig {
	/**
	 * The default value of the '{@link #getDriverClass() <em>Driver Class</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getDriverClass()
	 * @generated
	 * @ordered
	 */
	protected static final String DRIVER_CLASS_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getDriverClass() <em>Driver Class</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getDriverClass()
	 * @generated
	 * @ordered
	 */
	protected String driverClass = DRIVER_CLASS_EDEFAULT;

	/**
	 * The default value of the '{@link #getJdbcUrl() <em>Jdbc Url</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getJdbcUrl()
	 * @generated
	 * @ordered
	 */
	protected static final String JDBC_URL_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getJdbcUrl() <em>Jdbc Url</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getJdbcUrl()
	 * @generated
	 * @ordered
	 */
	protected String jdbcUrl = JDBC_URL_EDEFAULT;

	/**
	 * The default value of the '{@link #getUsername() <em>Username</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getUsername()
	 * @generated
	 * @ordered
	 */
	protected static final String USERNAME_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getUsername() <em>Username</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getUsername()
	 * @generated
	 * @ordered
	 */
	protected String username = USERNAME_EDEFAULT;

	/**
	 * The default value of the '{@link #getPasswordRef() <em>Password Ref</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getPasswordRef()
	 * @generated
	 * @ordered
	 */
	protected static final String PASSWORD_REF_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getPasswordRef() <em>Password Ref</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getPasswordRef()
	 * @generated
	 * @ordered
	 */
	protected String passwordRef = PASSWORD_REF_EDEFAULT;

	/**
	 * The default value of the '{@link #getPoolSize() <em>Pool Size</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getPoolSize()
	 * @generated
	 * @ordered
	 */
	protected static final int POOL_SIZE_EDEFAULT = 0;

	/**
	 * The cached value of the '{@link #getPoolSize() <em>Pool Size</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getPoolSize()
	 * @generated
	 * @ordered
	 */
	protected int poolSize = POOL_SIZE_EDEFAULT;

	/**
	 * The default value of the '{@link #getDialect() <em>Dialect</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getDialect()
	 * @generated
	 * @ordered
	 */
	protected static final SqlDialect DIALECT_EDEFAULT = SqlDialect.H2;

	/**
	 * The cached value of the '{@link #getDialect() <em>Dialect</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getDialect()
	 * @generated
	 * @ordered
	 */
	protected SqlDialect dialect = DIALECT_EDEFAULT;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected DataSourceConfigImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return JPAMappingPackage.Literals.DATA_SOURCE_CONFIG;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getDriverClass() {
		return driverClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setDriverClass(String newDriverClass) {
		String oldDriverClass = driverClass;
		driverClass = newDriverClass;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, JPAMappingPackage.DATA_SOURCE_CONFIG__DRIVER_CLASS, oldDriverClass, driverClass));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getJdbcUrl() {
		return jdbcUrl;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setJdbcUrl(String newJdbcUrl) {
		String oldJdbcUrl = jdbcUrl;
		jdbcUrl = newJdbcUrl;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, JPAMappingPackage.DATA_SOURCE_CONFIG__JDBC_URL, oldJdbcUrl, jdbcUrl));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getUsername() {
		return username;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setUsername(String newUsername) {
		String oldUsername = username;
		username = newUsername;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, JPAMappingPackage.DATA_SOURCE_CONFIG__USERNAME, oldUsername, username));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getPasswordRef() {
		return passwordRef;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setPasswordRef(String newPasswordRef) {
		String oldPasswordRef = passwordRef;
		passwordRef = newPasswordRef;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, JPAMappingPackage.DATA_SOURCE_CONFIG__PASSWORD_REF, oldPasswordRef, passwordRef));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public int getPoolSize() {
		return poolSize;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setPoolSize(int newPoolSize) {
		int oldPoolSize = poolSize;
		poolSize = newPoolSize;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, JPAMappingPackage.DATA_SOURCE_CONFIG__POOL_SIZE, oldPoolSize, poolSize));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public SqlDialect getDialect() {
		return dialect;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setDialect(SqlDialect newDialect) {
		SqlDialect oldDialect = dialect;
		dialect = newDialect == null ? DIALECT_EDEFAULT : newDialect;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, JPAMappingPackage.DATA_SOURCE_CONFIG__DIALECT, oldDialect, dialect));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case JPAMappingPackage.DATA_SOURCE_CONFIG__DRIVER_CLASS:
				return getDriverClass();
			case JPAMappingPackage.DATA_SOURCE_CONFIG__JDBC_URL:
				return getJdbcUrl();
			case JPAMappingPackage.DATA_SOURCE_CONFIG__USERNAME:
				return getUsername();
			case JPAMappingPackage.DATA_SOURCE_CONFIG__PASSWORD_REF:
				return getPasswordRef();
			case JPAMappingPackage.DATA_SOURCE_CONFIG__POOL_SIZE:
				return getPoolSize();
			case JPAMappingPackage.DATA_SOURCE_CONFIG__DIALECT:
				return getDialect();
		}
		return super.eGet(featureID, resolve, coreType);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void eSet(int featureID, Object newValue) {
		switch (featureID) {
			case JPAMappingPackage.DATA_SOURCE_CONFIG__DRIVER_CLASS:
				setDriverClass((String)newValue);
				return;
			case JPAMappingPackage.DATA_SOURCE_CONFIG__JDBC_URL:
				setJdbcUrl((String)newValue);
				return;
			case JPAMappingPackage.DATA_SOURCE_CONFIG__USERNAME:
				setUsername((String)newValue);
				return;
			case JPAMappingPackage.DATA_SOURCE_CONFIG__PASSWORD_REF:
				setPasswordRef((String)newValue);
				return;
			case JPAMappingPackage.DATA_SOURCE_CONFIG__POOL_SIZE:
				setPoolSize((Integer)newValue);
				return;
			case JPAMappingPackage.DATA_SOURCE_CONFIG__DIALECT:
				setDialect((SqlDialect)newValue);
				return;
		}
		super.eSet(featureID, newValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void eUnset(int featureID) {
		switch (featureID) {
			case JPAMappingPackage.DATA_SOURCE_CONFIG__DRIVER_CLASS:
				setDriverClass(DRIVER_CLASS_EDEFAULT);
				return;
			case JPAMappingPackage.DATA_SOURCE_CONFIG__JDBC_URL:
				setJdbcUrl(JDBC_URL_EDEFAULT);
				return;
			case JPAMappingPackage.DATA_SOURCE_CONFIG__USERNAME:
				setUsername(USERNAME_EDEFAULT);
				return;
			case JPAMappingPackage.DATA_SOURCE_CONFIG__PASSWORD_REF:
				setPasswordRef(PASSWORD_REF_EDEFAULT);
				return;
			case JPAMappingPackage.DATA_SOURCE_CONFIG__POOL_SIZE:
				setPoolSize(POOL_SIZE_EDEFAULT);
				return;
			case JPAMappingPackage.DATA_SOURCE_CONFIG__DIALECT:
				setDialect(DIALECT_EDEFAULT);
				return;
		}
		super.eUnset(featureID);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean eIsSet(int featureID) {
		switch (featureID) {
			case JPAMappingPackage.DATA_SOURCE_CONFIG__DRIVER_CLASS:
				return DRIVER_CLASS_EDEFAULT == null ? driverClass != null : !DRIVER_CLASS_EDEFAULT.equals(driverClass);
			case JPAMappingPackage.DATA_SOURCE_CONFIG__JDBC_URL:
				return JDBC_URL_EDEFAULT == null ? jdbcUrl != null : !JDBC_URL_EDEFAULT.equals(jdbcUrl);
			case JPAMappingPackage.DATA_SOURCE_CONFIG__USERNAME:
				return USERNAME_EDEFAULT == null ? username != null : !USERNAME_EDEFAULT.equals(username);
			case JPAMappingPackage.DATA_SOURCE_CONFIG__PASSWORD_REF:
				return PASSWORD_REF_EDEFAULT == null ? passwordRef != null : !PASSWORD_REF_EDEFAULT.equals(passwordRef);
			case JPAMappingPackage.DATA_SOURCE_CONFIG__POOL_SIZE:
				return poolSize != POOL_SIZE_EDEFAULT;
			case JPAMappingPackage.DATA_SOURCE_CONFIG__DIALECT:
				return dialect != DIALECT_EDEFAULT;
		}
		return super.eIsSet(featureID);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String toString() {
		if (eIsProxy()) return super.toString();

		StringBuilder result = new StringBuilder(super.toString());
		result.append(" (driverClass: ");
		result.append(driverClass);
		result.append(", jdbcUrl: ");
		result.append(jdbcUrl);
		result.append(", username: ");
		result.append(username);
		result.append(", passwordRef: ");
		result.append(passwordRef);
		result.append(", poolSize: ");
		result.append(poolSize);
		result.append(", dialect: ");
		result.append(dialect);
		result.append(')');
		return result.toString();
	}

} //DataSourceConfigImpl
