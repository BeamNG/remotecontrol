//
//  PTUtil.swift
//  BeamNG.SteeringDevice
//
//  Created by Pawel Sulik on 01.09.14.
//  Copyright (c) 2014 28Apps. All rights reserved.
//

import Foundation

class PTUtil
{
    class func lerp(t: Double, a: Double, b: Double) -> Double
    {
        return (b-a)*t + a;
    }
    class func clamp01(val: Double) -> Double
    {
        var retVal = val;
        if(val < 0.0)
        {
            retVal = 0.0;
        }
        else if(val > 1.0)
        {
            retVal = 1.0;
        }
        return retVal;
    }
    class func clamp(val: Double, min: Double, max: Double) -> Double
    {
        var retVal = val;
        if(val < min)
        {
            retVal = min;
        }
        else if(val > max)
        {
            retVal = max;
        }
        return retVal;
    }
    class func delay(delay:Double, closure:()->())
    {
        dispatch_after(
            dispatch_time(
                DISPATCH_TIME_NOW,
                Int64(delay * Double(NSEC_PER_SEC))
            ),
            dispatch_get_main_queue(), closure)
    }
    
    class func dispatch(delay:Double, closure:()->(), thread: dispatch_queue_t!)
    {
        var threadToRun = thread;
        if(threadToRun == nil)
        {
            threadToRun = dispatch_get_main_queue();
        }
        dispatch_after(
            dispatch_time(
                DISPATCH_TIME_NOW,
                Int64(delay * Double(NSEC_PER_SEC))
            ),
            threadToRun, closure);
    }
    
    class func saveToDocuments(filename: String, content: String)->Bool
    {
        let dirs : [String]? = NSSearchPathForDirectoriesInDomains(NSSearchPathDirectory.DocumentDirectory, NSSearchPathDomainMask.AllDomainsMask, true) as? [String]
        
        /*for(var i = 0; i < dirs!.count; i++)
        {
            println(dirs![i]);
        }*/
        
        if (dirs != nil)
        {
            let directories:[String] = dirs!;
            let dir = directories[0]; //documents directory
            let path = dir.stringByAppendingPathComponent(filename);
            //let text = "some text";
            
            //writing
            content.writeToFile(path, atomically: false, encoding: NSUTF8StringEncoding, error: nil);
            
            //reading
            //let text2 = String.stringWithContentsOfFile(path, encoding: NSUTF8StringEncoding, error: nil)
            //println(text2);
            return true;
        }
        return false;
    }
    class func loadFromDocuments(filename: String)->String?
    {
        let dirs : [String]? = NSSearchPathForDirectoriesInDomains(NSSearchPathDirectory.DocumentDirectory, NSSearchPathDomainMask.AllDomainsMask, true) as? [String]
        
        /*for(var i = 0; i < dirs!.count; i++)
        {
            println(dirs![i]);
        }*/
        
        if (dirs != nil)
        {
            let directories:[String] = dirs!;
            let dir = directories[0]; //documents directory
            let path = dir.stringByAppendingPathComponent(filename);
            //let text = "some text";
            
            //writing
            //content.writeToFile(path, atomically: false, encoding: NSUTF8StringEncoding, error: nil);
            
            //reading
            //let text2 = String.stringWithContentsOfFile(path, encoding: NSUTF8StringEncoding, error: nil)
            let text2 = NSString(contentsOfFile: path, encoding: NSUTF8StringEncoding, error: nil);
            //let text2 = String.stringWithContentsOfFile(path, encoding: NSUTF8StringEncoding, error: nil)
            return text2;
            //println(text2);
        }
        return nil;
    }
}