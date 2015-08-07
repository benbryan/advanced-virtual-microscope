/*
 * JOCL - Java bindings for OpenCL
 *
 * Copyright 2009 Marco Hutter - http://www.jocl.org/
 */

// A simple image convolution kernel

__kernel void simpleConvolution(
        __global uchar *input,
        __global float *mask,
        __global float *output,
        const int2 imageSize,
        const int2 maskSize,
        const int2 maskOrigin){
    int gx = get_global_id(0);
    int gy = get_global_id(1);

    if (    gx >= maskOrigin.x && gx < imageSize.x - (maskSize.x-maskOrigin.x-1) &&
            gy >= maskOrigin.y && gy < imageSize.y - (maskSize.y-maskOrigin.y-1)  ){
        float sum = (float)0;
        for(int mx=0; mx<maskSize.x; mx++) {
            for(int my=0; my<maskSize.x; my++) {
                int mi = mul24(my, maskSize.x) + mx;
                int ix = gx - maskOrigin.x + mx;
                int iy = gy - maskOrigin.y + my;
                int i = mul24(iy, imageSize.x) + ix;
                sum += convert_float(input[i]) * mask[mi];
            }
        }
        output[mul24(gy, imageSize.x)+gx] = sum;
    } else {
        if (gx >= 0 && gx < imageSize.x && gy >= 0 && gy < imageSize.y) {
            output[mul24(gy, imageSize.x)+gx] = (float)0;
        }
    }
}


