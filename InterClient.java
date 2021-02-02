package interruptible;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class InterClient {
    private static final InetSocketAddress adres = new InetSocketAddress("HP",12345);
    public static void main(String[] args) throws IOException {
        Socket client = new Socket();
        new Logowanie(client,adres);
    }
}

class Logowanie {
    private Socket socket = null;
    private InetSocketAddress address = null;
    private RamkaLogowania rl = null;

    public Logowanie(Socket socket,InetSocketAddress address) throws IOException {
        this.socket = socket;
        this.address = address;
        this.rl = new RamkaLogowania(socket,address);

        rl.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        rl.setTitle("Logowanie");
        rl.setVisible(true);
    }
}

class RamkaLogowania extends JFrame {
    private Socket socket = null;
    private InetSocketAddress address = null;

    private JLabel label = new JLabel("Id: ");
    private JTextField textField = new JTextField(10);
    private JButton button = new JButton("connecting ...");
    private String id = null;
    private Rameczka r = null;

    private int portIdNumber = 0; // port na którym nasłuchuje serwer odpowiedzialny za aktualizacje loginów

    public RamkaLogowania(Socket socket,InetSocketAddress address) throws IOException {
        this.socket = socket; this.address = address;

        FlowLayout layout = new FlowLayout(FlowLayout.CENTER);
        this.setLayout(layout);

        this.add(label);
        this.add(textField);
        this.add(button);

        Thread waitingFromActualizationUsersIDLogin = new Thread(new Runnable() {
            @Override
            public void run() {
                try {

                    ServerSocket serverSocket = new ServerSocket(portIdNumber); // 22222 było portIdNumber
                    Socket socketID = serverSocket.accept();
                    while(true) {
                        Scanner idLog = new Scanner(socketID.getInputStream());
                        String log = idLog.nextLine();
                        r.getDefaultListModel().addElement(log);
                        r.repaint();
                        //serverSocket.close();
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        Thread watingOnTheMessage = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Scanner scanner = new Scanner(socket.getInputStream());

                    while(true) {
                        Thread.sleep(250);
                        String text = scanner.nextLine();
                        r.getTextArea().append(text);
                        if(!scanner.hasNext()){ break; }
                    }

                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        button.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                id = textField.getText();
                try {

                    socket.connect(address);
                    portIdNumber = (socket.getLocalPort() + 23);
                    PrintStream printStream = new PrintStream(socket.getOutputStream());
                    printStream.println(id);

                    ObjectInputStream objectIn = new ObjectInputStream(socket.getInputStream());

                    BasicClass basicClass = (BasicClass) objectIn.readObject();
                    r = basicClass.getRamka();
                    r.setVisible(true);

                    watingOnTheMessage.start(); //wystartowanie wątku czekającego na wiadomość od zdalnego usera ////////////////////////////
                    waitingFromActualizationUsersIDLogin.start(); //wystartowanie wątku odpowiedzialnego za aktualizacje loginów

                    r.getButton().addActionListener(
                            (ActionEvent event)->{

                                try {
                                    //pobranie danych do wysyłki i wyslanie na serwer w celu przekierowania
                                    PrintWriter pr = new PrintWriter(socket.getOutputStream());
                                    String text = r.getTextField().getText();
                                    pr.write(text+"\n");
                                    pr.flush();
                                    //wybór do kogo wysłąć dane i przekazanie do serwera w celu przekierowaia do własciwego odbiorcy
                                    JList<String> lista = r.getshowViewLists();
                                    String choosing = lista.getSelectedValue();
                                    pr.write(choosing+"\n");
                                    pr.flush();
                                    Thread.sleep(1000);
                                } catch (IOException | InterruptedException ex) {
                                    ex.printStackTrace();
                                }

                            }
                    );

                    r.addWindowListener(new WindowAdapter() {
                        @Override
                        public void windowClosing(WindowEvent e) {
                            System.out.println("wysyłam do serwera gniazdo socket które ma być usunięte z mapy i serwera po zamknięciu okna = "+socket);
                            //basicClass.getList().remove(0);
                        }
                    });

                }catch(IOException | ClassNotFoundException excIO){}
            }
        });

        this.pack();
    }
}
