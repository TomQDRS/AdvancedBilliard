/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package control;

import javafx.geometry.Point2D;
import java.net.URL;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;
import java.util.ResourceBundle;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import model.AB_Model;
import model.Kugel;
import model.Loch;
import model.Objekt;

/**
 *
 * @author Tom
 */
public class FXML_GUIController implements Initializable, Observer {

    @FXML
    private AnchorPane root;

    @FXML
    private MenuItem termButton;
    @FXML
    private Pane playPane;
    @FXML
    private Button anstossButton;
    @FXML
    private Slider groSlider;
    @FXML
    private Slider stoWinSlider;
    @FXML
    private Slider stoKraSlider;
    @FXML
    private Label groSliAnz;
    @FXML
    private Label stoWinSliAnz;
    @FXML
    private Label stoKraSliAnz;
    @FXML
    private GridPane kugelGrid;

    private AB_Model model;

    private ArrayList<Circle> circles = new ArrayList<Circle>();
    private ArrayList<Rectangle> rectangles = new ArrayList<Rectangle>();
    private ArrayList<Circle> holes = new ArrayList<Circle>();

    private ObservableList<String> materialien = FXCollections.observableArrayList("Standard", "Holz", "Eisen");
    @FXML
    private MenuItem sim1;
    @FXML
    private MenuItem sim2;
    @FXML
    private MenuItem sim3;
    @FXML
    private ScrollPane scrollPane;
    @FXML
    private Label simNameLabel;
    @FXML
    private ScrollPane kugelEinsScrollPane;
    @FXML
    private GridPane hindernisGrid;

    //Initialisiert den Controller. Erstellt ein model und fügt diesen Controller als Observer hinzu.
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        model = new AB_Model();
        model.addObserver(this);
        initSliders();
        scrollPane.setFitToHeight(true);
        scrollPane.setFitToWidth(true);
    }

    //initialisiert Slider. Setzt ihre maximalen und minimalen Werte und verbindet die Anzeigefelder.
    private void initSliders() {

        groSlider.setValue(5);
        groSlider.setMax(50);
        groSlider.setMin(1);
        groSliAnz.textProperty().bindBidirectional(groSlider.valueProperty(), NumberFormat.getNumberInstance());

        stoWinSlider.setValue(0);
        stoWinSlider.setMax(360);
        stoWinSlider.setMin(0);
        stoWinSliAnz.textProperty().bindBidirectional(stoWinSlider.valueProperty(), NumberFormat.getNumberInstance());

        stoKraSlider.setValue(10);
        stoKraSlider.setMax(100);
        stoKraSlider.setMin(1);
        stoKraSliAnz.textProperty().bindBidirectional(stoKraSlider.valueProperty(), NumberFormat.getNumberInstance());
    }

