package projectesockets;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SocketsServidor {

    public static ArrayList<Client> ClientsLlista = new ArrayList<>();

    public static void main(String[] args) {
        try {
            ClientsLlista.add(new Client(0,"123456","Roger"));
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
            int valorAleatorio,idUsuario;
            String contraCod,contraSCod;
            Client client;
            valorAleatorio = (int) (Math.random() * (2000000000 - 1000000000) + 1000000000);
            try {
                misVal.writeInt(valorAleatorio);
                idUsuario=misRebut.readInt();
                contraCod=misRebut.readUTF();
                for(int i=0;i<ClientsLlista.size();i++){
                    if(ClientsLlista.get(i).idClient==idUsuario){
                        contraSCod=(ClientsLlista.get(i).password)+(valorAleatorio);
                    }
                }
                
                if(contraCod.equals(contraSCod)){
                    
                }
            } catch (IOException ex) {
                Logger.getLogger(SocketsServidor.class.getName()).log(Level.SEVERE, null, ex);
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
}
