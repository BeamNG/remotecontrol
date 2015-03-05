//
//  PSProgressBar.swift
//  BeamNG.SteeringDevice
//
//  Created by Pawel Sulik on 10.10.14.
//  Copyright (c) 2014 28Apps. All rights reserved.
//

import UIKit
import CoreMotion
import QuartzCore

class PSProgressBar : UIView
{
    var progressLayer : CAShapeLayer!;
    var progressLayer2 : CAShapeLayer!;
    
    var progress : CGFloat = 0.0;
    var angleBegin : CGFloat = 1.0;
    var angleEnd : CGFloat = 0.0;
    
    required init(coder aDecoder: NSCoder)
    {
        super.init(coder: aDecoder);
    }
    override init(frame: CGRect)
    {
        super.init(frame: frame);
    }
    func draw(startAngle : CGFloat, endAngle : CGFloat, lineWidth : CGFloat, strokeColor: CGColor, clockwise : Bool)
    {
        if let testVar = self.layer.sublayers
        {
            self.layer.sublayers.removeAll(keepCapacity: true);
        }
        
        
        angleBegin = startAngle;
        angleEnd = endAngle;
        
        
        var progressLayerRadius : CGFloat = self.frame.width * 0.5;
        progressLayer2 = CAShapeLayer();
        progressLayer2.path = UIBezierPath(arcCenter: CGPointMake(0, 0), radius: progressLayerRadius, startAngle: startAngle, endAngle: endAngle, clockwise: clockwise).CGPath;
        progressLayer2.strokeColor = UIColor.whiteColor().CGColor;
        progressLayer2.fillColor = UIColor.clearColor().CGColor;
        progressLayer2.lineWidth = lineWidth * 1.05;
        progressLayer2.bounds = CGRectMake(-progressLayerRadius, -progressLayerRadius, progressLayerRadius * 2, progressLayerRadius * 2);
        progressLayer2.anchorPoint = CGPointMake(0, 0);
        progressLayer2.position = CGPointMake(0, 0);
        //progressLayer2.borderWidth = 1.0;
        
        self.layer.addSublayer(progressLayer2);
        
        progressLayer = CAShapeLayer();
        progressLayer.path = UIBezierPath(arcCenter: CGPointMake(0, 0), radius: progressLayerRadius, startAngle: startAngle, endAngle: endAngle, clockwise: clockwise).CGPath;
        progressLayer.strokeColor = strokeColor;
        progressLayer.fillColor = UIColor.clearColor().CGColor;
        progressLayer.lineWidth = lineWidth;
        progressLayer.bounds = CGRectMake(-progressLayerRadius, -progressLayerRadius, progressLayerRadius * 2, progressLayerRadius * 2);
        progressLayer.anchorPoint = CGPointMake(0, 0);
        progressLayer.position = CGPointMake(0, 0);
        //progressLayer.borderWidth = 1.0;
        
        self.layer.addSublayer(progressLayer);
        
        setProgress(self.progress);
    }
    func setProgress(pr: CGFloat)
    {
        progress = pr;
        if(progress < 0.0)
        {
            progress = 0.0;
        }
        if(progress > 1.0)
        {
            progress = 1.0;
        }
        progressLayer.strokeEnd = progress;
        progressLayer2.strokeEnd = progress + 0.01;
    }
}