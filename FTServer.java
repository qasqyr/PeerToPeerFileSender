import com.sun.security.ntlm.Server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class FTServer {
    private static Map <String, Set<CustomFile>> clientFiles = new ConcurrentHashMap<>();
    private static Map <String, PeerValue> clientsScore = new ConcurrentHashMap<>();

    public static void removeFiles(String IP, int port) {
        for (Map.Entry<String, Set<CustomFile>> entry : clientFiles.entrySet()) {
            Set<CustomFile> temp = new HashSet<>(entry.getValue());
            for (CustomFile file : temp) {
                if (file.getPortNumber() == port && file.getIpAddress().equals(IP)) {
                    System.out.println(entry.getValue().remove(file));
                }
            }
            if (entry.getValue().size() == 0) {
                clientFiles.remove(entry.getKey());
            }
        }
    }

    public static class RunnableServer implements Runnable {
        protected Socket clientSocket;
        protected String peerIP = null;
        protected int peerPort = 0;

        public RunnableServer(Socket clientSocket) {
            this.clientSocket = clientSocket;
        }

        public void run() {
            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                ObjectInputStream os = new ObjectInputStream(clientSocket.getInputStream());

                String clienMessage = in.readLine();
                if(clienMessage.equals("HELLO")) {
                    out.println("HI");
                } else {
                    clientSocket.close();
                    return;
                }

                Set<CustomFile> temp = (HashSet<CustomFile>) os.readObject();
                if(temp == null) {
                    return;
                }

                for(CustomFile file:temp) {
                    peerIP = file.getIpAddress();
                    peerPort = file.getPortNumber();

                    if (clientFiles.containsKey(file.getFileName())) {
                        clientFiles.get(file.getFileName()).add(file);
                    } else {
                        Set<CustomFile> tempFiles = new HashSet<CustomFile>();
                        tempFiles.add(new CustomFile(file.getFileName(), file.getFileType(), file.getFileSize(), file.getModifiedDate(), file.getIpAddress(), file.getPortNumber()));
                        clientFiles.put(file.getFileName(), tempFiles);
                    }
                }

                clientsScore.put(peerIP + ":" + peerPort, new PeerValue());

                for(;;) {
                    clienMessage = in.readLine();

                    if(clienMessage == null) {
                        removeFiles(peerIP, peerPort);
                        return;
                    }

                    if(clienMessage.equals("BYE")) {
                        removeFiles(peerIP, peerPort);
                        return;
                    }

                    if(clienMessage.startsWith("SEARCH: ")){
                        String parts[] = clienMessage.split(" ", 2);
                        String res = "";
                        boolean found = false;
                        for(Map.Entry <String, Set<CustomFile> > f : clientFiles.entrySet()) {
                            if(f.getKey().contains(parts[1])) {
                                temp = clientFiles.get(f.getKey());
                                for(CustomFile file : temp) {
                                    if(file.getPortNumber() != peerPort || !file.getIpAddress().equals(peerIP)) {
                                        found = true;
                                        res += f.getKey().toString() + " " + file + " " + clientsScore.get(file.getIpAddress() + ":" + file.getPortNumber()) + "%]";
                                    }
                                }
                                break;
                            }
                        }
                        if(found)
                            out.println("FOUND: " + res);
                        else
                            out.println("NOT FOUND");
                    }

                    if(clienMessage.startsWith("SCORE of ")){
                        String[] parts  = clienMessage.split(" ");
                        clientsScore.get(parts[2]).add(Integer.valueOf(parts[4]));
                    }
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }

        public static void main(String[] args) throws IOException {
            ServerSocket ftSocket = new ServerSocket(12345);

            while(true) {
                Socket clientSocket = ftSocket.accept();
                new Thread(new RunnableServer(clientSocket)).start();
            }
        }
    }
}
