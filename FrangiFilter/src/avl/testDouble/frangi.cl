

#pragma OPENCL EXTENSION cl_khr_fp64: enable

__kernel void frangi(
        __global uchar *input,
        __global double *maskXX,
        __global double *maskXY,
        __global double *maskYY,
        __global double *out,
        const int2 imageSize,
        const int2 maskSize,
        const int2 maskOrigin,
        double2 sigmaSquared){
            
    int gx = get_global_id(0);
    int gy = get_global_id(1);

    if (    gx >= maskOrigin.x && 
            gx < imageSize.x - (maskSize.x-maskOrigin.x-1) && 
            gy >= maskOrigin.y && 
            gy < imageSize.y - (maskSize.y-maskOrigin.y-1)  ){
        double sumXX = 0,  sumXY = 0,  sumYY = 0;
        for(int my = 0; my < maskSize.y; my++) {
              for(int mx = 0; mx < maskSize.x; mx++) {  
                int mi = my* maskSize.x + mx;
                int ix = gx - maskOrigin.x + mx;
                int iy = gy - maskOrigin.y + my;
                int i = iy * imageSize.x + ix;
                sumXX += input[i] * maskXX[mi];
                sumXY += input[i] * maskXY[mi];
                sumYY += input[i] * maskYY[mi];
            }
        }
        
        double dXX = sumXX*sigmaSquared.x;
        double dXY = sumXY*sigmaSquared.x;
        double dYY = sumYY*sigmaSquared.x;
        
        double T = dXX+dYY;
        double D = dXX*dYY-dXY*dXY;
        double A = T * 0.5;
        double B = sqrt((T*T)/4-D);
        double L1 = A + B;
        double L2 = A - B;
        
        int idx = mul24(gy, imageSize.x)+gx;        
        out[idx] = L1+L2;
    } else {
        if (gx >= 0 && gx < imageSize.x && gy >= 0 && gy < imageSize.y) {
            out[mul24(gy, imageSize.x)+gx] = (double)0;
        }
    }
}


