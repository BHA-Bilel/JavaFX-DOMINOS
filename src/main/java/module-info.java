module MainModule {
    requires javafx.controls;
    requires com.jfoenix;
    requires org.controlsfx.controls;
    requires java.desktop;
    requires javafx.swing;

    exports shared;
    exports bg.dominos.server.local;
    exports bg.dominos.server.room;
    exports bg.dominos.lang;
    exports bg.dominos.room;
    exports bg.dominos.game;
    exports bg.dominos.model;
    exports bg.dominos.gfx;

    exports bg.dominos;
    exports shared.domino;
}