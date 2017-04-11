package Socket;

import UI.View;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * Created by yaxuSong on 2015/10/21.
 */
class chessThread extends Thread {
    MultiClient client;

    chessThread(MultiClient client) {
        this.client = client;
    }

    public void sendMessage(String sndMessage) {
        try {
            client.out.writeUTF(sndMessage);
            System.out.println("发送成功！");
        } catch (Exception ea) {
            System.out.println("chessThread.sendMessage:" + ea);
            System.out.println("发送失败！");
        }
    }


    public void acceptMessage(String recMessage) {
        JDialog dlg = new JDialog(client.frame, "房间列表");
        if (recMessage.startsWith("/listroom")) {
            client.roomName = null;
            client.roomList.setText("");
            client.roomName = recMessage.split(" ");
            if (client.roomName.length >= 2) {
                dlg.setSize(390, 270);
                dlg.setResizable(false);
                dlg.setLayout(new BorderLayout());
                JPanel downPanel = new JPanel();
                downPanel.setLayout(new FlowLayout());
                downPanel.setSize(390, 100);
                downPanel.add(client.labeltitle);
                downPanel.add(client.choseTextField);
                downPanel.add(client.okButton);
                client.roomList.setSize(380, 130);
                String[] sName = recMessage.split(" ");
                client.roomName = new String[sName.length - 1];
                for (int i = 0; i < client.roomName.length; i++) {
                    if (sName[i + 1].startsWith("1"))
                        client.roomName[i] = sName[i + 1].substring(2) + "的房间" + "-等待中";
                    if (sName[i + 1].startsWith(("2"))) {
                        String[] getTemp1 = sName[i + 1].split("-");
                        client.roomName[i] = getTemp1[1] + "与" + getTemp1[2] + "的房间" + "-游戏中";
                    }
                    client.roomList.append("[" + (i + 1) + "] " +
                            "" + client.roomName[i] + "\r\n");
                }
                dlg.add(client.roomList, BorderLayout.NORTH);
                dlg.add(downPanel, BorderLayout.SOUTH);
                dlg.setLocation(470, 305);
                dlg.setVisible(true);
            } else {
                JOptionPane.showMessageDialog(client.frame, "没有任何房间！", "提示", 2);
            }

        }
        if (recMessage.startsWith("/joinok")) {
            System.out.println("加入成功！");
            dlg.setVisible(false);
            String[] getPeerName = recMessage.split(" ");
            client.chessServerName = getPeerName[1];
            JOptionPane.showMessageDialog(client.frame, client.chessServerName + "加入成功！", "提示", 2);
            client.but.setEnabled(true);
            client.but1.setEnabled(true);
            client.button.setEnabled(false);
            client.button1.setEnabled(true);
            client.button3.setEnabled(false);
            client.messageBox.setText("加入成功！请选择棋子颜色并点击“开始游戏”！");
            client.isOnChess = true;
        }
        if (recMessage.startsWith("/talk")) {
            String[] getInfo = recMessage.split(" ");
            String message = "";
            for (int i = 2; i < getInfo.length; i++) {
                message += getInfo[i];
            }
            client.messageBox.setText(getInfo[1] + ":" + message);
        }
        if (recMessage.startsWith("/youwin1")) {
            client.isYourTurn = false;
            client.isOnStart = false;
            client.button.setEnabled(true);
            client.button1.setEnabled(false);
            client.button3.setEnabled(true);
            client.but1.setEnabled(false);
            client.messageBox.setText("");
            client.isOnChess = false;
            JOptionPane.showMessageDialog(client.frame, "对方落荒而逃！您获胜了！", "提示", 1);
            for (int i1 = 0; i1 < 15; i1++)
                for (int j1 = 0; j1 < 15; j1++) {
                    client.chessBoard[i1][j1] = -1;
                }
            client.canvas.paint(client.canvas.getGraphics());
            client.canvas.update(client.canvas.getGraphics());
        }
        if (recMessage.equals("/wait")) {
            System.out.println("wait");
            JOptionPane.showMessageDialog(client.frame, "请等待对方开始游戏！", "提示", 2);
        }
        if (recMessage.startsWith("/ready")) {
            System.out.println("ready");
            client.peerColor = recMessage.substring(7);
            if (Integer.valueOf(client.peerColor) == client.comboBox.getSelectedIndex())
                client.peerColor = String.valueOf((client.comboBox.getSelectedIndex() + 1) % 8);
            JOptionPane.showMessageDialog(client.frame, "等待对方先下棋！", "提示", 2);
            System.out.println(recMessage);
        }
        if (recMessage.startsWith("/youfirst")) {
            System.out.println("first");
            client.peerColor = recMessage.substring(10);
            if (Integer.valueOf(client.peerColor) == client.comboBox.getSelectedIndex())
                client.peerColor = String.valueOf((client.comboBox.getSelectedIndex() + 1) % 8);
            JOptionPane.showMessageDialog(client.frame, "请下棋！", "提示", 2);
            client.isYourTurn = true;
        }
        if (recMessage.startsWith("/reject")) {
            JOptionPane.showMessageDialog(client.frame, recMessage.substring(8), "提示", 0);
        }
        if (recMessage.startsWith("/chess")) {
            String[] getInfo = recMessage.split(" ");
            int x = Integer.parseInt(getInfo[2]);
            int y = Integer.parseInt(getInfo[3]);
            client.currentChessman.x = x;
            client.currentChessman.y = y;
            client.keepPoint(x, y, Integer.valueOf(client.peerColor));
            client.canvas.paint(client.canvas.getGraphics());
            client.canvas.update(client.canvas.getGraphics());
            client.isYourTurn = true;
            client.button2.setEnabled(false);
        }
        if (recMessage.equals("/youfail")) {
            client.comboBox.setEnabled(true);
            client.but.setEnabled(false);
            client.button3.setEnabled(true);
            client.button.setEnabled(true);
            client.button1.setEnabled(false);
            client.button2.setEnabled(false);
            for (int i1 = 0; i1 < 15; i1++)
                for (int j1 = 0; j1 < 15; j1++) {
                    client.chessBoard[i1][j1] = -1;
                }
            client.canvas.paint(client.canvas.getGraphics());
            client.canvas.update(client.canvas.getGraphics());
            client.isYourTurn = false;
            client.isOnChess = false;
            client.isOnStart = false;
            JOptionPane.showMessageDialog(client.frame, "很糟糕你失败了！", "提示", 1);
        }
        if(recMessage.startsWith("/takeback ")){
            int n=JOptionPane.showConfirmDialog(client.frame,"对方请求悔棋！","提示",JOptionPane.YES_NO_OPTION);
            if(n==0)
            {
                sendMessage("/takeback2 "+client.chessServerName);
                client.isYourTurn=false;
                client.chessBoard[(client.currentChessman.x-18)/35][(client.currentChessman.y-18)/35]=-1;
                client.canvas.paint(client.canvas.getGraphics());
                client.canvas.update(client.canvas.getGraphics());
            }
            if(n==1)
                sendMessage("/takeback1 "+client.chessServerName);
        }
        if(recMessage.equals("/takeback1")){
            JOptionPane.showMessageDialog(client.frame,"对方拒绝了你的请求！","提示",1);
        }
        if (recMessage.equals("/takeback2")){
            JOptionPane.showMessageDialog(client.frame,"对方同意了你的请求！","提示",2);
            client.isYourTurn=true;
            client.chessBoard[(client.currentChessman.x-18)/35][(client.currentChessman.y-18)/35]=-1;
            client.canvas.paint(client.canvas.getGraphics());
            client.canvas.update(client.canvas.getGraphics());
        }
    }

