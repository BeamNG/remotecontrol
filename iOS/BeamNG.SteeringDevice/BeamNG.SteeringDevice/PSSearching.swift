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
    
    var code : String = "232664";
    var initCon : Bool = false;
    
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
            try listenSocket.bind(toPort: 4445)
        } catch _ {
        };
        listenSocket.receive(withTimeout: -1, tag: 0);
    }
    deinit {
        listenSocket.close();
        socket.close();
    }
    convenience init(connectionHandler: @escaping ((String, UInt16)->(Void)))
    {
        self.init();
        self.onConnectToHost = connectionHandler;
    }
    
    func broadcast(_ timeout : CFTimeInterval)
    {
        let message : NSString = "beamng|\(UIDevice.current.name)|\(code)" as NSString;
        let data = message.data(using: String.Encoding.utf8.rawValue);
        
        print("Broadcasting from: \(PSNetUtil.localIPAddress())")
        socket.send(data, toHost: PSNetUtil.broadcastAddress(), port: 4444, withTimeout: timeout, tag: 0);
    }
    
    func onUdpSocket(_ sock: AsyncUdpSocket!, didNotReceiveDataWithTag tag: Int, dueToError error: NSError!)
    {
        print("PSSearching: didNotReceiveData");
        print(error);
    }
    
    func onUdpSocket(_ sock: AsyncUdpSocket!, didNotSendDataWithTag tag: Int, dueToError error: NSError!)
    {
        print("PSSearching: didNotSendData");
    }
    
    func onUdpSocket(_ sock: AsyncUdpSocket!, didReceive data: Data!, withTag tag: Int, fromHost host: String!, port: UInt16) -> Bool
    {
        if (!initCon) {
        let msg : NSString = NSString(data: data, encoding: String.Encoding.utf8.rawValue)!;
        print("\nPSSearching: Received data!\n\t\(msg)\nfrom: \(host):\(port)");
        
        let temp : NSString = NSString(string: host);
        if(temp.contains(PSNetUtil.localIPAddress()))
        {
            print("Received own data!");
            return false;
        }
        
        if(msg as String == "beamng|\(code)")
        {
            //print("Recieved Message...");
            if(onConnectToHost != nil)
            {
                //print(host);
                initCon = true;
                onConnectToHost(host, 4445);
            }
            print("Connecting people...");
        }
        listenSocket.receive(withTimeout: -1, tag: 0);
        return true;
        }
        return false;
    }
    
    func onUdpSocket(_ sock: AsyncUdpSocket!, didSendDataWithTag tag: Int)
    {
    }
    
    func onUdpSocketDidClose(_ sock: AsyncUdpSocket!)
    {
    }
}
