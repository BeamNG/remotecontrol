//
//  PSNetUtil.h
//  BeamNG.SteeringDevice
//
//  Created by Pawel Sulik on 09.10.14.
//  Copyright (c) 2014 28Apps. All rights reserved.
//

//This is a helper class for reading some network data

#ifndef BeamNG_SteeringDevice_PSNetUtil_h
#define BeamNG_SteeringDevice_PSNetUtil_h


@interface PSNetUtil : NSObject

+ (NSString *) localIPAddress;
+ (NSString *) netmask;
+ (NSString *) broadcastAddress;

@end

#endif