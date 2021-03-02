package bazzar.nodes;

import bazzar.data.ProductName;

import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

public interface PeerService {
    void lookup(ProductName productName, int hopCount) throws RemoteException, NotBoundException, MalformedURLException;
    void reply(String buyerId, String sellerId);
    void buy(String sellerId) throws NotBoundException, MalformedURLException, RemoteException;
}
