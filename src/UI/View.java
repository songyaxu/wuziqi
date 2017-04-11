package UI;


import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import static java.lang.System.exit;

/**
 * Created by yaxuSong on 2015/10/21.
 */

public class View {
    public JFrame frame=new JFrame("五子棋");
    public String clientName;

    public String[] roomName;
    public JLabel labeltitle=new JLabel("选择要加入的房间号:");
    public JButton okButton=new JButton("加入");
    public TextArea roomList=new TextArea();
    public JTextField choseTextField=new JTextField(10);

    public JPanel panelLeft=new JPanel();
    public JPanel panelRight=new JPanel();
    public JPanel panelColor=new JPanel();
    public JPanel controlPanel=new JPanel();
    public JPanel infoPanel=new JPanel();
    public JPanel contorlPanel1=new JPanel();
    public JTextArea messageBox=new JTextArea();

    public JLabel jLabel=new JLabel("请选择棋子颜色:");
    public JComboBox comboBox=new JComboBox();
    public JPanel color=new JPanel();

    public  JTextField jTextField2=new JTextField();
    public  JButton but=new JButton("发送");

    public JLabel label=new JLabel("角色名称:");
    public JLabel label1=new JLabel("");

    public  JButton but1=new JButton("开始游戏");
    public JButton button=new JButton("创建房间");
    public JButton button1=new JButton("退出房间");
    public JButton button2=new JButton("悔     棋");
    public JButton button3=new JButton("加入游戏");

    public View(){
        init();
        myEvent();
    }

    void init(){
        clientName=JOptionPane.showInputDialog(frame,"请你的游戏角色起一个名字吧！","提示",2);
        if(clientName==null||clientName.equals(""))
        {
            if(clientName.equals(""))
                JOptionPane.showMessageDialog(frame,"名字不能为空！");
            exit(0);
        }
        else
        {
            label1.setText(clientName);
        }
        frame.setResizable(false);
        panelLeft.setBounds(0, 0, 527, 547);
        panelRight.setBounds(527, 0, 271, 547);
        frame.setLocation(279,139);
        frame.setSize(798,547);
        frame.setLayout(null);
        panelLeft.setBackground(new Color(214,214,3));
        frame.getContentPane().add(panelRight);
        frame.getContentPane().add(panelLeft);
        panelLeft.setLayout(null);
        panelRight.setLayout(null);
        panelColor.setBounds(0, 0, 279, 160);
        controlPanel.setBackground(Color.pink);
        controlPanel.setBounds(0, 280, 279, 80);
        panelRight.add(controlPanel);
        panelRight.add(panelColor);
        infoPanel.setBackground(Color.pink);
        infoPanel.setBounds(0,160,279,120);
        contorlPanel1.setBackground(Color.orange);
        contorlPanel1.setBounds(0,340,279,547-360);
        panelRight.add(contorlPanel1);
        panelRight.add(infoPanel);
        frame.add(panelLeft);

        jLabel.setBounds(20,10,175,20);
        comboBox.setBounds(50,50,175,29);
        panelColor.setLayout(null);
        color.setBounds(50,110,175,29);
        color.setBackground(Color.black);
        panelColor.add(color);
        panelColor.add(jLabel);

        controlPanel.setLayout(null);
        but.setBounds(210,23,60,40);
        jTextField2.setBounds(0,20,209,48);
        Font f=new Font("黑体",1,30);
        jTextField2.setFont(f);
        controlPanel.add(jTextField2);
        controlPanel.add(but);

        infoPanel.setLayout(null);
        label.setBounds(5,0,88,29);
        Font font=new Font("宋体",5,20);
        label1.setFont(font);
        label1.setBounds(60,18,190,41);
        messageBox.setBounds(0,55,265,65);
        infoPanel.add(messageBox);
        infoPanel.add(label);
        infoPanel.add(label1);

        contorlPanel1.setLayout(null);
        but1.setBounds(88,90,88,29);
        button.setBounds(30,55,88,29);
        button3.setBounds(148,55,88,29);
        button2.setBounds(30,125,88,29);
        button1.setBounds(148,125,88,29);
        button1.setEnabled(false);
        button2.setEnabled(false);
        but.setEnabled(false);
        but1.setEnabled(false);

        contorlPanel1.add(but1);
        contorlPanel1.add(button);
        contorlPanel1.add(button1);
        contorlPanel1.add(button2);
        contorlPanel1.add(button3);
        comboBox.insertItemAt("黑    色",0);
        comboBox.insertItemAt("白    色",1);
        comboBox.insertItemAt("红    色",2);
        comboBox.insertItemAt("绿    色",3);
        comboBox.insertItemAt("蓝    色",4);
        comboBox.insertItemAt("粉    色",5);
        comboBox.insertItemAt("黄    色",6);
        comboBox.insertItemAt("橙    色",7);
        comboBox.setSelectedIndex(0);
        panelColor.add(comboBox);
        frame.setVisible(true);
    }
    void myEvent(){

        comboBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if((String)e.getItem()==("黑    色"))
                    color.setBackground(Color.black);
                if((String)e.getItem()==("白    色"))
                    color.setBackground(Color.white);
                if((String)e.getItem()==("红    色"))
                    color.setBackground(Color.red);
                if((String)e.getItem()==("绿    色"))
                    color.setBackground(Color.green);
                if((String)e.getItem()==("蓝    色"))
                    color.setBackground(Color.blue);
                if((String)e.getItem()==("粉    色"))
                    color.setBackground(Color.pink);
                if((String)e.getItem()==("黄    色"))
                    color.setBackground(Color.yellow);
                if((String)e.getItem()==("橙    色"))
                    color.setBackground(Color.orange);
            }
        });
    }
}
