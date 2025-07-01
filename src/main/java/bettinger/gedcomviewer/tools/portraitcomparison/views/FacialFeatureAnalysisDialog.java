package bettinger.gedcomviewer.tools.portraitcomparison.views;

import java.awt.BorderLayout;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;

import bettinger.gedcomviewer.Constants;
import bettinger.gedcomviewer.I18N;
import bettinger.gedcomviewer.model.Individual;
import bettinger.gedcomviewer.tools.portraitcomparison.model.AnalysisStarter;
import bettinger.gedcomviewer.views.HTMLTextPane;
import bettinger.gedcomviewer.views.IntegerPicker;
import bettinger.gedcomviewer.views.MainFrame;
import bettinger.gedcomviewer.views.icons.MaterialIcons;
import jiconfont.swing.IconFontSwing;

public class FacialFeatureAnalysisDialog extends JDialog {

    private final HTMLTextPane infoPane;
    private final IntegerPicker maxDepthPicker;
    private final IntegerPicker maxNumPortraitsPicker;
    private final JButton startButton;

    public FacialFeatureAnalysisDialog(final Individual individual, AnalysisStarter onStart) {
        setTitle(I18N.get("FacialFeatureAnalysis"));
        setModal(true);

        setLayout(new BorderLayout());

        this.infoPane = new HTMLTextPane();
        this.infoPane.setText(I18N.get("FacialFeatureAnalysisStartInfo"));

        this.maxDepthPicker = new IntegerPicker(I18N.get("MaxFacialFeatureComparisonDepth"), Constants.MIN_FACE_COMPARISON_DEPTH, Constants.MAX_FACE_COMPARISON_DEPTH, Constants.MAX_FACE_COMPARISON_DEPTH / 2);
        this.maxNumPortraitsPicker = new IntegerPicker(I18N.get("MaxNumPortraitsPerPerson"), Constants.MIN_NUM_PORTRAITS_FOR_FACE_COMPARISON, Constants.MAX_NUM_PORTRAITS_FOR_FACE_COMPARISON, Constants.MAX_NUM_PORTRAITS_FOR_FACE_COMPARISON);

        var vBox = new JPanel();
        vBox.setLayout(new BoxLayout(vBox, BoxLayout.PAGE_AXIS));
        vBox.setBackground(Constants.DEFAULT_CONTENT_COLOR);
        var proband = new HTMLTextPane();
        proband.setText(String.format("%s: %s", I18N.get("Proband"), individual.getName()));
        vBox.add(proband);
        vBox.add(this.maxDepthPicker);
        vBox.add(this.maxNumPortraitsPicker);
        var parameterPane = new JPanel();
        parameterPane.setBackground(Constants.DEFAULT_CONTENT_COLOR);
        parameterPane.add(vBox);

        this.startButton = new JButton(I18N.get("StartFacialFeatureAnalysis"), IconFontSwing.buildIcon(MaterialIcons.PLAY_ARROW, Constants.BUTTON_ICON_SIZE));
        this.startButton.addActionListener(_ -> {
            onStart.start(individual, maxDepthPicker.getValue(), maxNumPortraitsPicker.getValue());
            setVisible(false);
        });
        var buttonPanel = new JPanel();
        buttonPanel.add(this.startButton);

        add(this.infoPane, BorderLayout.PAGE_START);
        add(parameterPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.PAGE_END);

        pack();

        setSize(Constants.DEFAULT_MODAL_DIALOG_WIDTH, Constants.DEFAULT_MODAL_DIALOG_HEIGHT);
        setLocationRelativeTo(MainFrame.getInstance());

        setVisible(true);
    }
}
