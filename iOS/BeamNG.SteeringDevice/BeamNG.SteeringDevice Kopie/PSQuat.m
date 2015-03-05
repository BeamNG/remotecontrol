//
//  PSQuat.m
//  UDP_Test
//
//  Created by Pawel on 19.09.14.
//  Copyright (c) 2014 Pawel. All rights reserved.
//

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

@end