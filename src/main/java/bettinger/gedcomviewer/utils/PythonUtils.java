package bettinger.gedcomviewer.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public interface PythonUtils {

	public static List<String> callScript(final String scriptPath, String[] args) {
        List<String> result = new ArrayList<String>();

		try {
            ArrayList<String> command = new ArrayList<String>();
            command.add("python");
            command.add(scriptPath);
            for(String arg : args) {
                command.add(arg);
            }

			Process p = Runtime.getRuntime().exec((String[])command.toArray());
            BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String s = null;
            while ((s = stdInput.readLine()) != null) {
                result.add(s);
            }
            return result;
		} catch (IOException e) {
			Logger.getLogger(JSONUtils.class.getName()).log(Level.SEVERE, String.format("An error occured when calling python script %s.", scriptPath), e);
			return result;
		}
	}
}

