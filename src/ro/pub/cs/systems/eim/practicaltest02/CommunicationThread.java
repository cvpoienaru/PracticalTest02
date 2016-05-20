package ro.pub.cs.systems.eim.practicaltest02;

import android.util.Log;

import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;

public class CommunicationThread extends Thread {

    private ServerThread serverThread;
    private Socket socket;

    public CommunicationThread(ServerThread serverThread, Socket socket) {
        this.serverThread = serverThread;
        this.socket = socket;
    }

    @Override
    public void run() {
    	if(socket == null) {
    		Log.e(Constants.TAG, "[COMMUNICATION THREAD] Socket is null!");
    		return;
    	}
    	
        try {
            BufferedReader bufferedReader = Utilities.getReader(socket);
            PrintWriter printWriter = Utilities.getWriter(socket);
            
            if(bufferedReader == null || printWriter == null) {
            	Log.e(Constants.TAG, "[COMMUNICATION THREAD] BufferedReader / PrintWriter are null!");
            	socket.close();
            	return;
            }
            
            String request = bufferedReader.readLine();
            
            HashMap<String, String> data = serverThread.getData();
            String socketAddress = socket.getLocalAddress().toString();
            String socketPort = socket.getLocalPort() + "";
            String address = socketAddress + socketPort;
            String requestResult = null;
            
            Log.i(Constants.TAG, "[COMMUNICATION THREAD] Getting the information from the webservice...");
            HttpClient httpClient = new DefaultHttpClient();
            HttpGet httpGet = new HttpGet(Constants.WEB_SERVICE_ADDRESS);
            ResponseHandler<String> responseHandler = new BasicResponseHandler();
            String pageSourceCode = httpClient.execute(httpGet, responseHandler);
            
            if (data.containsKey(address)) {
                Log.i(Constants.TAG, "[COMMUNICATION THREAD] Getting the information from the cache...");
                requestResult = data.get(address);
                
                // Parse time and check if one minute passed here ...
            } else {
                serverThread.setData(socketAddress, pageSourceCode);
                requestResult = pageSourceCode;
            }

            if (requestResult != null) {
                String result = requestResult;
                printWriter.println(result);
                printWriter.flush();
            } else {
                Log.e(Constants.TAG, "[COMMUNICATION THREAD] Time information is null!");
            }

            socket.close();
        } catch (IOException ioException) {
            Log.e(Constants.TAG, "[COMMUNICATION THREAD] An exception has occurred: " + ioException.getMessage());
            if (Constants.DEBUG) {
                ioException.printStackTrace();
            }
        }
    }
}
