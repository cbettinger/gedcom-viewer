package bettinger.gedcomviewer.views;

import java.awt.Component;

import javax.swing.BoxLayout;
import javax.swing.JPanel;

public class VBox extends JPanel{

    private JPanel outerPanel;

    public VBox() {
        super();
        this.outerPanel = null;
        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
    }

    public VBox(boolean stretchableComponents)  {
        super();
        if (stretchableComponents) {
            this.outerPanel = null;
            setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        } else {
            this.outerPanel = new JPanel();
            this.outerPanel.setLayout(new BoxLayout(this.outerPanel, BoxLayout.PAGE_AXIS));

            super.add(this.outerPanel);
        }
    }

    @Override
    public Component add(Component comp) {
        if (outerPanel == null) {
            return add(comp);
        }
        return outerPanel.add(comp);
    }
}