    public void run() {
        String message = "";
        try {
            while (true) {
                message = client.in.readUTF();
                acceptMessage(message);
            }
        } catch (IOException es) {
        }
    }

}

public class MultiClient extends View implements ActionListener, KeyListener, MouseListener {
    Socket chatSocket;
    DataInputStream in;
    DataOutputStream out;
    String chessClientName = null;
    String chessServerName = null;
    String host = "127.0.0.1";
    int port = 5024;
    chessThread thread;
    public Canvas canvas = new MyCanvas();
    public int[][] chessBoard = new int[15][15];
    public Point currentChessman = new Point();


    boolean isOnChess = false;
    boolean isOnStart = false;
    String peerColor = "";
    boolean isYourTurn = false;  //你的回合

    public MultiClient() throws Exception {
        chessClientName = this.label1.getText();
        for (int i = 0; i < 15; i++) {
            for (int j = 0; j < 15; j++) {
                chessBoard[i][j] = -1;
            }
        }
        canvas.setSize(527, 547);
        panelLeft.add(canvas);
        but.addActionListener(this);
        but1.addActionListener(this);
        button.addActionListener(this);
        button1.addActionListener(this);
        button2.addActionListener(this);
        button3.addActionListener(this);
        okButton.addActionListener(this);
        jTextField2.addKeyListener(this);
        canvas.addMouseListener(this);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }

