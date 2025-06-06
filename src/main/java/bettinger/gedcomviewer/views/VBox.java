package bettinger.gedcomviewer.views;

import javax.swing.BoxLayout;
import javax.swing.JPanel;

public class VBox extends JPanel{
    public VBox() {
        super();

        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
    }
}
