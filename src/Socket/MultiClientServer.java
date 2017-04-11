package Socket;

/**
 * Created by yaxuSong on 2015/10/21.
 */

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.net.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;

class ServerThread extends Thread {
    Socket clientSocket;
    Hashtable<Object,Object> clientDataHash;
    Hashtable<Object,String> clientNameHash;
    Hashtable<Object,String> clientColorHash;
    String[] inChess;
    int[] roomNum;
    MessageServer server;

    boolean isClientClosed = false;

    ServerThread(Socket clientSocket, Hashtable<Object,Object> clientDataHash, Hashtable<Object,String> clientNameHash, int[] roomNum, String[] inChess, MessageServer server, Hashtable<Object,String> clientColorHash) {
        this.clientSocket = clientSocket;
        this.clientDataHash = clientDataHash;
        this.clientNameHash = clientNameHash;
        this.inChess = inChess;
        this.roomNum = roomNum;
        this.server = server;
        this.clientColorHash = clientColorHash;
    }

    public void Feedback(String feedbackString) {
        synchronized (clientDataHash) {
            DataOutputStream outData = (DataOutputStream) clientDataHash.get(clientSocket);
            try {
                outData.writeUTF(feedbackString);
            } catch (Exception eb) {
                eb.printStackTrace();
            }
        }

    }

    public String getDate() {
        DateFormat df = new SimpleDateFormat("HH:mm:ss");
        return df.format(new Date());
    }

    public Object getHashKey(Hashtable targetHash, Object hashValue) {
        Object hashKey;
        for (Enumeration enu = targetHash.keys(); enu.hasMoreElements(); ) {
            hashKey = (Object) enu.nextElement();
            if (hashValue.equals((Object) targetHash.get(hashKey)))
                return (hashKey);
        }
        return (null);
    }

    public String getRoomList() {
        String roomList = "";
        for (int i = 0; i < 50; i++) {
            if (inChess[i] != "0")
                roomList += " " + inChess[i];
        }
        return "/listroom" + (roomList);
    }

