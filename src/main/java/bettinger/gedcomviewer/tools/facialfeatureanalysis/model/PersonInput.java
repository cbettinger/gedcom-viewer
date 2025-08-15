package bettinger.gedcomviewer.tools.facialfeatureanalysis.model;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import bettinger.gedcomviewer.model.Individual;
import bettinger.gedcomviewer.model.Media;
import bettinger.gedcomviewer.utils.FileUtils;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonAutoDetect(fieldVisibility = Visibility.NONE, getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE)
public class PersonInput {
	@JsonProperty
	private final String id;
	@JsonProperty
	private final List<PortraitInput> portraits;
	@JsonProperty
	private PersonInput father;
	@JsonProperty
	private PersonInput mother;

	public PersonInput(final Individual individual, ArrayList<File> temporaryFiles) {
		this.id = individual.getId();
		this.portraits = new ArrayList<>();
		this.mother = null;
		this.father = null;

		Map<Media, Rectangle> portraitData = individual.getFacialPortraits();
		int portraitNumber = 0;
		for (Map.Entry<Media, Rectangle> entry : portraitData.entrySet()) {
			Media medium = entry.getKey();
			Rectangle clip = entry.getValue();

			String fileName = medium.getFileName();
			Date date = new Date();
        	long timeMillis = date.getTime();
			String clipFilePath = String.format("tmp/%s-%d-%d.jpg", this.id, portraitNumber, timeMillis);
			var image = (BufferedImage) medium.getImage(); 

			if (clip != null) {
				image = image.getSubimage(clip.x, clip.y, clip.width, clip.height);
			} 

			File clippedImageFile = new File(clipFilePath);
			try {
				ImageIO.write(image, "jpg", clippedImageFile);
				temporaryFiles.add(clippedImageFile);
			} catch (IOException e) {
				Logger.getLogger(PersonInput.class.getName()).log(Level.SEVERE, "Failed to write temporary image clip file");
			}

			this.portraits.add(new PortraitInput(fileName, clipFilePath));
			portraitNumber++;
		}
	}

	public PersonInput(final Individual individual, final int currentDepth, final int maxDepth, ArrayList<File> temporaryFiles) {
		this(individual, temporaryFiles);

		if (currentDepth < maxDepth) {
			Individual father = individual.getFather();
			if (father != null) {
				this.father = new PersonInput(father, currentDepth + 1, maxDepth, temporaryFiles);
			}
			Individual mother = individual.getMother();
			if (mother != null) {
				this.mother = new PersonInput(mother, currentDepth + 1, maxDepth, temporaryFiles);
			}
		}
	}
}
