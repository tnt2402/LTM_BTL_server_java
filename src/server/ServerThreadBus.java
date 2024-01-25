/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Admin
 */
public class ServerThreadBus {
    private List<ServerThread> listServerThreads;
    private List<String> listUserID;
    private List<String> listCompetitorUserID;
    private List<ServerThread> listCompetitorThreads;


    public List<ServerThread> getListServerThreads() {
        return listServerThreads;
    }

    public List<ServerThread> getListCompetitorThreads() {
        return listCompetitorThreads;
    }

    public List<String> getListCompetitorUserID() {
        return listCompetitorUserID;
    }


    public ServerThreadBus() {//Truyền danh sách Thread tại Server một ArrayList
        listServerThreads = new ArrayList<>();
        listCompetitorUserID = new ArrayList<>();
        listUserID = new ArrayList<>();
    }

    public void add(ServerThread serverThread){//Thêm Thread vào danh sách Thread tại Server
        listServerThreads.add(serverThread);
    }

    public void addUserID(String userID){//Thêm Thread vào danh sách Thread tại Server
        listUserID.add(userID);
    }

    public void add2WaitingList(String userID) {
        listCompetitorUserID.add(userID);

        if (listCompetitorUserID.size() >= 2) {
            // Create a new thread to handle the competitors
            Thread competitorThread = new Thread(() -> {
                String competitor1 = listCompetitorUserID.remove(0);
                String competitor2 = listCompetitorUserID.remove(0);

                // Perform any desired operations with the competitors
                // For example, you can print or process the competitors

                System.out.println("Competitor 1: " + competitor1);
                System.out.println("Competitor 2: " + competitor2);
                for (int i=0; i < listUserID.size(); i++) {
                    if (listUserID.get(i).equals(competitor1)) {
                        listCompetitorThreads.add(listServerThreads.get(i));
                    } else if (listUserID.get(i).equals(competitor2)) {
                        listCompetitorThreads.add(listServerThreads.get(i));
                    }
                }

            });

            // Start the competitor thread
            competitorThread.start();
        }
    }
    
    public void mutilCastSend(String message){ //like sockets.emit in socket.io
        for(ServerThread serverThread : Server.serverThreadBus.getListServerThreads()){
            serverThread.write(message);
        }
    }

    public void send2AllCompetitor(String message){ //like sockets.emit in socket.io
        for(ServerThread serverThread : Server.serverThreadBus.getListCompetitorThreads()){
            serverThread.write(message);
        }

    }
    
    public void boardCast(int id, String message){
        //Xét toàn bộ serverThread bằng cách get toàn bộ ListServerThreads và truyền vào serverThreadBus 
        for(ServerThread serverThread : Server.serverThreadBus.getListServerThreads()){
            if (serverThread.getClientNumber() == id) {//Nếu serverThread nhận ClientNumber == id thì tiếp tục 
                continue;
            } else {
                serverThread.write(message);
            }
        }
    }
    public int getLength(){//Lấy kích thước của danh sách Thread trong Server
        return listServerThreads.size();
    }
    
    public void sendOnlineList(){
        String res = "";//khởi tạo giá trị trả về
        List<ServerThread> threadbus = Server.serverThreadBus.getListServerThreads();
    //thiết lập giá trị threadbus bằng danh sách thread trong sẻver hiện tại
        for(ServerThread serverThread : threadbus){
    // dùng vòng lặp for-each để lặp danh sách thread có trong server hiện tại mà threadbus nhận được
            res+=serverThread.getClientNumber()+"-";
    // giá trị trả về bằng với số Client nhận được thông qua serverThread
        }
        Server.serverThreadBus.mutilCastSend("update-online-list"+","+res);
    //Sau đó gửi res đó tới toàn bộ server kèm với chuỗi update-online-list
    }
    
    public void sendMessageToPersion(int id, String message){
        //Gửi mess tới clent khi serverThread nhận đc id có trong hàm getClientNumber, 
        //sau đó thông qua chuỗi global-message để hiển thị lên màn hình hiển thị tin nhắn
        for(ServerThread serverThread : Server.serverThreadBus.getListServerThreads()){
            if(serverThread.getClientNumber()==id){
                serverThread.write("global-message"+","+message);
                break;
            }
        }
    }
    public void remove(int id){
        for(int i=0; i<Server.serverThreadBus.getLength(); i++){
            if(Server.serverThreadBus.getListServerThreads().get(i).getClientNumber()==id){
                Server.serverThreadBus.listServerThreads.remove(i);
            }
        }
    }
}
