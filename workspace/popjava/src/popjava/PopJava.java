package popjava;

import popjava.base.POPException;
import popjava.baseobject.ObjectDescription;
import popjava.baseobject.POPAccessPoint;
import popjava.buffer.Buffer;

/**
 * 
 * This class is used to create parallel object. All the methods from this class are static so no instantiation is needed.
 *
 */
public class PopJava {

	/** Creates a new instance of PopJava */
	public PopJava() {
	}

	/**
	 * Static method used to create a new parallel object by passing an object description
	 * @param targetClass			the parallel class to be created
	 * @param objectDescription		the object description for the resource requirements 
	 * @param argvs					arguments of the constructor (may be empty)
	 * @return references to the parallel object created
	 * @throws POPException 
	 */
	public static Object newActive(Class<?> targetClass,
			ObjectDescription objectDescription, Object... argvs)
			throws POPException {
		PJProxyFactory factoryProxy = new PJProxyFactory(targetClass);
		return factoryProxy.newPOPObject(objectDescription, argvs);
	}
	
	/**
	 * Static method used to create a new parallel object
	 * @param targetClass	the parallel class to be created
	 * @param argvs			arguments of the constructor (may be empty)
	 * @return references to the parallel object created
	 * @throws POPException
	 */
	public static Object newActive(Class<?> targetClass, Object... argvs)
			throws POPException {
		PJProxyFactory factoryProxy = new PJProxyFactory(targetClass);
		return factoryProxy.newPOPObject(argvs);
	}

	/**
	 * Static method used to create a parallel object from an existing access point
	 * @param targetClass	the parallel class to be created
	 * @param accessPoint	access point of the living object
	 * @return references to the parallel object
	 * @throws POPException
	 */
	public static Object newActive(Class<?> targetClass,
			POPAccessPoint accessPoint) throws POPException {
		PJProxyFactory factoryProxy = new PJProxyFactory(targetClass);
		return factoryProxy.bindPOPObject(accessPoint);
	}

	/**
	 * Static method used to create a parallel object from the buffer
	 * @param targetClass	the parallel class to be recovered
	 * @param buffer		buffer from which the object must be recovered
	 * @return references to the parallel object
	 * @throws POPException
	 */
	public static Object newActiveFromBuffer(Class<?> targetClass, Buffer buffer)
			throws POPException {
		PJProxyFactory factoryProxy = new PJProxyFactory(targetClass);
		return factoryProxy.newActiveFromBuffer(buffer);
	}
}