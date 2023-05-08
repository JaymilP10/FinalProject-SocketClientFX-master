package com.jtconnors.socketclientfx;

import com.jtconnors.socket.DebugFlags;
import com.jtconnors.socket.SocketListener;
import java.lang.invoke.MethodHandles;
import java.net.URL;
import java.util.ArrayList;
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
import javafx.scene.control.*;
import com.jtconnors.socketfx.FxSocketClient;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
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
    private Button connectButton, btnFindMatch, btnEnterName, btnReady;
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

//    @FXML
//    private ScrollPane scrollPane;

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

        for (int i = 0; i < displayButtons.length; i++) {
            for (int j = 0; j < displayButtons[0].length; j++) {
                displayButtons[i][j] = new Button();
                displayButtons[i][j].setPrefHeight(25);
                displayButtons[i][j].setPrefWidth(25);
                displayButtons[i][j].setMaxSize(50, 50);
                MAP.add(displayButtons[i][j], j, i);
            }
        }

        for (int i = 0; i < buttons.length; i++) {
            for (int j = 0; j < buttons[0].length; j++) {
                buttons[i][j] = new Button();
//                        buttons[i][j].setPrefSize(10, 10);
                buttons[i][j].setPrefHeight(50);
                buttons[i][j].setPrefWidth(50);
                buttons[i][j].setMaxSize(50, 50);

                map[i][j] = new Map(i, j, 4, false);

                if (i > 20 && i < 30 && j < 10){
                    map[i][j].Orignum = 1;
                    map[i][j].newNum = 1;
                } else if (i > 20 && i < 30 && j > 89){
                    map[i][j].Orignum = 2;
                    map[i][j].newNum = 2;
                } else if ((i < 10 && j >= 20 && j <= 79) || (i > 39 && j >= 20 && j <= 79)){
                    map[i][j].Orignum = 3;
                    map[i][j].newNum = 3;
                }
//                MAP.add(buttons[i][j], j, i);
            }
        }

        for (int i = 10; i <= 39; i++) {
            for (int j = 45; j < 55; j++) {
                map[i][j].Orignum = 1;
                map[i][j].newNum = 1;
            }
        }

        int x = 10;
        for (int i = 30; i <= 39; i++) {
            for (int k = 0; k <= 9; k++) {
//                map[i + k][j] = new Map(i + k, j, 3, false);
//                map[i - k][j] = new Map(i - k, j, 3, false);
                map[i + k][x].Orignum = 3;
                map[i + k][x].newNum = 3;
                map[i - k][x].Orignum = 3;
                map[i - k][x].newNum = 3;
                map[i][x + k].Orignum = 3;
                map[i][x + k].newNum = 3;
                map[i][x - k].Orignum = 3;
                map[i][x - k].newNum = 3;
//                map[i + k][j].isWall = false;
//                map[i - k][j].isWall = false;
            }
            x++;
        }

        int j = 89;
        for (int i = 20; i >= 10; i--) {
            for (int k = 0; k <= 9; k++) {
//                map[i + k][j] = new Map(i + k, j, 3, false);
//                map[i - k][j] = new Map(i - k, j, 3, false);
                map[i + k][j].Orignum = 3;
                map[i + k][j].newNum = 3;
                map[i - k][j].Orignum = 3;
                map[i - k][j].newNum = 3;
                map[i][j + k].Orignum = 3;
                map[i][j + k].newNum = 3;
                map[i][j - k].Orignum = 3;
                map[i][j - k].newNum = 3;
//                map[i + k][j].isWall = false;
//                map[i - k][j].isWall = false;
            }
            j--;
        }

        int y = 89;
        for (int i = 30; i <= 39; i++) {
            for (int k = 0; k <= 9; k++) {
//                map[i + k][j] = new Map(i + k, j, 3, false);
//                map[i - k][j] = new Map(i - k, j, 3, false);
                map[i + k][y].Orignum = 3;
                map[i + k][y].newNum = 3;
                map[i - k][y].Orignum = 3;
                map[i - k][y].newNum = 3;
                map[i][y + k].Orignum = 3;
                map[i][y + k].newNum = 3;
                map[i][y - k].Orignum = 3;
                map[i][y - k].newNum = 3;
//                map[i + k][j].isWall = false;
//                map[i - k][j].isWall = false;
            }
            y--;
        }

        int z = 10;
        for (int i = 20; i >= 10; i--) {
            for (int k = 0; k <= 9; k++) {
//                map[i + k][j] = new Map(i + k, j, 3, false);
//                map[i - k][j] = new Map(i - k, j, 3, false);
                map[i + k][z].Orignum = 3;
                map[i + k][z].newNum = 3;
                map[i - k][z].Orignum = 3;
                map[i - k][z].newNum = 3;
                map[i][z + k].Orignum = 3;
                map[i][z + k].newNum = 3;
                map[i][z - k].Orignum = 3;
                map[i][z - k].newNum = 3;
//                map[i + k][j].isWall = false;
//                map[i - k][j].isWall = false;
            }
            z++;
        }

        dragon = new Monsters(50, 500, 10, .25, 43, 32, buttons);
