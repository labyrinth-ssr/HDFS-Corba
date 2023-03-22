package client;


import impl.ClientImpl;

public class TestClient {
    static ClientImpl hdfsclient = new ClientImpl();

    public static void main(String[] args) {
        hdfsclient.run(args);
    }

}