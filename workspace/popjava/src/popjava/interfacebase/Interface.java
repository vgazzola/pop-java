/**
 * Interface.java 
 * 
 */

package popjava.interfacebase;

import popjava.PopJava;
import popjava.combox.*;
import popjava.dataswaper.ObjectDescriptionInput;
import popjava.dataswaper.POPString;
import popjava.serviceadapter.POPAppService;
import popjava.serviceadapter.POPJobManager;
import popjava.serviceadapter.POPJobService;
import popjava.system.*;
import popjava.util.Configuration;
import popjava.util.LogWriter;
import popjava.util.SystemUtil;
import popjava.util.Util;
import popjava.base.BindStatus;
import popjava.base.MessageHeader;
import popjava.base.POPErrorCode;
import popjava.base.POPException;
import popjava.base.Semantic;
import popjava.baseobject.ObjectDescription;
import popjava.baseobject.POPAccessPoint;
import popjava.buffer.*;

import java.util.*;


/**
 * Interface side of a POP-Java parallel object. This object is the local representative of the parallel object
 *	
 */
public class Interface {

	protected Combox combox;
	protected Buffer popBuffer;
	protected POPAccessPoint popAccessPoint = new POPAccessPoint();
	protected ObjectDescription od = new ObjectDescription();

	/**
	 * Default Interface constructor
	 */
	public Interface() {
		od = new ObjectDescription();
	}

	/**
	 * Create an Interface by giving the access point of the parallel object
	 * @param accessPoint	Access point of the parallel object
	 * @throws POPException	thrown of the interface cannot be bind with the parallel object
	 */
	public Interface(POPAccessPoint accessPoint) throws POPException {
		popAccessPoint = accessPoint;
		bind(accessPoint);
	}

	/**
	 * Serialization of the Interface into the buffer
	 * @param buffer	Buffer to serialize in
	 * @return	true if the serialization is finished without any problems
	 */
	public boolean serialize(Buffer buffer) {
		od.serialize(buffer);
		popAccessPoint.serialize(buffer);
		Buffer oldBuffer = null;

		if (buffer == popBuffer) {
			oldBuffer = popBuffer;
			popBuffer = combox.getBufferFactory().createBuffer();
		}
		int ref = addRef();
		buffer.putInt(ref);
		if (popBuffer != null) {
			popBuffer = oldBuffer;
		}
		return true;
	}

	/**
	 * Deserialize an Interface from a buffer
	 * @param buffer	Buffer to deserialize from
	 * @return	True if the deserialization has finished without any problems
	 */
	public boolean deserialize(Buffer buffer) {
		boolean result = true;
		od.deserialize(buffer);
		popAccessPoint.deserialize(buffer);
		Buffer oldBuffer = null;

		if (buffer == popBuffer) {
			oldBuffer = popBuffer;
			popBuffer = combox.getBufferFactory().createBuffer();
		}
		int ref = buffer.getInt();
		if (ref > 0) {
			try {
				bind(popAccessPoint);
			} catch (POPException e) {
				result = false;
				LogWriter.writeDebugInfo("Deserialize. Cannot bind to "
						+ popAccessPoint.toString());
				e.printStackTrace();
			}
			if (result){
				this.decRef();
			}
		}
		if (popBuffer != null) {
			popBuffer = oldBuffer;
		}
		return result;
	}

	/**
	 * Get the buffer of the interface
	 * @return the buffer associated with this interface
	 */
	public Buffer getBuffer() {
		return popBuffer;
	}

	/**
	 * Associate a buffer with this interface
	 * @param buffer Buffer to be associated with
	 */
	public void setBuffer(Buffer buffer) {
		this.popBuffer = buffer;
	}

	/**
	 * Return the access point of the parallel object associated with this interface
	 * @return	Access point of the associated parallel object
	 */
	public POPAccessPoint getAccessPoint() {
		return popAccessPoint;
	}