    public void messageTransfer(String message) {
        String clientName = new String();
        String peerName = new String();
        server.messageBoard.append(message);
        if (message.startsWith("/")) {
            if (message.startsWith("/sendname")) {
                clientName = message.substring(10);
                clientNameHash.put((Object)clientSocket, clientName);
            } else if (message.equals("/listroom")) {
                System.out.println("房间数：" + roomNum[0]);
                for (int i = 0; i < roomNum[0]; i++)
                    System.out.println(roomNum[0] + inChess[i]);
                Feedback(getRoomList());
            } else if (message.startsWith("/creategame")) {
                String chessServerName = message.substring(12);
                synchronized (clientNameHash) {
                    clientNameHash.put((Object)clientSocket, message.substring(12));
                }
                synchronized (inChess) {
                    inChess[roomNum[0]] = "1-" + chessServerName;
                    roomNum[0] += 1;
                }
            } else if (message.startsWith("/joingame")) {
                String serverName, selfName;
                String[] getName = message.split(" ");
                serverName = getName[1];
                selfName = getName[2];
                boolean IsOk = false;
                for (int i = 0; i < roomNum[0]; i++) {
                    String[] getTemp = inChess[i].split("-");
                    if (getTemp[0].equals("1") && getTemp[1].equals(serverName))
                    {
                        IsOk = true;
                        break;
                    }
                }
                if (IsOk) {
                    synchronized (inChess) {
                        for (int i = 0; i < roomNum[0]; i++) {
                            String[] getTemp = inChess[i].split("-");
                            if (getTemp[0].equals("1") && getTemp[1].equals(serverName))
                            {
                                inChess[i] = "2-" + serverName + "-" + selfName;
                                break;
                            }
                        }
                    }
                    chessPeerTalk(serverName, ("/joinok " + selfName));
                    Feedback("/joinok " + serverName);
                    synchronized (inChess) {
                        boolean isNeed=false;
                        for (int i = 0; i < roomNum[0]; i++) {
                            String[] getTemp = inChess[i].split("-");
                            if (getTemp[0].equals("1") && getTemp[1].equals(selfName))
                            {
                                inChess[i] = inChess[i + 1];
                                isNeed=true;
                            }
                        }
                        if(isNeed)
                        {
                            inChess[roomNum[0]] = "0";
                            roomNum[0]--;
                        }
                    }

                } else {
                    chessPeerTalk(selfName, "/reject");
                    try {
                        clientClose();
                    } catch (Exception ez) {
                    }
                }
            } else if (message.startsWith("/talk")) {
                String[] info = message.split(" ");
                peerName = info[1];
                String message1 = "";
                for (int i = 2; i < info.length; i++)
                    message1 += info[i];
                chessPeerTalk(peerName, "/talk " + (String) clientNameHash.get(clientSocket) + " " + message1);
            } else if (message.startsWith("/giveup")) {
                if (message.startsWith("/giveup1")) {
                    String chessClientName = message.substring(9);
                    synchronized (inChess) {
                        String status = "0";
                        for (int i = 0; i < roomNum[0]; i++) {
                            String[] getTemp = inChess[i].split("-");
                            if (getTemp[0].equals("1") && getTemp[1].equals(chessClientName))
                                status = "1-" + i;
                        }
                        if (status.startsWith("1")) {
                            String[] info = status.split("-");
                            for (int i = Integer.parseInt(info[1]); i < roomNum[0]; i++) {
                                inChess[i] = inChess[i + 1];
                            }
                            inChess[roomNum[0]] = "0";
                            roomNum[0]--;
                        }
                    }
                }
                if (message.startsWith("/giveup2")) {
                    String chessClientName = message.substring(9);
                    String[] getTemp = null;
                    int IsOk = -1;
                    for (int i = 0; i < roomNum[0]; i++) {
                        getTemp = inChess[i].split("-");
                        if (getTemp[0].equals("2") && getTemp[1].equals(chessClientName)) {
                            IsOk = 2;
                            break;
                        }
                        if (getTemp[0].equals("2") && getTemp[2].equals(chessClientName)) {
                            IsOk = 1;
                            break;
                        }
                    }
                    if (IsOk != -1) {
                        chessPeerTalk(getTemp[IsOk], "/youwin1");
                        synchronized (inChess) {
                            for (int i = 0; i < roomNum[0]; i++) {
                                String[] getTemp1 = inChess[i].split("-");
                                if (getTemp[0].equals("2") && (getTemp1[1].equals(chessClientName) || getTemp1[2].equals(chessClientName))) {
                                    if (roomNum[0] < 49)
                                        inChess[i] = inChess[i + 1];
                                }
                            }
                            inChess[roomNum[0] - 1] = "0";
                            roomNum[0]--;
                        }
                    }
                }
            } else if (message.startsWith("/startgame ")) {
                String[] getColorName = message.split(" ");
                clientName = getColorName[1];
                System.out.println("客户名"+clientName+" 颜色"+getColorName[2]+"房间数"+roomNum[0]);
                clientColorHash.put((Object)clientSocket, getColorName[2]);
                for (int i = 0; i < roomNum[0]; i++) {
                    String[] getTemp = inChess[i].split("-");
                    System.out.println(" ：：："+getTemp[0]+">"+getTemp[1]+">"+getTemp[2]);
                    if (getTemp[0].equals("2") && (getTemp[1].equals(clientName) || getTemp[2].equals(clientName))) {
                        {
                            if (getTemp[1].equals(clientName)) {
                                String colorName = (String) clientColorHash.get(getHashKey(clientNameHash, getTemp[2]));
                                if (colorName.equals("-1"))
                                {
                                    System.out.println("房主：Dadasd");
                                    Feedback("/wait");
                                }
                                else {
                                    System.out.println("房主：dadada");
                                    Feedback("/ready " + colorName);
                                    chessPeerTalk(getTemp[2], "/youfirst " + colorName);
                                }
                            }
                            if (getTemp[2].equals(clientName)) {
                                String colorName = (String) clientColorHash.get(getHashKey(clientNameHash, getTemp[1]));
                                if (colorName.equals("-1"))
                                {
                                    System.out.println("费房主：dasda");
                                    Feedback("/wait");
                                }
                                else {
                                    Feedback("/ready " + colorName);
                                    System.out.println("费房主：dasda非");
                                    chessPeerTalk(getTemp[1], "/youfirst " + colorName);
                                }
                            }
                        }
                    }
                }
            } else if (message.startsWith("/youfail")) {
                String[] getInfo = message.split(" ");
                chessPeerTalk(getInfo[1], "/youfail");
                synchronized (clientColorHash) {
                    String clientName1 = (String) clientNameHash.get(clientSocket);
                    for (int i = 0; i < roomNum[0]; i++) {
                        String[] getTemp1 = inChess[i].split("-");
                        if (getTemp1[0].equals("2") && (getTemp1[1].equals(clientName1) || getTemp1[2].equals(clientName1))) {
                            {
                                clientColorHash.put((Object)getHashKey(clientNameHash, getTemp1[1]), "-1");
                                clientColorHash.put((Object)getHashKey(clientNameHash, getTemp1[2]), "-1");
                            }
                        }
                    }
                }
                synchronized (inChess) {
                    String clientName1 = (String) clientNameHash.get(clientSocket);
                    for (int i = 0; i < roomNum[0]; i++) {
                        String[] getTemp1 = inChess[i].split("-");
                        if (getTemp1[0].equals("2") && (getTemp1[1].equals(clientName1) || getTemp1[2].equals(clientName1))) {
                            if (roomNum[0] < 49)
                                inChess[i] = inChess[i + 1];
                        }
                    }
                    inChess[roomNum[0] - 1] = "0";
                    roomNum[0]--;
                }
            } else if (message.startsWith("/chess")) {
                String[] getInfo = message.split(" ");
                chessPeerTalk(getInfo[1], message);
            } else if(message.startsWith("/takeback "))
            {
                chessPeerTalk(message.substring(10),message);
            }
            else if(message.startsWith("/takeback1")){
                chessPeerTalk(message.substring(11),"/takeback1");
            }
            else if(message.startsWith("/takeback2")){
                chessPeerTalk(message.substring(11),"/takeback2");
            }
            else{
                Feedback("/reject 发送无效！");
                return;
            }

        } else {
            message = getDate() + clientNameHash.get(clientSocket) + ":" + message;
            server.messageBoard.append(message + "\n");
            server.messageBoard.setCaretPosition(server.messageBoard.getText().length());
        }
    }

