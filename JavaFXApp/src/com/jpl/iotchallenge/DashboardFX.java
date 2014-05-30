package com.jpl.iotchallenge;

import com.jpl.iotchallenge.services.Persistence;
import com.jpl.iotchallenge.services.WindowButtons;
import com.jpl.iotchallenge.user.LhingsUser;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ToolBar;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * IoT Challenge 2014
 * @author José Pereda
 */
public class DashboardFX extends Application {
    
    static {
        Font.loadFont(DashboardFX.class.getResource("resources/Roboto-Regular.ttf").toExternalForm(), 10.0);
        Font.loadFont(DashboardFX.class.getResource("resources/icons.ttf").toExternalForm(), 10.0);
    
    }
    
    private double mouseDragOffsetX = 0;
    private double mouseDragOffsetY = 0;
    
    private LhingsUser user;
    private DashboardController controller;
    
    private final Thread shutdownHook = new Thread(){
        @Override
        public void run() {
            Persistence.saveProperties();
        }
    };
    
    @Override
    public void init(){
        
        user=LhingsUser.getInstance();
        
        Persistence.loadProperties();
        Persistence.loadProperty("user", user, "");
        Persistence.loadProperty("pass", user, "");
        Persistence.loadProperty("name", user, "");
        Persistence.loadProperty("surname1", user, "");
        Persistence.loadProperty("avatar", user, "");
        Persistence.loadProperty("apiKey", user, "");
        Persistence.loadProperty("uuidLamp", user, "");
        Persistence.loadProperty("uuidTable", user, "");
        Persistence.loadProperty("uuidCoffe", user, "");
        Persistence.loadProperty("uuidInterface", user, "");
            
        Runtime.getRuntime().addShutdownHook(this.shutdownHook);
    }
    
    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("dashboard.fxml"));
        Parent root = loader.load();
        controller=(DashboardController)loader.getController();
        
        Persistence.loadProperty("stageX", stage, 0);
        Persistence.loadProperty("stageY", stage, 0);
        Persistence.loadProperty("stageWidth", stage, 1024);
        Persistence.loadProperty("stageHeight", stage, 768);
        
        final WindowButtons windowButtons = new WindowButtons(stage);
        HBox t=(HBox)((BorderPane)root.getChildrenUnmodifiable().get(0)).getTop();
        t.getChildren().stream()
            .filter((n) -> !(n==null) && n instanceof ToolBar)
            .forEach((n) -> ((ToolBar)n).getItems().add(windowButtons));
            
        
        Scene scene = new Scene(root);
        
        // add window header double clicking
        scene.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                windowButtons.toogleMaximized();
            }
        });
        // add window dragging
        scene.setOnMousePressed(e -> {
            mouseDragOffsetX = e.getSceneX();
            mouseDragOffsetY = e.getSceneY();
        });
        scene.setOnMouseDragged(e -> {
            if(!windowButtons.isMaximized()) {
                stage.setX(e.getScreenX()-mouseDragOffsetX);
                stage.setY(e.getScreenY()-mouseDragOffsetY);
            }
        });
        
        
        stage.setScene(scene);
        stage.initStyle(StageStyle.TRANSPARENT);
        stage.getIcons().add(new Image(getClass().getResourceAsStream("resources/Duke14.png")));
        stage.show();
        
        controller.login(scene);
    }

    @Override
    public void stop(){
        controller.devLogout();
        Runtime.getRuntime().removeShutdownHook(shutdownHook);
        Persistence.saveProperties();
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
    
}
