//
//  PSSession.swift
//  BeamNG.SteeringDevice
//
//  Created by Pawel Sulik on 10.10.14.
//  Copyright (c) 2014 28Apps. All rights reserved.
//

import Foundation
import UIKit

class PSSteerData : NSObject
{
    var acceleration : Float = 0.0;
    var brake : Float = 0.0;
    var steer : Float = 0.0;
    var id : Float = 1;
    var lagDelay : Double = 0;
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
    var lights : UInt32 = 0;
    override init()
    {
        super.init();
    }
}

class PSReceivedData
{
    func fromData(_ data: Data)
    {
//        var s : NSInputStream = NSInputStream(data: data);

        
//        var buffer : [UInt8] = [UInt8](count: 64, repeatedValue: 0);
        
        //fix for accidentally grabbing hello message and trying to parse it, resulting in a crash
        if (data.count > 50) {
        
        (data as NSData).getBytes(&timer, range: NSMakeRange(0, 4));
        (data as NSData).getBytes(&carName, range: NSMakeRange(4, 4));
        
        (data as NSData).getBytes(&flags, range: NSMakeRange(8, 2));
        
        (data as NSData).getBytes(&gear, range: NSMakeRange(10, 1));
        (data as NSData).getBytes(&playerId, range: NSMakeRange(11, 1));
        
        (data as NSData).getBytes(&speed, range: NSMakeRange(12, 4));
        (data as NSData).getBytes(&rpm, range: NSMakeRange(16, 4));
        (data as NSData).getBytes(&turbo, range: NSMakeRange(20, 4));
        (data as NSData).getBytes(&engineTemperature, range: NSMakeRange(24, 4));
        (data as NSData).getBytes(&fuel, range: NSMakeRange(28, 4));
        (data as NSData).getBytes(&oilPressure, range: NSMakeRange(32, 4));
        (data as NSData).getBytes(&oilTemperature, range: NSMakeRange(36, 4));
        
        (data as NSData).getBytes(&dashLights, range: NSMakeRange(40, 4));
        (data as NSData).getBytes(&showLights, range: NSMakeRange(44, 4));
        
        (data as NSData).getBytes(&throttle, range: NSMakeRange(48, 4));
        (data as NSData).getBytes(&brake, range: NSMakeRange(52, 4));
        (data as NSData).getBytes(&clutch, range: NSMakeRange(56, 4));
        
        (data as NSData).getBytes(&display1, range: NSMakeRange(60, 16));
        (data as NSData).getBytes(&display2, range: NSMakeRange(76, 16));
        (data as NSData).getBytes(&rid, range: NSMakeRange(92, 4));
        }
    }
    var timer : UInt32 = 0;
    var carName = [UInt8](repeating: 0, count: 4);
    var flags : UInt16 = 0;
    var gear : UInt8 = 0;
    var playerId : UInt8 = 0;
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
    var display1 = [UInt8](repeating: 0, count: 16);
    var display2 = [UInt8](repeating: 0, count: 16);
    var rid : Int = 0;
}

class PSSession : NSObject, AsyncUdpSocketDelegate
{
    var sendSocket : AsyncUdpSocket!;
    var listenSocket : AsyncUdpSocket!;
    
    var currentData : PSSteerData!;
    var carData : PSCarData!;
    
    var onSessionBroken : ((NSError)->(Void))! = nil;
    
    var receiveTimeout : CFTimeInterval = 10.0;
    
    var finalHost : String = "";
    var finalPort: UInt16 = 4445;
    
    var lastID : Int = 0;
    var lpTime : Double = 0;
    var oldDiff : Double = 0;
    
    init(host: String, port: UInt16, sessionBrokenHandler: ((NSError)->(Void))!)
    {
        super.init();
        currentData = PSSteerData();
        carData = PSCarData();
        
        self.onSessionBroken = sessionBrokenHandler;
        
        self.sendSocket = AsyncUdpSocket(delegate: self);
        do {
            print("Connecting Send Socket: "+host+" : \(port)");
            try self.sendSocket.bind(toAddress: host, port: 4445)
            //try self.sendSocket.connect(toHost: host, onPort: 4445)
            //try sendSocket.bind(toPort: 4445)
        } catch _ {
        };
        
        let error : NSError? = nil;
        
        self.listenSocket = AsyncUdpSocket(delegate: self);
        do {
            print("Connecting Listen Socket: "+PSNetUtil.localIPAddress()+" : 4445");
            try self.listenSocket.bind(toAddress: PSNetUtil.localIPAddress(), port: 4445)
            //try self.listenSocket.connect(toHost: PSNetUtil.localIPAddress(), onPort: 4445)
        } catch _ {
        };
        
        if(error != nil)
        {
            print(error);
        }
        finalHost = host;
        finalPort = port;
        listenSocket.receive(withTimeout: -1, tag: 0);
    }
    deinit
    {
        print("close!!!!");
        listenSocket.close();
        sendSocket.close();
    }
    
