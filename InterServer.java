package interruptible;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.List;

public class InterServer {
    private static final InetSocketAddress adres = new InetSocketAddress("HP",12345);
    private static List<String> listsID = new ArrayList<String>(); //lista dostępnych ID users
    private static Map<Socket,String> socketMaps = new LinkedHashMap<>();
    private static Map<Socket,Integer> socketAndPortOfServerLogin = new LinkedHashMap<Socket,Integer>();

    public static void main(String[] args) throws IOException {

        ServerSocket server = new ServerSocket();
        server.bind(adres);

        while(true){
            System.out.println("waiting ...");
            Socket socket = server.accept();
            System.out.println("connection ...");


            OutputStream out = socket.getOutputStream();
            InputStream in = socket.getInputStream();
            ObjectOutputStream objectOut = new ObjectOutputStream(out);

            BasicClass basicClass = new BasicClass(listsID); //utworzenie instancji klasy głównej z serwera chatu

            String loginFromClinet = new Scanner(in).nextLine(); // downloads login from every user
            basicClass.setTitleFrameAndAddIdLoginToList(loginFromClinet); // set title frame and add new login user to list
            objectOut.writeObject(basicClass); //sending instance of BasicClass to every user client

            socketMaps.put(socket,loginFromClinet);

             //This thread has downloads dates from users and send date to the estabilished user
             Thread threadDownloadsAndSendDateOfUsers = new Thread(new Runnable() {
                 @Override
                 public void run() {
                     while(true){
                         Scanner sc = new Scanner(in);
                         if(sc.hasNextLine()) {
                             //downloads date from carently connecting user
                             String text = sc.nextLine();
                             String doKogo = sc.nextLine();

                             // send previously downloads date to estabilish user
                             for(Map.Entry<Socket,String> socketUser : socketMaps.entrySet()){
                                 if(socketUser.getValue().equals(doKogo)){
                                     try {
                                         PrintWriter printWriter = new PrintWriter(socketUser.getKey().getOutputStream());
                                         printWriter.write(text+"\n");
                                         printWriter.flush();
                                         Thread.sleep(200);
                                         //Thread.currentThread().notify();
                                         //printWriter.close();
                                     } catch (IOException | InterruptedException e) {
                                         e.printStackTrace();
                                     }
                                 }
                             }

                         }else{
                             System.out.println("nie mam żadnych danych");
                             break;
                         }
                     }
                 }
             });

            threadDownloadsAndSendDateOfUsers.start();
            String theLastMapsLogin = socketMaps.values().stream().skip(socketMaps.size() - 1).findFirst().get();

            //This method actualization all login of users
            metodaAktualizujacaLoginy(theLastMapsLogin,socketAndPortOfServerLogin,socket);
        }
    }

    public static void metodaAktualizujacaLoginy(String theLastMapElement,Map<Socket,Integer> listsOfPorts,Socket socket) throws IOException {
        Socket ss = new Socket();
        for(Map.Entry<Socket,Integer> el: listsOfPorts.entrySet()){
            PrintWriter writerLogin = new PrintWriter(el.getKey().getOutputStream());
            writerLogin.println(theLastMapElement); // loginIntoActualization
            writerLogin.flush();
            //ss.close();
        }
        ss.connect(new InetSocketAddress("localhost",(socket.getPort()+23)));
        listsOfPorts.put(ss,(socket.getPort()+23));
    }
}

class BasicClass implements Serializable{
    private static final long serialVersionUID = 1L;
    private List<String> listsID;
    private Map<Socket,String> socketMaps = null;
    private Rameczka r;
    
    public BasicClass(List<String> listaID) {
        this.listsID = listaID;
        this.socketMaps = socketMaps;
        r = new Rameczka(listsID);

        r.setSize(new Dimension(500,300));
        r.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        r.setAlwaysOnTop(true);
        r.setEnabled(true);
        r.setName("Moja Ramka");
    }

    public Rameczka getRamka() {
        return r;
    }

    //this method downoloding login from the user and set it in title na add to lists of login
    public void setTitleFrameAndAddIdLoginToList(String nn){
        r.setTitle(nn); // ustawienie Tytułu Ramki
        r.addIdLoginToList(nn); // dodanie loginu ID do Listy
    }
}

class Rameczka extends JFrame implements Serializable{
    private static final long serialVersionUID = 1L;

    private JLabel label = new JLabel("message: ");
    private JTextField textField = new JTextField(10);
    private JButton button = new JButton("send message");
    private JTextArea area = new JTextArea(10,10);

    private DefaultListModel<String> defaultListModel = new DefaultListModel<>();
    private JList<String> showViewLists = new JList<>(defaultListModel);
    private JScrollPane pane = new JScrollPane(showViewLists); //new JScrollPane(lists);

    List<String> listsID = null;

    public Rameczka(List<String> listsID){
        this.listsID = listsID;

        this.setLayout(new GridBagLayout());
        this.add(area,new LayoutGBC(0,0,3,1).setFill(LayoutGBC.BOTH).setWeight(10,10).setInsets(3));
        area.setBorder(BorderFactory.createLineBorder(Color.GREEN));
        area.setLineWrap(true);
        area.setEditable(false);
        pane.setWheelScrollingEnabled(true);
        this.add(pane,new LayoutGBC(3,0,1,1).setFill(LayoutGBC.BOTH).setWeight(10,10).setInsets(3));
        this.add(label,new LayoutGBC(0,1,1,1).setFill(LayoutGBC.HORIZONTAL).setWeight(0,0).setInsets(3));
        this.add(textField,new LayoutGBC(1,1,2,1).setFill(LayoutGBC.HORIZONTAL).setWeight(10,0).setInsets(3));
        this.add(button,new LayoutGBC(3,1,1,1).setFill(LayoutGBC.HORIZONTAL).setWeight(10,0).setInsets(3));
        pack();

    }

    public JButton getButton(){ return button; }
    public JList<String> getshowViewLists(){ return showViewLists; }
    public JTextField getTextField(){ return textField; }
    public JTextArea getTextArea(){ return area; }
    public List<String> getListsID(){ return listsID; }
    public DefaultListModel<String> getDefaultListModel(){ return defaultListModel; }

    //add user count to the users counts list
    public void addIdLoginToList(String nn){
        listsID.add(nn);
        defaultListModel.removeAllElements();

        //selection default user count
        for(String el : listsID){
            if(listsID.get(listsID.indexOf(el)).equals(nn)){
                el = "*"+el;
            }
            defaultListModel.addElement(el);
        }
    }
}

class LayoutGBC extends GridBagConstraints{

    public LayoutGBC(int gridx,int gridy) {
        this.gridx = gridx; this.gridy = gridy;
    }

    public LayoutGBC(int gridx,int gridy,int gridwidth,int gridheight){
        this.gridx = gridx; this.gridy = gridy; this.gridwidth = gridwidth; this.gridheight = gridheight;
    }

    public LayoutGBC setWeight(int weightx,int weighty){
        this.weightx = weightx; this.weighty = weighty;
        return this;
    }

    public LayoutGBC setFill(int fill){
        this.fill = fill;
        return this;
    }

    public LayoutGBC setAnchor(int anchor){
        this.anchor = anchor;
        return this;
    }

    public LayoutGBC setInsets(int distance){
        this.insets = new Insets(distance,distance,distance,distance);
        return this;
    }

    public LayoutGBC setInsets(int top,int left,int bottom,int right){
        this.insets = new Insets(top,left,bottom,right);
        return this;
    }
}