//Methode des Observer Modells. Momentan: Levelwechsel.
    @Override
    public void update(Observable ABModel, Object obj) {

        //überprüft ob ein neues Level geladen wurde oder nicht
        if (!model.getCurrentSimulation().toString().equals(model.getPreviousSimulation().toString())) {
            levelLaden();
        }
    }

    //Button Aktionen für Simulationauswahl
    @FXML
    private void sim1Auswaehlen(ActionEvent event) {
        model.setCurrentSimulation(0);
    }

    @FXML
    private void sim2Auswaehlen(ActionEvent event) {
        model.setCurrentSimulation(1);
    }

    @FXML
    private void sim3Auswaehlen(ActionEvent event) {
        model.setCurrentSimulation(2);
    }

    //Anstoß. Startet einen Timer und führt jedes Frame move() aus. 
    @FXML
    private void anstoss(ActionEvent event) {

        System.out.println("Anstoß!");

        EventHandler<ActionEvent> handler = new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                move(model.getCurrentSimulation().getStosskugel());
            }
        };
        KeyFrame f = new KeyFrame(Duration.millis(16.6), handler);
        Timeline timer = new Timeline(f);
        timer.setCycleCount(Timeline.INDEFINITE);
        timer.play();
        anstossButton.setDisable(true);
        groSlider.setDisable(true);
        stoKraSlider.setDisable(true);
        stoWinSlider.setDisable(true);
    }

    //bewegt die Kugel und updated die beiden kreis positionen.
    public void move(Kugel k) {

        Point2D anstoss = new Point2D(1, 0);
        double radi = getRadi();
        double stoWi = getStoWi();
        double stoKra = getStoKra();
        k.bewegen(anstoss, radi, stoWi, stoKra);
        circles.get(0).setCenterX(k.getPosition().getX());
        circles.get(0).setCenterY(k.getPosition().getY());

    }

    //beendet das Programm
    @FXML
    private void terminieren(ActionEvent event) {
        System.out.println("TERMINIERT!");
        Platform.exit();
    }

    //öffnet den LoaderF
    @FXML
    private void loadFile(ActionEvent event) {
        System.out.println("loadfile");
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Lade deinen Spielstand");
        fileChooser.showOpenDialog(root.getScene().getWindow());
    }

    @FXML
    private void about(ActionEvent event) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Über...");
        alert.setHeaderText("Über Advanced Billiard");
        alert.setContentText("Version: 1.0, Programm von Tom Quinders, Alexander Dmitriev, Jaqueline Timmermann und Markus Roth");

        alert.showAndWait();
    }

    private void levelLaden() {
        System.out.println("level geladen");

        model.setPreviousSimulation(model.getCurrentSimulation());

        circles.clear();
        rectangles.clear();
        holes.clear();
        playPane.getChildren().clear();
        kugelGrid.getChildren().clear();
        hindernisGrid.getChildren().clear();

        simNameLabel.setText("Simulation " + model.getCurrentSimulation().toString());

        //Erstellt einen Kreis für jede Kugel
        for (Kugel k : model.getCurrentSimulation().getKugeln()) {

            int posX = k.getPosX();
            int posY = k.getPosY();
            int rad = k.getRad();
            Circle c = new Circle(posX, posY, rad);
            circles.add(c);
            playPane.getChildren().add(c);
        }
        //Gibt den Kreisen Farbe je nach ihrer Nummer
        int num = 0;
        for (Circle c : circles) {

            Circle cEins = null;

            if (num != 0) {
                cEins = new Circle(30);
            } else {
                //bindet den Radius der weißen Kugel an den Sliderwert
                c.radiusProperty().bind(groSlider.valueProperty());
            }

            switch (num) {
                case (0):
                    c.setFill(Color.WHITE);
                    break;
                case (1):
                    c.setFill(Color.RED);
                    cEins.setFill(Color.RED);
                    break;
                case (2):
                    c.setFill(Color.AQUA);
                    cEins.setFill(Color.AQUA);
                    break;
                case (3):
                    c.setFill(Color.GREENYELLOW);
                    cEins.setFill(Color.GREENYELLOW);
                    break;
                case (4):
                    c.setFill(Color.YELLOW);
                    cEins.setFill(Color.YELLOW);
                    break;
                case (5):
                    c.setFill(Color.PINK);
                    cEins.setFill(Color.PINK);
                    break;
            }

            Label beschreibung = new Label("Material");
            ComboBox cbx = new ComboBox(materialien);

            kugelGrid.setVgap(5);

            if (num != 0) {
                kugelGrid.add(cEins, 0, (num - 1) * 2, 1, 2);
                kugelGrid.add(beschreibung, 1, (num - 1) * 2);
                kugelGrid.add(cbx, 1, (num - 1) * 2 + 1);
            }
            num++;
        }

        //Erstellt einen Kreis für jede Kugel
        for (Objekt o : model.getCurrentSimulation().getHindernisse()) {

            int posX = o.getPosX();
            int posY = o.getPosY();
            int width = o.getDimX();
            int height = o.getDimY();
            Rectangle r = new Rectangle(posX, posY, width, height);
            r.setArcHeight(5);
            r.setArcWidth(5);
            rectangles.add(r);
            playPane.getChildren().add(r);
        }

        //Gibt den Kreisen Farbe je nach ihrer Nummer
        int num2 = 0;
        for (Rectangle r : rectangles) {

            Rectangle rEins = null;

            rEins = new Rectangle(75, 50);

            Label beschreibung = new Label("Material");
            ComboBox cbx = new ComboBox(materialien);

            hindernisGrid.setVgap(5);

            hindernisGrid.add(rEins, 0, num2 * 2, 1, 2);
            hindernisGrid.add(beschreibung, 1, num2 * 2);
            hindernisGrid.add(cbx, 1, (num2 * 2) + 1);

            num2++;
        }

        for (Loch l : model.getCurrentSimulation().getLöcher()) {

            int posX = l.getPosX();
            int posY = l.getPosY();
            int rad = l.getRad();
            Circle c = new Circle(posX, posY, rad);

            holes.add(c);
            playPane.getChildren().add(c);
        }

        anstossButton.setDisable(false);
        groSlider.setDisable(false);
        stoKraSlider.setDisable(false);
        stoWinSlider.setDisable(false);
    }

    public final double getRadi() {

        return groSlider.valueProperty().get();
    }

    public final double getStoWi() {

        return stoWinSlider.valueProperty().get();
    }

    public final double getStoKra() {

        return stoKraSlider.valueProperty().get();
    }

    //öffnet den Saver
    /*
    private void saveFile(ActionEvent event) {
        System.out.println("saveFile");
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Speichere deinen Spielstand");
        File file = fileChooser.showSaveDialog(root.getScene().getWindow());
    }
     */
}
