//
//  PSSessionViewController.swift
//  BeamNG.SteeringDevice
//
//  Created by Pawel on 09.10.14.
//  Copyright (c) 2014 28Apps. All rights reserved.
//

import UIKit
import QuartzCore
import CoreMotion

class PSSessionViewController: UIViewController
{
    @IBOutlet weak var indicator: UIActivityIndicatorView!;
    var cm : CMMotionManager!;
    
    
    var steeringWheelLayer : CAShapeLayer!;
    var steeringWheelView : UIView!;
    var steeringWheelLabel : UILabel!;
    
    var session : PSSession!;
    
    override func viewDidLoad()
    {
        super.viewDidLoad();
        
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
        
        steeringWheelLabel = UILabel(frame: CGRectMake(0, 0, 2 * wheelRadius, 2 * wheelRadius))
        steeringWheelView.addSubview(steeringWheelLabel);
        
        self.view.addSubview(steeringWheelView);

        
        
        cm = CMMotionManager();
        cm.deviceMotionUpdateInterval = 0.050;
        //cm.startDeviceMotionUpdatesToQueue(NSOperationQueue.mainQueue(), withHandler:
        cm.startDeviceMotionUpdatesUsingReferenceFrame(CMAttitudeReferenceFrameXArbitraryZVertical, toQueue: NSOperationQueue.mainQueue(), withHandler:
        
        {
            (deviceMotion: CMDeviceMotion!, error: NSError!) in
        
            var gravity : PSVector = PSVector();
            gravity.x = deviceMotion.gravity.x;
            gravity.y = deviceMotion.gravity.y;
            gravity.z = deviceMotion.gravity.z;
        
            var upVec : PSVector = PSVector(x: 0, y: 1, z: 0);
        
            var angle : Double = acos(gravity.dot(upVec));
            var angleDeg : Double = angle * 180.0 / 3.145;
            var translatedAngle : Double = angleDeg - 90.0;
            self.steeringWheelLabel.text = String(format: "%.1f", translatedAngle);
            self.steeringWheelView.transform = CGAffineTransformMakeRotation(CGFloat(-(angle - (90.0 / (180.0 / 3.145)))));
            
            self.session.steerData.steerAngle = Float(translatedAngle);
            self.session.sendCurrentData();
        });
        
        
        
    }
    
    override func didReceiveMemoryWarning()
    {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    
    override func supportedInterfaceOrientations() -> Int
    {
        return Int(UIInterfaceOrientationMask.LandscapeRight.toRaw());
    }
    override func preferredInterfaceOrientationForPresentation() -> UIInterfaceOrientation {
        return UIInterfaceOrientation.LandscapeRight;
    }
    override func shouldAutorotate() -> Bool
    {
        return false;
    }
}