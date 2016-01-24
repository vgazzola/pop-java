package popjava.system;

import java.util.UUID;

import popjava.PopJava;
import popjava.baseobject.POPAccessPoint;
import popjava.serviceadapter.POPJobManager;
import popjava.serviceadapter.POPJobService;

public class POPInterest {
	
	private static POPJobManager getJobService() {
		POPAccessPoint jobContact = new POPAccessPoint(true);
		System.out.println(String.format("In POPInterest.getJobService: %s:%d", POPSystem.getHostIP(), POPJobManager.DEFAULT_PORT));
		jobContact.setAccessString(String.format("Socket://%s:%d", POPSystem.getHostIP(), POPJobManager.DEFAULT_PORT));
		return PopJava.newActive(POPJobManager.class, jobContact);
	}
	
	private static String generateUniqueId() {
		return UUID.randomUUID().toString();
	}
	
	public static String addInterest() {
		String id = generateUniqueId();
		return addInterest(id);
	}
	
	public static String addInterest(String id) {
		System.out.println("In POPInterest. addinterest id= "+id);
		POPJobManager jobmgr = getJobService();
		if (jobmgr.addInterest(id)) {
			System.out.println("In POPInterest: I am in the If close...");
			return id;
		}
		System.out.println("In POPInterest: I am out of the If close...");
		return null;
	}
	
	public static boolean removeInterest(String id) {
		return getJobService().removeInterest(id);
	}
	
	public static boolean addFriendToInterest(String id, String friendIP) {
		return getJobService().addFriendToInterest(id, friendIP);
	}
	
	public static boolean removeFriendFromInterest(String id, String friendIP) {
		return getJobService().removeFriendFromInterest(id, friendIP);
	}
}
