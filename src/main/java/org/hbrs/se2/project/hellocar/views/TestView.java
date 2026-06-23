package org.hbrs.se2.project.hellocar.views;

import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;

@Route("test")
public class TestView extends VerticalLayout {

    public TestView() {
        H2 title = new H2(this.getTranslation("view.test.title", "Aylin"));
        add(title);
    }
}