//        for (int i = 0; i < buttons.length; i++) {
//            for (int j = 0; j < buttons[0].length; j++) {
//                buttons[i][j] = new Button();
////                        buttons[i][j].setPrefSize(10, 10);
//                buttons[i][j].setPrefHeight(20);
//                buttons[i][j].setPrefWidth(20);
//
//                map[i][j] = new Map(i, j, 4, false);
//
//                if (i > 20 && i < 30 && j < 10){
//                    map[i][j].num = 1;
//                } else if (i > 20 && i < 30 && j > 89){
//                    map[i][j].num = 2;
//                } else if ((i < 10 && j >= 20 && j <= 79) || (i > 39 && j >= 20 && j <= 79)){
//                    map[i][j].num = 3;
//                }
//                MAP.add(buttons[i][j], j, i);
//            }
//        }
//
//        for (int i = 10; i <= 39; i++) {
//            for (int j = 45; j < 55; j++) {
//                map[i][j].num = 1;
//            }
//        }
//
//        int x = 10;
//        for (int i = 30; i <= 39; i++) {
//            for (int k = 0; k <= 9; k++) {
////                map[i + k][j] = new Map(i + k, j, 3, false);
////                map[i - k][j] = new Map(i - k, j, 3, false);
//                map[i + k][x].num = 3;
//                map[i - k][x].num = 3;
//                map[i][x + k].num = 3;
//                map[i][x - k].num = 3;
////                map[i + k][j].isWall = false;
////                map[i - k][j].isWall = false;
//            }
//            x++;
//        }
//
//        int j = 89;
//        for (int i = 20; i >= 10; i--) {
//            for (int k = 0; k <= 9; k++) {
////                map[i + k][j] = new Map(i + k, j, 3, false);
////                map[i - k][j] = new Map(i - k, j, 3, false);
//                map[i + k][j].num = 3;
//                map[i - k][j].num = 3;
//                map[i][j + k].num = 3;
//                map[i][j - k].num = 3;
////                map[i + k][j].isWall = false;
////                map[i - k][j].isWall = false;
//            }
//            j--;
//        }
//
//        int y = 89;
//        for (int i = 30; i <= 39; i++) {
//            for (int k = 0; k <= 9; k++) {
////                map[i + k][j] = new Map(i + k, j, 3, false);
////                map[i - k][j] = new Map(i - k, j, 3, false);
//                map[i + k][y].num = 3;
//                map[i - k][y].num = 3;
//                map[i][y + k].num = 3;
//                map[i][y - k].num = 3;
////                map[i + k][j].isWall = false;
////                map[i - k][j].isWall = false;
//            }
//            y--;
//        }
//
//        int z = 10;
//        for (int i = 20; i >= 10; i--) {
//            for (int k = 0; k <= 9; k++) {
////                map[i + k][j] = new Map(i + k, j, 3, false);
////                map[i - k][j] = new Map(i - k, j, 3, false);
//                map[i + k][z].num = 3;
//                map[i - k][z].num = 3;
//                map[i][z + k].num = 3;
//                map[i][z - k].num = 3;
////                map[i + k][j].isWall = false;
////                map[i - k][j].isWall = false;
//            }
//            z++;
//        }
        Weapon LMG = new Weapon("LMG", "MachineGun", 25, 10, 100, .1);
        Weapon RPG = new Weapon("RPG", "Rocket Launcher", 100, 400, 5, .25);
        Weapon AR = new Weapon("AR", "Assault Rifle", 40, 50, 30, .25);
        Weapon Sniper = new Weapon("Sniper", "Sniper", 100, 200, 10, .25);
        weapons.add(LMG);
        weapons.add(RPG);
        weapons.add(AR);
        weapons.add(Sniper);
        lstPrimaryWeapon.getItems().add(LMG.weaponName);
        lstPrimaryWeapon.getItems().add(AR.weaponName);
        lstSecondaryWeapon.getItems().add(RPG.weaponName);
        lstSecondaryWeapon.getItems().add(Sniper.weaponName);
    }

    public void start(){
        updateScreen();
        System.out.println("called start");
        new AnimationTimer(){
            @Override
            public void handle(long now) {
                if(startTime>0){
                    if (now - startTime > (900000000.0 * .1)) {
//                        System.out.println("ANIMATION TIMER IS WORKING");
                        if (frame < 9) {
                            frame++;
                        } else if (frame == 9) {
                            frame = 1;
                        }
                        dragon.changeImage(buttons, frame);
                        startTime = System.nanoTime();
                    }
                }
            }
        }.start();

        System.out.println("here");

        EventHandler<MouseEvent> z = new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                System.out.println("clicked something");
                if (event.getButton() == MouseButton.PRIMARY){
                    System.out.println("clicked primary");
                    Bullets bullet = new Bullets(player.xLoc, player.yLoc);
                    currentlyUsingWeapon.squaresTravelled = 0;
                    for (int i = 0; i < 26; i++) {
                        for (int j = 0; j < 26; j++) {
                            if (((Button) event.getSource()) == displayButtons[i][j]){
//                                int rowTo = player.yLoc + (i - (player.yLoc + 13));
//                                int colTo = player.xLoc + (j - (player.xLoc + 13));

                                int rowTo = player.yLoc + (i - 13);
                                int colTo = player.xLoc + (j - 13);

//                                int finalJ = j;
//                                int finalI = i;
                                new AnimationTimer(){
                                    @Override
                                    public void handle(long now) {
//                                        System.out.println("in animation timer");
                                        if (currentlyUsingWeapon.startTime > 0){
//                                            System.out.println("lollolololol");
                                            if (now - currentlyUsingWeapon.startTime > (900000000.0 * 2) && currentlyUsingWeapon.squaresTravelled < currentlyUsingWeapon.range){
                                                System.out.println("range: " + currentlyUsingWeapon.range);
                                                bullet.fire(colTo, rowTo, buttons, map, this);
                                                updateScreen();
                                                bullet.startTime = System.nanoTime();
                                            } else {
                                                this.stop();
                                            }
                                        }
                                    }
                                }.start();
                                System.out.println(i + " " + j);
//                            System.out.println("oc:"+i+"or:"+j);

                            }
                        }
                    }
                } else if (event.getButton() == MouseButton.SECONDARY){
                    System.out.println("clicked secondary");
                }
            }
        };
        for (int i = 0; i < 26; i++) {
            for (int j = 0; j < 26; j++) {
//                btn[i][j].setOnMouseClicked(z);
                displayButtons[i][j].setOnMouseClicked(z);
            }
        }
