package popjava.serviceadapter;

import popjava.annotation.POPClass;
import popjava.base.Semantic;
import popjava.annotation.POPConfig;
import popjava.annotation.POPObjectDescription;
import popjava.annotation.POPParameter;
import popjava.annotation.POPParameter.Direction;
import popjava.annotation.POPSyncConc;
import popjava.annotation.POPSyncMutex;
import popjava.baseobject.POPAccessPoint;
import popjava.dataswaper.ObjectDescriptionInput;
import popjava.dataswaper.POPSearchNodeInfos;
/**
 * Partial POP-Java class implementation to be used with the POP-C++ runtime
 * This class declares the necessary methods to use the JobMgr parallel object of POP-C++
 */
@POPClass(classId = 10, className = "JobCoreService", deconstructor = true)
public class POPJobService extends POPServiceBase {
	
	/**
	 * Default constructor of POPJobService.
	 * Create a POP-C++ object JobCoreService
	 */
	@POPObjectDescription(id = 10)
	public POPJobService() {
		//Class<?> c = POPJobService.class;
		/*defineMethod(c, "findAvailableMachines", 13, Semantic.CONCURRENT | Semantic.SYNCHRONOUS,
				ObjectDescriptionInput.class, String.class, POPSearchNodeInfos.class, String.class);
		defineMethod(c, "addInterest", 14, Semantic.MUTEX | Semantic.SYNCHRONOUS, String.class);
		defineMethod(c, "removeInterest", 15, Semantic.MUTEX | Semantic.SYNCHRONOUS, String.class);
		defineMethod(c, "addFriendToInterest", 16, Semantic.MUTEX | Semantic.SYNCHRONOUS, String.class, String.class);
		defineMethod(c, "removeFriendFromInterest", 17, Semantic.MUTEX | Semantic.SYNCHRONOUS, String.class,
				String.class);/*/
	}

	/**
	 * Constructor of POPAppService with parameters
	 * @param challenge		challenge string to stop the parallel object
	 */
	@POPObjectDescription(id = 11)
	public POPJobService(String challenge) {

	}

	/**
	 * Ask the JobCoreService service to create a new parallel object
	 * @param localservice	Access to the local application scope services
	 * @param objname		Name of the object to create
	 * @param od			Object description for the resource requirements of this object
	 * @param howmany		Number of objects to create
	 * @param objcontacts	Output arguments - contacts to the objects created
	 * @return 0 if the object is created correctly
	 */
	@POPSyncConc(id = 12)
	public int createObject(POPAccessPoint localservice, String objname,
			ObjectDescriptionInput od, int howmany, POPAccessPoint[] objcontacts, int howmany2, POPAccessPoint[] remotejobcontacts) {
		return 0;
	}


}
