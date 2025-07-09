package bettinger.gedcomviewer.tools.facialfeatureanalysis.model;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.pdfbox.util.Charsets;

import bettinger.gedcomviewer.I18N;
import bettinger.gedcomviewer.model.Individual;
import bettinger.gedcomviewer.utils.JSONUtils;
import bettinger.gedcomviewer.utils.PythonUtils;

public abstract class FacialFeatureAnalyser {

    public static TreeMap<FacialFeatures, FacialFeatureAnalysisResult> analyse(final Individual individual, final int maxDepth, final int maxNumPortraits) throws FacialFeatureAnalysisException {
        TreeMap<FacialFeatures, FacialFeatureAnalysisResult> results = new TreeMap<>();

        var defaultException = new FacialFeatureAnalysisException(I18N.get("FacialFeatureAnalysisFailed"));

        File inputFile;
        try {
            inputFile = createInputFile(individual, maxDepth);
        } catch (IOException e) {
            Logger.getLogger(FacialFeatureAnalyser.class.getName()).log(Level.SEVERE, defaultException.getMessage());
            throw defaultException;
        }

        final String pathToProject = System.getProperty("user.dir");
        final String pathToScript = Paths.get(pathToProject, "src", "main", "python", "familyFaceCompare.py").toString();

        final String[] args = { inputFile.getAbsolutePath(), Integer.toString(maxNumPortraits), Integer.toString(maxDepth) };
        final List<String> outputs = PythonUtils.executeScript(pathToScript, args);
        inputFile.delete();

        if (outputs.size() < 1) {
            Logger.getLogger(FacialFeatureAnalyser.class.getName()).log(Level.SEVERE, defaultException.getMessage());
            throw defaultException;
        }
        final var outputJSON = JSONUtils.fromString(outputs.getLast());
        if (outputJSON.get("isError") != null) {
            String message = I18N.get(outputJSON.get("messageKey").asText());
            Logger.getLogger(FacialFeatureAnalyser.class.getName()).log(Level.SEVERE, message);
            throw new FacialFeatureAnalysisException(message);
        } else if (outputJSON.get("success") != null) {
            try {
                var resultFilepath = Paths.get(outputJSON.get("filename").asText());
                final var resultJson = JSONUtils.fromString(Files.readString(resultFilepath, Charsets.UTF_8));
                Files.delete(resultFilepath);

                for (final var feature : FacialFeatures.values()) {
                    results.put(feature, FacialFeatureAnalysisResult.fromJSON(resultJson, feature.name()));
                }
            } catch (IOException e) {
                String message = I18N.get("ProcessingFacialFeatureAnalysisResultFailed");
                Logger.getLogger(FacialFeatureAnalyser.class.getName()).log(Level.SEVERE, message);
                throw new FacialFeatureAnalysisException(message);
            }
        } else {
            Logger.getLogger(FacialFeatureAnalyser.class.getName()).log(Level.SEVERE, defaultException.getMessage());
            throw defaultException;
        }

        return results;
    }

    private static File createInputFile(final Individual individual, final int maxDepth) throws IOException {
        PersonInput inputObject = new PersonInput(individual, 0, maxDepth);
        Date date = new Date();
        long timeMillis = date.getTime();
        Files.createDirectories(Paths.get("tmp"));
        return JSONUtils.toJSONFile(inputObject, String.format("tmp/%d-%s.json", timeMillis, individual.getName()));
    }
}
