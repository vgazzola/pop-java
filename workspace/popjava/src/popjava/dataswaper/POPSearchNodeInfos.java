package popjava.dataswaper;

import java.util.ArrayList;
import java.util.List;

import popjava.buffer.POPBuffer;

/**
 * This list contains all the responses sent by POP-C++ job managers in response
 * to a discovery request.
 * 
 * This class is compatible with the the POPCSearchNodeInfos POP-C++ class
 */
public class POPSearchNodeInfos implements IPOPBase {

	/**
	 * List with all the responses
	 * There is one item for each response
	 */
	private List<POPSearchNodeInfo>	nodeInfos;

	/**
	 * Default constructor needed by POP-Java.
	 */
	public POPSearchNodeInfos() {
		nodeInfos = new ArrayList<>();
	}

	/**
	 * Add a new response to this list of responses
	 *
	 * @param nodeInfo the new response
	 */
	public void addANodeInfo(POPSearchNodeInfo nodeInfo) {
		nodeInfos.add(nodeInfo);
	}
	
	/**
	 * Get all the responses.
	 *
	 * @return the responses
	 */
	public List<POPSearchNodeInfo> getNodeInfos() {
		return nodeInfos;
	}
	
	/**
	 * Serialize the POPSearchNodeInfos into the buffer
	 */
	@Override
	public boolean serialize(POPBuffer buffer) {
		buffer.putInt(nodeInfos.size());
		for (POPSearchNodeInfo nodeInfo : nodeInfos) {
			nodeInfo.serialize(buffer);
		}
		return true;
	}

	/**
	 * Deserialize the POPSearchNodeInfos from the buffer
	 */
	@Override
	public boolean deserialize(POPBuffer buffer) {
		int nb = buffer.getInt();
		nodeInfos.clear();
		for (int i = 0; i < nb; i++) {
			POPSearchNodeInfo nodeInfo = new POPSearchNodeInfo();
			nodeInfo.deserialize(buffer);
			nodeInfos.add(nodeInfo);
		}
		return true;
	}
}
