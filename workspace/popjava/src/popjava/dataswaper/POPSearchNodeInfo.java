package popjava.dataswaper;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import popjava.buffer.POPBuffer;
import popjava.util.Util;

/**
 * This class is compatible with the the POPCSearchNodeInfo POP-C++ class
 */
public class POPSearchNodeInfo implements IPOPBase {
	
	/**
	 * node's unique identifier
	 */
	private String	nodeId;
	/**
	 * node's operating system
	 */
	private String	operatingSystem;
	/**
	 * node's compute power in MFlops
	 */
	private float	power;
	/**
	 * node's cpu speed
	 */
	private int		cpuSpeed;
	/**
	 * node's memory size (MB)
	 */
	private float	memorySize;
	/**
	 * node's network bandwidth
	 */
	private float	networkBandwidth;
	/**
	 * node's disk space
	 */
	private int		diskSpace;
	/**
	 * node's supported protocol
	 */
	private String	protocol;
	/**
	 * node's supported encoding
	 */
	private String	encoding;
	/**
	 * SSH public key
	 */
	private String	pki;
	/**
	 * node's ip address
	 */
	private String	ip;
	
	/**
	 * Default constructor needed by POP-Java.
	 */
	public POPSearchNodeInfo() {
	}
	
	/**
	 * Get the IP address of the node
	 */
	public String getIP() {
		return ip;
	}
	
	/**
	 * Returns true if the node is on the local computer.
	 */
	public boolean isLocalHost() {
		return Util.isLocal(ip);
	}
	
	/**
	 * Get the unique id of the node
	 */
	public String getNodeId() {
		return nodeId;
	}
	
	/**
	 * Get the operating system of the node
	 */
	public String getOperatingSystem() {
		return operatingSystem;
	}
	
	/**
	 * Get the power available on the node
	 */
	public float getPower() {
		return power;
	}
	
	/**
	 * Get the speed of the CPU
	 */
	public int getCpuSpeed() {
		return cpuSpeed;
	}
	
	/**
	 * Get the memory available on the node (MB)
	 */
	public float getMemorySize() {
		return memorySize;
	}
	
	/**
	 * Get the network bandwidth
	 */
	public float getNetworkBandwidth() {
		return networkBandwidth;
	}
	
	/**
	 * Get the disk space available on the node
	 */
	public int getDiskSpace() {
		return diskSpace;
	}
	
	private void refreshIP() {
		// Parse the node id to get the IP of the remote computer
		Pattern pattern = Pattern.compile("(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)(\\.(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)){3}");
		Matcher matcher = pattern.matcher(nodeId);
		if (!matcher.find()) {
			ip = null;
			throw new IllegalStateException("The nodeId does not contain a valid ip address");
		}
		ip = matcher.group();
	}
	
	/**
	 * Serialize the POPSearchNodeInfo into the buffer
	 */
	@Override
	public boolean serialize(POPBuffer buffer) {
		buffer.putString(nodeId);
		buffer.putString(operatingSystem);
		buffer.putFloat(power);
		buffer.putInt(cpuSpeed);
		buffer.putFloat(memorySize);
		buffer.putFloat(networkBandwidth);
		buffer.putInt(diskSpace);
		buffer.putString(protocol);
		buffer.putString(encoding);
		buffer.putString(pki);

		return true;
	}
	
	/**
	 * Deserialize the POPSearchNodeInfo from the buffer
	 */
	@Override
	public boolean deserialize(POPBuffer buffer) {
		nodeId = buffer.getString();
		operatingSystem = buffer.getString();
		power = buffer.getFloat();
		cpuSpeed = buffer.getInt();
		memorySize = buffer.getFloat();
		networkBandwidth = buffer.getFloat();
		diskSpace = buffer.getInt();
		protocol = buffer.getString();
		encoding = buffer.getString();
		pki = buffer.getString();
		
		// refresh the ip address
		refreshIP();

		return true;
	}
}
