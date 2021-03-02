package bazzar.rmi;

import bazzar.nodes.Peer;

import java.io.Closeable;
import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class PeerRmiServer implements Closeable {

    static Registry registry;

    static {
        try {
            registry = LocateRegistry.createRegistry(1099);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public static void bindPeer(String peerId, Peer peer) throws RemoteException {
        registry.rebind(peerId, peer);
    }

    public static void unBindPeer(String peerId, Peer peer) throws RemoteException, NotBoundException {
        registry.unbind(peerId);
        UnicastRemoteObject.unexportObject(peer, true);
    }

    public static PeerRmiService lookupPeer(String neighbour) throws NotBoundException, MalformedURLException, RemoteException {
        return (PeerRmiService) Naming.lookup(neighbour);
    }

    @Override
    public void close() throws IOException {
        UnicastRemoteObject.unexportObject(registry, true);
    }
}
