package bettinger.gedcomviewer.tools.portraitcomparison.model;

import java.io.File;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import bettinger.gedcomviewer.model.Individual;
import bettinger.gedcomviewer.utils.JSONUtils;
import bettinger.gedcomviewer.utils.PythonUtils;

public abstract class FacialFeatureAnalyser {

    public static TreeMap<FacialFeatures, FacialFeatureAnalysisResult> analyse(final Individual individual, final int maxDepth, final int maxNumPortraits) {
        TreeMap<FacialFeatures, FacialFeatureAnalysisResult> results = new TreeMap<>();

        File inputFile = createInputFile(individual, maxDepth);

        final String pathToProject = System.getProperty("user.dir");
        final String pathToScript = Paths.get(pathToProject, "src", "main", "python", "familyFaceCompare.py").toString();

        final String[] args = {inputFile.getAbsolutePath(), Integer.toString(maxNumPortraits), Integer.toString(maxDepth)};
        final List<String> outputs = PythonUtils.callScript(pathToScript, args);
        inputFile.delete();

        final var outputJSON = JSONUtils.fromString(outputs.getLast());
        if (outputJSON.get("isError") != null) {
            
        } else {
             for (final var feature : FacialFeatures.values()) {
                results.put(feature, FacialFeatureAnalysisResult.fromJSON(outputJSON, feature.name()));
            }
        }

        return results;
    }

    private static File createInputFile(final Individual individual, final int maxDepth) {
        PersonInput inputObject = new PersonInput(individual, 0, maxDepth);
        Date date = new Date();
        long timeMillis = date.getTime();
        return JSONUtils.toJSONFile(inputObject, String.format("%d-%s.json", timeMillis, individual.getName()));
    }
}
