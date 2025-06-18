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
            runtime.exec(pipInstall);

            String[] pipenvInstall = {"pip", "install", "--user", "pipenv"};
            runtime.exec(pipenvInstall);

            String[] envCreate = {"pipenv", "shell"};
            runtime.exec(envCreate);

            String[] requirementsInstall = {"pipenv", "install"};
            runtime.exec(requirementsInstall);

            String[] mediapipeInstall = {"pipenv", "install", "mediapipe", "--user"};
            runtime.exec(mediapipeInstall);
            
            ArrayList<String> command = new ArrayList<String>();
            command.add("python");
            command.add(scriptPath);
            for(String arg : args) {
                command.add(arg);
            }

			Process p = runtime.exec(command.toArray(new String[0]));
            BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getErrorStream()));

            String s = null;
            while ((s = stdInput.readLine()) != null) {
                result.add(s);
            }
            Logger.getLogger(PythonUtils.class.getName()).log(Level.INFO, s, result);

            result.add("{\"pathSimilarities\": {\"CHEEKS\": {\"@I1@\": 0.80456}, \"CHIN\": {\"@I1@, @I1@\": 0.648}, \"EYEBROWS\": {\"@I1@\": 0.80456}, \"EYES\": {\"@I1@\": 0.80456}, \"FACESHAPE\": {\"@I1@\": 0.80456}, \"LIPS\": {\"@I1@\": 0.80456}, \"NOSE\": {\"@I1@\": 0.80456}}, \"nodes\": {\"CHEEKS\": {\"@I1@\": {\"maxSimilarity\": 0.80456, \"avgSimilarity\": 0.80456, \"maxMatchImgTarget\": \"h\", \"maxMatchImgAncestor\": \"i\"}, \"abc\": {\"maxSimilarity\": 0.9833, \"avgSimilarity\": 0.53865, \"maxMatchImgTarget\": \"h\", \"maxMatchImgAncestor\": \"i\"}}, \"CHIN\": {\"@I1@\": {\"maxSimilarity\": 0.80456, \"avgSimilarity\": 0.80456, \"maxMatchImgTarget\": \"h\", \"maxMatchImgAncestor\": \"i\"}, \"abc\": {\"maxSimilarity\": 0.80456, \"avgSimilarity\": 0.78, \"maxMatchImgTarget\": \"h\", \"maxMatchImgAncestor\": \"i\"}}, \"EYEBROWS\": {\"@I1@\": {\"maxSimilarity\": 0.80456, \"avgSimilarity\": 0.80456, \"maxMatchImgTarget\": \"h\", \"maxMatchImgAncestor\": \"i\"}, \"abc\": {\"maxSimilarity\": 0.80456, \"avgSimilarity\": 0.78, \"maxMatchImgTarget\": \"h\", \"maxMatchImgAncestor\": \"i\"}}, \"EYES\": {\"@I1@\": {\"maxSimilarity\": 0.80456, \"avgSimilarity\": 0.80456, \"maxMatchImgTarget\": \"h\", \"maxMatchImgAncestor\": \"i\"}, \"abc\": {\"maxSimilarity\": 0.80456, \"avgSimilarity\": 0.78, \"maxMatchImgTarget\": \"h\", \"maxMatchImgAncestor\": \"i\"}}, \"FACESHAPE\": {\"@I1@\": {\"maxSimilarity\": 0.80456, \"avgSimilarity\": 0.80456, \"maxMatchImgTarget\": \"h\", \"maxMatchImgAncestor\": \"i\"}, \"abc\": {\"maxSimilarity\": 0.80456, \"avgSimilarity\": 0.78, \"maxMatchImgTarget\": \"h\", \"maxMatchImgAncestor\": \"i\"}}, \"LIPS\": {\"@I1@\": {\"maxSimilarity\": 0.80456, \"avgSimilarity\": 0.80456, \"maxMatchImgTarget\": \"h\", \"maxMatchImgAncestor\": \"i\"}, \"abc\": {\"maxSimilarity\": 0.80456, \"avgSimilarity\": 0.78, \"maxMatchImgTarget\": \"h\", \"maxMatchImgAncestor\": \"i\"}}, \"NOSE\": {\"@I1@\": {\"maxSimilarity\": 0.80456, \"avgSimilarity\": 0.80456, \"maxMatchImgTarget\": \"h\", \"maxMatchImgAncestor\": \"i\"}, \"abc\": {\"maxSimilarity\": 0.80456, \"avgSimilarity\": 0.78, \"maxMatchImgTarget\": \"h\", \"maxMatchImgAncestor\": \"i\"}}}}");
            return result;
		} catch (IOException e) {
			Logger.getLogger(PythonUtils.class.getName()).log(Level.SEVERE, String.format("An error occured when calling python script %s.", scriptPath), e);
			return result;
		}
	}
}

