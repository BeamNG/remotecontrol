//
//  PSSessionViewController.swift
//  BeamNG.SteeringDevice
//
//  Created by Pawel Sulik on 10.10.14.
//  Copyright (c) 2014 28Apps. All rights reserved.
//

import UIKit
import CoreMotion
import QuartzCore

class PSSessionViewController : UIViewController
{
    var searching : PSSearching!;
    var session : PSSession! = nil;
    var cm : CMMotionManager!;
    
    var connectionButton : UIButton!;
    
    var steeringWheelLayer : CAShapeLayer!;
    var steeringWheelView : UIView!;
//    var steeringWheelLabel : UILabel!;
    
    var speed : PSProgressBar!;
    var rpm : PSProgressBar!;
    var fuel : PSProgressBar!;
    var temperature : PSProgressBar!;
    
    var hudView : UIView!;
    var hudImage : UIImage! = nil;
    var hudImageView : UIImageView! = nil;
    
    var labelSpeed : UILabel! = nil;
    var labelGear : UILabel! = nil;
    var labelDist : UILabel! = nil;
    var labelLag : UILabel! = nil;
    
    var buttonAccelerate : UIButton! = nil;
    var buttonBrake : UIButton! = nil;
    
    var lightsBG : UIImage! = nil;
    var lightsBGView : UIImageView! = nil;
    
    var lowBeams : UIImage! = nil;
    var lowBeamView : UIImageView! = nil;
    
    var highBeams : UIImage! = nil;
    var highBeamView : UIImageView! = nil;
    
    var lBlinker : UIImage! = nil;
    var lBlinkerView : UIImageView! = nil;
    
    var rBlinker : UIImage! = nil;
    var rBlinkerView : UIImageView! = nil;
    
