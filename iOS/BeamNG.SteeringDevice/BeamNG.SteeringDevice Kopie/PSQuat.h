//
//  PSQuat.h
//  UDP_Test
//
//  Created by Pawel on 19.09.14.
//  Copyright (c) 2014 Pawel. All rights reserved.
//

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