            @Override
            public void windowActivated(WindowEvent e) {

            }
        });
        if (connectServer(host, port)) {
            JOptionPane.showMessageDialog(frame, "连接服务器成功！", "提示", 2);
        } else {
            JOptionPane.showMessageDialog(frame, "连接服务器失败！", "提示", 0);
        }

    }

    public Color getColor(String color) {
        if (color.equals("0"))
            return Color.black;
        else if (color.equals("1"))
            return Color.white;
        else if (color.equals("2"))
            return Color.red;
        else if (color.equals("3"))
            return Color.green;
        else if (color.equals("4"))
            return Color.blue;
        else if (color.equals("5"))
            return Color.pink;
        else if (color.equals("6"))
            return Color.yellow;
        else
            return Color.orange;
    }

    public boolean checkwin() {
        int color = comboBox.getSelectedIndex();
        for (int i = 0; i < 15; i++)
            for (int j = 0; j < 11; j++)
                if (chessBoard[i][j] == color && chessBoard[i][j + 1] == color && chessBoard[i][j + 2] == color && chessBoard[i][j + 3] == color && chessBoard[i][j + 4] == color) {
                    thread.sendMessage("/youfail " + chessServerName);
                    JOptionPane.showMessageDialog(frame, "恭喜你获胜了！", "提示", 1);
                    comboBox.setEnabled(true);
                    but.setEnabled(false);
                    button3.setEnabled(true);
                    button.setEnabled(true);
                    button1.setEnabled(false);
                    button2.setEnabled(false);
                    for (int i1 = 0; i1 < 15; i1++)
                        for (int j1 = 0; j1 < 15; j1++) {
                            chessBoard[i1][j1] = -1;
                        }
                    canvas.paint(canvas.getGraphics());
                    canvas.update(canvas.getGraphics());
                    isYourTurn = false;
                    isOnChess = false;
                    isOnStart = false;
                    return true;
                }
        for (int i = 0; i < 11; i++)
            for (int j = 0; j < 15; j++)
                if (chessBoard[i][j] == color && chessBoard[i + 1][j] == color && chessBoard[i + 2][j] == color && chessBoard[i + 3][j] == color && chessBoard[i + 4][j] == color) {
                    thread.sendMessage("/youfail " + chessServerName);
                    JOptionPane.showMessageDialog(frame, "恭喜你获胜了！", "提示", 1);
                    comboBox.setEnabled(true);
                    but.setEnabled(false);
                    button3.setEnabled(true);
                    button.setEnabled(true);
                    button1.setEnabled(false);
                    button2.setEnabled(false);
                    for (int i1 = 0; i1 < 15; i1++)
                        for (int j1 = 0; j1 < 15; j1++) {
                            chessBoard[i1][j1] = -1;
                        }
                    canvas.paint(canvas.getGraphics());
                    canvas.update(canvas.getGraphics());
                    isYourTurn = false;
                    isOnChess = false;
                    isOnStart = false;
                    return true;
                }
        for (int i = 0; i < 11; i++)
            for (int j = 0; j < 11; j++)
                if (chessBoard[i][j] == color && chessBoard[i + 1][j + 1] == color && chessBoard[i + 2][j + 2] == color && chessBoard[i + 3][j + 3] == color && chessBoard[i + 4][j + 4] == color) {
                    thread.sendMessage("/youfail " + chessServerName);
                    JOptionPane.showMessageDialog(frame, "恭喜你获胜了！", "提示", 1);
                    comboBox.setEnabled(true);
                    but.setEnabled(false);
                    button3.setEnabled(true);
                    button.setEnabled(true);
                    button1.setEnabled(false);
                    button2.setEnabled(false);
                    for (int i1 = 0; i1 < 15; i1++)
                        for (int j1 = 0; j1 < 15; j1++) {
                            chessBoard[i1][j1] = -1;
                        }
                    canvas.paint(canvas.getGraphics());
                    canvas.update(canvas.getGraphics());
                    isYourTurn = false;
                    isOnChess = false;
                    isOnStart = false;
                    return true;
                }
        for (int i = 4; i < 15; i++)
            for (int j = 0; j < 11; j++)
                if (chessBoard[i][j] == color && chessBoard[i - 1][j + 1] == color && chessBoard[i - 2][j + 2] == color && chessBoard[i - 3][j + 3] == color && chessBoard[i - 4][j + 4] == color) {
                    thread.sendMessage("/youfail " + chessServerName);
                    JOptionPane.showMessageDialog(frame, "恭喜你获胜了！", "提示", 1);
                    comboBox.setEnabled(true);
                    but.setEnabled(false);
                    button3.setEnabled(true);
                    button.setEnabled(true);
                    button1.setEnabled(false);
                    button2.setEnabled(false);
                    for (int i1 = 0; i1 < 15; i1++)
                        for (int j1 = 0; j1 < 15; j1++) {
                            chessBoard[i1][j1] = -1;
                        }
                    canvas.paint(canvas.getGraphics());
                    canvas.update(canvas.getGraphics());
                    isYourTurn = false;
                    isOnChess = false;
                    isOnStart = false;
                    return true;
                }
        return false;
    }

    public void keepPoint(int x, int y, int color) {
        int tempX = ((x - 18) / 35);
        int tempY = ((y - 18) / 35);
        chessBoard[tempX][tempY] = color;
    }

    public boolean connectServer(String serverIP, int serverPort) throws Exception {
        try {
            chatSocket = new Socket(serverIP, serverPort);
            in = new DataInputStream(chatSocket.getInputStream());
            out = new DataOutputStream(chatSocket.getOutputStream());
            chessThread thread = new chessThread(this);
            thread.start();
            this.thread = thread;
            thread.sendMessage("/sendname " + chessClientName);
            return true;
        } catch (IOException ex) {
            messageBox.setText("连接服务器失败！");
        }
        return false;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == button) {
            JOptionPane.showMessageDialog(frame, "创建成功等待加入！", "提示", 2);
            messageBox.setText("创建成功等待加入！");
            but.setEnabled(false);
            but1.setEnabled(false);
            button.setEnabled(false);
            button1.setEnabled(true);
            button2.setEnabled(false);
            thread.sendMessage("/creategame " + chessClientName);
        }
        if (e.getSource() == button3) {
            thread.sendMessage("/listroom");
        }
        if (e.getSource() == button1) {
            if (isOnChess) {
                thread.sendMessage("/giveup2 " + chessClientName);
                button.setEnabled(true);
                button1.setEnabled(false);
                button3.setEnabled(true);
                but.setEnabled(false);
                but1.setEnabled(false);
                messageBox.setText("");
                for (int i1 = 0; i1 < 15; i1++)
                    for (int j1 = 0; j1 < 15; j1++) {
                        chessBoard[i1][j1] = -1;
                    }
                canvas.paint(canvas.getGraphics());
                canvas.update(canvas.getGraphics());
                isYourTurn = false;
                isOnChess = false;
                isOnStart = false;
            } else {
                thread.sendMessage("/giveup1 " + chessClientName);
                button.setEnabled(true);
                button1.setEnabled(false);
                button3.setEnabled(true);
                but.setEnabled(false);
                but1.setEnabled(false);
                messageBox.setText("");
                isOnChess = false;
                for (int i1 = 0; i1 < 15; i1++)
                    for (int j1 = 0; j1 < 15; j1++) {
                        chessBoard[i1][j1] = -1;
                    }
                canvas.paint(canvas.getGraphics());
                canvas.update(canvas.getGraphics());
                isYourTurn = false;
                isOnStart = false;
            }
        }
        if (e.getSource() == okButton) {
            int indexI = Integer.valueOf(choseTextField.getText());
            if (choseTextField.getText().equals("")) {
                JOptionPane.showMessageDialog(frame, "房间号不能为空！", "提示", 0);
            }
            if (indexI <= 0 || indexI > roomName.length) {
                JOptionPane.showMessageDialog(frame, "请输入正确的房间编号！", "提示", 0);
            } else if (1 <= indexI && indexI <= 25) {
                if (roomName[indexI - 1].endsWith("游戏中")) {
                    JOptionPane.showMessageDialog(frame, "游戏中不能加入！", "提示", 2);
                } else {
                    String peerName = roomName[indexI - 1].substring(0, roomName[indexI - 1].length() - 7);
                    if (peerName.equals(chessClientName))
                        JOptionPane.showMessageDialog(frame, "自己的房间无法加入！", "提示", 0);
                    else {
                        thread.sendMessage("/joingame " + peerName + " " + chessClientName);
                    }
                }
            }
        }
        if (e.getSource() == but) {
            if (!jTextField2.getText().equals("")) {
                thread.sendMessage("/talk " + chessServerName + " " + jTextField2.getText());
                jTextField2.setText("");
                messageBox.setText("发送消息成功！");
            }
        }
        if (e.getSource() == but1) {
            isOnStart = true;
            comboBox.setEnabled(false);
            but1.setEnabled(false);
            thread.sendMessage("/startgame " + chessClientName + " " + comboBox.getSelectedIndex());
        }
        if(e.getSource()==button2){
            thread.sendMessage("/takeback "+chessServerName);
        }
    }

    public int roundNum(double x) {
        double temp = Math.abs(x - 18);
        if (temp / 35.00 > 0.5)
            return 35 * (((int) temp / 35) + 1) + 18;
        else
            return 35 * ((int) temp / 35) + 18;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (isYourTurn) {
            if (chessBoard[(roundNum(e.getX() - 11) - 18) / 35][(roundNum(e.getY() - 11) - 18) / 35] == -1) {
                currentChessman.x = roundNum(e.getX() - 11);
                currentChessman.y = roundNum(e.getY() - 11);
                System.out.println(currentChessman);
                keepPoint(roundNum(e.getX() - 11), roundNum(e.getY() - 11), comboBox.getSelectedIndex());
                canvas.paint(canvas.getGraphics());
                canvas.update(canvas.getGraphics());
                button2.setEnabled(true);
                if (!checkwin()) {
                    isYourTurn = false;
                    thread.sendMessage("/chess " + chessServerName + " " + roundNum(e.getX() - 11) + " " + roundNum(e.getY() - 11));
                }
            }
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {

    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ENTER && but.isEnabled()) {
            if (!jTextField2.getText().equals("")) {
                thread.sendMessage("/talk " + chessServerName + " " + jTextField2.getText());
                jTextField2.setText("");
                messageBox.setText("发送消息成功！");
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {

    }

    public class MyCanvas extends Canvas {
        public void paint(Graphics g) {
            int gap = 18;
            for (int i = 0; i < 15; i++) {
                g.drawLine(18, gap, 508, gap);
                g.drawLine(gap, 18, gap, 508);
                gap += 35;
            }
            for (int i = 0; i < 15; i++)
                for (int j = 0; j < 15; j++)
                    if (chessBoard[i][j] != -1) {
                        int x = i * 35 + 18;
                        int y = j * 35 + 18;
                        g.setColor(getColor(String.valueOf(chessBoard[i][j])));
                        if (currentChessman.getX() == x && currentChessman.getY() == y)
                            g.fillRect(currentChessman.x - 11, currentChessman.y - 11
                                    , 22, 22);
                        else
                            g.fillOval(x - 11, y - 11, 22, 22);
                    }
        }
    }
}