	/**
	 * Set the access point associated with this interface
	 * @param accessPoint	Access point to associate
	 */
	public void setAccessPoint(POPAccessPoint accessPoint) {
		this.popAccessPoint = accessPoint;
	}

	/**
	 * Return the object description associated with this interface
	 * @return ObjectDescription of this interface
	 */
	public ObjectDescription getOD() {
		return od;
	}

	/**
	 * Associate an object description with this interface
	 * @param od	Object descritption to associate
	 */
	public void setOd(ObjectDescription od) {
		this.od = od;
	}

	
	public void release() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Allocate resource for the associated parallel object
	 * @param objectName	Name of the object
	 * @return	True if the interface can allocate some resources
	 * @throws POPException	thrown if any exception occurred during the allocating process
	 */
	public boolean allocate(String objectName) throws POPException {

		//Init the AP array for object contact
		popAccessPoint = new POPAccessPoint();
		POPAccessPoint[] allocatedAccessPoint = new POPAccessPoint[1];
		allocatedAccessPoint[0] = popAccessPoint;
		
		//Init the AP array for remote job contacts
		POPAccessPoint[] remotejobscontact = new POPAccessPoint[1];
		POPAccessPoint remotjob = new POPAccessPoint();
		remotejobscontact[0] = remotjob;
		
		boolean canExecLocal = false;
		canExecLocal = tryLocal(objectName, popAccessPoint);
		if (!canExecLocal) {
			// ask the job manager to allocate the broker

			String platforms = od.getPlatform();

			if (platforms.length() <= 0) {
				POPAppService appCoreService = null;
				appCoreService = (POPAppService) popjava.PopJava.newActive(
						POPAppService.class, POPSystem.AppService);
				POPString popStringPlatorm = new POPString();
				appCoreService.getPlatform(objectName, popStringPlatorm);
				platforms = popStringPlatorm.getValue();
				if (platforms.length() <= 0) {
					throw new POPException(
							POPErrorCode.OBJECT_EXECUTABLE_NOTFOUND,
							"OBJECT_EXECUTABLE_NOTFOUND");
				}
				od.setPlatform(platforms);
				appCoreService.exit();
			}
			// Global Resource management system --> Find a resource.
			String jobUrl = od.getJobUrl();
			POPAccessPoint jobContact = new POPAccessPoint();
			if (jobUrl.length() > 0) {
				jobContact.setAccessString(jobUrl);
			} else {
				jobContact = POPSystem.JobService;
			}

			if (jobContact.isEmpty()) {
				jobContact.setAccessString(String.format("%s:%d", POPSystem
						.getHost(), POPJobManager.DEFAULT_PORT));
			}

			POPJobService jobManager = (POPJobService) popjava.PopJava
					.newActive(POPJobService.class, jobContact);
			ObjectDescriptionInput constOd = new ObjectDescriptionInput(od);
			int createdCode = jobManager.createObject(POPSystem.AppService, objectName, constOd, allocatedAccessPoint.length, 
					allocatedAccessPoint, remotejobscontact.length, remotejobscontact);
			jobManager.exit();
			if (createdCode != 0) {
				switch (createdCode) {
					case POPErrorCode.POP_EXEC_FAIL:
						throw new POPException(createdCode,
							"OBJECT_EXECUTABLE_NOTFOUND");
					case POPErrorCode.POP_JOBSERVICE_FAIL:
						throw new POPException(createdCode, "NO_RESOURCE_MATCH");
					default:
						throw new POPException(createdCode, "UNABLE_TO_CREATED_THE_PARALLEL_OBJECT");
				}
			}

			for (int i = 0; i < allocatedAccessPoint.length; i++) {
				if(allocatedAccessPoint[0].get(0).getHost().equals("127.0.0.1") || 
						allocatedAccessPoint[0].get(0).getHost().equals("127.0.1.1"))
					allocatedAccessPoint[0].get(0).setHost(remotejobscontact[0].get(0).getHost());
			}
			popAccessPoint.setAccessString(allocatedAccessPoint[0].toString());

		}
		return bind(popAccessPoint);
	}

