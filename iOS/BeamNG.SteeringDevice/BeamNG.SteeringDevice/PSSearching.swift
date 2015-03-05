//
//  PSSearching.swift
//  BeamNG.SteeringDevice
//
//  Created by Pawel Sulik on 10.10.14.
//  Copyright (c) 2014 28Apps. All rights reserved.
//

import Foundation
import UIKit

class PSSearching : NSObject, AsyncUdpSocketDelegate
{
    var socket : AsyncUdpSocket!;
    var listenSocket : AsyncUdpSocket!;
    
    var onConnectToHost : ((String, UInt16)->(Void))! = nil;
    
    override init()
    {
        super.init();
        socket = AsyncUdpSocket();
        socket.enableBroadcast(true, error: nil);
        listenSocket = AsyncUdpSocket(delegate: self);
        listenSocket.bindToPort(4444, error: nil);
        listenSocket.receiveWithTimeout(-1, tag: 0);
    }
    convenience init(connectionHandler: ((String, UInt16)->(Void)))
    {
        self.init();
        self.onConnectToHost = connectionHandler;
    }
    
    func broadcast(timeout : CFTimeInterval)
    {
        var message : NSString = "beamng\(UIDevice.currentDevice().name)";
        var data = message.dataUsingEncoding(NSUTF8StringEncoding);
        
        socket.sendData(data, toHost: PSNetUtil.broadcastAddress(), port: 4444, withTimeout: timeout, tag: 0);
    }
    
    func onUdpSocket(sock: AsyncUdpSocket!, didNotReceiveDataWithTag tag: Int, dueToError error: NSError!)
    {
        println("PSSearching: didNotReceiveData");
        println(error);
    }
    
    func onUdpSocket(sock: AsyncUdpSocket!, didNotSendDataWithTag tag: Int, dueToError error: NSError!)
    {
        println("PSSearching: didNotSendData");
    }
    
    func onUdpSocket(sock: AsyncUdpSocket!, didReceiveData data: NSData!, withTag tag: Int, fromHost host: String!, port: UInt16) -> Bool
    {
        var msg : NSString = NSString(data: data, encoding: NSUTF8StringEncoding)!;
        println("\nPSSearching: Received data!\n\t\(msg)\nfrom: \(host):\(port)");
        
        var temp : NSString = NSString(string: host);
        if(temp.containsString(PSNetUtil.localIPAddress()))
        {
            println("Received own data!");
            return false;
        }
        
        if(msg == "beamng")
        {
            if(onConnectToHost != nil)
            {
                onConnectToHost(host, 4445);
            }
            println("Connecting people...");
        }
        listenSocket.receiveWithTimeout(-1, tag: 0);
        return true;
    }
    
    func onUdpSocket(sock: AsyncUdpSocket!, didSendDataWithTag tag: Int)
    {
    }
    
    func onUdpSocketDidClose(sock: AsyncUdpSocket!)
    {
    }
}