    func sendCurrentData()
    {
        
        let toBytes : [UInt32] = [UInt32(bigEndian: UInt32(currentData.steer.bitPattern)), UInt32(bigEndian: UInt32(currentData.brake.bitPattern)), UInt32(bigEndian: UInt32(currentData.acceleration.bitPattern)), UInt32(bigEndian: UInt32(currentData.id.bitPattern))];
        let dataBytes = Data(bytes: UnsafeRawPointer(toBytes), count: 16);

        /*var test = [UInt32](count: 4, repeatedValue: 0);
        dataBytes.getBytes(&test, length: 16);
        test[0] = UInt32(bigEndian: test[0]);
        test[1] = UInt32(bigEndian: test[1]);
        test[2] = UInt32(bigEndian: test[2]);
        test[3] = UInt32(bigEndian: test[3]);
        
        let dataBytes2 = NSData(bytes: test, length: 16);*/
        
        sendSocket.send(dataBytes, toHost: finalHost, port: 4444, withTimeout: -1, tag: 0);
        
        if (lastID != Int(currentData.id)) {
            lastID = Int(currentData.id);
            lpTime = CACurrentMediaTime();
        }
        
    }
    
    @objc func onUdpSocket(_ sock: AsyncUdpSocket!, didNotReceiveDataWithTag tag: Int, dueToError error: NSError!)
    {
        print("Did not receive!");
        print(error);
        //If receive timeout has been reached OR the connection has been actually broken, send a notification to given callback
        if(self.onSessionBroken != nil)
        {
            self.listenSocket.setDelegate(nil);
            self.sendSocket.setDelegate(nil);
            self.onSessionBroken(error);
        }
    }
    
    @objc func onUdpSocket(_ sock: AsyncUdpSocket!, didNotSendDataWithTag tag: Int, dueToError error: NSError!)
    {
        print("Data not sent!\n\(error)");
        //Something went wrong! It is better to raise an error than trying to send again!
        if(self.onSessionBroken != nil)
        {
            self.listenSocket.setDelegate(nil);
            self.sendSocket.setDelegate(nil);
            self.onSessionBroken(error);
        }
    }

    @objc func onUdpSocket(_ sock: AsyncUdpSocket!, didReceive data: Data!, withTag tag: Int, fromHost host: String!, port: UInt16) -> Bool
    {
        //print("PSSession: Received data!\n\t\(data)\nfrom: \(host):\(port)");
        let recData : PSReceivedData = PSReceivedData();
        
        recData.fromData(data);
        //print(recData.speed);
        
        carData.speed = recData.speed;
        
        carData.rpm = recData.rpm;
        
        carData.gear = Int(recData.gear);
        carData.fuel = recData.fuel;
        carData.temperature = recData.engineTemperature;
        //print(recData.showLights);
        carData.lights = recData.showLights;
        //print(carData.lights);
        
        if (recData.rid == Int(currentData.id)) {
            let diff : Double = (CACurrentMediaTime() - lpTime)*1000/2;
            
            currentData.lagDelay = (oldDiff + diff+currentData.lagDelay)/3;
            currentData.lagDelay = round(currentData.lagDelay*100)/100;
            
            //print(currentData.lagDelay);
            
            currentData.id += 1;
            if (currentData.id == 128) {
                currentData.id = 0;
            }
            
            oldDiff = diff;
        }
        //print(recData.showLights);
        
        listenSocket.receive(withTimeout: -1, tag: 0);
        
        
        return true;
    }
    
    @objc func onUdpSocket(_ sock: AsyncUdpSocket!, didSendDataWithTag tag: Int)
    {
    }

    @objc func onUdpSocketDidClose(_ sock: AsyncUdpSocket!)
    {
    }
}
