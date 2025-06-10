package bettinger.gedcomviewer.tools.portraitcomparison.model;

import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import bettinger.gedcomviewer.model.Individual;
import bettinger.gedcomviewer.utils.JSONUtils;
import bettinger.gedcomviewer.utils.PythonUtils;

public abstract class FacialFeatureAnalyser {

    public static HashMap<FacialFeatures, FacialFeatureAnalysisResult> analyse(final Individual individual, final int maxDepth, final int maxNumPortraits) {
        HashMap<FacialFeatures, FacialFeatureAnalysisResult> results = new HashMap<>();

        String inputJSON = createInputJSON(individual, maxDepth);

        final String pathToProject = System.getProperty("user.dir");
        final String pathToScript = Paths.get(pathToProject, "src", "main", "python", "familyFaceCompare.py").toString();

        final String[] args = {inputJSON, Integer.toString(maxNumPortraits), Integer.toString(maxDepth)};
        final List<String> outputs = PythonUtils.callScript(pathToScript, args);
        for(String o : outputs) {
            Logger.getLogger(FacialFeatureAnalyser.class.getName()).log(Level.INFO, o);
        }
        return results;
    }

    private static String createInputJSON(final Individual individual, final int maxDepth) {
        PersonInput inputObject = new PersonInput(individual, 0, maxDepth);
        return JSONUtils.toJSON(inputObject);
    }
}
