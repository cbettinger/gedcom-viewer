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
            Runtime runtime = Runtime.getRuntime();

            String[] pipInstall = {"python", "-m", "ensurepip", "--upgrade"};
            Process p = runtime.exec(pipInstall);
            p.waitFor();
            Logger.getLogger(PythonUtils.class.getName()).log(Level.INFO, "pip installed");

            String[] pipenvInstall = {"pip", "install", "--user", "pipenv"};
            p = runtime.exec(pipenvInstall);
            p.waitFor();
            Logger.getLogger(PythonUtils.class.getName()).log(Level.INFO, "pipenv installed");

            String[] requirementsInstall = {"pipenv", "install"};
            p = runtime.exec(requirementsInstall);
            p.waitFor();
            Logger.getLogger(PythonUtils.class.getName()).log(Level.INFO, "requirements installed");

            ArrayList<String> command = new ArrayList<String>();
            command.add("pipenv");
            command.add("run");
            command.add("python");
            command.add(scriptPath);
            for(String arg : args) {
                command.add(arg);
            }
            
			Process pOut = runtime.exec(command.toArray(new String[0]));
            pOut.waitFor();
            Logger.getLogger(PythonUtils.class.getName()).log(Level.INFO, "analysis done");
            BufferedReader stdInput = new BufferedReader(new InputStreamReader(pOut.getInputStream()));

            String s = null;
            while ((s = stdInput.readLine()) != null) {
                result.add(s);
            }
            Logger.getLogger(PythonUtils.class.getName()).log(Level.INFO, result.toString());

            return result;
		} catch (IOException e) {
			Logger.getLogger(PythonUtils.class.getName()).log(Level.SEVERE, String.format("An error occured when calling python script %s.", scriptPath), e);
			return result;
		} catch (InterruptedException e) {
            Logger.getLogger(PythonUtils.class.getName()).log(Level.SEVERE, String.format("An error occured when calling python script %s.", scriptPath), e);
			return result;
        }
	}
}

