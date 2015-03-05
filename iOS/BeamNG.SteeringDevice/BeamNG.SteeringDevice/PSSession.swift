//
//  PSSession.swift
//  BeamNG.SteeringDevice
//
//  Created by Pawel Sulik on 10.10.14.
//  Copyright (c) 2014 28Apps. All rights reserved.
//

import Foundation

class PSSteerData : NSObject
{
    var acceleration : Float = 0.0;
    var brake : Float = 0.0;
    var steer : Float = 0.0;
    override init()
    {
        super.init();
    }
}

class PSCarData : NSObject
{
    var speed : Float = 0.0;
    var rpm : Float = 0.0;
    var fuel : Float = 0.0;
    var temperature : Float = 0.0;
    var gear : Int = 0;
    var distance : Int = 0;
    override init()
    {
        super.init();
    }
}

class PSReceivedData
{
    func fromData(data: NSData)
    {
//        var s : NSInputStream = NSInputStream(data: data);
        
        var a : UInt32 = 0;
        var b : UInt32 = 0;
        var c : UInt32 = 0;
        
//        var buffer : [UInt8] = [UInt8](count: 64, repeatedValue: 0);
        
        
        data.getBytes(&timer, range: NSMakeRange(0, 4));
        data.getBytes(&carName, range: NSMakeRange(4, 4));
        
        data.getBytes(&flags, range: NSMakeRange(8, 2));
        
        data.getBytes(&gear, range: NSMakeRange(10, 1));
        data.getBytes(&playerId, range: NSMakeRange(11, 1));
        
        data.getBytes(&speed, range: NSMakeRange(12, 4));
        data.getBytes(&rpm, range: NSMakeRange(16, 4));
        data.getBytes(&turbo, range: NSMakeRange(20, 4));
        data.getBytes(&engineTemperature, range: NSMakeRange(24, 4));
        data.getBytes(&fuel, range: NSMakeRange(28, 4));
        data.getBytes(&oilPressure, range: NSMakeRange(32, 4));
        data.getBytes(&oilTemperature, range: NSMakeRange(36, 4));
        
        data.getBytes(&dashLights, range: NSMakeRange(40, 4));
        data.getBytes(&showLights, range: NSMakeRange(44, 4));
        
        data.getBytes(&throttle, range: NSMakeRange(48, 4));
        data.getBytes(&brake, range: NSMakeRange(52, 4));
        data.getBytes(&clutch, range: NSMakeRange(56, 4));
        
        data.getBytes(&display1, range: NSMakeRange(60, 16));
        data.getBytes(&display2, range: NSMakeRange(76, 16));
    }
    var timer : UInt32 = 0;
    var carName = [Byte](count: 4, repeatedValue: 0);
    var flags : UInt16 = 0;
    var gear : Byte = 0;
    var playerId : Byte = 0;
    var speed : Float = 0.0;
    var rpm : Float = 0.0;
    var turbo : Float = 0.0;
    var engineTemperature : Float = 0.0;
    var fuel : Float = 0.0;
    var oilPressure : Float = 0.0;
    var oilTemperature : Float = 0.0;
    var dashLights : UInt32 = 0;
    var showLights : UInt32 = 0;
    var throttle : Float = 0.0;
    var brake : Float = 0.0;
    var clutch : Float = 0.0;
    var display1 = [Byte](count: 16, repeatedValue: 0);
    var display2 = [Byte](count: 16, repeatedValue: 0);
    //var id : Int = 0;
}

class PSSession : AsyncUdpSocketDelegate
{
    var sendSocket : AsyncUdpSocket!;
    var listenSocket : AsyncUdpSocket!;
    
    var currentData : PSSteerData!;
    var carData : PSCarData!;
    
    var onSessionBroken : ((NSError)->(Void))! = nil;
    
    var receiveTimeout : CFTimeInterval = 10.0;
    
    init(host: String, port: UInt16, sessionBrokenHandler: ((NSError)->(Void))!)
    {
        currentData = PSSteerData();
        carData = PSCarData();
        
        self.onSessionBroken = sessionBrokenHandler;
        
        sendSocket = AsyncUdpSocket(delegate: self);
        sendSocket.connectToHost(host, onPort: port, error: nil);
        
        var error : NSError? = nil;
        
        listenSocket = AsyncUdpSocket(delegate: self);
        listenSocket.bindToPort(4445, error: &error);
        
        if(error != nil)
        {
            println(error);
        }
        
        listenSocket.receiveWithTimeout(-1, tag: 0);
    }
    deinit
    {
        println("close!!!!");
        listenSocket.close();
        sendSocket.close();
    }
    
    func sendCurrentData()
    {
        var msg : NSString = NSString(format: "wheel: %.1f acceleration: %.1f brake: %.1f", currentData.steer, currentData.acceleration, currentData.brake);
        var data = msg.dataUsingEncoding(NSUTF8StringEncoding);
        var mutData = NSMutableData();
        var nr = Int32(127);
        mutData.setData(data!);
        mutData = NSMutableData();
        mutData.appendBytes(&currentData.steer, length: 4);
        mutData.appendBytes(&currentData.acceleration, length: 4);
        mutData.appendBytes(&currentData.brake, length: 4);
        
        sendSocket.sendData(mutData, withTimeout: -1, tag: 0);
    }
    
    func onUdpSocket(sock: AsyncUdpSocket!, didNotReceiveDataWithTag tag: Int, dueToError error: NSError!)
    {
        println("Did not receive!");
        println(error);
        //If receive timeout has been reached OR the connection has been actually broken, send a notification to given callback
        if(self.onSessionBroken != nil)
        {
            self.listenSocket.setDelegate(nil);
            self.sendSocket.setDelegate(nil);
            self.onSessionBroken(error);
        }
    }
    
    func onUdpSocket(sock: AsyncUdpSocket!, didNotSendDataWithTag tag: Int, dueToError error: NSError!)
    {
        println("Data not sent!\n\(error)");
        //Something went wrong! It is better to raise an error than trying to send again!
        if(self.onSessionBroken != nil)
        {
            self.listenSocket.setDelegate(nil);
            self.sendSocket.setDelegate(nil);
            self.onSessionBroken(error);
        }
    }
    
    func onUdpSocket(sock: AsyncUdpSocket!, didReceiveData data: NSData!, withTag tag: Int, fromHost host: String!, port: UInt16) -> Bool
    {
        println("PSSession: Received data!\n\t\(data)\nfrom: \(host):\(port)");
        var paramID : Int8 = 0;
        var timer : UInt32 = 0;
        var recData : PSReceivedData = PSReceivedData();
        
        recData.fromData(data);
        
        carData.speed = recData.speed;
        carData.rpm = recData.rpm;
        
        carData.gear = Int(recData.gear);
        carData.fuel = recData.fuel;
        carData.temperature = recData.engineTemperature;
        
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