package popjava.serviceadapter;

import popjava.annotation.POPClass;
import popjava.base.Semantic;
import popjava.baseobject.POPAccessPoint;
import popjava.dataswaper.ObjectDescriptionInput;
import popjava.dataswaper.POPString;
/**
 * Partial POP-Java class implementation to be used with the POP-C++ runtime
 * This class declares the necessary methods to use the JobMgr parallel object of POP-C++
 */
@POPClass(classId = 15, className = "JobMgr")
public class POPJobManager extends POPJobService {

	/**
	 * Default running port of the JobMgr service
	 */
	public static final int DEFAULT_PORT = 2711;

	/**
	 * Default constructor of POPJobManager.
	 * Create a POP-C++ object JobMgr
	 */
	public POPJobManager() {
		setClassId(15);
		hasDestructor(true);
		Class<?> c = POPJobManager.class;
		this.definedMethodId = true;			
		defineConstructor(c,10);
		defineConstructor(c,11,String.class,String.class);
		//defineConstructor(c,12,String.class,String.class,String.class);
		
		defineMethod(c, "registerNode",13, Semantic.CONCURRENT
				| Semantic.ASYNCHRONOUS,String.class);
		defineMethod(c, "query", 14,Semantic.SEQUENCE | Semantic.SYNCHRONOUS,POPString.class,POPString.class);
		defineMethod(c, "createObject", 12,Semantic.CONCURRENT | Semantic.SYNCHRONOUS,
				POPAccessPoint.class, POPString.class,	ObjectDescriptionInput.class, int.class, POPAccessPoint[].class, int.class, POPAccessPoint[].class);
		defineMethod(c, "allocResource", 16,Semantic.CONCURRENT
				| Semantic.SYNCHRONOUS,String.class, String.class,
				ObjectDescriptionInput.class, int.class, float[].class,
				POPAccessPoint[].class, int[].class, int[].class,
				int[].class, int.class);
		defineMethod(c, "cancelReservation", 18,Semantic.SEQUENCE
				| Semantic.ASYNCHRONOUS,int[].class, int.class);

		defineMethod(c, "execObj", 19,Semantic.CONCURRENT | Semantic.SYNCHRONOUS,POPString.class, int.class, int[].class,
				String.class, POPAccessPoint[].class);
		defineMethod(c, "dump", 20,Semantic.SEQUENCE | Semantic.ASYNCHRONOUS);
		defineMethod(c, "start", 21,Semantic.SEQUENCE |Semantic.SYNCHRONOUS);
		defineMethod(c, "selfRegister", 22,Semantic.ASYNCHRONOUS);

	}

	/**
	 * Constructor of POPJobManager with challenge string
	 * @param daemon	Set the service in deamon mode
	 * @param challenge	Challenge string needed for the service stop
	 * @param url		URL of the JobMgr service
	 */
	public POPJobManager(boolean daemon, String challenge, String url) {

	}
	
	/**
	 * Constructor of POPCodeManager with challenge string
	 * @param daemon	Set the service in deamon mode
	 * @param config	Configuration information
	 * @param challenge	Challenge string needed for the service stop	
	 * @param url		URL of the JobMgr service
	 */
	public POPJobManager(boolean daemon, String config, String challenge,
			String url) {

	}

	/**
	 * Register a other JobMgr as a neighbor
	 * @param url	URL of the node to register
	 */
	public void registerNode(String url) {

	}

	/**
	 * Query configuration informations
	 * @param type	Name of the configuration element
	 * @param value	Output argument - Value of the configuration element
	 * @return 0 if the configuration element is not found
	 */
	public int query(POPString type, POPString value) {
		return 0;
	}

	/**
	 * Ask the JobMgr service to create a new parallel object
	 * @param localservice	Access to the local application scope services
	 * @param objname		Name of the object to create
	 * @param od			Object description for the resource requirements of this object
	 * @param howmany		Number of objects to create
	 * @param jobcontacts	Output arguments - contacts to the objects created
	 * @return 0 if the object is created correctly
	 */
	public int createObject(POPAccessPoint localservice, POPString objname,
			ObjectDescriptionInput od, int howmany, POPAccessPoint[] objcontacts, int howmany2, POPAccessPoint[] remotejobcontacts) {
		return 0;
	}

	/**
	 * Ask the JobMgr service to allocate resources for a new objects
	 * @param localservice	Access to the local application scope services
	 * @param objname		Name of the object to create
	 * @param od			Object description for the resource requirements of this object
	 * @param howmany		Number of objects to create
	 * @param fitness		Fitness of the resource
	 * @param jobcontacts	Output arguments - contacts to the JobMgr to create objects
	 * @param reserveIDs	Output arguments - reservation identifier for each objects
	 * @param requestInfo	
	 * @param trace			
	 * @param tracesize		
	 * @return	true if the runtime has allocated some resources for the parallel objects
	 */
	public boolean allocResource(String localservice, String objname,
			ObjectDescriptionInput od, int howmany, float[] fitness,
			POPAccessPoint[] jobcontacts, int[] reserveIDs, int[] requestInfo,
			int[] trace, int tracesize) {
		return true;
	}

	/**
	 * Ask the JobMgr service to cancel some reservation for parallel object
	 * @param req		Reservation identifiers of the reservations to cancel
	 * @param howmany	Number of reservations to cancel
	 */
	public void cancelReservation(int[] req, int howmany) {

	}

	/**
	 * Ask the JobMgr service to execute a specific object
	 * @param objname		Name of the object
	 * @param howmany		Number of object to execute
	 * @param reserveIDs	Reservations identifiers for these objects
	 * @param localservice	Access to the local application scope services
	 * @param objcontacts	Output arguments - contacts to the objects created
	 * @return	0 if the execution hasn't failed
	 */
	public int execObj(POPString objname, int howmany, int[] reserveIDs,
			String localservice, POPAccessPoint[] objcontacts) {
		return 0;
	}

	/**
	 * 
	 */
	public void dump() {

	}

	/**
	 * Start the JobMgr service
	 */
	@Override
    public void start() {

	}

	/**
	 * Register the local JobMgr service to its known neighbors
	 */
	public void selfRegister() {

	}

}