    public boolean chessPeerTalk(String chessPeerTalk, String chessTalkMessage) {

        for (Enumeration enu = clientDataHash.keys(); enu.hasMoreElements(); ) {
            Socket userClient = (Socket) enu.nextElement();

            if (chessPeerTalk.equals((String) clientNameHash.get(userClient)) && !chessPeerTalk.equals((String) clientNameHash.get(clientSocket))) {
                synchronized (clientDataHash) {
                    DataOutputStream peerOutData = (DataOutputStream) clientDataHash.get(userClient);
                    try {
                        peerOutData.writeUTF(chessTalkMessage);
                    } catch (IOException es) {
                        es.printStackTrace();
                    }
                }
                return (false);
            }
        }
        return (true);
    }


    public void firstCome() {
        Feedback("连接服务器成功！");
    }


    public void clientClose() {
        server.messageBoard.append("用户断开:" + clientSocket + "\n");

        synchronized (inChess) {
            String clientName = (String) clientNameHash.get(clientSocket);
            String status = "0";
            for (int i = 0; i < roomNum[0]; i++) {
                String[] getTemp = inChess[i].split("-");
                if (getTemp[0].equals("1") && getTemp[1].equals(clientName))
                    status = "1-" + i;
                if (getTemp[0].equals("2") && (getTemp[1].equals(clientName) || getTemp[2] == clientName))
                    status = "2-" + i;
            }
            if (status.startsWith("1")) {
                String[] info = status.split("-");
                for (int i = Integer.parseInt(info[1]); i < roomNum[0]; i++) {
                    inChess[i] = inChess[i + 1];
                }
                inChess[roomNum[0]] = "0";
                roomNum[0]--;
            }
            if (status.startsWith("2")) {
                messageTransfer("/giveup2 " + clientName);
            }
        }
        synchronized (clientDataHash) {
            clientDataHash.remove(clientSocket);
        }
        synchronized (clientNameHash) {
            clientNameHash.remove(clientSocket);
        }
        server.statusLabel.setText("当前连接数:" + clientDataHash.size());
        try {
            clientSocket.close();
        } catch (IOException exx) {

        }
        isClientClosed = true;
    }


