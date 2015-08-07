/*
 * JOCL - Java bindings for OpenCL
 * 
 * Copyright 2009 Marco Hutter - http://www.jocl.org/
 */

package avl.testDouble;

import java.awt.image.BufferedImage;
import java.io.*;
import org.jocl.*;
import static org.jocl.CL.*;

public class JOCL_FrangiFilter {
    
    private static String readKernel(InputStream kernelInputStream) {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(kernelInputStream ));
            StringBuffer sb = new StringBuffer();
            String line = null;
            while (true) {
                line = br.readLine();
                if (line == null) {
                    break;
                }
                sb.append(line).append("\n");
            }
            return sb.toString();
        }
        catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
            return null;
        }
    }
 
    private cl_kernel clKernel;
    private cl_context context;
    private cl_command_queue commandQueue;
    
    public JOCL_FrangiFilter () {
        // The platform, device type and device number
        // that will be used
        final int platformIndex = 0;
        final long deviceType = CL_DEVICE_TYPE_ALL;
        final int deviceIndex = 0;

        // Enable exceptions and subsequently omit error checks in this sample
        CL.setExceptionsEnabled(true);

        // Obtain the number of platforms
        int numPlatformsArray[] = new int[1];
        clGetPlatformIDs(0, null, numPlatformsArray);
        int numPlatforms = numPlatformsArray[0];

        // Obtain a platform ID
        cl_platform_id platforms[] = new cl_platform_id[numPlatforms];
        clGetPlatformIDs(platforms.length, platforms, null);
        cl_platform_id platform = platforms[platformIndex];

        // Initialize the context properties
        cl_context_properties contextProperties = new cl_context_properties();
        contextProperties.addProperty(CL_CONTEXT_PLATFORM, platform);
        
        // Obtain the number of devices for the platform
        int numDevicesArray[] = new int[1];
        clGetDeviceIDs(platform, deviceType, 0, null, numDevicesArray);
        int numDevices = numDevicesArray[0];
        
        // Obtain a device ID 
        cl_device_id devices[] = new cl_device_id[numDevices];
        clGetDeviceIDs(platform, deviceType, numDevices, devices, null);
        cl_device_id device = devices[deviceIndex];

        // Create a context for the selected device
        context = clCreateContext( contextProperties, 1, new cl_device_id[]{device}, null, null, null);
        
        // Create a command-queue for the selected device
        commandQueue = clCreateCommandQueue(context, device, 0, null);
        
        loadAndCompileKernel("frangi.cl");
    }
    
    private void loadAndCompileKernel(String kernelName){
        // Create the OpenCL kernel from the program
        InputStream kernelInputStream = getClass().getResourceAsStream(kernelName);
        String source = readKernel(kernelInputStream);
        cl_program program = clCreateProgramWithSource(context, 1, 
            new String[]{ source }, null, null);
        String compileOptions = "-cl-mad-enable";
        clBuildProgram(program, 0, null, compileOptions, null, null);
        clKernel = clCreateKernel(program, "frangi", null);
        clReleaseProgram(program);    
    }

    @Override
    protected void finalize() throws Throwable {
        clReleaseKernel(clKernel);
        clReleaseCommandQueue(commandQueue);
        clReleaseContext(context);          
        super.finalize();
    }
    
    private cl_mem loadKernel(KernelDouble kernel){
        double kernelData[] = kernel.getKernelData(null);
        cl_mem kernelMem = clCreateBuffer(context, CL_MEM_READ_ONLY, 
            kernelData.length * Sizeof.cl_double, null, null);
        clEnqueueWriteBuffer(commandQueue, kernelMem, 
            true, 0, kernelData.length * Sizeof.cl_double, 
            Pointer.to(kernelData), 0, null, null);
        return kernelMem;
    }
    
    public BufferedImage filter(BufferedImage inputImage, double sigmas[]){
        byte[] b = ArrayOp.getColorChannel(inputImage, 0);
        double dataDouble[] = filter(b, inputImage.getWidth(), inputImage.getHeight(), sigmas);
        byte data[] = new byte[dataDouble.length];
        ArrayOp.mat2gray(dataDouble, data);
        OtsuThresholder ot = new OtsuThresholder();
        data = ot.doThreshold(data);
        BufferedImage outputImage = ArrayOp.imagescGray(inputImage.getWidth(), inputImage.getHeight(), data);
        return outputImage;
    }
    
    public double[] filter(byte[] img, int imgWidth, int imgHeight,  double sigmas[]){
        Gaussian2ndDerivativeKernelSet kernelSets[] = new Gaussian2ndDerivativeKernelSet[sigmas.length];
        for (int i = 0; i < sigmas.length; i++){
            kernelSets[i] = Gaussian2ndDerivativeKernelSet.generateKernels(sigmas[i]);
        }
                
        double dataAll[][] = new double[sigmas.length][];
        for (int i = 0; i < sigmas.length; i++){
            long before = System.nanoTime();
            dataAll[i] = filterSigma(img, imgWidth, imgHeight, kernelSets[i]);
            System.out.println( "SigmaFilter Time: "+String.format("%.2f", (System.nanoTime()-before)/1e6)+" ms");
        }
        for (int j = 0; j < dataAll.length; j++){
            for (int i = 0; i < dataAll[0].length; i++){
                dataAll[0][i] += dataAll[j][i];
            } 
        }
        return dataAll[0];
    }
    
    private static long cal(long groupSize, long globalSize) {
        return (long) (groupSize*Math.ceil((double)globalSize/(double)groupSize));
//        long r = globalSize % groupSize;
//        if(r == 0) {
//            return globalSize;
//        } else {
//            return globalSize + groupSize - r;
//        }
    }
        
    private double[] filterSigma(byte[] dataSrc, int imgWidth, int imgHeight,  Gaussian2ndDerivativeKernelSet kernelSet) {
        cl_mem inputImageMem = clCreateBuffer(context, CL_MEM_READ_ONLY | CL_MEM_USE_HOST_PTR, dataSrc.length * Sizeof.cl_uchar, Pointer.to(dataSrc), null);
        cl_mem outDataMem = clCreateBuffer(context, CL_MEM_WRITE_ONLY, imgWidth * imgHeight * Sizeof.cl_double, null, null);
        
        //load kernels;
        cl_mem kernelMemXX = loadKernel(kernelSet.xx);
        cl_mem kernelMemXY = loadKernel(kernelSet.xy);
        cl_mem kernelMemYY = loadKernel(kernelSet.yy);
        
        // Set work sizes and arguments, and execute the kernel
        int kernelSizeX = kernelSet.getWidth();
        int kernelSizeY = kernelSet.getHeight();
        int kernelOriginX = kernelSet.getXOrigin();
        int kernelOriginY = kernelSet.getYOrigin();

        long localWorkSize[] = {16,16};
        long globalWorkSize[] = {cal(localWorkSize[0], imgWidth), cal(localWorkSize[1], imgHeight)};

        int imageSize[] = { imgWidth, imgHeight };
        int kernelSize[] = { kernelSizeX, kernelSizeY };
        int kernelOrigin[] = { kernelOriginX, kernelOriginY };
        double sigmaSquared[] = {Math.pow(kernelSet.getSigma(),2),0};

        clSetKernelArg(clKernel, 0, Sizeof.cl_mem, Pointer.to(inputImageMem));
        clSetKernelArg(clKernel, 1, Sizeof.cl_mem, Pointer.to(kernelMemXX));
        clSetKernelArg(clKernel, 2, Sizeof.cl_mem, Pointer.to(kernelMemXY));
        clSetKernelArg(clKernel, 3, Sizeof.cl_mem, Pointer.to(kernelMemYY));        
        clSetKernelArg(clKernel, 4, Sizeof.cl_mem, Pointer.to(outDataMem));
        clSetKernelArg(clKernel, 5, Sizeof.cl_int2, Pointer.to(imageSize));
        clSetKernelArg(clKernel, 6, Sizeof.cl_int2, Pointer.to(kernelSize));
        clSetKernelArg(clKernel, 7, Sizeof.cl_int2, Pointer.to(kernelOrigin));
        clSetKernelArg(clKernel, 8, Sizeof.cl_double2, Pointer.to(sigmaSquared));
        
        int result = clEnqueueNDRangeKernel(commandQueue, clKernel, 2, null, globalWorkSize, localWorkSize, 0, null, null);
        double outData[] = new double[imgWidth * imgHeight];
        clEnqueueReadBuffer(commandQueue, outDataMem, CL_TRUE, 0, outData.length * Sizeof.cl_double, Pointer.to(outData), 0, null, null);
        
        clReleaseMemObject(kernelMemXX);
        clReleaseMemObject(kernelMemXY);
        clReleaseMemObject(kernelMemYY);

        clReleaseMemObject(inputImageMem);
        clReleaseMemObject(outDataMem);
 
        return outData;
    }
    
    
}
