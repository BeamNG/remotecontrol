//
//  PSSession.swift
//  BeamNG.SteeringDevice
//
//  Created by Pawel on 09.10.14.
//  Copyright (c) 2014 28Apps. All rights reserved.
//

import Foundation

class  PSGameControllerState
{
    var steerAngle : Float = 0.0;
    var acceleration : Float = 0.0;
    var brake : Float = 0.0;
}

class PSSession : AsyncUdpSocketDelegate
{
    var udpSocket : AsyncUdpSocket!;
    var ip: String;
    var port: UInt16;
    
    var steerData : PSGameControllerState!;
    
    init(host: PSHostListElement, UDPSocket : AsyncUdpSocket)
    {
        ip = host.ip;
        port = host.port;
        var error : NSErrorPointer!;
        udpSocket = UDPSocket;
        //udpSocket = AsyncUdpSocket();
        udpSocket.setDelegate(self);
        udpSocket.connectToHost(ip, onPort: port, error: nil);
        if(error != nil)
        {
            println("Error!\n\(error)");
        }
        steerData = PSGameControllerState();
    }
    func sendCurrentData()
    {
        var data : NSData!;
        var message: NSString = String(format: "Wheel angle: %.3f\nAcceleration: %.3f\nBrake: %.3f", steerData.steerAngle, steerData.acceleration, steerData.brake);
        data = message.dataUsingEncoding(NSUTF8StringEncoding);
        udpSocket.sendData(data, withTimeout: -1, tag: 0);
        //udpSocket.sendData(data, toHost: ip, port: port, withTimeout: -1, tag: 0);
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
        println("onUdpSocket Session didReceiveData");
        println(NSString(data: data, encoding: NSUTF8StringEncoding));
        udpSocket.receiveWithTimeout(-1, tag: 0);
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