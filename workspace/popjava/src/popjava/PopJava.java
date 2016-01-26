package popjava;

import popjava.base.POPErrorCode;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.List;

import popjava.base.POPException;
import popjava.base.POPObject;
import popjava.baseobject.ObjectDescription;
import popjava.baseobject.POPAccessPoint;
import popjava.buffer.POPBuffer;
import popjava.codemanager.AppService;
import popjava.dataswaper.ObjectDescriptionInput;
import popjava.dataswaper.POPSearchNodeInfo;
import popjava.dataswaper.POPSearchNodeInfos;
import popjava.dataswaper.POPString;
import popjava.serviceadapter.POPAppService;
import popjava.serviceadapter.POPJobManager;
import popjava.serviceadapter.POPJobService;
import popjava.system.POPSystem;
import popjava.util.ClassUtil;
import popjava.util.Configuration;
import popjava.util.LogWriter;
import popjava.util.ClassUtil;

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
	public static <T> T newActive(Class<T> targetClass,
			ObjectDescription objectDescription, Object ... argvs)
			throws POPException {
	    POPSystem.start();
	    
T remoteObject = null;
		
		/* Load the ObjectDescription */
		Class<?>[] parameterTypes = ClassUtil.getObjectTypes(argvs);
		Constructor<?> constructor = null;
		try {
			try {
				constructor = targetClass.getConstructor(parameterTypes);
			} catch (Exception ex) {
				constructor = targetClass.getConstructor();
			}
		} catch (Exception e) {
			LogWriter.writeExceptionLog(e);
		}
		ObjectDescription od = new ObjectDescription();
		POPObject.loadDynamicOD(constructor, od, argvs);
		od.merge(objectDescription);
		
		/*
		 * hostname is not given, so find available computer with the
		 * job manager
		 */
		if (od.getHostName().length() <= 0) {
			
			/* Create an access point to contact the job manager */
			String jobUrl = od.getJobUrl();
			POPAccessPoint jobContact = new POPAccessPoint();
			if (jobUrl.length() > 0) {
				jobContact.setAccessString(jobUrl);
			} else {
				jobContact = POPSystem.jobService;
			}
			if (jobContact.isEmpty()) {
				jobContact.setAccessString(String.format("%s:%d", POPSystem.getHostIP(), POPJobManager.DEFAULT_PORT));
			}
			
			/* Bind to the job manager */
			POPJobManager jobManager = null;
			try {
				if (Configuration.CONNECT_TO_POPCPP) {
					jobManager = PopJava.newActive(POPJobManager.class, jobContact);
				}
			} catch (Exception e) {
			}
			if (jobManager == null) {
				LogWriter
						.writeDebugInfo("Could not contact jobmanager, running objects without od.url is not supported");
				POPException.throwObjectNoResource();
			}
			
			String platforms = od.getPlatform();
			AppService appCoreService = PopJava.newActive(POPAppService.class, POPSystem.appServiceAccessPoint);
			;
			if (platforms.length() <= 0) {
				POPString popStringPlatorm = new POPString();
				appCoreService.getPlatform(targetClass.getName(), popStringPlatorm);
				platforms = popStringPlatorm.getValue();
				if (platforms.length() <= 0) {
					throw new POPException(POPErrorCode.OBJECT_EXECUTABLE_NOTFOUND, "OBJECT_EXECUTABLE_NOTFOUND");
				}
				od.setPlatform(platforms);
			}
			
			/*
			 * Launch a discovery of available/suitable machines
			 */
			// input parameters
			ObjectDescriptionInput constOd = new ObjectDescriptionInput(od);
			String appId = appCoreService.getPOPCAppID();
			// output parameters
			POPSearchNodeInfos machines = new POPSearchNodeInfos();
			String requestId = "";
			jobManager.findAvailableMachines(constOd, appId, machines, requestId);
			
			/* No available/suitable computers */
			if (machines.getNodeInfos().size() == 0) {
				POPException.throwObjectNoResource();
			}
			
			/*
			 * Find the "chooseRemote" method to let the programmer choose the
			 * remote computer
			 */
			Method chooseRemoteMethod = null;
			try {
				chooseRemoteMethod = targetClass.getMethod("popChooseRemote", List.class);
			} catch (Exception e) {
				// should never happen, the chooseRemote is defined in the
				// POPObject class
				LogWriter.writeExceptionLog(e);
			}
			
			/* Call the "chooseRemote" static method */
			POPSearchNodeInfo chosenRemote = null;
			try {
				chosenRemote = (POPSearchNodeInfo) chooseRemoteMethod.invoke(null, machines.getNodeInfos());
			} catch (Exception e) {
				LogWriter.writeExceptionLog(e);
				throw new POPException(POPErrorCode.ALLOCATION_EXCEPTION, "Class \""
						+ chooseRemoteMethod.getClass().getName()
						+ "\", something is wrong with the \"popChooseRemote\" method: " + e.getMessage());
			}
			
			/* If chooseRemote wants to abort the reservation */
			if (chosenRemote == null) {
				POPException.throwObjectNoResource();
			}
			
			/* Parse the node id to get the IP of the remote computer */
			String ip = chosenRemote.getIP();
			if (ip == null) {
				throw new POPException(POPErrorCode.ALLOCATION_EXCEPTION, "Class \""
						+ chooseRemoteMethod.getClass().getName()
						+ "\", something is wrong with the \"popChooseRemote\" method:"
						+ "your choice doesn't have an IP address");
			}
			
			/* Update the hostname with the programmer choice */
			od.setHostname(ip);
		}
		
		/*
		 * Remote port is given, so the remote object should already be
		 * created. We normally just need to bind
		 */
		if (od.getConnectTo() > 0) {
			/*
			 * Constructs a new access point with the url+port of the remote
			 * existing object
			 */
			POPAccessPoint accessPoint = new POPAccessPoint();
			accessPoint.setAccessString(String.format("%s:%d", od.getHostName(), od.getConnectTo()));
			
			/* Bind to the remote object */
			remoteObject = newActive(targetClass, accessPoint);
		}
		
		/* CONNECT_TO is not set -> create a new object */
		else {
		PJProxyFactory factoryProxy = new PJProxyFactory(targetClass);
		remoteObject= (T)factoryProxy.newPOPObject(objectDescription, argvs);
		}
		return remoteObject;
	}
	
	public static Object newActive(String targetClass, Object... argvs) throws POPException, ClassNotFoundException{
	    return newActive(Class.forName(targetClass), argvs);
	}
	
	/**
	 * Static method used to create a new parallel object
	 * @param targetClass	the parallel class to be created
	 * @param argvs			arguments of the constructor (may be empty)
	 * @return references to the parallel object created
	 * @throws POPException
	 */
	public static <T> T newActive(Class<T> targetClass, Object... argvs)
			throws POPException {
	    POPSystem.start();
		PJProxyFactory factoryProxy = new PJProxyFactory(targetClass);
		return (T)factoryProxy.newPOPObject(argvs);
	}

	/**
	 * Static method used to create a parallel object from an existing access point
	 * @param targetClass	the parallel class to be created
	 * @param accessPoint	access point of the living object
	 * @return references to the parallel object
	 * @throws POPException
	 */
	public static <T> T newActive(Class<T> targetClass,
			POPAccessPoint accessPoint) throws POPException {
	    POPSystem.start();
		PJProxyFactory factoryProxy = new PJProxyFactory(targetClass);
		return (T)factoryProxy.bindPOPObject(accessPoint);
	}

	/**
	 * Static method used to create a parallel object from the buffer
	 * @param targetClass	the parallel class to be recovered
	 * @param buffer		buffer from which the object must be recovered
	 * @return references to the parallel object
	 * @throws POPException
	 */
	public static <T> T newActiveFromBuffer(Class<T> targetClass, POPBuffer buffer)
			throws POPException {
	    POPSystem.start();
		PJProxyFactory factoryProxy = new PJProxyFactory(targetClass);
		return (T)factoryProxy.newActiveFromBuffer(buffer);
	}
	
	public static POPAccessPoint getAccessPoint(Object object){
	    if(object instanceof POPObject){
	        POPObject temp = (POPObject) object;
	        return temp.getAccessPoint();
	    }
	    
	    throw new RuntimeException("Object was not of type "+POPObject.class.getName());
	}
	
	public static <T extends Object> T getThis(T object){
	    return (T) ((POPObject) object).getThis(object.getClass());
	}
	
	/**
	 * Returns true if POP-Java is loaded and enabled
	 * @return
	 */
	public static boolean isPOPJavaActive(){
	    try {
	        popjava.javaagent.POPJavaAgent.getInstance();
	    } catch (Exception e) {
	        return false;
	    }
	    
	    return true;
	}
}
