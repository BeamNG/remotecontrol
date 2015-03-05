//
//  PSNetUtil.h
//  BeamNG.SteeringDevice
//
//  Created by Pawel on 09.10.14.
//  Copyright (c) 2014 28Apps. All rights reserved.
//

#ifndef BeamNG_SteeringDevice_PSNetUtil_h
#define BeamNG_SteeringDevice_PSNetUtil_h


@interface PSNetUtil : NSObject

+ (NSString *) localIPAddress;
+ (NSString *) netmask;
+ (NSString *) broadcastAddress;

@end

#endif


/*
 #ifndef UDP_Test_PSQuat_h
 #define UDP_Test_PSQuat_h
 
 @interface PSVector : NSObject
 
 - (instancetype)initWithX:(double)X Y:(double)Y Z:(double)Z;
 
 - (PSVector*)normalize;
 - (double)dot:(PSVector*)vec0;
 - (double)magnitude;
 
 @property double x;
 @property double y;
 @property double z;
 @end
 
 @interface PSQuat : NSObject
 
 
 - (PSVector*)vecQuatMul:(double)x y:(double)y z:(double)z w:(double)w vec:(PSVector*)vec;
 @end
 #endif

*/