//            EventHandler<MouseEvent> z = new EventHandler<MouseEvent>() {
//                @Override
//                public void handle(MouseEvent event) {
//                    //all button code goes here
//                    for (int i = 0; i < 50; i++) {
//                        for (int j = 0; j < 100; j++) {
//                            if (((Button) event.getSource()) == buttons[i][j]){
////                                System.out.println("oc:"+i+"or:"+j);
//                                player.changeLoc(map, i, j);
//                                socketServer.postUpdate("PlayerMoved:" + playerName + "i:" + i + "j:" + j);
//                                startTime = System.nanoTime();
//                                System.out.println(startTime);
//                                System.out.println("end");
//                            }
//                        }
//                    }
//                }
//            };
//            for (int i = 0; i < 5; i++) {
//                for (int j = 0; j < 4; j++) {
////                btn[i][j].setOnMouseClicked(z);
//                    buttons[i][j].setOnMouseClicked(z);
//                }
//            }



    }

    ArrayList<Weapon> weapons = new ArrayList<>();
    Weapon primaryWeapon = new Weapon();
    Weapon secondaryWeapon = new Weapon();
    Weapon currentlyUsingWeapon = new Weapon();
    int numPlayersReady = 0;

    ArrayList<Player> players = new ArrayList<>();

    Monsters dragon;

    int frame = 0;

    Player player;

