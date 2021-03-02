import bazzar.data.ProductName;
import bazzar.data.PeerType;
import bazzar.nodes.Peer;
import bazzar.rmi.PeerRmiServer;

import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Collections;
import java.util.List;

import static bazzar.constant.Constant.HOP_COUNT;

public class BazaarTest {

    public void testBuySellFish() throws RemoteException, MalformedURLException, NotBoundException, InterruptedException {
        System.out.println("Test Case 1: 2 Peers, Peer 1 is buyer of FISH, peer 2 is seller of FISH");

        String peerId1 = "1";
        String peerId2 = "2";

        System.out.println("Creating Peers: ");
        Peer peer1 = buildAndBindBuyer(peerId1, PeerType.BUYER, false);
        Peer peer2 = buildAndBindSeller(peerId2, PeerType.SELLER, ProductName.FISH, 1, false);

        System.out.println("\nAssigning neighbours: ");
        assignNeighbours(peer1, Collections.singletonList(peerId2));
        assignNeighbours(peer2, Collections.singletonList(peerId1));

        System.out.println();
        peer1.lookup(ProductName.FISH, HOP_COUNT);

        PeerRmiServer.unBindPeer(peerId1,peer1);
        PeerRmiServer.unBindPeer(peerId2,peer2);
    }

    public void testBuySellBoar() throws RemoteException, MalformedURLException, NotBoundException, InterruptedException {
        System.out.println("\nTest Case 2: 2 Peers, Peer 1 is buyer of FISH, peer 2 is seller of BOAR");

        String peerId1 = "1";
        String peerId2 = "2";

        System.out.println("Creating Peers: ");
        Peer peer1 = buildAndBindBuyer(peerId1, PeerType.BUYER, false);
        Peer peer2 = buildAndBindSeller(peerId2, PeerType.SELLER, ProductName.BOAR, 2, false);

        System.out.println("\nAssigning neighbours: ");
        assignNeighbours(peer1, Collections.singletonList(peerId2));
        assignNeighbours(peer2, Collections.singletonList(peerId1));

        System.out.println();
        peer1.lookup(ProductName.FISH, HOP_COUNT);

        PeerRmiServer.unBindPeer(peerId1,peer1);
        PeerRmiServer.unBindPeer(peerId2,peer2);
    }

    public void testBuySellRandom() throws RemoteException, MalformedURLException, NotBoundException, InterruptedException {
        System.out.println("\nTest Case 3: 2 Peers, Peer 1 currently is buyer of BOAR, peer 2 currently is seller of BOAR and then random selection");

        String peerId1 = "1";
        String peerId2 = "2";

        System.out.println("Creating Peers: ");
        Peer peer1 = buildAndBindBuyer(peerId1, PeerType.BUYER, true);
        Peer peer2 = buildAndBindSeller(peerId2, PeerType.SELLER, ProductName.BOAR, 1, true);

        System.out.println("\nAssigning neighbours: ");
        assignNeighbours(peer1, Collections.singletonList(peerId2));
        assignNeighbours(peer2, Collections.singletonList(peerId1));

        System.out.println();
        peer1.lookup(ProductName.BOAR, HOP_COUNT);

        Thread.sleep(1000);
        PeerRmiServer.unBindPeer(peerId1,peer1);
        PeerRmiServer.unBindPeer(peerId2,peer2);
    }

    private Peer buildAndBindBuyer(String peerId, PeerType peerType, boolean chooseRandomProduct) throws RemoteException {
        Peer peer = new Peer(peerType, peerId, chooseRandomProduct);
        PeerRmiServer.bindPeer(peerId, peer);
        return peer;
    }

    private void assignNeighbours(Peer peer, List<String> neighbours) throws RemoteException {
        peer.assignNeighbours(neighbours);
    }

    private Peer buildAndBindSeller(String peerId, PeerType peerType, ProductName productName, int quantity, boolean chooseRandomProduct) throws RemoteException {
        Peer peer = new Peer(peerType, productName, quantity, peerId, chooseRandomProduct);
        PeerRmiServer.bindPeer(peerId, peer);
        return peer;
    }

    public static void main(String[] args) throws RemoteException, MalformedURLException, NotBoundException, InterruptedException {
        BazaarTest test = new BazaarTest();
        //test.testBuySellFish();
        //test.testBuySellBoar();
        test.testBuySellRandom();
    }

}