    override func viewDidLoad()
    {
        super.viewDidLoad();
        self.view.backgroundColor = UIColor.blackColor();
        
        hudImage = UIImage(named: "hud_single_nocolor")!;
        //hudImage = UIImage(named: "text_tester")!;
        let sizeRatio : CGFloat = hudImage.size.height / hudImage.size.width;
        var imgWidth : CGFloat = 500.0;
        var imgHeight : CGFloat = imgWidth * sizeRatio;
        imgWidth = hudImage.size.width;
        imgHeight = hudImage.size.height;
        hudView = UIImageView(frame: CGRectMake(self.view.frame.height * 0.5 - imgWidth * 0.5, self.view.frame.width * 0.5 - imgHeight * 0.5, imgWidth, imgHeight));
        self.view.addSubview(hudView);
        
        
        hudImageView = UIImageView(frame: CGRectMake(0, 0, imgWidth, imgHeight));
        hudImageView.image = hudImage;
        hudView.addSubview(hudImageView);
        
        lightsBG = UIImage(named: "lightsbg")!;
        lightsBGView = UIImageView(frame: CGRectMake(0, 0, imgWidth, imgHeight));
        lightsBGView.image = lightsBG;
        hudView.addSubview(lightsBGView);
        
        lowBeams = UIImage(named: "lowbeams")!;
        lowBeamView = UIImageView(frame: CGRectMake(0, 0, imgWidth, imgHeight));
        lowBeamView.image = lowBeams;
        hudView.addSubview(lowBeamView);
        
        highBeams = UIImage(named: "highbeams")!;
        highBeamView = UIImageView(frame: CGRectMake(0, 0, imgWidth, imgHeight));
        highBeamView.image = highBeams;
        hudView.addSubview(highBeamView);
        
        lBlinker = UIImage(named: "leftblinker")!;
        lBlinkerView = UIImageView(frame: CGRectMake(0, 0, imgWidth, imgHeight));
        lBlinkerView.image = lBlinker;
        hudView.addSubview(lBlinkerView);
        
        rBlinker = UIImage(named: "rightblinker")!;
        rBlinkerView = UIImageView(frame: CGRectMake(0, 0, imgWidth, imgHeight));
        rBlinkerView.image = rBlinker;
        hudView.addSubview(rBlinkerView);

        let wheelRadius : CGFloat = 100;
        steeringWheelLayer = CAShapeLayer();
        steeringWheelLayer.path = UIBezierPath(arcCenter: CGPointMake(0, 0), radius: wheelRadius, startAngle: 0, endAngle: 3.15 * 2.0, clockwise: true).CGPath;
        steeringWheelLayer.strokeColor = UIColor(red: 0, green: 0, blue: 0, alpha: 1).CGColor;
        steeringWheelLayer.fillColor = UIColor(red: 1, green: 1, blue: 1, alpha: 1).CGColor;
        steeringWheelLayer.lineWidth = 1.0;
        steeringWheelLayer.bounds = CGRectMake(-wheelRadius, -wheelRadius, wheelRadius * 2, wheelRadius * 2);
        steeringWheelLayer.anchorPoint = CGPointMake(0, 0);
        steeringWheelLayer.position = CGPointMake(0, 0);
        steeringWheelLayer.borderWidth = 1.0;
        steeringWheelLayer.masksToBounds = true;
        
        steeringWheelView = UIView(frame: CGRectMake(self.view.frame.height * 0.5 - wheelRadius, 50, wheelRadius * 2, wheelRadius * 2));
        steeringWheelView.layer.addSublayer(steeringWheelLayer);
        
        
        let beginAngle : CGFloat = CGFloat(-M_PI - M_PI_4 * 0.5);
        let endAngle : CGFloat = CGFloat(M_PI_4 * 0.5);
        //beginAngle = 0.0;
        
        //width and height = radius * 2
        
        //Those percent values are relative to the size of the HUDImage
        let percentSpeedRadius : CGFloat = 0.4520;
        let percentSpeedWidth : CGFloat = 0.117;
        
        let percentRPMRadius : CGFloat = 0.7083;
        let percentRPMWidth : CGFloat = 0.0777;
        
        let percentFuelRadius : CGFloat = 0.904355;
        let percentFuelWidth : CGFloat = 0.0796872;
        
        let percentTemperatureRadius : CGFloat = 0.901355;
        let percentTemperatureWidth : CGFloat = 0.0786872;
        
        let percentMiddle : CGPoint = CGPointMake(0.5, 0.693);
        
        speed = PSProgressBar(frame: CGRectMake(0, 0, percentSpeedRadius * imgWidth, percentSpeedRadius * imgWidth));
        speed.center = CGPointMake(hudView.frame.width * percentMiddle.x, hudView.frame.height * percentMiddle.y);
        speed.draw(beginAngle, endAngle: endAngle, lineWidth: percentSpeedWidth * imgWidth, strokeColor: UIColor(red: 0x14 / 255.0, green: 0x94 / 255.0, blue: 0x34 / 255.0, alpha: 1.0).CGColor, clockwise: true);
        hudView.addSubview(speed);
        
        rpm = PSProgressBar(frame: CGRectMake(0, 0, percentRPMRadius * imgWidth, percentRPMRadius * imgWidth));
        rpm.center = CGPointMake(hudView.frame.width * percentMiddle.x, hudView.frame.height * percentMiddle.y);
        rpm.draw(beginAngle, endAngle: endAngle, lineWidth: percentRPMWidth * imgWidth, strokeColor: UIColor(red: 0x2b / 255.0, green: 0x71 / 255.0, blue: 0xff / 255.0, alpha: 1.0).CGColor, clockwise: true);
        hudView.addSubview(rpm);
        
        fuel = PSProgressBar(frame: CGRectMake(0, 0, percentFuelRadius * imgWidth, percentFuelRadius * imgWidth));
        fuel.center = CGPointMake(hudView.frame.width * percentMiddle.x, hudView.frame.height * percentMiddle.y);
        fuel.draw(beginAngle, endAngle: -CGFloat(M_PI_2) * 1.4, lineWidth: percentFuelWidth * imgWidth, strokeColor: UIColor(red: 0x31 / 255.0, green: 0x32 / 255.0, blue: 0x6e / 255.0, alpha: 1.0).CGColor, clockwise: true);
        hudView.addSubview(fuel);
        
        temperature = PSProgressBar(frame: CGRectMake(0, 0, percentTemperatureRadius * imgWidth, percentTemperatureRadius * imgWidth));
        temperature.center = CGPointMake(hudView.frame.width * percentMiddle.x, hudView.frame.height * percentMiddle.y);
        temperature.draw(endAngle, endAngle: -CGFloat(M_PI_2) * 0.6, lineWidth: percentTemperatureWidth * imgWidth, strokeColor: UIColor(red: 0x6a / 255.0, green: 0x27 / 255.0, blue: 0x27 / 255.0, alpha: 1.0).CGColor, clockwise: false);
        hudView.addSubview(temperature);
        
        hudView.bringSubviewToFront(hudImageView);
        
        let percentageLabelWidth : CGFloat = 0.3;
        let labelWidth : CGFloat = percentageLabelWidth * imgWidth;
        
        labelSpeed = UILabel(frame: CGRectMake(0.5029 * imgWidth - labelWidth * 0.5, 0.641 * imgHeight - labelWidth * 0.5, labelWidth, labelWidth));
        //labelSpeed.backgroundColor = UIColor.redColor();
        labelSpeed.text = "046";
        labelSpeed.textColor = UIColor.whiteColor();
        labelSpeed.font = UIFont(name: "OpenSans-Bold", size: 0.1215 * imgWidth);
        labelSpeed.textAlignment = NSTextAlignment.Center;
        hudView.addSubview(labelSpeed);
        
        labelGear = UILabel(frame: CGRectMake(0.47 * imgWidth - labelWidth * 0.5, 0.813 * imgHeight - labelWidth * 0.5, labelWidth, labelWidth));
        //labelGear.backgroundColor = UIColor.redColor();
        labelGear.text = "2";
        labelGear.textColor = UIColor.whiteColor();
        //labelGear.font = UIFont(name: "OpenSans-Bold", size: 0.162234 * imgWidth);
        labelGear.font = UIFont(name: "OpenSans-Bold", size: 0.156915 * imgWidth);
        labelGear.textAlignment = NSTextAlignment.Center;
        hudView.addSubview(labelGear);
        
        
        labelDist = UILabel(frame: CGRectMake(0.535 * imgWidth - labelWidth * 0.5, 0.9593 * imgHeight - labelWidth * 0.5, labelWidth, labelWidth));
        //labelGear.backgroundColor = UIColor.redColor();
        labelDist.text = "000086";
        labelDist.textColor = UIColor.whiteColor();
        //labelGear.font = UIFont(name: "OpenSans-Bold", size: 0.162234 * imgWidth);
        labelDist.font = UIFont(name: "OpenSans-ExtraBold", size: 0.0532 * imgWidth);
        labelDist.textAlignment = NSTextAlignment.Center;
        hudView.addSubview(labelDist);
        
        labelLag = UILabel(frame: CGRectMake(0.5029 * imgWidth - labelWidth * 0.5, 0.641 * imgHeight - labelWidth * -0.5, labelWidth, labelWidth));
        //labelSpeed.backgroundColor = UIColor.redColor();
        labelLag.text = "Delay: 0.0ms";
        labelLag.textColor = UIColor.whiteColor();
        labelLag.font = UIFont(name: "OpenSans-Bold", size: 0.03 * imgWidth);
        labelLag.textAlignment = NSTextAlignment.Center;
        hudView.addSubview(labelLag);

        
        buttonAccelerate = UIButton(type: UIButtonType.System) as UIButton;
        buttonAccelerate.frame = CGRectMake(0, 0, self.view.frame.height * 0.5, self.view.frame.width);
        buttonAccelerate.setTitle("", forState: UIControlState.Normal);
        buttonAccelerate.addTarget(self, action: "onButtonAccelerate0", forControlEvents: UIControlEvents.TouchDown);
        buttonAccelerate.addTarget(self, action: "onButtonAccelerate1", forControlEvents: UIControlEvents.TouchUpInside);
        buttonAccelerate.backgroundColor = UIColor(red: 1.0, green: 1.0, blue: 1.0, alpha: 0.0);
        self.view.addSubview(buttonAccelerate);
        
        buttonBrake = UIButton(type: UIButtonType.System) as UIButton;
        buttonBrake.frame = CGRectMake(self.view.frame.height * 0.5, 0, self.view.frame.height * 0.5, self.view.frame.width);
        buttonBrake.setTitle("", forState: UIControlState.Normal);
        buttonBrake.addTarget(self, action: "onButtonBrake0", forControlEvents: UIControlEvents.TouchDown);
        buttonBrake.addTarget(self, action: "onButtonBrake1", forControlEvents: UIControlEvents.TouchUpInside);
        buttonBrake.backgroundColor = UIColor(red: 1.0, green: 1.0, blue: 1.0, alpha: 0.0);
        self.view.addSubview(buttonBrake);
        
        self.searching = PSSearching(connectionHandler: self.onConnected);
        
        self.connectionButton = UIButton(type: UIButtonType.System) as UIButton;
        self.connectionButton.setTitle("Connect", forState: UIControlState.Normal);
        self.connectionButton.addTarget(self, action: Selector("onButtonConnect"), forControlEvents: UIControlEvents.TouchUpInside);
        self.connectionButton.frame = CGRectMake(10.0, 10.0, 100, 100);
        self.view.addSubview(self.connectionButton);
        
        cm = CMMotionManager();
        
        cm.deviceMotionUpdateInterval = 0.05;
        cm.startDeviceMotionUpdatesUsingReferenceFrame(CMAttitudeReferenceFrame.XArbitraryZVertical, toQueue: NSOperationQueue.mainQueue(), withHandler:
            
        {
        (deviceMotion: CMDeviceMotion?, error: NSError?) in
            
            if let deviceMotion = deviceMotion {
                let gravity : PSVector = PSVector();
                gravity.x = deviceMotion.gravity.x;
                gravity.y = deviceMotion.gravity.y;
                gravity.z = deviceMotion.gravity.z;
                
                let upVec : PSVector = PSVector(x: 0, y: 1, z: 0);
            
                var angle : Double = acos(gravity.dot(upVec));
                if(self.interfaceOrientation == UIInterfaceOrientation.LandscapeLeft)
                {
                    angle -= M_PI_2;
                    angle *= -1;
                    angle += M_PI_2;
                }
                let angleDeg : Double = angle * 180.0 / 3.145;
                let translatedAngle : Double = angleDeg - 135.0;
                
                if(self.session != nil)
                {
                    //print("get steer angle");
                    //self.session.currentData.steer = round(Float(translatedAngle / 90.0) * -1.0);
                    self.session.currentData.steer = Float(translatedAngle / 90.0) * -1.0;
                    //print("session exists, send data");
                    self.session.sendCurrentData();
                }
                
                let animDuration : Double = 0.07;
                UIView.animateWithDuration(animDuration, animations: { () -> Void in
                    self.hudView.transform = CGAffineTransformConcat(CGAffineTransformMakeRotation(CGFloat(-(angle - (90.0 / (180.0 / 3.145))))), CGAffineTransformMakeScale(1.0, 1.0));
                    
                    if(self.session != nil)
                    {
                        self.speed.progress = CGFloat((self.session.carData.speed*2.23694)/220);
                        self.rpm.progress = CGFloat(self.session.carData.rpm/8000);
                        //m/s to mph
                        self.labelSpeed.text = String(format: "%03d", Int(self.session.carData.speed*2.23694));
                        if(self.session.carData.gear == 0)
                        {
                            self.labelGear.text = String(format: "R");
                        }
                        else if(self.session.carData.gear == 1)
                        {
                            self.labelGear.text = String(format: "N");
                        }
                        else
                        {
                            self.labelGear.text = String(format: "%01d", Int(self.session.carData.gear-1));
                        }
                        self.labelDist.text = String(format: "%06d", Int(self.session.carData.distance));
                        self.fuel.progress = CGFloat(self.session.carData.fuel);
                        self.temperature.progress = CGFloat(self.session.carData.temperature);
                        self.labelLag.text = "Delay: "+String(self.session.currentData.lagDelay)+"ms";
                        var lights : Int = Int(self.session.carData.lights);
                        if (lights - 96 >= 0) {
                            //print("show hazards");
                            self.lBlinkerView.hidden = false;
                            self.rBlinkerView.hidden = false;
                            lights -= 96;
                        }
                        else if (lights - 64 >= 0) {
                            //print("show right blinker");
                            self.lBlinkerView.hidden = true;
                            self.rBlinkerView.hidden = false;
                            lights -= 64;
                        }
                        else if (lights - 32 >= 0) {
                            //print("show Left blinker");
                            self.rBlinkerView.hidden = true;
                            self.lBlinkerView.hidden = false;
                            lights -= 32;
                        }
                        else {
                            self.lBlinkerView.hidden = true;
                            self.rBlinkerView.hidden = true;
                        }
                        if (lights - 2 >= 0) {
                            //print("show high beams");
                            self.highBeamView.hidden = false;
                            lights -= 2;
                        }
                        else {
                            self.highBeamView.hidden = true;
                        }
                        if (lights - 1 >= 0) {
                            //print("show low beams");
                            self.lowBeamView.hidden = false;
                            lights -= 1;
                        }
                        else {
                            self.lowBeamView.hidden = true;
                        }
                    }
                });
            }
        });
    }
    func onConnected(toHost: String, onPort: UInt16)
    {
        if(self.session == nil)
        {
            self.connectionButton.hidden = true;
            self.session = PSSession(host: toHost, port: onPort, sessionBrokenHandler: self.onDisconnected);
        }
        else
        {
            print("Tried to connect once more!");
        }
    }
    func onDisconnected(error: NSError)
    {
        //If the session invokes this method, it means that the user has to reconnect.
        self.session = nil;
        self.connectionButton.hidden = false;
    }
    func onButtonConnect()
    {
        self.searching.broadcast(1);
    }
    func onButtonAccelerate0()
    {
        buttonAccelerate.backgroundColor = UIColor(red: 1.0, green: 1.0, blue: 1.0, alpha: 0.1);
        if(self.session != nil)
        {
            self.session.currentData.acceleration = 1.0;
        }
    }
    func onButtonAccelerate1()
    {
        buttonAccelerate.backgroundColor = UIColor.clearColor();
        if(self.session != nil)
        {
            self.session.currentData.acceleration = 0.0;
        }
    }
    func onButtonBrake0()
    {
        buttonBrake.backgroundColor = UIColor(red: 1.0, green: 1.0, blue: 1.0, alpha: 0.1);
        if(self.session != nil)
        {
            self.session.currentData.brake = 1.0;
        }
    }
    func onButtonBrake1()
    {
        buttonBrake.backgroundColor = UIColor.clearColor();
        if(self.session != nil)
        {
            self.session.currentData.brake = 0.0;
        }
    }
    
    override func shouldAutorotate() -> Bool {
        return true;
    }
    override func supportedInterfaceOrientations() -> UIInterfaceOrientationMask
    {
        return UIInterfaceOrientationMask.Landscape;
    }
}