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
    
    var buttonAccelerate : UIButton! = nil;
    var buttonBrake : UIButton! = nil;
    
    override func viewDidLoad()
    {
        super.viewDidLoad();
        
        self.view.backgroundColor = UIColor.blackColor();
        
        hudImage = UIImage(named: "hud_single_nocolor")!;
        //hudImage = UIImage(named: "text_tester")!;
        var sizeRatio : CGFloat = hudImage.size.height / hudImage.size.width;
        var imgWidth : CGFloat = 500.0;
        var imgHeight : CGFloat = imgWidth * sizeRatio;
        imgWidth = hudImage.size.width;
        imgHeight = hudImage.size.height;
        hudView = UIImageView(frame: CGRectMake(self.view.frame.height * 0.5 - imgWidth * 0.5, self.view.frame.width * 0.5 - imgHeight * 0.5, imgWidth, imgHeight));
        self.view.addSubview(hudView);
        
        hudImageView = UIImageView(frame: CGRectMake(0, 0, imgWidth, imgHeight));
        hudImageView.image = hudImage;
        hudView.addSubview(hudImageView);

        var wheelRadius : CGFloat = 100;
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
        
        
        var beginAngle : CGFloat = CGFloat(-M_PI - M_PI_4 * 0.5);
        var endAngle : CGFloat = CGFloat(M_PI_4 * 0.5);
        //beginAngle = 0.0;
        
        //width and height = radius * 2
        
        //Those percent values are relative to the size of the HUDImage
        var percentSpeedRadius : CGFloat = 0.4520;
        var percentSpeedWidth : CGFloat = 0.117;
        
        var percentRPMRadius : CGFloat = 0.7083;
        var percentRPMWidth : CGFloat = 0.0777;
        
        var percentFuelRadius : CGFloat = 0.904355;
        var percentFuelWidth : CGFloat = 0.0796872;
        
        var percentTemperatureRadius : CGFloat = 0.901355;
        var percentTemperatureWidth : CGFloat = 0.0786872;
        
        var percentMiddle : CGPoint = CGPointMake(0.5, 0.693);
        
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
        
        var percentageLabelWidth : CGFloat = 0.3;
        var labelWidth : CGFloat = percentageLabelWidth * imgWidth;
        
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
        
        buttonAccelerate = UIButton.buttonWithType(UIButtonType.System) as UIButton;
        buttonAccelerate.frame = CGRectMake(0, 0, self.view.frame.height * 0.5, self.view.frame.width);
        buttonAccelerate.setTitle("", forState: UIControlState.Normal);
        buttonAccelerate.addTarget(self, action: "onButtonAccelerate0", forControlEvents: UIControlEvents.TouchDown);
        buttonAccelerate.addTarget(self, action: "onButtonAccelerate1", forControlEvents: UIControlEvents.TouchUpInside);
        buttonAccelerate.backgroundColor = UIColor(red: 1.0, green: 1.0, blue: 1.0, alpha: 0.0);
        self.view.addSubview(buttonAccelerate);
        
        buttonBrake = UIButton.buttonWithType(UIButtonType.System) as UIButton;
        buttonBrake.frame = CGRectMake(self.view.frame.height * 0.5, 0, self.view.frame.height * 0.5, self.view.frame.width);
        buttonBrake.setTitle("", forState: UIControlState.Normal);
        buttonBrake.addTarget(self, action: "onButtonBrake0", forControlEvents: UIControlEvents.TouchDown);
        buttonBrake.addTarget(self, action: "onButtonBrake1", forControlEvents: UIControlEvents.TouchUpInside);
        buttonBrake.backgroundColor = UIColor(red: 1.0, green: 1.0, blue: 1.0, alpha: 0.0);
        self.view.addSubview(buttonBrake);
        
        self.searching = PSSearching(self.onConnected);
        
        self.connectionButton = UIButton.buttonWithType(UIButtonType.System) as UIButton;
        self.connectionButton.setTitle("Connect", forState: UIControlState.Normal);
        self.connectionButton.addTarget(self, action: Selector("onButtonConnect"), forControlEvents: UIControlEvents.TouchUpInside);
        self.connectionButton.frame = CGRectMake(10.0, 10.0, 100, 100);
        self.view.addSubview(self.connectionButton);
        
        cm = CMMotionManager();
        
        cm.deviceMotionUpdateInterval = 0.05;
        cm.startDeviceMotionUpdatesUsingReferenceFrame(CMAttitudeReferenceFrameXArbitraryZVertical, toQueue: NSOperationQueue.mainQueue(), withHandler:
        
        {
        (deviceMotion: CMDeviceMotion!, error: NSError!) in
        
        
            var gravity : PSVector = PSVector();
            gravity.x = deviceMotion.gravity.x;
            gravity.y = deviceMotion.gravity.y;
            gravity.z = deviceMotion.gravity.z;
            
            var upVec : PSVector = PSVector(x: 0, y: 1, z: 0);
        
            var angle : Double = acos(gravity.dot(upVec));
            if(self.interfaceOrientation == UIInterfaceOrientation.LandscapeLeft)
            {
                angle -= M_PI_2;
                angle *= -1;
                angle += M_PI_2;
            }
            var angleDeg : Double = angle * 180.0 / 3.145;
            var translatedAngle : Double = angleDeg - 90.0;
        
            if(self.session != nil)
            {
                self.session.currentData.steer = Float(translatedAngle / 90.0) * -1.0;
                self.session.sendCurrentData();
            }
            
            var animDuration : Double = 0.07;
                UIView.animateWithDuration(animDuration, animations: { () -> Void in
                    self.hudView.transform = CGAffineTransformConcat(CGAffineTransformMakeRotation(CGFloat(-(angle - (90.0 / (180.0 / 3.145))))), CGAffineTransformMakeScale(1.0, 1.0));
                    
                    if(self.session != nil)
                    {
                        self.speed.setProgress(CGFloat(self.session.carData.speed));
                        self.rpm.setProgress(CGFloat(self.session.carData.rpm));
                        self.labelSpeed.text = NSString(format: "%03d", Int(220.0 * self.session.carData.speed));
                        if(self.session.carData.gear == 0)
                        {
                            self.labelGear.text = NSString(format: "N");
                        }
                        else if(self.session.carData.gear == 7)
                        {
                            self.labelGear.text = NSString(format: "R");
                        }
                        else
                        {
                            self.labelGear.text = NSString(format: "%01d", Int(self.session.carData.gear));
                        }
                        self.labelDist.text = NSString(format: "%06d", Int(self.session.carData.distance));
                        self.fuel.setProgress(CGFloat(self.session.carData.fuel));
                        self.temperature.setProgress(CGFloat(self.session.carData.temperature));
                    }
                });
        });
    }
    func onConnected(toHost: String, onPort: UInt16)
    {
        if(self.session == nil)
        {
            self.connectionButton.hidden = true;
            self.session = PSSession(host: toHost, port: onPort, self.onDisconnected);
        }
        else
        {
            println("Tried to connect once more!");
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
    override func supportedInterfaceOrientations() -> Int
    {
        return Int(UIInterfaceOrientationMask.Landscape.rawValue);
    }
}