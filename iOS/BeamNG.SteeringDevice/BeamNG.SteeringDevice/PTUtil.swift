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
    class func lerp(_ t: Double, a: Double, b: Double) -> Double
    {
        return (b-a)*t + a;
    }
    class func clamp01(_ val: Double) -> Double
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
    class func clamp(_ val: Double, min: Double, max: Double) -> Double
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
    class func delay(_ delay:Double, closure:@escaping ()->())
    {
        DispatchQueue.main.asyncAfter(
            deadline: DispatchTime.now() + Double(Int64(delay * Double(NSEC_PER_SEC))) / Double(NSEC_PER_SEC), execute: closure)
    }
    
    class func dispatch(_ delay:Double, closure:@escaping ()->(), thread: DispatchQueue!)
    {
        var threadToRun = thread;
        if(threadToRun == nil)
        {
            threadToRun = DispatchQueue.main;
        }
        threadToRun?.asyncAfter(
            deadline: DispatchTime.now() + Double(Int64(delay * Double(NSEC_PER_SEC))) / Double(NSEC_PER_SEC), execute: closure);
    }
    
    class func saveToDocuments(_ filename: String, content: String)->Bool
    {
        let dirs : [String]? = NSSearchPathForDirectoriesInDomains(FileManager.SearchPathDirectory.documentDirectory, FileManager.SearchPathDomainMask.allDomainsMask, true)
        
        /*for(var i = 0; i < dirs!.count; i++)
        {
            println(dirs![i]);
        }*/
        
        if (dirs != nil)
        {
            let directories:[String] = dirs!;
            let dir = directories[0]; //documents directory
            let path = (dir as NSString).appendingPathComponent(filename);
            do {
                //let text = "some text";
            
                //writing
                try content.write(toFile: path, atomically: false, encoding: String.Encoding.utf8)
            } catch _ {
            };
            
            //reading
            //let text2 = String.stringWithContentsOfFile(path, encoding: NSUTF8StringEncoding, error: nil)
            //println(text2);
            return true;
        }
        return false;
    }
    class func loadFromDocuments(_ filename: String)->String?
    {
        let dirs : [String]? = NSSearchPathForDirectoriesInDomains(FileManager.SearchPathDirectory.documentDirectory, FileManager.SearchPathDomainMask.allDomainsMask, true)
        
        /*for(var i = 0; i < dirs!.count; i++)
        {
            println(dirs![i]);
        }*/
        
        if (dirs != nil)
        {
            let directories:[String] = dirs!;
            let dir = directories[0]; //documents directory
            let path = (dir as NSString).appendingPathComponent(filename);
            //let text = "some text";
            
            //writing
            //content.writeToFile(path, atomically: false, encoding: NSUTF8StringEncoding, error: nil);
            
            //reading
            //let text2 = String.stringWithContentsOfFile(path, encoding: NSUTF8StringEncoding, error: nil)
            let text2 = try? NSString(contentsOfFile: path, encoding: String.Encoding.utf8.rawValue);
            //let text2 = String.stringWithContentsOfFile(path, encoding: NSUTF8StringEncoding, error: nil)
            return text2 as? String;
            //println(text2);
        }
        return nil;
    }
}
