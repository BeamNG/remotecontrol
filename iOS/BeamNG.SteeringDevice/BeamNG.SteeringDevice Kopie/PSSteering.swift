//
//  PSSteering.swift
//  BeamNG.SteeringDevice
//
//  Created by Pawel on 09.10.14.
//  Copyright (c) 2014 28Apps. All rights reserved.
//

import Foundation

protocol PSSteeringDelegate
{
    func onSteering(newElementAdded element: PSHostListElement);
    
    func onSteering(sessionApproved session: PSSession, withHost host: PSHostListElement);
    func onSteering(sessionDeniedByHost host: PSHostListElement);
}

enum PSSteeringState
{
    case Idle
    case WaitingForHosts
    case WaitingForSession
    case AtSession
}

class PSHostListElement : NSObject
{
    var ip : String!;
    var port : UInt16 = 0;
}

class PSSteering : NSObject, AsyncUdpSocketDelegate
{
    var broadcastPort : UInt16 = 8888;
    var udpSocket : AsyncUdpSocket!;
    var hostsPending : [PSHostListElement] = [];
    
    //var handler : ((PSComputerListElement!)->())!;
    
    var selectedHost : PSHostListElement! = nil;
    var currentSession : PSSession! = nil;
    
    var state : PSSteeringState = PSSteeringState.Idle;
    
    var delegate : PSSteeringDelegate! = nil;
    
    override init()
    {
        super.init();
        
        var error : NSErrorPointer = NSErrorPointer();
        
        udpSocket = AsyncUdpSocket();
        udpSocket.setDelegate(self);
        udpSocket.enableBroadcast(true, error: error);
        if(error != nil)
        {
            println(error);
        }
    }
    func broadcast(timeout : CFTimeInterval)
    {
        /*for(var a : Int = 0; a < 256; a++)
        {
            for(var b : Int = 0; b < 256; b++)
            {
                for(var c : Int = 0; c < 256; c++)
                {
                    for(var d : Int = 0; d < 256; d++)
                    {
                        var ipToSend : NSString = NSString(format: "%d.%d.%d.%d", a, b, c, d);
                        println(ipToSend);
                    }
                }
            }
        }*/
        //PSNetUtil.netInfo();
        
        var port: UInt16 = 8888;
        var data : NSData!;
        var message: NSString = "TempMessage";
        data = message.dataUsingEncoding(NSUTF8StringEncoding);
        
        self.hostsPending.removeAll(keepCapacity: true);
        state = PSSteeringState.WaitingForHosts;
        PSNetUtil.netmask();
        //self.udpSocket.sendData(data, toHost: "192.168.178.255", port: port, withTimeout: -1, tag: 0);
        for(var i : Int = 0; i < 256; i++)
        {
            var tempIp : NSString = "192.168.178.";
            tempIp = tempIp.stringByAppendingString(NSString(format: "%d", i));
            self.udpSocket.sendData(data, toHost: tempIp, port: port, withTimeout: -1, tag: 0);
        }
        self.udpSocket.receiveWithTimeout(-1, tag: 0);
    }
    func sendRequestForSession(comp: PSHostListElement)
    {
        state = PSSteeringState.WaitingForSession;
        selectedHost = comp;
        var data : NSData!;
        var message: NSString = "sessionStart";
        data = message.dataUsingEncoding(NSUTF8StringEncoding);
        self.udpSocket.sendData(data, toHost: comp.ip, port: comp.port, withTimeout: -1, tag: 0);
    }
    
    
    //*****UDSocketDelegate
    func onUdpSocket(sock: AsyncUdpSocket!, didNotReceiveDataWithTag tag: Int, dueToError error: NSError!)
    {
        println("onUdpSocket didNotReceiveDataWithTag");
    }
    
    func onUdpSocket(sock: AsyncUdpSocket!, didNotSendDataWithTag tag: Int, dueToError error: NSError!)
    {
        //println("onUdpSocket didNotSendDataWithTag.\n\tError: \(error)");
    }
    
    func onUdpSocket(sock: AsyncUdpSocket!, didReceiveData data: NSData!, withTag tag: Int, fromHost host: String!, port: UInt16) -> Bool
    {
        self.udpSocket.receiveWithTimeout(-1, tag: 0);
        println("onUdpSocket main didReceiveData");
        var respond : NSString = NSString(data: data, encoding: NSUTF8StringEncoding);
        println(respond);
        if(state == PSSteeringState.WaitingForSession)
        {
            var hosts = hostsPending.filter({ (element: PSHostListElement) -> Bool in return element.ip == host && element.port == port;});
            if(respond == "ok" && hosts.count != 0)
            {
                //udpSocket.close();
                currentSession = PSSession(host: hosts[0], UDPSocket: udpSocket);
                if(delegate != nil)
                {
                    delegate.onSteering(sessionApproved: currentSession, withHost: hosts[0]);
                    state = PSSteeringState.AtSession;
                }
            }
            if(respond == "no" && hosts.count != 0)
            {
                if(delegate != nil)
                {
                    delegate.onSteering(sessionDeniedByHost: selectedHost);
                    state = PSSteeringState.WaitingForHosts;
                    selectedHost = nil;
                }
            }
        }
        if(respond == "play")
        {
            if(hostsPending.filter({ (element: PSHostListElement) -> Bool in
                return element.ip == host && element.port == port;
            }).count != 0)
            {
                return true;
            }
            var newEl : PSHostListElement = PSHostListElement();
            newEl.port = port;
            newEl.ip = host;
            hostsPending.append(newEl);
            if(delegate != nil)
            {
                delegate.onSteering(newElementAdded: newEl);
            }
        }
        return true;
    }
    
    func onUdpSocket(sock: AsyncUdpSocket!, didSendDataWithTag tag: Int)
    {
        //println("onUdpSocket didSendDataWithTag");
    }
    
    func onUdpSocketDidClose(sock: AsyncUdpSocket!)
    {
        println("onUdpSocketDidClose");
    }
}