	/**
	 * Bind the interface with a parallel object (Broker-side)
	 * @param accesspoint	Access point of the parallel object (Broker-side)s
	 * @return	true if the interface is binded to the broker-side
	 * @throws POPException	thrown if any exception occurred during the binding process
	 */
	protected boolean bind(POPAccessPoint accesspoint) throws POPException {

		if (accesspoint == null || accesspoint.isEmpty())
			POPException.throwAccessPointNotAvailableException();
		ComboxFactoryFinder finder = ComboxFactoryFinder.getInstance();

		if (combox != null)
			combox.close();
		combox = finder.findFactory(Configuration.DefaultProtocol)
				.createClientCombox(accesspoint);
		popBuffer = combox.getBufferFactory().createBuffer();
		if (combox.connect(accesspoint, Configuration.CONNECTION_TIMEOUT)) {

			BindStatus bindStatus = new BindStatus();
			bindStatus(bindStatus);
			switch (bindStatus.getCode()) {
			case BindStatus.BindOK:
				this.getOD().setPlatform(bindStatus.getPlatform());
				negotiateEncoding(Configuration.SelectedEncoding, bindStatus
						.getPlatform());
				return true;
			case BindStatus.BindForwardSession:
			case BindStatus.BindForwardPermanent:
				break;
			default:
				break;
			}

		} else
			POPException.throwObjectBindException(accesspoint);

		return true;
	}

	/**
	 * Get the current binding status 
	 * @param bindStatus	
	 * @throws POPException
	 */
	private void bindStatus(BindStatus bindStatus) throws POPException {
		if (combox == null)
			POPException.throwComboxNotAvailableException();
		if (popBuffer == null)
			POPException.throwBufferNotAvailableException();

		popBuffer = combox.getBufferFactory().createBuffer();
		MessageHeader messageHeader = new MessageHeader(0,
				MessageHeader.BindStatusCall, Semantic.Synchronous);
		popBuffer.setHeader(messageHeader);
		this.popDispatch(popBuffer);
		int errorcode = 0;
		Buffer responseBuffer = combox.getBufferFactory().createBuffer();
		this.popResponse(responseBuffer);
		errorcode = responseBuffer.getInt();

		bindStatus.setCode(errorcode);
		String platform = responseBuffer.getString();
		String info = responseBuffer.getString();
		bindStatus.setPlatform(platform);
		bindStatus.setInfo(info);

	}

	/**
	 * Negotiate the encoding between the interface-side and the broker-side
	 * @param info		Encoding information
	 * @param platform	Platform of the interface
	 * @throws POPException	thrown if any exception occurred during the negotiating process
	 */
	private void negotiateEncoding(String info, String platform)
			throws POPException {
		if (combox == null)
			POPException.throwComboxNotAvailableException();
		if (popBuffer == null)
			POPException.throwBufferNotAvailableException();
		popBuffer = combox.getBufferFactory().createBuffer();
		MessageHeader messageHeader = new MessageHeader(0,
				MessageHeader.GetEncodingCall, Semantic.Synchronous);
		popBuffer.setHeader(messageHeader);
		popBuffer.putString(Configuration.SelectedEncoding);

		this.popDispatch(popBuffer);

		boolean result = false;
		Buffer responseBuffer = combox.getBufferFactory().createBuffer();
		this.popResponse(responseBuffer);
		result = responseBuffer.getBoolean();
		if (result) {
			BufferFactory bufferFactory = BufferFactoryFinder.getInstance()
					.findFactory(Configuration.SelectedEncoding);
			combox.setBufferFactory(bufferFactory);
			popBuffer = bufferFactory.createBuffer();
		}
	}