    public void run() {
        DataInputStream inData;
        synchronized (clientDataHash) {
            server.statusLabel.setText("当前连接数:" + clientDataHash.size());
        }
        try {
            inData = new DataInputStream(clientSocket.getInputStream());
            firstCome();
            while (true) {
                String message = inData.readUTF();
                messageTransfer(message);
            }
        } catch (IOException esx) {
        } finally {
            if (!isClientClosed) {
                clientClose();
            }
        }
    }
}

class MessageServer extends Panel {
    TextArea messageBoard = new TextArea();
    Label statusLabel = new Label("当前连接数:");

    MessageServer() {
        setLayout(null);
        setBounds(0, 0, 300, 330);
        setBackground(Color.pink);
        statusLabel.setBounds(7, 25, 150, 40);
        messageBoard.setBounds(7, 60, 292, 256);
        add(messageBoard);
        add(statusLabel);
    }
}


public class MultiClientServer extends Frame implements ActionListener {

    Button messageClearButton = new Button("清除显示");
    Button serverStatusButton = new Button("服务器状态");
    Button serverOffButton = new Button("关闭服务器");
    Panel buttonPanel = new Panel();

    @Override
    public void setLayout(LayoutManager mgr) {
        super.setLayout(null);
    }

    @Override
    public void setBackground(Color bgColor) {
        super.setBackground(Color.white);
    }

    MessageServer server = new MessageServer();
    ServerSocket serverSocket;
    Hashtable<Object,Object> clientDataHash = new Hashtable<Object,Object>(50);
    Hashtable<Object,String> clientNameHash = new Hashtable<Object,String>(50);
    Hashtable<Object,String> clientColorHash = new Hashtable<Object,String>(50);
    String[] inChess = new String[50];
    int[] roomNum = new int[1];

    MultiClientServer() {
        super("五子棋服务器");
        for (int i = 0; i < 50; i++)
            inChess[i] = "0";
        setBackground(Color.pink);

        setLayout(null);
        buttonPanel.setBounds(0, 330, 300, 50);
        buttonPanel.setLayout(new FlowLayout());
        buttonPanel.setBackground(Color.pink);
        messageClearButton.setSize(20, 25);
        buttonPanel.add(messageClearButton);
        messageClearButton.addActionListener(this);
        serverStatusButton.setSize(75, 25);
        buttonPanel.add(serverStatusButton);
        serverStatusButton.addActionListener(this);
        serverOffButton.setSize(75, 25);
        buttonPanel.add(serverOffButton);
        serverOffButton.addActionListener(this);

        add(server, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
        pack();
        setLocation(534, 230);
        setVisible(true);
        setSize(300, 380);
        setResizable(false);
        validate();
        try {
            makeMessageServer(5024, server);
        } catch (Exception e) {
            System.out.println("e");
        }
    }

    public void makeMessageServer(int port, MessageServer server) throws IOException {
        Socket clientSocket;
        this.server = server;

        try {
            serverSocket = new ServerSocket(port);
            server.messageBoard.setText("服务器:" + serverSocket.getInetAddress().getLocalHost() + ":" + serverSocket.getLocalPort() + "\n");

            while (true) {
                clientSocket = serverSocket.accept();
                server.messageBoard.append("用户连接:" + clientSocket + "\n");

                DataOutputStream outData = new DataOutputStream(clientSocket.getOutputStream());
                clientColorHash.put((Object)clientSocket, "-1");
                clientDataHash.put((Object)clientSocket, outData);
                ServerThread thread = new ServerThread(clientSocket, clientDataHash, clientNameHash, roomNum, inChess, server, clientColorHash);
                thread.start();
            }
        } catch (IOException ex) {
            System.out.println("已经有服务器在运行. \n");
        }


    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == messageClearButton) {
            server.messageBoard.setText("");
        }
        if (e.getSource() == serverStatusButton) {
            try {
                server.messageBoard.append("服务器信息:" + serverSocket.getInetAddress().getLocalHost() + ":" + serverSocket.getLocalPort() + "\n");
            } catch (Exception ee) {
                System.out.println("serverSocket.getInetAddress().getLocalHost() error \n");
            }
        }
        if (e.getSource() == serverOffButton) {
            System.exit(0);
        }
    }

    public static void main(String args[]) {
        MultiClientServer chessServer = new MultiClientServer();
    }
}
