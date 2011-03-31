package popjava.system;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * @author Valentin Clement This thread is responsible to handle the remote log
 *         service provided by POP-C++
 */
public class POPRemoteLogThread extends Thread {
	private String appID;
	private String filename;
	private boolean running = true;

	/**
	 * POPRemoteLogThread constructor
	 * 
	 * @param appID
	 *            POP Application ID
	 */
	public POPRemoteLogThread(String appID) {
		this.appID = appID;
	}

	/**
	 * Get the file name used for the remote logging
	 * 
	 * @return File name as a string
	 */
	public String getFilename() {
		return filename;
	}

	/**
	 * Set the boolean value used to run or stop the thread
	 * 
	 * @param value
	 *            Boolean value (false will stop the thread)
	 */
	public void setRunning(boolean value) {
		running = value;
	}

	/**
	 * Running method of the thread. The thread will work in this method until
	 * it is stopped
	 */
	public void run() {

		filename = new String("/tmp/popjava_logremote_");
		filename += appID;

		File logFile = new File(filename);
		if (!logFile.exists()) {
			try {
				logFile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		try {
			RandomAccessFile log = new RandomAccessFile(logFile, "r");
			long filePointer = 0;
			while (running) {
				long fileLength = log.length();
				if (fileLength > filePointer) {
					log.seek(filePointer);
					String line = log.readLine();
					while (line != null) {
						System.out.println(line);
						line = log.readLine();
					}
					filePointer = log.getFilePointer();
					sleep(750);
				}
			}
			logFile.delete();
		} catch (Exception e) {
			e.printStackTrace();

		}

	}

}