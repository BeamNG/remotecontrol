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
    
    var code : Int = 232664;
    
    override init()
    {
        super.init();
        socket = AsyncUdpSocket();
        do {
            try socket.enableBroadcast(true)
        } catch _ {
        };
        listenSocket = AsyncUdpSocket(delegate: self);
        do {
            try listenSocket.bindToPort(4445)
        } catch _ {
        };
        listenSocket.receiveWithTimeout(-1, tag: 0);
    }
    convenience init(connectionHandler: ((String, UInt16)->(Void)))
    {
        self.init();
        self.onConnectToHost = connectionHandler;
    }
    
    func broadcast(timeout : CFTimeInterval)
    {
        let message : NSString = "beamng|\(UIDevice.currentDevice().name)|\(code)";
        let data = message.dataUsingEncoding(NSUTF8StringEncoding);
        
        socket.sendData(data, toHost: PSNetUtil.broadcastAddress(), port: 4444, withTimeout: timeout, tag: 0);
    }
    
    func onUdpSocket(sock: AsyncUdpSocket!, didNotReceiveDataWithTag tag: Int, dueToError error: NSError!)
    {
        print("PSSearching: didNotReceiveData");
        print(error);
    }
    
    func onUdpSocket(sock: AsyncUdpSocket!, didNotSendDataWithTag tag: Int, dueToError error: NSError!)
    {
        print("PSSearching: didNotSendData");
    }
    
    func onUdpSocket(sock: AsyncUdpSocket!, didReceiveData data: NSData!, withTag tag: Int, fromHost host: String!, port: UInt16) -> Bool
    {
        let msg : NSString = NSString(data: data, encoding: NSUTF8StringEncoding)!;
        print("\nPSSearching: Received data!\n\t\(msg)\nfrom: \(host):\(port)");
        
        let temp : NSString = NSString(string: host);
        if(temp.containsString(PSNetUtil.localIPAddress()))
        {
            print("Received own data!");
            return false;
        }
        
        if(msg == "beamng|\(code)")
        {
            //print("Recieved Message...");
            if(onConnectToHost != nil)
            {
                //print(host);
                onConnectToHost(host, 4445);
            }
            print("Connecting people...");
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