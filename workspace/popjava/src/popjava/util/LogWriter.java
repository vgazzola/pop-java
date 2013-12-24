package popjava.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.Date;

import popjava.system.POPJavaConfiguration;

/**
 * This class is used to write log file
 */
public class LogWriter {
	
	private static final String LOG_FOLDER_NAME = "logFolder";
	private static final String DEFAULT_LOCATION = "/usr/local/popj/"+LOG_FOLDER_NAME;

	/**
	 * Process identifier
	 */
	private static String PID = "";
	
	/**
	 * Log folder where the log files will be written
	 */
	public static String LogFolder;
	
	/**
	 * Prefix of the log file
	 */
	public static String Prefix = "";

	static {
		String pid = ManagementFactory.getRuntimeMXBean()
				.getName();
		for (int index = 0; index < pid.length(); index++) {
			if (Character.isLetterOrDigit(pid.charAt(index))){
				PID += pid.charAt(index);
			}
		}
		
		String popLocation = POPJavaConfiguration.getPopJavaLocation();
		
		if(!popLocation.isEmpty()){
			LogFolder = String.format("%s%s%s", popLocation, File.separator, LOG_FOLDER_NAME);
		}else{
			LogFolder = DEFAULT_LOCATION;
		}
		
		
		new File(LogFolder).mkdirs(); //Create log folder if it does not exist yet
	}

	/**
	 * Write a new log information line in the file
	 * @param info		Information to write
	 * @param filePath	Path of the log file
	 */
	public static void writeLogInfo(String info, String filePath) {
		info = PID + "-" + (new Date()).toString() +":"+System.currentTimeMillis() + "-" + info;
		info += "\r\n";
		writeLogfile(info, filePath);

	}

	/**
	 * Write a new debug information line in the file
	 * @param info	Information to write
	 */
	public static void writeDebugInfo(String info) {
		
		if (Configuration.Debug) {
			System.out.println(info);
			logInfo(info);
		}
	}
	
	private static void logInfo(String info){
		if (Configuration.Debug) {
			info = PID + "-" + (new Date()).toString()+":"+System.currentTimeMillis() + "-" + info;
			info += "\r\n";
			String path = String.format("%s%s%s.txt", LogFolder, File.separator, Prefix);
			writeLogfile(info, path);
		}
	}
	
	/**
	 * Writes an exception to the same log as writeDebugInfo. The complete
	 * backtrace is logged.
	 * @param e The exception to be logged
	 */
	public static void writeExceptionLog(Throwable e){
		e.printStackTrace();
		logInfo("Exception "+ e.getClass().getName()+" "+e.getMessage());
        for(int i = 0; i < e.getStackTrace().length; i++){
        	logInfo(e.getStackTrace()[i].getClassName()+" "+e.getStackTrace()[i].getLineNumber());
        }
	}

	/**
	 * Write new log information into a file
	 * @param info	Information to write
	 * @param path	Path of the file
	 */
	public synchronized static void writeLogfile(String info, String path) {
		path = path.replace(".txt", PID + ".txt");
		FileWriter fstream;
		try {
			File logFile = new File(path);
			if(!logFile.exists()){
				logFile.getParentFile().mkdirs();
				logFile.createNewFile();
			}
			fstream = new FileWriter(logFile, true);
			BufferedWriter out = new BufferedWriter(fstream);
			out.write(info);

			// Close the output stream
			out.close();
			fstream.close();
		}catch (IOException e) {
			e.printStackTrace();
		}

	}
	
	/**
	 * Remove all file in the log directory
	 * @return	true if the action is succeed
	 */
	public static boolean deleteLogDir() {
		File dir = new File(LogFolder);
		String[] children = dir.list();
		for (int i = 0; i < children.length; i++) {
			new File(dir, children[i]).delete();
		}
		return true;
	}

}
