package bettinger.gedcomviewer.views;

import java.awt.FlowLayout;

import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

public class IntegerPicker extends JPanel{

    private final HTMLTextPane labelPane;
    private final JSpinner valueSpinner;

    public IntegerPicker(String label, int min, int max) {
        super();
        
        setLayout(new FlowLayout(FlowLayout.LEFT));

        SpinnerNumberModel spinnerModel = new SpinnerNumberModel(max, min, max, 1);
        this.labelPane = new HTMLTextPane();
        this.labelPane.setText(label);
        this.valueSpinner = new JSpinner(spinnerModel);

        add(this.labelPane);
        add(this.valueSpinner);
    }
}
