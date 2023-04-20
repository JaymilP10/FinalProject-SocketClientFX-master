package com.jtconnors.socketclientfx;

import com.jtconnors.socket.DebugFlags;
import com.jtconnors.socket.SocketListener;
import java.lang.invoke.MethodHandles;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Logger;

import javafx.animation.AnimationTimer;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import com.jtconnors.socketfx.FxSocketClient;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;

/**
 * FXML Controller class
 *
 * @author jtconnor
 */
public class FXMLDocumentController implements Initializable {

    @FXML
    private ListView<String> rcvdMsgsListView;
    @FXML
    private ListView<String> sentMsgsListView;
    @FXML
    private Button sendButton;
    @FXML
    private TextField sendTextField;
    @FXML
    private TextField selectedTextField, txtName;
    @FXML
    private Button connectButton, btnFindMatch, btnEnterName;
    @FXML
    private Button disconnectButton;
    @FXML
    private TextField hostTextField;
    @FXML
    private TextField portTextField;
    @FXML
    private CheckBox autoConnectCheckBox;
    @FXML
    private TextField retryIntervalTextField;
    @FXML
    private Label connectedLabel, lblPickLoadout;
    @FXML
    private ListView lstPrimaryWeapon, lstSecondaryWeapon, lstItems;

    private String playerName;

