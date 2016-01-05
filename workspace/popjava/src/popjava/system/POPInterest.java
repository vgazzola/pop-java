package popjava.system;

import java.util.UUID;

import popjava.PopJava;
import popjava.baseobject.POPAccessPoint;
import popjava.serviceadapter.POPJobManager;
import popjava.serviceadapter.POPJobService;

public class POPInterest {
	
	private static POPJobService getJobService() {
		POPAccessPoint jobContact = new POPAccessPoint();
		jobContact.setAccessString(String.format("%s:%d", POPSystem.getHostIP(), POPJobManager.DEFAULT_PORT));
		return PopJava.newActive(POPJobService.class, jobContact);
	}
	
	private static String generateUniqueId() {
		return UUID.randomUUID().toString();
	}
	
	public static String addInterest() {
		String id = generateUniqueId();
		return addInterest(id);
	}
	
	public static String addInterest(String id) {
		POPJobService jobmgr = getJobService();
		if (jobmgr.addInterest(id)) {
			return id;
		}
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