	public int addRef() {
		if (combox == null)
			return -1;
		popBuffer = combox.getBufferFactory().createBuffer();
		MessageHeader messageHeader = new MessageHeader(0,
				MessageHeader.AddRefCall, Semantic.Synchronous);
		popBuffer.setHeader(messageHeader);

		this.popDispatch(popBuffer);
		int result = 0;
		try {
			Buffer responseBuffer = combox.getBufferFactory().createBuffer();
			this.popResponse(responseBuffer);
			result = responseBuffer.getInt();
		} catch (POPException e) {
			return -1;
		}
		return result;
	}

	public int decRef() {
		if (combox == null)
			return -1;
		popBuffer = combox.getBufferFactory().createBuffer();
		MessageHeader messageHeader = new MessageHeader(0,
				MessageHeader.DecRefCall, Semantic.Synchronous);
		popBuffer.setHeader(messageHeader);

		this.popDispatch(popBuffer);
		int result = 0;
		try {
			Buffer responseBuffer = combox.getBufferFactory().createBuffer();
			this.popResponse(responseBuffer);
			result = responseBuffer.getInt();
		} catch (POPException e) {
			return -1;
		}
		return result;
	}

	/**
	 * Check if the parallel object associated with this interface is still alive
	 * @return	true if the parallel object is alive
	 */
	public boolean isAlive() {
		if (combox == null)
			return false;
		popBuffer = combox.getBufferFactory().createBuffer();
		MessageHeader messageHeader = new MessageHeader(0,
				MessageHeader.ObjectAliveCall, Semantic.Synchronous);
		popBuffer.setHeader(messageHeader);

		this.popDispatch(popBuffer);
		boolean result = false;
		try {
			Buffer responseBuffer = combox.getBufferFactory().createBuffer();
			this.popResponse(responseBuffer);
			result = responseBuffer.getBoolean();
		} catch (POPException e) {
			return false;
		}
		return result;
	}

	/**
	 * Kill the associated parallel object
	 */
	public void kill() {
		if (combox == null)
			return;
		popBuffer = combox.getBufferFactory().createBuffer();
		MessageHeader messageHeader = new MessageHeader(0,
				MessageHeader.KillCall, Semantic.Synchronous);
		popBuffer.setHeader(messageHeader);

		this.popDispatch(popBuffer);
		try {
			Buffer responseBuffer = combox.getBufferFactory().createBuffer();
			this.popResponse(responseBuffer);
		} catch (POPException e) {
			return;
		}
	}

	/**
	 * Try a local execution for the associated parallel object
	 * @param objectName	Name of the object
	 * @param accesspoint	Output parameter - Access point of the object
	 * @return true if the local execution succeed
	 * @throws POPException thrown if any exception occurred during the creation process
	 */
	private boolean tryLocal(String objectName, POPAccessPoint accesspoint)
			throws POPException {
	//	String hostname = "";
		String joburl ="";
		String codeFile = "";
		// Get ClassName

		// Check if Od is empty
		boolean odEmpty = od.isEmpty();
		if (odEmpty)
			return false;

		joburl = od.getHostName();
		
		//if (joburl == null || joburl.length() == 0 || !Util.sameContact(joburl, POPSystem.getHost()))
		//	return false;
		
		if(joburl == null || joburl.length() == 0)
			return false;

		codeFile = od.getCodeFile();
		// Host name existed
		if (codeFile == null || codeFile.length() == 0) {
			// Lookup local code manager for the binary source....
			POPAppService appCoreService = null;
			appCoreService = (POPAppService) PopJava.newActive(
					POPAppService.class, POPSystem.AppService);
			POPString popStringCodeFile = new POPString();
			appCoreService.queryCode(objectName, POPSystem.getPlatform(),
					popStringCodeFile);
			codeFile = popStringCodeFile.getValue();
			appCoreService.exit();
			if (codeFile.length() == 0)
				return false;

		}

		String rport = "";
		int index = joburl.lastIndexOf(":");
		if (index > 0) {
			rport = joburl.substring(index + 1);
			joburl = joburl.substring(0, index);
		}

		int status = localExec(joburl, codeFile, objectName, rport,
				POPSystem.JobService, POPSystem.AppService, accesspoint);
		if (status != 0) {
			// Throw exception
		}
		return (status == 0);
	}

