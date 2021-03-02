package bazzar.nodes;

import bazzar.data.ProductName;
import bazzar.data.PeerType;
import bazzar.rmi.PeerRmiServer;
import bazzar.rmi.PeerRmiService;

import java.lang.reflect.Array;
import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;

import static bazzar.constant.Constant.HOP_COUNT;

public class Peer extends UnicastRemoteObject implements PeerService, PeerRmiService {
    private static final long serialVersionUID = 1L;

    private PeerType peertype;
    private ProductName productName;
    private int quantity;
    private int capacity;
    private String peerId;
    private List<String> neighbours = new ArrayList<>();
    boolean chooseRandomProduct;

    //Buyer Constructor
    public Peer(PeerType peertype,String peerId, boolean chooseRandomProduct) throws RemoteException{
        this.peertype = peertype;
        this.peerId = peerId;
        this.chooseRandomProduct = chooseRandomProduct;
        System.out.printf("Peer %s created with type %s\n",peerId,peertype);
    }

    //Seller Constructor
    public Peer(PeerType peertype, ProductName productName, int quantity, String peerId, boolean chooseRandomProduct) throws RemoteException {
        this.peertype = peertype;
        this.productName = productName;
        this.capacity = quantity>0?quantity:1; //Hardcoding qty to 1 if qty value < 0
        this.quantity = capacity;
        this.peerId = peerId;
        this.chooseRandomProduct = chooseRandomProduct;
        System.out.printf("Peer %s created with type %s, selling %s of quantity %s\n",peerId, peertype,productName,quantity);
    }

    @Override
    public void lookup(ProductName productName, int hopCount) throws RemoteException, NotBoundException, MalformedURLException {
        if(peertype==PeerType.BUYER){
            //Assigning what buyer wants to buy
            this.productName = productName;
            System.out.printf("Peer %s wants to buy %s%n",this.peerId,this.productName);

            flood(productName,hopCount,new ArrayList<>(), true);
        }else{
            System.err.println("Seller Cant initiate lookup");
        }
    }

    @Override
    public void reply(String buyerId, String sellerId) {
        System.out.printf("Replying back from seller %s to buyer %s, with current peer %s\n", sellerId,buyerId,this.peerId);
    }

    @Override
    public void buy(String sellerId) throws NotBoundException, MalformedURLException, RemoteException {
        PeerRmiService seller = PeerRmiServer.lookupPeer(sellerId);
        System.out.printf("Peer %s initiated buy transaction with peer %s\n", this.peerId, sellerId);
        seller.transact(this.peerId);
    }

    @Override
    public void flood(ProductName productName, int hopCount, ArrayList<String> peerIds, boolean isFirstLookup) throws RemoteException, MalformedURLException, NotBoundException {
        String buyerId = extractBuyerId(peerIds);
        if(hopCount==0){
            System.out.printf("Lookup request reached peer %s, but hopcount is 0 so terminating lookup search%n", peerId);
            return;
        }
        if(!Objects.isNull(buyerId) && buyerId.equals(peerId)){
            System.err.printf("Buyer %s lookup request reached to itself, so terminating lookup search%n", peerId);
            return;
        }
        if(peertype==PeerType.SELLER && this.productName == productName){
            System.out.println("Seller of "+ productName +" found with peer "+peerId);
            replyToPrevPeer(peerIds);
        }else{
            //Doesnt sell desired product, adding peerId to the list and flooding the network with lookup request
            peerIds.add(this.peerId);
            // If its the first lookup, not decreasing hop count
            lookupNeighbours(isFirstLookup?hopCount:hopCount-1, productName,peerIds);
        }
    }

    @Override
    public void traverseBack(String buyerId, String sellerId, ArrayList<String> peerIds) throws RemoteException, NotBoundException, MalformedURLException {
        if(buyerId.equals(this.peerId)){
            System.out.println("Reply from peer "+ sellerId +" has reached to peer " + this.peerId);
            buy(sellerId);
            selectNextItemToBuy();
        }else{
            peerIds.remove(peerIds.size()-1);
            replyToPrevPeer(peerIds);
        }
    }

    private void selectNextItemToBuy() throws RemoteException, NotBoundException, MalformedURLException {
        if(chooseRandomProduct){
            ProductName[] productNames = ProductName.values();
            this.productName = productNames[new Random().nextInt(productNames.length)];
            System.out.printf("Peer %s is now buying %s\n",this.peerId, productName);
            lookup(this.productName,HOP_COUNT);
        }
    }

    @Override
    public void transact(String buyerId) throws RemoteException {
        if(quantity>0){
            System.out.printf("Peer %s bought %s from peer %s%n",buyerId, productName, peerId);
            decreaseInventory();
            System.out.printf("Peer %s now has %s quantity of %s\n", this.peerId,quantity,productName);
        }
        if(quantity==0){
            restock();
        }
    }

    private void replyToPrevPeer(ArrayList<String> peerIds) throws NotBoundException, MalformedURLException, RemoteException {
        String buyerId = extractBuyerId(peerIds);
        reply(buyerId,this.peerId);
        PeerRmiService prevPeer = PeerRmiServer.lookupPeer(peerIds.get(peerIds.size()-1));
        prevPeer.traverseBack(buyerId,this.peerId, peerIds);
    }

    private String extractBuyerId(ArrayList<String> peerIds) {
        return peerIds.isEmpty()?null:peerIds.get(0);
    }

    private void lookupNeighbours(int hopCount, ProductName productName, ArrayList<String> peerIds) {
        for(String neighbour:neighbours){
            try {
                PeerRmiService peer = PeerRmiServer.lookupPeer(neighbour);
                peer.flood(productName,hopCount, peerIds, false);
            } catch (NotBoundException | MalformedURLException | RemoteException e) {
                System.err.println("Neighbour lookup failed");
            }
        }
    }

    public void assignNeighbours (List<String> neighbours) throws RemoteException{
        if(neighbours!=null && neighbours.size()>0) {
            this.neighbours = neighbours;
            System.out.println(this.peerId+" --> "+ Arrays.toString(neighbours.toArray()));
        }
    }

    private void decreaseInventory() {
        System.out.printf("Peer %s reduced current quantity %s of %s by 1%n", this.peerId,quantity, productName);
        quantity--;
    }

    private void restock(){
        quantity=capacity;
        if(chooseRandomProduct){
            ProductName[] productNames = ProductName.values();
            this.productName = productNames[new Random().nextInt(productNames.length)];
            System.out.printf("Peer %s is now next selling %s of quantity %s\n",this.peerId, productName,quantity);
        }else{
            System.out.printf("Peer %s restocked %s to quantity %s\n", this.peerId, productName,quantity);
        }
    }
}