//    @FXML
//    private void pickLoadout(ActionEvent event){
//        String primaryWeaponName = lstPrimaryWeapon.getSelectionModel().getSelectedItem().toString();
//        String secondaryWeaponName = lstSecondaryWeapon.getSelectionModel().getSelectedItem().toString();
//        for (Weapon weapon : weapons) {
//            if (weapon.weaponName.equals(primaryWeaponName))
//                primaryWeapon = weapon;
//            else if (weapon.weaponName.equals(secondaryWeaponName))
//                secondaryWeapon = weapon;
//        }
//        player = new Player(playerName, 1, 250, 25, .5, primaryWeapon, secondaryWeapon, 5, 5);
//        players.add(player);
//        btnReady.setDisable(true);
//        btnReady.setVisible(false);
//        socket.sendMessage("Ready");
//        socket.sendMessage("Create Player:" + playerName);
//        MAP.setVisible(true);
//    }

    @FXML
    private void pickLoadout(ActionEvent event){
        String primaryWeaponName = lstPrimaryWeapon.getSelectionModel().getSelectedItem().toString();
        String secondaryWeaponName = lstSecondaryWeapon.getSelectionModel().getSelectedItem().toString();
        for (Weapon weapon : weapons) {
            if (weapon.weaponName.equals(primaryWeaponName))
                primaryWeapon = weapon;
            else if (weapon.weaponName.equals(secondaryWeaponName))
                secondaryWeapon = weapon;
        }
        currentlyUsingWeapon = primaryWeapon;
        btnReady.setDisable(true);
        btnReady.setVisible(false);
        numPlayersReady++;
        player = new Player(playerName, 1, 250, 25, .5, 5, 23, map);
        player.primary = primaryWeapon;
        player.secondary = secondaryWeapon;
        updateScreen();
        players.add(player);
//        socketServer.postUpdate("Create Player:" + playerName);
        socket.sendMessage("Ready");
        if (numPlayersReady == 2){
            lstPrimaryWeapon.setVisible(false);
            lstSecondaryWeapon.setVisible(false);
            lstItems.setVisible(false);
//            scrollPane.setVisible(true);
            txtName.setVisible(false);
            btnEnterName.setVisible(false);
            lblPickLoadout.setVisible(false);
            MAP.setVisible(true);
            start();
        }
    }

    public void enterName() {
        playerName = txtName.getText();
//        socket.sendMessage("Client1Name" + playerName);
        btnFindMatch.setDisable(false);
    }

    @FXML
    private void move(KeyEvent keyEvent){
        System.out.println("works");
        KeyCode key = keyEvent.getCode();
        System.out.println("Key Pressed: " + key);
        if (keyEvent.getCode().equals(KeyCode.D) && player.xLoc < 99) {  // left arrow key
            map[player.yLoc - 1][player.xLoc - 1].newNum = map[player.yLoc - 1][player.xLoc - 1].Orignum;
            map[player.yLoc - 1][player.xLoc].newNum = map[player.yLoc - 1][player.xLoc].Orignum;
            map[player.yLoc][player.xLoc - 1].newNum = map[player.yLoc][player.xLoc - 1].Orignum;
            map[player.yLoc][player.xLoc].newNum = map[player.yLoc][player.xLoc].Orignum;
            player.xLoc++;
        }
        else if (keyEvent.getCode().equals(KeyCode.A) && player.xLoc > 0 && player.xLoc - 1 > 1) {
            map[player.yLoc - 1][player.xLoc - 1].newNum = map[player.yLoc - 1][player.xLoc - 1].Orignum;
            map[player.yLoc - 1][player.xLoc].newNum = map[player.yLoc - 1][player.xLoc].Orignum;
            map[player.yLoc][player.xLoc - 1].newNum = map[player.yLoc][player.xLoc - 1].Orignum;
            map[player.yLoc][player.xLoc].newNum = map[player.yLoc][player.xLoc].Orignum;
            System.out.println("rk");
            player.xLoc--;
        }
        else if (keyEvent.getCode().equals(KeyCode.W) && player.yLoc > 0 && player.yLoc - 1 > 1) {
            map[player.yLoc - 1][player.xLoc - 1].newNum = map[player.yLoc - 1][player.xLoc - 1].Orignum;
            map[player.yLoc - 1][player.xLoc].newNum = map[player.yLoc - 1][player.xLoc].Orignum;
            map[player.yLoc][player.xLoc - 1].newNum = map[player.yLoc][player.xLoc - 1].Orignum;
            map[player.yLoc][player.xLoc].newNum = map[player.yLoc][player.xLoc].Orignum;
            System.out.println("w");
            player.yLoc--;
        }
        else if (key == KeyCode.S && player.yLoc < 49) {
            map[player.yLoc - 1][player.xLoc - 1].newNum = map[player.yLoc - 1][player.xLoc - 1].Orignum;
            map[player.yLoc - 1][player.xLoc].newNum = map[player.yLoc - 1][player.xLoc].Orignum;
            map[player.yLoc][player.xLoc - 1].newNum = map[player.yLoc][player.xLoc - 1].Orignum;
            map[player.yLoc][player.xLoc].newNum = map[player.yLoc][player.xLoc].Orignum;
            player.yLoc++;
        } else if (key == KeyCode.Q && player.yLoc > 0 && player.xLoc > 0 && player.yLoc - 1 > 1 && player.xLoc - 1 > 1){
            map[player.yLoc - 1][player.xLoc - 1].newNum = map[player.yLoc - 1][player.xLoc - 1].Orignum;
            map[player.yLoc - 1][player.xLoc].newNum = map[player.yLoc - 1][player.xLoc].Orignum;
            map[player.yLoc][player.xLoc - 1].newNum = map[player.yLoc][player.xLoc - 1].Orignum;
            map[player.yLoc][player.xLoc].newNum = map[player.yLoc][player.xLoc].Orignum;
            player.yLoc--;
            player.xLoc--;
        } else if (key == KeyCode.E && player.yLoc > 0 && player.xLoc < 99 && player.yLoc - 1 > 1){
            map[player.yLoc - 1][player.xLoc - 1].newNum = map[player.yLoc - 1][player.xLoc - 1].Orignum;
            map[player.yLoc - 1][player.xLoc].newNum = map[player.yLoc - 1][player.xLoc].Orignum;
            map[player.yLoc][player.xLoc - 1].newNum = map[player.yLoc][player.xLoc - 1].Orignum;
            map[player.yLoc][player.xLoc].newNum = map[player.yLoc][player.xLoc].Orignum;
            player.yLoc--;
            player.xLoc++;
        } else if (key == KeyCode.Z && player.yLoc < 99 && player.xLoc > 0 && player.xLoc - 1 > 1){
            map[player.yLoc - 1][player.xLoc - 1].newNum = map[player.yLoc - 1][player.xLoc - 1].Orignum;
            map[player.yLoc - 1][player.xLoc].newNum = map[player.yLoc - 1][player.xLoc].Orignum;
            map[player.yLoc][player.xLoc - 1].newNum = map[player.yLoc][player.xLoc - 1].Orignum;
            map[player.yLoc][player.xLoc].newNum = map[player.yLoc][player.xLoc].Orignum;
            player.yLoc++;
            player.xLoc--;
        } else if (key == KeyCode.C && player.yLoc < 49 && player.xLoc < 99){
            map[player.yLoc - 1][player.xLoc - 1].newNum = map[player.yLoc - 1][player.xLoc - 1].Orignum;
            map[player.yLoc - 1][player.xLoc].newNum = map[player.yLoc - 1][player.xLoc].Orignum;
            map[player.yLoc][player.xLoc - 1].newNum = map[player.yLoc][player.xLoc - 1].Orignum;
            map[player.yLoc][player.xLoc].newNum = map[player.yLoc][player.xLoc].Orignum;
            player.yLoc++;
            player.xLoc++;
        }
        map[player.yLoc - 1][player.xLoc - 1].newNum = 6;
        map[player.yLoc - 1][player.xLoc].newNum = 6;
        map[player.yLoc][player.xLoc - 1].newNum = 6;
        map[player.yLoc][player.xLoc].newNum = 6;
        updateScreen();
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
            } else if (line.startsWith("NumConnections")){
                int numConnections = Integer.parseInt(line.substring(line.indexOf("s") + 1));
                if (numConnections == 2){
                    txtName.setVisible(true);
                    btnFindMatch.setDisable(true);
                    btnEnterName.setDisable(true);
                    btnEnterName.setVisible(false);
                    btnFindMatch.setVisible(false);
                    lblPickLoadout.setVisible(true);
                    lstPrimaryWeapon.setVisible(true);
                    lstSecondaryWeapon.setVisible(true);
                    lstItems.setVisible(true);
//                    scrollPane.setVisible(false);
                    lblPickLoadout.setVisible(false);
                    txtName.setVisible(false);
                }
            } else if (line.startsWith("Ready")){
                numPlayersReady = Integer.parseInt(line.substring(line.indexOf("y") + 1));
                if (numPlayersReady == 2){
                    lstPrimaryWeapon.setVisible(false);
                    lstSecondaryWeapon.setVisible(false);
                    lstItems.setVisible(false);
//                    scrollPane.setVisible(true);
                    lblPickLoadout.setVisible(false);
                    txtName.setVisible(false);
                    MAP.setVisible(true);
                }
            } else if (line.startsWith("Create Player:")){
//                players.add(new Player(line.substring(line.indexOf(":") + 1), 1, 250, 25, .5, primaryWeapon, secondaryWeapon, 5, 5));
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

    private double startTime;
    private void inGame(ActionEvent event){
        EventHandler<MouseEvent> z = new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                //all button code goes here
                for (int i = 0; i < 5; i++) {
                    for (int j = 0; j < 4; j++) {
                        if (((Button) event.getSource()) == buttons[i][j]){
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

    Button[][] buttons = new Button[50][100];

    Button[][] displayButtons = new Button[26][26];

    Map[][] map = new Map[50][100];

    @FXML
    private GridPane MAP;

    private void updateScreen(){
        for (int i = 0; i < map.length; i++) {
            for (int c = 0; c < map[0].length; c++) {
                if (map[i][c].newNum == 1){
                    buttons[i][c].setStyle("-fx-background-color: blue");
                } else if (map[i][c].newNum == 2){
                    buttons[i][c].setStyle("-fx-background-color: red");
                } else if (map[i][c].newNum == 3){
                    buttons[i][c].setStyle("-fx-background-color: yellow");
                } else if (map[i][c].newNum == 4){
                    buttons[i][c].setStyle("-fx-background-color: green");
                } else if (map[i][c].newNum == 5){
                    buttons[i][c].setStyle("-fx-background-color: black");
                } else if (map[i][c].newNum == 6){
                    buttons[i][c].setStyle("-fx-background-color: grey");
                } else if (map[i][c].newNum == 7){
                    buttons[i][c].setStyle("-fx-background-color: brown");
                }
            }
        }

        for (int i = 0; i < 13; i++) {
            for (int k = 0; k < 13; k++) {
                if (player.yLoc - (13 - i) > 0 && player.xLoc - (13 - k) > 0){
                    displayButtons[i][k].setStyle(buttons[player.yLoc - (13 - i)][player.xLoc - (13 - k)].getStyle());
                    displayButtons[i][k].setGraphic(buttons[player.yLoc - (13 - i)][player.xLoc - (13 - k)].getGraphic());
                } else {
                    displayButtons[i][k].setStyle("-fx-background-color: black");
                }
            }
        }

        for (int i = 0; i < 13; i++) {
            for (int k = 0; k < 13; k++) {
                if (player.yLoc - (13 - i) > 0 && player.xLoc + k < 99){
                    displayButtons[i][k + 13].setStyle(buttons[player.yLoc - (13 - i)][player.xLoc + k].getStyle());
                    displayButtons[i][k + 13].setGraphic(buttons[player.yLoc - (13 - i)][player.xLoc + k].getGraphic());
                } else {
                    displayButtons[i][k + 13].setStyle("-fx-background-color: black");
                }
            }
        }

        for (int i = 0; i < 13; i++) {
            for (int k = 0; k < 13; k++) {
                if (player.yLoc + i < 49 && player.xLoc + k < 99){
                    displayButtons[i + 13][k + 13].setStyle(buttons[player.yLoc + i][player.xLoc + k].getStyle());
                    displayButtons[i + 13][k + 13].setGraphic(buttons[player.yLoc + i][player.xLoc + k].getGraphic());
                } else {
                    displayButtons[i + 13][k + 13].setStyle("-fx-background-color: black");
                }
            }
        }

        for (int i = 0; i < 13; i++) {
            for (int k = 0; k < 13; k++) {
                if (player.yLoc + i < 49 && player.xLoc - (13 - k) > 0){
                    displayButtons[i + 13][k].setStyle(buttons[player.yLoc + i][player.xLoc - (13 - k)].getStyle());
                    displayButtons[i + 13][k].setGraphic(buttons[player.yLoc + i][player.xLoc - (13 - k)].getGraphic());
                } else {
                    displayButtons[i + 13][k].setStyle("-fx-background-color: black");
                }
            }
        }
    }
}
