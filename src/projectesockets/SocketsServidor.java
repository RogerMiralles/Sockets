package projectesockets;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;

public class SocketsServidor {

    public static ArrayList<Client> ClientsLlista = new ArrayList<>();

    public static void main(String[] args) {
        try {
            ClientsLlista.add(new Client(0, "123456", "Roger"));
            ServerSocket socketServidor = new ServerSocket(20002);
            System.out.println("Servidor iniciado");
            while (true) {
                Socket socket = socketServidor.accept();
                Servidor servidor = new Servidor(socket);
                servidor.start();
            }
        } catch (Exception e) {
            Logger.getLogger(SocketsServidor.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    static class Servidor extends Thread {

        Socket socket;
        DataOutputStream misVal;
        DataInputStream misRebut;

        public Servidor(Socket socket) {
            this.socket = socket;
            try {
                // misVal envia  dades al client en aquest cas el valorAleatori
                misVal = new DataOutputStream(socket.getOutputStream());
                // misRebut rep dades del client
                misRebut = new DataInputStream(socket.getInputStream());
            } catch (IOException ex) {
                Logger.getLogger(SocketsServidor.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        @Override
        public void run() {
            int valorAleatorio, idUsuario, numRebut;
            String contraCod, contraSCod = null;
            byte array[];
            Client client = null;
            valorAleatorio = (int) (Math.random() * (2000000000 - 1000000000) + 1000000000);
            try {
                misVal.writeInt(valorAleatorio);
                idUsuario = misRebut.readInt();
                contraCod = misRebut.readUTF();
                for (int i = 0; i < ClientsLlista.size(); i++) {
                    if (ClientsLlista.get(i).idClient == idUsuario) {
                        contraSCod = (ClientsLlista.get(i).password) + (valorAleatorio);
                        client = ClientsLlista.get(i);
                    }
                }
                try {
                    MessageDigest msgD = MessageDigest.getInstance("MD5");
                    array = msgD.digest(contraSCod.getBytes());
                    if (contraCod.equals(conversor(array))) {
                        //envia 1 
                        misVal.writeInt(1);
                        numRebut = misRebut.readInt();
                        opcions(numRebut, client);
                        socket.close();
                    } else {
                        //envia -1
                        misVal.writeInt(-1);
                        misVal.flush();
                        socket.close();
                    }
                } catch (NoSuchAlgorithmException ex) {
                    Logger.getLogger(SocketsServidor.class.getName()).log(Level.SEVERE, null, ex);
                }

            } catch (IOException ex) {
                Logger.getLogger(SocketsServidor.class.getName()).log(Level.SEVERE, null, ex);
            }

        }

        public void opcions(int num, Client client) throws IOException {
            switch (num) {
                case 10:    //int: cuantos mensajes tiene pendientes
                    synchronized (client) {
                        misVal.writeInt(client.mensaje.size());
                    }
                    break;
                case 11:    //int: cuantos mensajes tiene pendientes
                    synchronized (client) {
                        int numMisatges = client.mensaje.size();
                        misVal.writeInt(numMisatges);
                        for (int i = 0; i < numMisatges; i++) {
                            misVal.writeInt(client.mensaje.get(i).idOrigen); //int: id del cliente que ha enviado el mensaje 
                            misVal.writeUTF(client.mensaje.get(i).texto);    //String: texto del mensaje
                        }
                        client.mensaje.clear();
                    }
                    break;
                case 20:
                    int idDestinoPClient;
                    String msgPClient;
                    boolean varTF = false;

                    idDestinoPClient = misRebut.readInt();
                    msgPClient = misRebut.readUTF();
                    for (int i = 0; i < ClientsLlista.size(); i++) {
                        if (ClientsLlista.get(i).idClient == idDestinoPClient) {
                            varTF = true;
                            synchronized (ClientsLlista.get(i)) {
                                ClientsLlista.get(i).afegirMissatge(client.idClient, msgPClient);
                            }
                            i = ClientsLlista.size();
                        } else {
                            varTF = false;
                        }
                    }

                    if (varTF) {
                        misVal.writeInt(0);
                    } else {
                        misVal.writeInt(1);
                    }
                    break;
            }
        }
    }

    public static class Mensaje {

        int idOrigen;
        String texto;

        public Mensaje(int idOrigen, String texto) {
            this.idOrigen = idOrigen;
            this.texto = texto;
        }
    }

    public static class Client {

        int idClient;
        String password;
        String nomClient;
        ArrayList<Mensaje> mensaje;

        public Client(int idClient, String password, String nomClient) {
            this.idClient = idClient;
            this.password = password;
            this.nomClient = nomClient;
            mensaje = new ArrayList<>();
        }

        void afegirMissatge(int id, String missatge) {
            Mensaje mis = new Mensaje(id, missatge);
            mensaje.add(mis);
        }

    }
    
    private final static char[] hexArray = "0123456789ABCDEF".toCharArray();

    public static String conversor(byte[] cadena) {
        char[] hexChars = new char[cadena.length * 2];
        for (int j = 0; j < cadena.length; j++) {
            int v = cadena[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

}
