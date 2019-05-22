//package nist.p_70nanb17h188.demo.pscr19.logic.link;
//
//import android.app.Application;
//import java.net.InetSocketAddress;
//import java.nio.channels.ServerSocketChannel;
//import java.nio.channels.SocketChannel;
//import nist.p_70nanb17h188.demo.pscr19.logic.Device;
//import nist.p_70nanb17h188.demo.pscr19.logic.log.Log;
//
///**
// *
// * @author Jiachen Chen
// */
//public class TCPConnectionServer {
//
//    public static final String TAG = "TCPConnectionServer";
//
//    public static void startServer() {
//        Device.setName(Device.NAME_M1);
//        Application application = new Application();
//        Log.init(1, application);
//        TCPConnectionManager manager = TCPConnectionManager.init();
//        assert manager != null;
//        Log.d(TAG, "TCPConnectionManager initialized!");
//        ServerSocketChannel serverSocketChannel = TCPConnectionManager.getDefaultInstance().
//                addServerSocketChannel(new InetSocketAddress(Constants.WIFI_DIRECT_SERVER_LISTEN_PORT),
//                        new ServerEventListener());
//        if (serverSocketChannel == null) {
//            Log.e(TAG, "Failed in adding port %d on listen!", Constants.WIFI_DIRECT_SERVER_LISTEN_PORT);
//        } else {
//            Log.d(TAG, "Added port %d on listen!", Constants.WIFI_DIRECT_SERVER_LISTEN_PORT);
//            try {
//                Thread.sleep(60 * 60 * 1000);
//            } catch (InterruptedException ex) {
//            }
//        }
//
//    }
//
//    private static class ServerEventListener implements
//            TCPConnectionManager.ServerSocketChannelEventHandler,
//            TCPConnectionManager.SocketChannelEventHandler {
//
//        @Override
//        public void onServerSocketChannelClosed(ServerSocketChannel serverSocketChannel) {
//            Log.d(TAG, "Server socket channel closed! %s", serverSocketChannel);
//        }
//
//        @Override
//        public void onServerSocketChannelCloseFailed(ServerSocketChannel serverSocketChannel) {
//            Log.d(TAG, "Server socket channel close failed! %s", serverSocketChannel);
//        }
//
//        @Override
//        public void onServerSocketChannelAcceptFailed(ServerSocketChannel serverSocketChannel) {
//            Log.d(TAG, "Server socket channel accept failed! %s", serverSocketChannel);
//        }
//
//        @Override
//        public TCPConnectionManager.SocketChannelEventHandler getSocketChannelEventHandler() {
//            return this;
//        }
//
//        @Override
//        public void onSocketConnected(SocketChannel socketChannel) {
//            Log.d(TAG, "Socket channel connected! %s", socketChannel);
//        }
//
//        @Override
//        public void onSocketConnectFailed(SocketChannel socketChannel) {
//            Log.d(TAG, "Socket channel connect failed! %s", socketChannel);
//        }
//
//        @Override
//        public void onSocketChannelClosed(SocketChannel socketChannel) {
//            Log.d(TAG, "Socket channel closed! %s", socketChannel);
//        }
//
//        @Override
//        public void onSocketChannelCloseFailed(SocketChannel socketChannel) {
//            Log.d(TAG, "Socket channel close failed! %s", socketChannel);
//        }
//
//        @Override
//        public void onSocketChannelNameReceived(SocketChannel socketChannel, String name) {
//            Log.d(TAG, "Socket channel name received! %s, %s", socketChannel, name);
//        }
//
//        @Override
//        public void onSocketChannelDataReceived(SocketChannel socketChannel, byte[] data) {
//            Log.d(TAG, "Socket channel data received! %s, %d", socketChannel, data.length);
//        }
//    }
//
//}