    private final static Logger LOGGER
            = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());

    private ObservableList<String> rcvdMsgsData;
    private ObservableList<String> sentMsgsData;
    private ListView<String> lastSelectedListView;
    private Tooltip portTooltip;
    private Tooltip hostTooltip;

    private boolean connected;
    private volatile boolean isAutoConnected;

    private static final int DEFAULT_RETRY_INTERVAL = 2000; // in milliseconds

    public void enterName() {
        playerName = txtName.getText();
        btnFindMatch.setDisable(false);
    }

    public enum ConnectionDisplayState {

        DISCONNECTED, ATTEMPTING, CONNECTED, AUTOCONNECTED, AUTOATTEMPTING
    }

    private FxSocketClient socket;

    /*
     * Synchronized method set up to wait until there is no socket connection.
     * When notifyDisconnected() is called, waiting will cease.
     */
    private synchronized void waitForDisconnect() {
        while (connected) {
            try {
                wait();
            } catch (InterruptedException e) {
            }
        }
    }

    /*
     * Synchronized method responsible for notifying waitForDisconnect()
     * method that it's OK to stop waiting.
     */
    private synchronized void notifyDisconnected() {
        connected = false;
        notifyAll();
    }

    /*
     * Synchronized method to set isConnected boolean
     */
    private synchronized void setIsConnected(boolean connected) {
        this.connected = connected;
    }

    /*
     * Synchronized method to check for value of connected boolean
     */
    private synchronized boolean isConnected() {
        return (connected);
    }

    private void connect() {
        socket = new FxSocketClient(new FxSocketListener(),
                hostTextField.getText(),
                Integer.valueOf(portTextField.getText()),
                DebugFlags.instance().DEBUG_NONE);
        socket.connect();
    }

    private void autoConnect() {
        new Thread() {
            @Override
            public void run() {
                while (isAutoConnected) {
                    if (!isConnected()) {
                        socket = new FxSocketClient(new FxSocketListener(),
                                hostTextField.getText(),
                                Integer.valueOf(portTextField.getText()),
                                DebugFlags.instance().DEBUG_NONE);
                        socket.connect();
                    }
                    waitForDisconnect();
                    try {
                        Thread.sleep(Integer.valueOf(retryIntervalTextField.getText()) * 1000);
                    } catch (InterruptedException ex) {
                    }
                }
            }
        }.start();
    }

    private void displayState(ConnectionDisplayState state) {
        switch (state) {
            case DISCONNECTED:
                connectButton.setDisable(false);
                disconnectButton.setDisable(true);
//                sendButton.setDisable(true);
//                sendTextField.setDisable(true);
                connectedLabel.setText("Not connected");
                break;
            case ATTEMPTING:
            case AUTOATTEMPTING:
                connectButton.setDisable(true);
                disconnectButton.setDisable(true);
//                sendButton.setDisable(true);
//                sendTextField.setDisable(true);
                connectedLabel.setText("Attempting connection");
                break;
            case CONNECTED:
                connectButton.setDisable(true);
                disconnectButton.setDisable(false);
//                sendButton.setDisable(false);
//                sendTextField.setDisable(false);
                connectedLabel.setText("Connected");
                break;
            case AUTOCONNECTED:
                connectButton.setDisable(true);
                disconnectButton.setDisable(true);
                sendButton.setDisable(false);
                sendTextField.setDisable(false);
                connectedLabel.setText("Connected");
                break;
        }
    }

    /**
     * Initializes the controller class.
     *
     * @param url
     * @param rb
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setIsConnected(false);
        isAutoConnected = false;
        displayState(ConnectionDisplayState.DISCONNECTED);

//        sentMsgsData = FXCollections.observableArrayList();
//        sentMsgsListView.setItems(sentMsgsData);
//        sentMsgsListView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
//        sentMsgsListView.setOnMouseClicked((Event event) -> {
//            String selectedItem
//                    = sentMsgsListView.getSelectionModel().getSelectedItem();
//            if (selectedItem != null && !selectedItem.equals("null")) {
//                selectedTextField.setText("Sent: " + selectedItem);
//                lastSelectedListView = sentMsgsListView;
//            }
//        });
//
//        rcvdMsgsData = FXCollections.observableArrayList();
//        rcvdMsgsListView.setItems(rcvdMsgsData);
//        rcvdMsgsListView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
//        rcvdMsgsListView.setOnMouseClicked((Event event) -> {
//            String selectedItem
//                    = rcvdMsgsListView.getSelectionModel().getSelectedItem();
//            if (selectedItem != null && !selectedItem.equals("null")) {
//                selectedTextField.setText("Received: " + selectedItem);
//                lastSelectedListView = rcvdMsgsListView;
//            }
//        });

        portTooltip = new Tooltip("Port number cannot be modified once\n" +
        "the first connection attempt is initiated.\n" +
        "Restart application in order to change.");

        portTextField.textProperty().addListener((obs, oldText, newText) -> {
            try {
                Integer.parseInt(newText);
            } catch (NumberFormatException e) {
                portTextField.setText(oldText);
            }
        });

        hostTooltip = new Tooltip("Host cannot be modified once\n" +
        "the first connection attempt is initiated.\n" +
        "Restart application to change.");

        retryIntervalTextField.textProperty().addListener((obs, oldText, newText) -> {
            try {
                Integer.parseInt(newText);
            } catch (NumberFormatException e) {
                retryIntervalTextField.setText(oldText);
            }
        });

        Runtime.getRuntime().addShutdownHook(new ShutDownThread());

        for (int i = 0; i < map.length; i++) {
            for (int j = 0; j < map[0].length; j++) {
                map[i][j] = new Button();
//                        map[i][j].setPrefSize(10, 10);
                map[i][j].setPrefHeight(20);
                map[i][j].setPrefWidth(20);

                if (i > 89 && j < 10){
                    intMap[i][j] = 1;
                } else if (i < 10 && j > 89){
                    intMap[i][j] = 2;
                } else if ((i < 10 && j >= 10 && j <= 89) || (j < 10 && i <= 89) || (i > 89 && j >= 10 && j <= 89) || (j > 89 && i >= 10)){
                    intMap[i][j] = 3;
                } else {
                    intMap[i][j] = 4;
                }
                MAP.add(map[i][j], j, i);
            }
        }

        int x = 10;
        for (int i = 10; i <= 89; i++) {
            for (int k = 0; k <= 7; k++) {
                intMap[i + k][x] = 1;
                intMap[i][x + k] = 1;
            }
            x++;
        }

        int j = 10;
        for (int i = 89; i >= 10; i--) {
            for (int k = 0; k <= 7; k++) {
                intMap[i + k][j] = 3;
                intMap[i - k][j] = 3;
            }
            j++;
        }

    }

    class ShutDownThread extends Thread {

        @Override
        public void run() {
            if (socket != null) {
                if (socket.debugFlagIsSet(DebugFlags.instance().DEBUG_STATUS)) {
                    LOGGER.info("ShutdownHook: Shutting down Server Socket");
                }
                socket.shutdown();
            }
        }
    }

    class FxSocketListener implements SocketListener {

        @Override
        public void onMessage(String line) {
            if (line != null && !line.equals("")) {
//                rcvdMsgsData.add(line);
            }
            if (line.equals("Update Screen")){
                updateScreen();
            }
        }

        @Override
        public void onClosedStatus(boolean isClosed) {
            if (isClosed) {
                notifyDisconnected();
                if (isAutoConnected) {
                    displayState(ConnectionDisplayState.AUTOATTEMPTING);
                } else {
                    displayState(ConnectionDisplayState.DISCONNECTED);
                }
            } else {
                setIsConnected(true);
                if (isAutoConnected) {
                    displayState(ConnectionDisplayState.AUTOCONNECTED);
                } else {
                    displayState(ConnectionDisplayState.CONNECTED);
                }
            }
        }
    }

    @FXML
    private void handleClearRcvdMsgsButton(ActionEvent event) {
        rcvdMsgsData.clear();
        if (lastSelectedListView == rcvdMsgsListView) {
            selectedTextField.clear();
        }
    }

    @FXML
    private void handleClearSentMsgsButton(ActionEvent event) {
        sentMsgsData.clear();
        if (lastSelectedListView == sentMsgsListView) {
            selectedTextField.clear();
        }
    }

    @FXML
    private void handleSendMessageButton(ActionEvent event) {
        if (!sendTextField.getText().equals("")) {
            socket.sendMessage(sendTextField.getText());
            sentMsgsData.add(sendTextField.getText());
        }
    }

    @FXML
    private void handleConnectButton(ActionEvent event) {
        displayState(ConnectionDisplayState.ATTEMPTING);
        hostTextField.setEditable(false);
        hostTextField.setTooltip(hostTooltip);
        portTextField.setEditable(false);
        portTextField.setTooltip(portTooltip);
        connect();
    }

    @FXML
    private void handleDisconnectButton(ActionEvent event) {
        socket.shutdown();
    }

    @FXML
    private void handleAutoConnectCheckBox(ActionEvent event) {
        if (autoConnectCheckBox.isSelected()) {
            isAutoConnected = true;
            hostTextField.setEditable(false);
            hostTextField.setTooltip(hostTooltip);
            portTextField.setEditable(false);
            portTextField.setTooltip(portTooltip);
            if (isConnected()) {
                displayState(ConnectionDisplayState.AUTOCONNECTED);
            } else {
                displayState(ConnectionDisplayState.AUTOATTEMPTING);
                autoConnect();
            }
        } else {
            isAutoConnected = false;
            if (isConnected()) {
                displayState(ConnectionDisplayState.CONNECTED);
            } else {
                displayState(ConnectionDisplayState.DISCONNECTED);
            }
        }
    }

    Button[][] map = new Button[100][100];

    int[][] intMap = new int[100][100];

    private double startTime;
    private void inGame(ActionEvent event){
        EventHandler<MouseEvent> z = new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                //all button code goes here
                for (int i = 0; i < 5; i++) {
                    for (int j = 0; j < 4; j++) {
                        if (((Button) event.getSource()) == map[i][j]){
//                            System.out.println("oc:"+i+"or:"+j);
                            startTime = System.nanoTime();
                            new AnimationTimer(){
                                @Override
                                public void handle(long now) {
                                    if(startTime>0){
                                        if (now - startTime > (900000000.0 * 2)){
                                            this.stop();
                                        }
                                    }
                                }
                            }.start();
                        }
                    }
                }
            }
        };
    }

    @FXML
    private GridPane MAP;

    private void updateScreen(){
        for (int i = 0; i < intMap.length; i++) {
            for (int j = 0; j < intMap[0].length; j++) {
                if (intMap[i][j] == 1){
                    map[i][j].setStyle("-fx-background-color: blue");
                } else if (intMap[i][j] == 2){
                    map[i][j].setStyle("-fx-background-color: red");
                } else if (intMap[i][j] == 3){
                    map[i][j].setStyle("-fx-background-color: yellow");
                } else if (intMap[i][j] == 4){
                    map[i][j].setStyle("-fx-background-color: green");
                } else if (intMap[i][j] == 5){
                    map[i][j].setStyle("-fx-background-color: black");
                }
            }
        }
    }
}