	/**
	 * Launch a parallel object with a command
	 * @param hostname	Hostname to create the object
	 * @param codeFile	Path of the executable code file
	 * @param classname	Name of the Class of the parallel object
	 * @param rport		port
	 * @param jobserv	jobMgr service access point
	 * @param appserv	Application service access point
	 * @param objaccess	Output arguments - Access point to the object
	 * @return -1 if the local execution failed 
	 */
	private static int localExec(String hostname, String codeFile,
			String classname, String rport, POPAccessPoint jobserv,
			POPAccessPoint appserv, POPAccessPoint objaccess) {
		boolean isLocal = popjava.util.Util.isLocal(hostname);
		/*if (!isLocal) {
			return -1;
		}*/
		if (codeFile == null || codeFile.length() == 0)
			return -1;
		codeFile = codeFile.trim();

		ArrayList<String> argvList = new ArrayList<String>();

		ArrayList<String> codeList = Util.splitTheCommand(codeFile);
		argvList.addAll(codeList);

		ComboxAllocateSocket allocateCombox = new ComboxAllocateSocket();
		String callbackString = String.format("-callback=%s", allocateCombox
				.getUrl());
		argvList.add(callbackString);
		if (classname != null && classname.length() > 0) {
			String objectString = String.format("-object=%s", classname);
			argvList.add(objectString);
		}
		if (appserv != null && !appserv.isEmpty()) {
			String appString = String.format("-appservice=%s", appserv
					.toString());
			argvList.add(appString);
		}
		if (jobserv != null && !jobserv.isEmpty()) {
			String jobString = String.format("-jobservice=%s", jobserv
					.toString());
			argvList.add(jobString);
		}

		if (rport != null && rport.length() > 0) {
			String portString = String.format("-socket_port=%s", rport);
			argvList.add(portString);
		}
		
		if(!isLocal){
			ArrayList<String> tmp = new ArrayList<String>();
			tmp.add("ssh");
			tmp.add(hostname);
			for (int i = 0; i < argvList.size(); i++) {
				tmp.add(argvList.get(i));
			}
			
			argvList.clear();
			for (int i = 0; i < tmp.size(); i++) {
				argvList.add(tmp.get(i));
			}
			
		}
		
		int ret = SystemUtil.runCmd(argvList);

		if (ret == -1) {
			return ret;
		}
		allocateCombox.startToAcceptOneConnection();
		BufferXDR buffer = new BufferXDR();
		int result = 0;
		if (allocateCombox.receive(buffer) > 0) {
			int status = buffer.getInt();
			String str = buffer.getString();

			if (status == 0)
				objaccess.setAccessString(str);
			else
				result = status;

		} else {
			result = -1;
		}
		allocateCombox.close();
		return result;
	}

	/**
	 * Send the buffer content to the broker-side
	 * @param buffer	Buffer to send
	 */
	protected void popDispatch(Buffer buffer) {
		combox.send(buffer);
	}

	/**
	 * Receive response from the broker-side
	 * @param buffer
	 * @return
	 * @throws POPException
	 */
	protected int popResponse(Buffer buffer) throws POPException {
		if (combox.receive(buffer) > 0) {
			MessageHeader messageHeader = buffer.getHeader();
			if (messageHeader.getRequestType() == MessageHeader.Exception) {
				int errorCode = messageHeader.getExceptionCode();
				Buffer.checkAndThrow(errorCode, buffer);
			}
		}
		return 0;
	}

	/**
	 * Close the combox associated with this interface
	 */
	public void close() {
		combox.close();
	}

	/**
	 * Close everything
	 */
	protected void finalize() throws Throwable {
		this.decRef();
		try {
			close(); // close open files
		} finally {
			super.finalize();
		}

	}

}