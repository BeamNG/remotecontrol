//
//  PSNetUtil.m
//  BeamNG.SteeringDevice
//
//  Created by Pawel on 09.10.14.
//  Copyright (c) 2014 28Apps. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <ifaddrs.h>
#include <arpa/inet.h>

@implementation PSNetUtil : NSObject 

+ (NSString *) localIPAddress
{
    NSString *address = @"error";
    struct ifaddrs *interfaces = NULL;
    struct ifaddrs *temp_addr = NULL;
    int success = 0;
    
    // retrieve the current interfaces - returns 0 on success
    success = getifaddrs(&interfaces);
    
    if (success == 0)
    {
        temp_addr = interfaces;
        
        while(temp_addr != NULL)
        {
            // check if interface is en0 which is the wifi connection on the iPhone
            if(temp_addr->ifa_addr->sa_family == AF_INET)
            {
                if([[NSString stringWithUTF8String:temp_addr->ifa_name] isEqualToString:@"en0"])
                {
                    address = [NSString stringWithUTF8String:inet_ntoa(((struct sockaddr_in *)temp_addr->ifa_addr)->sin_addr)];
                }
            }
            
            temp_addr = temp_addr->ifa_next;
        }
    }
    
    freeifaddrs(interfaces);
    
    return address;
}

+ (NSString *) netmask
{
    NSString *netmask = @"error";
    struct ifaddrs *interfaces = NULL;
    struct ifaddrs *temp_addr = NULL;
    int success = 0;
    
    // retrieve the current interfaces - returns 0 on success
    success = getifaddrs(&interfaces);
    
    if (success == 0)
    {
        temp_addr = interfaces;
        
        while(temp_addr != NULL)
        {
            // check if interface is en0 which is the wifi connection on the iPhone
            if(temp_addr->ifa_addr->sa_family == AF_INET)
            {
                if([[NSString stringWithUTF8String:temp_addr->ifa_name] isEqualToString:@"en0"])
                {
                    netmask = [NSString stringWithUTF8String:inet_ntoa(((struct sockaddr_in *)temp_addr->ifa_netmask)->sin_addr)];
                    NSLog(@"%d", ((struct sockaddr_in *)temp_addr->ifa_netmask)->sin_addr.s_addr);
                }
            }
            
            temp_addr = temp_addr->ifa_next;
        }
    }
    
    freeifaddrs(interfaces);
    
    return netmask;
}

+ (NSString *) broadcastAddress
{
    NSString *broadcastAddr = @"error";
    struct ifaddrs *interfaces = NULL;
    struct ifaddrs *temp_addr = NULL;
    int success = 0;
    
    // retrieve the current interfaces - returns 0 on success
    success = getifaddrs(&interfaces);
    
    if (success == 0)
    {
        temp_addr = interfaces;
        
        while(temp_addr != NULL)
        {
            // check if interface is en0 which is the wifi connection on the iPhone
            if(temp_addr->ifa_addr->sa_family == AF_INET)
            {
                if([[NSString stringWithUTF8String:temp_addr->ifa_name] isEqualToString:@"en0"])
                {
                    broadcastAddr = [NSString stringWithUTF8String:inet_ntoa(((struct sockaddr_in *)temp_addr->ifa_dstaddr)->sin_addr)];
                }
            }
            
            temp_addr = temp_addr->ifa_next;
        }
    }
    
    freeifaddrs(interfaces);
    
    return broadcastAddr;
}
@end

/*

 
 #import <Foundation/Foundation.h>
 #import <CoreMotion/CoreMotion.h>
 #import <GLKit/GLKit.h>
 #import "PSQuat.h"
 
 @implementation PSVector
 - (instancetype)initWithX:(double)X Y:(double)Y Z:(double)Z
 {
 self.x = X;
 self.y = Y;
 self.z = Z;
 
 return self;
 }
 - (PSVector*)normalize
 {
 PSVector * retVal = [[PSVector alloc]init];
 
 retVal.x = self.x;
 retVal.y = self.y;
 retVal.z = self.z;
 
 retVal.x /= self.magnitude;
 retVal.y /= self.magnitude;
 retVal.z /= self.magnitude;
 
 return retVal;
 }
 - (double)dot:(PSVector*)vec0
 {
 return (vec0.x * self.x) + (vec0.y * self.y) + (vec0.z * self.z);
 }
 - (double)magnitude
 {
 return sqrt((self.x * self.x) + (self.y * self.y) + (self.z * self.z));
 }
 @end
 
 @implementation PSQuat
 
 - (PSVector*)vecQuatMul:(double)x y:(double)y z:(double)z w:(double)w vec:(PSVector*)vec
 {
 PSVector* retVal = [[PSVector alloc]init];
 retVal.x = 0;
 
 GLKVector3 result = GLKQuaternionRotateVector3(GLKQuaternionMake(x, y, z, w), GLKVector3Make(vec.x, vec.y, vec.z));
 
 retVal.x = result.x;
 retVal.y = result.y;
 retVal.z = result.z;
 
 return retVal;
 }
 
 @end*/