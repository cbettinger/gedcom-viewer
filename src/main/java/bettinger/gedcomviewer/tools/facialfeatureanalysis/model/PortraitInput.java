package bettinger.gedcomviewer.tools.facialfeatureanalysis.model;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonAutoDetect(fieldVisibility = Visibility.NONE, getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE)
public class PortraitInput {
    @JsonProperty
    private final String originalFilePath;

    @JsonProperty
    private final String imageClipFilePath;

    public PortraitInput(final String originalFilePath, final String clipFilePath) {
        this.originalFilePath = originalFilePath;
        this.imageClipFilePath = clipFilePath;
    }
}
