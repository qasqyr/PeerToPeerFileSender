import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.*;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTextField;


public class ExampleGUI extends JFrame implements ActionListener{

    private static Set<CustomFile> clientFiles = new HashSet<CustomFile>();
    private static Socket clientSocket;
    private static ServerSocket peerSocket;
    private static PrintWriter ftOut;

    private JButton search;  //Buttons
    private JButton dload;
    private JButton close;

    private JList jl;   // List that will show found files
    private JLabel label; //Label "File Name
    private JTextField tf,tf2; // Two textfields: one is for typing a file name, the other is just to show the selected file
    DefaultListModel listModel; // Used to select items in the list of found files

    String str[]={"Info1","Info2","Info3","Info4","Info5"}; // Files information

    public ExampleGUI(){
        super("Example GUI");
        setLayout(null);
        setSize(500,600);

        label=new JLabel("File name:");
        label.setBounds(50,50, 80,20);
        add(label);

        tf=new JTextField();
        tf.setBounds(130,50, 220,20);
        add(tf);

        search=new JButton("Search");
        search.setBounds(360,50,80,20);
        search.addActionListener(this);
        add(search);

        listModel = new DefaultListModel();
        jl=new JList(listModel);

        JScrollPane listScroller = new JScrollPane(jl);
        listScroller.setBounds(50, 80,300,300);

        add(listScroller);

        dload=new JButton("Download");
        dload.setBounds(200,400,130,20);
        dload.addActionListener(this);
        add(dload);

        tf2=new JTextField();
        tf2.setBounds(200,430,130,20);
        add(tf2);

        close=new JButton("Close");
        close.setBounds(360,470,80,20);
        close.addActionListener(this);
        add(close);

        setVisible(true);
    }
    public void actionPerformed(ActionEvent e){
        if(e.getSource()==search){ //If search button is pressed show 25 randomly generated file info in text area 
            try {
                listModel.clear();
                if(tf.getText().length() == 0) {
                    listModel.insertElementAt("Enter something", 0);
                    return;
                }
                PrintWriter ftOut = new PrintWriter(clientSocket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                ftOut.println("SEARCH: " + tf.getText());
                String serverMessage = in.readLine();
                if(serverMessage.equals("NOT FOUND")) {
                    listModel.insertElementAt("NOT FOUND", 0);
                } else {
                    String[] parts = serverMessage.split(" ", 2);
                    parts = parts[1].split("]");
                    for(int i = 0; i < parts.length; i++) {
                        listModel.insertElementAt(parts[i], i);
                    }
                }


            } catch (IOException e1) {

            } catch (NullPointerException e2) {
                listModel.clear();
                listModel.insertElementAt("Server disconnected", 0);
            }
        }
        else if(e.getSource()==dload){   //If download button is pressed get the selected value from the list and show it in text field
            String fileName, fileType, peerIP;
            long fileSize;
            int peerPort;
            System.out.println(jl.getSelectedValue().toString());
            String[] parts = jl.getSelectedValue().toString().split(" ", 2);
            fileName = parts[0];
            parts[1] = parts[1].replaceAll("<", "");
            parts[1] = parts[1].replaceAll(">", "");
            parts = parts[1].split(", ");
            fileType = parts[0];
            fileSize = Integer.parseInt(parts[1]);
            peerIP = parts[3];
            parts = parts[4].split(" ");
            peerPort = Integer.parseInt(parts[0]);

            try {
                Socket seed = new Socket(peerIP, peerPort);
                PrintWriter seedOut = new PrintWriter(seed.getOutputStream(), true);
                BufferedReader seedIn = new BufferedReader(new InputStreamReader(seed.getInputStream()));
                seedOut.println("DOWNLOAD: " + fileName + ", " + fileType + ", " + fileSize);
                if(seedIn.readLine().equals("NO!")) {
                    tf2.setText("Peer rejected your request");
                    ftOut.println("SCORE of " + peerIP + ":" + peerPort + " : 0");
                } else {
                    byte[] fileBytes = new byte[(int) fileSize];
                    for(int i = 0; i < fileBytes.length; i++) {
                        fileBytes[i] = (byte) seedIn.read();
                    }
                    File dir = new File("downloads");
                    dir.mkdir();

                    int cnt = 0;
                    boolean exists = new File("downloads/" + fileName + "." + fileType).exists();
                    String availableName;
                    if (exists) {
                        while (new File("downloads/" + fileName + "(" + cnt + ")" + "." + fileType).exists()) {
                            cnt++;
                        }
                        availableName = "downloads/" + fileName + "(" + cnt + ")" + "." + fileType;
                    } else {
                        availableName = "downloads/" + fileName + "." + fileType;
                    }

                    BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(availableName));
                    bos.write(fileBytes, 0, fileBytes.length);
                    bos.flush();
                    tf2.setText("file saved as " + availableName);
                    ftOut.println("SCORE of " + peerIP + ":" + peerPort + " : 1");
                }

            } catch (IOException e1) {
                tf2.setText("Peer left the FTServer");
            }

            //tf2.setText(jl.getSelectedValue().toString()+" donwloaded");
        }
        else if(e.getSource()==close){ //If close button is pressed exit
            ftOut.println("BYE");
            try {
                clientSocket.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            System.exit(0);
        }

    }

    public static class RunnablePeer implements Runnable {

        private static Socket seed;

        public RunnablePeer(Socket seed) {
            this.seed = seed;
        }

        public void run() {
            try {
                BufferedReader seedIn = new BufferedReader(new InputStreamReader(seed.getInputStream()));
                PrintWriter seedOut = new PrintWriter(seed.getOutputStream(), true);
                String seedMessage = seedIn.readLine();
                if(seedMessage.startsWith("DOWNLOAD: ")) {
                    int xrand = (new Random().nextInt(99) + 1);
                    if (xrand >= 50) {
                        seedOut.println("NO!");
                        seed.close();
                        return;
                    }

                    String[] parts = seedMessage.split(", ");

                    long size = Integer.parseInt(parts[2]);
                    String type = parts[1];
                    parts = parts[0].split(" ", 2);
                    String name = parts[1];

                    BufferedInputStream fileToSeed = new BufferedInputStream(new FileInputStream("files/" + name + "." + type));
                    byte[] fileBytes = new byte[(int) size];
                    fileToSeed.read(fileBytes, 0, fileBytes.length);
                    seedOut.println("FILE: ");
                    for (byte fByte : fileBytes) {
                        seedOut.write(fByte);
                    }
                    seedOut.flush();
                }

            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    private static synchronized String getIPAddress() {
        //code from stackoverflow
        String ip = "";
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface networkInterface = interfaces.nextElement();
                if (networkInterface.isLoopback() || !networkInterface.isUp()) {
                    continue;
                }

                Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
                while(addresses.hasMoreElements()) {
                    InetAddress address = addresses.nextElement();
                    ip = address.getHostAddress();
                }
            }
        } catch (SocketException e) {
            System.out.println(e.getMessage());
        }
        return ip;
    }

    public static void listFiles(String folder) throws UnknownHostException {
        File directory = new File(folder);
        File[] contents = directory.listFiles();
        int r = new Random().nextInt(4) + 1;
        int i = 0;
        for(File f : contents) {
            if(i < r)
            {
                String name, type;

                int index = f.getName().lastIndexOf('.');
                if(index == -1) {
                    name = f.getName();
                    type = "unknown type";
                } else {
                    name = f.getName().substring(0, index);
                    type = f.getName().substring(index + 1);
                }
                clientFiles.add(new CustomFile(name, type, f.length(), new SimpleDateFormat("dd/MM/YYYY").format(new Date(f.lastModified())), getIPAddress(), peerSocket.getLocalPort()));
            }
            i++;
        }
    }

    public static void main(String[] args) throws IOException {
        peerSocket = new ServerSocket(0);
        clientSocket = new Socket("localhost", 12345);
        System.out.println(peerSocket + " " + clientSocket);
        ftOut = new PrintWriter(clientSocket.getOutputStream(), true);
        BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        ObjectOutputStream os = new ObjectOutputStream(clientSocket.getOutputStream());
        ftOut.println("HELLO");

        if(in.readLine().equals("HI")) {
            listFiles("files");
            System.out.println(clientFiles);
            os.writeObject(clientFiles);
            ExampleGUI ex=new ExampleGUI();
            ex.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Close the window if x button is pressed
            for(;;) {
                Socket seed = peerSocket.accept();
                new Thread(new RunnablePeer(seed)).start();
            }
        } else {
            return;
        }
    }
}