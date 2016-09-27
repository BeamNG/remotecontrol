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
    
    var _progress : CGFloat = 0.0;
    var progress : CGFloat {
        set(pr) {
            self._progress = pr;
            if(self._progress < 0.0)
            {
                self._progress = 0.0;
            }
            if(self._progress > 1.0)
            {
                self._progress = 1.0;
            }
            progressLayer.strokeEnd = self._progress;
            progressLayer2.strokeEnd = self._progress + 0.01;
        }
        get {
            return self._progress
        }
    }
    var angleBegin : CGFloat = 1.0;
    var angleEnd : CGFloat = 0.0;
    
    required init?(coder aDecoder: NSCoder)
    {
        super.init(coder: aDecoder);
    }
    override init(frame: CGRect)
    {
        super.init(frame: frame);
    }
    func draw(_ startAngle : CGFloat, endAngle : CGFloat, lineWidth : CGFloat, strokeColor: CGColor, clockwise : Bool)
    {
        if let testVar = self.layer.sublayers
        {
            if var unwrapped = self.layer.sublayers {
                unwrapped.removeAll(keepingCapacity: true);
            }
        }
        
        
        angleBegin = startAngle;
        angleEnd = endAngle;
        
        
        let progressLayerRadius : CGFloat = self.frame.width * 0.5;
        progressLayer2 = CAShapeLayer();
        progressLayer2.path = UIBezierPath(arcCenter: CGPoint(x: 0, y: 0), radius: progressLayerRadius, startAngle: startAngle, endAngle: endAngle, clockwise: clockwise).cgPath;
        progressLayer2.strokeColor = UIColor.white.cgColor;
        progressLayer2.fillColor = UIColor.clear.cgColor;
        progressLayer2.lineWidth = lineWidth * 1.05;
        progressLayer2.bounds = CGRect(x: -progressLayerRadius, y: -progressLayerRadius, width: progressLayerRadius * 2, height: progressLayerRadius * 2);
        progressLayer2.anchorPoint = CGPoint(x: 0, y: 0);
        progressLayer2.position = CGPoint(x: 0, y: 0);
        //progressLayer2.borderWidth = 1.0;
        
        self.layer.addSublayer(progressLayer2);
        
        progressLayer = CAShapeLayer();
        progressLayer.path = UIBezierPath(arcCenter: CGPoint(x: 0, y: 0), radius: progressLayerRadius, startAngle: startAngle, endAngle: endAngle, clockwise: clockwise).cgPath;
        progressLayer.strokeColor = strokeColor;
        progressLayer.fillColor = UIColor.clear.cgColor;
        progressLayer.lineWidth = lineWidth;
        progressLayer.bounds = CGRect(x: -progressLayerRadius, y: -progressLayerRadius, width: progressLayerRadius * 2, height: progressLayerRadius * 2);
        progressLayer.anchorPoint = CGPoint(x: 0, y: 0);
        progressLayer.position = CGPoint(x: 0, y: 0);
        //progressLayer.borderWidth = 1.0;
        
        self.layer.addSublayer(progressLayer);
        
        self.progress = 0.0;
    }
}
