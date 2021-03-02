package bazzar.rmi;

import bazzar.data.ProductName;

import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

/**
 * //TODO: Documentation
 *
 */
public interface PeerRmiService extends Remote {
    void flood(ProductName productName, int hopCount, ArrayList<String> peerIds, boolean isFirstLookup) throws RemoteException, MalformedURLException, NotBoundException;
    void traverseBack(String buyerId, String sellerId, ArrayList<String> peerIds) throws RemoteException, NotBoundException, MalformedURLException;
    void transact(String buyerId) throws RemoteException;
}
